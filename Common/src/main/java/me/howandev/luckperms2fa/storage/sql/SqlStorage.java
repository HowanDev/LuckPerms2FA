package me.howandev.luckperms2fa.storage.sql;

import me.howandev.luckperms2fa.auth.AuthUser;
import me.howandev.luckperms2fa.storage.StorageImplementation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SqlStorage implements StorageImplementation {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS luckperms_2fa(uuid varchar(36) NOT NULL PRIMARY KEY, secret text NOT NULL, creation_complete boolean)";
    public static final String QUERY_USER = "SELECT * FROM luckperms_2fa WHERE uuid = ?";
    public static final String SAVE_USER = "INSERT INTO luckperms_2fa (uuid, secret, creation_complete) VALUES (?, ?, ?)";

    private final File file;
    public SqlStorage(File file) {
        this.file = file;
    }

    private @Nullable Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public void init() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs(); //false if directory already exists, don't care...
            file.createNewFile(); //false if file already exists, don't care...
        }

        try (Connection connection = getConnection()) {
            if (connection == null) return;

            connection.prepareStatement(CREATE_TABLE).execute();
        }
    }

    @Override
    public void shutdown() {
        /* SQLite database is in auto-commit mode, there is not much to be saved, if anything.
        try (Connection connection = getConnection()) {
            if (connection == null) return;
        }
        */
    }

    @Override
    public @Nullable AuthUser loadUser(UUID userUuid) throws Exception {
        AuthUser authUser = null;
        try (Connection connection = getConnection()) {
            if (connection == null) return null;

            PreparedStatement statement = connection.prepareStatement(QUERY_USER);
            statement.setString(1, userUuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String token = resultSet.getString("secret");
                boolean creationComplete = resultSet.getBoolean("creation_complete");
                authUser = new AuthUser(uuid, token, creationComplete);
            }
            resultSet.close();
        }

        return authUser;
    }

    @Override
    public void saveUser(@NotNull AuthUser authUser) throws Exception {
        try (Connection connection = getConnection()) {
            if (connection == null) return;

            PreparedStatement statement = connection.prepareStatement(SAVE_USER);
            statement.setString(1, authUser.getUniqueId().toString());
            statement.setString(2, authUser.getSecret());
            statement.setBoolean(3, authUser.isCreationComplete());
            statement.executeUpdate();
        }
    }
}
