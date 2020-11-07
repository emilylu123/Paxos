//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class TesterBonusFastPaxos extends ElectionFast {
    public static void main(String[] args) {
        System.out.println("============= [ Test Description ] ===================");
        System.out.println("Test 4 (Bonus) :: Fast Paxos");
        System.out.println("1. Fast Paxos Algorithm: Leader - M1");
        System.out.println("2. Proposer: M1 - M3 proposes to Leader at the same time;");
        System.out.println("3. Acceptor: M2 - M9 have random response time: immediate; medium; late; never.");
        System.out.println("======================================================\n");

        ElectionFast test = new ElectionFast();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);

        // allow M4-M9 to have random response time
        System.out.println("<<<<< Fast Paxos :: M2 - M9 will have random response time >>>>>");
        test.doRandom(true);
    }
}
