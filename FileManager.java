/*
 *  FileManager.java
 *  Created by Damien Robinson
 *
 *  The purpose of this class is to handle all files the program needs to process
 *  Unzipping files, merging files and removing old files.
 */

package CopyrightProgram;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FileManager {

    public static String mergedLogs = ".\\CopyrightProgram\\mergedLogs";
    public static String networkLogDirectory = ".\\CopyrightProgram\\networkLogs";
    public static String unzippedLogs = ".\\CopyrightProgram\\unzippedLogs";

    /**
     * Default Constructor for the class
     * Calls the UnZip Function for all Zipped files in a given directory.
     */
    public FileManager() {
        //Stores an array of files listed in the directory
        File[] zipFiles = fileList(networkLogDirectory);
        //For each file
        for (File file : zipFiles) {
            //Unzip the file
            unZipIt(networkLogDirectory + "\\" + file.getName(), unzippedLogs);
        }
        //Merge all the log files unzipped into one single file for easier searching
        mergeCSV(unzippedLogs);
    }

    /**
     * Unzips files passed to it and stores them in a given folder
     *
     * @param zipFile      contains the name of the ".zip" file to be unzipped
     * @param outputFolder contains the name of the folder to store the unzipped files into.
     */
    private void unZipIt(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create a log name with the current date as the name.
     *
     * @return the name of the log file.
     */
    public String netLog() {

        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        Date date = new Date();
        String networkLog = mergedLogs + "\\NetworkLog_" + dateFormat.format(date);
        return (networkLog + ".csv");

    }

    /**
     * mergeCSV, merges all the CSV files.
     */
    private void mergeCSV(String outputFolder) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "copy " + outputFolder + "\\*.csv " + netLog());
        builder.redirectErrorStream(true);
        try {
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeLogs(outputFolder, "csv");

    }

    /**
     * remove all the already processed files.
     *
     */
    public void removeLogs(String folderName, String fileType) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "del " + folderName + "\\*." + fileType);
        builder.redirectErrorStream(true);
        try {
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a list of all the files in a given directory
     *
     * @param Directory
     * @return the file list
     */
    public File[] fileList(String Directory) {
        File dir = new File(Directory);
        File[] matches = dir.listFiles();
        return matches;
    }

}
