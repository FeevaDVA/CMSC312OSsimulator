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
    private boolean test = false;
    private boolean updated = false;
    private boolean isInterrupted = false;
    private Threads thread1, thread2, thread3, thread4, thread5, thread6, thread7, thread8;

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
        thread8 = new Threads();
    }

    public void setExit(boolean e){ exit = e;}
    @Override
    public void run() {
        while (true) {
            while(!exit) {
                for (int i = 0; i < list.getList().size(); i++) {
                    if(isInterrupted){
                        isInterrupted = false;
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    ProcessList.Process p = list.getList().get(i);
                    updateProcesses(p, i);
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

    public void updateProcesses(ProcessList.Process p, int i){
        String state = p.getState();
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
                if(t.getTaskName().equals("I/OInterrupt")){
                    isInterrupted = true;
                    p.nextTask();
                    break;
                } else if (t.getTaskName().equals("FORK")){
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
                }

                if(p.isParent()){
                    p.updateState("WAITING");
                    break;
                }

                if (t.getTaskName().equals("I/O")) {
                    p.updateState("WAITING");
                } else if (runningProc < 8) {
                    runningProc += 1;
                    assignThread(p);
                    p.updateState("RUNNING");
                } else {
                    p.updateState("READY");
                }
            }
            case "RUNNING" -> {
                System.out.println(p.getThread());
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

    public void assignThread(ProcessList.Process p){
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
        } else if(!thread8.isAlive()) {
            p.setThread(7);
            thread8 = new Threads(list, p, quantum, this);
            thread8.start();
        }
    }

    public void setSemaphore(int i){ semaphore = i;}
    public void setRunningProc(int i){ runningProc = i;}
    public int getSemaphore(){ return semaphore; }
    public int getRunningProc(){ return runningProc; }
    public ProcessList getPCB(){ return list; }
}
