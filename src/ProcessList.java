import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProcessList{
    private int countProcess;
    public class Task{
        private String taskName;
        private int time;
        private int memory;

        public Task(String name, int t, int m){
            taskName = name;
            time = t;
            memory = m;
        }

        public int getTime(){
            return time;
        }
        public void setTime(int t) { time = t; }

        public int getMemory(){
            return memory;
        }

        public String getTaskName(){
            return taskName;
        }
    }
    public class Process{
        private int number, arrivalTime, completionTime, burstTime;
        private String state;
        private List<Task> taskList;
        private int currentTask;

        public Process(int tempNum, int num) throws FileNotFoundException {
            number = num + 1000;
            taskList = new ArrayList<Task>();
            generateTasks(tempNum);
            state = "NEW";
            currentTask = 0;
            arrivalTime = 0;
            completionTime = 0;
            burstTime = getTotalCycles();
        }

        private void generateTasks(int tempNum) throws FileNotFoundException {
            Path curRelPath = Paths.get("");
            String s = curRelPath.toAbsolutePath().toString();
            File temp = new File(s + "\\Process Templates\\Template " + tempNum + ".txt");
            Scanner scanner = new Scanner(temp);

            while(scanner.hasNextLine()){

                String nam = scanner.next();
                if(nam.equals("P") || nam.equals("V")){
                    taskList.add(new Task(nam, 0, 0));
                } else {
                    int n = scanner.nextInt(), m = scanner.nextInt();
                    int i = scanner.nextInt(), j = scanner.nextInt();
                    taskList.add(new Task(nam, (int) (Math.random() * m) + n, (int) (Math.random() * j) + i));
                }
            }
        }
        public void setArrivalTime(int n){
            arrivalTime = n;
        }
        public void setCompletionTime(int n){
            completionTime = n;
        }
        public int getArrivalTime(){
            return arrivalTime;
        }
        public int getCompletionTime(){
            return completionTime;
        }
        public int getTurnAroundTime(){
            return completionTime - arrivalTime;
        }
        public int getWaitTime(){ return getTurnAroundTime() - burstTime; }
        public int getTotalMem(){
            int tot = 0;
            for(int i = 0; i<taskList.size(); i++){
                tot += taskList.get(i).getTime();
            }
            return tot;
        }

        public int getTotalCycles(){
            int tot = 0;
            for(int i = currentTask; i<taskList.size(); i++){
                tot += taskList.get(i).getTime();
            }
            return tot;
        }

        public Task getCurrentTask(){
            return taskList.get(currentTask);
        }

        public boolean nextTask(){
            currentTask++;
            if (currentTask>=taskList.size())
                return false;
            else
                return true;
        }

        public void updateState(String newState){
            state = newState;
        }

        public String getState(){
            return state;
        }

        public int getNumber(){
            return number;
        }

    }
    private List<Process> processlist;
    private List<Process> terminatedList;

    public ProcessList(){
        countProcess = 0;
        processlist = new ArrayList<Process>();
        terminatedList = new ArrayList<Process>();
    }

    public void generateProcesses(int count, int temp) throws FileNotFoundException {
        for(int i = 0; i<count; i++){
            Process p = new Process(temp, countProcess++);
            processlist.add(p);
        }
    }

    public List<Process> getList(){
        return processlist;
    }

    public List<Process> getTerminatedList(){
        return terminatedList;
    }

    public Process getProcess(int n){
        return processlist.get(n);
    }

    public void removeProcess(int n){
        getProcess(n).updateState("TERMINATED");
    }

}