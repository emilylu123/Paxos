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
/*
30 points – Paxos implementation works when M1 – M9 have responses to voting queries suggested by the profiles above,
including when M2 or M3 propose and then go offline

Bonus
10 points – Paxos implementation works with a number ‘n’ of councilors with four profiles of response times:
immediate;  medium; late; never
50 points – (you can use these points in this assignment, or in any other subsequent assignment) –
Fast Byzantine Paxos implementation that works when councilors lie, collude, or intentionally do not participate
in some voting queries but participate in others.*/

public class Election {
    protected static Member M1, M2, M3, M4, M5, M6, M7, M8, M9;
    protected static ArrayList<Member> council = new ArrayList<>();

    //Start all servers
    public void start() {
        cleanUp();  // delete backup files
        createMembers();
        startElection();
    }

    public void createMembers() {
        M1 = new Member(1);
        M2 = new Member(2);
        M3 = new Member(3);
        M4 = new Member(4);
        M5 = new Member(5);
        M6 = new Member(6);
        M7 = new Member(7);
        M8 = new Member(8);
        M9 = new Member(9);
        council.addAll(Arrays.asList(M1, M2, M3, M4, M5, M6, M7, M8, M9));
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

    protected void goOffline() {
        int id = new Random().nextInt(2) + 2;
        System.out.println(">> Offline :: M" + id + " will be offline after proposal");
        if (id == 2) {
            M2.isOffline = true;
            M2.randomResponse = 3;
        } else {
            M3.isOffline = true;
            M3.randomResponse = 3;
        }
    }

    protected void doRandom() {
        goOffline();
//        random(M2);
//        random(M3);
        random(M4);
        random(M5);
        random(M6);
        random(M7);
        random(M8);
        random(M9);
    }

    protected void random(Member member) {
        int random = new Random().nextInt(6);
        switch (random) {
            case 0:
            case 1:
                member.randomResponse = 0;
                System.out.println(">> Random:: M" + member.MID + " - immediately response " + random);
                break;
            case 2:
            case 3:
                member.randomResponse = 1;
                System.out.println(">> Random:: M" + member.MID + " - medium response " + random);
                break;
            case 4:
                member.randomResponse = 2;
                System.out.println(">> Random:: M" + member.MID + " - late response " + random);
                break;
            default:
                member.randomResponse = 3;
                System.out.println(">> Random:: M" + member.MID + " - never response " + random);
                break;
        }
    }
}