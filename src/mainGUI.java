import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

public class mainGUI extends JFrame{
    private JPanel panel1;
    private JButton addButton;
    private JTextField a0TextField;
    private JTextField a0TextField1;
    private JButton startButton;
    private JList ReadyQ;
    private JList All;
    private JList WaitingQ;
    private JPanel panel2;
    private JScrollPane scollpane1;
    private JScrollPane scrollpane2;
    private JScrollPane scrollpane3;
    private JLabel turnAroundLabel;
    private JLabel waitTimeLabel;
    private Scheduler s;

    private mainGUI main;
    private ProcessList pcb;
    public mainGUI(){
        pcb = new ProcessList();
        setContentPane(panel1);
        setTitle("OS Simulator");
        setSize(800, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        main = this;

        s = new Scheduler(pcb, main);
        s.start();


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(startButton.getText().equals("Start")) {
                    startButton.setText("Stop");
                    s.setExit(false);
                } else {
                    startButton.setText("Start");
                    s.setExit(true);
                }
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    pcb.generateProcesses(getNumberProcess(), getTempNumber());
                    updateList();
                    System.out.println("this");
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
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

        allModel.addElement("Process #:| State | Remaining Cycles | Turn Around Time");
        int n = pcb.getList().size();
        for(int i = 0; i<n; i++){
            ProcessList.Process p = pcb.getList().get(i);
            if(p.getState().equals("WAITING")){
                waitModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
            } else if(p.getState().equals("READY")){
                readyModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
            } else {
                allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles());
            }
        }
        n = pcb.getTerminatedList().size();
        int sum = 0;
        int sum2 = 0;
        for(int i = 0; i<n; i++) {
            ProcessList.Process p = pcb.getTerminatedList().get(i);
            sum += p.getTurnAroundTime();
            sum2 += p.getWaitTime();
            allModel.addElement("Process " + p.getNumber() +": " + p.getState() + " " + p.getTotalCycles() + " " + p.getTurnAroundTime());
        }
        if(n != 0) {
            sum /= n;
            sum2 /= n;
        }

        turnAroundLabel.setText("Average Turn Around: " + sum);
        waitTimeLabel.setText("Average Wait Time: " + sum2);
        ReadyQ.setModel(readyModel);
        WaitingQ.setModel(waitModel);
        All.setModel(allModel);
    }
}
