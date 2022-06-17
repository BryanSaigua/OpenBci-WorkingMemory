package com.example.openbci_workingmemory.components;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads EEG data from CSV files
 */

public class EEGFileReader {

    // ---------------------------------------------------------------------------
    // Variables

    FileReader inputStream;
    //InputStream inputStream;
    private Context context;
    List<double[]> readList;

    public EEGFileReader(FileReader inputStream) {
    //    public EEGFileReader(InputStream inputStream) {
        this.inputStream = inputStream;
        this.context = context;
    }

    public List read() {
        List<double[]> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(inputStream);
        //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] stringLine = csvLine.split(",");
                double[] line = new double[stringLine.length];
                for (int i = 0; i < line.length; i++) {
                    line[i] = Double.parseDouble(stringLine[i]);
                }
                resultList.add(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        return resultList;
    }

    public double[][] readToArray() {
        readList = read();
        int len = readList.size();

        double[][] readArray = new double[readList.size()][4];
        System.out.println("tamaño ---------"+readArray.length);
        for (int i = 0; i < len; i++) {
            readArray[i] = readList.get(i);
        }

        return readArray;
    }

    public float[] readToVector(int channelOfInterest) {
        /*
        * 0 - timestamp
        * 1 - canal 1
        * 2
        * 3
        * 4
        * */
        readList = read();
        int len = readList.size();

        float[] readArray = new float[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (float) readList.get(i)[channelOfInterest];
        }

        return readArray;
    }

    public float[] readNoneBlink() {
        readList = read();
        int len = readList.size();

        float[] readArray = new float[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (float) 0;
        }

        return readArray;
    }

    public int[] readConfigurations() {
        readList = read();
        int len = readList.size();

        int[] readArray = new int[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (int) readList.get(i)[0];
        }

        return readArray;
    }
}