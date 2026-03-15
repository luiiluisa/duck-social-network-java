package ui.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Duck;
import model.Friendship;
import model.User;
import network.DuckSocialNetwork;
import service.FriendshipService;
import service.UserService;

import java.io.IOException;
import java.util.List;

/**
 * GUI pentru:
 *  - listare prietenii in tabel
 *  - adaugare prietenii
 *  - stergere prietenii pe baza randului selectat
 *  - deschidere fereastra separata pentru Communities
 */
public class FriendshipsController {


    @FXML
    private TableView<Friendship> tableFriendships;

    @FXML
    private TableColumn<Friendship, String> colFUser1Id;

    @FXML
    private TableColumn<Friendship, String> colFUser1Name;

    @FXML
    private TableColumn<Friendship, String> colFUser2Id;

    @FXML
    private TableColumn<Friendship, String> colFUser2Name;

    private final ObservableList<Friendship> friendshipsModel = FXCollections.observableArrayList();


    @FXML
    private ComboBox<User> comboAddUser1;
    @FXML
    private ComboBox<User> comboAddUser2;

    private DuckSocialNetwork app;
    private UserService userService;
    private FriendshipService friendshipService;

    @FXML
    public void initialize() {
        app = DuckSocialNetwork.getInstance();
        userService = app.getUserService();
        friendshipService = app.getFriendshipService();

        tableFriendships.setItems(friendshipsModel);

        colFUser1Id.setCellValueFactory(cd -> {
            Friendship f = cd.getValue();
            if (f == null || f.getUser1() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(String.valueOf(f.getUser1().getId()));
        });

        colFUser1Name.setCellValueFactory(cd -> {
            Friendship f = cd.getValue();
            if (f == null || f.getUser1() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(f.getUser1().getUsername());
        });

        colFUser2Id.setCellValueFactory(cd -> {
            Friendship f = cd.getValue();
            if (f == null || f.getUser2() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(String.valueOf(f.getUser2().getId()));
        });

        colFUser2Name.setCellValueFactory(cd -> {
            Friendship f = cd.getValue();
            if (f == null || f.getUser2() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(f.getUser2().getUsername());
        });

        loadUsersIntoCombos();
        setupComboRendering(comboAddUser1);
        setupComboRendering(comboAddUser2);

        loadFriendships();
    }

    private void loadUsersIntoCombos() {
        List<User> all = userService.findAll();
        comboAddUser1.getItems().setAll(all);
        comboAddUser2.getItems().setAll(all);
    }


    private void setupComboRendering(ComboBox<User> combo) {
        combo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getUsername());
                }
            }
        });

        //dupa ce selectez un user sa apara frumos in combobox
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getUsername());
                }
            }
        });
    }

    //punem friendship in tabela
    private void loadFriendships() {
        List<Friendship> list = friendshipService.findAllFriendships();
        friendshipsModel.setAll(list);
    }


    @FXML
    private void handleAddFriendship() {
        try {
            User u1 = comboAddUser1.getValue();
            User u2 = comboAddUser2.getValue();

            if (u1 == null || u2 == null) {
                showError("Selectează ambii utilizatori pentru a crea prietenia.");
                return;
            }
            if (u1.equals(u2)) {
                showError("Nu poți crea prietenie cu tine însuți.");
                return;
            }

            boolean ok = friendshipService.addFriend(u1.getId(), u2.getId());
            if (ok) {
                showInfo("Prietenie adăugată!");
                loadFriendships();
            } else {
                showError("Prietenia există deja sau nu a putut fi creată.");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    @FXML
    private void handleRemoveSelected() {
        try {
            Friendship f = tableFriendships.getSelectionModel().getSelectedItem();
            if (f == null) {
                showError("Selectează o prietenie din tabel pentru a o șterge.");
                return;
            }

            Long id1 = f.getUser1() != null ? f.getUser1().getId() : null;
            Long id2 = f.getUser2() != null ? f.getUser2().getId() : null;

            if (id1 == null || id2 == null) {
                showError("Prietenie invalidă (fără ID-uri).");
                return;
            }

            boolean ok = friendshipService.removeFriend(id1, id2);
            if (ok) {
                showInfo("Prietenie ștearsă!");
                loadFriendships();
            } else {
                showError("Nu s-a putut șterge prietenia.");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }



    @FXML
    private void handleOpenCommunities() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/communities-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Communities");
            stage.setScene(new Scene(loader.load(), 600, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Nu s-a putut deschide fereastra de comunități.");
        }
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
