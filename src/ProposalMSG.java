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
import java.util.Random;

public class ProposalMSG implements Serializable {
    private int MID;
    private int PID;
    private Object value;
    private String type = "";


    public ProposalMSG(int MID) {
        this.MID = MID;
        this.PID = 0;  // set PID = 0 when first new Proposal
        createVotingValue();
    }

    public ProposalMSG(int MID, int PID, Object value, String type) {
        this.MID = MID;
        this.PID = PID;
        this.value = value;
        this.type = type;
    }

    // M1-M3 will always vote for them self, and M4-M9 randomly vote from [M1, M2, M3]
    public void createVotingValue() {
//        if (this.MID <= 4)
            this.value = MID;  // M1 M2 M3 always vote for itself
//        else this.value = new Random().nextInt(3) + 1;
    }

    // generate unique PID = current time stamp (7th digits ~ end) + 1 digit of random number +MemberID
    public void generateProposalID() {
        int ran = new Random().nextInt(9) + 1;
        String timeStamp = new Date().getTime() + Integer.toString(ran * 10 + this.MID);
        this.PID = Integer.parseInt(timeStamp.substring(7));
    }

    @Override
    // compare if two ProposalMSG objects are same
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        ProposalMSG other = (ProposalMSG) obj;
        if (PID != other.PID) return false;
        if (MID != other.MID) return false;
        if (value == null) {
            return other.value == null;
        } else {
            if (other.value == null) return false;
            else {
                return (int) value == (int) other.value;
            }
        }
    }

    // a simple way to print proposalMSG object in a nice format
    public String getProposalMSG() {
        return type + " ( " + getPID() + ", " + getValue() + ")";
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
