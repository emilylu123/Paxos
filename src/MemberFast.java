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

public class MemberFast extends Communication {
    protected final int majority = 9 / 2 + 1;
    protected int MID; //memberID 1-9
    protected ProposalMSG proposalMSG = null; //p
    protected int lastPromisedPID = -1; //p
    protected int maxProposalID = -1;
    protected Object acceptedValue = null; //a
    protected ProposalMSG promisedMSG = null; //a
    protected ProposalMSG acceptedMSG = null; //a
    protected Object finalValue = null; //l
    protected ProposalMSG finalProposalMSG = null; //l
    protected List<Integer> promisesReceived;
    protected HashMap<Integer, ProposalMSG> proposals;  //l
    protected ArrayList<ProposalMSG> acceptorList;
    protected Socket socket;
    protected ServerSocket server;
    protected int localport;
    protected String backupPath = "";
    protected boolean isOffline = false;   // to simulate M2 M3 random go offline
    protected boolean isByzantine = false;  // for Byzantine's algorithm
    protected boolean isDone = false;
    protected int randomResponse = 0;

    // M1, M2, M3 will proposal for themselves, M4-M9 will randomly choose one from M1-M3 as proposal value
    MemberFast(int MID) {
        this.MID = MID;
        this.proposalMSG = new ProposalMSG(MID);  // set initial PID = 0
        this.backupPath = MID + "data.txt";
        this.localport = MID * 1111;  // number * 1111
    }

    void connecting() {
        cleanCloseSocket();  // close previous connection if any
        deleteBackup();
        try {
            // fault tolerance: read file and recover
            readLocalData(backupPath);
            server = new ServerSocket(this.localport);
            do {
                socket = server.accept();
                 /* below parts will simulate random behaviors
                 1 isOffline -> M2, M3 goes offline
                 2 isRandom -> M4~M9 random response times: immediate;  medium; late; never
                 3 isByzantine -> lie, collude, or intentionally do not participate (fast Byzantine)
                 */
                if (isOffline) {
                    server.close();
                    printNice(" Warning:: ", "       M" + this.MID + " is offline");
                    break;
                }
                // immediate;  medium; late; never
                if (randomResponse == 1) {  // medium response
                    Thread.sleep(1000);
                } else if (randomResponse == 2) { // late response
                    Thread.sleep(3000);
                } else if (randomResponse != 0) { // never response
                    socket.close();
                    printNice(" Warning:: ", "       M" + this.MID + " is never response");
                    break;
                }
                accept(socket);
            } while (!isDone);
            if (this.MID == 1 || this.randomResponse == 1 || this.randomResponse == 2)
                finalResultOutput();
        } catch (Exception e) {
            System.out.println("Error in Server Socket connection for M" + this.MID);
        }
    }

    /*  Phrase 1a :
        Member generate a proposal ID and send to acceptors*/
    public void prepareToLeader() {
        // clear all data that has received with earlier proposal(s) to restart a proposal
        this.promisesReceived = new ArrayList<>();
        this.proposals = new HashMap<>();  //l
        this.acceptorList = new ArrayList<>(); //l
        this.proposalMSG.generateProposalID();  // create unique PID = last digits time stamp + ID
        ProposalMSG toSendMSG = new ProposalMSG(this.MID, this.proposalMSG.getPID(), this.MID,
                "PrepareToLeader");
        outMSG(1, toSendMSG);
    }

    public void receivePrepareToLeader(ProposalMSG in) throws Exception {
        int id = in.getMID();
        ProposalMSG prepareMSG = new ProposalMSG(in.getMID(), in.getPID(), null, "Prepare");
        proposals.put(id, prepareMSG);
        // clear all data that has received with earlier proposal(s) to restart a proposal
        broadcast(prepareMSG, "Prepare");
    }

