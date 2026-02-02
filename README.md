# custom_VirtualMemory_Paging_App

Your program will accept data from a set of input files, whose names are specified as part of a command line argument. 

Input to your program should be via command line arguments (see also in Section 3.6. Deliverable:), where the arguments are system configurations and the names of files that specify the execution trace of different processes. All processes start execution at time 0, and are entered into the system in the order of the command line arguments (with the third argument being the first process).

For example:
java A3 30 3 process1.txt process2.txt process3.txt

…where 30 is the number of frames (F) and 3 is the quantum size (Q) for Round Robin and related algorithms. Values F and Q are assumed to be integer values.
This is the only valid means of execution for this assignment.

There are 2 sample sets of data provided

You will write a program to simulate a system that uses paging with virtual memory. You are to compare the performance of the First In First Out (FIFO) page replacement algorithms for the fixed allocation with local replacement and variable allocation with global replacement strategies, as introduced in lectures.
The system characteristics are described below:

2.1. Memory
The system has F frames available in user memory space, a value that will be supplied as an input argument. During execution, the processor will determine if the page required for the currently running process is in main memory.
a. If the page is in main memory, the processor will access the instruction and continue.
b. If the page is not in main memory, the processor will issue a page fault and block the process until the page has been transferred to main memory.
c. Initially no page is in the memory, i.e., the simulation will be strictly using demand paging, where pages are only brought into main memory when they are requested.
d. In fixed allocation scheme frames are equally divided among processes, additional frames remain unused. In variable allocation scheme all frames are available to the processes.

You will need to implement two different schemes for resident set management
i. ‘Fixed Allocation with Local Replacement Scope’ – In this scheme, the frames allocated to a process do not change over the simulation period, i.e., even after a process finishes (and the allocated frames to that process become free), the allocated frames to other processes do not change. And the page
replacements (if necessary) will occur within the frames allocated to a process using FIFO policy.

ii. ‘Variable Allocation with Global Replacement Scope’ – In this scheme, no specific frame is allocated to any process rather all frames are available to the processes for use. A process can use an unused frame in the user memory space to bring in its own page. For page replacement it will use FIFO policy
but will consider all the pages in the user memory space, i.e., the page selected for  replacement may belong to any process running in the system. When a process finishes execution, the frames allocated to that finished process are released immediately and will be available for loading new pages if necessary

