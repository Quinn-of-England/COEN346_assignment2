/* COEN 346 Practical Assignment 2
 * Mark Zalass - 40097293
 * Quinn Hogg - 40093086
 */
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.math.RoundingMode;
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
            Process p = new Process(arrivalTime, burstTime);
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

        /* Constructor */
        Process(int arrivalTime, int burstTime) {
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }

        @Override
        public void run() {
            while (true) {
                while (!hasCPU) Thread.onSpinWait();
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

        public int getBurstTime() {
            return burstTime;
        }

        public int getArrivalTime() {
            return arrivalTime;
        }

        public float getTime() {
            return time;
        }

        public float getTimeToRun() {
            return timeToRun;
        }

        public Boolean getHasCPU() {
            return hasCPU;
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

        public void setQuantum(float quantum) {
            this.quantum = quantum;
        }
    }

    class Scheduler implements Runnable {

    /* Variables for scheduler */
        private Process[] processes;
        private int processNum;
        private float totalRemainingTime;
        private float time = 0;
        private float quantum;
        private int runningProcess;

        /* Format outputs to 2 decimal places */
        private static DecimalFormat df = new DecimalFormat("0.00");

        /* Constructor */
        Scheduler(Process[] processes, int processNum) {
            this.processes = processes;
            this.processNum = processNum;

            for (int i = 0; i < processNum; i++) {
                totalRemainingTime += processes[i].getBurstTime();
            }

            /* Calculate quantum as 1/10th the total remaining time */
            quantum = totalRemainingTime / 10;
            System.out.println("Quantum: " + quantum); // Debug statement
        }

        @Override
        public void run() {
            runningProcess = 0;
            Thread pThread = new Thread(processes[runningProcess]);
            runProcess(pThread);
        }

        void runProcess(Thread pThread) {
            /* Code for when process is first run TODO move somewhere else */
            // Time that process starts sent to the process
            processes[runningProcess].setTimeStart(time);
            // Quantum time sent to the process
            processes[runningProcess].setQuantum(quantum);

            // Process thread starts, will busy wait until given CPU access
            pThread.start();
            System.out.println("Time " + df.format(time) + ", Process " + (runningProcess + 1) + ", Started");
            while (true) {
                // Process thread resumes
                pThread.resume();

                // Process' time (clock) updated with current time
                processes[runningProcess].setTime(time);
                System.out.println("Time " + df.format(time) + ", Process " + (runningProcess + 1) + ", Resumed");

                // Process given CPU access, can now execute it's task
                processes[runningProcess].setHasCPU(true);

                // Until CPU given back to the scheduler, scheduler will wait
                while (processes[runningProcess].getHasCPU()) Thread.onSpinWait();

                // Once CPU given back to the scheduler, scheduler suspends the process
                pThread.suspend();

                // Global time incremented by the time the process ran for
                time += processes[runningProcess].getTimeToRun();
                System.out.println("Time " + df.format(time) + ", Process " + (runningProcess + 1) + ", Paused");

                // If the process reports to the scheduler that it has finished it's execution
                if (processes[runningProcess].getFinished()) {
                    System.out.println("Time " + df.format(time) + ", Process " + (runningProcess + 1) + ", Finished");
                    System.out.println("Waiting Time, Process " + (runningProcess + 1) + ", " + df.format(processes[runningProcess].getWaitingTime()));
                    pThread.stop(); // Process stopped
                    break; // While loop breaks, process execution is finished
                }
                // Todo Add round robin scheduler code
            }
        }
    }