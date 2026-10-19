package fr.novadeskt;

import fr.novadeskt.config.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            Database.initialize();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Impossible d'initialiser la base SQLite.", e);
        }

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/fxml/dashboard.fxml"));

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());

        stage.setTitle("NovaDeskt");
        stage.setMinWidth(700);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
