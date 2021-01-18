import java.util.ArrayList;
import java.util.Random;

public class ResourceAllocation {
    private static final int INDIVIDUALS_PER_POPULATION = 20;
    private static final int MAX_LOOPS = 1000;
    private static final int STABLE_LOOPS = 10;
    private static final double STABLE_THRESHOLD = 0.001;
    private static int stableCount = 0;

    public static void main(String[] args) {
        ArrayList<Population> populations = new ArrayList<>();
        Random random = new Random();

        /* Main information */
        Database.readData("src/data/data1.txt");
        for (int i = 1; i <= Database.getNumTask(); i++)
            Database.setBestAllocation(new Individual((1 << Database.getMachineBitLength()) | 1), i);

        /* Populations initialization */
        for (int i = 1; i <= Database.getNumTask(); i++)
            populations.add(new Population(INDIVIDUALS_PER_POPULATION, i));

        /* Evolution */
        for (int iteration = 0; ; iteration++) {
            for (Population population : populations) {
                population.computeFitness();
                population.sort();
                Database.setBestAllocation(population.getBestAllocation(), population.getTaskId());
                population.filter();
                while (population.getCurrentSize() < INDIVIDUALS_PER_POPULATION) {
                    int fatherId = random.nextInt(population.getCurrentSize());
                    int mutationId = random.nextInt(population.getCurrentSize());
                    int motherId;
                    do {
                        motherId = random.nextInt(population.getCurrentSize());
                    } while (motherId == fatherId);
                    population.crossover(fatherId, motherId);
                    population.mutate(mutationId);
                }
            }
            if (stop(iteration))
                break;
        }

        /* Print the final result */
        for (int i = 1; i <= Database.getNumTask(); i++) {
            int bestAllocation = Database.getBestAllocation(i).getBinaryExpression();
            int humanAllocation = bestAllocation >> Database.getMachineBitLength();
            int machineAllocation = bestAllocation ^ (humanAllocation << Database.getMachineBitLength());
            System.out.print("Task #" + i + ": human #" + humanAllocation);
            if (machineAllocation > 0)
                System.out.println(", machine #" + machineAllocation);
            else
                System.out.println(", no machine!");
        }
    }

    private static boolean stop(int iteration) {
        if (Database.compareWithLast() < STABLE_THRESHOLD)
            stableCount++;
        else
            stableCount = 0;
        if (iteration >= MAX_LOOPS) {
            System.out.println("Stop because of max loop");
            return true;
        }
        if (stableCount >= STABLE_LOOPS) {
            System.out.println("Stop when reach stable state");
            return true;
        }
        return false;
    }
}
