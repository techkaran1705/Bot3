package com.vegazsdev.bobobot.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GDrive {

    // most of code here is from: https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java
    // and from: https://developers.google.com/drive/api/v3/quickstart/java

    private static final Logger LOGGER = (Logger) LogManager.getLogger(GDrive.class);

    private static String APPLICATION_NAME = "Google Drive API Java Quickstart";

    private static final java.io.File CREDENTIALS_FOLDER = new java.io.File("credentials");

    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Directory to store user credentials for this application.
    private static String CLIENT_SECRET_FILE_NAME = "credentials.json";

    private static List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    // Global instance of the {@link FileDataStoreFactory}.
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    // Global instance of the HTTP transport.
    private static HttpTransport HTTP_TRANSPORT;

    private static Drive _driveService;

    public static void createGoogleFolder(String folderIdParent, String folderName) throws IOException {
        File fileMetadata = new File();

        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (folderIdParent != null) {
            List<String> parents = Collections.singletonList(folderIdParent);
            fileMetadata.setParents(parents);
        }
        Drive driveService = getDriveService();
        // Create a Folder.
        // Returns File object with id & name fields will be assigned values
        driveService.files().create(fileMetadata).setFields("id, name").execute();
    }

    private static Drive getDriveService() throws IOException {
        reset();
        _driveService = null;
        Credential credential = getCredentials();
        _driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
                .setApplicationName(APPLICATION_NAME).build();
        return _driveService;
    }

    private static void reset() {
        APPLICATION_NAME = "Google Drive API Java Quickstart";
        JSON_FACTORY = JacksonFactory.getDefaultInstance();
        CLIENT_SECRET_FILE_NAME = "credentials.json";
        SCOPES = Collections.singletonList(DriveScopes.DRIVE);
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(CREDENTIALS_FOLDER);
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        _driveService = null;
    }

    private static Credential getCredentials() throws IOException {
        java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

        if (!clientSecretFilePath.exists()) {
            throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static List<File> getGoogleRootFolders() throws IOException {
        return getGoogleSubFolders(null);
    }

    public static List<File> getGoogleSubFolders(String googleFolderIdParent) throws IOException {

        Drive driveService = getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<>();

        String query;
        if (googleFolderIdParent == null) {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents";
        } else {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents";
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime
                    .setFields("nextPageToken, files(id, name, createdTime)")//
                    .setPageToken(pageToken).execute();
            list.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    public static void createGoogleFile(String googleFolderIdParent, String contentType, //
                                        String customFileName, java.io.File uploadFile) throws IOException {
        AbstractInputStreamContent uploadStreamContent = new FileContent(contentType, uploadFile);
        _createGoogleFile(googleFolderIdParent, customFileName);
    }

    private static void _createGoogleFile(String googleFolderIdParent, String customFileName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(customFileName);

        List<String> parents = Collections.singletonList(googleFolderIdParent);
        fileMetadata.setParents(parents);

        Drive driveService = getDriveService();
    }

    public static List<File> showFiles(String parent) throws IOException {

        Drive driveService = getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<>();

        do {
            FileList result = driveService.files().list()
                    .setQ("'" + parent + "' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, parents)")
                    .setPageToken(pageToken)
                    .execute();
            list.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    public static void createPublicPermission(String googleFileId) throws IOException {
        // All values: user - group - domain - anyone
        String permissionType = "anyone";
        // All values: organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        Drive driveService = getDriveService();
        driveService.permissions().create(googleFileId, newPermission).execute();
    }


}
