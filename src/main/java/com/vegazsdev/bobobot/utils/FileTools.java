package com.vegazsdev.bobobot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class FileTools {

    private static final Logger logger = LoggerFactory.getLogger(FileTools.class);

    public static boolean checkIfFolderExists(String folder) {
        return !new File(folder).exists();
    }

    public static boolean createFolder(String folder) {
        if (checkIfFolderExists(folder)) {
            File dir = new File(folder);
            return dir.mkdir();
        } else {
            return false;
        }
    }

    public static boolean checkFileExistsCurPath(String file) {
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }

    public void gzipFile(String source_filepath, String dest) {
        FileOutputStream fileOutputStream = null;
        GZIPOutputStream gzipOutputStream = null;
        FileInputStream fileInput = null;

        byte[] buffer = new byte[1024];

        try {
            fileOutputStream = new FileOutputStream(dest);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            fileInput = new FileInputStream(source_filepath);

            int bytes_read;

            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, bytes_read);
            }

            fileInput.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (gzipOutputStream != null) {
                    gzipOutputStream.finish();
                    gzipOutputStream.close();
                }

                if (fileInput != null) fileInput.close();

                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        }
    }

    public String readFile(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        return sb.toString();
    }
}