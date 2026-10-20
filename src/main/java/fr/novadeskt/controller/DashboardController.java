
package fr.novadeskt.controller;

import fr.novadeskt.repository.ProjectRepository;
import fr.novadeskt.service.ProjectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML
    private TextField projectNameField;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> projectList;

    @FXML
    private Label statusLabel;

    private final ProjectService projectService =
            new ProjectService(new ProjectRepository());

    private final ObservableList<String> allProjects =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterProjects(newValue)
        );

        refreshProjects();
    }

    @FXML
    private void handleCreateProject() {
        try {
            projectService.create(projectNameField.getText(), "");

            projectNameField.clear();
            refreshProjects();

            statusLabel.setText("Projet créé avec succès.");

        } catch (IllegalArgumentException | SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de créer le projet");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void refreshProjects() {
        try {
            allProjects.setAll(
                    projectService.getAll()
                            .stream()
                            .map(project -> project.getName())
                            .toList()
            );

            filterProjects(searchField.getText());

        } catch (SQLException e) {
            statusLabel.setText(
                    "Erreur de lecture de la base de données."
            );
        }
    }

    private void filterProjects(String query) {
        String normalizedQuery = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        List<String> filteredProjects = allProjects.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT)
                        .contains(normalizedQuery))
                .toList();

        projectList.getItems().setAll(filteredProjects);
    }
}

