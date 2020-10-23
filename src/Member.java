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

public class Member extends Communication {

    protected int MID; //memberID 1-9
    protected ProposalMSG proposalMSG = null; //p
    protected ProposalMSG lastAcceptedMSG = null; //p
    protected int acceptCount = 0;
    protected int retentionCount;
    protected int maxProposalID = -1;
    protected Object acceptedValue; //a
    protected ProposalMSG promisedMSG; //a
    protected ProposalMSG acceptedMSG; //a
    protected Object finalValue = null; //l
    protected ProposalMSG finalProposalMSG = null; //l
    protected HashSet<Integer> promisesReceived = new HashSet<>(); //p
    protected HashSet<Integer> acceptedReceived = new HashSet<>(); //p
    protected HashMap<ProposalMSG, Member> proposals = new HashMap<>();  //l
    protected HashMap<Integer, ProposalMSG> acceptors = new HashMap<>();  //l
    protected Socket socket;
    protected ServerSocket server;
    protected int localport;
    protected String backupPath = "";
    protected final int majority = 9/2 + 1;
    protected boolean isOffline = false;   // to simulate M2 M3 random go offline
    protected boolean isRandomWait = false;  // to simulate M4-M9 random network issue
    protected boolean isByzantine = false;  // for Byzantine's algorithm

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    Member(int MID){
        this.MID = MID;
        this.proposalMSG = new ProposalMSG(MID);  // set PID = 0
        this.backupPath = this.MID +"data.txt";
        this.localport = MID * 1111;  // number * 1111
    }

    Member(int acceptCount, int retentionCount,Object acceptedValue){
        this.acceptCount = acceptCount;
        this.retentionCount = retentionCount;
        this.acceptedValue = acceptedValue;
    }

    void connecting ()  {
        cleanCloseSocket();  // close previous connection if any
        File backup = new File(backupPath) ;
        try {
            // fault tolerance: read file and recover
            if (!backup.createNewFile()) readLocalData(backupPath);
            server = new ServerSocket(this.localport);
            System.out.printf("<<<<< Member M%d is ready >>>>>\n", this.MID );
            do {
                socket = server.accept();
                accept(socket);// do random wait | go offline | act crazy (fast Byzantine)
               if (isOffline) {
                    System.out.println(this.MID + " will be offline soon \n\n");
                    server.close();
                }
               if (isByzantine){
                    System.out.println(this.MID + " will behave crazy\n\n");
                    // todo fast Byzantine
               }
               if (isRandomWait){
                    System.out.println(this.MID + " will Randomly Wait or stop\n\n");
                    int ran = new Random().nextInt(9)+1;
                    if (ran == this.MID){
                        socket.close();
                    } else {
                        socket.wait(ran * 1000);
                        System.out.println("M" + this.MID + " will wait " + ran +"s");
                    }
               }
            } while (!isOffline);  // todo check this condition
            System.out.println("M" + this.MID + " is offline." + this.socket.isClosed() + this.socket.isConnected());
        } catch (Exception e) {
            System.out.println("Error in Server Socket connection for M" + this.MID);
        }
    }

    /*  Phrase 1a :
        Member generate a proposal ID and send to acceptors*/
    public void prepare() {
        promisesReceived.clear();  // clear all promises that have received with earlier proposal(s)
        this.proposalMSG.generateProposalID();  // unique ID = time stamp + ID
//        this.proposalMSG.setType("Prepare");
        ProposalMSG toSendMSG = new ProposalMSG(this.MID,this.proposalMSG.getPID(),null,"Prepare");
        System.out.println( "[ Phrase 1 ] M" + this.MID + " :: sent " + toSendMSG.getProposalMSG() + "to all\n");

        // send prepare to all other members
        for (int i = 1; i <= 9; i++) {
            if ( i == this.MID)
                continue;
            sendPrepare(i,toSendMSG);  // send prepare(n) to M(i) except itself
//            if (isOffline) do something
        }
        System.out.printf("************ M%s :: End sent prepare to all ***************\n\n", this.MID);
    }

