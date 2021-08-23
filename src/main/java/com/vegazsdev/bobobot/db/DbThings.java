package com.vegazsdev.bobobot.db;

import com.vegazsdev.bobobot.Main;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.XMLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Objects;

public class DbThings {

    /**
     * Logger: To send warning, info & errors to terminal.
     */
    private static final Logger logger = LoggerFactory.getLogger(DbThings.class);

    public static void createNewDatabase(String database) {
        if (!FileTools.checkIfFolderExists("databases")) {
            FileTools.createFolder("databases");
        }
        try (Connection conn = connect(database)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info(Objects.requireNonNull(
                        XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "sql_driver_info"))
                        .replace("%1", meta.getDriverName()));
                logger.info(Objects.requireNonNull(
                        XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "sql_db_ok"))
                        .replace("%1", database));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static Connection connect(String database) {
        String url = "jdbc:sqlite:databases/" + database;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return conn;
    }

    public static void createTable(String database, String query) {
        try (Connection conn = connect(database);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // specific prefs.db methods

    public static void insertIntoPrefsTable(double id) {
        String sql = "INSERT INTO chat_prefs(group_id) VALUES(?)";
        try (Connection conn = connect("prefs.db");
             PreparedStatement prepareStatement = conn.prepareStatement(sql)) {
            prepareStatement.setDouble(1, id);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static PrefObj selectIntoPrefsTable(double id) {
        String sql = "SELECT group_id, lang, hotkey FROM chat_prefs WHERE group_id = " + id;
        PrefObj prefObj = null;
        try (Connection conn = connect("prefs.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prefObj = new PrefObj(rs.getDouble("group_id"), rs.getString("lang"), rs.getString("hotkey"));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return prefObj;
    }

    public static void changeLanguage(double groupId, String newLang) {
        String sql = "UPDATE chat_prefs SET lang = '" + newLang + "' WHERE group_id = " + groupId;
        try (Connection conn = connect("prefs.db");
             PreparedStatement prepareStatement = conn.prepareStatement(sql)) {
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static void changeHotkey(double groupId, String newHotkey) {
        String sql = "UPDATE chat_prefs SET hotkey = '" + newHotkey + "' WHERE group_id = " + groupId;
        try (Connection conn = connect("prefs.db");
             PreparedStatement prepareStatement = conn.prepareStatement(sql)) {
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}