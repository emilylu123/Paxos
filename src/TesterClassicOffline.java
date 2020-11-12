//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class TesterClassicOffline extends Election {
    public static void main(String[] args) {
        System.out.println("============= [ Test Description ] ===================");
        System.out.println("Test 2 :: Classic Paxos - with failure (Offline)");
        System.out.println("1. Proposer: M1 - M3 proposes to all members at the same time;");
        System.out.println("2. Offline : M2 / M3 proposes and then goes offline;");
        System.out.println("3. Acceptor: M4 - M9 has random response time: immediate; medium; late; never.");
        System.out.println("======================================================\n");

        Election test = new Election();
        test.start(false);
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // randomly choose M2 or M3 go offline after making proposal
        test.goOffline();

        //let M4-M9 have random response time
        test.doRandom(false);
    }
}
