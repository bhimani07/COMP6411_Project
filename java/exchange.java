import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class exchange extends Thread implements PrintListener {
    private static final long WAIT_TIME_MILLIS = 10 * 1000;

    static HashMap<String, caller> callerThreadList = new HashMap<>();
    static HashMap<String, ArrayList<String>> callerMap = new HashMap<>();
    private BlockingQueue<Printer> printerQueue = new ArrayBlockingQueue<>(1000);

    static void print(String msg) {
        System.out.println(msg);
    }

    public void run() {
        long startTimeMillis = System.currentTimeMillis();
        long currentTimeMillis = System.currentTimeMillis();
        while (currentTimeMillis - startTimeMillis < WAIT_TIME_MILLIS) {
            synchronized (this) {
                Printer printer = printerQueue.poll();
                if (printer != null) {
                    print(printer.message);
                }
                currentTimeMillis = System.currentTimeMillis();
            }
        }
        print("\nMaster has received no replies for 10 seconds, ending...");
    }

    public static void main(String args[]) throws IOException {
        FileReader fileReader = new FileReader(new File("calls.txt"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        ArrayList<String> tempArrayList = new ArrayList<>();
        exchange Master = new exchange();

        while ((line = bufferedReader.readLine()) != null) {
            line = line.replace("{", "").replace("}", "").replace(".", "").replace("[", "").replace("]", "");
            String[] tokens = line.split(",");
            for (int i = 1; i < tokens.length; i++) {
                tempArrayList.add(tokens[i].trim());
            }
            callerMap.put(tokens[0], new ArrayList<>(tempArrayList));
            tempArrayList.clear();
        }

        print("** Calls to be made **");
        callerMap.entrySet().forEach(entry -> {
            print(entry.getKey() + ": " + entry.getValue());
        });
        print("\n");

        callerMap.entrySet().forEach(entry -> {
            caller caller = new caller(entry.getKey());
            caller.addMaster(Master);
            callerThreadList.put(entry.getKey(), caller);
        });

        callerMap.entrySet().forEach(entry -> {
            for (String s : entry.getValue()) {
                callerThreadList.get(entry.getKey()).addFriend(callerThreadList.get(s));
            }
        });

        Master.start();
        callerThreadList.entrySet().forEach(entry -> {
            entry.getValue().start();
        });
    }

    @Override
    public synchronized void onPrintMessageReceived(Printer printer) {
        printerQueue.offer(printer);
    }
}

class Printer {
    public final String message;

    Printer(String message) {
        this.message = message;
    }
}

interface PrintListener {
    void onPrintMessageReceived(Printer printer);
}
