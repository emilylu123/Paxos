//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Member {

    protected int localport;
    protected String memberID = "M";
    protected int UID; //UID 1-9
    protected final String backupPath = "backup.txt";
    protected final String host = "0.0.0.0";
    protected final int councilSize = 9;
    protected final int majority = councilSize/2 + 1;
    protected final int MAX_TRY = 5;
    protected ProposalID proposalID; //p
//    protected Object proposedValue = null; //p
    protected ProposalID lastAcceptedID = null; //p
    protected HashSet<String> promisesReceived = new HashSet<>(); //p
    protected int acceptCount = 0;
    protected int promiseCount = 0;
    protected int retentionCount;
    protected int maxProposalID = -1;
    protected Object acceptedValue; //a
    protected ProposalID promisedID; //a
    protected ProposalID acceptedID; //a
    protected boolean hasPromised = false;
    protected Object finalValue = null; //l
    protected ProposalID finalProposalID = null; //l
    protected HashMap<ProposalID, Member> proposals = new HashMap<>();  //l
    protected HashMap<String,  ProposalID> acceptors = new HashMap<>();  //l

    protected boolean isOffline = false;

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    Member(int UID){
        this.UID = UID;
        this.memberID = "M" + String.valueOf(UID);
        this.localport = UID * 1111;  // number * 1111
        this.proposalID = new ProposalID(UID);
//        this.proposalID = new ProposalID(0, UID);
    }

    Member(int acceptCount, int retentionCount,Object acceptedValue){
        this.acceptCount = acceptCount;
        this.retentionCount = retentionCount;
        this.acceptedValue = acceptedValue;
    }

    void connecting (){
        File backup = new File( backupPath) ;
        if (backup.exists()){
            // todo read file and recover
        }
        try (ServerSocket server = new ServerSocket(this.localport)) {
            System.out.printf("<<<<<<<<<< Member %d is ready >>>>>>>>>>   \t%s\n",this.UID, server.toString());
            do {
                Socket member_socket = server.accept();
                runAcceptor(member_socket);

                if (isOffline) {
                    member_socket.close(); break; //todo .wait(10000);
                }
            } while (true);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in create Server Socket!");
            e.printStackTrace();
        }
    }

    protected void runAcceptor(Socket a_member_socket) throws IOException, ClassNotFoundException {
        System.out.println("<<<<<<<<<< Run Acceptor & receive messages >>>>>>>>>>" + a_member_socket.toString());
        ObjectInputStream ois = new ObjectInputStream(a_member_socket.getInputStream());
//        DataInputStream in = new DataInputStream(a_member_socket.getInputStream());
//        String inMSG = in.readUTF();
        ProposalID inPID = (ProposalID) ois.readObject();
        Object v = inPID.getValue();
        int p = inPID.getProposalID();
        int mid = inPID.getUID();
        System.out.printf(">> Acceptor Receive a message from M%d: (%d, %d)", mid, p, v);
//        String [] msg = inMSG.split(",");
//        ProposalID pid = new ProposalID(Integer.parseInt(msg[0]));
        if (v==null) {
            receiveAccept(a_member_socket.getPort(),inPID);
        } else {
            receivePrepare(a_member_socket.getPort(),inPID);
        }
    }

//    @Override
    // As a proposer set its proposal value to the given value
    public void setProposal(Object value) {
        if ( this.proposalID.getValue() == null ) //todo
            this.proposalID.setValue(value);
    }

