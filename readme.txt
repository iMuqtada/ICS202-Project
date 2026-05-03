KFUPM Clinic Management System
ICS 202 – Data Structures & Algorithms

Project Overview
----------------------------------------
We built a command-line clinic management system using our own data structures. It acts a lot like a real clinic, handling patient records, setting up appointments, dealing with walk-ins, giving urgent cases priority, and tracking visits.

The point? Not just to do the job, but to make all the data structures ourselves instead of leaning on anything Java already gives you. It gave us a hands-on feel for how things really work under the hood.

Data Structures Used
----------------------------------------
- Hash Table:
Fast way to stash and pull up patients and appointments by their IDs.

- AVL Tree:
Keeps all the appointments sorted by date and time, so it’s easy to look up or list them in order.

- Max Heap:
Lets us deal with urgent patients—whoever has the most serious case gets attention first.

- Linked Queue:
Handles walk-ins fairly, first come, first served.

- Linked Stack:
Holds all the recent actions for quick “undo” moves.

- Singly Linked List:
Stores all visit logs in order, so you can review or search through them later.

String Matching
----------------------------------------
We used two ways to search within visit logs:

- Naive Matching:
Just goes through the logs and checks for a match at every spot.

- KMP Matching:
Smarter—skips over spots where a match isn’t possible, using an LPS array.

System Features
----------------------------------------
Here’s what the system can do:

- Add, find, or remove patients
- Schedule and manage appointments
- View appointments for a day or a range of times
- Process walk-in patients
- Prioritize and manage urgent cases
- Serve patients in this order: Urgent, then Walk-in, then Appointment
- Log visits and let you print out the logs
- Search logs with Naive or KMP methods
- Undo the last thing you did

How to Run
----------------------------------------
1. Open up the project in any Java IDE you like.
2. Run: ClinicSystem.java
3. Use the terminal to enter your commands.

Team Members
----------------------------------------
- Ahmed Alsaad (202432980)
- [Teammate Name] (ID)

----------------------------------------
GitHub Repository
----------------------------------------
https://github.com/iMuqtada/ICS202-Project

----------------------------------------
Notes
----------------------------------------
- We built every data structure ourselves.
- No core logic used built-in Java structures.
- this project showed us how to really work together with Git and GitHub.
