//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.Serializable;

public class ProposalID implements Serializable {
    private int proposalID;
    private Object value;
    private int UID;

    public ProposalID(int UID) {
        this.UID = UID;
        this.proposalID = UID;
    }

    public ProposalID(int proposalID, int value) {
        this.proposalID = proposalID;
        this.UID = value;
        if (value <= 3){
            this.value = value;  // M1 M2 M3 always vote for itself
        } else {
            this.value = (int)(Math.random() * 3) + 1; // M4-M9 randomly choose value from [M1, M2, M3]
        }
    }
    public void printPID (){
        System.out.printf(" (%d, %d) ", this.getProposalID(), (int)this.getValue());
    }
    public String getPID (){
        return "( " + this.getProposalID() + ", " + this.getValue() + " )";
    }
    public int getProposalID() {
        return proposalID;
    }

    public void setProposalID(int proposalID) {
        this.proposalID = proposalID;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
       this.value = value;
    }


    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public int incrementProposalID() {
        return this.proposalID += Math.random()*10; // todo random increment proposalID
    }

    public boolean isGreaterThan( ProposalID id ) {
        return proposalID > id.proposalID;
    }

    public boolean isLessThan( ProposalID id ) {
        return proposalID < id.proposalID;
    }

    @Override
    // todo check
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        ProposalID other = (ProposalID) obj;
        if(UID!=other.UID) return false;
        if (proposalID != other.proposalID)  return false;
        if (value == null){
            if (other.value != null)  return false;
        } else {
            if (other.value == null) return false;
            else if ((int)value != (int)other.value) return false;
        }
        return true;
    }
}
