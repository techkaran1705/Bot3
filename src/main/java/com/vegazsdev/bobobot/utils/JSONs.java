package com.vegazsdev.bobobot.utils;

import com.google.gson.Gson;
import com.vegazsdev.bobobot.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JSONs {

    private static final Logger logger = LoggerFactory.getLogger(JSONs.class);

    public static void writeArrayToJSON(ArrayList<String> values, String file) {
        Gson gson = new Gson();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(gson.toJson(values).getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList getArrayFromJSON(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            Gson gson = new Gson();
            return gson.fromJson(json, ArrayList.class);
        } catch (Exception e) {
            logger.error(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "config_file_not_found") + "\n" + e.getMessage(), e);
            return null;
        }
    }
}