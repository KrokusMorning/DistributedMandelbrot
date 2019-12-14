package client.model;

import client.net.Connection;
import org.apache.commons.math3.complex.Complex;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Distributes
 */
public class Mandelbroter {
    private Double min_c_re;
    private Double min_c_im;
    private Double max_c_re;
    private Double max_c_im;
    private int x;
    private int y;
    private int inf_n;
    private int divisions;
    private Complex cMax;
    private Complex cMin;
    private final int COLORS = 256;
    private int colorStep;
    private int biteSizeX;
    private int biteSizeY;
    private double biteSizeRe;
    private double biteSizeIm;
    private List<Connection> connections;
    private int dispatchedBites;
    private int bites;
    private List<String> pgmParts;

    public Mandelbroter(Double min_c_re, Double min_c_im, Double max_c_re, Double max_c_im, int x, int y, int inf_n, int divisions) {
        this.min_c_re = min_c_re;
        this.min_c_im = min_c_im;
        this.max_c_re = max_c_re;
        this.max_c_im = max_c_im;
        this.x = x;
        this.y = y;
        this.inf_n = inf_n;
        this.divisions = divisions;
        cMax = new Complex(max_c_re, max_c_im);
        cMin = new Complex(min_c_re, min_c_im);
        this.colorStep = inf_n / COLORS;
        this.biteSizeY = setBiteSize(y, divisions);
        this.biteSizeIm = setBiteSizeC(min_c_im, max_c_im, divisions);
        this.bites = y/this.biteSizeY;
        pgmParts = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    /**
     * Divides a mandelbrot interval into smaller intervals and calls a method in the connection class which sends the data to the connected server.
     * @param host, the host address of the connection associated with the calling thread.
     * @param port, the port number of the connection associated with the calling thread.
     */
    public synchronized void mandelbrotBiteForServer(String host, int port){
        double minIm;
        double maxIm;
        if(this.dispatchedBites < this.bites){
            for(Connection connection : connections){
                if(connection.getPort() == port && connection.getHost().equals(host)){
                    minIm = (min_c_im + (biteSizeIm * dispatchedBites));
                    maxIm = minIm + biteSizeIm;
                    connection.dataToServer("GET /mandelbrot/" + min_c_re +"/" + minIm + "/" + max_c_re + "/" +  maxIm + "/" + x + "/" + biteSizeY + "/" + inf_n + "/");
                    this.dispatchedBites++;
                }

            }
        }
    }

    /**
     * Sorts and iterates trough all data from the servers. Adds relevant data to produce a pgm file. Data is sorted on the first number which corresponds to the
     * min_c_im value which is varied among all the mandelbrot pieces.
     */
    public void generatePgm(){

        try {
            Collections.sort(pgmParts, (s1, s2) -> {
                double d1 = Double.valueOf(s1.substring(0, 10).split(" ")[0]);
                double d2 = Double.valueOf(s2.substring(0, 10).split(" ")[0]);
                return Double.compare(d1, d2);
            });
            FileWriter fstream = new FileWriter(min_c_re +"_"+ min_c_im +"_"+ max_c_re +"_"+ max_c_im +"_"+ x + "x" + y + "_" + inf_n + "_" + "mandelbrot" + ".pgm");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("P2");
            out.newLine();
            double xStep = (max_c_re-min_c_re)/x;
            double yStep = (max_c_im-min_c_im)/y;
            Complex c = new Complex(min_c_re, min_c_im);
            out.write(x + " " + y);
            out.newLine();
            out.write("256");
            out.newLine();
            for(String pgmPart : pgmParts){
                String[] pgmValues = pgmPart.split(" ");
                for(int i= 4; i < pgmValues.length; i++)
                    out.write(pgmValues[i] + " ");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.dispatchedBites = 0;
        this.pgmParts = new ArrayList<>();
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;

    }

    private int setBiteSize(int pixels, int divisions){
        for(int i = 0; i < divisions; i++){
            pixels /= 2;
        }
        return pixels;
    }

    private double setBiteSizeC(double minData, double maxData, int divisions){
        double data = maxData - minData;
        for(int i = 0; i < divisions; i++){
            data /= 2;
        }
        return data;
    }

    public void addPgmPart(String pgmPart){
        this.pgmParts.add(pgmPart);
    }

    public List<String> getPgmParts() {
        return pgmParts;
    }

    public boolean pgmComplete() {
        return bites <= pgmParts.size();
    }

    public boolean allBitesTaken() {
        return bites <= dispatchedBites;
    }

    public void addConnection(Connection newConnection) {
        this.connections.add(newConnection);
    }
}
