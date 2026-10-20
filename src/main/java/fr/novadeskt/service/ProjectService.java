package fr.novadeskt.service;

import fr.novadeskt.model.Project;
import fr.novadeskt.repository.ProjectRepository;

import java.sql.SQLException;
import java.util.List;

public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public Project create(String name, String description)
            throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du projet est obligatoire.");
        }

        Project project = new Project(
                0, name.trim(), description, null);
        return repository.save(project);
    }

    public List<Project> getAll() throws SQLException {
        return repository.findAll();
    }
}
