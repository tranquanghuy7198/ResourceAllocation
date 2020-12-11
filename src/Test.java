import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        try {
            File data = new File("src/data/data2.txt");
            Scanner scanner = new Scanner(data);
            System.out.println(scanner.nextDouble());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
