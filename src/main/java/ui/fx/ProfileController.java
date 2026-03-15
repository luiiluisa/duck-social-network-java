package ui.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Duck;
import model.Person;
import model.User;
import network.DuckSocialNetwork;

public class ProfileController {

    @FXML private Label lblAvatar;
    @FXML private Label lblUsername;

    @FXML private HBox boxSocialStats;
    @FXML private Label lblFriends;
    @FXML private Label lblPending;

    @FXML private Label lblName;
    @FXML private Label lblBio;
    @FXML private Label lblEmail;

    @FXML private Button btnFriendRequests;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();

    @FXML
    public void initialize() {
        loadProfile();
    }

    @FXML
    private void handleRefresh() {
        loadProfile();
    }


    private void openWindow(String fxml, String title, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load(), w, h));


            Stage owner = null;
            if (lblUsername != null && lblUsername.getScene() != null) {
                owner = (Stage) lblUsername.getScene().getWindow();
            }
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.NONE);
            }

            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Nu s-a putut deschide: " + title);
        }
    }

    @FXML
    private void openFriendRequests() {
        // butonul oricum e ascuns pentru Duck
        openWindow("/ui/friend-requests-view.fxml", "Friend Requests", 900, 600);
    }

    @FXML
    private void openMessages() {
        openWindow("/ui/messages-view.fxml", "Messages", 900, 600);
    }

    @FXML
    private void openRaceEvent() {
        openWindow("/ui/raceevent-view.fxml", "RaceEvent", 900, 600);
    }

    //logica

    private void loadProfile() {
        User me = app.getLoggedUser();

        if (me == null) {
            lblUsername.setText("@-");
            lblName.setText("-");
            lblBio.setText("Nu e nimeni logat.");
            lblEmail.setText("-");
            hideSocialArea(true);
            return;
        }

        lblUsername.setText("@" + safe(me.getUsername()));

        boolean isDuck = (me instanceof Duck);

        // avatar
        lblAvatar.setText(isDuck ? "🦆" : "👤");

        // email simplu (fără mailto)
        lblEmail.setText(safe(me.getEmail()));

        if (me instanceof Person p) {
            String full = (safe(p.getNume()) + " " + safe(p.getPrenume())).trim();
            lblName.setText(full.isBlank() ? "Person" : full);
            lblBio.setText("Ocupație: " + safe(p.getOcupatie()) + " | Empatie: " + p.getNivelEmpatie());

            // Person -> arătăm social + încărcăm din BD
            hideSocialArea(false);

            int friendsCount = 0;
            int pendingCount = 0;
            try {
                friendsCount = app.getFriendshipService().friendsOf(me.getId()).size();
            } catch (Exception ignored) {}
            try {
                pendingCount = app.getFriendRequestService().pendingFor(me.getId()).size();
            } catch (Exception ignored) {}

            lblFriends.setText("Prieteni: " + friendsCount);
            lblPending.setText("Cereri PENDING: " + pendingCount);

        } else if (me instanceof Duck d) {
            // Duck -> ascundem social + buton Friend Requests
            hideSocialArea(true);

            lblName.setText("Duck");
            lblBio.setText("Tip: " + d.getTip()
                    + " | Viteză: " + d.getViteza()
                    + " | Rezistență: " + d.getRezistenta());

        } else {
            // fallback
            hideSocialArea(true);
            lblName.setText("User");
            lblBio.setText("Email: " + safe(me.getEmail()));
        }
    }

    private void hideSocialArea(boolean hide) {
        if (boxSocialStats != null) {
            boxSocialStats.setVisible(!hide);
            boxSocialStats.setManaged(!hide);
        }
        if (btnFriendRequests != null) {
            btnFriendRequests.setVisible(!hide);
            btnFriendRequests.setManaged(!hide);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
