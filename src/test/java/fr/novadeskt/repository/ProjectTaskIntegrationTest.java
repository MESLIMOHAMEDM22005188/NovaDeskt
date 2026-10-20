
        package fr.novadeskt.repository;

import fr.novadeskt.config.Database;
import fr.novadeskt.model.Project;
import fr.novadeskt.model.Task;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTaskIntegrationTest {

    @Test
    void deletingProjectShouldDeleteItsTasks() throws SQLException {
        Database.initialize();

        ProjectRepository projectRepository = new ProjectRepository();
        TaskRepository taskRepository = new TaskRepository();

        Project project = projectRepository.save(
                new Project(0, "Projet cascade test", "", null)
        );

        Task task = taskRepository.save(
                new Task(
                        0,
                        project.getId(),
                        "Tâche liée",
                        "",
                        Task.Status.TODO,
                        null,
                        null
                )
        );

        assertTrue(taskRepository.findById(task.getId()).isPresent());

        assertTrue(projectRepository.deleteById(project.getId()));

        assertTrue(projectRepository.findById(project.getId()).isEmpty());
        assertTrue(taskRepository.findById(task.getId()).isEmpty());
    }

    @Test
    void deletingProjectShouldNotDeleteTasksFromAnotherProject()
            throws SQLException {
        Database.initialize();

        ProjectRepository projectRepository = new ProjectRepository();
        TaskRepository taskRepository = new TaskRepository();

        Project projectA = projectRepository.save(
                new Project(0, "Projet A cascade", "", null)
        );

        Project projectB = projectRepository.save(
                new Project(0, "Projet B cascade", "", null)
        );

        Task taskA = taskRepository.save(
                new Task(0, projectA.getId(), "Tâche A", "",
                        Task.Status.TODO, null, null)
        );

        Task taskB = taskRepository.save(
                new Task(0, projectB.getId(), "Tâche B", "",
                        Task.Status.TODO, null, null)
        );

        assertTrue(projectRepository.deleteById(projectA.getId()));

        assertTrue(taskRepository.findById(taskA.getId()).isEmpty());
        assertTrue(taskRepository.findById(taskB.getId()).isPresent());
    }

    @Test
    void foreignKeysShouldBeEnabledOnEveryConnection()
            throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "PRAGMA foreign_keys");
             ResultSet result = statement.executeQuery()) {

            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }
    }
}
