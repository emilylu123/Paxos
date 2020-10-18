//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
import java.util.*;
import java.io.*;

public class Election {
    protected final int councilSize = 9;
    protected final int majority = (int) councilSize/2 + 1;
    protected static Member M1, M2, M3, M4, M5, M6, M7, M8, M9;
    protected static final String ip = "0.0.0.0";

    protected final List<Member> council = new ArrayList<>();//Define a voters collection
    protected final List<Member> proposers = new ArrayList<>();//Define a proposers collection


    //Start all servers
    public void start(int serverNumber) throws Exception {
        System.out.println("==========Start Council Election==========");
        if (council != null && council.size() >0)
            throw new Exception("restart error");
    }

    //Started server registration
    public void register(Member a_member) {
        council.add(a_member);
    }

    //Get all servers
    public int getMajority() {
        return council.size()/2+1;
    }

    public void startProposers(){
        M1 = new Member(1);
        M2 = new Member(2);
        M3 = new Member(3);
        M4 = new Member(4);
        M5 = new Member(5);
        M6 = new Member(6);
        M7 = new Member(7);
        M8 = new Member(8);
        M9 = new Member(9);
    }
    //Create a legal Election Council
    public synchronized void startElection() {
        new Thread(()->{
            M1.start(1111);
            register(M1);
        }).start();
        new Thread(()->{
            M2.start(2222);
            register(M2);
        }).start();
        new Thread(()->{
            M3.start(3333);
            register(M3);
        }).start();
        new Thread(()->{
            M4.start(4444);
            register(M4);
        }).start();
        new Thread(()->{
            M5.start(5555);
            register(M5);
        }).start();
        new Thread(()->{
            M6.start(6666);
            register(M6);
        }).start();
        new Thread(()->{
            M7.start(7777);
            register(M7);
        }).start();
        new Thread(()->{
            M8.start(8888);
            register(M8);
        }).start();
        new Thread(()->{
            M9.start(9999);
            register(M9);
        }).start();
    }

    //Get a legal collection with random order
    public synchronized List<Member> startCouncil() {
        List<Member> list = new ArrayList<>();
        int count = 0;
        int councilSize = council.size();  //9
        int majority = getMajority();

        Random random = new Random();
        while(count < majority) {
            int _random = Math.abs(random.nextInt(councilSize)); // Generate a random number 0~8
            Member _member = council.get(_random);
            if(!list.contains(_member)) {
                list.add(_member);
                count++;
            }
        }
        return list;
    }
}
