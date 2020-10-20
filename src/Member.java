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

public class Member extends Communication{

    protected int MID; //memberID 1-9
    protected int localport;
    protected final String backupPath = "backup.txt";
    protected final int councilSize = 9;
    protected final int majority = councilSize/2 + 1;
    protected ProposalID proposalID = null; //p
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
    protected Object finalValue = null; //l
    protected ProposalID finalProposalID = null; //l
    protected HashMap<ProposalID, Member> proposals = new HashMap<>();  //l
    protected HashMap<String,  ProposalID> acceptors = new HashMap<>();  //l
    protected boolean isOffline = false;

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    Member(int MID){
        this.MID = MID;
        this.localport = MID * 1111;  // number * 1111
        this.proposalID = new ProposalID(MID);
//        this.proposalID = new ProposalID(0, MID);
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
            System.out.printf("<<<<< Member M%d is ready >>>>> Port: %d \n", this.MID , server.getLocalPort());
            do {
                Socket member_socket = server.accept();
                // todo random wait a certain time
                accept(member_socket);
                if (isOffline) {
                    member_socket.close(); break; //todo .wait(10000);
                }
            } while (true);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in create Server Socket!");
            e.printStackTrace();
        }
    }

    // As a proposer set its proposal value to the given value
    public void setProposal(Object value) {
        if ( this.proposalID.getValue() == null ) //todo
            this.proposalID.setValue(value);
    }

    // phrase 1a : generate a proposal ID and send to acceptors
    public void prepare() throws IOException, InterruptedException, ClassNotFoundException {
        promisesReceived.clear();  // clear all promises that have received with earlier proposal(s)
        proposalID.incrementProposalID();
        System.out.println( "<<<<< Phrase 1a >>>>>  M" + this.MID + " :: sent Prepare(" + this.proposalID.getProposalID() + ") to all");
        // send prepare to all other members
        for (int i = 1; i <= councilSize; i++) {
            if ( i != this.MID) sendPrepare(i);  // send prepare(n) to M(i) except itself
        }
        System.out.println("************ End sent prepare to all *****************");
    }


    // sending prepare (pID) to the assigned one Member(ID)
    public void sendPrepare(int acceptorMID) throws InterruptedException, IOException, ClassNotFoundException {
        outMSG(acceptorMID, this.proposalID);
//        ProposalID inPID = inMSG(acceptorMID);
//        receivePromise(acceptorMID, this.proposalID, inPID);
    }

