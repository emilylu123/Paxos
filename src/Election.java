//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
import java.io.IOException;
import java.util.*;

public class Election {
    protected final int councilSize = 9;
    protected final int majority = councilSize/2 + 1;
    protected static Member M1, M2, M3, M4, M5, M6, M7, M8, M9;
    protected List<Member> council = new ArrayList<>();//Define a voters collection
    protected List<Member> proposers = new ArrayList<>();//Define a proposers collection


    //Start all servers
    public void start() throws Exception {
        if (council != null && council.size() >0)
            throw new Exception("restart error");
        createMembers();
        startElection();
    }

    //Started server registration
    public void register(Member member) {
        council.add(member);
        if (member == M1 || member == M2 || member == M3) {
            proposers.add(member);
        }
    }

    public void createMembers(){
        M1 = new Member(1);
        M2 = new Member(2);
        M3 = new Member(3);
        M4 = new Member(4);
        M5 = new Member(5);
        M6 = new Member(6);
        M7 = new Member(7);
        M8 = new Member(8);
        M9 = new Member(9);
        register(M1);
        register(M2);
        register(M3);
        register(M4);
        register(M5);
        register(M6);
        register(M7);
        register(M8);
        register(M9);
    }
    //Create a legal Election Council with 9 members
    public synchronized void startElection() {
        System.out.println("<<<<<<<<<< Start Council Election >>>>>>>>");
        new Thread(M1::connecting).start();
        new Thread(M2::connecting).start();
        new Thread(M3::connecting).start();
        new Thread(M4::connecting).start();
        new Thread(M5::connecting).start();
        new Thread(M6::connecting).start();
        new Thread(M7::connecting).start();
        new Thread(M8::connecting).start();
        new Thread(M9::connecting).start();
    }

    protected void propose(Member a_member) throws InterruptedException {
        Thread.sleep(2000);
        new Thread(()->{
            while(true){
                try {
                    // phrase 1 : prepare(n), receive promise
                    System.out.println("Election:: proposal " + a_member.memberID );
                    a_member.prepare();

                    Thread.sleep(3000);
                    // phrase 2 : accept(n, value)
                    if(a_member.promiseCount >= majority){
                        System.out.printf("Election:: %s has received majority promises\n", a_member.memberID);
                        int value = (int) a_member.proposalID.getValue(); // todo
                        a_member.accept(value);
                        if (a_member.acceptCount>majority){
                            System.out.printf("Election:: %s has received majority accepted\n", a_member.memberID);
                            // todo
                            a_member.sendAll();
                            break;
                        }
                    } else {
                        System.out.printf("Election:: %s increase Proposal ID\n", a_member.memberID);
                        a_member.proposalID.incrementProposalID(); // increase proposal ID and retry prepare(n)
                    }
                } catch (InterruptedException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

/*    // todo let member sent prepare request
    private void prepare(Member member) {
        Thread thread = new Thread(){
            public synchronized void run(){
                member.prepare();
            }
        };
        thread.start();
    }

    //todo
    private void commit(Member member, int value) {
        Thread thread = new Thread(){
            public synchronized void run(){
                member.accept();
            }
        };
        thread.start();
    }*/
}
