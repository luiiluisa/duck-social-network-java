package ui.fx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import model.Duck;
import model.Enums.TipDuck;
import model.RaceEvent;
import model.User;
import network.DuckSocialNetwork;
import javafx.application.Platform;


import java.util.*;
import java.util.stream.Collectors;

public class RaceEventController {

    @FXML private BorderPane root;

    @FXML private Label lblMe;

    @FXML private TextField tfSearchDuck;
    @FXML private ListView<User> listDucks;
    @FXML private Label lblSelected;

    @FXML private TextField tfDistances;
    @FXML private TextArea taResults;

    @FXML private Button btnSubscribe;
    @FXML private Button btnUnsubscribe;

    private final DuckSocialNetwork app = DuckSocialNetwork.getInstance();
    private final RaceEvent race = new RaceEvent();

    @FXML
    public void initialize() {
        User me = app.getLoggedUser();
        lblMe.setText(me == null ? "Logged: -" : ("Logged: " + me.getUsername() + " (id=" + me.getId() + ")"));

        boolean isDuck = (me instanceof Duck);
        btnSubscribe.setDisable(!isDuck);
        btnUnsubscribe.setDisable(!isDuck);

        listDucks.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listDucks.setCellFactory(lv -> {
            ListCell<User> cell = new ListCell<>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText("DUCK | " + safe(item.getUsername()) + " | id=" + item.getId());
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (cell.isEmpty()) return;
                int idx = cell.getIndex();
                var sm = lv.getSelectionModel();
                if (sm.getSelectedIndices().contains(idx)) sm.clearSelection(idx);
                else sm.select(idx);
                e.consume();
                updateSelectedLabel();
            });

            return cell;
        });

        listDucks.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<User>) c -> updateSelectedLabel()
        );

        loadDucks();
        applySubscribedAutoSelection();
        updateSelectedLabel();
    }


    @FXML
    private void handleSearchDucks() {
        loadDucks();
        applySubscribedAutoSelection();
        updateSelectedLabel();
    }

    @FXML
    private void handleRefreshDucks() {
        loadDucks();
        applySubscribedAutoSelection();
        updateSelectedLabel();
    }

    @FXML
    private void handleClearSelection() {
        listDucks.getSelectionModel().clearSelection();
        updateSelectedLabel();
    }

    @FXML
    private void handleClearDistances() {
        tfDistances.clear();
    }

    @FXML
    private void handleSubscribe() {
        User me = app.getLoggedUser();
        if (!(me instanceof Duck d)) {
            showError("Doar rațele se pot abona.");
            return;
        }

        boolean ok = app.getRaceEventService().subscribe(d);

        System.out.println("SUB ok=" + ok
                + " id=" + d.getId()
                + " subs=" + app.getRaceEventService().subscribedIds()
                + " service@" + System.identityHashCode(app.getRaceEventService()));

        if (!ok) {
            showError("Ești deja abonată.");
            return;
        }

        loadDucks();
        applySubscribedAutoSelection();
        selectUserById(d.getId());
        updateSelectedLabel();

        new Alert(Alert.AlertType.INFORMATION, "Te-ai abonat la RaceEvent.").showAndWait();
    }

    @FXML
    private void handleUnsubscribe() {
        User me = app.getLoggedUser();
        if (!(me instanceof Duck d)) {
            showError("Doar rațele se pot dezabona.");
            return;
        }

        System.out.println("UNSUB before id=" + d.getId()
                + " subs=" + app.getRaceEventService().subscribedIds()
                + " service@" + System.identityHashCode(app.getRaceEventService()));

        boolean ok = app.getRaceEventService().unsubscribe(d);


        System.out.println("UNSUB ok=" + ok
                + " id=" + d.getId()
                + " subs=" + app.getRaceEventService().subscribedIds()
                + " service@" + System.identityHashCode(app.getRaceEventService()));

        if (!ok) {
            showError("Nu ești abonată (sau ai deschis altă instanță a aplicației).");
            return;
        }

        // scoate selecția raței logate din lista
        listDucks.getSelectionModel().clearSelection();
        loadDucks();
        applySubscribedAutoSelection();
        updateSelectedLabel();

        new Alert(Alert.AlertType.INFORMATION, "Te-ai dezabonat de la RaceEvent.").showAndWait();
    }


    @FXML
    private void handleStartRace() {
        var selected = listDucks.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Selectează cel puțin o rață SWIMMING.");
            return;
        }

        List<Duck> ducks = selected.stream()
                .filter(u -> u instanceof Duck)
                .map(u -> (Duck) u)
                .collect(Collectors.toList());

        List<Double> distances = parseDistances(tfDistances.getText());
        if (distances.isEmpty()) {
            showError("Introdu distanțe valide (> 0). Exemplu: 100 80 60");
            return;
        }

        int M = distances.size();
        if (ducks.size() != M) {
            showError("Trebuie EXACT " + M + " rațe selectate (câte distanțe).\n" +
                    "Selectate: " + ducks.size() + ", Distanțe: " + M);
            return;
        }

        // disable butoane
        setUiBusy(true);
        taResults.setText("Se rulează cursa...");

        app.getRaceEventService()
                .runRaceAsync(ducks, distances)
                .thenAccept(lines -> Platform.runLater(() -> {
                    setUiBusy(false);

                    String resultText = String.join("\n", lines);
                    taResults.setText(resultText);

                    // popup simplu
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Race Result");
                    a.setHeaderText("Rezultatul cursei");

                    TextArea area = new TextArea(resultText);
                    area.setEditable(false);
                    area.setWrapText(true);
                    area.setPrefWidth(650);
                    area.setPrefHeight(350);

                    a.getDialogPane().setContent(area);
                    a.getDialogPane().setExpandableContent(null);
                    a.showAndWait();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setUiBusy(false);
                        taResults.clear();
                        showError("Eroare la simulare: " + ex.getMessage());
                    });
                    return null;
                });
    }


    private void loadDucks() {
        User me = app.getLoggedUser();
        List<User> all = app.getUserService().findAll();

        String q = tfSearchDuck.getText();
        q = (q == null) ? "" : q.trim().toLowerCase();


        List<Long> selectedIds = listDucks.getSelectionModel().getSelectedItems().stream()
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .toList();

        List<User> ducks = new ArrayList<>();
        for (User u : all) {
            if (!(u instanceof Duck d)) continue;
            if (d.getId() == null) continue;


            if (d.getTip() != TipDuck.SWIMMING) continue;
            if (d.getViteza() <= 0) continue;


            if (!q.isEmpty()) {
                String un = safe(d.getUsername()).toLowerCase();
                String em = safe(d.getEmail()).toLowerCase();
                if (!(un.contains(q) || em.contains(q))) continue;
            }

            ducks.add(d);
        }

        listDucks.getItems().setAll(ducks);

        listDucks.getSelectionModel().clearSelection();
        for (int i = 0; i < ducks.size(); i++) {
            User u = ducks.get(i);
            if (u != null && u.getId() != null && selectedIds.contains(u.getId())) {
                listDucks.getSelectionModel().select(i);
            }
        }
    }

    private void applySubscribedAutoSelection() {
        List<Long> subs = app.getRaceEventService().subscribedIds();
        if (subs.isEmpty()) return;

        for (int i = 0; i < listDucks.getItems().size(); i++) {
            User u = listDucks.getItems().get(i);
            if (u != null && u.getId() != null && subs.contains(u.getId())) {
                listDucks.getSelectionModel().select(i);
            }
        }
    }

    private void selectUserById(Long id) {
        if (id == null) return;
        for (int i = 0; i < listDucks.getItems().size(); i++) {
            User u = listDucks.getItems().get(i);
            if (u != null && Objects.equals(u.getId(), id)) {
                listDucks.getSelectionModel().select(i);
                return;
            }
        }
    }

    private void updateSelectedLabel() {
        var selected = listDucks.getSelectionModel().getSelectedItems();
        int n = (selected == null) ? 0 : selected.size();

        StringBuilder sb = new StringBuilder("Selectate: ").append(n);
        if (selected != null && !selected.isEmpty()) {
            sb.append(" -> ");
            int shown = 0;
            for (User u : selected) {
                if (u == null) continue;
                if (shown >= 6) { sb.append("..."); break; }
                sb.append(safe(u.getUsername())).append(", ");
                shown++;
            }
            if (sb.toString().endsWith(", ")) sb.setLength(sb.length() - 2);
        }

        lblSelected.setText(sb.toString());
    }

    private List<Double> parseDistances(String raw) {
        List<Double> out = new ArrayList<>();
        if (raw == null) return out;

        String[] toks = raw.trim().split("[,;\\s]+");
        for (String t : toks) {
            if (t == null || t.isBlank()) continue;
            try {
                double d = Double.parseDouble(t.trim());
                if (d > 0) out.add(d);
            } catch (Exception ignored) {}
        }
        return out;
    }

    private void setUiBusy(boolean busy) {
        listDucks.setDisable(busy);
        tfDistances.setDisable(busy);
        btnSubscribe.setDisable(busy || !(app.getLoggedUser() instanceof Duck));
        btnUnsubscribe.setDisable(busy || !(app.getLoggedUser() instanceof Duck));
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
