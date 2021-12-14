import java.util.Date;

public class FirstComeThread extends Thread{
    private ProcessList list;
    private final mainGUI main;
    private int cycleCount = 0;
    private int runningProc = 0;
    private int semaphore = 0;
    private int procCrit = 0;
    private volatile boolean exit = true;

    public FirstComeThread(ProcessList l, mainGUI m) {
        list = l;
        main = m;
    }

    public void setExit(boolean e) {
        exit = e;
    }

    public void run() {
        while (true) {
            while (!exit) {
                if (list.getFirstCome().size() > 0) {
                    ProcessList.Process p = list.getFirstProcess(0);
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
                            System.out.println(t.getTaskName());
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
                                if(1<list.getFirstCome().size()) {
                                    list.getFirstCome().get(1).setResource(p.getResource());
                                }
                            } else if(t.getTaskName().equals("m2")){
                                if(list.getFirstCome().size() - 1 > 0) {
                                    list.getFirstCome().get(list.getFirstCome().size() - 1).getResource().setMessage("Hello");
                                }
                            }
                            if (p.isParent()) {
                                p.updateState("WAITING");
                                break;
                            }

                            if (t.getTaskName().equals("I/O")) {
                                p.updateState("WAITING");
                            } else {
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
                            int timeLeft = t.getTime() - 1;
                            if (timeLeft == 0) {
                                cycleCount += 1;
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
                                    cycleCount += 1;
                                } else {
                                    p.updateState("TERMINATED");
                                    cycleCount -= timeLeft;
                                    Date date = new Date();
                                    p.setCompletionTime(date.getTime());
                                }
                            } else {
                                t.setTime(timeLeft);
                                p.updateState("WAITING");
                                cycleCount += 1;
                            }

                        }
                        case "RUNNING" -> {
                            ProcessList.Task t = p.getCurrentTask();
                            int timeLeft = t.getTime() - 1;
                            if (timeLeft == 0) {
                                cycleCount += 1;
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
                                    cycleCount += 1;
                                } else {
                                    p.updateState("TERMINATED");
                                    cycleCount -= timeLeft;
                                    Date date = new Date();
                                    p.setCompletionTime(date.getTime());
                                }
                            } else {
                                t.setTime(timeLeft);
                                p.updateState("READY");
                                cycleCount += 1;
                            }
                            runningProc = 0;
                        }
                        case "TERMINATED" -> {
                            list.getFirstCome().remove(0);
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
