package com.vegazsdev.bobobot.core.gsi;

import com.google.api.services.drive.model.File;
import com.vegazsdev.bobobot.utils.GDrive;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class GSIUpload {

    GDriveGSI enviarGSI(String gsi, ArrayList<String> var) {
        String rand = RandomStringUtils.randomAlphabetic(8);
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm z").format(Calendar.getInstance().getTime());
        try {
            String uid = gsi + " GSI " + date + " " + rand;

            GDrive.createGoogleFolder(null, uid);

            List<File> googleRootFolders = GDrive.getGoogleRootFolders();

            String folderId = "";

            for (File folder : googleRootFolders) {
                if (folder.getName().equals(uid)) {
                    folderId = folder.getId();
                }
            }

            for (String sendFile : var) {
                String fileTrim = sendFile.split("output/")[1];
                java.io.File uploadFile = new java.io.File(sendFile);
                GDrive.createGoogleFile(folderId, "application/gzip", fileTrim, uploadFile);
            }

            String aonly = "";
            String ab = "";

            List<File> arquivosNaPasta = GDrive.showFiles(folderId);
            for (File f : arquivosNaPasta) {
                if (!f.getName().contains(".txt")) {
                    if (f.getName().contains("Aonly")) {
                        aonly = f.getId();
                    } else if (f.getName().contains("AB")) {
                        ab = f.getId();
                    }
                }
            }

            GDriveGSI links = new GDriveGSI();
            if (ab != null && !ab.trim().equals("")) {
                links.setAb(ab);
            }

            if (aonly != null && !aonly.trim().equals("")) {
                links.setA(aonly);
            }

            links.setFolder(folderId);
            GDrive.createPublicPermission(folderId);
            return links;
        } catch (Exception e) {
            return null;
        }
    }

}