//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class TesterBonusByzantine extends ElectionFast {
    public static void main(String[] args) {
        System.out.println("============= [ Test Description ] ===================");
        System.out.println("Test 5 (Bonus) :: Fast Byzantine Paxos");
        System.out.println("1. Byzantine Traitor : 1-2 members from M4 - M9 will lie or collude;");
        System.out.println("2. Proposer: M1 - M3 propose to Leader M1 at the same time;");
        System.out.println("3. Acceptor: M2 - M9 have random response time: immediate; medium; late; never.");
        System.out.println("======================================================\n");

        ElectionFast test = new ElectionFast();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // allow M4-M9 to have random response time
        System.out.println("<<<<< Byzantine:: member from M4 - M9 will lie or collude >>>>>");
        test.doRandom(true);
        test.doByzantine();
        test.doByzantine();
    }
}