    protected synchronized void accept(Socket a_member_socket) {
        try {
            ProposalMSG inObject = inMSG(a_member_socket);
            String type = inObject.getType();

            switch (type){
                case "Prepare":     receivePrepare(inObject);   break;
                case "Promise":     receivePromise(inObject);   break;
                case "Accept":      receiveAccept(inObject);    break;
                case "Accepted":    receiveAccepted(inObject);  break;
                case "Nah":         receiveNah(inObject);       break;
                case "Whatever":    receiveWhatever(inObject);  break;
                case "Final":       receiveFinal(inObject);     break;
                default:
                    System.out.println(">> M"+ this.MID + "Error: Unknown MSG type!");
                    break;
            }
        } catch (Exception e) {
            System.out.println(">> M"+ this.MID + "Error in run accept");
        }
    }


    // sending prepare (pID) to the assigned one Member(ID)
    public void sendPrepare(int acceptorMID, ProposalMSG toSendMSG) {
        String str = outMSG(acceptorMID, toSendMSG);
        System.out.println("[ Phrase 1a ] M" + this.MID + str);
    }

    /*  phrase 1a : acceptor compare received proposalID and maxProposalID,
     if no maxPID exist, accept this proposalID as maxPID and accept value
     if proposalID > max, reply accepted ID and accepted value
     otherwise ignore this prepare message
     */
    public void receivePrepare(ProposalMSG proposalMSG) {
        int fromMID = proposalMSG.getMID();
        if ( maxProposalID == -1 || proposalMSG.getPID()>maxProposalID || this.promisedMSG == null){
            maxProposalID = proposalMSG.getPID();
            if (acceptedValue != null) // todo check??
                this.promisedMSG.setValue(acceptedValue);
            this.promisedMSG = new ProposalMSG(this.MID, proposalMSG.getPID(), acceptedValue,"Promise");
            sendPromise(fromMID, promisedMSG);
        }
        else if (proposalMSG.getPID()<maxProposalID) {
            sendNah(fromMID);
//        }
        // ProposalMSG previousID, Object acceptedValue
//        else if (this.promisedMSG != null && proposalMSG.equals(promisedMSG)) {  // duplicate message
//            sendPromise(fromMID, this.promisedMSG);
//            sendPromise(fromMID, proposalMSG, acceptedMSG, acceptedValue);
        } else {
            System.out.println("Empty case in receive Prepare");
        }
    }

    // 1b send promise to proposerUID
    public void sendPromise(int toUID, ProposalMSG proposalMSG)  {
        proposalMSG.setType("Promise");
        proposalMSG.setMID(this.MID);
        String str = outMSG(toUID, proposalMSG);
//        System.out.println("[ Phrase 1b.1 ] M" + this.MID + str);
    }

    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
     * it will set value to its proposal and send an Accept message (pid, V).
     */
    public void receivePromise(ProposalMSG promisedMSG) throws Exception {
        int fromMID = promisedMSG.getMID();
        System.out.printf("[ Phrase 1b ] M%d:: <- Receive %s \t(Total %d Promise)\n", this.MID,
                promisedMSG.getProposalMSG(), (promisesReceived.size()+1));

        int prevAcceptedPID = promisedMSG.getPID();
        Object prevAcceptedValue = promisedMSG.getValue();
        // return if receives from itself or already received
        if ( promisedMSG.equals(this.proposalMSG) || promisesReceived.contains(fromMID)){
            System.out.println("~~~~~ return ~~~~~");
            return;
        }
        // add memberID to promises received collection
        promisesReceived.add(fromMID);
//        System.out.println("[ 1b ] receive Promises total number - " + promisesReceived.size());

        if (lastAcceptedMSG == null || promisedMSG.getPID()> lastAcceptedMSG.getPID()) {
            lastAcceptedMSG = promisedMSG;
//            lastAcceptedMSG.createVotingValue();
            // todo check?
            System.out.println("*******  here here here last accepted set as " + lastAcceptedMSG.getProposalMSG());

            if (prevAcceptedValue != null)
                this.setProposalValue(prevAcceptedValue);  // change this value to inMSG.value
        }

        if (promisesReceived.size() +1 >= majority){
            System.out.printf("\n***** M%d has Received Majority Promises (%d)!! *****\n***** M%d will send Accept to" +
                            " M%d *****\n\n",
                    this.MID, (promisesReceived.size()+1), this.MID,fromMID);
            if (this.proposalMSG.getValue() != null){
                broadcast(this.proposalMSG,"Accept"); // broadcast proposalMSG (PID,V) to all
            }
        }
    }

