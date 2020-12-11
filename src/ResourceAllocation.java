import java.util.ArrayList;
import java.util.Random;

public class ResourceAllocation {
    public static void main(String[] args) {
        ArrayList<Population> populations = new ArrayList<>();
        final int INDIVIDUALS_PER_POPULATION = 20;
        Random random = new Random();

        /* Main information */
        Database.readData("src/data/data1.txt");
        for (int i = 1; i <= Database.getNumTask(); i++)
            Database.setBestAllocation(new Individual(Utils.power(2, Database.getNumHuman() + Database.getNumMachine()) - 1), i);

        /* Populations initialization */
        for (int i = 1; i <= Database.getNumTask(); i++)
            populations.add(new Population(INDIVIDUALS_PER_POPULATION, i));

        /* Evolution */
        for (int iteration = 0; iteration < 100; iteration++) {
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
        }

        /* Print the final result */
        for (int i = 1; i <= Database.getNumTask(); i++) {
            int bestAllocation = Database.getBestAllocation(i).getBinaryExpression();
            int humanAllocation = bestAllocation >> Database.getNumMachine();
            int machineAllocation = bestAllocation ^ (humanAllocation << Database.getNumMachine());
            System.out.print("Task #" + i + ": Human{");
            for (int k = 1; k <= Database.getNumHuman(); k++)
                if ((humanAllocation & (1 << (Database.getNumHuman() - k))) != 0)
                    System.out.print(k);
            System.out.print("} Machine{");
            for (int k = 1; k <= Database.getNumMachine(); k++)
                if ((machineAllocation & (1 << (Database.getNumMachine() - k))) != 0)
                    System.out.print(k);
            System.out.println("}");
        }
    }
}
