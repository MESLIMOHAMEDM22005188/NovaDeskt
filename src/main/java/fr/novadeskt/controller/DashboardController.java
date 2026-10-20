
package fr.novadeskt.controller;

import fr.novadeskt.model.Project;
import fr.novadeskt.repository.ProjectRepository;
import fr.novadeskt.service.ProjectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML
    private TextField projectNameField;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Project> projectList;

    @FXML
    private Label statusLabel;

    private final ProjectService projectService =
            new ProjectService(new ProjectRepository());

    private final ObservableList<Project> allProjects =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        projectList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);

                setText(empty || project == null
                        ? null
                        : project.getName());

                setStyle("-fx-padding: 14px;");
            }
        });

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterProjects(newValue)
        );

        projectList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Project selected = projectList.getSelectionModel()
                        .getSelectedItem();

                if (selected != null) {
                    openProject(selected);
                }
            }
        });

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
            showError("Impossible de créer le projet", e.getMessage());
        }
    }

    private void refreshProjects() {
        try {
            allProjects.setAll(projectService.getAll());
            filterProjects(searchField.getText());
        } catch (SQLException e) {
            statusLabel.setText(
                    "Erreur de lecture de la base de données."
            );
        }
    }

    private void filterProjects(String query) {
        String normalized = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        List<Project> filtered = allProjects.stream()
                .filter(project -> project.getName()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalized))
                .toList();

        projectList.getItems().setAll(filtered);
    }

    private void openProject(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/project-tasks.fxml")
            );

            Parent root = loader.load();

            ProjectTasksController controller = loader.getController();
            controller.setProject(project);

            Stage stage = (Stage) projectList.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("NovaDeskt — " + project.getName());

        } catch (IOException e) {
            showError("Impossible d'ouvrir le projet", e.getMessage());
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NovaDeskt");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
