package com.github.gtopinio.socket_matrix_distribution;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

// ClientHandler class to handle each client connection in a separate thread
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ArrayList<ArrayList<ArrayList<Float>>> subMatrices;
    private int clientIndex;
    private static int numberOfAcks = 0;

    public ClientHandler(Socket socket, ArrayList<ArrayList<ArrayList<Float>>> subMatrices, int clientIndex) {
        this.clientSocket = socket;
        this.subMatrices = subMatrices;
        this.clientIndex = clientIndex;
    }

    // getter and setter for the number of acknowledgements received by the server
    synchronized static int getNumOfAcks() {
        return numberOfAcks;
    }

    synchronized static void increaseNumOfAcks() {
        numberOfAcks++;
    }


    @Override
    public void run() {
        try {
            // send the appropriate submatrix to the client
            sendData(this.subMatrices.get(clientIndex), clientIndex);

            // close the client socket
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    // method for sending the 2D submatrix to the client
    private void sendData(ArrayList<ArrayList<Float>> submatrix, int index) {
        try {
            // initialize output stream
            ObjectOutputStream out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            // send the submatrix to the client
            out.writeObject(submatrix);
            // flush the output stream
            out.flush();
            System.out.println("Submatrix sent to client " + (index + 1) + " successfully!");
            

            // try to receive an "ACK" from the client
            try {
                ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());
                String ack = (String) in.readObject();
                System.out.println("Received from client " + (index + 1) + ": " + ack);
                // increase the number of acknowledgements received by the server
                increaseNumOfAcks();
                if(getNumOfAcks() == Server.getNumberOfClients()){
                    System.out.println("All clients have received their submatrices!");
                }

                
            } catch (ClassNotFoundException e) {
                System.out.println("Failed to receive acknowledgement from client " + (index + 1));
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Failed to send submatrix to client " + (index + 1));
            e.printStackTrace();
        }
    }
}