import java.util.Date;

public class Threads extends Thread{
    private ProcessList.Process proc;
    private int cycles;
    private ProcessList list;
    private Scheduler scheduler;

    public Threads(){
        list = null;
        proc = null;
        cycles = 0;
        scheduler = null;
    }

    public Threads(ProcessList l, ProcessList.Process p, int c, Scheduler s){
        list = l;
        proc = p;
        cycles = c;
        scheduler = s;
    }

    public void run() {
        ProcessList.Task t = proc.getCurrentTask();
        for (int i = 0; i <= cycles; i++) {
            if(t.getTaskName().equals("P") || t.getTaskName().equals("V") || t.getTaskName().equals("FORK")){
                proc.updateState("READY");
                break;
            }
            t.setTime(t.getTime() - i);
            int taskTime = t.getTime();
            if (taskTime == 0) {
                if (!proc.nextTask()) {
                    proc.updateState("TERMINATED");
                    Date date = new Date();
                    proc.setCompletionTime(date.getTime());
                    break;
                } else {
                    t = proc.getCurrentTask();
                }
            } else if(taskTime < 0){
                if (!proc.nextTask()) {
                    proc.updateState("TERMINATED");
                    Date date = new Date();
                    proc.setCompletionTime(date.getTime());
                    break;
                } else {
                    t = proc.getCurrentTask();
                }
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (proc.getTotalCycles() > 0 ) {
            proc.updateState("READY");
        }
        scheduler.setRunningProc(scheduler.getRunningProc() - 1);
    }
}
