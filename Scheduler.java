/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 *
 * Abstract superclass that both page replacement policies inherit from.
 * Data structures and the print method common to both algorithms have been stored here to reduce repetition
 * 
 * The simulation adheres to the following rules specified in the assignment document.
 * Simulation scheduling rules: 
 * 			- The system is to use a Round Robin short-term scheduling algorithm with time a quantum of Q.
 * 			- Executing a single instruction (i.e. a page) takes 1 unit of time
 * 			- Switching the processes does not take any time
 * 			- All the processes start execution at time t = 0. And they will be processed in the order the process names appear in the input.
 * 			- If a process becomes ready at time unit t then execution of that process may occur in the same time unit t without any delay (if there is no other process running or waiting in the ready queue).
 * 			- If multiple process becomes ready at the same time, then they will enter the ready queue in the order they became blocked.
 * 			- If a process P1 is finishes it’s time quantum at t1 and another process P2 becomes unblocked at the same time t1, then the unblocked process, P2, is added in the ready queue first and the time-quantum expired process, P1, is added after tha
 * 
 * Simulation memory rules:
 * 			- The system has F frames available in user memory space, a value that will be supplied as an input argument. 
 * 			- During execution, the algorithm will determine if the page required for the currently running process is in main memory.
 * 				a.If the page is in main memory, the algorithm will access the instruction and continue.
 * 				b.If the page is not in main memory, the algorithm will issue a page fault and block the process until the page has been transferred to main memory.
 * 				c.Initially no page is in the memory, i.e., the simulation will be strictly using demand paging, where pages are only brought into main memory when they are requested.
 * 				d.In the fixed allocation scheme frames are equally divided among processes, additional frames remain unused. In the variable allocation scheme all frames are available to the processes.
 *
 *	Page Fault Handling rules:
 *			- Issuing a page fault and blocking a process takes no time, so multiple page faults may occur and then another ready process can run immediately at the same time unit.
 *			- Swapping in a page takes 4 units of time (if a page required by a process is not in main memory, the process must be put into its blocked state until the required page is available).
 *			- If a process is unblocked (i.e. the requested page is placed in the main memory) at time t then it can be scheduled and the requested page can be executed at t
 * */

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class Scheduler {
    protected final int NUMBER_FRAMES;			//Number of frames allocated in this simulation - from input file
    protected final int TIME_QAUNTUM;			//Time quantum to be used for RR simulation	- from input file
    protected int algorithmTime;				//Running simulation time
    protected Queue<Process> readyQueue;		//Holds processes ready to be run
    protected Queue<Process> blockedQueue;		//Holds processes that have been blocked
    protected int framesPerProcess;				//Number of frames each process has been allocated - Used for fixed local replacement policy
    protected Queue<Frame> pendingQueue;		//Pages wait in this queue for 4 units before loading into main memory
    
    /**
     * Constructor for Scheduler class. 
     * Creates an instance of scheduler class and initialises its data structures.
     * All processes are added to the ready queue since they all arrive at time t = 0.
     *
     * Preconditions: - processes != null — must contain at least one process
     * 				  - numFrames > 0 — total number of frames must be positive
     * 				  - quantum > 0 — time quantum must be a positive integer
     * Postconditions:	- All processes are added to the ready queue
     * 					- Blocked queue is initialised and empty
     * 					- Simulation time is set to zero
     * 
     * @param processes - a list of Process objects participating in the simulation
     * @param numFrames - total number of frames (F) available in main memory
     * @param quantum - time quantum (Q) used for the Round Robin CPU scheduling
     * 
     * @throws IllegalArgumentException if the number of frames passed into the program is < the number of input processes
     */
    public Scheduler(List<Process> processes, int numFrames, int quantum) {
        this.NUMBER_FRAMES = numFrames;
        this.TIME_QAUNTUM = quantum;
        this.algorithmTime = 0;
        
        //Create data structures to hold ready, blocked and pending processes
        readyQueue = new LinkedList<Process>();
        blockedQueue = new LinkedList<Process>();
        pendingQueue = new LinkedList<Frame>();
        
	   	//Since all processes start at t=0 add them all to the ready queue now
	   	for(Process p: processes) {
	   		readyQueue.add(p);
	   	}
	   	
	   	this.framesPerProcess = numFrames / processes.size(); 	//Calculate the number of frames given per process. Used for fixed allocation scheme - frames are equally divided among processes. Additional frames remain unused
	   	
	   	//Ensure each process gets at least one frame - i.e. ensure the frame value passed in when running the program is >= number of input processes, otherwise the simulation won't work
	   	if (framesPerProcess < 1) {
	   	    throw new IllegalArgumentException("Insufficient frames: " + numFrames + " frames for " + processes.size() + " processes. Each process must have at least 1 frame.");
	   	}
    }

    /**
     * Runs the full Round Robin simulation
     * 
     * Preconditions:
     *  - processes has been parsed and initialised correctly.
     *  - algorithmTime starts at 0.
     * 
     * Postconditions:
     *  - All processes have either finished execution or been blocked/handled.
     *  - Page faults are recorded in each process.
     *  - Finish times are set for all completed processes.
     *  - Results are printed to console at the end of execution.
     *  
     *  @param processes - a list of Process objects participating in the simulation
     */
    public void runSimulation(List<Process> processes) {

		//Run simulation until both queues are empty
		while (!readyQueue.isEmpty() || !blockedQueue.isEmpty()) {

			checkPendingPages();	//Load any pages from pendingLoads queue to main memory that are ready now
			unblockProcesses();		//Add any processes to the ready queue (from the blocked list) that have unblocked before or at the current algorithm time

			//"CPU idle" i.e. No process available at this time (But there is one on the blocked queue) - Skip to next iteration
			if(readyQueue.isEmpty()) { 
				algorithmTime++; //Increase the simulation time
				continue; 
			}

			Process currentProcess = readyQueue.poll(); //Retrieve the head of the queue to run 
			int allocatedTime = TIME_QAUNTUM; //Each process is allocated a set amount of time to run per cycle

			//Run this process until it's used its whole time quantum or becomes blocked
			while (allocatedTime > 0 && !currentProcess.isProcessFinished()) { 
				int requestedPage = currentProcess.getNextPage();

				if (isPageInMemory(currentProcess, requestedPage)) { //Check if page is in this process' allocated memory

					//Page hit - "run" the instruction
					currentProcess.incrementPosIndex(); //Increment the page index (We've executed this page)
					algorithmTime++; //Add +1 to the simulation time
					allocatedTime--; 

					checkPendingPages(); //Load any pages that are ready at the current time
					unblockProcesses();	 //Check for any unblocked processes before placing this process at the back of the queue (As required in spec - "the unblocked process...is added in the ready queue first and the time-quantum expired process...is added after that")

					//Check finished + set finish time and clear memory frames
					if (currentProcess.isProcessFinished()) {
						currentProcess.setFinishTime(algorithmTime);
						clearProcessFrames(currentProcess); //Process has finished, clear the process' main memory segment (As required in spec, "after a process finishes...the allocated frames to that process becomes free"
					}
				} else {//Page fault
					currentProcess.addFaultTime(algorithmTime); 			//A fault has occurred, add current time to the process' fault list
					loadIntoPendingQueue(currentProcess, requestedPage);	//Load page into pending queue - it will wait here for 4 time units before being placed into main memory

					//Process blocked for 4 time units
					currentProcess.setBlockedTime(algorithmTime + 4);
					blockedQueue.add(currentProcess);
					currentProcess.setIsBlocked(true);
					break; // stop using its time quantum
				}
			}

			//Check if process needs to be added back to the ready queue (i.e. not blocked and has pages left to execute)
			if (!currentProcess.isProcessFinished() && !currentProcess.getIsBocked()) {
				readyQueue.add(currentProcess);
			}
		}
	}
    
	/**
	 * Determines whether a given page for a process is currently loaded in main memory.
	 *
	 * Preconditions: - p != null
	 * 				  - page >= 0
	 * 				  - Process p has a valid offset and frame segment assigned.
	 *
	 * Postconditions: A boolean indicating whether a matching process id and page value has been returned
	 *
	 * @param p - process requesting the page
	 * @param page - the page value being checked
	 * @return true if the page is in memory, otherwise false
	 */
    protected abstract boolean isPageInMemory(Process p, int page);
    
	/**
	 * Loads a process and page data into a frame in main memory.
	 * Locates an empty frame or performs FIFO replacement with an older frame.
	 *
	 *	Preconditions: - p != null
	 *				   - page >= 0
	 *
	 * Postconditions: The requested page is loaded into main memory.
	 * 				   If no free frame was available, the oldest frame has been replaced.
	 * 
	 * @param p - the process who owns the data to be loaded into main memory
	 * @param page - the page value that needs to be stored
	 */
	protected abstract void loadIntoMainMemory(Frame frameData);
	
	/**
	 * Clears all frames belonging to a finished process and marks them as free.
	 *
	 * Preconditions: - p != null
	 * 				  - Process p has a valid offset and frame segment assigned.
	 * 
	 * Postconditions: All frames within the process’s allocated segment are marked empty.
	 *
	 * @param p - the process whose frames are to be cleared
	 */
    protected abstract void clearProcessFrames(Process p);


    /**
     * Adds any frames whose wait time has been reached or elapsed to main memory queue.
     * 
     * This method will check the wait time of the frame at the head of the pending queue to see if it has expired and if so is added to main memory.
     * If this occurs we will loop and check the wait time of the next frame in the queue (the new head) 
     * This will continue until we reach a frame that isn't ready or the queue is empty, at which time the loop (and method) will finish.
     *
     * Preconditions:
     *   - pendingLoads contains Frame objects in the order they were requested.
     *   - Each Frame has a valid load time set.
     *
     * Postconditions:
     *   - All Frames in pendingLoads whose load time has expired (load time + 4 ≤ algorithmTime) will be removed from pendingLoads and loaded into main memory.
     */
	protected void checkPendingPages() {
    	
    	//Loop while the queue isn't empty and check the head of the queue for its expiry time. 
    	while (!pendingQueue.isEmpty()) {
            Frame pendingFrame = pendingQueue.peek(); //Retrieve the head of the queue

            if (pendingFrame.getFrameLoadTime() + 4 <= algorithmTime) {		//Check if the frame's wait time has expired
            	pendingQueue.poll(); 										//Remove frame from head of pendingLoads queue
                loadIntoMainMemory(pendingFrame); 							//Load into main memory - either in an empty frame or replace the oldest frame in memory
            } else {
                break;	//Since it's a FIFO queue, no later frames will be ready either
            }

            //Loops here in case there are more frames that have reached their wait time
        }
    }
    
    
    /**
     * Loads a blocked process' page into the pending queue. The page will wait in this queue for 4 time units before being placed in the main memory queue
     * 
     * Preconditions:
     *  - Process p has requested a page not currently in globalMainMemory.
     *  - algorithmTime reflects the time of the page fault.
     * 
     * Postconditions:
     *  - A new Frame object with the page is added to pendingLoads.
     *  - The page will be ready for processing by the CPU after 4 time units.
     *
     * @param p - the process that caused the page fault
     * @param page - the page value requested
     */
	protected void loadIntoPendingQueue(Process p, int page) {
        
    	//Create a pending frame load request, will be loaded after 4 time units
        Frame pending = new Frame();
        pending.addPageToFrame(p, page, algorithmTime);
        pendingQueue.add(pending);
    }
	
    /**
    * Moves processes from the blocked queue back to the ready queue if their blocked time has expired.
    * Processes will block (be placed in blocked queue) if the page they have requested is not in main memory.
    * They must wait 4 time units in the blocked queue for the page to load into memory. 
    * They are then placed back into the ready queue.
    * 
    * Preconditions: blockedQueue is not null.
    * 
    * Postconditions:
    *  - All processes with blockedTime <= algorithmTime are moved to readyQueue.
    *  - Their isBlocked status is set to false.
    */
	protected void unblockProcesses() {
		//Check all processes in the blocked queue, if their blockedUntil time has been reached/elapsed they can go back on the ready queue
        while (!blockedQueue.isEmpty() && blockedQueue.peek().getBlockedTime() <= algorithmTime) {
        	Process p = blockedQueue.poll();			//Remove process from the front of the blocked queue
        	p.setIsBlocked(false);						//Reset blocked status
        	readyQueue.add(p); 							//Add the process back to the ready queue
        }
	}

    
    /**
     * Prints the results of the simulation for a given page replacement strategy.
     * Displays the turn-around time, number of page faults, and the specific times each page fault occurred for every process.
     *
     * Preconditions: strategyName != null && !strategyName.isEmpty()
     * 			      inputProcesses != null
     * Postconditions: Simulation results have been displayed to the console in the specified format
     *
     * @param strategyName - the name of the page replacement strategy used (e.g., “Fixed-Local Replacement”)
     * @param inputProcesses - the list of processes that were simulated
     */
    protected void printResults(String strategyName, List<Process> inputProcesses) {
        System.out.println("FIFO - " + strategyName + ":");
        System.out.printf("%-4s %-20s %-16s %-9s %s%n",
                "PID", "Process Name", "Turnaround Time", "# Faults", "Fault Times");

        for (Process p : inputProcesses) {
            System.out.printf("%-4d %-20s %-16d %-9d %s%n",
                    p.getProcessID(),
                    p.getName(),
                    p.calcTurnaroundTime(),
                    p.getFaultTimes().size(),
                    p.getFaultTimes().toString().replace("[", "{").replace("]", "}")); //Need to replace [] otherwise will print with square brackets, assignment specs want curly braces
        }
        if(strategyName.equals("Fixed-Local Replacement")) { //Only need one separator line for output...
        	System.out.println("------------------------------------------------------------");
        }
    }
}
