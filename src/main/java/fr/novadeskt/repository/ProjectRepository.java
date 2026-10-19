package fr.novadeskt.repository;

import fr.novadeskt.config.Database;
import fr.novadeskt.model.Project;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public Project save(Project project) throws SQLException {
        String sql = """
            INSERT INTO projects(name, description)
            VALUES (?, ?)
            """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getDescription());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(connection, keys.getLong(1));
                }
            }
        }

        throw new SQLException("Creation du projet impossible.");
    }

    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY id DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                projects.add(map(result));
            }
        }

        return projects;
    }

    private Project findById(Connection connection, long id)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM projects WHERE id = ?")) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
            }
        }

        throw new SQLException("Projet introuvable.");
    }

    private Project map(ResultSet result) throws SQLException {
        String created = result.getString("created_at");
        LocalDateTime createdAt = created == null
                ? null : LocalDateTime.parse(created.replace(" ", "T"));

        return new Project(
                result.getLong("id"),
                result.getString("name"),
                result.getString("description"),
                createdAt
        );
    }
}
