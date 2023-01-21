import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClientManagementThread extends Thread {

    private final DatabaseNode node; // node associated with the parent thread
    private final Socket client; // client that sent the request
    private final NodeThread parentThread; // server thread to which the request was sent

    public ClientManagementThread(DatabaseNode node, Socket client, NodeThread parentThread) {
        this.client = client;
        this.node = node;
        this.parentThread = parentThread;
    }


    @Override
    public void run() {
        if(!parentThread.isInterrupted()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line = in.readLine();
                System.out.println("Received: " + line);
                manageOperation(line, client);
                in.close();
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // function responsible for managing client's request, more details in README.txt file
    public void manageOperation(String line, Socket client) throws IOException {
        PrintWriter out = new PrintWriter(client.getOutputStream());
        String[] operation = line.split(" ");
        String[] pair;
        int key;
        String result;
        System.out.println("Handling operation: " + line);
        switch (operation[0]) {
            //set a new value (the second parameter) for the key being the
            //first parameter. The result of this operation is either an OK message if operation succeeded or
            //ERROR if the database contains no pair with a requested key value.
            case ("set-value"):
                pair = operation[1].split(":");
                result = setValue(pair,operation,line);
                out.println(result);
                out.flush();
                break;
            //get a value associated with the key being the parameter. The result of
            //this operation is a message consisting of a pair <key>:<value> if operation succeeded or
            //ERROR if the database contains no pair with a requested key value.
            case ("get-value"):
                key = Integer.parseInt(operation[1]);
                result = getValue(key,operation,line);
                out.println(result);
                out.flush();
                break;
            //find the address and the port number of a node, which hosts a pair with
            //the key value given as the parameter. If such node exists, the answer is a pair
            //<address>:<port> identifying this node, or the message ERROR if no node has a key with
            //such a value.
            case ("find-key"):
                key = Integer.parseInt(operation[1]);
                result = findKey(key, operation, line);
                out.println(result);
                out.flush();
                break;
            //find the biggest value of all values stored in the database. The result is a pair
            //consisting of <key>:<value>.
            case ("get-max"):
                int max = node.getValue();
                int maxKey = node.getKey();
                result = getMax(max,maxKey,operation,line);
                out.println(result);
                out.flush();
                break;
            //find the smallest value of all values stored in the database. The result is a pair
            //consisting of <key>:<value>.
            case ("get-min"):
                int min = node.getValue();
                int minKey = node.getKey();
                result = getMin(min, minKey, operation, line);
                out.println(result);
                out.flush();
                break;
            //remember a new pair key:value given as a parameter instead of the pair currently
            //stored in the node to which the client is connected. The result of this operation is the OK
            //message.
            case ("new-record"):
                pair = operation[1].split(":");
                newRecord(pair);
                out.println("OK");
                out.flush();
                break;
            //detaches the node from the database. The node informs its neighbours
            //about this fact and terminate. The informed neighbours store this fact in their resources and no
            //longer communicate with it. Just before the node terminates, it sends back the OK message to
            //a client.
            case ("terminate"):
                sendTerminate();
                out.println("OK");
                out.flush();
                parentThread.terminate();
                break;
            //information message sent from other nodes informing them about a termination of
            //other node (client).
            case ("node-terminates"):
                node.disconnect(operation[1]);
                System.out.println("Disconnected node with address " + operation[1]);
                break;
            //information message sent from other nodes informing them that the other node (client)
            //wants to set a connection with it. The new node is then added to the list of connections
            case ("connect"):
                String newAddress = operation[1];
                sendConnection(newAddress);
                break;
            //Outputs error message for command not recognized by the system.
            default:
                out.println("ERROR: WRONG COMMAND");
                out.flush();
                break;

        }
        System.out.println("...Operation finished");
    }

    // OPERATION FUNCTIONS

    // function for set-value operation
    public String setValue(String[] pair, String[] operation, String line) {
        String result;
        int backupVal = node.getValue();
        if (node.getKey() == Integer.parseInt(pair[0])) {
            System.out.println("Key found!");
            System.out.println("Changing value from " + node.getValue() + " to " + pair[1]);
            try {
                node.setValue(Integer.parseInt(pair[1]));
            } catch (Exception e) {
                node.setValue(backupVal);
            }

            System.out.println("New value set");
            result = "OK";
        } else {
            if (operation.length == 2) {
                line += " " + parentThread.getAddress() + ":" + node.getPort();
                result = sendToConnected(line);
            } else {
                line += "_" + parentThread.getAddress() + ":" + node.getPort();
                List<String> checked = Arrays.asList(operation[2].split("_"));
                result = checkSendToConnected(line, checked);
            }
        }
        return result;
    }

    // function for get-value operation
    public String getValue(int key, String[] operation, String line) {
        String result;
        if (node.getKey() == key) {
            System.out.println(node.getKey() + " found equal with " + key);
            result = node.getKey() + ":" + node.getValue();
        } else {
            if(operation.length == 2) {
                line += " " + parentThread.getAddress() + ":" + node.getPort();
                result = sendToConnected(line);
            } else {
                line += "_" + parentThread.getAddress() + ":" + node.getPort();
                List<String> checked = Arrays.asList(operation[2].split("_"));
                result = checkSendToConnected(line, checked);
            }
        }
        return result;
    }

    // function for find-key operation
    public String findKey(int key, String[] operation, String line) {
        String result;
        if (node.getKey() == key) {
            System.out.println("Key found!");
            result = parentThread.getAddress() + ":" + node.getPort();
        } else {
            if(operation.length == 2) {
                line += " " + parentThread.getAddress() + ":" + node.getPort();
                result = sendToConnected(line);
            } else {
                line += "_" + parentThread.getAddress() + ":" + node.getPort();
                List<String> checked = Arrays.asList(operation[2].split("_"));
                result = checkSendToConnected(line, checked);
            }
        }
        return result;
    }

    // function for get-max operation
    public String getMax(int max, int maxKey, String[] operation, String line) {
        String result = "";
        if(operation.length == 1) {
            line += " " + parentThread.getAddress() + ":" + node.getPort();
            result = sendToConnected(line);

            if(!Objects.equals(result, "ERROR")) {
                String[] splitRes = result.split(":");
                if(checkMax(max,Integer.parseInt(splitRes[1]))) {
                    max = Integer.parseInt(splitRes[1]);
                    maxKey = Integer.parseInt(splitRes[0]);
                }
            }
            System.out.println("current max: "  + maxKey + ":" + max);
        } else {
            line += "_" + parentThread.getAddress() + ":" + node.getPort();
            List<String> checked = Arrays.asList(operation[1].split("_"));
            result = checkSendToConnected(line,checked);
            if(!Objects.equals(result, "ERROR")) {
                String[] splitRes = result.split(":");
                if(checkMax(max,Integer.parseInt(splitRes[1]))) {
                    max = Integer.parseInt(splitRes[1]);
                    maxKey = Integer.parseInt(splitRes[0]);
                }
            }
            System.out.println("current max: "  + maxKey + ":" + max);
        }
        return maxKey + ":" + max;
    }

    // helper function for get-max operation. compares two numbers
    // and returns true if pretender is greater
    // max - currently established max value
    // pretender - value to be compared with max
    public boolean checkMax(int max, int pretender) {
        if(pretender > max) {
            System.out.println(pretender + " is greater than " + max);
            return true;
        } else {
            System.out.println(max + " is greater than " + pretender);
            return false;
        }
    }

    // function for get-min operation
    public String getMin(int min, int minKey, String[] operation, String line) {
        String result = "";
        if(operation.length == 1) {
            line += " " + parentThread.getAddress() + ":" + node.getPort();
            result = sendToConnected(line);
            if(!Objects.equals(result,"ERROR")) {
                String[] splitRes = result.split(":");
                if(checkMin(min,Integer.parseInt(splitRes[1]))) {
                    min = Integer.parseInt(splitRes[1]);
                    minKey = Integer.parseInt(splitRes[0]);
                }
            }
            System.out.println("Current min " + minKey + ":" + min);
        } else {
            line += "_" + parentThread.getAddress() + ":" + node.getPort();
            List<String> checked = Arrays.asList(operation[1].split("_"));
            result = checkSendToConnected(line, checked);
            if(!Objects.equals(result,"ERROR")) {
                String[] splitRes = result.split(":");
                if(checkMin(min,Integer.parseInt(splitRes[1]))) {
                    min = Integer.parseInt(splitRes[1]);
                    minKey = Integer.parseInt(splitRes[0]);
                }
            }
            System.out.println("Current min " + minKey + ":" + min);
        }
        return minKey + ":" + min;
    }

    // helper function for get-min operation. compares two numbers
    // and returns true if pretender is smaller
    // min - currently established min value
    // pretender - value to be compared with max
    public boolean checkMin(int min, int pretender) {
        if(pretender < min) {
            System.out.println(pretender + " is lesser than " + min);
            return true;
        } else {
            System.out.println(min + " is lesser than " + pretender);
            return false;
        }
    }

    // function for new-record operation
    public void newRecord(String[] pair) {
        int backupKey = node.getKey();
        int backupValue = node.getValue();
        try {
            node.setKey(Integer.parseInt(pair[0]));
            node.setValue(Integer.parseInt(pair[1]));
            System.out.println("New values were set");
        } catch (Exception e) {
            node.setKey(backupKey);
            node.setValue(backupValue);
            System.out.println("Error during assignment: values were not changed");
        }
    }

    // SENDER FUNCTIONS

    //function used to send a message (communicate) with other node
    public static void sendToNode(String line, String address) {
        String[] nodeAddress = address.split(":");
        PrintWriter out;
        try {
            Socket otherNode = new Socket(nodeAddress[0], Integer.parseInt(nodeAddress[1]));
            out = new PrintWriter(otherNode.getOutputStream());
            System.out.println("Sending " + line + " to " + address);
            out.println(line);
            out.flush();
            otherNode.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //function used to send a message (communicate) with other node,
    //then wait and receive response from it
    public static String sendAndReceive(String line, String address) {
        String[] nodeAddress = address.split(":");
        PrintWriter out;
        BufferedReader in;
        try (Socket otherNode = new Socket(nodeAddress[0], Integer.parseInt(nodeAddress[1]))) {
            out = new PrintWriter(otherNode.getOutputStream());
            System.out.println("Sending " + line + " to " + address);
            out.println(line);
            out.flush();
            in = new BufferedReader(new InputStreamReader(otherNode.getInputStream()));
            String result = in.readLine();
            System.out.println("Received: " + result + " from node " + address);
            otherNode.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-";
    }

    // sends line to all connected nodes
    public String sendToConnected(String line) {
        String result = "ERROR";
        for (String connection : node.getConnections()) {
            result = sendAndReceive(line, connection);

            if (Objects.equals(result, "OK"))
                break;
        }
        return result;
    }

    // sends line to all connected nodes IF they were not checked before
    public String checkSendToConnected(String line, List<String> checked) {
        String result = "ERROR";
        for (String s : node.getConnections()) {
            if (!checked.contains(s))
                result = sendAndReceive(line, s);

            if (Objects.equals(result, "OK"))
                break;

        }
        return result;
    }

    // function for sending info about node termination
    public void sendTerminate() {
        for (String connection : node.getConnections()) {
            sendToNode("node-terminates " + parentThread.getAddress() + ":" + node.getPort(), connection);
        }
    }

    // function for sending connect message to different node
    public void sendConnection(String newAddress) {
        if (!node.getConnections().contains(newAddress)) {
            this.node.connect(newAddress);
            System.out.println("Node connected with new address " + newAddress);
        } else {
            System.out.println("Node already connected");
        }
    }

}
