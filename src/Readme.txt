//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

This project demonstrates that how to implement consensus and voting protocols in the presence of failures of one or more of the participants.

5 automated testing are provided.

The test files are named as "TesterClassicNormal", "TesterMultiWithOffline", "TesterBonusRandom", "TesterBonusFastPaxos", "TesterBonusByzantine"

//How to run tests
1. Compile
    Console Command: javac *.java

2. Run Test 1 :: Classic Paxos - No failure
	Console Command: java TesterClassicNormal
	1. Proposer: M1 - M3 proposes to all members at the same time;
        2. Acceptor: All members response immediately.

3. Run Test 2 : Classic Paxos - with failure (Offline)
       	Console Command: java TesterClassicOffline
	1. Proposer: M1 - M3 proposes to all members at the same time;
        2. Offline : M2 / M3 proposes and then goes offline;
        3. Acceptor: M4 - M9 has random response time: immediate; medium; late; never.

4. Run Test 3 (Bonus) : Classic Paxos - with random response
	Console Command: java TesterBonusRandom
	1. Proposer: M1 - M3 proposes at the same time;
	2. Acceptor: M2 - M9 has random response time: immediate; medium; late; never.

5. Run Test 4 (Bonus) :: Fast Paxos Algorithm
	Console Command: java TesterBonusFastPaxos
	1. Fast Paxos Algorithm: Leader - M1
        2. Proposer: M1 - M3 proposes to Leader at the same time;
        3. Acceptor: M2 - M9 have random response time: immediate; medium; late; never.


6. Run Test 5 (Bonus) :: Fast Byzantine Paxos
	Console Command: java TesterBonusByzantine
	This test will simulate the case that 
	1. Byzantine Traitor : 1-2 members from M4 - M9 will lie, collude,
	or intentionally do not participate in some voting queries;
        2. Proposer: M1 - M3 propose to Leader M1 at the same time;
        3. Acceptor: M2 - M9 have random response time: immediate; medium; late; never


Checklist
Basic functionality:
√ Paxos implementation works when two councillors send voting proposals at the same time [ Test 1 TesterClassicNormal - 10pts]

√ Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries [ Test 1 TesterClassicNormal - 30pts]

√ Paxos implementation works when M1 – M9 have responses to voting queries suggested by the profiles above, including
when M2 or M3 propose and then go offline [ Test 2 TesterClassicOffline - 30pts]

Bonus functionality:
√ Paxos implementation works with a number ‘n’ of councillors with four profiles of response times: immediate;  medium; late; never.  [ Test 3 TesterBonusRandom - 10pts]


√ Fast Pax's implementation works [ Test 4 TesterBonusFastPaxos - (?)/50 points]

√ Fast Byzantine Paxos implementation that works when councillors lie, collude,
    or intentionally do not participate in some voting queries but participate in others. [ Test 5 TesterBonusByzantine - 50 points]

The project has following files:
    Member : a class for councillor member to propose and accept using classic Paxos
    ProposalMSG : a class for proposal message, including memberID, proposalID, value, message typ, etc.
    Communication : a class for sending and receiving ProposalMSG object using socket
    Election : a class to create threads to run M1-M9 members, set status, and make propose.
    
    MemberFast : a class for councillor member to propose and accept using fast Paxos and fast Byzantine Paxos
    ElectionFast : a class to create threads to run M1-M9 fast members, set status, and make propose.