//    @Override
    // phrase 1a : generate a proposal ID and send to acceptors
    public void prepare() throws IOException, InterruptedException, ClassNotFoundException {
        promisesReceived.clear();  // clear all promises that have received with earlier proposal(s)
        proposalID.incrementProposalID();
        System.out.println( "[ Phrase 1a ] " + this.memberID + " :: send Prepare(" + this.proposalID.getValue() + ")");

        // send prepare to all other members
        for (int i = 1; i <= councilSize; i++) {
            if ( i != this.UID ) sendPrepare(i);  // send prepare(n) to M(i) except itself
        }
    }

    // sending prepare (pID) to the assigned one Member(ID)
    public void sendPrepare(int acceptorMID) throws InterruptedException, IOException, ClassNotFoundException {
        Socket p_socket = null;
        int acceptorIP = acceptorMID * 1111;
        int count_try = 0;
        System.out.println(">> sending Prepare to M" + acceptorMID);

        while(count_try<=MAX_TRY){
            count_try++;
            try{
                p_socket = new Socket ( host, acceptorIP);

                System.out.println(">> Connected to M" + acceptorMID);
                break;
            } catch (Exception e) {
                System.out.println(">> Connection to M" + acceptorMID + " failed. Will retry in 3s. Retry " + count_try);
                Thread.sleep(3000);
                e.printStackTrace();
            }
            break; // todo
        }

        if (p_socket != null && p_socket.isConnected()){
            ObjectOutputStream oos = new ObjectOutputStream(p_socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(p_socket.getInputStream());
//            DataOutputStream out = new DataOutputStream(p_socket.getOutputStream());
//            DataInputStream in = new DataInputStream(p_socket.getInputStream());
            // send out proposalID
            oos.writeObject(this.proposalID);
            oos.flush();
//            out.writeUTF(String.valueOf(pID));
//            out.flush();
            System.out.println(">> MSG out: " + this.proposalID.getProposalID());
            ProposalID inPID;
            try {
                inPID = (ProposalID) ois.readObject();
                System.out.println(">> MSG in:" + inPID.getProposalID());
                //ProposalID prevAcceptedID,  Object prevAcceptedValue)
//                Object prevAcceptedValue = inPID.getValue();
                receivePromise(acceptorMID, this.proposalID, inPID); //todo
            } catch(Exception e){
                System.out.println("Error in receiving return message"); // todo
                e.printStackTrace();
            }
        }
    }

//    @Override
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

    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
     * it will set value to its proposal and send an Accept message (pid, V).
     */
    public void receivePromise( int fromMID, ProposalID proposalID, ProposalID prevAcceptedID){
        Object prevAcceptedValue = prevAcceptedID.getValue();
        // return if receives from itself or already received
        if ( !proposalID.equals(this.proposalID) || promisesReceived.contains(String.valueOf(fromMID)) )
            return;
        // add memberID to promises received collection
        promisesReceived.add( String.valueOf(fromMID) );

        if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID)) {
            lastAcceptedID = prevAcceptedID;
            if (prevAcceptedValue != null) // todo
                this.setProposal(prevAcceptedValue);
        }

        if (promisesReceived.size() == majority){
            if (this.proposalID.getValue() != null)
                sendAccept(this.proposalID, this.proposalID.getValue());
        }
    }

//    @Override
    public synchronized void accept(int value) {
        System.out.println("<<<<<<<<<< Accept >>>>>>>>>>");

    }

//    @Override
    public synchronized void accept(Socket member, String inMSG) throws IOException{

    }


//    @Override
    /* phrase 2 : acceptor compare received pID and maxPID,
    * if acceptPID = maxPID = n, accpet value = value , save to local backup, return
    * todo? otherwise return maxPID
    */
    public void receiveAccept(int fromMID, ProposalID proposalID) {

        System.out.println("in receive Accept");
        Object value = proposalID.getValue();
        if (promisedID == null || proposalID.isGreaterThan(promisedID) || proposalID.equals(promisedID)) {
            promisedID    = proposalID;
            acceptedID    = proposalID;
            acceptedValue = value;

            sendAccepted(acceptedID, acceptedValue);
        }
    }

//    @Override
    public void receiveAccepted(int fromMID, ProposalID proposalID, Object acceptedValue) {
        System.out.println("in receive Accepted");
        if (isComplete())
            return;

        ProposalID oldMID = acceptors.get(fromMID);

        if (oldMID != null && !proposalID.isGreaterThan(oldMID))
            return;

        acceptors.put(String.valueOf(fromMID), proposalID);

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


    // 1b send promise to proposer
    public void sendPromise(int proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) {
        System.out.println("in send Promise");
        //todo
    }

    // 2a
    public void sendAccept(ProposalID proposalID, Object proposalValue) {
        System.out.println("in send Accept");
        //todo
    }

    //2b
    public void sendAccepted(ProposalID proposalID, Object acceptedValue) {
        System.out.println("in receive Accepted");
        //todo
    }

    private void onResolution(ProposalID proposalID, Object acceptedValue) {
    }

    // final value
    public void sendAll() {
    }

//    @Override
    public boolean isComplete() {
        return finalValue != null;
    }
}