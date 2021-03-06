//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.util.Date;

public class TesterClassicNormal extends Election {
    public static void main(String[] args) {
        System.out.println("============= [ Test Description ] ===================");
        System.out.println("Test 1 :: Classic Paxos - No failure");
        System.out.println("1. Proposer: M1 - M3 proposes to all members at the same time;");
        System.out.println("2. Acceptor: All members response immediately.");
        System.out.println("======================================================\n");

        Election test = new Election();

        test.start(false);   // true will test 13 nodes, false will test 9 nodes
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);
        test.propose(M4);
        test.propose(M5);
    }
}
