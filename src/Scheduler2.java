import java.util.Date;

public class Scheduler2 extends Thread {
    private ProcessList list;
    private final mainGUI main;
    private int cycleCount = 0;
    private int runningProc = 0;
    private int semaphore = 0;
    private int procCrit = 0;
    private volatile boolean exit = true;

    public Scheduler2(ProcessList l, mainGUI m) {
        list = l;
        main = m;
    }

    public void setExit(boolean e) {
        exit = e;
    }

    public void run() {
        while (true) {
            while (!exit) {
                if (list.getList().size() > 0) {
                    int lowest = 0;
                    for (int i = 0; i < list.getList().size(); i++) {
                        if (list.getList().get(i).getTotalCycles() < list.getList().get(lowest).getTotalCycles() && !list.getList().get(i).isParent())
                            lowest = i;
                    }
                    ProcessList.Process p = list.getProcess(lowest);
                    String state = p.getState();
                    switch (state) {
                        case "NEW" -> {
                            if (list.getMemory() - p.getTotalMem() > 0) {
                                list.setMemory(list.getMemory() - p.getTotalMem());
                                p.updateState("READY");
                                Date date = new Date();
                                p.setArrivalTime(date.getTime());
                            }
                        }
                        case "READY" -> {
                            ProcessList.Task t = p.getCurrentTask();
                            if (t.getTaskName().equals("FORK")) {
                                p.nextTask();
                                p.setParent(true);
                                ProcessList.Process child = new ProcessList.Process(p, p.getTaskPos());
                                list.addProcess(child, lowest);
                            } else if (t.getTaskName().equals("P")) {
                                if (semaphore == 1) {
                                    p.updateState("WAITING");
                                    break;
                                } else {
                                    p.nextTask();
                                    procCrit = 0;
                                    semaphore = 1;
                                }
                            } else if (t.getTaskName().equals("V")) {
                                semaphore = 0;
                                p.nextTask();
                            }

                            if (p.isParent()) {
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
                            if (p.isParent()) {
                                p.updateState("WAITING");
                                break;
                            }
                            if (t.getTaskName().equals("P")) {
                                if (semaphore == 1) {
                                    p.updateState("WAITING");
                                    break;
                                } else {
                                    p.nextTask();
                                    procCrit = 0;
                                    semaphore = 1;
                                }
                            }
                            int timeLeft = t.getTime() - 20;
                            if (timeLeft == 0) {
                                cycleCount += 20;
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
                                    cycleCount += 20;
                                } else {
                                    p.updateState("TERMINATED");
                                    cycleCount -= timeLeft;
                                    Date date = new Date();
                                    p.setCompletionTime(date.getTime());
                                }
                            } else {
                                t.setTime(timeLeft);
                                p.updateState("WAITING");
                                cycleCount += 20;
                            }

                        }
                        case "RUNNING" -> {
                            ProcessList.Task t = p.getCurrentTask();
                            int timeLeft = t.getTime() - 20;
                            if (timeLeft == 0) {
                                cycleCount += 20;
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
                                    cycleCount += 20;
                                } else {
                                    p.updateState("TERMINATED");
                                    cycleCount -= timeLeft;
                                    Date date = new Date();
                                    p.setCompletionTime(date.getTime());
                                }
                            } else {
                                t.setTime(timeLeft);
                                p.updateState("READY");
                                cycleCount += 20;
                            }
                            runningProc = 0;
                        }
                        case "TERMINATED" -> {
                            if (p.isChild()) {
                                p.getParent().setParent(false);
                            }
                            list.setMemory(list.getMemory() + p.getTotalMem());
                            list.getTerminatedList().add(p);
                            list.getList().remove(lowest);
                        }
                    }
                    main.updateList2();
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

