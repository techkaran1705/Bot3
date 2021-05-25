package com.vegazsdev.bobobot.utils;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.vegazsdev.bobobot.Main;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class JSONs {

    /**
     * Logger: To send warning, info & errors to terminal.
     */
    private static final Logger logger = LoggerFactory.getLogger(JSONs.class);

    public static void writeArrayToJSON(ArrayList<String> values, String file) {
        Gson gson = new Gson();
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(gson.toJson(values).getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList getArrayFromJSON(String file) {
        Gson gson = new Gson();

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String json = stringBuilder.toString();
            return gson.fromJson(json, ArrayList.class);
        } catch (Exception e) {
            logger.error(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "config_file_not_found") + "\n" + e.getMessage(), e);
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }
        }
    }

    // START: LAZY METHODS
    @Beta
    public static String getValueOfArrayFromJSONObject(String json, String array, String value) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(array);
            return jsonArray.getJSONObject(0).getString(value);
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
        return null;
    }

    @Beta
    public static long getLongOfArrayFromJSONObject(String json, String array, String value) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(array);
            return jsonArray.getJSONObject(0).getLong(value);
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
        return 0;
    }
    // END: LAZY METHODS
}