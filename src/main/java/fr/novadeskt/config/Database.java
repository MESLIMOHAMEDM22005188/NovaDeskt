package fr.novadeskt.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final Path DATABASE_PATH =
            Path.of("data", "novadeskt.db");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DATABASE_PATH.getParent());
        } catch (IOException e) {
            throw new SQLException("Impossible de creer le dossier de la base.", e);
        }

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static void initialize() throws SQLException {
        try (InputStream stream = Database.class.getResourceAsStream(
                "/db/schema.sql")) {

            if (stream == null) {
                throw new SQLException("schema.sql introuvable.");
            }

            String schema = new String(
                    stream.readAllBytes(), StandardCharsets.UTF_8);

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                for (String sql : schema.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Lecture du schema impossible.", e);
        }
    }
}
