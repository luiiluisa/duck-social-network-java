package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login-view.fxml"));
        stage.setTitle("Login - Duck Social Network");
        stage.setScene(new Scene(loader.load(), 420, 260));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
