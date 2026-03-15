package ui.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import network.DuckSocialNetwork;

public class MainMenuController {

    @FXML private BorderPane root;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();


    private void openWindow(String fxml, String title, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load(), w, h));

            Stage owner = (Stage) root.getScene().getWindow();
            stage.initOwner(owner);
            stage.initModality(Modality.NONE);

            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Nu s-a putut deschide: " + title);
        }
    }

    private boolean ensureLogged() {
        if (app.getLoggedUser() == null) {
            showError("Nu ești logat.");
            return false;
        }
        return true;
    }

    @FXML
    private void openUsers() {
        if (!ensureLogged()) return;
        openWindow("/ui/users-view.fxml", "Users", 800, 500);
    }

    @FXML
    private void openFriendships() {
        if (!ensureLogged()) return;
        openWindow("/ui/friendships-view.fxml", "Friendships", 900, 600);
    }

    @FXML
    private void openDucks() {
        if (!ensureLogged()) return;
        openWindow("/ui/ducks-view.fxml", "Ducks", 900, 600);
    }



    @FXML
    private void openMessages() {
        if (!ensureLogged()) return;
        openWindow("/ui/messages-view.fxml", "Messaging", 900, 600);
    }

    @FXML
    private void openFriendRequests() {
        if (!ensureLogged()) return;
        openWindow("/ui/friend-requests-view.fxml", "Friend Requests", 900, 600);
    }

    @FXML
    private void openRaceEvent() {
        if (!ensureLogged()) return;
        openWindow("/ui/raceevent-view.fxml", "RaceEvent", 900, 600);
    }

    @FXML
    private void openProfile() {
        if (!ensureLogged()) return;
        openWindow("/ui/profile-view.fxml", "Profile", 900, 600);
    }


    @FXML
    private void handleLogout() {
        try {
            // scoatem userul logat
            app.setLoggedUser(null);

            // înapoi la login (în aceeași fereastră principală)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Eroare la logout.");
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
