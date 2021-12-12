import java.util.Date;
public class Scheduler extends Thread {
    private static final int quantum = 20;
    private final ProcessList list;
    private final mainGUI main;
    private int cycleCount = 0;
    private int runningProc = 0;
    private int semaphore = 0;
    private int procCrit = 0;
    private volatile boolean exit = true;
    private Threads thread1, thread2, thread3, thread4, thread5, thread6, thread7;
    private FirstComeThread thread8;

    public Scheduler(ProcessList l, mainGUI m) {
        list = l;
        main = m;
        thread1 = new Threads();
        thread2 = new Threads();
        thread3 = new Threads();
        thread4 = new Threads();
        thread5 = new Threads();
        thread6 = new Threads();
        thread7 = new Threads();
        thread8 = new FirstComeThread(l, m);
        thread8.start();
    }

    public void setExit(boolean e){
        exit = e;
        thread8.setExit(e);
    }
    @Override

    //main run loop
    public void run() {
        while (true) {
            while(!exit) {
                for (int i = 0; i < list.getList().size(); i++) {
                    ProcessList.Process p = list.getList().get(i);
                    if(p.getTotalCycles()<2000)
                        updateProcesses(p, i);
                    else if(!p.isAdded())
                        list.addFirstCome(p);
                    main.updateList();
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //updates the given process
    public void updateProcesses(ProcessList.Process p, int i){
        String state = p.getState();
        if (p.isRunning()){
            return;
        }
        switch (state) {
            case "NEW" -> {
                if(list.getMemory() - p.getTotalMem() > 0) {
                    list.setMemory(list.getMemory() - p.getTotalMem());
                    p.updateState("READY");
                    Date date = new Date();
                    p.setArrivalTime(date.getTime());
                }
            }
            case "READY" -> {
                ProcessList.Task t = p.getCurrentTask();
                if (t.getTaskName().equals("FORK")){
                    p.setParent(true);
                    p.nextTask();
                    ProcessList.Process child = new ProcessList.Process(p, p.getTaskPos());
                    list.addProcess(child, i);
                } else if (t.getTaskName().equals("P")) {
                    if (semaphore == 1) {
                        p.updateState("WAITING");
                        break;
                    } else {
                        p.nextTask();
                        procCrit = i;
                        semaphore = 1;
                    }
                } else if (t.getTaskName().equals("V")) {
                    semaphore = 0;
                    p.nextTask();
                    break;
                } else if(t.getTaskName().equals("m1")){
                    if(i+1<list.getList().size()) {
                        ProcessList.Task temp = list.getList().get(i + 1).getCurrentTask();
                        temp.setTime(temp.getTime() + 30);
                    }
                } else if(t.getTaskName().equals("m2")){
                    if(list.getList().size()-1>0) {
                        ProcessList.Task temp = list.getList().get(list.getList().size() - 1).getCurrentTask();
                        temp.setTime(temp.getTime() + 100);
                    }
                }

                if(p.isParent()){
                    p.updateState("WAITING");
                    break;
                }

                if (t.getTaskName().equals("I/O")) {
                    p.updateState("WAITING");
                } else if (runningProc < 7) {
                    runningProc += 1;
                    assignThread(p);
                    p.updateState("RUNNING");
                } else {
                    p.updateState("READY");
                }
            }
            case "WAITING" -> {
                ProcessList.Task t = p.getCurrentTask();
                if(p.isParent()){
                    p.updateState("WAITING");
                    break;
                }
                if (t.getTaskName().equals("P")) {
                    if (semaphore == 1) {
                        p.updateState("WAITING");
                        break;
                    } else {
                        p.nextTask();
                        procCrit = i;
                        semaphore = 1;
                    }
                }
                int timeLeft = t.getTime() - quantum;
                if (timeLeft == 0) {
                    cycleCount += quantum;
                    if (!p.nextTask()) {
                        p.updateState("TERMINATED");
                        Date date = new Date();
                        p.setCompletionTime(date.getTime());
                    } else {
                        p.updateState("READY");
                    }
                } else if (timeLeft < 0) {
                    if (p.nextTask()) {
                        t = p.getCurrentTask();
                        t.setTime(t.getTime() + timeLeft);
                        p.updateState("READY");
                        cycleCount += quantum;
                    } else {
                        p.updateState("TERMINATED");
                        cycleCount -= timeLeft;
                        Date date = new Date();
                        p.setCompletionTime(date.getTime());
                    }
                } else {
                    t.setTime(timeLeft);
                    p.updateState("WAITING");
                    cycleCount += quantum;
                }

            }
            case "TERMINATED" -> {
                if(p.isChild()){
                    p.getParent().setParent(false);
                }
                list.setMemory(list.getMemory() + p.getTotalMem());
                list.getTerminatedList().add(p);
                list.getList().remove(i);
            }
        }
    }

    //assigns the process to a thread that is not running one already
    public void assignThread(ProcessList.Process p){
        p.setRunning(true);
        if(!thread1.isAlive()){
            p.setThread(0);
            thread1 = new Threads(list, p, quantum, this);
            thread1.start();
        } else if(!thread2.isAlive()){
            p.setThread(1);
            thread2 = new Threads(list, p, quantum, this);
            thread2.start();
        } else if(!thread3.isAlive()) {
            p.setThread(2);
            thread3 = new Threads(list, p, quantum, this);
            thread3.start();
        } else if(!thread4.isAlive()) {
            p.setThread(3);
            thread4 = new Threads(list, p, quantum, this);
            thread4.start();
        } else if(!thread5.isAlive()) {
            p.setThread(4);
            thread5 = new Threads(list, p, quantum, this);
            thread5.start();
        } else if(!thread6.isAlive()) {
            p.setThread(5);
            thread6 = new Threads(list, p, quantum, this);
            thread6.start();
        } else if(!thread7.isAlive()) {
            p.setThread(6);
            thread7 = new Threads(list, p, quantum, this);
            thread7.start();
        }
    }

    public void setSemaphore(int i){ semaphore = i;}
    public void setRunningProc(int i){ runningProc = i;}
    public int getSemaphore(){ return semaphore; }
    public int getRunningProc(){ return runningProc; }
    public ProcessList getPCB(){ return list; }
}