    protected synchronized void accept(Socket a_member_socket) {
        try {
            ProposalMSG inObject = inMSG(a_member_socket);
            String type = inObject.getType();

            switch (type) {
                case "PrepareToLeader":
                    receivePrepareToLeader(inObject);
                    break;
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
                case "Check":
                    receiveCheck(inObject);
                    break;
                case "Final":
                    receiveFinal(inObject);
                    break;
                default:
                    System.out.println(">> M" + this.MID + "Error:: Unknown MSG type! ");
            }
        } catch (Exception e) {
            System.out.println(">> M" + this.MID + "Error in accept loop");
        }
    }

    /*  phrase 1a : acceptor compare received proposalID and maxProposalID,
     if no maxPID exist, accept this proposalID as maxPID and accept value
     if proposalID > max, reply accepted ID and accepted value
     otherwise ignore this prepare message
     */
    public void receivePrepare(ProposalMSG prepareMSG) {
        int proposerMID = prepareMSG.getMID();
        int proposerPID = prepareMSG.getPID();
        // ProposalMSG previousID, Object acceptedValue
        if (promisedMSG != null && proposerPID == maxProposalID) {  // duplicate message
            System.out.println("[ 1b ] Duplicate prepare MSG");
            outMSG(proposerMID, promisedMSG);
        } else if (promisedMSG == null || proposerPID > maxProposalID) {
            maxProposalID = proposerPID;  // update maxPID
            promisedMSG = new ProposalMSG(MID, maxProposalID, acceptedValue, "Promise"); // create promisedMSG
            System.out.printf("[ 1b ] Acceptor M%d:: send to -> M%d %s\n", MID, proposerMID, promisedMSG.getProposalMSG());
            outMSG(1, promisedMSG);
        } else if (proposerPID < maxProposalID) {
            // ignore the prepare request or being nice to return "Nah"
            // System.out.printf("[ 1b ] Acceptor M%d:: send Nah Nah Nah Nah -> M%d \n", this.MID, proposerMID);
//            ProposalMSG nah = new ProposalMSG(MID, maxProposalID, null, "Nah");
//            outMSG(1, nah);
        } else {
            System.out.println("[ 1b ] Error:: Empty case in receive Prepare");
        }
    }

    /* phrase 2a : If a Proposer receives a majority of Promises from Acceptors,
     * it will set value to its proposal and send an Accept message (pid, V).
     */
    public void receivePromise(ProposalMSG receivedMSG) {
        int acceptorMID = receivedMSG.getMID();
        int receivedPID = receivedMSG.getPID();
        Object receivedValue = receivedMSG.getValue();
        boolean isValueChanged = false;

        if (receivedPID > lastPromisedPID) { // update PID and accepted value
            lastPromisedPID = receivedPID;
            promisesReceived.clear();
            this.proposalMSG.setPID(receivedPID);
            if (receivedValue == null) { // fast paxos return "any"
                int randomValue = new Random().nextInt(3) + 1;
                printNice("[ Fast Paxos ]", "M" + this.MID + " submit ANY Value from proposals: " + randomValue);
                this.proposalMSG.setValue(randomValue);
            } else {
                this.proposalMSG.setValue(receivedValue);
                System.out.println("[ 1b -> Update ] Proposer M" + this.MID + " found new PID & set value -> " + proposalMSG.getValue());
            }
        }

        // add memberID to promises received collection
        if (!promisesReceived.contains(acceptorMID))
            promisesReceived.add(acceptorMID);

        if (promisesReceived.size() == majority) {
            try {
                System.out.println("broadcast accept " + proposalMSG.getProposalMSG());
                broadcast(this.proposalMSG, "Accept"); // broadcast proposalMSG (PID,V) to all
            } catch (Exception e) {
                System.out.println("Error in send accept ");
            }
        }
    }

