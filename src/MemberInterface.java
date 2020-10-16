public interface MemberInterface {

    public void prepare(); // proposer
    public void setProposal(Object value);// proposer
    public void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID,
                               Object prevAcceptedValue); // proposer

    public void receivePrepare(String fromUID, ProposalID proposalID); // acceptor
    public void receiveAcceptRequest(String fromUID, ProposalID proposalID, Object value); // acceptor

    public void sendPrepare(ProposalID proposalID); //msg
    public void sendPromise(String proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue);
    public void sendAccept(ProposalID proposalID, Object proposalValue); //msg
    public void sendAccepted(ProposalID proposalID, Object acceptedValue); //msg
    public void onResolution(ProposalID proposalID, Object value); //msg

    public boolean isComplete();     //learner
    public Object getFinalValue();   //learner
    ProposalID getFinalProposalID(); //learner
    public void receiveAccepted(String fromUID, ProposalID proposalID, Object acceptedValue); //learner
}
