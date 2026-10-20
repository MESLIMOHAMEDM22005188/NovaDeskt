
package fr.novadeskt.controller;

import fr.novadeskt.model.Project;
import fr.novadeskt.model.Task;
import fr.novadeskt.repository.TaskRepository;
import fr.novadeskt.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProjectTasksController {

    @FXML
    private Label projectTitle;

    @FXML
    private Label taskSummary;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField titleField;

    @FXML
    private TextField searchField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ListView<Task> taskList;

    private final TaskService taskService =
            new TaskService(new TaskRepository());

    private Project project;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private boolean editing;
    private long editingTaskId;

    @FXML
    private void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous les statuts",
                "À faire",
                "En cours",
                "Terminées"
        ));

        statusFilter.setValue("Tous les statuts");

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> refreshTasks()
        );

        statusFilter.valueProperty().addListener(
                (observable, oldValue, newValue) -> refreshTasks()
        );

        taskList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String status = switch (task.getStatus()) {
                    case TODO -> "À FAIRE";
                    case IN_PROGRESS -> "EN COURS";
                    case DONE -> "TERMINÉE";
                };

                String dueDate = task.getDueDate() == null
                        ? "Sans échéance"
                        : "Échéance : "
                        + task.getDueDate().format(dateFormatter);

                String late = task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && task.getStatus() != Task.Status.DONE
                        ? " — EN RETARD"
                        : "";

                setText(task.getTitle()
                        + "\n" + status
                        + " • " + dueDate + late
                        + (task.getDescription().isBlank()
                        ? ""
                        : "\n" + task.getDescription()));

                setStyle("-fx-padding: 12px; -fx-font-size: 13px;");
            }
        });

        taskList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldTask, newTask) -> {
                    if (newTask != null && !editing) {
                        fillForm(newTask);
                    }
                });
    }

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException(
                    "Le projet ne peut pas être null."
            );
        }

        this.project = project;
        projectTitle.setText(project.getName());
        refreshTasks();
    }

    @FXML
    private void handleCreateTask() {
        if (project == null) {
            showError("Aucun projet sélectionné.");
            return;
        }

        try {
            if (editing) {
                Task.Status selectedStatus =
                        getSelectedTaskStatus();

                taskService.update(
                        editingTaskId,
                        titleField.getText(),
                        descriptionField.getText(),
                        selectedStatus,
                        dueDatePicker.getValue()
                );

                statusLabel.setText("Tâche modifiée.");
            } else {
                Task created = taskService.create(
                        project.getId(),
                        titleField.getText(),
                        descriptionField.getText()
                );

                if (dueDatePicker.getValue() != null) {
                    taskService.update(
                            created.getId(),
                            created.getTitle(),
                            created.getDescription(),
                            created.getStatus(),
                            dueDatePicker.getValue()
                    );
                }

                statusLabel.setText("Tâche créée.");
            }

            clearForm();
            refreshTasks();

        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleEditTask() {
        Task selected = taskList.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showError("Sélectionne une tâche à modifier.");
            return;
        }

        editing = true;
        editingTaskId = selected.getId();
        fillForm(selected);
        statusLabel.setText(
                "Modification : change les champs puis valide."
        );
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = taskList.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showError("Sélectionne une tâche à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer une tâche");
        confirmation.setHeaderText(selected.getTitle());
        confirmation.setContentText(
                "Cette action est irréversible."
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent()
                && result.get() == ButtonType.OK) {
            try {
                taskService.delete(selected.getId());
                clearForm();
                refreshTasks();
                statusLabel.setText("Tâche supprimée.");
            } catch (IllegalArgumentException | SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) taskList.getScene().getWindow();

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass().getResource("/fxml/dashboard.fxml")
                    );

            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene =
                    new javafx.scene.Scene(root, 900, 600);

            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css")
                            .toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("NovaDeskt");

        } catch (java.io.IOException e) {
            showError("Impossible de revenir au tableau de bord.");
        }
    }

    private void refreshTasks() {
        if (project == null) {
            return;
        }

        try {
            List<Task> tasks =
                    taskService.getByProject(project.getId());

            String query = searchField.getText() == null
                    ? ""
                    : searchField.getText()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String selectedFilter = statusFilter.getValue();

            List<Task> filtered = tasks.stream()
                    .filter(task -> task.getTitle()
                            .toLowerCase(Locale.ROOT)
                            .contains(query)
                            || task.getDescription()
                            .toLowerCase(Locale.ROOT)
                            .contains(query))
                    .filter(task -> matchesStatus(
                            task, selectedFilter))
                    .toList();

            taskList.getItems().setAll(filtered);

            long done = tasks.stream()
                    .filter(task ->
                            task.getStatus() == Task.Status.DONE)
                    .count();

            long late = tasks.stream()
                    .filter(task -> task.getDueDate() != null
                            && task.getDueDate()
                            .isBefore(LocalDate.now())
                            && task.getStatus() != Task.Status.DONE)
                    .count();

            taskSummary.setText(
                    tasks.size() + " tâche(s) • "
                            + done + " terminée(s) • "
                            + late + " en retard"
            );

        } catch (SQLException e) {
            showError("Impossible de charger les tâches.");
        }
    }

    private boolean matchesStatus(
            Task task,
            String selectedFilter
    ) {
        if (selectedFilter == null
                || selectedFilter.equals("Tous les statuts")) {
            return true;
        }

        return switch (selectedFilter) {
            case "À faire" ->
                    task.getStatus() == Task.Status.TODO;
            case "En cours" ->
                    task.getStatus() == Task.Status.IN_PROGRESS;
            case "Terminées" ->
                    task.getStatus() == Task.Status.DONE;
            default -> true;
        };
    }

    private void fillForm(Task task) {
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
    }

    private Task.Status getSelectedTaskStatus() {
        Task selected = taskList.getSelectionModel()
                .getSelectedItem();

        return selected == null
                ? Task.Status.TODO
                : selected.getStatus();
    }

    private void clearForm() {
        editing = false;
        editingTaskId = 0;
        titleField.clear();
        descriptionField.clear();
        dueDatePicker.setValue(null);
        taskList.getSelectionModel().clearSelection();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NovaDeskt");
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
