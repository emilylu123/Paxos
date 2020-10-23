//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

/* Single test to check if Paxos  Algorithm when only propose M1 works
in the case where all M1-M9 have immediate responses to voting queries*/
public class SingleCouncillorTest extends Election {
    public static void main(String[] args) {
        Election single_election_test = new Election();
        single_election_test.start();
        single_election_test.propose(M1);
    }
}
