/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 * 
 * Represents an individual process in the virtual memory simulation.
 * Each process holds a list of page references to be executed and tracks its progress using an index of the list
 * Also stores page faults, various variables to track blocking periods (Times the process can't run)
 * Uses an offset to track the starting index of memory segment allocated to this process (An index in a list of frames that represents "memory") - Used in fixed local replacement scheduler
 * 
 * This class is used by both the local and global page replacement schedulers.
 */

import java.util.ArrayList;
import java.util.List;

public class Process {
	
	//Data from input file
	private final int PROCESSID;				//Unique process ID
    private final String NAME;					//Process name
    private final List<Integer> PAGES;			//List of pages to be executed for this process

    //Simulation variables
    private int positionIndex = 0;				//Index of the next page to be executed
    private int finishTime = -1;         		//Time when process finishes
    private int blockedUntil = -1;      		//Time when process becomes unblocked
    private int arrivalTime = 0; 				//Time when process arrived - All processes arrive at t=0
    private List<Integer> faultTimes;  	 		//List of times when page faults occurred
    private boolean isBlocked = false;			//Flag for whether this process is blocked or not
    private int listOffset;					 	//Starting index of frame list allocated to this process - A process "owns" a segment in the main memory (Frames list) - Used in fixed local replacement scheduler
    
    /**
     * Constructs a Process instance with the given ID, name, and page reference sequence.
     *
     * Preconditions: pages must not be null or empty.
     * Postconditions: A new Process object is created with initialised state.
     * 
     * @param ID unique identifier for the process
     * @param name process name
     * @param pages list of pages to execute for this process
     */
    public Process(int ID, String name, List<Integer> pages) {   	
    	this.PROCESSID = ID;
        this.NAME = name;
        this.PAGES = pages;
        this.faultTimes = new ArrayList<>();
    }

    /**
     * Records a page fault at the specified simulation time.
     * 
     * Preconditions: currentTime >= 0
     * Postconditions: faultTimes list includes currentTime.
     * 
     * @param currentTime simulation time when fault occurred
     */
    public void addFaultTime(int currentTime) {
        faultTimes.add(currentTime);
    }

    /**
     * Advances the index counter to the index of the next page in the list.
     * 
     * Preconditions: positionIndex < total number of pages.
     * Postconditions: positionIndex is incremented by one.
     */
    public void incrementPosIndex() {
    	positionIndex++;
    } 

    /**
     * Checks if this process has completed execution.
     * 
     * @return true if all pages have been executed, false otherwise
     */
    public boolean isProcessFinished() {
        return positionIndex >= PAGES.size();
    }
    
    /**
     * Calculates the turnaround time for this process.
     * 
     * Preconditions: finishTime > arrivalTime
     * Postconditions: None.
     * 
     * @return turnaround time (finishTime - arrivalTime)
     */
    public int calcTurnaroundTime() {
        return finishTime - arrivalTime;
    }
    
    /**
     * Resets all runtime attributes to prepare the process for reuse.
     * 
     * Preconditions: None.
     * Postconditions: All state variables reset to initial conditions.
     */
	public void resetProcess() {
		this.faultTimes = new ArrayList<>();
		this.blockedUntil = -1;
		this.isBlocked = false;
		this.finishTime = -1;
		this.positionIndex = 0;
	}
	
    /**
     * Provides a string representation of the process. Used to nicely display a process for testing.
     * 
     * @return formatted string with process name and number of pages
     */
    @Override
    public String toString() {
        return "Process{name='" + NAME + "', pages=" + PAGES.size() + "}";
    }
    
    //------------------------------Getters---------------------------------------
    /**
     * Retrieves the process ID.
     * 
     * @return unique process identifier
     */
	public int getProcessID() {
        return PROCESSID;
    }
    
    /**
     * Retrieves the process name.
     * 
     * @return process name
     */
    public String getName() {
        return NAME;
    }

    /**
     * Retrieves the next page to be accessed by the process.
     * 
     * Preconditions: positionIndex must be within bounds of PAGES.
     * Postconditions: None.
     * 
     * @return next page number, or null if all pages have been accessed
     */
    public Integer getNextPage() {
    	if (positionIndex < PAGES.size()) {
            return PAGES.get(positionIndex);
        }
        return null; //Reached end of pages list
    }

    /**
     * Retrieves all recorded page fault times.
     * 
     * @return list of fault occurrence times
     */
    public List<Integer> getFaultTimes() {
        return faultTimes;
    }

    /**
     * Retrieves the simulation time when the process will become unblocked.
     * 
     * @return unblock time
     */
    public int getBlockedTime() {
        return blockedUntil;
    }

    /**
     * Retrieves the starting frame offset owned by this process.
     * 
     * @return starting index in frame list for this process
     */
    public int getStartOffset() { 
    	return listOffset; 
    }
    
    /**
     * Retrieves the flag indicating if the process is currently blocked.
     * 
     * @return true if blocked, false otherwise
     */
	public boolean getIsBocked() {
		return isBlocked;
	}
	
	//-------------------------Setters-------------------------------------------------------

    /**
     * Assigns the starting frame offset in the global frame list for this process.
     * Used by the Local Replacement Scheduler to define frame ownership.
     * 
     * Preconditions: startFrame >= 0
     * Postconditions: listOffset is set to startFrame.
     * 
     * @param startFrame index of the first frame allocated to this process
     */
    public void setOffset(int startFrame) {
        this.listOffset = startFrame;
    }
    
    /**
     * Sets the time at which the process will become unblocked.
     * 
     * Preconditions: unblockTime >= current simulation time
     * Postconditions: blockedUntil is updated.
     * 
     * @param unblockTime time when process becomes unblocked
     */

    public void setBlockedTime(int unblockTime) {
        this.blockedUntil = unblockTime;
    }

    /**
     * Updates whether the process is currently blocked.
     * 
     * @param blocked true if blocked, false otherwise
     */
    public void setIsBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }
    
    /**
     * Sets the time when the process finishes execution.
     * 
     * Preconditions: currentTime >= 0
     * Postconditions: finishTime is set.
     * 
     * @param currentTime current simulation time
     */
    public void setFinishTime(int currentTime) {
        this.finishTime = currentTime;
    }    
}