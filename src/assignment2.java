
/* COEN 346 Practical Assignment 2
 * Mark Zalass - 40097293
 * Quinn Hogg - 40093086
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.text.DecimalFormat;

class Assignment2 {

    public static void main(String[] args) throws FileNotFoundException{
        if (args.length != 1) { // Checks if arg is present, will exit if not correct (file name/path passed from args[0])
            System.err.println("You must pass the input file name");
            System.exit(0);
        }

        // File name of input stored from command line argument
        String fileName = args[0];

        // Processes read from file, line from input file stored in array list
        ArrayList<String> inProcesses = readInput(fileName);

        // Number of processes defined
        int processNum = inProcesses.size();

        // Array of processes created
        Process[] processes = new Process[processNum];

        // Arrival time and burst time separated and used to create process objects
        for (int i = 0; i < processNum; i++) {
            String[] split = inProcesses.get(i).split(" ");
            int arrivalTime = Integer.parseInt(split[0]);
            int burstTime = Integer.parseInt(split[1]);
            Process p = new Process(arrivalTime, burstTime, i); // New process created
            processes[i] = p; // New process added to processes array
        }

        // Output file created and System.out set to output to file
        PrintStream output = new PrintStream(new File("output.txt"));
        System.setOut(output);

        // Scheduler object and thread created and started
        Scheduler taskScheduler = new Scheduler(processes, processNum);
        Thread schedulerThread = new Thread(taskScheduler);
        schedulerThread.start();
    }

    /* Function to read input file line by line */
    public static ArrayList<String> readInput(String fileName) {
        ArrayList<String> inLine = new ArrayList<>();
        try {
            InputStream is = new FileInputStream(fileName); // Input stream from file opened
            BufferedReader br = new BufferedReader(new InputStreamReader(is)); // Line stored in buffered reader
            String readLine; // String storing the line read
            while ((readLine = br.readLine()) != null) {
                inLine.add(readLine); // Line read added to array list of all processes (lines read)
            }
            br.close();
        } catch (Exception e) { // If file fails to be read, exception generated and error message shown
            e.printStackTrace();
            System.err.println("Error reading file");
        }
        return inLine;
    }
}

class Process implements Runnable {
    /* Variables for process*/
    private int arrivalTime;
    private float waitingTime = 0;
    private float remainingTime;
    private float time;
    private volatile Boolean hasCPU = false;
    private Boolean isFinished = false;
    private float quantum;
    private float runTime = 0;
    private float timeToRun;
    private Boolean hasRun = false;
    private int processNum;

    /* Format floats to 2 decimal places */
    private static DecimalFormat df = new DecimalFormat("0.00");

    /* Constructor */
    Process(int arrivalTime, int burstTime, int processNum) {
        this.arrivalTime = arrivalTime;
        this.remainingTime = burstTime;
        quantum = (float) 0.10 * burstTime; // Quantum 10% of the remaining time (initial)
        this.processNum = processNum;
    }

    @Override
    public void run() {
        while (true) {
            while (!hasCPU || isFinished) Thread.onSpinWait(); // If thread starts but process does not have CPU, wait until given CPU
            timeToRun = Math.min(remainingTime, quantum); // Process will run for either the whole quantum or earlier if time remaining is less than the quantum
            time += timeToRun; // Time increments
            remainingTime -= timeToRun; // Time ran deducted from time remaining for the process to execute
            runTime += timeToRun; // Time to run (time process ran in quantum) added to total time ran

            hasCPU = false; // Process relinquishes CPU back to the scheduler

            if (remainingTime <= 0) { // If the process is done
                waitingTime = (float) (time - runTime - arrivalTime); // Waiting time calculated
                isFinished = true; // Process signals to scheduler that it is done execution
            }
        }
    }

    /* Getters */
    public Boolean getFinished() {
        return isFinished;
    }
    public float getWaitingTime() {
        return waitingTime;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public float getTimeToRun() {
        return timeToRun;
    }
    public Boolean getHasCPU() {
        return hasCPU;
    }
    public Boolean getHasRun() {
        return hasRun;
    }
    public int getProcessNum() {
        return processNum;
    }
    public float getRemainingTime() {
        return remainingTime;
    }

    /* Setters */
    public void setHasCPU(Boolean hasCPU) {
        this.hasCPU = hasCPU;
    }
    public void setTime(float time) {
        this.time = time;
    }
    public void setHasRun(Boolean hasRun) {
        this.hasRun = hasRun;
    }
}

class Scheduler implements Runnable {

    /* Variables for scheduler */
    private int numProcesses;
    private float time = 0;
    private PriorityQueue<Process> arrivalQueue;
    private PriorityQueue<Process> readyQueue;
    private float[] waitTimes;
    private Boolean[] processFinished;

    /* Format outputs to 2 decimal places */
    private static DecimalFormat df = new DecimalFormat("0.00");

