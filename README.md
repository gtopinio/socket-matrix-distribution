# Socket Matrix Distribution

Author: Mark Genesis C. Topinio
Creation Date: May 09, 2023 1:13 AM
Course: CMSC 180 - Introduction to Parallel Computing (T4L)

## Project Description

This project aims to distribute a matrix across multiple instances using socket programming. It provides a program that creates an open socket for sending and receiving data. The program allows users to specify the port number to open and listen to, as well as the configuration of the server and clients. Multiple instances of the program can be run on different terminals or PCs, facilitating parallel processing of the matrix.

## Installation

To install and run this Java Maven project, follow these steps:

1. Install Java on your machine if it's not already installed.
2. Install Maven as well.
3. Make a copy of the git repository: [https://github.com/gtopinio/socket-matrix-distribution](https://github.com/gtopinio/socket-matrix-distribution)
4. Once Java and Maven are successfully installed, navigate to the project directory.
5. Run the command `mvn clean package` to generate the target folder where the JAR file will be located.
6. After the JAR file is created, navigate to the target folder.
7. Run the program using the command `java -jar socket-matrix-distribution-1.0.0.jar`.
    - **Note:** If you're in any other operating system other than Windows, you need to change the path directories from the class files. Change the double backslashes to forward slashes. To do this easily, press `Ctrl + H` to open the Find and Replace dialog box. In the Find field, enter `\\` and in the Replace field, enter `/`. Click Replace All to replace all instances of double backslashes with forward slashes. 
    - If there are any modifications to the project code or configuration file, the program must be recompiled using the command `mvn clean package` before running it again. If there are errors while recompling, run the command `mvn package` to clean the project and generate the JAR file.

## Usage

To use the program, follow these instructions:

1. After running the program, you will be prompted to enter the size of the square matrix (n) as a positive integer (should be divisible by 10).
2. Enter the port number. If the instance is a server, the default port is 5000. If it's a client, the port number must be within the range specified in the configuration file.
3. Enter the status of the instance (0 for server, 1 for client).
4. If the instance is a server, you will receive details about the matrix, and the server will wait for the specified number of clients (specified in the configuration file) to connect.
   - **Note:** Ensure that the exact number of clients specified in the configuration file is created before running the server.
5. If the instance is a client, it will continuously wait for a connection to the server. The client will receive a submatrix distributed by the server and send an acknowledgement.
6. After receiving the submatrix, the client will interpolate it and check if the interpolation is correct.
7. The server will keep track of the time until all clients have sent an acknowledgement.
    - **Note:** For the devs, for easier testing, you can run the server and clients using a different command. Run `java -jar socket-matrix-distribution-1.0.0.jar client` to run the client and `java -jar socket-matrix-distribution-1.0.0.jar server [size of matrix]` to run the server. This will automatically set the status of the instance to client or server, respectively.

## Configuration

Before running the program, it is crucial to check the configuration file `socket-comm.conf` located in the resources directory. The file has the following format:

*Configuration file for socket communication program*

*IP Address of Server*\
server-ip-address=127.0.0.1

*Port Number of Server*\
server-port=5000

*Port Numbers of Clients*\
client-ports=5001, 5002, 5003, 5004, 5005

*Number of Clients*\
num-clients = 3

## Features

The program offers the following main features and functionalities:

- **Socket Communication**: The program utilizes socket programming to establish communication between the server and clients, allowing for the distribution of data.

- **Matrix Distribution**: The server instance creates a square matrix and divides it into submatrices based on the specified number of clients. Each client receives a submatrix for processing.

- **Parallel Processing**: By running multiple instances of the program on different terminals or PCs, the matrix processing is parallelized, enabling faster computation.

- **Configuration File**: The program provides a configuration file (`socket-comm.conf`) to specify the IP address and port details of the server, the ports for clients, and the number of clients. This allows for flexible setup and customization.

- **Interpolation**: The client instances are responsible for interpolating the received submatrix and verifying its correctness.

- **Time Tracking**: The server tracks the time taken for all clients to send their acknowledgements, providing insights into the overall processing time.

- **Command-Line Interface**: The program offers a user-friendly command-line interface, prompting the user to input the matrix size, port number, and instance status (server or client).

These features combine to enable efficient distribution and processing of matrices across multiple instances, facilitating parallel computing in a socket-based environment.
