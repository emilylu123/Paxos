import java.io.IOException;
import java.net.Socket;

public interface MemberInterface {

    public void prepare(); // proposer
    public void accept(Socket member, String inMSG) throws IOException; // proposer
    public void setProposal(Object value);// proposer
    public void receivePromise(int fromUID, ProposalID proposalID, ProposalID prevAcceptedID,
                               Object prevAcceptedValue); // proposer

    public void receivePrepare(int fromUID, ProposalID proposalID); // acceptor
    public void receiveAccept(int fromUID, ProposalID proposalID, Object value); // acceptor

    public void sendPrepare(ProposalID proposalID); //msg
    public void sendPromise(int proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue);
    public void sendAccept(ProposalID proposalID, Object proposalValue); //msg
    public void sendAccepted(ProposalID proposalID, Object acceptedValue); //msg
    public void onResolution(ProposalID proposalID, Object value); //msg

    public boolean isComplete();     //learner
    public void receiveAccepted(int fromUID, ProposalID proposalID, Object acceptedValue); //learner
}