    // 2a
    public void sendAccept(int toUID, ProposalMSG proposalMSG) {
        proposalMSG.setType("Accept");
        String str = outMSG(toUID, proposalMSG);
//        System.out.println("[ Phrase 2a ] M" + this.MID + str);
    }

    //    @Override
    /* phrase 2 : acceptor compare received pID and maxPID,
    * if acceptPID = maxPID = n, accept value = value , save to local backup, return
    * todo? otherwise return maxPID
    */
    public void receiveAccept(ProposalMSG proposalMSG) {
        int fromMID = proposalMSG.getMID();
        System.out.printf("[ Phrase 2a ] M%d receive Accept %s <- M%d\n", this.MID,proposalMSG.getProposalMSG(),
                fromMID);
        // todo check??
        if (promisedMSG == null || proposalMSG.getPID()>= maxProposalID) {
            promisedMSG = proposalMSG;
            this.acceptedMSG = new ProposalMSG (this.MID, proposalMSG.getPID(), proposalMSG.getValue(),
                    "Accepted");
            sendAccepted(fromMID, acceptedMSG);
        }
    }

    //2b
    public void sendAccepted(int toUID, ProposalMSG proposalMSG) {
        String str = outMSG(toUID, proposalMSG);
//        System.out.println("[ Phrase 2a ] M" + this.MID + str);
    }

    public void receiveAccepted(ProposalMSG proposalMSG) throws Exception {
        int fromMID = proposalMSG.getMID();
        Object acceptedValue = proposalMSG.getValue();
        System.out.printf("[ Phrase 2b ] M%d receive %s <- M%d\n", this.MID,proposalMSG.getProposalMSG(),
                fromMID);
        if (isComplete())
            return;

        ProposalMSG oldMID = acceptors.get(fromMID);

        if (oldMID != null && proposalMSG.getPID()<=oldMID.getPID())
            return;

        acceptors.put(fromMID, proposalMSG);
        System.out.println("[ 2b.2 ] receive Accepted total number - " + acceptors.size());

        if (oldMID != null) {
            Member oldProposal = proposals.get(oldMID);
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0)
                proposals.remove(oldMID);
        }

        if (!proposals.containsKey(proposalMSG))
            proposals.put(proposalMSG, new Member(0, 0, acceptedValue));

/*        Member thisProposal = proposals.get(proposalMSG);
        thisProposal.acceptCount    += 1;
        thisProposal.retentionCount += 1;

        if (thisProposal.acceptCount +1 >= majority ) { */
        if (acceptors.size()+1>=majority) { //todo check
            System.out.printf("*************** M%d has received Enough Accepted (%d)!! ************ ",this.MID ,
                    acceptedValue, (acceptors.size()+1));
            finalProposalMSG = new ProposalMSG(this.MID,proposalMSG.getPID(),acceptedValue,"Final");
            proposals.clear();
            acceptors.clear();
            determination(finalProposalMSG);  // acceptedValue
        }
    }


    private void receiveFinal(ProposalMSG inObject) {
        System.out.println(">> Final :: M" + MID + " receive Final and save to local file (Please check results)");
        saveToLocalData(inObject.getProposalMSG(),backupPath);
    }

    private void sendNah(int toMID) {
        ProposalMSG nah = new ProposalMSG(this.MID,-1,null,"Nah");
        outMSG(toMID, nah);
//        System.out.println(" M" + this.MID + "send Nah Nah Nah Nah Nah Nah Nah Nah to " + toMID);
    }

    private void receiveNah(ProposalMSG obj) {
        int fromMID = obj.getMID();
        System.out.println(">> M" + this.MID +" receive Nah Nah Nah Nah Nah Nah Nah from M" + fromMID);
        // todo check action
//        if (isByzantine) sendWhatever(fromMID);
//        prepare();
    }

    private void sendWhatever(int toMID) {
        System.out.println(">> M" + this.MID +" say Whatever ~ Whatever ~ Whatever ~ to M" + toMID);
        ProposalMSG whatever = new ProposalMSG(this.MID,-1,null,"Whatever");
        outMSG(toMID, whatever);
    }

    private void receiveWhatever(ProposalMSG inObject) {
        System.out.println("in receiver whatever (todo)");
    }


    private void determination(ProposalMSG proposalMSG) throws Exception {
        System.out.println("******** Determination Final value -> " + proposalMSG.getProposalMSG() + "********");
        String content = proposalMSG.getProposalMSG();
        saveToLocalData(content,backupPath);
        broadcast(proposalMSG,"Final");
    }

    // final value
    public void broadcast(ProposalMSG proposalMSG, String type) throws Exception {
        System.out.println(">> M" + this.MID + " Broadcast to all members");
        ProposalMSG toSendMSG = new ProposalMSG(this.MID,proposalMSG.getPID(), proposalMSG.getValue(),type);
        for (int i = 1; i <= 9; i++) {
            outMSG(i, toSendMSG);
        }
        if(type.equals("Final"))
            finalResultOutput(); // todo? what is next action ? how to known it is complete
    }

