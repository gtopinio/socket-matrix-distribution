package com.github.gtopinio.socket_matrix_distribution;

import java.net.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Server {
    private int arraySize;
    private int portNumber;
    private int numberOfClients;
    private ArrayList<ArrayList<Float>> lowResArray;    // Used for the initial form of the array (low resolution)
    private ArrayList<ArrayList<ArrayList<Float>>> subMatrices; // Used for the subdivideMatrices method
    private long startTime;                             // start time variable
    private long endTime;                               // end time variable

    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;

    public Server(int n, int p) {
        this.arraySize = n;
        this.portNumber = p;
        this.numberOfClients = this.readConfigFile();
        this.lowResArray = populateArray(this.arraySize);
    }

    private int readConfigFile() {
        // Read the 'socket-comm.conf' file from the resources folder
        // The file contains the IP address of the server

        Properties prop = new Properties();
        FileInputStream input = null;
        String numClients = "";

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
            numClients = prop.getProperty("num-clients");
            this.portNumber = Integer.parseInt(prop.getProperty("server-port"));

        } catch(IOException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(numClients);
    }

    public void start() {
        System.out.println("---------------- SERVER RUNNING ----------------");
        
        // // See if population of array is correct
        // System.out.println("---------------- INITIAL ARRAY ----------------");
        // print2DArray(this.lowResArray);
        // System.out.println("---------------- END OF INITIAL ARRAY ----------------");




        // First, we should interpolate per row, but only those rows that have values where we could interpolate
        interpolatePerRow(this.lowResArray);
        // Show how many clients we must wait for
        System.out.println("Waiting for " + this.numberOfClients + " client(s) to connect...");


        System.out.println("---------------- PARTIALLY INTERPOLATING THE MATRIX ----------------");

        // See if population of array is correct
        // System.out.println("---------------- PARTIALLY INTERPOLATED ARRAY ----------------");
        // print2DArray(this.lowResArray);
        // System.out.println("---------------- END OF PARTIALLY INTERPOLATED ARRAY ----------------");




        System.out.println("---------------- SUBDIVIDING ARRAY ----------------");
        // Subdivide the array into submatrices
        this.subMatrices = subdivideMatrices(this.lowResArray, this.numberOfClients);

        // // for every 2D array from using subdivideMatrices, we print it
        // for (ArrayList<ArrayList<Float>> submatrix : this.subdivideMatrices(this.lowResArray, this.numberOfClients)) {
        //     System.out.println("---------------- SUBMATRIX ----------------");
        //     print2DArray(submatrix);
        //     System.out.println("---------------- END OF SUBMATRIX ----------------");
        // }
        System.out.println("---------------- END OF SUBDIVIDING ARRAY ----------------");



        
        System.out.println("---------------- INITIALIZING SOCKET CONNECTION ----------------");        
        // Server starts and waits for a connection
        this.serverListen();



        
        System.out.println("---------------- CALCULATING RUNNING TIME ----------------");
        // Calculate the running time
        long elapsedTimeInMillis = endTime - startTime;
        double elapsedTimeInSeconds = (double) elapsedTimeInMillis / 1000.0;
        System.out.println("Execution time in milliseconds: " + (endTime - startTime) + " ms");
        System.out.println("Elapsed time in seconds: " + elapsedTimeInSeconds + " seconds");

        System.out.println("---------------- SERVER SHUTTING DOWN ----------------");

    }

    private void interpolatePerRow(ArrayList<ArrayList<Float>> arr) {
        for (List<Float> row : arr) {
            int start = -1;
            int end = -1;
            for (int i = 0; i < row.size(); i++) {
                if (row.get(i) != 0) {
                    if (start == -1) {
                        start = i;
                    } else {
                        end = i;
                        // interpolate
                        for (int j = start + 1; j < end; j++) {
                            row.set(j, solve(row.get(start), j, start, end, row.get(end)));
                        }
                        start = end;
                    }
                }
            }
        }
    }

    // method to print 2D array
    private void print2DArray(ArrayList<ArrayList<Float>> arr){
        for(ArrayList<Float> sub: arr){
            System.out.println(sub);    // prints per row
        }
    }

    // method for populating the 2D array
    private ArrayList<ArrayList<Float>> populateArray(int size){
        ArrayList<ArrayList<Float>> populatedArr = new ArrayList<>();

        for(int i=0; i<size; i++){
            ArrayList<Float> sub = new ArrayList<>();
            for(int j=0; j<size; j++){
                if(i%10==0 && j%10==0){ // populate the given coordinate with random value
                    // Random value for elevation
                    float min = 1.0f;
                    float max = 1000.0f;
                    float randomFloat = new Random().nextFloat()*(max - min) + min;
                    randomFloat = (float) (Math.round(randomFloat * 100.0) / 100.0);
                    sub.add(randomFloat);

                } else {
                    sub.add(0.0f);
                }
            }
            populatedArr.add(sub); // add the row to the matrix
        }
        return populatedArr;
    }

    // method for sub-dividing the 2D array into sub-matrices for each client to process and interpolate further on their own end
    private ArrayList<ArrayList<ArrayList<Float>>> subdivideMatrices(ArrayList<ArrayList<Float>> matrix, int numOfClients) {
        int numRows = matrix.size();
        int numCols = matrix.get(0).size();
    
        int numSubmatrices = numOfClients;
        int numColsPerSubmatrix = numCols / numSubmatrices;
        int remainingCols = numCols % numSubmatrices; // Number of columns remaining after division
    
        ArrayList<ArrayList<ArrayList<Float>>> submatrices = new ArrayList<>();
    
        int startCol = 0;
        for (int i = 0; i < numSubmatrices; i++) {
            int endCol = startCol + numColsPerSubmatrix + (remainingCols > 0 ? 1 : 0);
            remainingCols--;
    
            ArrayList<ArrayList<Float>> submatrix = new ArrayList<>();
    
            for (int row = 0; row < numRows; row++) {
                ArrayList<Float> subRow = new ArrayList<>(matrix.get(row).subList(startCol, endCol));
                submatrix.add(subRow);
            }
    
            submatrices.add(submatrix);
            startCol = endCol;
        }
    
        return submatrices;
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

    // method for starting the server and listening for a connection
    private void serverListen(){
        int counterIndex = 0;

        try{
            this.server = new ServerSocket(this.portNumber);
            System.out.println("Server instantiated at port " + this.portNumber);

            System.out.println("Waiting for client(s) to connect...");
            
            // start the timer
            this.startTime = System.currentTimeMillis();

            // while the server is running, accept connections from clients
            while(counterIndex != this.numberOfClients){
                this.socket = server.accept();
                System.out.println("Client connected using port " + this.socket.getPort());

                // send the appropriate submatrix to the client by using the counterIndex as the index
                sendData(this.subMatrices.get(counterIndex++), counterIndex);
                
                // // get and print the interpolated submatrix from the client using receiveData()
                // ArrayList<ArrayList<Float>> interpolatedSubmatrix = receiveData();
                // System.out.println("Interpolated submatrix from client " + counterIndex + ": ");
                // print2DArray(interpolatedSubmatrix);
                
            }

            // stop the timer
            this.endTime = System.currentTimeMillis();
            
            // close connection
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
    
    // method for sending the 2D submatrix to the client
    private void sendData(ArrayList<ArrayList<Float>> submatrix, int index){
        try{
            // initialize output stream
            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
            // send the submatrix to the client
            out.writeObject(submatrix);
            // flush the output stream
            out.flush();
            System.out.println("Submatrix sent to client " + index + " successfully!");

            // try to receive an "ACK" from the client
            try{
                ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
                String ack = (String) in.readObject();
                System.out.println("Received from client " + index + ": " + ack);
            }
            catch(ClassNotFoundException e){
                System.out.println(e);
            }
        }
        catch(IOException i){
            System.out.println(i);
        }
    }

        // // method for receiving the 2D submatrix from the client
        // private ArrayList<ArrayList<Float>> receiveData() {
        //     // initialize an empty submatrix
        //     ArrayList<ArrayList<Float>> temp = new ArrayList<ArrayList<Float>>();
        //     try {
    
        //         // initialize input stream
        //         ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
        //         // receive the submatrix from the server
        //         @SuppressWarnings("unchecked")
        //         ArrayList<ArrayList<Float>> submatrix = (ArrayList<ArrayList<Float>>) in.readObject();
    
        //         // flush the input stream
        //         // in.close();
        //         System.out.println("Submatrix received from server successfully!");
    
        //         return submatrix;
        //     } catch (IOException i) {
        //         System.out.println(i);
        //     } catch (ClassNotFoundException c) {
        //         System.out.println(c);
        //     }
        //     return temp;
        // }
}
