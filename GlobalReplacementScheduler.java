/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 * 
 * This scheduler demonstrates the variable-allocation with global replacement scope policy
 * 
 * In this scheme, no specific frame is allocated to any process rather all frames are available to the processes for use i.e. global mainMemory queue
 * Page replacement uses a FIFO policy and will consider all the pages in the user memory space, i.e., the page selected for replacement may belong to any process running in the system
 * When a process finishes execution, the frames allocated to that finished process are released immediately and will be available for loading new pages if necessary.
 * Variable allocation scheme - number of page frames allocated to a process is varied over the lifetime of the process
 * 					   	      - processes with higher fault rates are allocated more frames (Since they request page storage more frequently, they are allocated more room in memory)
 * 					          - processes with lower fault rates are allocated fewer frames (Since they request page storage less frequently, they are allocated less room in memory)
 * 
 * Processes execute one page per time unit and are blocked for four time units whenever a page fault occurs. 
 * The scheduler continues execution until all processes have completed their page sequences.
 * Once all processes have finished execution the results are displayed to the console.
 */

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GlobalReplacementScheduler extends Scheduler {
	
    private final Queue<Frame> globalMainMemory = new LinkedList<Frame>(); 		//Global frame queue. Processes can untilise space anywhere in this queue. FIFO replacement is applied to the whole queue.

    /**
	 * Constructor for GlobalReplacementScheduler class
	 *  
	 * Each process can store data in a global main memory queue.
	 *
	 * Preconditions: - processes != null && !processes.isEmpty()
	 * 				  - numFrames > 0
	 * 				  - quantum > 0
	 *
	 * Postconditions:  - All Frame objects are initialised as empty
	 *   				- Each process in the process list has had it's variables reset to default values, ready for this simulation.
	 *
	 * @param processes - a list of Process objects participating in the simulation
	 * @param numFrames - the total number of frames (F) available in main memory
	 * @param quantum - the time quantum (Q) used for Round Robin process scheduling
	 */
    public GlobalReplacementScheduler(List<Process> processes, int numFrames, int quantum) {
        super(processes, numFrames, quantum);
        
        //Initialise all frames in the global main memory queue
        for (int i = 0; i < numFrames; i++) {
        	globalMainMemory.add(new Frame());
        }
        
        //Since this scheduler runs after the Local replacement scheduler, reset the process simulation variables back to their defaults so we can run this simulation
        for(Process p: processes) {
        	p.resetProcess();
        }
    }

    /**
	 * Executes the Round Robin simulation for the variable-allocation global replacement model.
	 * Each process executes its pages in time quanta, experiencing page faults and blocking as required.
	 * The simulation runs until both the ready and blocked queues are empty.
	 *
	 * Preconditions: processes != null && !processes.isEmpty()
	 * 
	 * Postconditions: All processes have executed their page instructions.
	 * 				   Process data, turnaround time and fault data are printed to the console
	 * 
	 * @param processes - the list of processes to simulate
	 */
    public void run(List<Process> processes) {
    	
    	super.runSimulation(processes);											//Run the simulation
    	super.printResults("Variable-Global Replacement", processes);			//Print results to console
    }
    
    /**
     * Checks if a requested page is already in globalMainMemory queue for a given process.
     * 
     * Preconditions:
     *  - globalMainMemory contains valid Frame objects.
     *  - currentProcess is not null.
     * 
     * Postconditions:
     *  - Returns true if the page exists in main memory queue.
     *  - Returns false otherwise.
     *
     * @param page - the page value being checked
     * @param currentProcess - the process making the request for this page
     * @return true if the page is in memory, false otherwise
     */
    @Override
	protected boolean isPageInMemory(Process currentProcess, int page) {
    	
    	//Loop through all processes in main memory, check for a frame with a matching page value and ownerID
        for (Frame frame : globalMainMemory) {
            if (!frame.isFree() && frame.getPageValue() == page && frame.getOwnerProcessID() == currentProcess.getProcessID()) {
                return true; //Match found
            }
        }
        return false; //No match, page is not stored in memory
    }

    /**
     * This simulation uses a FIFO replacement strategy to place page data into a global main memory queue
     * 
     * Checks for an empty frame, if found the page's data is placed here (in a frame)
     * If no empty frame is found, the oldest frame is removed from the head of the queue, 
     * the old frame data is replaced with the new page's data and the frame is re-added to the back of the queue.
     * 
     * Preconditions:
     *  - newFrame is a Frame object representing a page ready to be loaded.
     * 
     * Postconditions:
     *  - If a free frame exists, newFrame is copied into it.
     *  - Otherwise, the oldest frame is evicted and replaced by newFrame.
     *
     * @param newFrameData - the frame containing the page to load
     */
    @Override
	protected void loadIntoMainMemory(Frame newFrameData) {
        
    	//Check for empty frame first
        for (Frame frame : globalMainMemory) {
            if (frame.isFree()) { 							//Empty frame found, place the page data here
                frame.copyFrom(newFrameData); 				//Use helper method in Frame class to copy data (page/processID/loadedTime) to the empty frame
                return;
            }
        }

        //FIFO replacement - Oldest frame is removed from the queue, overwritten with the new frame data and re-added to the back of the queue
        Frame oldest = globalMainMemory.poll();
        oldest.copyFrom(newFrameData);
        globalMainMemory.add(oldest);
    }
            
    /**
     * Clears all frames owned by a process when it finishes.
     * 
     * Preconditions: 	Process p has finished execution.
     * Postconditions:	All frames belonging to p are marked as free.
     * 
     * @param p - the process whose frames should be released.
     */
    @Override
	protected void clearProcessFrames(Process p) {
    	
    	//Loop through all processes used by this process in main memory and reset all frames values to default (Ensuring this frame can be re-used by a different process) 
        for (Frame frame : globalMainMemory) {
        	
        	if(!frame.isFree()) { //Only check frames that arn't empty
        		if (frame.getOwnerProcessID() == p.getProcessID()) { 	//Process owns frame
                    frame.clearPageFromFrame();							//Helper method in frame class to reset to default values
                }
        	}
        }
    }
}
