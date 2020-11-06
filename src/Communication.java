//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Communication {

    protected static final int MAX_TRY = 3;
    protected static final String host = "0.0.0.0";

    public static Socket getSocket(int toMID) throws Exception {
        Socket socket = null;
        int acceptorIP = toMID * 1111;
        int count_try = 0;

        while (count_try < MAX_TRY) {
            count_try++;
            try {
                socket = new Socket(host, acceptorIP);
                break;
            } catch (Exception e) {
                if (toMID!=1)
                    System.out.println(">> Connection to M" + toMID + " failed. Will retry in 1s. Retry " + count_try);
                Thread.sleep(1000);
            }
        }
        return socket;
    }

    public static String outMSG(int toMID, Object outOBJ) {
        ProposalMSG pid = (ProposalMSG) outOBJ;
        try {
            Socket socket = getSocket(toMID);
            if (socket != null && socket.isConnected()) {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(outOBJ);
                oos.flush();
                return "*".repeat(20) + " send -> M" + toMID + " :: " + pid.getProposalMSG() + "*".repeat(20);
            }
        } catch (Exception e) {
            System.out.println("Failed to send " + pid.getProposalMSG());
        }
        return null;
    }

    public static ProposalMSG inMSG(Socket socket) throws Exception {
        ObjectInputStream ois;
        ProposalMSG inPID = null;
        if (socket != null && socket.isConnected()) {
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                inPID = (ProposalMSG) ois.readObject();
            } catch (Exception e) {
                System.out.println("Failed to receive in coming message");
            }
        }
        return inPID;
    }
}
