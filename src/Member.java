//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class Member implements MemberInterface {

    protected final String backupPath = "backup.txt";
    protected final int majority = 9/2 + 1;
    protected int memberID; //p  Math.abs(new Random().nextInt(9));//ID 1-9
    protected ProposalID proposalID; //p
    protected Object proposedValue = null; //p
    protected ProposalID lastAcceptedID = null; //p
    protected HashSet<String> promisesReceived = new HashSet<>(); //p
    protected int acceptProposalID =-1;
    protected int acceptValue = -1;
    protected int acceptCount = 0;
    protected int retentionCount;
    protected int maxProposalID = -1;
    protected Object acceptedValue; //a
    protected ProposalID promisedID; //a
    protected ProposalID acceptedID; //a

    protected Object finalValue = null; //l
    protected ProposalID finalProposalID = null; //l
    protected HashMap<ProposalID, Member> proposals = new HashMap<>();  //l
    protected HashMap<String,  ProposalID> acceptors = new HashMap<>();  //l

    protected boolean disconnect = false;

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    Member(int memberID){
        if (memberID <= 3){
            this.memberID = memberID; // M1 M2 M3 always vote for itself
        } else {
            this.memberID = (int)(Math.random() * 3) + 1;  // randomly vote for [M1, M2, M3]
        }
        this.proposalID = new ProposalID(0, memberID);
    }

    Member(int acceptCount, int retentionCount,Object acceptedValue){
        this.acceptCount = acceptCount;
        this.retentionCount = retentionCount;
        this.acceptedValue=acceptedValue;
    }

    void connecting (int port){
        File backup = new File( backupPath) ;
        if (backup.exists()){
            // todo read file and recover
        }
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.printf("******** Server %d is ready ********\n",port);
            do {
                Socket a_member = server.accept();
                runProposal(a_member);
            } while (true);
        } catch (IOException e) {
            System.out.println("Error in create Server Socket!");
            e.printStackTrace();
        }
    }

    protected void runProposal(Socket a_member) throws IOException {
        DataInputStream in = new DataInputStream(a_member.getInputStream());
        String inMSG = in.readUTF();

        System.out.println("In message: " + inMSG);
        String [] msg = inMSG.split(",");
        ProposalID pid = new ProposalID(Integer.parseInt(msg[0]));
        if (msg.length>1) {
            String value = msg[1];
            receiveAccept(a_member.getPort(),pid,msg[1]);
//            commit(a_member, inMSG);
        } else {
            receivePrepare(a_member.getPort(),pid);
        }

    }

    @Override
    // As a proposer set its proposal value to the given value
    public void setProposal(Object value) {
        if ( this.proposedValue == null ) //todo
            this.proposedValue = value;
    }

    @Override
    // generate a proposal ID and send to acceptors
    public void prepare() {
        promisesReceived.clear();  // clear all promises that have received with earlier proposal(s)
        proposalID.incrementProposalID(); // todo increment proposal ID number
        sendPrepare(proposalID);
    }

    @Override
    public synchronized void accept(Socket member, String inMSG) throws IOException{

    }

    @Override
    /*  phrase 1 : acceptor compare received proposalID and maxProposalID,
     if no maxPID exsit, accept this proposalID as maxPID and accept value
     if proposalID > max, reply accepted ID and accepted value
     otherwise ignore this prepare message
     */
    public void receivePrepare(int fromMID, ProposalID proposalID) {
        // duplicate message
        if (this.promisedID != null && proposalID.equals(promisedID)) {
            sendPromise(fromMID, proposalID, acceptedID, acceptedValue);
        } else if (this.promisedID == null || proposalID.isGreaterThan(promisedID)) {
            promisedID = proposalID;
            sendPromise(fromMID, proposalID, acceptedID, acceptedValue);
        }
    }

    @Override
    //  receive promise from majority acceptors and send Accept
    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
    * it will set value to its proposal and send an Accept message (pid, V).
    */
    public void receivePromise(int fromMID, ProposalID proposalID, ProposalID prevAcceptedID, Object prevAcceptedValue) {
        // return if receives from itself or already received
        if ( !proposalID.equals(this.proposalID) || promisesReceived.contains(Integer.toString(fromMID)) )
            return;
        // add memberID to promises received collection
        promisesReceived.add( Integer.toString(fromMID) );

        if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID)) {
            lastAcceptedID = prevAcceptedID;
            if (prevAcceptedValue != null)
                proposedValue = prevAcceptedValue;
        }

        if (promisesReceived.size() == majority){
            if (proposedValue != null)
                sendAccept(this.proposalID, proposedValue);
        }
    }

    @Override
    /* phrase 2 : acceptor compare received pID and maxPID,
    * if acceptPID = maxPID = n, accpet value = value , save to local backup, return
    * todo? otherwise return maxPID
    */
    public void receiveAccept(int fromMID, ProposalID proposalID, Object value) {
        if (promisedID == null || proposalID.isGreaterThan(promisedID) || proposalID.equals(promisedID)) {
            promisedID    = proposalID;
            acceptedID    = proposalID;
            acceptedValue = value;

            sendAccepted(acceptedID, acceptedValue);
        }
    }

    @Override
    // send proposal ID to all acceptors
    public void sendPrepare(ProposalID proposalID) {
        //todo
    }

    @Override
    // send promise to proposer
    public void sendPromise(int proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) {
        //todo
    }

    @Override
    public void sendAccept(ProposalID proposalID, Object proposalValue) {
        //todo
    }

    @Override
    public void sendAccepted(ProposalID proposalID, Object acceptedValue) {
        //todo
    }

    @Override
    public void onResolution(ProposalID proposalID, Object value) {
        //todo
    }

    @Override
    public boolean isComplete() {
        return finalValue != null;
    }

    @Override
    public void receiveAccepted(int fromMID, ProposalID proposalID, Object acceptedValue) {
        if (isComplete())
            return;

        ProposalID oldMID = acceptors.get(fromMID);

        if (oldMID != null && !proposalID.isGreaterThan(oldMID))
            return;

        acceptors.put(Integer.toString(fromMID), proposalID);

        if (oldMID != null) {
            Member oldProposal = proposals.get(oldMID);
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0)
                proposals.remove(oldMID);
        }

        if (!proposals.containsKey(proposalID))
            proposals.put(proposalID, new Member(0, 0, acceptedValue));

        Member thisProposal = proposals.get(proposalID);

        thisProposal.acceptCount    += 1;
        thisProposal.retentionCount += 1;

        if (thisProposal.acceptCount == majority) {
            finalProposalID = proposalID;
            finalValue      = acceptedValue;
            proposals.clear();
            acceptors.clear();

            onResolution(proposalID, acceptedValue);
        }
    }

    public void start(int port) {

    }
}