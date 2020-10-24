//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Member extends Communication {

    protected final int majority = 9 / 2 + 1;
    protected int MID; //memberID 1-9
    protected ProposalMSG proposalMSG = null; //p
    protected ProposalMSG lastAcceptedMSG = null; //p
    protected int acceptCount = 0;
    protected int retentionCount = 0;
    protected int maxProposalID = -1;
    protected Object acceptedValue; //a
    protected ProposalMSG promisedMSG; //a
    protected ProposalMSG acceptedMSG; //a
    protected Object finalValue = null; //l
    protected ProposalMSG finalProposalMSG = null; //l
    protected List<Integer> promisesReceived = new ArrayList<>();
    protected HashMap<ProposalMSG, Member> proposals = new HashMap<>();  //l
    protected HashMap<Integer, ProposalMSG> acceptors = new HashMap<>();  //l
    protected Socket socket;
    protected ServerSocket server;
    protected int localport;
    protected String backupPath = "";
    protected boolean isOffline = false;   // to simulate M2 M3 random go offline
    protected boolean isRandom = false;  // to simulate M4-M9 random network issue
    protected boolean isByzantine = false;  // for Byzantine's algorithm

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    Member(int MID) {
        this.MID = MID;
        this.proposalMSG = new ProposalMSG(MID);  // set PID = 0
        this.backupPath = MID + "data.txt";
        this.localport = MID * 1111;  // number * 1111
    }

    Member(int acceptCount, int retentionCount, Object acceptedValue) {
        this.acceptCount = acceptCount;
        this.retentionCount = retentionCount;
        this.acceptedValue = acceptedValue;
    }

    void connecting() {
        cleanCloseSocket();  // close previous connection if any
        deleteBackup();
        File backup = new File(backupPath);
        try {
            // fault tolerance: read file and recover
            if (!backup.createNewFile()) readLocalData(backupPath);
            server = new ServerSocket(this.localport);
            System.out.printf("<<<<< Member M%d is ready to work >>>>>\n", this.MID);
            do {
                socket = server.accept();
                accept(socket);

                 /* below parts will simulate random behaviors
                 1 isOffline -> M2, M3 goes offline
                 2 isRandom -> M4~M9 random response times: immediate;  medium; late; never
                 3 isByzantine -> lie, collude, or intentionally do not participate (fast Byzantine)
                 */
                if (isOffline) {
                    System.out.println("\n>> Warning:: M" + this.MID + " will be offline soon \n\n");
                    server.close();
                }
                if (isRandom) {
                    // immediate;  medium; late; never
                    int random = new Random().nextInt(10);
                    System.out.println("\n>> Warning:: M" + this.MID + " will have random response " + random + "\n");
                    if (random == this.MID) {
                        System.out.println("\n>> Random:: M" + this.MID + " - never response\n\n");
                        socket.close();  // never response
                    } else if (random % 2 == 0) {
                        System.out.println("\n>> Random:: M" + this.MID + " - immediately response\n\n");
                    } else {
                        if (random <= 5) {
                            System.out.println("\n>> Random:: M" + this.MID + " - medium response\n\n");
                            socket.wait(random * 1000);
                        } else {
                            System.out.println("\n>> Random:: M" + this.MID + " - late response\n\n");
                            socket.wait(random * 3000);
                            System.out.println("M" + this.MID + " will wait " + random + "s");
                        }
                    }
                }
                if (isByzantine) {
                    System.out.println("\n>> Warning:: M" + this.MID + " will behave crazy\n\n");
                    // todo fast Byzantine
                }
            } while (true);
//            } while (!isOffline);  // todo check this condition
//            System.out.println("M" + this.MID + " is offline." + this.socket.isClosed() + this.socket.isConnected());
        } catch (Exception e) {
            System.out.println("Error in Server Socket connection for M" + this.MID);
        }
    }

    /*  Phrase 1a :
        Member generate a proposal ID and send to acceptors*/
    public void prepare() {
        this.promisesReceived.clear();  // clear all promises that have received with earlier proposal(s)
        this.proposalMSG.generateProposalID();  // unique ID = time stamp + ID
        ProposalMSG toSendMSG = new ProposalMSG(this.MID, this.proposalMSG.getPID(), null, "Prepare");
        try {
//            System.out.print("\n[ Phrase 1 start ] :: \n[ 1a ] ");
            broadcast(toSendMSG, "Prepare"); // send prepare(n) to all members include itself
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("\n ************ End :: M%s :: sent Prepare Request to all ***************\n\n", this.MID);
    }

    protected synchronized void accept(Socket a_member_socket) {
        try {
            ProposalMSG inObject = inMSG(a_member_socket);
            String type = inObject.getType();

            switch (type) {
                case "Prepare":
                    receivePrepare(inObject);
                    break;
                case "Promise":
                    receivePromise(inObject);
                    break;
                case "Accept":
                    receiveAccept(inObject);
                    break;
                case "Accepted":
                    receiveAccepted(inObject);
                    break;
                case "Nah":
                    receiveNah(inObject);
                    break;
                case "Whatever":
                    receiveWhatever(inObject);
                    break;
                case "Final":
                    receiveFinal(inObject);
                    break;
                default:
                    System.out.println(">> M" + this.MID + "Error: Unknown MSG type!");
            }
        } catch (Exception e) {
            System.out.println(">> M" + this.MID + "Error in run accept");
            e.printStackTrace();
        }
    }

    /*  phrase 1a : acceptor compare received proposalID and maxProposalID,
     if no maxPID exist, accept this proposalID as maxPID and accept value
     if proposalID > max, reply accepted ID and accepted value
     otherwise ignore this prepare message
     */
    public void receivePrepare(ProposalMSG proposalMSG) {
        int proposerMID = proposalMSG.getMID();
        if (maxProposalID == -1 || proposalMSG.getPID() > maxProposalID || this.promisedMSG == null) {
            maxProposalID = proposalMSG.getPID();
            // todo check?? sendPromise (acceptedPID, acceptedValue);
            this.promisedMSG = new ProposalMSG(this.MID, maxProposalID, acceptedValue, "Promise");
            System.out.printf("[ 1b ] Acceptor M%d:: -> M%d send %s\n", this.MID, proposerMID,
                    promisedMSG.getProposalMSG());
            outMSG(proposerMID, promisedMSG);
        } else if (proposalMSG.getPID() < maxProposalID) {
            sendNah(proposerMID);//ignore the prepare request or being nice to return "Nah"
//        }
            // ProposalMSG previousID, Object acceptedValue
//        else if (this.promisedMSG != null && proposalMSG.equals(promisedMSG)) {  // duplicate message
//            sendPromise(fromMID, this.promisedMSG);
//            sendPromise(fromMID, proposalMSG, acceptedMSG, acceptedValue);
        } else {
            System.out.println("Empty case in receive Prepare");
        }
    }

    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
     * it will set value to its proposal and send an Accept message (pid, V).
     */
    public void receivePromise(ProposalMSG promiseMSG) {
//        System.out.println("\n [ Receive a promise ] :: " + promiseMSG.getProposalMSG());
        int acceptorMID = promiseMSG.getMID();

        int prevAcceptedPID = promiseMSG.getPID();
        Object prevAcceptedValue = promiseMSG.getValue();
        // return if receives from itself or already received
        if (promiseMSG.equals(this.proposalMSG) || promisesReceived.contains(acceptorMID)) {
            System.out.println("~~~~~ Duplicate promise & return ~~~~~");
            return;
        }
        // add memberID to promises received collection
        promisesReceived.add(acceptorMID);
        System.out.printf("[ 1b ] Proposer M%d:: <- Receive %s \t( M%d ::Total %d Promise)\n", this.MID,
                promiseMSG.getProposalMSG(), acceptorMID, promisesReceived.size());

        if (lastAcceptedMSG == null || promiseMSG.getPID() > lastAcceptedMSG.getPID()) {
            lastAcceptedMSG = promiseMSG;
            System.out.println("[ Update ] new PID found & set -> lastAcceptedMSG " + lastAcceptedMSG.getProposalMSG());

            if (prevAcceptedValue != null)
                this.proposalMSG.setValue(prevAcceptedValue);  // change this value to inMSG.value
        }

        if (promisesReceived.size() == majority) {
            System.out.printf("\n<<<<< M%d has Received Majority Promises (%d)!! send Accept Request to all members >>>>>\n\n", this.MID, promisesReceived.size());

            if (lastAcceptedMSG.getValue() != null)
                this.proposalMSG.setValue(lastAcceptedMSG.getValue());


            if (this.proposalMSG.getValue() != null) {
                try {
                    broadcast(this.proposalMSG, "Accept"); // broadcast proposalMSG (PID,V) to all
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.printf("\n************ End :: M%s :: sent Accept Request to all ***************\n\n",
                            this.MID);
                }
            }
        }
    }

    /* phrase 2a : acceptor compare received pID and maxPID,
     * if acceptPID = maxPID = n, accept value = value , save to local backup, return
     * otherwise return maxPID
     */
    public void receiveAccept(ProposalMSG acceptRequestMSG) {
        int proposerMID = acceptRequestMSG.getMID();
        System.out.printf("[ 2a ] M%d receive %s \n", this.MID, acceptRequestMSG.getProposalMSG());
        // todo check??
        if (promisedMSG == null || acceptRequestMSG.getPID() >= maxProposalID) {
            if (maxProposalID != acceptRequestMSG.getPID()) {
                maxProposalID = acceptRequestMSG.getPID();
                System.out.println("[ Update ] maxProposalID = " + maxProposalID);
            }
            promisedMSG = acceptRequestMSG;
            this.acceptedMSG = new ProposalMSG(this.MID, acceptRequestMSG.getPID(), acceptRequestMSG.getValue(),
                    "Accepted");
            saveToLocalData(acceptedMSG.getProposalMSG());  // save to local file
        } else {
            this.acceptedMSG = new ProposalMSG(this.MID, maxProposalID, null, "Accepted");
        }
        outMSG(proposerMID, acceptedMSG);  // send accepted MSG to proposer
    }

    /* phrase 2b : must Accept (n, v) iff  !promised in Phase 1b || > maxPID
    register AcceptMSG.V -> acceptedValue & send  AcceptedMSG to the Proposer (Learner)
    Else ignore.*/
    public void receiveAccepted(ProposalMSG acceptedMSG) throws Exception {
        int acceptorMID = acceptedMSG.getMID();
        int acceptorPID = acceptedMSG.getPID();
        Object acceptedValue = acceptedMSG.getValue();
        System.out.printf("[ Phrase 2b ] M%d receive %s\n", this.MID, acceptedMSG.getProposalMSG());

        if (isComplete()) return;

        ProposalMSG oldMID = acceptors.get(acceptorMID);

        // if duplicate msg or smaller PID -> ignore
        if (oldMID != null && acceptedMSG.getPID() <= oldMID.getPID()) return;

        else acceptors.put(acceptorMID, acceptedMSG);

        /*// delete this duplicate msg from proposals map
        if (oldMID != null) {
            Member oldProposal = proposals.get(oldMID);
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0)
                proposals.remove(oldMID);
        }

        if (!proposals.containsKey(acceptedMSG))  // if unique (not in proposal)
            proposals.put(acceptedMSG, new Member(0, 0, acceptedValue));

        Member thisProposal = proposals.get(acceptedMSG);
        thisProposal.acceptCount +=1;
        thisProposal.retentionCount +=1;*/

        System.out.println("[ 2b ] receive Accepted total number - " + acceptors.size());
//        if (thisProposal.acceptCount +1 == majority ) { //todo check
        if (acceptors.size() >= majority) {
            System.out.printf("*************** M%d has received Enough Accepted (%d)!! ************ \n", this.MID,
                    acceptedValue, acceptors.size());

            if (acceptorPID > this.proposalMSG.getPID()) {
                System.out.printf("\n**** M%d Found new Proposal with larger PID (%d)from M%d *****\n", this.MID,
                        acceptorPID, acceptorMID);
                prepare(); // todo check
                return;
            } else {
                finalProposalMSG = new ProposalMSG(this.MID, acceptedMSG.getPID(), acceptedValue, "Final");
                proposals.clear();
                acceptors.clear();
                agreement(finalProposalMSG);  // acceptedValue
            }
        }
    }

    private void receiveFinal(ProposalMSG inObject) {
        System.out.println(">> Final :: M" + MID + " receive Final and save to local file (Please check results)");
        saveToLocalData(inObject.getProposalMSG());
        // todo stop socket?
    }

    private void sendNah(int toMID) {
        ProposalMSG nah = new ProposalMSG(this.MID, -1, null, "Nah");
        outMSG(toMID, nah);
//        System.out.println(" M" + this.MID + "send Nah Nah Nah Nah Nah Nah Nah Nah to " + toMID);
    }

    private void receiveNah(ProposalMSG obj) {
        int fromMID = obj.getMID();
        System.out.println(">> M" + this.MID + " receive Nah Nah Nah Nah Nah Nah Nah from M" + fromMID);
        // todo go back to stage 1
//        prepare();
//        if (isByzantine) sendWhatever(fromMID);
    }

    private void sendWhatever(int toMID) {
        System.out.println(">> M" + this.MID + " say Whatever ~ Whatever ~ Whatever ~ to M" + toMID);
        ProposalMSG whatever = new ProposalMSG(this.MID, -1, null, "Whatever");
        outMSG(toMID, whatever);
    }

    private void receiveWhatever(ProposalMSG inObject) {
        System.out.println("in receiver whatever (todo)");
    }

    private void agreement(ProposalMSG proposalMSG) throws Exception {
        System.out.println("\n******** Agreement on Final value -> " + proposalMSG.getProposalMSG() + "********\n");
        saveToLocalData(proposalMSG.getProposalMSG());
        broadcast(proposalMSG, "Final");
    }

    // final value
    public void broadcast(ProposalMSG proposalMSG, String type) throws Exception {
        ProposalMSG toSendMSG = new ProposalMSG(this.MID, proposalMSG.getPID(), proposalMSG.getValue(), type);
        if (type.equals("Prepare")) {
            System.out.println("\n[ Phrase 1 start ] :: [ 1a ] M" + this.MID + " Broadcast " + proposalMSG.getProposalMSG());
        } else if (type.equals("Accept")) {
            System.out.println("\n[ Phrase 2 Start ] :: [ 2a ] M" + this.MID + " Broadcast " + proposalMSG.getProposalMSG());
        }


        for (int i = 1; i <= 9; i++) {
            outMSG(i, toSendMSG);
        }
        if (type.equals("Final"))
            finalResultOutput(); // todo? what is next action ? how to known it is complete
    }

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
    public void deleteBackup() {
        File backup = new File(backupPath);
        if (backup.exists())
            backup.delete();
    }

    private void saveToLocalData(String content) {
        FileWriter writer;
        try {
            writer = new FileWriter(backupPath);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readLocalData(String FILE_TO_RECOVER) throws IOException {
        File file = new File(FILE_TO_RECOVER);
        String content = "";
        if (!file.exists()) {
            file.createNewFile();
        } else if (file.exists() && file.length() == 0) {
            System.out.println("Reading... Local File is EMPTY.");
        } else if (file.exists() && file.length() != 0) {
            System.out.println("Reading Proposal from local file " + FILE_TO_RECOVER);
            content = "";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            do {
                content += line + "\n";
                line = br.readLine();
            } while (line != null);
            br.close();
        }
        return content;
    }

    public void finalResultOutput() throws IOException {
        String output = "";
        LinkedList<String> check = new LinkedList<>();
        for (int i = 1; i <= 9; i++) {
            String content = readLocalData(i + "data.txt");
            if (!content.isEmpty()) {
                check.add(content);
                output += "Data saved in M" + i + ": " + content;
            }
        }
        if (checkPaxos(check)) {
            System.out.println();
            printNice(" Paxos Works! Output: ", output);
        } else
            printNice(" Paxos failed! Output: ", output);
    }

    public boolean checkPaxos(LinkedList<String> check) {
        for (int i = 0; i < check.size() - 1; i++) {
            if (!check.get(i).equals(check.get(i + 1))) {
                System.out.println("Paxos Failed! Paxos Failed! Paxos Failed! ");
                return false;
            }
        }
        System.out.println("Paxos Works! Paxos Works! Paxos Works! ");
        return true;
    }

    public void printNice(String keyword, String content) {
        String stars = "*************";
        if (keyword.isEmpty()) keyword = "*".repeat(6);
        String output = "\n\n" + stars + keyword + stars + "\n" + content +
                stars + "*".repeat(keyword.length()) + stars + "\n\n";
        System.out.println(output);
    }
}