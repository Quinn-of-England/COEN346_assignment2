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

    public static void main(String[] args) {
        if (args.length != 1) { // Checks if arg is present, will exit if not correct (file name/path passed from args[0])
            System.err.println("You must pass the input file name");
            System.exit(0);
        }

        String fileName = args[0];
        ArrayList<String> inProcesses = readInput(fileName);
        //System.out.println(inProcesses); // Debug statement, remove when done

        int processNum = inProcesses.size();
        //System.out.println(processNum); // Debug statement, remove when done
        Process[] processes = new Process[processNum];
        for (int i = 0; i < processNum; i++) {
            String[] split = inProcesses.get(i).split(" ");
            int arrivalTime = Integer.parseInt(split[0]);
            int burstTime = Integer.parseInt(split[1]);
            Process p = new Process(arrivalTime, burstTime, i);
            processes[i] = p;
        }

        Scheduler taskScheduler = new Scheduler(processes, processNum);
        Thread schedulerThread = new Thread(taskScheduler);
        schedulerThread.start();
    }

    public static ArrayList<String> readInput(String fileName) {
        ArrayList<String> inLine = new ArrayList<>();
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String readProcess;
            while ((readProcess = br.readLine()) != null) {
                inLine.add(readProcess);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inLine;
    }
}

    class Process implements Runnable {
    /* Variables for process*/
        private int arrivalTime;
        private int burstTime;
        private float waitingTime = 0;
        private float remainingTime;
        private float timeStart;
        private float time;
        private volatile Boolean hasCPU = false;
        private Boolean isFinished = false;
        private float quantum;
        private float runTime = 0;
        private float timeToRun;
        private Boolean hasRun = false;
        private int processNum;
        private Boolean hasBeenAddedToQueue = false;

        /* Constructor */
        Process(int arrivalTime, int burstTime, int processNum) {
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            quantum = (float) 0.10 * burstTime;
            this.processNum = processNum;
        }

        @Override
        public void run() {
            while (true) {
                while (!hasCPU || isFinished) Thread.onSpinWait();
                timeToRun = Math.min(remainingTime, quantum);
                time += timeToRun; // Time increments
                remainingTime -= timeToRun; // Time ran deducted from time remaining for the process to execute
                runTime += timeToRun; // Time to run (time process ran in quantum) added to total time ran

                hasCPU = false; // Process relinquishes CPU back to the scheduler

                if (remainingTime <= 0) { // If the process is done
                    waitingTime = (time - runTime - timeStart); // Waiting time calculated
                    isFinished = true; // Process signals to scheduler that it is done execution
                }
            }
        }

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
        public Boolean getHasBeenAddedToQueue() {
            return hasBeenAddedToQueue;
        }

        public void setHasCPU(Boolean hasCPU) {
            this.hasCPU = hasCPU;
        }
        public void setTimeStart(float timeStart) {
            this.timeStart = timeStart;
        }
        public void setTime(float time) {
            this.time = time;
        }
        public void setHasRun(Boolean hasRun) {
            this.hasRun = hasRun;
        }
        public void setHasBeenAddedToQueue(Boolean hasBeenAddedToQueue) {
            this.hasBeenAddedToQueue = hasBeenAddedToQueue;
        }
    }

    class Scheduler implements Runnable {

    /* Variables for scheduler */
        private Process[] processes;
        private int numProcesses;
        private float time = 0;
        private int runningProcess;
        private PriorityQueue<Process> readyQueue;
        private Comparator<Process> remainingTimeCompare = new RemainingTimeComparator();
        private float[] waitTimes;
        private Boolean[] processFinished;

        /* Format outputs to 2 decimal places */
        private static DecimalFormat df = new DecimalFormat("0.00");

        /* Constructor */
        Scheduler(Process[] processes, int numProcesses) {
            this.processes = processes;
            this.numProcesses = numProcesses;
            readyQueue = new PriorityQueue<>(numProcesses, remainingTimeCompare);
            waitTimes = new float[numProcesses];
            processFinished = new Boolean[numProcesses];
            Arrays.fill(processFinished, false);
        }

        @Override
        public void run() {
            // Todo Add function that selects process to run

            // Todo Add code for when they are all done (for loop to check if all processDone are true)

            // Testing Priority Queue, making sure that smallest one is always in front
            readyQueue.add(processes[0]);
            readyQueue.add(processes[1]);
            readyQueue.add(processes[2]);

            for (Process item: readyQueue) {
                System.out.println("Process " + item.getProcessNum() + ", Time Remaining " + item.getRemainingTime());
            }

            readyQueue.remove(processes[2]);
            System.out.println("");
            for (Process item: readyQueue) {
                System.out.println("Process " + item.getProcessNum() + ", Time Remaining " + item.getRemainingTime());
            }

            readyQueue.remove(processes[1]);
            System.out.println("");
            for (Process item: readyQueue) {
                System.out.println("Process " + item.getProcessNum() + ", Time Remaining " + item.getRemainingTime());
            }

        }

        void addToQueueWhenArrivalTimeReached (Process[] processes) {
            for (int i = 0; i < numProcesses; i++) {
                if (processes[i].getArrivalTime() >= time && !processes[i].getHasBeenAddedToQueue()) {
                    processes[i].setHasBeenAddedToQueue(true);
                    readyQueue.add(processes[i]);
                }
            }
        }

        void runProcess(Process process) {
            if (!process.getHasRun()) {
                // Time that process starts sent to the process
                process.setTimeStart(time);
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
            System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Resumed");

            // Process given CPU access, can now execute it's task
            process.setHasCPU(true);

            // Until CPU given back to the scheduler, scheduler will wait
            while (process.getHasCPU()) Thread.onSpinWait();

            // Once CPU given back to the scheduler, scheduler suspends the process
            pThread.suspend();

            // Global time incremented by the time the process ran for
            time += process.getTimeToRun();
            System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Paused");

            // If the process reports to the scheduler that it has finished it's execution
            if (process.getFinished()) {
                System.out.println("Time " + df.format(time) + ", Process " + (process.getProcessNum() + 1) + ", Finished");
                waitTimes[process.getProcessNum()] = process.getWaitingTime();
                processFinished[process.getProcessNum()] = true;
            }
            pThread.stop(); // Process stopped
        }
    }

class RemainingTimeComparator implements Comparator<Process>{

    @Override
    public int compare(Process p1, Process p2) {
        // If selected process has less time left than the one being compared
        if (p1.getRemainingTime() < p2.getRemainingTime())
            return -1;
        // If selected process has more time left than the one being compared
        else if (p1.getRemainingTime() > p2.getRemainingTime())
            return 1;
        return 0; // If same times
    }
}
