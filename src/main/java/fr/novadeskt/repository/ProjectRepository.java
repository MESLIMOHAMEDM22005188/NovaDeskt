
package fr.novadeskt.repository;

import fr.novadeskt.config.Database;
import fr.novadeskt.model.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                    return findById(connection, keys.getLong(1))
                            .orElseThrow(() ->
                                    new SQLException(
                                            "Projet créé mais introuvable."
                                    ));
                }
            }
        }

        throw new SQLException("Création du projet impossible.");
    }

    public List<Project> findAll() throws SQLException {
        String sql = "SELECT * FROM projects ORDER BY id DESC";
        List<Project> projects = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                projects.add(map(result));
            }
        }

        return projects;
    }

    public Optional<Project> findById(long id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            return findById(connection, id);
        }
    }

    public boolean update(Project project) throws SQLException {
        String sql = """
                UPDATE projects
                SET name = ?, description = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getDescription());
            statement.setLong(3, project.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM projects WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Optional<Project> findById(
            Connection connection,
            long id
    ) throws SQLException {

        String sql = "SELECT * FROM projects WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        }

        return Optional.empty();
    }

    private Project map(ResultSet result) throws SQLException {
        String created = result.getString("created_at");

        LocalDateTime createdAt = created == null
                ? null
                : LocalDateTime.parse(created.replace(" ", "T"));

        return new Project(
                result.getLong("id"),
                result.getString("name"),
                result.getString("description"),
                createdAt
        );
    }
}
