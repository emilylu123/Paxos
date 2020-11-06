//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

/*Multiple test with failures to check if Paxos works
when multiple councillors send voting proposals at the same time
*/
public class TesterMultiNormal extends Election {
    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("Test 1 :: Multiple proposers with immediate response");
        System.out.println("This test will simulate the case that\n" +"M1 M2 M3 propose at the same time,\n" +
                "and M1-M9 have immediate response.");
        System.out.println("======================================================\n");

        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);
    }
}
