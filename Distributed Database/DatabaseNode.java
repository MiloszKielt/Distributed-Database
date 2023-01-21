import java.util.ArrayList;
import java.util.List;

public class DatabaseNode {
    private int port = 0;
    private int key;
    private int value;
    private final List<String> connections;

    public DatabaseNode(String[] args) {
        connections = new ArrayList<>();
        // Parameter scan loop
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case ("-tcpport"):
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case ("-record"):
                    String[] pair = args[i + 1].split(":");
                    key = Integer.parseInt(pair[0]);
                    value = Integer.parseInt(pair[1]);
                    break;
                case ("-connect"):
                    connections.add(args[i + 1]);
                    break;
                default:
                    System.out.println("ERROR: wrong command: " + args[i]);
                    break;
            }

        }

    }

    // initialization of server thread
    public void initialize() {
        NodeThread thread = new NodeThread(this);
        thread.start();
    }

    // function for connecting a new node to this one
    public void connect(String address) {
        this.connections.add(address);
        System.out.println("Added: " + address);
        System.out.println(connections);
    }

    // function for disconnecting a node from this one
    public void disconnect(String address) {
        this.connections.remove(address);
    }

    // getter functions used in operation management
    public List<String> getConnections() {
        return this.connections;
    }

    public int getPort() {
        return this.port;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return this.value;
    }

    // setter functions used in operation management
    public void setKey(int key) {
        this.key = key;
    }

    public void setValue(int value) {
        this.value = value;
    }

    // main function, starts the execution of the program and initializes a new DatabaseNode object
    public static void main(String[] args) {
        DatabaseNode newNode = new DatabaseNode(args);
        newNode.initialize();
    }
}
