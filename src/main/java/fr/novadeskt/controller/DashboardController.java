package fr.novadeskt.controller;

import fr.novadeskt.repository.ProjectRepository;
import fr.novadeskt.service.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class DashboardController {
    @FXML private TextField projectNameField;
    @FXML private ListView<String> projectList;
    @FXML private Label statusLabel;

    private final ProjectService projectService =
            new ProjectService(new ProjectRepository());

    @FXML
    private void initialize() {
        refreshProjects();
    }

    @FXML
    private void handleCreateProject() {
        try {
            projectService.create(projectNameField.getText(), "");
            projectNameField.clear();
            refreshProjects();
            statusLabel.setText("Projet cree.");
        } catch (IllegalArgumentException | SQLException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Creation impossible : " + e.getMessage()).showAndWait();
        }
    }

    private void refreshProjects() {
        try {
            projectList.getItems().clear();
            projectService.getAll().forEach(
                    project -> projectList.getItems().add(project.getName()));
        } catch (SQLException e) {
            statusLabel.setText("Erreur de lecture de la base.");
        }
    }
}
