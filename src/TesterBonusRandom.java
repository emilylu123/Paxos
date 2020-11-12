//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class TesterBonusRandom extends Election {
    public static void main(String[] args) {
        System.out.println("============= [ Test Description ] ===================");
        System.out.println("Test 3 (Bonus) :: Classic Paxos - with random response");
        System.out.println("1. Proposer: M1 - M3 proposes at the same time;");
        System.out.println("2. Acceptor: M2 - M9 has random response time: immediate; medium; late; never.");
        System.out.println("======================================================\n");
        Election test = new Election();
        test.start(false);
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);
        test.propose(M4);
        test.propose(M5);

        // allow M4-M9 to have random response time
        test.doRandom();
    }
}
