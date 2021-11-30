import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class mainGUI extends JFrame{
    private JPanel panel1;
    private JButton addButton;
    private JTextField a0TextField;
    private JTextField a0TextField1;
    private JButton startButton;
    private JList ReadyQ;
    private JList All;
    private JList WaitingQ;
    private JScrollPane scollpane1;
    private JScrollPane scrollpane2;
    private JScrollPane scrollpane3;
    private JLabel turnAroundLabel;
    private JLabel waitTimeLabel;
    private JTabbedPane tabbedPane1;
    private JLabel memLabel;
    private JList ready2List;
    private JList waiting2List;
    private JList all2List;
    private JLabel turnaround2label;
    private JLabel waitlabel2;
    private JLabel memlabel2;
    private Scheduler s;
    private Scheduler2 n;
    private SchedulerThread2 m;

    private mainGUI main;
    private ProcessList pcb, pcb2;
    public mainGUI(){
        setContentPane(panel1);
        setTitle("OS Simulator");
        setSize(1000, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        main = this;
        a0TextField.setPreferredSize(new Dimension(30, 20));
        a0TextField1.setPreferredSize(new Dimension(30, 20));
        a0TextField1.updateUI();
        a0TextField.updateUI();
        pcb = new ProcessList();
        s = new Scheduler(pcb, main);
        m = new SchedulerThread2(pcb, main, s);
        s.setScheduler(m);
        pcb2 = new ProcessList();
        n = new Scheduler2(pcb2, main);
        s.start();
        n.start();


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(startButton.getText().equals("Start")) {
                    startButton.setText("Stop");
                    s.setExit(false);
                    m.setExit(false);
                    n.setExit(false);
                } else {
                    startButton.setText("Start");
                    s.setExit(true);
                    m.setExit(true);
                    n.setExit(true);
                }
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    pcb.generateProcesses(getNumberProcess(), getTempNumber());
                    pcb2.generateProcesses(getNumberProcess(), getTempNumber());
                    updateList();
                    updateList2();
                    System.out.println("this");
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        DefaultListModel allModel = new DefaultListModel();
        allModel.addElement("Process #:| State | Remaining Cycles | Total Mem | Turn Around Time (seconds)");
        All.setModel(allModel);
        all2List.setModel(allModel);
    }

    public JButton getStartButton(){
        return startButton;
    }
    public static void main(String[] args){
        mainGUI myFrame = new mainGUI();
    }
    private int getTempNumber(){
        return Integer.parseInt(a0TextField1.getText());
    }
    private int getNumberProcess(){
        return Integer.parseInt(a0TextField.getText());
    }

    public void updateList(){
        DefaultListModel allModel = new DefaultListModel();
        DefaultListModel readyModel = new DefaultListModel();
        DefaultListModel waitModel = new DefaultListModel();

        allModel.addElement("Process #:| State | Remaining Cycles | Total Mem | Turn Around Time (seconds)");
        for(int i = 0; i<pcb.getList().size(); i++){
            ProcessList.Process p = pcb.getList().get(i);
            if(p.getState().equals("WAITING")){
                waitModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
            } else if(p.getState().equals("READY")){
                readyModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
            } else {
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
            }
        }
        double sum = 0;
        double sum2 = 0;
        NumberFormat format = new DecimalFormat("#0.000");
        for(int i = 0; i<pcb.getTerminatedList().size(); i++) {
            ProcessList.Process p = pcb.getTerminatedList().get(i);
            sum += p.getTurnAroundTime();
            sum2 += p.getWaitTime();
            allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + "0 "+ format.format(p.getTurnAroundTime()/1000.0));
        }

        if(pcb.getTerminatedList().size() != 0) {
            sum /= pcb.getTerminatedList().size();
            sum2 /= pcb.getTerminatedList().size();
        }
        turnAroundLabel.setText("Average Turn Around: " + format.format(sum/1000.0));
        waitTimeLabel.setText("Average Wait Time: " + format.format(sum2/1000.0));
        memLabel.setText("Mem: " + pcb.getMemory());
        ReadyQ.setModel(readyModel);
        WaitingQ.setModel(waitModel);
        All.setModel(allModel);
    }

    public void updateList2(){
        DefaultListModel allModel = new DefaultListModel();
        DefaultListModel readyModel = new DefaultListModel();
        DefaultListModel waitModel = new DefaultListModel();

        allModel.addElement("Process #:| State | Remaining Cycles | Total Mem | Turn Around Time (seconds)");
        int n = pcb2.getList().size();
        for(int i = 0; i<n; i++){
            ProcessList.Process p = pcb2.getList().get(i);
            if(p.getState().equals("WAITING")){
                waitModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
            } else if(p.getState().equals("READY")){
                readyModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
            } else {
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTotalMem());
            }
        }

        n = pcb2.getTerminatedList().size();
        double sum = 0;
        double sum2 = 0;
        NumberFormat format = new DecimalFormat("#0.000");
        for(int i = 0; i<n; i++) {
            ProcessList.Process p = pcb2.getTerminatedList().get(i);
            sum += p.getTurnAroundTime();
            sum2 += p.getWaitTime();
            allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + "0 "+ format.format(p.getTurnAroundTime()/1000.0));
        }
        if(n != 0) {
            sum /= n;
            sum2 /= n;
        }
        turnaround2label.setText("| " + format.format(sum/1000.0));
        waitlabel2.setText("| " + format.format(sum2/1000.0));
        memlabel2.setText("| " + pcb2.getMemory());
        ready2List.setModel(readyModel);
        waiting2List.setModel(waitModel);
        all2List.setModel(allModel);
    }
}
