import java.util.ArrayList;

public class Utils {
    public static final double wDuration = 0.2;
    public static final double wAssignment = 0.5;
    public static final double wCost = 0.3;
    public static final double filterRate = 0.5;        // 50% of individuals will be removed after each iteration
    private static int[] arrPower = new int[Database.getNumHuman() + Database.getNumMachine() + 5];

    public static int power(int base, int power) {
        if (power == 0)
            return 1;
        if (arrPower[power] == 0) {
            if (power % 2 == 0)
                arrPower[power] = power(base, power / 2) * power(base, power / 2);
            else
                arrPower[power] = power(base, power - 1) * base;
        }
        return arrPower[power];
    }

    public static double fitness(int binaryExpression, int taskId) {
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
            if (i != taskId)
                allocation = Database.getBestAllocation(i).getBinaryExpression();
            else
                allocation = binaryExpression;
            humanAllocation[i - 1] = allocation >> Database.getNumMachine();
            machineAllocation[i - 1] = allocation ^ (humanAllocation[i - 1] << Database.getNumMachine());
        }

        /* Final fitness result */
        return wDuration * fDuration(humanAllocation, machineAllocation, start, duration, finish)
                + wAssignment * fAssignment(humanAllocation, machineAllocation, start, duration, finish, humanWorkingTime, machineWorkingTime)
                + wCost * fCost(humanWorkingTime, machineWorkingTime);
    }

    public static double fDuration(int[] humanAllocation, int[] machineAllocation, double[] start, double[] duration, double[] finish) {
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
                double sumExp = 0;
                for (int k = 1; k <= Database.getNumHuman(); k++)
                    if ((humanAllocation[i] & (1 << (Database.getNumHuman() - k))) != 0)
                        sumExp += lexp[k - 1][j];
                if (sumExp != 0)
                    humanAffection = Math.max(humanAffection, sreq[i][j] / sumExp);
                else {
                    humanAffection = Math.sqrt(Double.MAX_VALUE) - 1;
                    break;
                }
            }
            if (Database.getMREQ(i + 1) != 0) {
                double sumProductivity = 0;
                for (int k = 1; k <= Database.getNumMachine(); k++)
                    if ((machineAllocation[i] & (1 << (Database.getNumMachine() - k))) != 0)
                        sumProductivity += Database.getProductivity(k);
                if (sumProductivity != 0)
                    machineAffection = Database.getMREQ(i + 1) / sumProductivity;
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
                int commonHumanAllocation = humanAllocation[u] & humanAllocation[v];
                int commonMachineAllocation = machineAllocation[u] & machineAllocation[v];
                for (int i = 1; i <= Database.getNumHuman(); i++)
                    if ((commonHumanAllocation & (1 << (Database.getNumHuman() - i))) != 0)
                        humanConflict[i - 1] += taskConflict;
                for (int i = 1; i <= Database.getNumMachine(); i++)
                    if ((commonMachineAllocation & (1 << (Database.getNumMachine() - i))) != 0)
                        machineConflict[i - 1] += taskConflict;
            }
        for (int u = 0; u < Database.getNumTask(); u++) {
            for (int i = 1; i <= Database.getNumHuman(); i++)
                if ((humanAllocation[u] & (1 << (Database.getNumHuman() - i))) != 0)
                    humanWorkingTime[i - 1] += duration[u];
            for (int i = 1; i <= Database.getNumMachine(); i++)
                if ((machineAllocation[u] & (1 << (Database.getNumMachine() - i))) != 0)
                    machineWorkingTime[i - 1] += duration[u];
        }
        for (int i = 0; i < Database.getNumHuman(); i++)
            totalHumanConflict += (humanConflict[i] / humanWorkingTime[i]);
        for (int i = 0; i < Database.getNumMachine(); i++)
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
}
