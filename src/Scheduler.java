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
    private SchedulerThread2 scheduler;

    public Scheduler(ProcessList l, mainGUI m) {
        list = l;
        main = m;
    }

    public void setExit(boolean e){ exit = e;}
    @Override
    public void run() {
        while (true) {
            while(!exit) {
                for (int i = 0; i < list.getList().size()/2; i++) {
                    ProcessList.Process p = list.getList().get(i);
                    updateProcesses(p, i);
                    System.out.println(i);
                    int n = list.getList().size() - i - 1;
                    ProcessList.Process b = list.getList().get(n);
                    updateProcesses(b, n);

                    main.updateList();
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(list.getList().size() == 1)
                    updateProcesses(list.getList().get(0), 0);
                main.updateList();
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
                if (t.getTaskName().equals("FORK")){
                    p.setParent(true);
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
                }

                if(p.isParent()){
                    p.updateState("WAITING");
                    break;
                }

                if (t.getTaskName().equals("I/O")) {
                    p.updateState("WAITING");
                } else if (runningProc == 0) {
                    runningProc = 1;
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
            case "RUNNING" -> {
                ProcessList.Task t = p.getCurrentTask();
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
                    p.updateState("READY");
                    cycleCount += quantum;
                }
                runningProc = 0;
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

    public void setScheduler(SchedulerThread2 s){ scheduler = s; }
    public void setSemaphore(int i){ semaphore = i;}
    public void setRunningProc(int i){ runningProc = i;}
    public int getSemaphore(){ return semaphore; }
    public int getRunningProc(){ return runningProc; }
    public boolean updated(){ return updated; }
    public void setUpdated(boolean u){ updated = u; }
    public ProcessList getPCB(){ return list; }
}