//    @Override
    // todo where to use this
    public boolean isComplete() {
        if (finalProposalMSG == null) return false;
        else return finalProposalMSG.getValue() != null;
    }

    // close previous connection if any
    private void cleanCloseSocket() {
        try {
            if (socket != null) {
                socket.shutdownOutput();
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete and create a new backup file at first connection
    public void deleteBackup(){
        File backup = new File(backupPath) ;
        if (backup.exists())
            backup.delete();
    }

    // As a proposer set its proposal value to the given value
    public void setProposalValue(Object value) {
//        if ( this.proposalMSG.getValue() == null ) //todo check??
            this.proposalMSG.setValue(value);
    }

    private void saveToLocalData(String content, String backupPath) {
        FileWriter writer;
        try {
            writer = new FileWriter (backupPath);
            writer.write (content);
            writer.flush ( );
            writer.close ( );
        } catch (IOException e) {
            e.printStackTrace ( );
        }
    }

    private String readLocalData(String FILE_TO_RECOVER) throws IOException {
        File file = new File(FILE_TO_RECOVER);
        String content = "";
        if (!file.exists()){
            file.createNewFile();
        } else if (file.exists() && file.length()==0) {
            System.out.println("Reading... Local File is EMPTY.");
        } else if(file.exists() && file.length()!=0){
            System.out.println("Reading Proposal from local file " + FILE_TO_RECOVER);
            content = "";
            BufferedReader br = new BufferedReader (new FileReader (file));
            String line = br.readLine ( );
            do {
                content += line + "\n";
                line = br.readLine ( );
            } while (line != null);
            br.close ( );
        }
        return content;
    }

    public void finalResultOutput() throws IOException {
        LinkedList<String> results = new LinkedList<>();
        String output = "";
        LinkedList<String> check = new LinkedList<>();
        for (int i = 1; i <= 9; i++) {
            String content = readLocalData(i + "data.txt");
            check.add(content);
            output += "Data saved in M" + i + ": " + content;
        }
        if (checkPaxos(check)){
            System.out.println();
            printNice(" Paxos Works! Output: ", output);
        } else
            printNice(" Paxos failed! Output: ", output);
    }

    public boolean checkPaxos( LinkedList<String> check ){
        for (int i = 0; i < check.size()-1; i++) {
            if (!check.get(i).equals(check.get(i+1))){
                System.out.println("Paxos Failed! Paxos Failed! Paxos Failed! ");
                return false;
            }
        }
        System.out.println("Paxos Works! Paxos Works! Paxos Works! ");
        return true;
    }

    public void printNice(String keyword, String content){
        String stars = "**********";
        if (keyword.isEmpty()) keyword = "*".repeat(6);
        String output = stars + keyword + stars + "\n" + content +
                stars + "*".repeat(keyword.length()) + stars;
        System.out.println(output);
    }
}