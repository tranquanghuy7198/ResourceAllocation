import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Database {
    private static ArrayList<ArrayList<Integer>> dependencyGraph;
    private static double[][] lexp;
    private static double[][] sreq;
    private static final ArrayList<Double> mreq = new ArrayList<>();
    private static final ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    private static final ArrayList<Double> baseSalary = new ArrayList<>();
    private static final ArrayList<Double> consume = new ArrayList<>();
    private static final ArrayList<Double> estimate = new ArrayList<>();
    private static final ArrayList<Double> productivity = new ArrayList<>();
    private static Individual[] bestAllocation;
    private static int numHuman;
    private static int numMachine;
    private static int humanBitLength;
    private static int machineBitLength;
    private static int numTask;
    private static int numSkill;

    public static int getNumHuman() {
        return numHuman;
    }

    public static int getNumMachine() {
        return numMachine;
    }

    public static int getNumTask() {
        return numTask;
    }

    public static int getNumSkill() {
        return numSkill;
    }

    public static Individual getBestAllocation(int taskId) {
        return bestAllocation[taskId - 1];
    }

    public static ArrayList<ArrayList<Integer>> getDependencyGraph() {
        return dependencyGraph;
    }

    public static ArrayList<Integer> getAbility(int humanId) {
        return ability.get(humanId - 1);
    }

    public static double getMREQ(int taskId) {
        return mreq.get(taskId - 1);
    }

    public static double[][] getLEXP() {
        return lexp;
    }

    public static double[][] getSREQ() {
        return sreq;
    }

    public static double getBaseSalary(int humanId) {
        return baseSalary.get(humanId - 1);
    }

    public static double getConsume(int machineId) {
        return consume.get(machineId - 1);
    }

    public static double getProductivity(int machineId) {
        return productivity.get(machineId - 1);
    }

    public static int getHumanBitLength() {
        return humanBitLength;
    }

    public static int getMachineBitLength() {
        return machineBitLength;
    }

    public static void setNumHuman(int numHuman) {
        Database.numHuman = numHuman;
    }

    public static void setNumMachine(int numMachine) {
        Database.numMachine = numMachine;
    }

    public static void setNumTask(int numTask) {
        Database.numTask = numTask;
        bestAllocation = new Individual[numTask];
    }

    public static double getEstimatedTime(int taskId) {
        return estimate.get(taskId - 1);
    }

    public static void setNumSkill(int numSkill) {
        Database.numSkill = numSkill;
    }

    public static void setBestAllocation(Individual allocation, int taskId) {
        bestAllocation[taskId - 1] = allocation;
    }

    public static void setAbility(int[][] arrAbility) {
        for (int i = 0; i < numHuman; i++) {
            ArrayList<Integer> arr = new ArrayList<>();
            for (int j = 1; j <= arrAbility[i][0]; j++)
                arr.add(arrAbility[i][j]);
            ability.add(arr);
        }
    }

    public static void setMREQ(double[] arrMREQ) {
        for (int i = 0; i < numTask; i++)
            mreq.add(arrMREQ[i]);
    }

    public static void setDependencyGraph(int[][] adj) {
        dependencyGraph = new ArrayList<>();
        for (int i = 0; i < numTask; i++) {
            ArrayList<Integer> neighbor = new ArrayList<>();
            for (int j = 0; j < numTask; j++)
                if (adj[i][j] == 1)
                    neighbor.add(j);
            dependencyGraph.add(neighbor);
        }
    }

    public static void setLEXP(double[][] lexp) {
        Database.lexp = lexp;
    }

    public static void setSREQ(double[][] sreq) {
        Database.sreq = sreq;
    }

    public static void setBaseSalary(double[] baseSalary) {
        for (int i = 0; i < numHuman; i++)
            Database.baseSalary.add(baseSalary[i]);
    }

    public static void setConsume(double[] consume) {
        for (int i = 0; i < numMachine; i++)
            Database.consume.add(consume[i]);
    }

    public static void setEstimatedTime(double[] estimatedTime) {
        for (int i = 0; i < numTask; i++)
            estimate.add(estimatedTime[i]);
    }

    public static void setProductivity(double[] productivity) {
        for (int i = 0; i < numMachine; i++)
            Database.productivity.add(productivity[i]);
    }

    public static void setHumanBitLength(int humanBitLength) {
        Database.humanBitLength = humanBitLength;
    }

    public static void setMachineBitLength(int machineBitLength) {
        Database.machineBitLength = machineBitLength;
    }

    public static void readData(String dataFile) {
        try {
            File data = new File(dataFile);
            Scanner scanner = new Scanner(data);
            setNumTask(scanner.nextInt());
            setNumHuman(scanner.nextInt());
            setNumMachine(scanner.nextInt());
            setNumSkill(scanner.nextInt());
            setHumanBitLength((int) Math.ceil(Math.log(numHuman) / Math.log(2)));
            setMachineBitLength((int) Math.ceil(Math.log(numMachine) / Math.log(2)));
            int[][] adj = new int[numTask][numTask];
            int[][] arrAbility = new int[numHuman][numMachine + 1];
            double[] arrMREQ = new double[numTask];
            double[] arrConsume = new double[numMachine];
            double[] arrBaseSalary = new double[numHuman];
            double[][] mLEXP = new double[numHuman][numSkill];
            double[] arrEstimatedTime = new double[numTask];
            double[] arrProd = new double[numMachine];
            double[][] mSREQ = new double[numTask][numSkill];
            for (int i = 0; i < numTask; i++)
                for (int j = 0; j < numTask; j++)
                    adj[i][j] = scanner.nextInt();
            for (int i = 0; i < numHuman; i++)
                for (int j = 0; j < numMachine + 1; j++)
                    arrAbility[i][j] = scanner.nextInt();
            for (int i = 0; i < numTask; i++)
                arrMREQ[i] = scanner.nextDouble();
            for (int i = 0; i < numMachine; i++)
                arrConsume[i] = scanner.nextDouble();
            for (int i = 0; i < numHuman; i++)
                arrBaseSalary[i] = scanner.nextDouble();
            for (int i = 0; i < numHuman; i++)
                for (int j = 0; j < numSkill; j++)
                    mLEXP[i][j] = scanner.nextDouble();
            for (int i = 0; i < numTask; i++)
                arrEstimatedTime[i] = scanner.nextDouble();
            for (int i = 0; i < numMachine; i++)
                arrProd[i] = scanner.nextDouble();
            for (int i = 0; i < numTask; i++)
                for (int j = 0; j < numSkill; j++)
                    mSREQ[i][j] = scanner.nextDouble();
            setDependencyGraph(adj);
            setAbility(arrAbility);
            setMREQ(arrMREQ);
            setConsume(arrConsume);
            setBaseSalary(arrBaseSalary);
            setLEXP(mLEXP);
            setEstimatedTime(arrEstimatedTime);
            setProductivity(arrProd);
            setSREQ(mSREQ);
            scanner.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File " + dataFile + " does not exist!");
        }
    }
}
