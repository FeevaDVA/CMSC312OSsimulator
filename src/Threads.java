import java.util.Date;
import java.util.Random;

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
    //main run method for thread
    //updates the given process with a 1 in 10 chance to have a forced io interrupt
    public void run() {
        Random r = new Random();
        int n = r.nextInt(11);
        if(n == 5){
            proc.updateState("WAITING");
            for(int i = 0; i<30; i++){
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            proc.updateState("RUNNING");
        }
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
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (proc.getTotalCycles() > 0 ) {
            proc.updateState("READY");
        }
        proc.setRunning(false);
        scheduler.setRunningProc(scheduler.getRunningProc() - 1);
    }
}
