
package fr.novadeskt.repository;

import fr.novadeskt.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryTest {

    private Path databaseFile;
    private TaskRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        databaseFile = Files.createTempFile("novadeskt-test-", ".db");

        repository = new TaskRepository() {
            @Override
            protected Connection getConnection() throws SQLException {
                Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:" + databaseFile.toAbsolutePath()
                );

                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA foreign_keys = ON");
                }

                return connection;
            }
        };

        try (Connection connection = repository.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT ''
                )
                """);

            statement.execute("""
                CREATE TABLE tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    status TEXT NOT NULL DEFAULT 'TODO'
                        CHECK(status IN ('TODO', 'IN_PROGRESS', 'DONE')),
                    due_date TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (project_id) REFERENCES projects(id)
                        ON DELETE CASCADE
                )
                """);

            statement.execute("""
                INSERT INTO projects (id, name, description)
                VALUES (1, 'Projet test', '')
                """);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databaseFile);
    }

    @Test
    void shouldSaveAndFindTaskById() throws SQLException {
        Task saved = repository.save(task("Préparer le rapport"));

        Optional<Task> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("Préparer le rapport", result.get().getTitle());
        assertEquals(1, result.get().getProjectId());
        assertEquals(Task.Status.TODO, result.get().getStatus());
    }

    @Test
    void shouldFindTasksByProjectId() throws SQLException {
        repository.save(task("Tâche A"));
        repository.save(task("Tâche B"));

        List<Task> tasks = repository.findByProjectId(1);

        assertEquals(2, tasks.size());
        assertEquals("Tâche B", tasks.get(0).getTitle());
        assertEquals("Tâche A", tasks.get(1).getTitle());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() throws SQLException {
        assertTrue(repository.findById(999).isEmpty());
    }

    @Test
    void shouldUpdateTask() throws SQLException {
        Task saved = repository.save(task("Ancien titre"));

        Task updated = new Task(
                saved.getId(),
                saved.getProjectId(),
                "Nouveau titre",
                "Description modifiée",
                Task.Status.IN_PROGRESS,
                LocalDate.of(2026, 11, 1),
                saved.getCreatedAt()
        );

        assertTrue(repository.update(updated));

        Task result = repository.findById(saved.getId()).orElseThrow();

        assertEquals("Nouveau titre", result.getTitle());
        assertEquals("Description modifiée", result.getDescription());
        assertEquals(Task.Status.IN_PROGRESS, result.getStatus());
        assertEquals(LocalDate.of(2026, 11, 1), result.getDueDate());
    }

    @Test
    void shouldReturnFalseWhenUpdatingMissingTask() throws SQLException {
        Task missing = new Task(
                999, 1, "Inexistante", "",
                Task.Status.TODO, null, null
        );

        assertFalse(repository.update(missing));
    }

    @Test
    void shouldDeleteTask() throws SQLException {
        Task saved = repository.save(task("À supprimer"));

        assertTrue(repository.deleteById(saved.getId()));
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingTask() throws SQLException {
        assertFalse(repository.deleteById(999));
    }

    @Test
    void shouldRejectTaskWithUnknownProject() {
        Task invalidTask = new Task(
                0, 999, "Tâche invalide", "",
                Task.Status.TODO, null, null
        );

        assertThrows(SQLException.class,
                () -> repository.save(invalidTask));
    }

    private Task task(String title) {
        return new Task(
                0, 1, title, "",
                Task.Status.TODO, null, null
        );
    }
}