    /* phrase 2a : acceptor compare received pID and maxPID,
     * if acceptPID = maxPID = n, accept value = value , save to local backup, return
     * otherwise return maxPID
     */
    public void receiveAccept(ProposalMSG acceptRequestMSG) {
        if (acceptRequestMSG.getValue() == null) {
            System.out.println("Error: in receive Accept" + acceptRequestMSG.getProposalMSG());
            return;
        }
        int proposerMID = acceptRequestMSG.getMID();
        int proposerPID = acceptRequestMSG.getPID();
        Object proposerValue = acceptRequestMSG.getValue();
        if (promisedMSG == null || proposerPID >= maxProposalID || acceptedValue == null) {
            if (proposerPID > maxProposalID) {
                maxProposalID = proposerPID;
                System.out.printf("< 2a Update > M%d Found maxProposalID in accept(PID: %d)\n", MID, maxProposalID);
            }
            acceptedValue = proposerValue;

            // do Byzantine cheating on purpose: change accepted value
            if (isByzantine) {
                int ran;
                do {
                    ran = new Random().nextInt(3) + 1;
                }
                while ((int) this.acceptedValue == ran);
                printNice(" Warning:: Byzantine Traitor ",
                        "   M" + this.MID + " will change accepted Value from " + acceptedValue + " to " + ran);
                this.acceptedValue = ran;
            }
            promisedMSG = new ProposalMSG(MID, maxProposalID, acceptedValue, "Promise"); // update promisedMSG
            acceptedMSG = new ProposalMSG(MID, maxProposalID, acceptedValue, "Accepted");
        }
        // return macPID only to proposer because proposerPID is smaller than maxPID
        else {
            acceptedMSG = new ProposalMSG(MID, maxProposalID, null, "Accepted");
        }
        System.out.printf("[ 2a ] M%d send %s to M%d\n", MID, acceptedMSG.getProposalMSG(), proposerMID);
        outMSG(proposerMID, acceptedMSG);  // send accepted MSG to proposer
    }

    /* phrase 2b : must Accept (n, v) iff  !promised in Phase 1b || > maxPID
    register AcceptMSG.V -> acceptedValue & send  AcceptedMSG to the Proposer (Learner)
    Else ignore.*/
    public void receiveAccepted(ProposalMSG acceptedMSG) throws Exception {
        if (isComplete()) return;
        int acceptorMID = acceptedMSG.getMID();
        int acceptorPID = acceptedMSG.getPID();

        ProposalMSG oldMID = null;
//        ProposalMSG oldMID = acceptors.get(acceptorMID);
        for (int i = 0; i < acceptorList.size(); i++) {
            if (acceptorMID == acceptorList.get(i).getMID()) {
                oldMID = acceptorList.get(i);
                break;
            }
        }
        // if duplicate msg or smaller PID means outdated MSG -> ignore
        if (oldMID != null && acceptorPID <= oldMID.getPID()) return;
        if (acceptedValue != null) {
            acceptorList.add(acceptedMSG);
            System.out.println("[ 2b ] M" + this.MID + " receive Accepted from M" + acceptedMSG.getMID() + " -> " +
                    acceptedValue + " total number - " + acceptorList.size());
        }

        if (acceptorList.size() == majority) {
            System.out.printf("\n******** M%d has received Enough Accepted (%d) for Value %s !! ********\n\n", MID,
                    acceptorList.size(), acceptedValue);
            // check PID in acceptors
            int max = 0;
            int temp;
            for (int i = 0; i < acceptorList.size(); i++) {
                if (acceptorList.get(i) != null) {
                    temp = acceptorList.get(i).getPID();
                    if (temp > max) {
                        max = temp;
                    }
                }
            }

            if (max > this.proposalMSG.getPID()) {
                System.out.println(" M" + MID + "Found larger PID in accepted MSG\n");
            } else {
                printNice(" [ Start Learn ] ", " M" + this.MID + " has Received Majority Accepted ( "
                        + promisesReceived.size() + " )\n Send Final Agreement to all members");
                finalValue = checkByzantine();
                finalProposalMSG = new ProposalMSG(this.MID, proposalMSG.getPID(), finalValue, "Final");
                proposals.clear();
                acceptorList.clear();
                finalAgreement(finalProposalMSG);  // acceptedValue
            }
        }
    }

