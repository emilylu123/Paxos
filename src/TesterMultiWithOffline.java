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
in the case where M2 or M3 will propose and then go offline
*/
public class TesterMultiWithOffline extends Election {
    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("Test 2 :: Multiple proposers with proposer being offline ");
        System.out.println("This test will simulate the case that\n" +"M1 M2 M3 propose at the same time, " +
                "M2/M3 propose and then go offline, \n"+ "and M4-M9 have random response.");
        System.out.println("======================================================\n");

        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // randomly choose M2 or M3 go offline after making proposal
        test.goOffline();
        //let M4-M9 have random response time
        test.doRandom(false);
    }
}
