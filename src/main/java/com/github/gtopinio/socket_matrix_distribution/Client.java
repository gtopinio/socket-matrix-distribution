package com.github.gtopinio.socket_matrix_distribution;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Client {
    private ArrayList<Integer> subMatrix;
    private int portNumber;

    public Client(int p) {
        this.portNumber = p;
    }

    public void start() {
        System.out.println("---------------- CLIENT RUNNING ----------------");
        // Client should read the configuration file first to determine the IP address of the server
        readConfigFile();

    }

    private void readConfigFile() {
        // TODO: read the config file
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

}
