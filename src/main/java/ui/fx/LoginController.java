package ui.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import network.DuckSocialNetwork;

public class LoginController {

    @FXML private TextField tfUser;
    @FXML private PasswordField pfPass;
    @FXML private Label lblError;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();

    @FXML
    private void handleLogin() {
        //citesti input
        String userOrEmail = tfUser.getText() == null ? "" : tfUser.getText().trim();
        String pass = pfPass.getText() == null ? "" : pfPass.getText();

        //validare
        if (userOrEmail.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completeaza username/email si parola.");
            return;
        }

        User logged = app.getUserService().login(userOrEmail, pass);
        if (logged == null) {
            lblError.setText("Date invalide. Incearca din nou.");
            return;
        }

        app.setLoggedUser(logged); //salvez userul logat
        openMainMenu();
    }

    /**butonul de exit*/
    @FXML
    private void handleExit() {
        Stage stage = (Stage) tfUser.getScene().getWindow();
        stage.close();
    }

    private void openMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main-menu.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            Stage stage = (Stage) tfUser.getScene().getWindow();
            stage.setTitle("Duck Social Network");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Eroare la deschiderea meniului.");
        }
    }
}
