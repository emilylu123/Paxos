//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Phase 1
 * (a) A proposer selects a proposal number n and sends a prepare request with number n to a majority of acceptors.
 * (b) If an acceptor receives a prepare request with number n greater than that of any prepare request to which it has already responded, then it responds to the request with a promise not to accept any more proposals numbered less than n and with the highest-numbered pro-posal (if any) that it has accepted.
 * Phase 2
 * (a) If the proposer receives a response to its prepare requests (numbered n) from a majority of acceptors, then it sends an accept request to each of those acceptors for a proposal numbered n with a value v , where v is the value of the highest-numbered proposal among the responses, or is any value if the responses reported no proposals.
 * (b) If an acceptor receives an accept request for a proposal numbered n, it accepts the proposal unless it has already responded to a prepare request having a number greater than n.
 */

public class MyPaxos {
    protected final Member member;
    String add;
    int port;
    Socket socket;

    MyPaxos(Member member, String add, int port) {
        this.member = member;
        this.add = add;
        this.port = port;
        try {
            new Thread ( ) {
                @Override
                public synchronized void run() {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace ( );
                    }
                }
            }.start ( );
            ServerSocket server = null;

            server = new ServerSocket (port);

            System.out.println ("**********\nServer is ready!\n**********");

            while (true) {
                socket = server.accept ( );
            }

        } catch (IOException e) {
            e.printStackTrace ( );
        }
    }


    public static void main(String[] args) {

    }

}

