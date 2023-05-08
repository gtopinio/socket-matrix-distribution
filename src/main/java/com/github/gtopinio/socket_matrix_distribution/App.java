package com.github.gtopinio.socket_matrix_distribution;

/**
 * Author: Mark Genesis C. Topinio
 * Creation Date: May 09, 2023 1:13 AM
 * Project: Socket Matrix Distribution
 * Couse: CMSC 180 - Introduction to Parallel Computing
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "\n================ SOCKET MATRIX DISTRIBUTION APP ================");

        // Read n, p and s as user input
        int n = -1;  // Size of the square matrix
        int p = -1;  // Port number
        int s = -1;  // Status of the instance
        // Prompt user for input and store it to 'n'
        System.out.print("Enter the size of the square matrix: ");
        n = Integer.parseInt(System.console().readLine());
        // Prompt user for input and store it to 'p'
        System.out.print("Enter the port number: ");
        p = Integer.parseInt(System.console().readLine());
        // Prompt user for input and store it to 's'. S is either 0 or 1, 0 for master and 1 for slave, 
        // so we must continue to prompt the user until he/she enters 0 or 1
        while (s != 0 && s != 1) {
            System.out.print("Enter the status of the instance (0 for master, 1 for slave): ");
            s = Integer.parseInt(System.console().readLine());
        }

        // See if the user input is correct
        System.out.println( "---------------- DETAILS ----------------");
        System.out.println( "Size of the square matrix: " + n);
        System.out.println( "Port number: " + p);
        System.out.println( "Status of the instance: " + s);

        // Check the status of the instance. If it is 0, then it is the master instance, otherwise it is the slave instance.
        // If it is the master instance, then we must create a Server object and start its task
        // If it is the slave instance, then we must create a Client object and start its task.
        if (s == 0) {
            // Create a Server object and start it
            Server server = new Server(n, p);
            server.start();
        } else {
            // Create a Client object and start it
            // Client client = new Client(n, p);
            // client.start();
        }

    }
}
