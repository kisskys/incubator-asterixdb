package edu.uci.ics.asterix.api.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class JoinPointDatasetGen {

    public static void main(String[] args) {

        //This program reads points formatted as asterixdb's point type in the input file.
        //example input file:
        //------------------
        //point("10.1286166,53.6075788")
        //point("13.285573,53.785484")
        //point("39.514164,53.928272")
        //------------------
        //Then, the corresponding adm records are created for those points.
        //example output file:
        //------------------
        //{ "id": 0i64, "coordinates": point("10.1286166,53.6075788") }
        //{ "id": 1i64, "coordinates": point("13.285573,53.785484") }
        //{ "id": 2i64, "coordinates": point("39.514164,53.928272") }
        
        
        if (args.length < 2) {
            System.out.println("Usage: java -jar JoinPointDatasetGen.jar <input file name> <output file name>");
            System.exit(-1);
        }
        String inputFileName = args[0];
        String outputFileName = args[1];

        BufferedReader br = null;
        FileOutputStream fos = null;
        String line;
        StringBuilder sb = new StringBuilder();
        long id = 0;
        try {
            br = new BufferedReader(new FileReader(inputFileName));
            fos = openOutputFile(outputFileName);
            while ((line = br.readLine()) != null) {
                sb.setLength(0);
                sb.append("{ ");
                sb.append("\"id\": ").append(id++).append("i64");
                sb.append(", \"coordinates\": ").append(line.trim());
                sb.append(" }\n");
                fos.write(sb.toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeOutputFile(fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static FileOutputStream openOutputFile(String filepath) throws IOException {
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return new FileOutputStream(file);
    }

    public static void closeOutputFile(FileOutputStream fos) throws IOException {
        fos.flush();
        fos.close();
        fos = null;
    }

}