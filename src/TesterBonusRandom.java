//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.IOException;

/* Single test to check if Paxos Algorithm works when multiple propose M1 M2 M3 proposal
in the case where M2 - M9 will have random response time
*/
public class TesterBonusRandom extends Election {
    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("Test 3 (Bonus) :: Multiple proposers with random response");
        System.out.println("This test will simulate the case that\n" +"M1 M2 M3 propose at the same time, " +
                "and M2-M9 have random response time: immediate;  medium; late; never.");
        System.out.println("======================================================\n");
        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // allow M4-M9 to have random response time
        System.out.println("<<<<< Random:: M2 - M9 will have random response time >>>>>");
        test.doRandom(true);
    }
}
