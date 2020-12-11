import java.util.Random;

public class Individual {
    private int binaryExpression;

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
        int rightLength = Database.getNumHuman() + Database.getNumMachine() - position;
        return (binaryExpression >> rightLength) << rightLength;
    }

    /** Remove some machines that people in this task cannot use */
    public static Individual adjust(int binaryExpression, int taskId) {
        Random random = new Random();
        int humanAllocation = binaryExpression >> Database.getNumMachine();
        int machineAllocation = binaryExpression ^ (humanAllocation << Database.getNumMachine());
        if (humanAllocation == 0)
            humanAllocation = random.nextInt(Utils.power(2, Database.getNumHuman()) - 1) + 1;
        int abilitySet = 0;
        if (Database.getMREQ(taskId) > 0)
            for (int i = 1; i <= Database.getNumHuman(); i++)
                if ((humanAllocation & (1 << (Database.getNumHuman() - i))) != 0)
                    abilitySet |= Database.getAbility(i);
        if ((machineAllocation & abilitySet) == 0)
            machineAllocation = abilitySet;
        else
            machineAllocation &= abilitySet;
        return new Individual(((binaryExpression >> Database.getNumMachine()) << Database.getNumMachine()) | machineAllocation);
    }
}
