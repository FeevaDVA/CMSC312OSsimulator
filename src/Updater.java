public class Updater extends Thread{
    mainGUI main;
    Updater(mainGUI m){
        super();
        main = m;
    }

    public void run(){
        main.updateList();
    }
}
