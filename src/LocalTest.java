import java.util.Date;
import java.util.Random;

public class LocalTest {
    public static void main(String[] args) {
        // test create PID = time stamp + ID
        int UID1 = 2;
        int UID2 = 9;
        String time2 = new Date().getTime() + Integer.toString(UID2);
        String time1 = System.currentTimeMillis() + Integer.toString(UID1);
        int PID1 = Integer.parseInt(time1.substring(6))  ;
        int PID2 = Integer.parseInt(time2.substring(6))  ;
        System.out.println(time1);
        System.out.println(time2);
        System.out.println(PID1);
        System.out.println(PID2);

        System.out.println(PID1==PID2);

        // test random number from [1, 9]
        System.out.println(new Random().nextInt(2)+2);
        System.out.println(new Random().nextInt(2)+2);
        System.out.println(new Random().nextInt(2)+2);
        System.out.println(new Random().nextInt(2)+2);
        System.out.println(new Random().nextInt(2)+2);
    }
}
