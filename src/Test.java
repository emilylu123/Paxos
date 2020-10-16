//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
//
public class Test {
    public static void main(String[] args) {
        Election election = new Election();
        try{
            election.start(9);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
