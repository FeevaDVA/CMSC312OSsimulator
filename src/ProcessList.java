import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ProcessList{
    private int countProcess;
    private int memory = 1024;
    private int frames = 240;
    public static class Task{
        private String taskName;
        private int time;
        private int memory;
        //constructor of a task that takes the name, the cycles, and the memory
        public Task(String name, int t, int m){
            taskName = name;
            time = t;
            memory = m;
        }
        //returns the time/cycles of the task
        public int getTime(){
            return time;
        }
        //sets the time/cycles of the task
        public void setTime(int t) { time = t; }
        //returns the memory of a task
        public int getMemory(){
            return memory;
        }
        //returns the name of the task
        public String getTaskName(){
            return taskName;
        }
    }
    public static class Process implements Comparable<Process>{
        private int number, thread, priority, frames;
        private double burstTime;
        private long arrivalTime, completionTime;
        private String state;
        private List<Task> taskList;
        private int currentTask;
        private boolean parent, child, running, added;
        Process parentProc;
        ProcessList list;
        //constructor for the process class that takes the number of processes to make and the template number
        public Process(int tempNum, int num, ProcessList l) throws FileNotFoundException {
            parent = false;
            child = false;
            running = false;
            added = false;
            number = num + 1000;
            priority = (int) (Math.random()*6);
            taskList = new ArrayList<Task>();
            generateTasks(tempNum);
            state = "NEW";
            currentTask = 0;
            arrivalTime = 0;
            completionTime = 0;
            burstTime = getTotalCycles()/10;
            list = l;
        }

        public Process(Process P, int pos){
            parentProc = P;
            added = false;
            parent = false;
            child = true;
            taskList = new ArrayList<Task>();
            priority = parentProc.getPriority();
            List<Task> oTasks = P.getTaskList();
            for(int i = pos+1; i<oTasks.size(); i++){
                Task temp = oTasks.get(i);
                taskList.add(new Task(temp.getTaskName(), temp.getTime(), 0));
            }
            number = P.getNumber() + 1000;
            state = "NEW";
            currentTask = 0;
            arrivalTime = 0;
            completionTime = 0;
            burstTime = getTotalCycles()/10;
            list = parentProc.getList();
        }
        //Generates a process from the given template number if the file is not there it will throw exception, but you can try again.
        private void generateTasks(int tempNum) throws FileNotFoundException {
            Path curRelPath = Paths.get("");
            String s = curRelPath.toAbsolutePath().toString();
            File temp = new File(s + "\\Process Templates\\Template " + tempNum + ".txt");
            Scanner scanner = new Scanner(temp);

            while(scanner.hasNextLine()){

                String nam = scanner.next();
                if(nam.equals("P") || nam.equals("V") || nam.equals("FORK") || nam.equals("m1") || nam.equals("m2")){
                    taskList.add(new Task(nam, 0, 0));
                } else {
                    int n = scanner.nextInt(), m = scanner.nextInt();
                    int i = scanner.nextInt(), j = scanner.nextInt();
                    taskList.add(new Task(nam, (int) (Math.random() * m) + n, (int) (Math.random() * j) + i));
                }
            }
        }
        //sets the arrival time to the given milliseconds long n
        public void setArrivalTime(long n){
            arrivalTime = n;
        }
        //sets the completion time of the process to the given time in milliseconds long n
        public void setCompletionTime(long n){
            completionTime = n;
        }
        //returns the turn around time in milliseconds which should be the completion time - the arrival time
        public long getTurnAroundTime(){
            return completionTime - arrivalTime;
        }
        public long getWaitTime(){ return (long) (getTurnAroundTime() - burstTime); }
        public List<Task> getTaskList() { return taskList; }
        public int getTaskPos(){ return currentTask; }
        public int getThread(){ return thread; }
        public boolean isRunning(){ return running; }
        public void setRunning(boolean r){ running = r; }
        public void setThread(int n) { thread = n; }
        public boolean isChild(){ return child; }
        public boolean isAdded() { return added; }
        public int getFrames(){ return frames; }
        public void setFrames(int f){
            frames = (getTotalMem()/list.getTotalMem())*f;
        }

        public void setAdded(boolean added) {
            this.added = added;
        }

        public ProcessList getList() {
            return list;
        }

        public int compareTo(Process other){
            int res = other.getPriority() - this.getPriority();
            if(res >= 0)
                return -1;
            else
                return 1;
        }
        public int getPriority() {
            return priority;
        }

        public Process getParent(){ return parentProc; }
        public int getTotalMem(){
            int tot = 0;
            for(int i = 0; i<taskList.size(); i++){
                tot += taskList.get(i).getMemory();
            }
            return tot;
        }
        //returns the processes remaining cycles
        public int getTotalCycles(){
            int tot = 0;
            for(int i = currentTask; i<taskList.size(); i++){
                tot += taskList.get(i).getTime();
            }
            return tot;
        }
        //returns the current task that the processes is on
        public Task getCurrentTask(){
            return taskList.get(currentTask);
        }
        // moves to the next task and will return false if there is no next task
        public boolean nextTask(){
            currentTask++;
            if (currentTask>=taskList.size())
                return false;
            else
                return true;
        }
        //Sets the state of the process to given string new state
        public void updateState(String newState){
            state = newState;
        }
        //returns the state of the process
        public String getState(){
            return state;
        }
        //returns the numebr of the process
        public int getNumber(){
            return number;
        }
        public boolean isParent(){
            return parent;
        }
        public void setParent(boolean p){
            parent = p;
        }

    }
    private List<Process> processlist;
    private List<Process> terminatedList;
    private List<Process> firstCome;

    //default constructor of the ProcessList class
    public ProcessList(){
        countProcess = 0;
        processlist = new ArrayList<Process>();
        terminatedList = new ArrayList<Process>();
        firstCome = new ArrayList<Process>();
    }
    //generates a given number count of processes using the given template number of temp
    public void generateProcesses(int count, int temp) throws FileNotFoundException {
        for(int i = 0; i<count; i++){
            Process p = new Process(temp, countProcess++, this);
            processlist.add(p);
        }
    }

    //returns the list of processes
    public List<Process> getList(){
        return processlist;
    }
    public int getFrames(){
        return frames;
    }

    public void setFrames(int n){
        frames = n;
    }

    public List<Process> getFirstCome() {
        return firstCome;
    }
    public void setFrames(){
        for(int i = 0; i<processlist.size(); i++){
            processlist.get(i).setFrames(frames);
        }
    }
    public int getTotalMem() {
        int tot = 0;
        for(int i = 0; i<processlist.size(); i++){
            tot += processlist.get(i).getTotalMem();
        }
        return tot;
    }

    //sets the list for the pcb
    public void setList(List<Process> n){
        for(Process p: n){
            processlist.add(p);
        }
    }
    public void sortPriorityList(){
        Collections.sort(processlist);
    }
    //returns the list of terminated processes
    public List<Process> getTerminatedList(){
        return terminatedList;
    }
    //returns the process at the given int n
    public Process getProcess(int n){
        return processlist.get(n);
    }
    public Process getFirstProcess(int n){
        return firstCome.get(n);
    }
    //removes the process at given int n
    public void removeProcess(int n){
        getProcess(n).updateState("TERMINATED");
    }
    public int getMemory(){return memory;}
    public void setMemory(int n){memory = n;}
    public void addProcess(Process p, int pos){
        processlist.add(pos, p);
    }
    public void addFirstCome(Process p){
        p.setAdded(true);
        firstCome.add(p); }

}