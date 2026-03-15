package ui.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import model.Message;
import model.ReplyMessage;
import model.User;
import network.DuckSocialNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessagesController {

    @FXML private BorderPane root;

    @FXML private Label lblMe;
    @FXML private Label lblChatWith;

    @FXML private TextField tfSearch;
    @FXML private ListView<User> listUsers;

    @FXML private ListView<String> listMessages;

    @FXML private Label lblReply;
    @FXML private TextField tfMessage;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();

    private ScheduledExecutorService scheduler;
    private Long lastMaxMsgId = -1L;

    // reply state
    private Message replyToMessage = null;

    @FXML
    public void initialize() {
        User me = app.getLoggedUser();
        lblMe.setText(me == null ? "Logged: -" : ("Logged: " + me.getUsername() + " (id=" + me.getId() + ")"));

        //cum va arata user
        listUsers.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getUsername() + " | " + item.getEmail() + " | id=" + item.getId());
            }
        });

        //apas pe user
        listUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            clearReply(); //reseteaza reply
            loadConversation(); //iaconversatiile
        });

        //pt reply
        listMessages.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> {
            int idx = (newV == null) ? -1 : newV.intValue();
            setReplyByIndex(idx);
        });


        loadUsers();
        loadConversation();


        startAutoRefresh();

        // stop thread cand se inchide fereastra
        Platform.runLater(() -> {
            var stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setOnHidden(e -> stopAutoRefresh());
        });
    }



    @FXML
    private void handleRefresh() {
        loadUsers();
        loadConversation();
    }

    @FXML
    private void handleSearch() {
        loadUsers();
    }

    @FXML
    private void clearReply() {
        replyToMessage = null;
        lblReply.setText("(none)");
        listMessages.getSelectionModel().clearSelection();
    }

    /** trimite un mesaj */
    @FXML
    private void handleSend() {
        User me = app.getLoggedUser();
        User other = listUsers.getSelectionModel().getSelectedItem();

        if (me == null) {
            showError("Nu ești logat.");
            return;
        }
        if (other == null) {
            showError("Selectează un user din stânga.");
            return;
        }

        String text = tfMessage.getText();
        if (text == null || text.isBlank()) return;
        //creaza lista de destinatari (1)
        try {

            List<User> to = List.of(other);

            Message m;
            if (replyToMessage != null) { //replymessage
                m = new ReplyMessage(null, me, to, text.trim(), null, replyToMessage);
            } else {
                m = new Message(null, me, to, text.trim(), null);
            }

            app.getMessageService().send(m);

            //golim textbox
            tfMessage.clear();
            clearReply();

            // dupa send , reincarcam conversatia
            loadConversation();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Eroare la trimitere mesaj.");
        }
    }



    /** lista de utilizatori ce pot trimite mesaje (stanga)*/
    private void loadUsers() {
        String q = tfSearch.getText(); //searchbar
        if (q == null) q = "";
        String query = q.trim().toLowerCase();

        User me = app.getLoggedUser();
        List<User> all = app.getUserService().findAll(); //lista completa de utilizatori

        List<User> filtered = new ArrayList<>();
        for (User u : all) {
            if (u == null || u.getId() == null) continue;

            // sare peste user logat
            if (me != null && Objects.equals(me.getId(), u.getId())) continue;

            if (query.isEmpty()) {
                filtered.add(u);
            } else {
                String usern = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
                String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
                if (usern.contains(query) || email.contains(query)) filtered.add(u);
            }
        }

        listUsers.getItems().setAll(filtered);
    }

    /**afisarea conversatiilor*/
    private void loadConversation() {
        User me = app.getLoggedUser();
        User other = listUsers.getSelectionModel().getSelectedItem();

        //nu ai selectat
        if (me == null || other == null) {
            lblChatWith.setText("Conversation: (select a user)");
            listMessages.getItems().clear();
            lastMaxMsgId = -1L;
            return;
        }

        lblChatWith.setText("Conversation with: " + other.getUsername() + " (id=" + other.getId() + ")");

        //luam conversatia
        List<Message> conv = app.getMessageService().conversation(me.getId(), other.getId());

        // daca a aparul un id nou->refresh
        lastMaxMsgId = conv.stream()
                .map(Message::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(-1L);

        // afișare
        List<String> lines = new ArrayList<>();
        int i = 1;
        for (Message m : conv) {
            String who = (m.getFrom() != null && m.getFrom().getId() != null && m.getFrom().getId().equals(me.getId()))
                    ? "Me"
                    : (m.getFrom() == null ? "?" : m.getFrom().getUsername());

            String reply = "";
            if (m instanceof ReplyMessage rm && rm.getRepliedTo() != null && rm.getRepliedTo().getId() != null) {
                reply = " (reply to #" + rm.getRepliedTo().getId() + ")";
            }

            String when = (m.getData() == null) ? "" : (" [" + m.getData() + "]");
            lines.add(i + ") " + who + when + ": " + m.getMessage() + reply);
            i++;
        }

        //aici afisam
        listMessages.getItems().setAll(lines);
    }


    /**rulare in paralel*/
    private void startAutoRefresh() {
        stopAutoRefresh(); //sa nu faci refresh pe 2 threaduri
        scheduler = Executors.newSingleThreadScheduledExecutor(); //creaza un thread

        //ruleaza bucata asta de cod periodic
        scheduler.scheduleAtFixedRate(() -> {
            try {
                User me = app.getLoggedUser();
                User other = (listUsers == null) ? null : listUsers.getSelectionModel().getSelectedItem();
                if (me == null || other == null) return;

                List<Message> conv = app.getMessageService().conversation(me.getId(), other.getId()); //conversatia

                Long maxId = conv.stream()
                        .map(Message::getId)
                        .filter(Objects::nonNull)
                        .max(Long::compareTo)
                        .orElse(-1L);

                if (!Objects.equals(maxId, lastMaxMsgId)) {
                    Platform.runLater(this::loadConversation);
                }
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.SECONDS);
    }

    ///inchid un thread
    private void stopAutoRefresh() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**messagereply*/
    private void setReplyByIndex(int idx) {
         User me = app.getLoggedUser();
        User other = listUsers.getSelectionModel().getSelectedItem();
        if (me == null || other == null) return;
        if (idx < 0) return;

        List<Message> conv = app.getMessageService().conversation(me.getId(), other.getId()); //avem nevoie de obiectul message
        if (idx >= conv.size()) return;

        replyToMessage = conv.get(idx);
        if (replyToMessage != null && replyToMessage.getId() != null) {
            lblReply.setText("#" + replyToMessage.getId());
        } else {
            lblReply.setText("(none)");
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
