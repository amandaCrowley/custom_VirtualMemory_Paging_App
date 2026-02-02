/*
 * Author: Amanda Foxley c3137540
 * Created: 27/09/25
 * COMP2240 Assignment 3
 * 
 * Main driver class for the A3 paging with virtual memory simulation.
 * 
 * Two different FIFO page replacement policies are simulated: 
 * 		- Fixed allocation with local replacement
 * 		- Variable allocation with global replacement
 * Both simulations use a Round Robin short-term scheduling algorithm, the time quantum for this is passed into the program using command line arguments - args[1]
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class A3 {

	/**
	 * Main method for the A3 virtual memory simulation.
	 * Initial program inputs are taken from command line arguments.
	 *
	 * Preconditions: args cannot be null 
	 *  			  Input file/s must follow correct format, as specified in the assignment specs
	 * Postconditions: Input has been read from file
	 * 				   Process threads have been run in the virtual memory simulations, turnaround times and fault data has been displayed to the console
	 * 
	 * @param args program arguments, where:
	 * 			- args[0] is the number of frames(F) allocated for this simulation
	 * 			- args[1] is the time quantum(Q) to be used for this simulation
	 * 			- all following args are in the form of an input file containing process information (e.g. process1.txt process2.txt) and can use any file extension type
	 * 		  When running the program the arguments should look like: java A3 F Q data1 data2 ... dataN
	 * 
	 * @throws FileNotFoundException if the file cannot be found or opened
	 * @throws Exception for any other unexpected parsing errors
	 */
	public static void main(String[] args) throws Exception {

		//No files found
		if (args.length == 0) {
			System.out.println("Error: Input file not found");
			return;
		}

		//Parse initial simulation inputs
		int numFrames = Integer.parseInt(args[0]); 						//F - frames
		int quantum   = Integer.parseInt(args[1]); 						//Q - time quantum

		List<Process> processes = new ArrayList<>();					//List of processes from input files
		
		int processId = 1; 												//Start process id's from 1
		for (int i = 2; i < args.length; i++) { 						//Start from index 2 as the first 2 arguments in the args list are number frames (index 0) and time quantum (index 1)
			File inputFile = new File(args[i]);
			
			try {
				Process p = parseProcessFile(inputFile, processId++);	//Parse each input file into a process object (Each input file represents one process and it's page info)
				processes.add(p);

			}catch(FileNotFoundException e) {  //Input file not found
				System.out.println("Error: Input file not found");
				e.printStackTrace();
			}
			catch (Exception e) { //Generic error
				System.out.println("Error: " + e.getMessage());
			}
		}
		
		//Run simulations
		LocalReplacementScheduler local = new LocalReplacementScheduler(processes, numFrames, quantum);
        local.run(processes);

        GlobalReplacementScheduler global = new GlobalReplacementScheduler(processes, numFrames, quantum);
        global.run(processes);
	}
	
	/**
     * Parses a single process file into a Process object.
     * Expected input format:
     *   name: Process1; page: 1; page: 2; ... ; end;
     *
     * @param inputFile  the file containing process description
     * @param processID  the unique ID assigned to this process
     * @return Process object with name and list of page references
     * 
     * @throws IllegalArgumentException if the file format is invalid or if the process contains more than 50 pages
     */
	public static Process parseProcessFile(File inputFile, int processID) throws Exception{

		//Read whole file into a single string
	    StringBuilder sb = new StringBuilder();
	    try (Scanner scan = new Scanner(inputFile)) {
	        while (scan.hasNextLine()) {
	            sb.append(scan.nextLine()).append(" ");
	        }
	    }

	    //Now we get the process info out of the string to be placed into a process object
	    String content = sb.toString().trim();
	    String[] parts = content.split(";"); 									//Split the input by semi-colon

	    String processName = null;
	    List<Integer> pages = new ArrayList<Integer>();							//Add pages to list

	    for (String part : parts) {
	        part = part.trim();
	        
	        if (part.startsWith("name:")) { 									//Process name
	            processName = part.substring(5).trim();
	        } else if (part.startsWith("page:")) {								//Pages containing instruction/s this process wants to run, add these to the pages a list
	            int pageNum = Integer.parseInt(part.substring(5).trim());
	            pages.add(pageNum);												//Page number value
	        } else if (part.equalsIgnoreCase("end")) {							//End of process file
	            break;
	        }
	    }
	    
	    //Stop simulation if process has too many pages - Rule in assignment spec
        if (pages.size() > 50) {
        	throw new IllegalArgumentException("Process \"" + processName +  "\" exceeds maximum of 50 pages (" + pages.size() + " found) " + "this process will not be included in the simulation");
        }
	    
	    return new Process(processID, processName, pages); //Return the newly created process object
	}
}



