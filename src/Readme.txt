//====================================
// Distributed System
// Name: Yingyao Lu
// ID: a1784870
// Semester: S2
// Year: 2020
// Assignment3: Paxos
//=====================================

This project demonstrates that how to implement consensus and voting protocols in the presence of failures of one or more of the participants.

Three of automated testing are provided.

The test file is named as "TesterMultiNormal", "TesterMultiWithOffline", "TesterMultiWithRandom"

//How to run tests
1. Compile
    Console Command: javac *.java

2. Run Tester Multiple Proposers without failure
	Console Command: java TesterMultiNormal
	This test will simulate the case that M1 M2 M3 to propose at the same time,
	and M1-M9 have immediate response.

3. Run Tester Multiple Proposers with M2 / M3 goes offline
	Console Command: java TesterMultiWithOffline
	This test will simulate the case that M1 M2 M3 to propose at the same time,
	M1-M9 have immediate response, and M2 or M3 stop response after proposing.


4. Run Tester Multiple Proposers with M2 / M3 goes offline, M4-M9 have random response times [bonus]
	Console Command: java TesterMultiWithRandom
	This test will simulate the case that M1 M2 M3 to propose at the same time,
	M4-M9 have random response time, and M2 or M3 stop response after proposing.

5. Run Fast Byzantine Pax's [bonus]
	Console Command: ...
	This test will simulate the case that M1 M2 M3 to propose at the same time,
	M4-M9 have random response time and will lie collude, or intentionally 
	do not participate in some voting queries.

Checklist
Basic functionality:
√ Paxos implementation works when two councillors send voting proposals at the same time [TesterMultiNormal - 10pts]

√ Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries [TesterMultiNormal - 30pts]

√ Paxos implementation works when M1 – M9 have responses to voting queries suggested by the profiles above, including
when M2 or M3 propose and then go offline [TesterMultiWithOffline - 30pts]

Bonus functionality:
√? Paxos implementation works with a number ‘n’ of councillors with four profiles of response times:
    immediate;  medium; late; never.  [TesterMultiWithRandom - 10pts]
    (still a little buggy - Paxos does not stop properly)

? (todo) Fast Byzantine Paxos implementation that works when councillors lie, collude,
    or intentionally do not participate in some voting queries but participate in others. [ (todo) - TesterByantine - 50 points]

The project has three servers:
    Member : a class for councillor member to propose and accept using paxos
    ProposalMSG : a class for proposal message, including memberID, proposalID, value, message typ, etc.
    Communication : a class for sending and receiving ProposalMSG object using socket
    Election : a class to create threads to run M1-M9 members and make propose.
