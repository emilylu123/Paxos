import java.util.Date;

public class LocalTest {
    public static void main(String[] args) {
        int UID1 = 2;
        int UID2 = 9;
        String time2 = Long.toString(System.currentTimeMillis()) + Integer.toString(UID2);
        String time1 = Long.toString(System.currentTimeMillis()) + Integer.toString(UID1);
        int PID1 = Integer.parseInt(time1.substring(6,time1.length()))  ;
        int PID2 = Integer.parseInt(time2.substring(6,time2.length()))  ;
        System.out.println(time1);
        System.out.println(time2);
        System.out.println(PID1);
        System.out.println(PID2);

        System.out.println(PID1==PID2);
    }
}
