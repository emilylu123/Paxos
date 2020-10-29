//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.IOException;
import java.util.Random;

/* Single test to check if Paxos Algorithm works when multiple propose M1 M2 M3 proposal
in the case where all M2 M3 will propose and then go offline
*/
public class TesterMultiWithRandom extends Election {
    public static void main(String[] args) throws IOException {
        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // randomly choose M2 or M3 go offline after making proposal
//        int ran = new Random().nextInt(2) + 2;
//        test.goOffline(ran);

        // M4-M9 will have random response time
        test.doRandomResponse();
    }
}
