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
in the case where all M2 M3 will propose and then go offline
*/
public class TesterBonusByzantine extends Election {
    public static void main(String[] args) throws IOException {
        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // allow M4-M9 to have random response time
        System.out.println("<<<<< Byzantine:: M2 - M9 will have random response time >>>>>");
        test.doRandom();
        test.doByzantine();
    }
}
