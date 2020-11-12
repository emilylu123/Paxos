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
import java.util.Date;
import java.util.Random;

public class Election {
    long startTime = new Date().getTime();
    protected static Member M1, M2, M3, M4, M5, M6, M7, M8, M9, M10, M11, M12, M13;
    protected static ArrayList<Member> council = new ArrayList<>();

    // Start all servers
    public void start(boolean ismoreMembers) {
        cleanUp();  // delete backup files
        createMembers(ismoreMembers);
        startElection();
    }

    // Create M1 - M9
    public void createMembers(boolean ismoreMembers) {
        M1 = new Member(1);
        M2 = new Member(2);
        M3 = new Member(3);
        M4 = new Member(4);
        M5 = new Member(5);
        M6 = new Member(6);
        M7 = new Member(7);
        M8 = new Member(8);
        M9 = new Member(9);
//        M1.startTime = startTime;
        council.addAll(Arrays.asList(M1, M2, M3, M4, M5, M6, M7, M8, M9));

        if (ismoreMembers){
            M10 = new Member(10);
            M11 = new Member(11);
            M12 = new Member(12);
            M13 = new Member(13);
            council.addAll(Arrays.asList(M10,M11,M12,M13));
        }

        for (Member member : council) {
            member.startTime = startTime;
            member.nodes = council.size();
            member.majority = council.size() / 2 + 1;
        }
    }

    //Create a legal Election Council with 9 members
    public synchronized void startElection() {
        System.out.println("<<<<<<<<<<< Test:: Start Council Election >>>>>>>>>");
        for (Member member : council) {
            new Thread(member::connecting).start();
        }
    }

    protected void propose(Member a_member) {
        new Thread(() -> {
            try {
                System.out.println("<<<<< Election:: M" + a_member.MID + " will send proposal >>>>>");
                a_member.prepare(); // phrase 1 : prepare(n), receive promise
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
        System.out.println(">> Offline:: M" + id + " will go offline after proposal ( Offline Warning!)");
        if (id == 2) {
            M2.isOffline = true;
            M2.randomResponse = 3;
        } else {
            M3.isOffline = true;
            M3.randomResponse = 3;
        }
    }

    // isAll define if random response setting include M2 and M3
    protected void doRandom() {
//        M2.randomResponse = 1;
//        M3.randomResponse = 1;
//        M4.randomResponse = 1;
        M5.randomResponse = 1;
        M6.randomResponse = 1;
        M7.randomResponse = 1;
        M8.randomResponse = 1;
        M9.randomResponse = 1;
    }

    /*   random a integer between 0 - 5,
         the possibility of late response and never response are both 1/6
         the possibility of immediately response and medium response are both 1/3
         so that the random test is less likely to have majority (5) failures.
         */
    protected void random(Member member) {
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
}