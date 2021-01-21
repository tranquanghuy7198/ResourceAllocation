import java.util.ArrayList;

public class Utils {
    private static final double wDuration = 0.00005249;
    private static final double wAssignment = 0.76447751;
    private static final double wCost = 0.23547;
    public static final double FILTER_RATE = 0.5;        // 50% of individuals will be removed after each iteration
    public static final int INDIVIDUALS_PER_POPULATION = 200;
    public static final int MAX_LOOPS = 2000000;
    public static final int STABLE_LOOPS = 10;
    public static final double STABLE_THRESHOLD = 0.0001;

    private static String formatTime(int seconds) {
        int[] time = new int[5];
        int[] timeLength = new int[]{60, 60, 24, 7};
        String[] timeUnits = new String[]{" seconds ", " minutes ", " hours ", " days ", " weeks "};
        time[0] = seconds;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (time[i] > timeLength[i]) {
                time[i + 1] = time[i] / timeLength[i];
                time[i] %= timeLength[i];
            }
        }
        for (int i = 4; i >= 1; i--)
            if (time[i] > 0)
                result.append(time[i]).append(timeUnits[i]);
        return result.toString();
    }

    public static double fitness(int binaryExpression, int taskId, boolean readFromDB) {
        double[] start = new double[Database.getNumTask()];     // t_i^start
        double[] duration = new double[Database.getNumTask()];      // t_i^duration
        double[] finish = new double[Database.getNumTask()];    // t_i^finish
        double[] humanWorkingTime = new double[Database.getNumHuman()];     // h_i^assigned
        double[] machineWorkingTime = new double[Database.getNumMachine()];     // m_i^assigned

        /* Collect the allocation from all tasks */
        int[] humanAllocation = new int[Database.getNumTask()];     // humanAllocation[i] is the human allocation of task number #(i+1)
        int[] machineAllocation = new int[Database.getNumTask()];   // machineAllocation[i] is the machine allocation of task number #(i+1)
        for (int i = 1; i <= Database.getNumTask(); i++) {
            int allocation;
            if (i != taskId || readFromDB)
                allocation = Database.getBestAllocation(i).getBinaryExpression();
            else
                allocation = binaryExpression;
            humanAllocation[i - 1] = allocation >> Database.getMachineBitLength();
            machineAllocation[i - 1] = allocation ^ (humanAllocation[i - 1] << Database.getMachineBitLength());
        }

        /* Final fitness result */
        double fitnessValue = wDuration * fDuration(humanAllocation, machineAllocation, start, duration, finish)
                + wAssignment * fAssignment(humanAllocation, machineAllocation, start, duration, finish, humanWorkingTime, machineWorkingTime)
                + wCost * fCost(humanWorkingTime, machineWorkingTime);
        if (readFromDB)
            printFinalResult(start, finish, fitnessValue);
        return fitnessValue;
    }

    private static double fDuration(int[] humanAllocation, int[] machineAllocation, double[] start, double[] duration, double[] finish) {
        ArrayList<ArrayList<Integer>> adj = Database.getDependencyGraph();
        double[][] lexp = Database.getLEXP();
        double[][] sreq = Database.getSREQ();
        int[] numDependence = new int[Database.getNumTask()];
        double projectFinish = 0;

        /* Compute the duration of every task */
        for (int i = 0; i < Database.getNumTask(); i++) {
            start[i] = 0;
            double humanAffection = 0, machineAffection = 1;
            for (int j = 0; j < Database.getNumSkill(); j++) {
                if (lexp[humanAllocation[i] - 1][j] != 0)
                    humanAffection = Math.max(humanAffection, sreq[i][j] / lexp[humanAllocation[i] - 1][j]);
                else {
                    humanAffection = Math.sqrt(Double.MAX_VALUE) - 1;
                    break;
                }
            }
            if (Database.getMREQ(i + 1) != 0) {
                if (Database.getProductivity(machineAllocation[i]) != 0)
                    machineAffection = Database.getMREQ(i + 1) / Database.getProductivity(machineAllocation[i]);
                else
                    machineAffection = Math.sqrt(Double.MAX_VALUE) - 1;
            }
            duration[i] = Database.getEstimatedTime(i + 1) * humanAffection * machineAffection;
        }

        /* Init numDependence */
        for (int i = 0; i < Database.getNumTask(); i++)
            for (int neighbor : adj.get(i))
                numDependence[neighbor]++;

        /* Execute tasks based on their dependency relationship */
        while (true) {
            boolean done = true;
            for (int i = 0; i < Database.getNumTask(); i++) {
                if (numDependence[i] == 0) {
                    done = false;
                    finish[i] = start[i] + duration[i];
                    projectFinish = Math.max(projectFinish, finish[i]);
                    numDependence[i] = -1;
                    for (int j : adj.get(i)) {
                        numDependence[j]--;
                        start[j] = Math.max(start[j], finish[i]);
                    }
                }
            }
            if (done) break;
        }
        return projectFinish;
    }

    private static double fAssignment(int[] humanAllocation, int[] machineAllocation, double[] start, double[] duration, double[] finish, double[] humanWorkingTime, double[] machineWorkingTime) {
        double[] humanConflict = new double[Database.getNumHuman()];    // h_i^conflict
        double[] machineConflict = new double[Database.getNumMachine()];    // m_i^conflict
        double totalHumanConflict = 0;
        double totalMachineConflict = 0;

        for (int u = 0; u < Database.getNumTask() - 1; u++)
            for (int v = u + 1; v < Database.getNumTask(); v++) {
                double taskConflict = Math.max(0, Math.min(finish[u], finish[v]) - Math.max(start[u], start[v]));
                if (humanAllocation[u] == humanAllocation[v])
                    humanConflict[humanAllocation[u] - 1] += taskConflict;
                if (machineAllocation[u] == machineAllocation[v] && machineAllocation[u] > 0)
                    machineConflict[machineAllocation[u] - 1] += taskConflict;
            }
        for (int u = 0; u < Database.getNumTask(); u++) {
            humanWorkingTime[humanAllocation[u] - 1] += duration[u];
            if (machineAllocation[u] > 0)
                machineWorkingTime[machineAllocation[u] - 1] += duration[u];
        }
        for (int i = 0; i < Database.getNumHuman(); i++)
            if (humanWorkingTime[i] != 0)
                totalHumanConflict += (humanConflict[i] / humanWorkingTime[i]);
        for (int i = 0; i < Database.getNumMachine(); i++)
            if (machineWorkingTime[i] != 0)
                totalMachineConflict += (machineConflict[i] / machineWorkingTime[i]);
        return totalHumanConflict / Database.getNumHuman() + totalMachineConflict / Database.getNumMachine();
    }

    private static double fCost(double[] humanWorkingTime, double[] machineWorkingTime) {
        double[][] lexp = Database.getLEXP();
        double humanCost = 0, machineCost = 0;
        for (int i = 0; i < Database.getNumHuman(); i++) {
            double wage = 0;
            for (int k = 0; k < Database.getNumSkill(); k++)
                wage += lexp[i][k];
            humanCost += Database.getBaseSalary(i + 1) * wage * humanWorkingTime[i];
        }
        for (int i = 0; i < Database.getNumMachine(); i++)
            machineCost += Database.getConsume(i + 1) * machineWorkingTime[i];
        return humanCost / Database.getNumHuman() + machineCost / Database.getNumMachine();
    }

    public static double compare(Individual[] allocation1, Individual[] allocation2) {
        double totalDifference = 0;
        double[][] lexp = Database.getLEXP();
        for (int taskId = 1; taskId <= Database.getNumTask(); taskId++) {
            int human1 = allocation1[taskId - 1].getHuman();
            int machine1 = allocation1[taskId - 1].getMachine();
            int human2 = allocation2[taskId - 1].getHuman();
            int machine2 = allocation2[taskId - 1].getMachine();
            for (int i = 0; i < Database.getNumSkill(); i++)
                totalDifference += Math.abs(lexp[human1 - 1][i] - lexp[human2 - 1][i]);
            if (machine1 != 0 && machine2 != 0)
                totalDifference += Math.abs(Database.getProductivity(machine1) - Database.getProductivity(machine2));
        }
        return totalDifference / Database.getNumTask();
    }

    private static void printFinalResult(double[] start, double[] finish, double fitnessValue) {
        double projectFinish = 0;
        for (int i = 1; i <= Database.getNumTask(); i++) {
            Individual allocation = Database.getBestAllocation(i);
            System.out.println("Task #" + i + ":");
            System.out.println("\t- Human #" + allocation.getHuman() + " is allocated.");
            if (allocation.getMachine() != 0)
                System.out.println("\t- Machine #" + allocation.getMachine() + " is allocated.");
            else
                System.out.println("\t- This task does not use machine.");
            System.out.println("\t- Starts at " + String.format("%.2f", start[i - 1]) + " (h).");
            System.out.println("\t- Lasts for " + String.format("%.2f", finish[i - 1] - start[i - 1]) + " (h).");
            System.out.println("\t- Finishes at " + String.format("%.2f", finish[i - 1]) + " (h).");
            projectFinish = Math.max(projectFinish, finish[i - 1]);
        }
        System.out.println("The whole project lasts for " + formatTime((int) (projectFinish * 3600)));
        System.out.println("Final fitness value: " + fitnessValue);
    }
}
