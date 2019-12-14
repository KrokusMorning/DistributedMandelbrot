package client.view;

import client.model.Mandelbroter;
import client.net.Connection;
import client.net.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Interpreter implements Runnable{

    private static final String PROMPT = "";
    private final Scanner userIn = new Scanner(System.in);
    private boolean active = false;
    private List<Connection> connections;
    private Mandelbroter mandelbroter;
    private List<String> servers;

    public void start() {


        connections = new ArrayList<>();
        active = true;
        new Thread(this).start();

    }

    /**
     * Reads input form user and handles connections.
     */
    public void run() {

        printInfo();
        while(active){
            System.out.print(PROMPT);
            Input input = new Input(userIn.nextLine());
            if(input.parseInput()){
                servers = input.getServers();
                mandelbroter = input.getMandelbroter();
                String[] hostPort;
                for (String server: servers){
                    boolean alreadyConnected = false;
                    hostPort = server.split(":");
                    for(int i = 0; i < connections.size(); i++){
                        if(hostPort[0].equals(connections.get(i).getHost()) && hostPort[1].equals(String.valueOf(connections.get(i).getPort()))){
                            alreadyConnected = true;
                            mandelbroter.addConnection(connections.get(i));
                            mandelbroter.mandelbrotBiteForServer(connections.get(i).getHost(), connections.get(i).getPort());
                            break;
                        }
                    }
                    if(!alreadyConnected){
                        Connection connection = new Connection();
                        connection.setListener(new ConsoleOutput());
                        connection.connect(hostPort[0], Integer.parseInt(hostPort[1]));
                        connections.add(connection);
                    }
                }
                mandelbroter.setConnections(connections);
            }
            else{
                System.out.print("Wrong arguments. Please provide data as described below: \n");
                printInfo();
            }
        }
    }

    /**
     * Handles messages with commands and data. Data from servers are sent here trough the connection class.
     */
    private class ConsoleOutput implements Listener {
        @Override
        public void handleMsg(String msg) {
            String inputArray[] = msg.split(":");
            if(inputArray[0].equals("CONNECTED TO")){
                msg = "Connected to " + inputArray[1] + ":" + inputArray[2];
                mandelbroter.mandelbrotBiteForServer(inputArray[1], Integer.parseInt(inputArray[2]));
            }
            if(inputArray[0].equals("DISCONNECTED")){
                msg = "Disconnected from " + inputArray[1] + ":" + inputArray[2];
            }
            if(inputArray[0].equals("PGMPART")){
                mandelbroter.addPgmPart(inputArray[1]);
                if(mandelbroter.pgmComplete()){
                    mandelbroter.generatePgm();
                    System.out.println("Done!");
                }
                else if(!mandelbroter.allBitesTaken()){
                    mandelbroter.mandelbrotBiteForServer(inputArray[2], Integer.parseInt(inputArray[3]));
                }
            }
        }

    }

    private void printInfo(){
        System.out.println(
                "\n" +
                        "\n" +
                        "\n" + "please provide data in the following order: " + "\n" +
                        "min_c_re min_c_im max_c_re max_c_im max_n x y divisions list-of-servers\n" +
                        "Example: " + "\n" +
                        "-1 -1.5 2 1.5 1000 1000 1024 4 localhost:4444 localhost:3333 localhost:5555" +
                        "\n" +
                        "\n" +
                        "" );
    }


}
