/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 * 
 * Represents a single frame in main memory.
 * Each frame can hold one page from a specific process. If a page is stored in the frame it will also store the process object that owns the page data.
 * 
 * Frames are stored in a data structure that is used to represent main memory for this simulation.
 * 		- LocalReplacementScheduler uses a list of frames
 *      - GlobalReplacementScheduler uses a queue of frames
 */
 
public class Frame {
	private Process frameOwner;		//Process object that "owns" the page data in this frame
    private int pageValue;			//Value of the page stored in this frame
    private boolean empty;			//Flag that indicates whether this frame is holding a page or not
    private int frameLoadTime;		//Time a page has been added to this frame

    /**
     * Constructor for the frame class.
     * Initialises this frame as empty.
     *
     * Preconditions: none
     * Postconditions: Frame is marked as empty with default placeholder values (-1)
     */
    public Frame() {
        clearPageFromFrame();
    }

    /**
     * Checks whether the frame is currently empty (not holding a page).
     *
     * @return true if the frame is empty; false otherwise
     */
    public boolean isFree() {
        return empty;
    }

    /**
     * Loads a page into this frame and assigns ownership to a process.
     *
     * Preconditions: - p is not null
     *   			  - page >= 0 — must be a valid page number
     *   			  - currentTime >= 0 — simulation time must be non-negative
     *   
     * Postconditions: - The frame is marked as occupied
     *   			   - Process, page value, and load time are updated
     *
     * @param p process owning the page value
     * @param page the page value to store
     * @param currentTime the current simulation time when the page is loaded
     */
    public void addPageToFrame(Process p, int page, int currentTime) {
    	this.frameOwner = p;
        this.pageValue = page;
        this.frameLoadTime = currentTime; //Store when the page was loaded into this frame
        this.empty = false;
    }

    /**
     * Clears this frame and resets it to an empty state.
     *
     * Preconditions: none
     * Postconditions:	- The frame no longer holds any page data
	 * 				  	- All values are reset to defaults (-1 or 0)
     *   				- The frame is marked as free
     */
    public void clearPageFromFrame() {
        this.frameOwner = null;  
        this.pageValue = -1;	// -1 means empty
        this.frameLoadTime = 0;
        this.empty = true;
    }

    /**
     * Retrieves the process object of the process currently owning this frame.
     *
     * @return process or null if the frame is empty
     */
    public Process getOwnerProcess() {
        return frameOwner;
    }
    
    /**
     * Retrieves the ID of the process currently owning this frame.
     *
     * @return process ID, or -1 if the frame is free
     */
    public int getOwnerProcessID() {
        return frameOwner.getProcessID();
    }

    /**
     * Retrieves the page value currently stored in this frame.
     *
     * @return page value, or -1 if the frame is free
     */
    public int getPageValue() {
        return pageValue;
    }
    
    /**
     * Retrieves the simulation time when the page was loaded into this frame.
     *
     * @return frame load time, or 0 if the frame is empty
     */
    public int getFrameLoadTime() {
        return frameLoadTime;
    }
    
    /**
     * Determines whether this frame contains a specific page from a specific process.
     *
     * Preconditions:  p is not null and page >= 0
     * Postconditions: none
     *
     * @param process owner to check for a matching ID
     * @param the page value to check
     * @return true if this frame holds the specified page from the given process;
     *         false otherwise
     */
    public boolean checkPageInMemory(Process p, int page) {
    	if(empty) {
    		return false; //The frame is empty and doesn't contain the required page
    	}else {
    		//Not empty check for matching page value and ID's
    		return this.frameOwner.getProcessID() == p.getProcessID() && this.pageValue == page;
    	}
    }
    
    /**
     * Copies the contents of another frame into this frame.
     * This method is used during frame replacement operations.
     *
     * Preconditions:	other != null
     * Postconditions: This frame now contains the same data as other
     *   			   This frame is marked as occupied
     *
     * @param the frame whose data will be copied into this frame
     */
    public void copyFrom(Frame other) {
        this.frameOwner = other.frameOwner;
        this.pageValue = other.pageValue;
        this.frameLoadTime = other.frameLoadTime;
        this.empty = false;
    }
    
    /**
     * Returns a string representation of the frame. Used to nicely display a frame for testing.
     *
     * @return formatted string containing process ID, page value, and load time
     */
    @Override
    public String toString() {
        return "Frame{ processOwner= " + frameOwner.getProcessID() + " value= " + pageValue + " time= " + frameLoadTime+"}";
    }
}
