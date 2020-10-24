import java.io.IOException;
import java.net.Socket;

public interface MemberInterface {

    void prepare() throws IOException, InterruptedException, ClassNotFoundException; // proposer

    void accept(int value);

    void accept(Socket member, String inMSG) throws IOException; // proposer

    void setProposal(Object value);// proposer

    void receivePromise(int fromUID, ProposalMSG proposalID, ProposalMSG prevAcceptedID); // proposer

    void receivePrepare(int fromUID, ProposalMSG proposalID); // acceptor

    void receiveAccept(int fromUID, ProposalMSG proposalID, Object value); // acceptor

//    public void sendPrepare(ProposalMSG proposalID); //msg
//    public void sendPromise(int proposerUID, ProposalMSG proposalID, ProposalMSG previousID, Object acceptedValue);
//    public void sendAccept(ProposalMSG proposalID, Object proposalValue); //msg
//    public void sendAccepted(ProposalMSG proposalID, Object acceptedValue); //msg
//    public void onResolution(ProposalMSG proposalID, Object value); //msg

    boolean isComplete();     //learner

    void receiveAccepted(int fromUID, ProposalMSG proposalID, Object acceptedValue); //learner
}
