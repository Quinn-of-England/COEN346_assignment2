/* COEN 346 Practical Assignment 2
 * Mark Zalass - 40097293
 * Quinn Hogg - 40093086
 */
import java.io.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
        //printFunction(processes, n, exect, quantum);
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
        int arrivalTime;
        int burstTime;
        int waitingTime = 0;
        float remainingTime;
        int timeStart;
        int timeNow;
        volatile Boolean hasCPU = false;
        Boolean isFinished = false;
        float quantum;
        float runTime = 0;

        Process(int arrivalTime, int burstTime) {
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }

        //@Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!hasCPU) {
                    Thread.onSpinWait();
                    System.out.println("Waiting");
                }
                remainingTime -= quantum;
                runTime += quantum;
                if (remainingTime < quantum) {
                    waitingTime = (int) (timeNow - timeStart - runTime + 1);
                    isFinished = true;
                }
            }
        }

        public Boolean getFinished() {
            return isFinished;
        }

        public int getWaitingTime() {
            return waitingTime;
        }

        public float getRunTime() {
            return runTime;
        }

        public void setHasCPU(Boolean hasCPU) {
            this.hasCPU = hasCPU;
        }

        public void setTimeStart(int timeStart) {
            this.timeStart = timeStart;
        }

        public void setTimeNow(int timeNow) {
            this.timeNow = timeNow;
        }

        public void setQuantum(float quantum) {
            this.quantum = quantum;
        }
    }

    class Scheduler implements Runnable {

        Process[] processes;
        int processNum;
        float totalRemainingTime;
        int time = 1;
        float quantum = (float) 1;

        Scheduler(Process[] processes, int processNum) {
            this.processes = processes;
            this.processNum = processNum;

            for (int i = 0; i < processNum; i++) {
                totalRemainingTime += processes[i].burstTime;
            }

            //quantum = totalRemainingTime / 10;
            System.out.println("Quantum: " + quantum);
        }

        //@Override
        public void run() {
            Thread pThread = new Thread(processes[0]);
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
                processes[0].setTimeNow(time);
                processes[0].setHasCPU(true);
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time += 1;
                processes[0].setHasCPU(false);
                pThread.suspend();
                System.out.println("Time " + time + ", Process " + 0 + ", Paused");
                if (processes[0].getFinished()) {
                    System.out.println("Time " + time + ", Process " + 0 + ", Finished");
                    System.out.println("Waiting Time 0: " + processes[0].getWaitingTime());
                    pThread.stop();
                    break;
                }
                // Todo Add round robin scheduler code
            }
        }
    }