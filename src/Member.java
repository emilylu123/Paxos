//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Member implements Serializable, Runnable, MemberInterface {
    private int mID;
    private int id = Math.abs(new Random().nextInt());//Server ID
    private Serializable message;
    private int maxProposalID = -1;//Currently received proposal number
    private int acceptProposalID = -1;//The proposal number that has been agreed
    private int acceptValue= -1;//The value corresponding to the proposal number that has been agreed
    private Election election;

    protected String proposerUID;
    protected final int  quorumSize = -1;
    protected ProposalID proposalID;
    protected Object proposedValue = null;
    protected ProposalID lastAcceptedID = null;
    protected HashSet<String> promisesReceived = new HashSet<String>();

    protected ProposalID promisedID;
    protected ProposalID acceptedID;
    protected Object acceptedValue;

    private Object finalValue = null;
    private ProposalID finalProposalID = null;
    private HashMap<ProposalID, Proposal> proposals       = new HashMap<ProposalID, Proposal>();
    private HashMap<String,  ProposalID> acceptors       = new HashMap<String, ProposalID>();

    class Proposal {
        int    acceptCount;
        int    retentionCount;
        Object value;

        Proposal(int acceptCount, int retentionCount, Object value) {
            this.acceptCount    = acceptCount;
            this.retentionCount = retentionCount;
            this.value          = value;
        }
    }

    Member (int mID, Serializable message){
        this.mID = mID;
        this.message = message;
    }

    Member(Election election) {
        this.election = election;
    }

    // phrase 1 prepare
    public synchronized Object[] prepare(int acceptN) {
        System.out.println("----------------------Dividing line---------------------");
        System.out.println(acceptN +"Request Proposal: "+this.id+"max Proposal ID:"+this.maxProposalID +" accept " +
                "Proposal ID "+this.acceptProposalID +" accept V"+this.acceptValue);

        /*This simulates a broken network. If it is random, it will break the network*/
        Random random = new Random();
        int state = random.nextInt(10);
        if(state == 2) return null;

        /*The following is normal connection*/
        // If you have not accepted the proposal before, return null directly
        if(maxProposalID == -1) {
            this.maxProposalID = acceptN;//The current received proposal number = the current proposal number
            return new Object[]{"pok", null, null};
        }

        if(maxProposalID > acceptN) {
            //Because the current application proposal number is smaller than the agreed proposal number, the proposal is not accepted.
            return new Object[]{"error", null, null};
        }

        if(acceptN > maxProposalID) {//Identifies whether the new ly applied proposal is a new  proposal
            this.maxProposalID = acceptN;//The current received proposal number = the current proposal number
            if(this.acceptProposalID == -1) {//If no proposal has been passed before, return  null
                return new Object[]{"pok", null, null};
            }else{ //If you agreed to the proposal before, return  the last agreed proposal number and proposal value
                return new Object[]{"pok", this.acceptProposalID, this.acceptValue};
            }
        }
        return  null;
    }

    public synchronized String accept(int acceptN,int acceptV) {
        // First the current proposal number acceptN can not be less than maxN
        if(maxProposalID <= acceptN) {
            maxProposalID = acceptN;
            this.acceptProposalID = acceptN;
            this.acceptValue = acceptV;
            return "aok";
        }
        return "error";
    }

    //Conduct elections
    public void paxos(Member member) {
        // Get a legal collection
        List<Member> council = election.getLegalCouncil();
        int  _acceptN = 0;
        int  _acceptV = 0;
        int count = 0;
        int  cid = Paxos.pID.getPID();
        for(Member _member : council) {
            Object[] prepare = _member.prepare (cid);//Request to submit a proposal
            if(prepare == null)
                continue;
            System.out.println(cid +"("+ _acceptN +":"+ _acceptV +")"+" return s the proposal: "+ _member.id+".........."+ prepare[0] +"........"+ prepare[1] +"..."+ prepare[2]);
            String state = (String) prepare[0];
            if("pok".equals(state)) {//If you receive an application
                count++;
                if(_acceptN == 0&& prepare[1] == null) {
                    // Generate a new  acceptV
                    _acceptV = member.id;
                }else{
                    int  acceptN = (int ) prepare[1];
                    int  acceptV = (int ) prepare[2];

                    // Use the return ed acceptV
                    if(acceptN >= _acceptN) {
                        _acceptN = acceptN;
                        _acceptV = acceptV;
                    }
                }
            }
        }
        //If the response received exceeds half, the proposal is formally submitted.
        if(count >= election.getMajority ()) {
            _acceptN = cid;
            // Get a legal collection
            List<Member> computers1 = election.getLegalCouncil();
            int acount = 0;
            for(Member _member : computers1) {
                System.out.println(_acceptN +"("+ _acceptV +")"+" Submit proposal: "+ _member.id+".........."+ _member.maxProposalID +"...... .."+ _member.acceptProposalID +"......"+ _member.acceptValue);
                String accept = _member.accept(_acceptN,_acceptV);//Request to submit a proposal
                if("aok".equals(accept)) {
                    acount++;
                }
            }
            if(acount >= election.getMajority ()) {
                System.out.println("Proposal is mostly passed: "+ _acceptN +"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~"+ _acceptV);
                for(Member _member : computers1) {
                    System.out.println(_member.id+".........."+ _member.maxProposalID +"........"+ _member.acceptProposalID +"......"+ _member.acceptValue);
                }
            }
        }
    }

    //Start command
    public void run() {
        Random random = new  Random();
        try {
            Thread.sleep(random.nextInt(10) * 1000); // random delay for a few seconds, simulating the message sending process or the boot process
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Member member = this;
        election.register(member);//Register to the boot cluster
        paxos(member);
    }

    @Override
    public void prepare() {
        promisesReceived.clear();
        proposalID.incrementNumber();
        sendPrepare(proposalID);
    }

    @Override
    public void setProposal(Object value) {
        if ( proposedValue == null )
            proposedValue = value;
    }

    @Override
    public void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID, Object prevAcceptedValue) {
        if ( !proposalID.equals(this.proposalID) || promisesReceived.contains(fromUID) )
            return;

        promisesReceived.add( fromUID );

        if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID)) {
            lastAcceptedID = prevAcceptedID;
            if (prevAcceptedValue != null)
                proposedValue = prevAcceptedValue;
        }

        if (promisesReceived.size() == quorumSize){
            if (proposedValue != null)
                sendAccept(this.proposalID, proposedValue);
        }
    }

    @Override
    public void receivePrepare(String fromUID, ProposalID proposalID) {
        if (this.promisedID != null && proposalID.equals(promisedID)) { // duplicate message
            sendPromise(fromUID, proposalID, acceptedID, acceptedValue);
        }
        else if (this.promisedID == null || proposalID.isGreaterThan(promisedID)) {
            promisedID = proposalID;
            sendPromise(fromUID, proposalID, acceptedID, acceptedValue);
        }
    }

    @Override
    public void receiveAcceptRequest(String fromUID, ProposalID proposalID, Object value) {
        if (promisedID == null || proposalID.isGreaterThan(promisedID) || proposalID.equals(promisedID)) {
            promisedID    = proposalID;
            acceptedID    = proposalID;
            acceptedValue = value;

            sendAccepted(acceptedID, acceptedValue);
        }
    }

    @Override
    public void sendPrepare(ProposalID proposalID) {

    }

    @Override
    public void sendPromise(String proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) {

    }

    @Override
    public void sendAccept(ProposalID proposalID, Object proposalValue) {

    }

    @Override
    public void sendAccepted(ProposalID proposalID, Object acceptedValue) {

    }

    @Override
    public void onResolution(ProposalID proposalID, Object value) {

    }

    @Override
    public boolean isComplete() {
        return finalValue != null;
    }

    @Override
    public Object getFinalValue() {
        return finalValue;
    }

    @Override
    public ProposalID getFinalProposalID() {
        return finalProposalID;
    }

    @Override
    public void receiveAccepted(String fromUID, ProposalID proposalID, Object acceptedValue) {
        if (isComplete())
            return;

        ProposalID oldPID = acceptors.get(fromUID);

        if (oldPID != null && !proposalID.isGreaterThan(oldPID))
            return;

        acceptors.put(fromUID, proposalID);

        if (oldPID != null) {
            Proposal oldProposal = proposals.get(oldPID);
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0)
                proposals.remove(oldPID);
        }

        if (!proposals.containsKey(proposalID))
            proposals.put(proposalID, new Proposal(0, 0, acceptedValue));

        Proposal thisProposal = proposals.get(proposalID);

        thisProposal.acceptCount    += 1;
        thisProposal.retentionCount += 1;

        if (thisProposal.acceptCount == quorumSize) {
            finalProposalID = proposalID;
            finalValue      = acceptedValue;
            proposals.clear();
            acceptors.clear();

            onResolution(proposalID, acceptedValue);
        }
    }
}