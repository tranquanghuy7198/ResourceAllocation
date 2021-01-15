import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Database.readData("src/data/data1.txt");
        Database.setBestAllocation(Individual.adjust(8, 1), 1);
        Database.setBestAllocation(Individual.adjust(15, 2), 2);
        Database.setBestAllocation(Individual.adjust(12, 3), 3);
        Database.setBestAllocation(Individual.adjust(7, 4), 4);
        Database.setBestAllocation(Individual.adjust(21, 5), 5);
        Database.setBestAllocation(Individual.adjust(16, 6), 6);
        Database.setBestAllocation(Individual.adjust(7, 7), 7);
        System.out.println("Fitness: " + Utils.fitness(1, 1));
    }
}
