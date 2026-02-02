/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 * 
 * This scheduler demonstrates the Fixed-allocation with local replacement scope policy
 * 
 * In this scheme, each process is assigned a fixed subset of frames in main memory. i.e. mainMemory frames list
 * Processes use an offset index to determine where their segment of memory starts in the list.
 * The end of this segment is determined by the number of frames allocated per process.
 * When a page fault occurs, the page replacement is handled only within that process’s  allocated memory space — no other processes are affected.
 * Page replacement uses a FIFO policy
 * Allocated frames to other processes do not change, even when another process finishes execution, these now empty frames can't be used by another process.

 * Processes execute one page per time unit and are blocked for four time units whenever a page fault occurs. 
 * The scheduler continues execution until all processes have completed their page sequences.
 * Once all processes have finished execution the results are displayed to the console.
 */

import java.util.ArrayList;
import java.util.List;

public class LocalReplacementScheduler extends Scheduler {

	private final List<Frame> mainMemory = new ArrayList<>(); 				//Represents all frames in main memory. Each process is given a fixed segment to work with.

	/**
	 * Constructor for LocalReplacementScheduler class
	 *  
	 * Each process is assigned a contiguous block of frames determined by dividing the total number of frames by the number of processes.
	 *
	 * Preconditions: - processes != null && !processes.isEmpty()
	 * 				  - numFrames > 0
	 * 				  - quantum > 0
	 *
	 * Postconditions:  - All Frame objects are initialised as empty
	 *   				- Each process has a fixed, unique segment of the memory frame list assigned.
	 *
	 * @param processes - a list of Process objects participating in the simulation
	 * @param numFrames - the total number of frames (F) available in main memory
	 * @param quantum - the time quantum (Q) used for Round Robin process scheduling
	 */
	public LocalReplacementScheduler(List<Process> processes, int numFrames, int quantum) {
		super(processes, numFrames, quantum); //Initialise the scheduler with the processes required for the simulation, the number of frames allocated, the time quantum and set the algorithm time to zero

		//Initialise each frame in the frames list
		for (int i = 0; i < numFrames; i++) {
			mainMemory.add(new Frame());
		}

		//Assign offset to each process - This determines what section of the frames list each process "owns"
		int offset = 0;
		for (Process p : processes) {
			p.setOffset(offset);
			offset += framesPerProcess; //Calculate the next process' offset
		}
	}

	/**
	 * Executes the Round Robin simulation for the fixed-allocation local replacement model.
	 * Each process executes its pages in a specified time quanta, experiencing page faults and blocking as required.
	 * The simulation runs until both the ready and blocked queues are empty.
	 *
	 * Preconditions: processes != null && !processes.isEmpty()
	 * 
	 * Postconditions: All processes have executed their page instructions.
	 * 				   Process data, turn-around time and fault data are printed to the console
	 * 
	 * @param processes - the list of processes to simulate
	 */
	public void run(List<Process> processes) {
		super.runSimulation(processes);									//Run the simulation
		super.printResults("Fixed-Local Replacement", processes); 		//Print results to console
	}


	/**
	 * {@inheritDoc}
	 * See super class comments for base implementation details
	 *
	 * This implementation uses a fixed allocation scheme:
	 * 		- Each process has a fixed segment of frames, only use this segment of memory to load the frame data
	 * 		- Page replacement uses FIFO within that segment
	 */
	@Override
	protected void loadIntoMainMemory(Frame frameData) {
		Process p = frameData.getOwnerProcess();
		int start = p.getStartOffset();
		int end = start + framesPerProcess;

		//Check for free frame first
		for (int i = start; i < end; i++) { //Only check the segment allocated for this process
			Frame f = mainMemory.get(i);		

			if (f.isFree()) { //Found a free frame, add process/page info and return
				f.addPageToFrame(p, frameData.getPageValue(), algorithmTime);
				return; 
			}
		}

		//FIFO replacement - get oldest frame, and use this space instead
		Frame oldest = mainMemory.get(start);
		for (int i = start + 1; i < end; i++) {	//Only check the segment allocated for this process
			if (mainMemory.get(i).getFrameLoadTime() < oldest.getFrameLoadTime()) { //Found an older frame
				oldest = mainMemory.get(i);
			}
		}
		oldest.addPageToFrame(p, frameData.getPageValue(), algorithmTime); //Replace oldest frame's process/page info
	}

	/**
	 * {@inheritDoc}
	 * See super class comments for base implementation details
	 *
	 * This implementation uses a fixed allocation scheme:
	 * 		- Each process has a fixed segment of frames, only check for a match within this segment of memory
	 */
	@Override
	protected boolean isPageInMemory(Process p, int page) {

		int start = p.getStartOffset();
		int end = start + framesPerProcess;


		for (int i = start; i < end; i++) { 	//Only check the segment allocated for this process
			Frame currentFrame = mainMemory.get(i);
			if (!currentFrame.isFree() && currentFrame.getOwnerProcessID() == p.getProcessID() && currentFrame.getPageValue() == page) { //Can't be empty + owner id and page values should match
				return true; //Match found
			}
		}
		return false; //No match found, page not in main memory
	}

	/**
	 * {@inheritDoc}
	 * See super class comments for base implementation details
	 *
	 * This implementation uses a fixed allocation scheme:
	 * 		- Each process has a fixed segment of frames, only clear the frames in memory segment belonging to this process
	 * 		
	 */
	@Override
	protected void clearProcessFrames(Process p) {
		int start = p.getStartOffset();
		int end = start + framesPerProcess;

		for (int i = start; i < end; i++) {
			mainMemory.get(i).clearPageFromFrame(); //Reset frame to default values
		}
	}
}