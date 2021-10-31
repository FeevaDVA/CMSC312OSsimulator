import java.util.Date;
public class Scheduler extends Thread {
    private static final int quantum = 10;
    private final ProcessList list;
    private final mainGUI main;
    private int cycleCount = 0;
    private int runningProc = 0;
    private int semaphore = 0;
    private int procCrit = 0;
    private volatile boolean exit = true;

    public Scheduler(ProcessList l, mainGUI m) {
        list = l;
        main = m;
    }

    public void setExit(boolean e){ exit = e;}
    @Override
    public void run() {
        while (true) {
            while(!exit) {
                System.out.println(procCrit);
                for (int i = 0; i < list.getList().size(); i++) {
                    ProcessList.Process p = list.getList().get(i);
                    String state = p.getState();
                    switch (state) {
                        case "NEW" -> {
                            p.updateState("READY");
                            Date date = new Date();
                            p.setArrivalTime(date.getTime());
                        }
                        case "READY" -> {
                            ProcessList.Task t = p.getCurrentTask();
                            if (t.getTaskName().equals("P")) {
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
                            list.getTerminatedList().add(p);
                            list.getList().remove(i);
                        }
                    }
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
}
