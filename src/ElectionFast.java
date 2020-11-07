//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ElectionFast {
    protected static MemberFast M1, M2, M3, M4, M5, M6, M7, M8, M9;
    protected static ArrayList<MemberFast> council = new ArrayList<>();

    // Start all servers
    public void start() {
        cleanUp();  // delete backup files
        createMembers();
        startElection();
    }

    // Create M1 - M9
    public void createMembers() {
        M1 = new MemberFast(1);
        M2 = new MemberFast(2);
        M3 = new MemberFast(3);
        M4 = new MemberFast(4);
        M5 = new MemberFast(5);
        M6 = new MemberFast(6);
        M7 = new MemberFast(7);
        M8 = new MemberFast(8);
        M9 = new MemberFast(9);
        council.addAll(Arrays.asList(M1, M2, M3, M4, M5, M6, M7, M8, M9));
    }

    //Create a legal Election Council with 9 members
    public synchronized void startElection() {
        System.out.println("<<<<<<<<<<< Test:: Start Council Election >>>>>>>>>");
        for (MemberFast member : council) {
            new Thread(member::connecting).start();
        }
    }

    protected void propose(MemberFast a_member) {
        new Thread(() -> {
            try {
                System.out.println("<<<<< Election:: M" + a_member.MID + " will send proposal >>>>>");
                a_member.prepareToLeader(); // phrase 1 : prepare(n) to leader M1
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // clean up local files
    private void cleanUp() {
        try {
            System.out.println(">> Test Preparation:: Delete all local backup files.\n");
            for (int i = 1; i <= 9; i++) {
                String fileName = i + "data.txt";
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in delete files function");
        }
    }

    // randomly choose M2 or M3 going offline
    protected void goOffline() {
        int id = new Random().nextInt(2) + 2;
        System.out.println(">> Offline Warning :: M" + id + " will be offline after proposal");
        if (id == 2) {
            M2.isOffline = true;
            M2.randomResponse = 3;
        } else {
            M3.isOffline = true;
            M3.randomResponse = 3;
        }
    }

    // isAll define if random response setting include M2 and M3
    protected void doRandom(boolean isAll) {
        if (isAll) {
            random(M2);
            random(M3);
        }
        random(M4);
        random(M5);
        random(M6);
        random(M7);
        random(M8);
        random(M9);
    }

    /*   random a integer between 0 - 5,
         the possibility of late response and never response are both 1/6
         the possibility of immediately response and medium response are both 1/3
         so that the random test is less likely to have majority (5) failures.
         */
    protected void random(MemberFast member) {
        int random = new Random().nextInt(6);
        switch (random) {
            case 0:
            case 1:
                member.randomResponse = 0;
                System.out.println(">> Random response :: M" + member.MID + " - immediately");
                break;
            case 2:
            case 3:
                member.randomResponse = 1;
                System.out.println(">> Random response :: M" + member.MID + " - medium");
                break;
            case 4:
                member.randomResponse = 2;
                System.out.println(">> Random response :: M" + member.MID + " - late");
                break;
            default:
                member.randomResponse = 3;
                System.out.println(">> Random response :: M" + member.MID + " - never ( Offline Warning!)");
                break;
        }
    }

    // bonus function for choose random Member from M4-M9 as the cheater, who will change value
    protected void doByzantine() {
        int random = new Random().nextInt(5) + 3;
        council.get(random).isByzantine = true;  // set Member random as the Byzantine lie
        System.out.printf(">> Byzantine Traitor >> :: Member %d will send wrong accepted Value on purpose \n", random);
    }
}