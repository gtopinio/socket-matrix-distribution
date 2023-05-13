package com.github.gtopinio.socket_matrix_distribution;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Properties;

public class Client {
    private ArrayList<Integer> subMatrix;
    private String serverAddress;
    private int portNumber;
    private ArrayList<String> portNumberList;

    // initialize socket and input output streams
    private Socket socket = null;
    private BufferedReader input = null;
    private DataOutputStream out = null;

    public Client(int p) {
        this.portNumber = p;
    }

    public void start() {
        System.out.println("---------------- CLIENT RUNNING ----------------");
        // Client should read the configuration file first to determine the IP address of the server
        Boolean portExists = readConfigFile();
        if(!portExists) {
            System.out.println("---------------- CLIENT TERMINATING ----------------");
            System.exit(0);
        } else { // If port exists, we now establish a connection to the server
            System.out.println("---------------- INITIALIZING SOCKET CONNECTION ----------------");
            // Client starts and connects to the server
            clientConnect();
        }

    }

    // method for reading the configuration file
    private Boolean readConfigFile() {
        Boolean isSuccessful = false;
        // Read the 'socket-comm.conf' file from the resources folder
        // The file contains the IP address of the server

        Properties prop = new Properties();
        FileInputStream input = null;

        try {
            // Get the socket-comm.conf file from the resources folder
            
            // But get the base path first of the project
            String basePath = new java.io.File(".").getCanonicalPath();
            
            // Then get the socket-comm.conf file from the resources folder by replacing the "\target" part of the path
            // with "\src\main\java\com\github\gtopinio\socket_matrix_distribution\resources\socket-comm.conf"
            String path = basePath.replace("\\target", "\\src\\main\\java\\com\\github\\gtopinio\\socket_matrix_distribution\\resources\\socket-comm.conf");

            // Put the path to the input stream
            input = new FileInputStream(path);

            // Load the properties file
            prop.load(input);

            // Get the values of the properties
            this.serverAddress = prop.getProperty("server-ip-address");
            String clientPort = prop.getProperty("client-ports");

            // Print out the values for testing
            System.out.println("Server IP Address: " + this.serverAddress);
            System.out.println("Client Port Numbers: " + clientPort);

            // Get the client ports and store them in the portNumberList ArrayList
            this.portNumberList = new ArrayList<String>();
            for (String port : clientPort.split(", ")) {
                this.portNumberList.add(port);
            }

            // Print out the values for testing
            System.out.println("This Client's Port Number: " + this.portNumber);

            Boolean isPortNumber = false;

            for (String portNumber : this.portNumberList) {
                // Check if the port number is the same as the port number of this client
                if (Integer.parseInt(portNumber) == this.portNumber) {
                    // If it is, then set the isPortNumber to true
                    isPortNumber = true;
                    break;
                }
            }

            if(!isPortNumber) { // If the port number is not the same as the port number of this client
                // Then print out the error message
                System.out.println("ERROR: The port number of this client is not in the list of client port numbers in the configuration file.");

            } else { // If the port number is the same as the port number of this client
                // Then print out the success message
                System.out.println("SUCCESS: The port number of this client is in the list of client port numbers in the configuration file.");
                isSuccessful = true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return isSuccessful;


    }

    // method for interpolating the missing grid points using the given data points (Federal Communications Commission (FCC) Interpolation)
    private Float solve(float y1, float x, float x1, float x2, float y2){
        float ans = y1;
        float secondPart = (x-x1)/(x2-x1);
        float lastPart = y2-y1; 

        DecimalFormat df = new DecimalFormat("#.##");
        float roundedNum = Float.parseFloat(df.format(ans + secondPart * lastPart));
        return roundedNum;
    }

    private void clientConnect() {
         // establish a connection
         try {
            // We must keep reaching out to the server until we get a connection
            while(true) {
                try {
                    socket = new Socket(this.serverAddress, this.portNumber);
                    break;
                } catch (ConnectException e) {
                    System.out.println("Connection refused. Retrying...");
                }
            }
            System.out.println("Connected. Listening to server with ip address: " + socket.getRemoteSocketAddress());
            // continuously receive arrays from server
            while (true) {
                ArrayList<Integer> receivedArr = receiveData();
                for (int i : receivedArr) {
                    System.out.print(i + " ");
                }
            }

        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }
    }

    private ArrayList<Integer> receiveData() throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        int length = inputStream.readInt();
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            arr.add(inputStream.readInt());
        }
        return arr;
    }

}
