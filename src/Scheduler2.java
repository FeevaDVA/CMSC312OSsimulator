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
                            if (t.getTaskName().equals("FORK")){
                                p.setParent(true);
                                p.nextTask();
                                ProcessList.Process child = new ProcessList.Process(p, p.getTaskPos());
                                list.addProcess(child, 0);
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
                                break;
                            } else if(t.getTaskName().equals("m1")){
                                if(1<list.getList().size()) {
                                    list.getList().get(1).setResource(p.getResource());
                                }
                            } else if(t.getTaskName().equals("m2")){
                                if(list.getList().size() - 1 > 0) {
                                    list.getList().get(list.getList().size() - 1).getResource().setMessage("Hello");
                                }
                            }

                            if (p.isParent()) {
                                p.updateState("WAITING");
                                break;
                            }

                            if (t.getTaskName().equals("I/O")) {
                                p.updateState("WAITING");
                            } else  {
                                p.updateState("RUNNING");
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

