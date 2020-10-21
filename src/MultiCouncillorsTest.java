//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
//
 public class MultiCouncillorsTest extends Election{
    public static void main(String[] args) throws InterruptedException {

        Election election = new Election();
        try{
            election.start();
        }catch(Exception e) {
            e.printStackTrace();
        }

        election.propose(M1);
        election.propose(M3);
    }
}
