package com.github.gtopinio.socket_matrix_distribution;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Properties;

public class Client {
    private ArrayList<ArrayList<Float>> subMatrix;
    private String serverAddress;
    private int serverPort;
    private int portNumber;
    private ArrayList<String> portNumberList;

    // initialize socket and input output streams
    private Socket socket = null;

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
            this.serverPort = Integer.parseInt(prop.getProperty("server-port"));
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
                    socket = new Socket(this.serverAddress, this.serverPort);
                    break;
                } catch (ConnectException e) {
                    System.out.println("Connection refused. Retrying...");
                }
            }
            System.out.println("Connected. Listening to server with ip address: " + socket.getRemoteSocketAddress());
            // continuously receive arrays from server
            this.subMatrix = receiveData();
            // print the submatrix
            System.out.println("Submatrix received from server: ");
            for (int i = 0; i < this.subMatrix.size(); i++) {
                for (int j = 0; j < this.subMatrix.get(i).size(); j++) {
                    System.out.print(this.subMatrix.get(i).get(j) + " ");
                }
                System.out.println();
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

    // method for receiving the 2D submatrix from the server
    private ArrayList<ArrayList<Float>> receiveData(){
        // initialize an empty submatrix
        ArrayList<ArrayList<Float>> temp = new ArrayList<ArrayList<Float>>();
        try{
            // initialize input stream
            ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
            // receive the submatrix from the server
            @SuppressWarnings("unchecked")
            ArrayList<ArrayList<Float>> submatrix  = (ArrayList<ArrayList<Float>>) in.readObject();
            // flush the input stream
            in.close();
            System.out.println("Submatrix received from server successfully!");
            return submatrix;
        }
        catch(IOException i){
            System.out.println(i);
        }
        catch(ClassNotFoundException c){
            System.out.println(c);
        }
        return temp;
    }

}
