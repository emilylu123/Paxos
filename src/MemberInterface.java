import java.io.IOException;
import java.net.Socket;

public interface MemberInterface {

    public void prepare() throws IOException, InterruptedException, ClassNotFoundException; // proposer
    public void accept(int value);
    public void accept(Socket member, String inMSG) throws IOException; // proposer
    public void setProposal(Object value);// proposer
    public void receivePromise(int fromUID, ProposalMSG proposalID, ProposalMSG prevAcceptedID); // proposer

    public void receivePrepare(int fromUID, ProposalMSG proposalID); // acceptor
    public void receiveAccept(int fromUID, ProposalMSG proposalID, Object value); // acceptor

//    public void sendPrepare(ProposalMSG proposalID); //msg
//    public void sendPromise(int proposerUID, ProposalMSG proposalID, ProposalMSG previousID, Object acceptedValue);
//    public void sendAccept(ProposalMSG proposalID, Object proposalValue); //msg
//    public void sendAccepted(ProposalMSG proposalID, Object acceptedValue); //msg
//    public void onResolution(ProposalMSG proposalID, Object value); //msg

    public boolean isComplete();     //learner
    public void receiveAccepted(int fromUID, ProposalMSG proposalID, Object acceptedValue); //learner
}
