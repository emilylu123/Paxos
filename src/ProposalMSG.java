//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.Serializable;
import java.util.Date;

public class ProposalMSG implements Serializable {
    private int PID;
    private Object value;
    private int MID;

    public ProposalMSG(int MID) {
        this.MID = MID;
        this.PID = 0;  // set PID = 0 when first new Proposal
    }
    public ProposalMSG(int MID, boolean ifID) {
        this.MID = MID;
        if (ifID) generateProposalID();  // (int) unique PID = (6th digits ~ end) current time stamp + MemberID
    }

    public ProposalMSG(int proposalID, int value) {
        this.MID = value;
        this.PID = proposalID;
        if (value <= 3){
            this.value = value;  // M1 M2 M3 always vote for itself
        } else {
            this.value = (int)(Math.random() * 3) + 1; // M4-M9 randomly choose value from [M1, M2, M3]
        }
    }

    public int getPID() {
        return PID;
    }

    public void generateProposalID() {
        String timeStamp = Long.toString(new Date().getTime()) + Integer.toString(this.MID);
        this.PID = Integer.parseInt(timeStamp.substring(6,timeStamp.length())) ;
    }
    public void setPID(int pID) {
        this.PID = pID;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
       this.value = value;
    }


    public int getMID() {
        return MID;
    }

    public void setMID(int MID) {
        this.MID = MID;
    }

    public int incrementProposalID() {
        return this.PID += Math.random()*10; // todo random increment proposalID
    }

    public boolean isGreaterThan( ProposalMSG id ) {
        return PID > id.PID;
    }

    public boolean isLessThan( ProposalMSG id ) {
        return PID < id.PID;
    }

    @Override
    // todo check
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        ProposalMSG other = (ProposalMSG) obj;
        if(MID !=other.MID) return false;
        if (PID != other.PID)  return false;
        if (value == null){
            if (other.value != null)  return false;
        } else {
            if (other.value == null) return false;
            else if ((int)value != (int)other.value) return false;
        }
        return true;
    }

    public void printP (){
        System.out.println(getProposalMSG());
    }
    public String getProposalMSG(){
        return "( " + this.getPID() + ", " + this.getValue() + " )";
    }
}
