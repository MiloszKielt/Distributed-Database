rem Compile the program, start 7 network nodes, get max value, get min value, set 2 new values that change the max and min, ask for min and max again, find address for the key of value 3, set new record for the pair 3:6, get value for key 2, ask for min and max again, get value from all nodes, terminate all nodes

javac *.java
timeout 1 > NUL

start java DatabaseNode -tcpport 9000 -record 1:8
timeout 1 > NUL
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:7
timeout 1 > NUL
start java DatabaseNode -tcpport 9002 -connect localhost:9001 -record 3:6
timeout 1 > NUL
start java DatabaseNode -tcpport 9003 -connect localhost:9002 -record 4:5
timeout 1 > NUL
start java DatabaseNode -tcpport 9004 -connect localhost:9003 -record 5:4
timeout 1 > NUL
start java DatabaseNode -tcpport 9005 -connect localhost:9004 -record 6:3
timeout 1 > NUL
start java DatabaseNode -tcpport 9006 -connect localhost:9005 -connect localhost:9000 -record 7:1
timeout 1 > NUL

java DatabaseClient -gateway localhost:9001 -operation get-max
java DatabaseClient -gateway localhost:9002 -operation get-min
java DatabaseClient -gateway localhost:9005 -operation set-value 4:9
java DatabaseClient -gateway localhost:9002 -operation set-value 7:2
java DatabaseClient -gateway localhost:9001 -operation get-max
java DatabaseClient -gateway localhost:9002 -operation get-min
java DatabaseClient -gateway localhost:9004 -operation find-key 3
java DatabaseClient -gateway localhost:9002 -operation new-record 8:10
java DatabaseClient -gateway localhost:9006 -operation get-value 2
java DatabaseClient -gateway localhost:9005 -operation get-max
java DatabaseClient -gateway localhost:9000 -operation get-min
java DatabaseClient -gateway localhost:9006 -operation get-value 1
java DatabaseClient -gateway localhost:9006 -operation get-value 2
java DatabaseClient -gateway localhost:9006 -operation get-value 3
java DatabaseClient -gateway localhost:9006 -operation get-value 4
java DatabaseClient -gateway localhost:9006 -operation get-value 5
java DatabaseClient -gateway localhost:9006 -operation get-value 6
java DatabaseClient -gateway localhost:9006 -operation get-value 7
java DatabaseClient -gateway localhost:9006 -operation get-value 8
timeout 10 > NUL

java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9002 -operation terminate
java DatabaseClient -gateway localhost:9003 -operation terminate
java DatabaseClient -gateway localhost:9004 -operation terminate
java DatabaseClient -gateway localhost:9005 -operation terminate
java DatabaseClient -gateway localhost:9006 -operation terminate
