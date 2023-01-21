import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class NodeThread extends Thread {
    private final DatabaseNode node;  // node associated with this thread
    private ServerSocket serverSocket; //

    public NodeThread(DatabaseNode dn) {
        this.node = dn;
    }

    // Initialization and the main loop for the server
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(node.getPort(), 50, InetAddress.getByName("localhost"));
            for (String s : node.getConnections()) {
                ClientManagementThread.sendToNode("connect " + getAddress() + ":" + serverSocket.getLocalPort(), s);
            }
            while (!this.isInterrupted()) {
                System.out.println("Waiting for connection on port " + serverSocket.getLocalPort());
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Received connection from " + client.getInetAddress().getHostAddress());
                    new ClientManagementThread(node, client, this).start(); // initialization of client management thread
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // if server creation fails, the node terminates
        } catch (IOException e) {
            for (String s : node.getConnections()) {
                ClientManagementThread.sendToNode("node-terminates", s);
            }
            e.printStackTrace();
        }

    }

    public String getAddress() {
        if(Objects.equals(this.serverSocket.getInetAddress().getHostName(), "172.0.0.1"))
            return "localhost";
        else
            return this.serverSocket.getInetAddress().getHostName();
    }

    // function used to terminate the server process, first it closes the server so that it won't accept any new clients
    // and then interrupts and ends the Thread loop
    public void terminate() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.interrupt();
    }
}