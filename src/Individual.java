import java.util.ArrayList;
import java.util.Random;

public class Individual {
    private final int binaryExpression;

    public Individual(int binaryExpression) {
        this.binaryExpression = binaryExpression;
    }

    public int getBinaryExpression() {
        return binaryExpression;
    }

    /** Get some bits from "position + 1" to the end of binary string */
    public int getTail(int position) {
        return binaryExpression ^ getHead(position);
    }

    /** Get some bits from the start to "position" of binary string */
    public int getHead(int position) {
        int rightLength = Database.getHumanBitLength() + Database.getMachineBitLength() - position;
        return (binaryExpression >> rightLength) << rightLength;
    }

    public int getHuman() {
        return binaryExpression >> Database.getMachineBitLength();
    }

    public int getMachine() {
        return binaryExpression ^ ((binaryExpression >> Database.getMachineBitLength()) << Database.getMachineBitLength());
    }

    /** Remove some machines that a person in this task cannot use */
    public static Individual adjust(int binaryExpression, int taskId) {
        Random random = new Random();
        int humanId = binaryExpression >> Database.getMachineBitLength();
        int machineId = binaryExpression ^ (humanId << Database.getMachineBitLength());
        if (humanId <= 0 || humanId > Database.getNumHuman())
            humanId = random.nextInt(Database.getNumHuman()) + 1;
        ArrayList<Integer> abilitySet = Database.getAbility(humanId);
        if (Database.getMREQ(taskId) > 0) {
            if (!abilitySet.contains(machineId))
                machineId = abilitySet.get(random.nextInt(abilitySet.size()));
        } else
            machineId = 0;
        return new Individual((humanId << Database.getMachineBitLength()) | machineId);
    }

    /** Random a new individual */
    public static Individual random(int taskId) {
        Random random = new Random();
        int humanAllocation = random.nextInt(Database.getNumHuman()) + 1;
        int machineAllocation = random.nextInt(Database.getNumMachine() + 1);
        return adjust((humanAllocation << Database.getMachineBitLength()) | machineAllocation, taskId);
    }
}