    /* Constructor */
    Scheduler(Process[] processes, int numProcesses) {
        this.numProcesses = numProcesses;

        // Create ready queue
        Comparator<Process> remainingTimeCompare = new RemainingTimeComparator();
        readyQueue = new PriorityQueue<>(numProcesses, remainingTimeCompare);

        // Create arrival queue
        Comparator<Process> arrivalTimeCompare = new ArrivalTimeComparator();
        arrivalQueue = new PriorityQueue<>(numProcesses, arrivalTimeCompare);

        // Create arrays for wait times and finished status (initialize to false)
        waitTimes = new float[numProcesses];
        processFinished = new Boolean[numProcesses];
        Arrays.fill(processFinished, false);

        addToArrivalQueue(processes); // All processes added to arrival queue
    }

    @Override
    public void run() { // When scheduler is started
        // Keep running processes until all have completed
        while (!isDone()) {
            if (readyQueue.isEmpty() && !arrivalQueue.isEmpty()) { // If all arrived processes have run but some processes have not arrived yet
                time = arrivalQueue.element().getArrivalTime(); // Current time set to next process' arrival time
            }
            if (!arrivalQueue.isEmpty()) { // If there are still processes that have arrived but not in ready queue
                addToReadyQueue(arrivalQueue); // Add to ready queue
            }
            runProcess(readyQueue.element()); // Run process with shorted remaining time
        }
        // When all processes have concluded, print waiting times
        System.out.println("-----------------------------");
        System.out.println("Waiting Times:");
        for (int i = 0; i < waitTimes.length; i++) {
            System.out.println("Process " + (i + 1) + ": " + Math.ceil(waitTimes[i]));
        }
    }

    /* Function that adds all processes entering scheduler to the arrival queue */
    void addToArrivalQueue (Process[] processes) {
        arrivalQueue.addAll(Arrays.asList(processes).subList(0, numProcesses));
    }

    /* Function that adds processes to the ready queue once they have arrived */
    void addToReadyQueue (PriorityQueue<Process> arrivalQueue) {
        // Current time is at or after the arrival time
        if (arrivalQueue.element().getArrivalTime() <= time) {
            readyQueue.add(arrivalQueue.element()); // Added to ready queue
            arrivalQueue.remove(); // Removed from arrival queue
        }
    }

    /* Function that determines whether all processes have completed */
    Boolean isDone() {
        boolean done = true;
        for (int i = 0; i < numProcesses; i++) {
            if (!processFinished[i]) {
                done = false;
                break;
            }
        }
        return done;
    }

    /* Function that runs the process (quantum) */
    void runProcess(Process process) {
        // If process has never started, start the process
        if (!process.getHasRun()) {
            // Process knows that it has run
            process.setHasRun(true);
            System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Started");
        }

        Thread pThread = new Thread(process);
        // Process thread starts, will busy wait until given CPU access
        pThread.start();

        // Process thread resumes
        pThread.resume();

        // Process' time (clock) updated with current time
        process.setTime(time);

        // Process given CPU access, can now execute resume execution
        process.setHasCPU(true);
        System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Resumed");

        // Until CPU given back to the scheduler, scheduler will wait
        while (process.getHasCPU()) Thread.onSpinWait();

        // Once CPU given back to the scheduler, scheduler suspends the process
        pThread.suspend();

        // Global time incremented by the time the process ran for
        time += process.getTimeToRun();
        System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Paused");

        // If the process reports to the scheduler that it has finished its execution
        if (process.getFinished()) {
            System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Finished");
            waitTimes[process.getProcessNum()] = process.getWaitingTime(); // Total wait time stored in wait times array
            processFinished[process.getProcessNum()] = true; // Process marked as finished
            readyQueue.remove(); // Process removed from ready queue
        }
        pThread.stop(); // Thread stopped
    }
}

/* Comparator to implement ready queue (sort by time remaining) */
class RemainingTimeComparator implements Comparator<Process>{
    // Sort by time remaining
    @Override
    public int compare(Process p1, Process p2) {
        // If selected process has less time left than the one being compared
        if (p1.getRemainingTime() < p2.getRemainingTime())
            return -1;
            // If selected process has more time left than the one being compared
        else if (p1.getRemainingTime() > p2.getRemainingTime())
            return 1;
        else // If remaining times are the same, prioritize process that has been in system longer
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
    }
}

/* Comparator to implement arrival queue (sort by arrival time) */
class ArrivalTimeComparator implements Comparator<Process>{

    @Override
    public int compare(Process p1, Process p2) {
        // If selected process has less time left than the one being compared
        if (p1.getArrivalTime() < p2.getArrivalTime())
            return -1;
            // If selected process has more time left than the one being compared
        else if (p1.getArrivalTime() > p2.getArrivalTime())
            return 1;
        else // If two processes have same arrival times, prioritize one with least remaining time (burst time)
            return Float.compare(p1.getRemainingTime(), p2.getRemainingTime());
    }
}