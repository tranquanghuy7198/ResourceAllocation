import java.util.ArrayList;
import java.util.Random;

public class ResourceAllocation {
    private static int stableCount = 0;

    public static void main(String[] args) {
        ArrayList<Population> populations = new ArrayList<>();
        Random random = new Random();

        /* Main information */
        Database.readData("src/data/data1.txt");

        /* Populations initialization */
        for (int i = 1; i <= Database.getNumTask(); i++)
            populations.add(new Population(Utils.INDIVIDUALS_PER_POPULATION, i));

        /* Evolution */
        for (int iteration = 0; ; iteration++) {
            for (Population population : populations) {
                population.computeFitness();
                population.sort();
                Database.setBestAllocation(population.getBestAllocation(), population.getTaskId());
                population.filter();
                while (population.getCurrentSize() < Utils.INDIVIDUALS_PER_POPULATION) {
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
        Utils.fitness(1, 1, true);
    }

    private static boolean stop(int iteration) {
        if (Database.compareWithLast() < Utils.STABLE_THRESHOLD)
            stableCount++;
        else
            stableCount = 0;
        if (iteration >= Utils.MAX_LOOPS) {
            System.out.println("The evolution has reached the maximum generations!");
            return true;
        }
        if (stableCount >= Utils.STABLE_LOOPS) {
            System.out.println("The evolution has reached the stable state!");
            return true;
        }
        return false;
    }
}
