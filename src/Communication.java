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
    protected static int localport;
    protected static final String host = "0.0.0.0";

    public static Socket getSocket (int toMID) throws InterruptedException, IOException, ClassNotFoundException {
        Socket socket = null;
        int acceptorIP = toMID * 1111;
        int count_try = 0;

        while(count_try<=MAX_TRY){
            count_try++;
            try{
                socket = new Socket ( host, acceptorIP);
                break;
            } catch (Exception e) {
                System.out.println(">> Connection to M" + toMID + " failed. Will retry in 3s. Retry " + count_try);
                Thread.sleep(3000);
                e.printStackTrace();
            }
        }
        return socket;
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

    public static boolean outMSG(int toMID, Object outOBJ) throws InterruptedException, IOException, ClassNotFoundException {
        Socket socket = getSocket(toMID);
        ObjectOutputStream oos = getOOS(socket);
        oos.writeObject(outOBJ);
        oos.flush();
        ProposalMSG pid = (ProposalMSG) outOBJ;
        System.out.printf(">> MSG out:: -> M%s: %s\n",toMID, pid.getProposalMSG());
        return true;
    }

    public static ProposalMSG inMSG(int fromMID) throws InterruptedException, IOException, ClassNotFoundException {
        Socket socket = getSocket(fromMID);
        ObjectInputStream ois = getOIS(socket);
        ProposalMSG inPID = null;
        try {
            inPID = (ProposalMSG) ois.readObject();
            System.out.printf(">> MSG in:: M%s -> : %s\n",fromMID, inPID.getProposalMSG());
        } catch(Exception e){
            System.out.println("Error in receiving in message");
            e.printStackTrace();
        }
        return inPID;
    }
}
