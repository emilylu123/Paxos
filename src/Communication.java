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

    protected static final int MAX_TRY = 5;
    protected static final String host = "0.0.0.0";

    public static Socket getSocket (int toMID) throws Exception {
        Socket socket = null;
        int acceptorIP = toMID * 1111;
        int count_try = 0;

        while(count_try<MAX_TRY){
            count_try++;
            try{
                socket = new Socket ( host, acceptorIP);
                break;
            } catch (Exception e) {
                System.out.println(">> Connection to M" + toMID + " failed. Will retry in 3s. Retry " + count_try);
                Thread.sleep(3000);
            }
        }
        if (count_try == 5)
            System.out.println(">> Connection is failed. M" + toMID + " is offline\n" + "*".repeat(40));
        return socket;
    }

    public static String outMSG(int toMID, Object outOBJ) {
        try {
            Socket socket = getSocket(toMID);
            ProposalMSG pid = (ProposalMSG) outOBJ;
            if (socket != null && socket.isConnected()){
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(outOBJ);
                oos.flush();
                return " send -> M" + toMID + " :: " + pid.getProposalMSG();
            }
        } catch (Exception e) {
            System.out.println("Failed to send message to " + toMID);
        }
        return null;
    }

    public static ProposalMSG inMSG(Socket socket) throws Exception {
        ObjectInputStream ois ;
        ProposalMSG inPID = null;
        if (socket != null && socket.isConnected()){
            ois = new ObjectInputStream(socket.getInputStream());
            try {
                inPID = (ProposalMSG) ois.readObject();
            } catch(Exception e){
                System.out.println("Failed to receive in coming message");
            }
        }
        return inPID;
    }

    public static ObjectOutputStream getOOS(Socket socket) throws IOException {
        if (socket != null && socket.isConnected()){
            return new ObjectOutputStream(socket.getOutputStream());
        }
        return null;
    }

    public static ObjectInputStream getOIS(Socket socket) throws IOException {
        if (socket != null && socket.isConnected())
            return new ObjectInputStream(socket.getInputStream());
        return null;
    }
}
