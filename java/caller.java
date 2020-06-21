import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class caller extends Thread implements MessageListener {
    private static final long WAIT_TIME_MILLIS = 5 * 1000;

    private String name;
    private ArrayList<MessageListener> friends = new ArrayList<>();
    private BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(1000);
    private PrintListener printListener;

    caller(String name) {
        this.name = name;
    }

    public void addFriend(MessageListener... messageListener) {
        friends.addAll(Arrays.asList(messageListener));
    }

    public void addMaster(PrintListener printlistener) {
        this.printListener = printlistener;
    }

    public void run() {
        for (MessageListener friend : friends) {
            friend.onMessageReceived(new Message(this.name, MessageType.INTRO, this));
        }
        long startTimeMillis = System.currentTimeMillis();
        long currentTimeMillis = System.currentTimeMillis();
        while (currentTimeMillis - startTimeMillis < WAIT_TIME_MILLIS) {
            Message message = messageQueue.poll();
            if (message != null) {
                printer(this.name, message);
                if (message.messageType == MessageType.INTRO) {
                    message.receiverAck.onMessageReceived(new Message(this.name, MessageType.REPLY, this));
                }
            }
            currentTimeMillis = System.currentTimeMillis();
        }
        System.out.println("\nProcess " + this.name + " has received no calls for 5 seconds, ending...");
    }

    private void printer(String name, Message message) {
        this.printListener.onPrintMessageReceived(new Printer(name + " received " + message.messageType.MessageType + " message from " + message.sendName + " [ " + message.timeStamp + " ]"));
    }

    public synchronized void onMessageReceived(Message message) {
        message.timeStamp = System.currentTimeMillis();
        messageQueue.offer(message);
    }
}

enum MessageType {
    INTRO("intro"), REPLY("reply");

    public final String MessageType;

    MessageType(String messageType) {
        this.MessageType = messageType;
    }
}

class Message {
    public final String sendName;
    public final MessageType messageType;
    public final MessageListener receiverAck;
    public long timeStamp;

    Message(String sendName, MessageType messageType, MessageListener receiverAck) {
        this.sendName = sendName;
        this.messageType = messageType;
        this.receiverAck = receiverAck;
    }
}

interface MessageListener {
    void onMessageReceived(Message message);
}