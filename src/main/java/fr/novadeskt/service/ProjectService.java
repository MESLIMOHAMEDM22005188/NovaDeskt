
package fr.novadeskt.service;

import fr.novadeskt.model.Project;
import fr.novadeskt.repository.ProjectRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException(
                    "Le repository ne peut pas être null."
            );
        }

        this.projectRepository = projectRepository;
    }

    public Project create(String name, String description)
            throws SQLException {

        String validatedName = validateName(name);
        String validatedDescription = normalizeDescription(description);

        Project project = new Project(
                0,
                validatedName,
                validatedDescription,
                null
        );

        return projectRepository.save(project);
    }

    public List<Project> getAll() throws SQLException {
        return projectRepository.findAll();
    }

    public Optional<Project> getById(long id) throws SQLException {
        validateId(id);
        return projectRepository.findById(id);
    }

    public Project update(long id, String name, String description)
            throws SQLException {

        validateId(id);

        String validatedName = validateName(name);
        String validatedDescription = normalizeDescription(description);

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun projet trouvé avec l'identifiant " + id + "."
                ));

        Project updatedProject = new Project(
                id,
                validatedName,
                validatedDescription,
                existingProject.getCreatedAt()
        );

        boolean updated = projectRepository.update(updatedProject);

        if (!updated) {
            throw new SQLException("La modification du projet a échoué.");
        }

        return projectRepository.findById(id)
                .orElseThrow(() -> new SQLException(
                        "Le projet modifié est introuvable."
                ));
    }

    public void delete(long id) throws SQLException {
        validateId(id);

        boolean deleted = projectRepository.deleteById(id);

        if (!deleted) {
            throw new IllegalArgumentException(
                    "Aucun projet trouvé avec l'identifiant " + id + "."
            );
        }
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du projet est obligatoire."
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Le nom du projet ne peut pas dépasser 100 caractères."
            );
        }

        return normalizedName;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }

        String normalizedDescription = description.trim();

        if (normalizedDescription.length() > 2000) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser 2000 caractères."
            );
        }

        return normalizedDescription;
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant du projet doit être positif."
            );
        }
    }
}
