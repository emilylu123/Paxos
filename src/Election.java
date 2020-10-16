//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
import java.util.*;

public class Election {
    private final List<Member> council = new ArrayList<>();//Define a collection

    //Start all servers
    public void start(int serverNumber) throws Exception {
        if (council != null && council.size() >0)
            throw new Exception("restart error");

        Paxos paxos = new Paxos();
        for(int i = 0; i < serverNumber; i++) {
            Member a_member = new Member(this);
            Thread thread = new Thread(a_member);
            thread.start();
        }
    }

    //Started server registration
    public void register(Member a_member) {
        council.add(a_member);
    }

    //Get all servers
    public int getMajority() {
        return council.size()/2+1;
    }

    //Get a legal collection
    public synchronized List<Member> getLegalCouncil() {
        List<Member> list = new ArrayList<>();
        int count = 0;
        int councilSize = council.size();
        int majority = getMajority();

        Random random = new Random();
        while(count < majority) {
            int _random = Math.abs(random.nextInt(councilSize)); // Generate a random number
            if( _random < councilSize) {  // _random >= 0
                Member _member = council.get(_random);
                if(!list.contains(_member)) {
                    list.add(_member);
                    count++;
                }
            }
        }
        return list;
    }
}
