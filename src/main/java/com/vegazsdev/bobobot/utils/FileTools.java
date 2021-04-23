package com.vegazsdev.bobobot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class FileTools {

    private static final Logger logger = LoggerFactory.getLogger(FileTools.class);

    public boolean checkFileExistsCurPath(String file) {
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }

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

    public void gzipFile(String source_filepath, String dest) {

        byte[] buffer = new byte[1024];

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(dest);

            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);

            FileInputStream fileInput = new FileInputStream(source_filepath);

            int bytes_read;

            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, bytes_read);
            }

            fileInput.close();

            gzipOutputStream.finish();
            gzipOutputStream.close();


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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


    public List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}