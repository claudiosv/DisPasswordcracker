package benchmark;
public class TestThread extends Thread {
    private volatile boolean exit = false;
    @Override
    public void run() {
        while(!exit){/*WAIT*/}
    }

    public void halt(){
        exit = true;
    }
}
