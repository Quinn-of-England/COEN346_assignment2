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
        private int arrivalTime;
        private int burstTime;
        private float waitingTime = 0;
        private float remainingTime;
        private float timeStart;
        private float timeNow;
        private volatile Boolean hasCPU = false;
        private Boolean isFinished = false;
        private float quantum;
        private float runTime = 0;

        Process(int arrivalTime, int burstTime) {
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }

        @Override
        public void run() {
            while (true) {
                while (!hasCPU) Thread.onSpinWait();
                remainingTime -= quantum;
                runTime += quantum;
                hasCPU = false;
                if (remainingTime <= 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    waitingTime = (timeNow - runTime - timeStart);
                    isFinished = true;
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

        public void setHasCPU(Boolean hasCPU) {
            this.hasCPU = hasCPU;
        }

        public void setTimeStart(float timeStart) {
            this.timeStart = timeStart;
        }

        public void setTimeNow(float timeNow) {
            this.timeNow = timeNow;
        }

        public void setQuantum(float quantum) {
            this.quantum = quantum;
        }
    }

    class Scheduler implements Runnable {

        private Process[] processes;
        private int processNum;
        private float totalRemainingTime;
        private float time = 0;
        private float quantum;

        private static DecimalFormat df = new DecimalFormat("0.00");

        Scheduler(Process[] processes, int processNum) {
            this.processes = processes;
            this.processNum = processNum;

            for (int i = 0; i < processNum; i++) {
                totalRemainingTime += processes[i].getBurstTime();
            }

            quantum = totalRemainingTime / 10;
            System.out.println("Quantum: " + quantum);
        }

        @Override
        public void run() {
            Thread pThread = new Thread(processes[0]);
            time += 5.12;
            runProcess(pThread);
        }

        void runProcess(Thread pThread) {
            processes[0].setTimeStart(time);
            pThread.start();
            System.out.println("Time " + time + ", Process " + 0 + ", Started");
            while (true) {
                pThread.resume();
                System.out.println("Time " + time + ", Process " + 0 + ", Resumed");
                processes[0].setQuantum(quantum);
                processes[0].setHasCPU(true);
                time += quantum;
                processes[0].setTimeNow(time);
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pThread.suspend();
                System.out.println("Time " + df.format(time) + ", Process " + 0 + ", Paused");
                if (processes[0].getFinished()) {
                    System.out.println("Time " + df.format(time) + ", Process " + 0 + ", Finished");
                    System.out.println("Waiting Time, Process " + 0 + ", " + df.format(processes[0].getWaitingTime()));
                    pThread.stop();
                    break;
                }
                // Todo Add round robin scheduler code
            }
        }
    }