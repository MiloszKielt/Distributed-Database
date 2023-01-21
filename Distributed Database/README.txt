READ BEFORE USING THE PROGRAM

1. HOW TO COMPILE/INSTALL
    - program requiers JDK 1.8 or newer to work correctly, if not present please install before going to next steps
    - navigate through the console to SKJProject folder
    - run command: javac *.java (assuming JDK is installed on the device)
    - after all .class files were created or altered the program is ready to go
    - additionally there is a test-script.bat file included in the folder which compiles the program and runs it with
	test data and client operations (script file is written for usage on Windows OS).

2. WHAT WAS IMPLEMENTED
    - Incremental creation of Network
        Database is created by running new instances of DatabaseNode. When initialized, the node will scan its parameters,
        then initialize its Thread, create the server socket, then connect to all specified nodes, and after that it will
        start functioning as a part of database and accept clients.
    - Storing <key>:<value> pairs in node's data
    - all project specified operations including additional 2 designed for internal communication (Server-Server)
        For further information, all operation specification is described in comments of ClientManagementThread.java file
        manageOperation method.
    - all communication is using messages in ASCII format
    - Server-Client communication based on TCP
        1. Server receives a single line message from the client
        2. Server initializes a new Thread managing and processing the clients request, and then goes back to waiting for
           another client to accept
        3. If client's request requires data information from other nodes, the server sends his message to the connected
           nodes, along with a flag containing its address marking itself as already checked. the flag parameter is
           created only during the first node's processing of the request and each subsequent node will add its address
           to it, until the request will finish processing.
        3. after Thread finishes processing, it sends back the response to the client in form of single line message.
    - Server-Server (internal) communication based on TCP
        1. Server receives a single line message from other node
        2. Server treats the other node as a client, creating a Thread managing and processing its request
        3. after Thread finishes processing, it sends back the response to the client (node) in form of single line
           message.
    - Pseudo-DNS
        Server has a getAddress() method which returns "localhost" IP address for a node with IP address "172.0.0.1"
        it is used to enable better management of flags for already checked node. Although it is used only to translate
        "localhost" hostname.
    - Exception thrown during assignment of new data results in re-assigning old data (data safety)
    - In case of sending a non-existing command to the system, a message "ERROR: WRONG COMMAND" will be returned

3. WHAT DOES NOT WORK:
    - Because of the nature of this program, server nodes can only function on one device (if we would create 2 nodes
      on different devices they wouldn't communicate on the database level). This could be solved by reconfiguring
      target's device DNS settings to return "localhost" for the localhost host address (127.0.0.1), and then changing
      "localhost" strings in the code to *.getInetAddress().getHostName() method, where (*) is the name of the socket.