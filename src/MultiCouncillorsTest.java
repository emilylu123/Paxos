import java.io.IOException;
import java.util.Random;

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
including when M2 or M3 propose and then go offline*/
 public class MultiCouncillorsTest extends Election{
    public static void main(String[] args) {
        Election multi_election_test = new Election();
        multi_election_test.start();
        multi_election_test.propose(M3);
        multi_election_test.propose(M2);
        multi_election_test.propose(M1);

        // randomly choose M2 or M3 go offline after making proposal
        int ran = new Random().nextInt(2)+2;
//        multi_election_test.goOffline(ran);
    }
}
