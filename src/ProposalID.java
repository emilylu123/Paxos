//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class ProposalID {
    private int proposalID;
    private int value;

    public ProposalID(int proposalID) {
        this.proposalID = proposalID;
        this.value = 0;
    }

    public ProposalID(int proposalID, int value) {
        this.proposalID = proposalID;
        this.value = value;
    }

    public int getProposalID() {
        return proposalID;
    }

    public void setProposalID(int proposalID) {
        this.proposalID = proposalID;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
       this.value = value;
    }

    public void incrementProposalID() {
        this.proposalID += Math.random()*10;
    } // todo random increment proposalID

    public boolean isGreaterThan( ProposalID id ) {
        return proposalID > id.proposalID;
    }

    public boolean isLessThan( ProposalID id ) {
        return proposalID < id.proposalID;
    }

    @Override
    // todo check
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProposalID other = (ProposalID) obj;
        if (proposalID != other.proposalID)
            return false;
        if (value <= 0) {
            return other.value <= 0;
        } else return value == other.value;
    }
}