//    @Override
    /*  phrase 1 : acceptor compare received proposalID and maxProposalID,
     if no maxPID exist, accept this proposalID as maxPID and accept value
     if proposalID > max, reply accepted ID and accepted value
     otherwise ignore this prepare message
     */
    public void receivePrepare(int fromMID, ProposalID proposalID) throws InterruptedException, IOException, ClassNotFoundException {
        System.out.printf(">> (Acceptor) M%s  :: Receive Prepare (%d) from M%d\n",this.MID,
                proposalID.getProposalID(), fromMID);
        // duplicate message
        if (this.promisedID != null && proposalID.equals(promisedID)) {
            sendPromise(fromMID, proposalID, acceptedID, acceptedValue);
        } else if (this.promisedID == null || proposalID.isGreaterThan(promisedID)) {
            promisedID = proposalID;
            sendPromise(fromMID, proposalID, acceptedID, acceptedValue);
        }
    }

    // 1b send promise to proposerUID
    public void sendPromise(int proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) throws InterruptedException, IOException, ClassNotFoundException {
        System.out.printf("\n<<<<< Phrase 1b >>>>>  M%s :: send Promise (%s) to M%d\n",this.MID ,
                proposalID.getProposalID(), proposerUID);
        outMSG(proposerUID,proposalID);
//        ProposalID inPID = inMSG(proposerUID);
    }


    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
     * it will set value to its proposal and send an Accept message (pid, V).
     */
    public void receivePromise( int fromMID, ProposalID proposalID, ProposalID prevAcceptedID){
        System.out.printf(">> M%d:: Receive Promise %s from M%d\n", this.MID, proposalID.getPID() ,fromMID);
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


    protected synchronized void accept() throws IOException, ClassNotFoundException {
        System.out.println("<< run empty accept function >>");
        // todo
    }

    protected synchronized void accept(Socket a_member_socket) throws IOException, ClassNotFoundException {
        System.out.println("<<<<<<<<<< M" + this.MID + " Start Accept >>>>>>>>>>    " + a_member_socket.toString());
        ObjectInputStream ois;
        ProposalID inPID;
        try {
            ois = new ObjectInputStream(a_member_socket.getInputStream());
            inPID= (ProposalID) ois.readObject();
            Object v = inPID.getValue();
            int p = inPID.getProposalID();
            int fromMID = inPID.getUID();
//            System.out.printf(">> M%s (Acceptor) :: Receive (%d, %s) from M%d\n", this.MID, p, v, fromMID);
            if (v != null) {
                receiveAccept(fromMID,inPID);
            } else {
                receivePrepare(fromMID,inPID);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.out.println("error in run acceptor");
            e.printStackTrace();
        }
    }

    //    @Override
    /* phrase 2 : acceptor compare received pID and maxPID,
    * if acceptPID = maxPID = n, accept value = value , save to local backup, return
    * todo? otherwise return maxPID
    */
    public void receiveAccept(int fromMID, ProposalID proposalID) {
        System.out.println(">> receive Accept");
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

    private void onResolution(ProposalID proposalID, Object acceptedValue) {
        System.out.println("in on Resolution (todo)");
        // todo
    }

    // 2a
    public void sendAccept(ProposalID proposalID, Object proposalValue) {
        System.out.println("in send Accept (todo)");
        //todo
    }

    //2b
    public void sendAccepted(ProposalID proposalID, Object acceptedValue) {
        System.out.println("in receive Accepted (todo)");
        //todo
    }

    // final value
    public void sendAll() {
        System.out.println("in send all");
        // todo
    }

//    @Override
    public boolean isComplete() {
        return finalValue != null;
    }

//    public Socket getSocket (int toMID) throws InterruptedException, IOException, ClassNotFoundException {
//        Socket socket = null;
//        int acceptorIP = toMID * 1111;
//        int count_try = 0;
//
//        while(count_try<=MAX_TRY){
//            count_try++;
//            try{
//                socket = new Socket ( host, acceptorIP);
//                System.out.println("Connected to M" + toMID);
//                break;
//            } catch (Exception e) {
//                System.out.println(">> Connection to M" + toMID + " failed. Will retry in 3s. Retry " + count_try);
//                Thread.sleep(3000);
//                e.printStackTrace();
//            }
//        }
//        return socket;
//    }
//
//    public ObjectOutputStream getOOS(Socket socket) throws IOException {
//        if (socket != null && socket.isConnected()){
//            return new ObjectOutputStream(socket.getOutputStream());
//        }
//        return null;
//    }
//
//    public ObjectInputStream getOIS(Socket socket) throws IOException {
//        if (socket != null && socket.isConnected())
//            return new ObjectInputStream(socket.getInputStream());
//        return null;
//    }
//
//    public boolean outMSG(int toMID, Object outOBJ) throws InterruptedException, IOException, ClassNotFoundException {
//        Socket socket = getSocket(toMID);
//        ObjectOutputStream oos = getOOS(socket);
//        oos.writeObject(outOBJ);
//        oos.flush();
//        System.out.println("M"+ MID +"sent obj to M" + toMID);
//        System.out.printf(">> MSG out M%s -> %s: (%s, %s)\n",this.MID,toMID, this.proposalID.getProposalID(),
//                this.proposalID.getValue());
//        return true;
//    }
//
//    public ProposalID inMSG(int fromMID) throws InterruptedException, IOException, ClassNotFoundException {
//        Socket socket = getSocket(fromMID);
//        ObjectInputStream ois = getOIS(socket);
//        ProposalID inPID = null;
//        try {
//            inPID = (ProposalID) ois.readObject();
//            System.out.printf(">> MSG in %s -> M%s: (%s, %s)",fromMID, this.MID, inPID.getProposalID(), inPID.getValue());
//        } catch(Exception e){
//            System.out.println("Error in receiving in message");
//            e.printStackTrace();
//        }
//        return inPID;
//    }
}