    public Object checkByzantine() {
        String check = "";
        if (acceptorList.size() == 0) return null;
        int checkedValue1 = (int) acceptorList.get(0).getValue();
        Object checkedValue2 = null;
        int countValue1 = 0;
        int countValue2 = 0;

        for (int i = 0; i < acceptorList.size(); i++) {
            check += acceptorList.get(i).getValue() + "\n";
            int temp = (int) acceptorList.get(i).getValue();
            if (temp == checkedValue1) {
                countValue1++;
            } else {
                checkedValue2 = acceptorList.get(i).getValue();
                countValue2++;
            }
        }

        if (countValue1 > countValue2) {
            printNice(" [ Check Byzantine Traitor ] ", check + "Final value is decided: " + checkedValue1);
            return checkedValue1;
        } else {
            printNice(" [ Check Byzantine Traitor ] ", check + "Final value : " + checkedValue2);
            return checkedValue2;
        }
    }

    private void receiveCheck(ProposalMSG inObject) {
        checkByzantine();
    }

    private void finalAgreement(ProposalMSG proposalMSG) throws Exception {
        System.out.println("\n******** Agreement on Final value -> " + proposalMSG.getProposalMSG() + "********\n");
        broadcast(proposalMSG, "Final");
    }

    protected void receiveFinal(ProposalMSG inObject) throws IOException {
        if (isDone) return;
        if (inObject.getValue() != null)
            saveToLocalData((int) inObject.getValue());
//        finalResult();
        isDone = true;
        cleanCloseSocket();
    }

    // final value
    public void broadcast(ProposalMSG proposalMSG, String type) throws Exception {
        ProposalMSG toSendMSG = new ProposalMSG(this.MID, proposalMSG.getPID(), proposalMSG.getValue(), type);
        if (type.equals("Prepare")) {
            printNice(" [ 1a ] M" + this.MID + " Start Phrase 1 - Prepare() ",
                    " Broadcast " + proposalMSG.getProposalMSG() + " to all members");
        } else if (type.equals("Accept")) {
            printNice(" [ 2a ] M" + this.MID + " Start Phrase 2 - Accept() ", " M" + this.MID
                    + " has Received Majority Promises ( " + promisesReceived.size() + " )\n Send Accept Request "
                    + proposalMSG.getProposalMSG() + " to all members");
        }
        for (int i = 1; i <= 9; i++) {
            outMSG(i, toSendMSG);
        }
    }

    public boolean isComplete() {
        return finalProposalMSG != null;
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
            System.out.println("Error in clean close Socket");
        }
    }

    // delete and create a new backup file at first connection
    public void deleteBackup() {
        File backup = new File(backupPath);
        if (backup.exists()) {
            backup.delete();
            System.out.println(backupPath + " is deleted.");
        }
    }

    private void saveToLocalData(int content) {
        String toWriteData = String.valueOf(content);
        FileWriter writer;
        try {
            writer = new FileWriter(backupPath);
            writer.write(toWriteData);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error in save to local Data. Please Check connection of M" + this.MID);
        }
    }

    private String readLocalData(String FILE_TO_RECOVER) throws IOException {
        File file = new File(FILE_TO_RECOVER);
        String content = "";
        if (!file.exists()) {
            file.createNewFile();
        } else if (file.exists() && file.length() == 0) {
//            System.out.println("Reading... Local File is EMPTY.");
        } else if (file.exists() && file.length() != 0) {
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
            } else {
                output += "Data saved in M" + i + ": N/A ( Possibly offline. Please check" +
                        " member response status at the beginning )\n";
            }
        }
        if (checkPaxosResults(check)) {
            printNice(" M" + MID + " :: Output: ", "Test Result : << PASS >>\n\n" + output);
        } else
            printNice(" M" + MID + " :: Output: ", "Test Result : << FAIL >>\n\n" + output);
    }

    public boolean checkPaxosResults(LinkedList<String> check) {
        for (int i = 0; i < check.size() - 1; i++) {
            if (!check.get(i).equals(check.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    public void printNice(String keyword, String content) {
        String stars = "*************";
        if (keyword.isEmpty()) keyword = "*".repeat(6);
        String output = "\n" + stars + keyword + stars + "\n" + content + "\n" +
                stars + "*".repeat(keyword.length()) + stars + "\n";
        System.out.println(output);
    }
}