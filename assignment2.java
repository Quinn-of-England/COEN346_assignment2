package COEN346_assignment2;
/* COEN 346 Practical Assignment 1
 * Mark Zalass - 40097293
 * Quinn Hogg - 40093086
 */
import java.io.File;
import java.util.Scanner;

public class assignment2 {

    public static void main(String[] args){
        int[] processes= null;
        int[] readytime= null; //time of arrival/ready time (1st column)
        int[] exect= null; //required execution time (2nd column)
        if (args.length != 1) { // Checks if arg is present, will exit if not correct (file name/path passed from args[0]) 
            System.err.println("You must pass the input file name");
            System.exit(0);
        }
        try { // Reads file using provided file name
            String fileName = args[0];
            File input = new File(fileName);
            processes = ReadInput(fileName, input);
            int n = processes.length;
        }
        catch(Exception e) { // If file cannot be opened
            System.err.println("No file found");
        }
        printFunction(processes, n, exect, quantum);

    }
    // public static int[] ReadInput(String fileName, File file) throws Exception {
    //     Scanner s = new Scanner(file); // Reads first entry for # of inputs
    //     int inputNum = Integer.parseInt(s.nextLine());
    //     int[] inputs = new int[inputNum];
    //     int linecount = 0;
    //     while (s.hasNextLine()) { // Reads all inputs and stores it into an array
    //         inputs[linecount] = Integer.parseInt(s.nextLine());
    //         linecount++;
    //     }
    //     s.close();
    //     return inputs;
    // }

    // waiting time method
    static void findWaitingTime(int processes[], int n, 
                 int exect[], int wt[], int quantum) 
    { 
        // copy of exec time to store remaining exec times exect[]
        int rem_exect[] = new int[n]; 
        for (int i = 0 ; i < n ; i++) 
            rem_exect[i] =  exect[i]; 
            quantum = rem_exect[i]/10;

        int t = 0; // Current time 
       
        //round robin 
        while(true) 
        { 
            boolean done = true; 
       
            // Traverse all processes one by one repeatedly 
            for (int i = 0 ; i < n; i++) 
            { 
                // If execution time of a process is greater than 0 
                // then they need to be processed
                if (rem_exect[i] > 0) 
                { 
                    done = false; // There is a pending process 
       
                    if (rem_exect[i] > quantum) 
                    { 
                        // Increase the value of t i.e. shows 
                        // how much time a process has been processed 
                        t += quantum; 
       
                        // Decrease the exec_time of current process 
                        // by quantum 
                        rem_exect[i] -= quantum; 
                    } 
       
                    else
                    { 
                        // Increase the value of t i.e. shows 
                        // how much time a process has been processed 
                        t = t + rem_exect[i]; 
       
                        // Waiting time is current time minus time 
                        // used by this process 
                        wt[i] = t - exect[i]; 
       
                        // As the process gets fully executed 
                        // make its remaining execution time = 0 
                        rem_exect[i] = 0; 
                    } 
                } 
            } 
        
            if (done == true) 
              break; 
        } 
    } 
    static void printFunction(int processes[], int n, int exect[], 
                                         int quantum) 
    { 
        int wt[] = new int[n]; 
        int total_wt = 0; 
       
        // Function to find waiting time of all processes 
        findWaitingTime(processes, n, bt, wt, quantum); 
       
       
        // Calculate total waiting time
        for (int i=0; i<n; i++) 
        { 
            total_wt = total_wt + wt[i]; 
            System.out.println("Time " + (i+1) + ", Process " + exect[i] + ", " + wt[i] + "/t"); //Need to add resume, pause, started, finished
        } 
        System.out.println("--------------------------------------" + "\t");
        System.out.println("Waiting time : " + "\t" +
        "Process " + (i+1) + ": " + wt[i] + "/t");
    } 
}
    