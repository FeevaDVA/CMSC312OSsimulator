import java.util.Date;

public class SchedulerThread2 extends Thread {
    private static final int quantum = 20;
    private final ProcessList list;
    private final mainGUI main;
    private int cycleCount = 0;
    private int runningProc = 0;
    private int semaphore = 0;
    private int procCrit = 0;
    private volatile boolean exit = true;
    private boolean test = false;
    private Scheduler scheduler;

    public SchedulerThread2(ProcessList l, mainGUI m, Scheduler s) {
        list = l;
        main = m;
        scheduler = s;
    }

    public void setExit(boolean e){ exit = e;}
    @Override
    public void run() {
        while (true) {
            while(!exit) {
                for (int i = list.getList().size()-1; i >= 0; i--) {
                    System.out.println(i);
                    ProcessList.Process p = list.getList().get(i);
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
                                if (scheduler.getSemaphore() == 1) {
                                    p.updateState("WAITING");
                                    break;
                                } else {
                                    p.nextTask();
                                    procCrit = i;
                                    scheduler.setSemaphore(1);
                                }
                            } else if (t.getTaskName().equals("V")) {
                                scheduler.setSemaphore(0);
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
                                if (scheduler.getSemaphore() == 1) {
                                    p.updateState("WAITING");
                                    break;
                                } else {
                                    p.nextTask();
                                    procCrit = i;
                                    scheduler.setSemaphore(1);
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
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
