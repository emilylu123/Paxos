//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

public class ProposalID {

    private int number;
    private final String uid;

    public ProposalID(int number, String uid) {
        this.number = number;
        this.uid    = uid;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void incrementNumber() {
        this.number += 1;
    }

    public String getUID() {
        return uid;
    }

    public int compare( ProposalID id ) {
        if ( equals(id) )
            return 0;
        if ( number < id.number || (number == id.number && uid.compareTo(id.uid) < 0) )
            return -1;
        return 1;
    }
    

    public boolean isGreaterThan( ProposalID id ) {
        return compare(id) > 0;
    }

    public boolean isLessThan( ProposalID id ) {
        return compare(id) < 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProposalID other = (ProposalID) obj;
        if (number != other.number)
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }
}
