import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Population {
    private final int taskId;
    private final ArrayList<Individual> individuals;
    private final ArrayList<Double> adaption;

    public Population(int numIndividuals, int taskId) {
        individuals = new ArrayList<>();
        adaption = new ArrayList<>();
        this.taskId = taskId;
        Random random = new Random();
        for (int i = 0; i < numIndividuals; i++) {
            int humanAllocation = random.nextInt(Database.getNumHuman()) + 1;
            int machineAllocation = random.nextInt(Database.getNumMachine() + 1);
            individuals.add(Individual.adjust((humanAllocation << Database.getMachineBitLength()) | machineAllocation, taskId));
        }
    }

    public Individual getBestAllocation() {
        return individuals.get(0);
    }

    public int getTaskId() {
        return taskId;
    }

    public int getCurrentSize() {
        return individuals.size();
    }

    public void crossover(int fatherIndex, int motherIndex) {
        Individual father = individuals.get(fatherIndex);
        Individual mother = individuals.get(motherIndex);
        Random random = new Random();
        int crossPosition = random.nextInt(Database.getHumanBitLength() + Database.getMachineBitLength()) + 1;
        individuals.add(Individual.adjust(father.getHead(crossPosition) | mother.getTail(crossPosition), taskId));
        individuals.add(Individual.adjust(mother.getHead(crossPosition) | father.getTail(crossPosition), taskId));
    }

    public void mutate(int index) {
        Individual origin = individuals.get(index);
        Random random = new Random();
        int position = random.nextInt(Database.getHumanBitLength() + Database.getMachineBitLength());
        individuals.add(Individual.adjust(origin.getBinaryExpression() ^ (1 << position), taskId));
    }

    public void computeFitness() {
        adaption.clear();
        for (Individual individual : individuals)
            adaption.add(Utils.fitness(individual.getBinaryExpression(), taskId));
    }

    public void sort() {
        for (int i = 0; i < individuals.size() - 1; i++)
            for (int j = i + 1; j < individuals.size(); j++)
                if (adaption.get(i) > adaption.get(j)) {
                    Collections.swap(individuals, i, j);
                    Collections.swap(adaption, i, j);
                }
    }

    public void filter() {
        int numDeath = (int) (Utils.filterRate * individuals.size());
        for (int i = 0; i < numDeath; i++) {
            individuals.remove(individuals.size() - 1);
            adaption.remove(adaption.size() - 1);
        }
    }
}
