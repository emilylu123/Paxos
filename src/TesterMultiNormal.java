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
        Election test = new Election();
        test.start();
        test.propose(M1);
        test.propose(M2);
        test.propose(M3);
    }
}
