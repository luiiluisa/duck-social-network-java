package ui.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import model.FriendRequest;
import model.User;
import network.DuckSocialNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendRequestsController {

    @FXML private BorderPane root;

    @FXML private Label lblMe;

    @FXML private TextField tfSearch;
    @FXML private ListView<User> listUsers;

    @FXML private ListView<FriendRequest> listPending;

    @FXML private ListView<User> listFriends;

    @FXML private Label lblSendInfo;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();

    private ScheduledExecutorService scheduler;


    private final java.util.Set<Long> seenPendingIds = new java.util.HashSet<>();
    private boolean pendingInitialized = false;


    @FXML
    public void initialize() {
        User me = app.getLoggedUser();
        lblMe.setText(me == null ? "Logged: -" : ("Logged: " + me.getUsername() + " (id=" + me.getId() + ")"));

        //user list
        listUsers.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getUsername() + " | " + item.getEmail() + " | id=" + item.getId());
            }
        });

        //pending list
        listPending.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(FriendRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String from = item.getFrom() == null ? "?" : item.getFrom().getUsername();
                    setText("Request #" + item.getId() + " from " + from + " [" + item.getStatus() + "]");
                }
            }
        });

        // friends list
        listFriends.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getUsername() + " | id=" + item.getId());
            }
        });

        loadUsers();
        loadPending();
        loadFriends();

        startAutoRefresh();

        Platform.runLater(() -> {
            var stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setOnHidden(e -> stopAutoRefresh());
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        loadPending();
        loadFriends();
    }

    @FXML
    private void handleSearch() {
        loadUsers();
    }

    @FXML
    private void handleSendRequest() {
        User me = app.getLoggedUser();
        User other = listUsers.getSelectionModel().getSelectedItem();

        if (me == null) { showError("Nu ești logat."); return; }
        if (other == null) { showError("Selectează un user."); return; }

        var fr = app.getFriendRequestService().sendRequest(me.getId(), other.getId());
        if (fr == null) {
            lblSendInfo.setText("Nu s-a putut trimite (deja există sau eroare).");
        } else {
            lblSendInfo.setText("Cerere trimisă către " + other.getUsername() + "!");
        }

        // refresh imediat
        loadPending();
        loadFriends();
    }

    @FXML
    private void handleAccept() {
        User me = app.getLoggedUser();
        FriendRequest selected = listPending.getSelectionModel().getSelectedItem();
        if (me == null) { showError("Nu ești logat."); return; }
        if (selected == null) { showError("Selectează o cerere."); return; }

        boolean ok = app.getFriendRequestService().accept(selected.getId(), me.getId());
        if (!ok) showError("Nu s-a putut accepta cererea.");
        loadPending();
        loadFriends();
        loadUsers();
    }

    @FXML
    private void handleReject() {
        User me = app.getLoggedUser();
        FriendRequest selected = listPending.getSelectionModel().getSelectedItem();
        if (me == null) { showError("Nu ești logat."); return; }
        if (selected == null) { showError("Selectează o cerere."); return; }

        boolean ok = app.getFriendRequestService().reject(selected.getId(), me.getId());
        if (!ok) showError("Nu s-a putut respinge cererea.");
        loadPending();
        loadFriends();
        loadUsers();
    }

    private void loadUsers() {
        // memoreaza selectia curenta
        User selected = listUsers.getSelectionModel().getSelectedItem();
        Long selectedId = (selected == null) ? null : selected.getId();

        User me = app.getLoggedUser();
        List<User> all = app.getUserService().findAll();

        String q = tfSearch.getText();
        q = (q == null) ? "" : q.trim().toLowerCase();

        // prietenii mei
        List<User> myFriends = (me == null)
                ? List.of()
                : app.getFriendshipService().friendsOf(me.getId());

        // cereri primite
        List<FriendRequest> myPending = (me == null)
                ? List.of()
                : app.getFriendRequestService().pendingFor(me.getId());

        List<User> filtered = new ArrayList<>();
        for (User u : all) {
            if (u == null || u.getId() == null) continue;

            // doar persoane
            if (!(u instanceof model.Person)) continue;

            // nu arat pe mine
            if (me != null && Objects.equals(me.getId(), u.getId())) continue;

            // nu arat userii deja prieteni cu mine
            boolean isAlreadyFriend = myFriends.stream()
                    .anyMatch(f -> Objects.equals(f.getId(), u.getId()));
            if (isAlreadyFriend) continue;

            // mi-au cerut deja cerere de pending
            boolean hasPendingFromThatUser = myPending.stream().anyMatch(r ->
                    r.getFrom() != null && Objects.equals(r.getFrom().getId(), u.getId())
            );
            if (hasPendingFromThatUser) continue;

            // filtrare dupa search
            if (q.isEmpty()) {
                filtered.add(u);
            } else {
                String un = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
                String em = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
                if (un.contains(q) || em.contains(q)) filtered.add(u);
            }
        }

        //repopulează lista
        listUsers.getItems().setAll(filtered);

        // reselecteaza ce era selectat inainte
        if (selectedId != null) {
            for (int i = 0; i < filtered.size(); i++) {
                if (Objects.equals(filtered.get(i).getId(), selectedId)) {
                    listUsers.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }



    private void loadPending() {
        // memoreaza selecția curentă
        FriendRequest selected = listPending.getSelectionModel().getSelectedItem();
        Long selectedId = (selected == null) ? null : selected.getId();

        // user logat
        User me = app.getLoggedUser();
        if (me == null) {
            listPending.getItems().clear();
            return;
        }

        // ia cererile PENDING
        List<FriendRequest> reqs = app.getFriendRequestService().pendingFor(me.getId());

        // cereri noi (doar după prima încărcare)
        if (!pendingInitialized) {
            for (FriendRequest r : reqs) {
                if (r != null && r.getId() != null) seenPendingIds.add(r.getId());
            }
            pendingInitialized = true;
        } else {
            for (FriendRequest r : reqs) {
                if (r == null || r.getId() == null) continue;

                if (!seenPendingIds.contains(r.getId())) {
                    seenPendingIds.add(r.getId());

                    String from = (r.getFrom() == null) ? "cineva" : r.getFrom().getUsername();
                    showInfo("Ai primit o cerere de prietenie de la " + from + "!");
                }
            }
        }

        listPending.getItems().setAll(reqs);

        if (selectedId != null) {
            for (int i = 0; i < reqs.size(); i++) {
                if (Objects.equals(reqs.get(i).getId(), selectedId)) {
                    listPending.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }


    private void loadFriends() {
        //memoreaza selectia curenta
        User selected = listFriends.getSelectionModel().getSelectedItem();
        Long selectedId = (selected == null) ? null : selected.getId();

        // user logat
        User me = app.getLoggedUser();
        if (me == null) {
            listFriends.getItems().clear();
            return;
        }

        // reîncarcă lista de prieteni
        List<User> friends = app.getFriendshipService().friendsOf(me.getId());

        // repopulează
        listFriends.getItems().setAll(friends);

        // reselectează ce era selectat înainte
        if (selectedId != null) {
            for (int i = 0; i < friends.size(); i++) {
                if (Objects.equals(friends.get(i).getId(), selectedId)) {
                    listFriends.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }



    @FXML
    private void handleUnfriend() {
        User me = app.getLoggedUser();
        User selectedFriend = listFriends.getSelectionModel().getSelectedItem();

        if (me == null) {
            showError("Nu ești logat.");
            return;
        }
        if (selectedFriend == null) {
            showError("Selectează un prieten din lista 'My Friends'.");
            return;
        }

        // confirmare
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Unfriend");
        confirm.setHeaderText(null);
        confirm.setContentText("Sigur vrei să îl ștergi pe " + selectedFriend.getUsername() + " din prieteni?");

        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        boolean ok = app.getFriendshipService().removeFriend(me.getId(), selectedFriend.getId());
        if (!ok) {
            showError("Nu s-a putut șterge prietenia.");
            return;
        }


        loadFriends();  // dispare din My Friends
        loadUsers();    // reapare în Send Friend Request
    }


    private void startAutoRefresh() {
        stopAutoRefresh();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                User me = app.getLoggedUser();
                if (me == null) return;

                // live update la B si A
                Platform.runLater(() -> {
                    loadPending();
                    loadFriends();
                    loadUsers();
                });

            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Notificare");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
