package com.github.gtopinio.socket_matrix_distribution;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static final String ACK_STRING = "ACK";

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
            // with "\\target", "\\src\\main\\java\\com\\github\\gtopinio\\socket_matrix_distribution\\resources\\socket-comm.conf" [for Windows]
            // or with "/src/main/java/com/github/gtopinio/socket_matrix_distribution/resources/socket-comm.conf" [for Linux]
            String path = "";
            try {
                path = basePath.replace("/target", "/src/main/java/com/github/gtopinio/socket_matrix_distribution/resources/socket-comm.conf");
            } catch (Exception e) {
               
            } finally {
                path = basePath.replace("\\target", "\\src\\main\\java\\com\\github\\gtopinio\\socket_matrix_distribution\\resources\\socket-comm.conf");
            }


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

            System.out.println("---------------- CLIENT SHUTTING DOWN ----------------");
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

    // method for connecting to the server, receiving the submatrix, sending an acknowledgement, and interpolating the missing grid points
    private void clientConnect() {
         // establish a connection
         try {
            // We must keep reaching out to the server until we get a connection
            while(true) {
                try {
                    this.socket = new Socket(this.serverAddress, this.serverPort);
                    break;
                } catch (ConnectException e) {
                    System.out.println("Connection refused. Retrying...");
                }
            }
            System.out.println("Connected. Listening to server with ip address: " + socket.getRemoteSocketAddress());

            // receive the submatrix from the server and send an acknowledgement
            this.subMatrix = receiveData();

            // print the submatrix
            // System.out.println("Submatrix received from server: ");
            // for (int i = 0; i < this.subMatrix.size(); i++) {
            //     for (int j = 0; j < this.subMatrix.get(i).size(); j++) {
            //         System.out.print(this.subMatrix.get(i).get(j) + " ");
            //     }
            //     System.out.println();
            // }

            
            System.out.println("---------------- INTERPOLATING MISSING GRID POINTS ----------------");
            // interpolate the missing grid points
            this.interpolateMatrix(this.subMatrix);

            System.out.println("---------------- CHECKING INTERPOLATED SUBMATRIX ----------------");
            if(this.checkInterpolation(this.subMatrix)){
                System.out.println("SUCCESS: All missing grid points have been interpolated.");
            } else {
                System.out.println("ERROR: Not all missing grid points have been interpolated.");
            }
            

            // System.err.println("---------------- INTERPOLATED SUBMATRIX ----------------");
            // // print the interpolated submatrix
            // for (int i = 0; i < this.subMatrix.size(); i++) {
            //     for (int j = 0; j < this.subMatrix.get(i).size(); j++) {
            //         System.out.print(this.subMatrix.get(i).get(j) + " ");
            //     }
            //     System.out.println();
            // }

            // // send the interpolated submatrix to the server
            // sendData(this.subMatrix);

            System.out.println("---------------- CLIENT SHUTTING DOWN ----------------");

            // close the connection
            this.socket.close();
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

    private ArrayList<ArrayList<Float>> interpolateMatrix(ArrayList<ArrayList<Float>> matrix) {
        for (int col = 0; col < matrix.get(0).size(); col++) {
            int firstKnownValueIndex = -1;
            for (int row = 0; row < matrix.size(); row++) {
                if (matrix.get(row).get(col) != 0.0) {
                    if (firstKnownValueIndex == -1) {
                        firstKnownValueIndex = row;
                    } else {
                        int secondKnownValueIndex = row;
                        float y1 = matrix.get(firstKnownValueIndex).get(col);
                        float y2 = matrix.get(secondKnownValueIndex).get(col);
                        float x1 = firstKnownValueIndex;
                        float x2 = secondKnownValueIndex;
                        for (int i = firstKnownValueIndex + 1; i < secondKnownValueIndex; i++) {
                            float x = i;
                            float interpolatedValue = solve(y1, x, x1, x2, y2);
                            matrix.get(i).set(col, interpolatedValue);
                        }
                        firstKnownValueIndex = secondKnownValueIndex;
                    }
                }
            }
        }
        return matrix;
    }

    // method for checking if the interpolated submatrix is correct
    private boolean checkInterpolation(ArrayList<ArrayList<Float>> matrix) {
        boolean isCorrect = true;
        for (int col = 0; col < matrix.get(0).size(); col++) {
            int firstKnownValueIndex = -1;
            for (int row = 0; row < matrix.size(); row++) {
                if (matrix.get(row).get(col) != 0.0) {
                    if (firstKnownValueIndex == -1) {
                        firstKnownValueIndex = row;
                    } else {
                        int secondKnownValueIndex = row;
                        float y1 = matrix.get(firstKnownValueIndex).get(col);
                        float y2 = matrix.get(secondKnownValueIndex).get(col);
                        float x1 = firstKnownValueIndex;
                        float x2 = secondKnownValueIndex;
                        for (int i = firstKnownValueIndex + 1; i < secondKnownValueIndex; i++) {
                            float x = i;
                            float interpolatedValue = solve(y1, x, x1, x2, y2);
                            if (interpolatedValue != matrix.get(i).get(col)) {
                                isCorrect = false;
                                break;
                            }
                        }
                        firstKnownValueIndex = secondKnownValueIndex;
                    }
                }
            }
        }
        return isCorrect;
    }

    // method for receiving the 2D submatrix from the server and sending an acknowledgement
    private ArrayList<ArrayList<Float>> receiveData() {
        // initialize an empty submatrix
        ArrayList<ArrayList<Float>> temp = new ArrayList<ArrayList<Float>>();
        try {

            // initialize input stream
            ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
            // receive the submatrix from the server
            @SuppressWarnings("unchecked")
            ArrayList<ArrayList<Float>> submatrix = (ArrayList<ArrayList<Float>>) in.readObject();
            
            // try to send an "ACK" message to the server
            try {
                // initialize output stream
                ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
                // send the "ACK" message to the server
                out.writeObject(Client.ACK_STRING);
                // flush the output stream
                out.flush();
            } catch (IOException i) {
                System.out.println(i);
            }

            // flush the input stream
            in.close();
            System.out.println("Submatrix received from server successfully!");

            return submatrix;
        } catch (IOException i) {
            System.out.println(i);
        } catch (ClassNotFoundException c) {
            System.out.println(c);
        }
        return temp;
    }

        // // method for sending the 2D submatrix to the client
        // private void sendData(ArrayList<ArrayList<Float>> submatrix){
        //     try{
        //         // initialize output stream
        //         ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
        //         // send the submatrix to the client
        //         out.writeObject(submatrix);
        //         // flush the output stream
        //         out.flush();
        //         System.out.println("Submatrix sent to server successfully!");
        //     }
        //     catch(IOException i){
        //         System.out.println(i);
        //     }
        // }


}
