package com.github.gtopinio.socket_matrix_distribution;

import java.net.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
    private int arraySize;
    private int portNumber;
    private ArrayList<ArrayList<Float>> lowResArray;    // Used for the initial form of the array (low resolution)
    private long startTime;                             // start time variable
    private long endTime;                               // end time variable

    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;

    public Server(int n, int p) {
        this.arraySize = n;
        this.portNumber = p;
        this.lowResArray = populateArray(this.arraySize);
    }

    public void start() {
        System.out.println("---------------- SERVER RUNNING ----------------");
        
        // See if population of array is correct
        // System.out.println("---------------- INITIAL ARRAY ----------------");
        // print2DArray(this.lowResArray);
        // System.out.println("---------------- END OF INITIAL ARRAY ----------------");

        // First, we should interpolate per row, but only those rows that have values where we could interpolate
        interpolatePerRow(this.lowResArray);

        // See if population of array is correct
        // System.out.println("---------------- PARTIALLY INTERPOLATED ARRAY ----------------");
        // print2DArray(this.lowResArray);
        // System.out.println("---------------- END OF PARTIALLY INTERPOLATED ARRAY ----------------");

        System.out.println("---------------- INITIALIZING SOCKET CONNECTION ----------------");
        // Server starts and waits for a connection
        serverListen();

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
        Boolean isRunning = true;

        try{
            this.server = new ServerSocket(this.portNumber);
            System.out.println("Server instantiated at port " + this.portNumber);

            System.out.println("Waiting for client(s) to connect...");
            
            while(isRunning){
                this.socket = server.accept();
                System.out.println("Client connected using port " + this.socket.getPort());

            }
            
    
            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
}
