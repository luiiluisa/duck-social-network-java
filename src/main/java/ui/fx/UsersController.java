package ui.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import model.*;
import network.DuckSocialNetwork;

import java.util.List;

/**
 * GUI pentru:
 *  - listare + filtrare users (ALL / PERSON / DUCK)
 *  - paginare pentru DUCK (Prev/Next)
 *  - afisare detalii specifice (nume/empatie sau tip/viteza/rezistenta)
 *  - adaugare user (PERSON / DUCK)
 *  - stergere user selectat din tabel
 */
public class UsersController {


    @FXML private ComboBox<String> comboUserType;

    @FXML private TableView<User> tableUsers;

    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colType;


    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colOccupation;
    @FXML private TableColumn<User, String> colEmpathy;


    @FXML private TableColumn<User, String> colDuckKind;
    @FXML private TableColumn<User, String> colSpeed;
    @FXML private TableColumn<User, String> colStamina;

    private final ObservableList<User> model = FXCollections.observableArrayList();



    @FXML private ComboBox<String> comboType;     // PERSON / DUCK
    @FXML private GridPane formGrid;

    private TextField tfUsername, tfEmail, tfPassword;
    private TextField tfLastName, tfFirstName, tfOccupation, tfEmpathy;
    private ComboBox<String> cbDuckType;
    private TextField tfViteza, tfRezistenta;


    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label lblPage;

    private int page = 0;
    private static final int PAGE_SIZE = 5;

    private DuckSocialNetwork app;

    @FXML
    public void initialize() {
        app = DuckSocialNetwork.getInstance();


        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colType.setCellValueFactory(cd -> {
            User u = cd.getValue();
            String t = (u instanceof Duck) ? "DUCK" : "PERSON";
            return new SimpleStringProperty(t);
        });

        colLastName.setCellValueFactory(cd -> {
            User u = cd.getValue();
            return (u instanceof Person p) ? new SimpleStringProperty(nv(p.getNume())) : new SimpleStringProperty("");
        });

        colFirstName.setCellValueFactory(cd -> {
            User u = cd.getValue();
            return (u instanceof Person p) ? new SimpleStringProperty(nv(p.getPrenume())) : new SimpleStringProperty("");
        });

        colOccupation.setCellValueFactory(cd -> {
            User u = cd.getValue();
            return (u instanceof Person p) ? new SimpleStringProperty(nv(p.getOcupatie())) : new SimpleStringProperty("");
        });

        colEmpathy.setCellValueFactory(cd -> {
            User u = cd.getValue();
            return (u instanceof Person p) ? new SimpleStringProperty(String.valueOf(p.getNivelEmpatie())) : new SimpleStringProperty("");
        });

        colDuckKind.setCellValueFactory(cd -> {
            User u = cd.getValue();
            if (u instanceof Duck d) {
                return new SimpleStringProperty(d.getTip() == null ? "" : d.getTip().name());
            }
            return new SimpleStringProperty("");
        });

        colSpeed.setCellValueFactory(cd -> {
            User u = cd.getValue();
            if (u instanceof Duck d) return new SimpleStringProperty(String.valueOf(d.getViteza()));
            return new SimpleStringProperty("");
        });

        colStamina.setCellValueFactory(cd -> {
            User u = cd.getValue();
            if (u instanceof Duck d) return new SimpleStringProperty(String.valueOf(d.getRezistenta()));
            return new SimpleStringProperty("");
        });

        tableUsers.setItems(model);


        comboUserType.getItems().setAll("ALL", "PERSON", "DUCK");
        comboUserType.setValue("ALL");
        comboUserType.valueProperty().addListener((obs, oldV, newV) -> {
            page = 0;      // când schimb filtrul, revin la pagina 0
            reloadData();
        });

        comboType.getItems().setAll("PERSON", "DUCK");
        comboType.setValue("PERSON");
        comboType.setOnAction(e -> {
            if ("PERSON".equals(comboType.getValue())) showPersonForm();
            else showDuckForm();
        });

        showPersonForm();

        // initial load
        reloadData();
    }



    private void reloadData() {
        String selected = comboUserType.getValue();
        final String filter = (selected == null) ? "ALL" : selected;

        updateColumnVisibility(filter);

        // Paginare DOAR pentru DUCK
        if ("DUCK".equalsIgnoreCase(filter)) {
            loadDuckPage();
            setPaginationVisible(true);
            return;
        }

        // ALL / PERSON: fara pag.
        var userService = app.getUserService();
        List<User> all = userService.findAll();

        List<User> filtered = all.stream()
                .filter(u -> {
                    if ("ALL".equalsIgnoreCase(filter)) return true;
                    if ("PERSON".equalsIgnoreCase(filter)) return !(u instanceof Duck);
                    return true;
                })
                .toList();

        model.setAll(filtered);
        setPaginationVisible(false);
    }

    /**
     * Încarcă pagina curentă de DUCK din BD și actualizează UI-ul de paginare.
     */
    private void loadDuckPage() {
        var userService = app.getUserService();

        List<Duck> ducksPage = userService.findDucksPage(page, PAGE_SIZE);
        model.setAll(ducksPage); // Duck extends User -> ok

        int total = userService.countDucks();
        int maxPage = (total == 0) ? 1 : ((total - 1) / PAGE_SIZE) + 1;

        // dacă cumva ai ramas pe o pagina prea mare (dupa delete), te aducem inapoi
        if (page > maxPage - 1) {
            page = Math.max(0, maxPage - 1);
            ducksPage = userService.findDucksPage(page, PAGE_SIZE);
            model.setAll(ducksPage);
        }

        btnPrev.setDisable(page == 0);
        btnNext.setDisable(page >= maxPage - 1);
        lblPage.setText("Page " + (page + 1) + " / " + maxPage);
    }

    private void setPaginationVisible(boolean visible) {
        // dacă în FXML sunt într-un HBox/VBox, poți ascunde și containerul;
        // aici facem simplu pe butoane/label
        btnPrev.setVisible(visible);
        btnNext.setVisible(visible);
        lblPage.setVisible(visible);

        btnPrev.setManaged(visible);
        btnNext.setManaged(visible);
        lblPage.setManaged(visible);

        if (!visible) {
            btnPrev.setDisable(true);
            btnNext.setDisable(true);
            lblPage.setText("");
        }
    }

    // ==========================
    // PAGINATION EVENTS
    // ==========================

    @FXML
    private void nextPage() {
        if (!"DUCK".equalsIgnoreCase(comboUserType.getValue())) return;
        page++;
        loadDuckPage();
    }

    @FXML
    private void prevPage() {
        if (!"DUCK".equalsIgnoreCase(comboUserType.getValue())) return;
        if (page > 0) {
            page--;
            loadDuckPage();
        }
    }

    @FXML
    private void handleRefresh() {
        page = 0;      // opțional: revii la prima pagină
        reloadData();
    }


    // ==========================
    // COLUMN VISIBILITY
    // ==========================

    private void updateColumnVisibility(String filter) {
        if (filter == null) filter = "ALL";

        switch (filter.toUpperCase()) {
            case "PERSON" -> {
                // person columns ON
                colLastName.setVisible(true);
                colFirstName.setVisible(true);
                colOccupation.setVisible(true);
                colEmpathy.setVisible(true);

                // duck columns OFF
                colDuckKind.setVisible(false);
                colSpeed.setVisible(false);
                colStamina.setVisible(false);
            }
            case "DUCK" -> {
                // person columns OFF
                colLastName.setVisible(false);
                colFirstName.setVisible(false);
                colOccupation.setVisible(false);
                colEmpathy.setVisible(false);

                // duck columns ON
                colDuckKind.setVisible(true);
                colSpeed.setVisible(true);
                colStamina.setVisible(true);
            }
            default -> { // ALL
                // show only base columns
                colLastName.setVisible(false);
                colFirstName.setVisible(false);
                colOccupation.setVisible(false);
                colEmpathy.setVisible(false);

                colDuckKind.setVisible(false);
                colSpeed.setVisible(false);
                colStamina.setVisible(false);
            }
        }
    }

    // ==========================
    // FORM UI (PERSON / DUCK)
    // ==========================

    private void showPersonForm() {
        formGrid.getChildren().clear();

        tfUsername = new TextField();
        tfEmail = new TextField();
        tfPassword = new TextField();

        tfLastName = new TextField();
        tfFirstName = new TextField();
        tfOccupation = new TextField();
        tfEmpathy = new TextField();

        addRow("Username:", tfUsername, 0);
        addRow("Email:", tfEmail, 1);
        addRow("Password:", tfPassword, 2);
        addRow("Nume:", tfLastName, 3);
        addRow("Prenume:", tfFirstName, 4);
        addRow("Ocupatie:", tfOccupation, 5);
        addRow("Empatie (0-100):", tfEmpathy, 6);
    }

    private void showDuckForm() {
        formGrid.getChildren().clear();

        tfUsername = new TextField();
        tfEmail = new TextField();
        tfPassword = new TextField();

        cbDuckType = new ComboBox<>();
        cbDuckType.getItems().setAll("FLYING", "SWIMMING");
        cbDuckType.getSelectionModel().select("SWIMMING");

        tfViteza = new TextField();
        tfRezistenta = new TextField();

        addRow("Username:", tfUsername, 0);
        addRow("Email:", tfEmail, 1);
        addRow("Password:", tfPassword, 2);
        addRow("Tip rata:", cbDuckType, 3);
        addRow("Viteza:", tfViteza, 4);
        addRow("Rezistenta:", tfRezistenta, 5);
    }

    private void addRow(String text, Control field, int row) {
        formGrid.add(new Label(text), 0, row);
        formGrid.add(field, 1, row);
    }

    // ==========================
    // ADD / DELETE USER
    // ==========================

    @FXML
    private void addUser() {
        try {
            var userService = app.getUserService();

            if ("PERSON".equals(comboType.getValue())) {
                int empathy = Integer.parseInt(tfEmpathy.getText());

                Person p = new Person(
                        null,
                        tfUsername.getText(),
                        tfEmail.getText(),
                        tfPassword.getText(),
                        tfLastName.getText(),
                        tfFirstName.getText(),
                        null,
                        tfOccupation.getText(),
                        empathy
                );

                userService.addUser(p);

            } else {
                String typeStr = cbDuckType.getValue();
                double v = Double.parseDouble(tfViteza.getText());
                double r = Double.parseDouble(tfRezistenta.getText());

                Duck d;
                if ("FLYING".equals(typeStr)) {
                    d = new FlyingDuck(null, tfUsername.getText(), tfEmail.getText(), tfPassword.getText(), v, r);
                } else {
                    d = new SwimmingDuck(null, tfUsername.getText(), tfEmail.getText(), tfPassword.getText(), v, r);
                }

                userService.addUser(d);
            }

            showInfo("User added!");
            clearForm();

            // după add, reîncărcăm în funcție de filtrul curent
            reloadData();

        } catch (NumberFormatException ex) {
            showError("Valori numerice invalide (empatie, viteza sau rezistenta).");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        try {
            User selected = tableUsers.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Selectează mai întâi un user din tabel.");
                return;
            }

            Long id = selected.getId();
            if (id == null) {
                showError("User-ul selectat nu are ID valid.");
                return;
            }

            app.getUserService().removeUser(id);

            showInfo("User deleted!");
            reloadData();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ==========================
    // HELPERS
    // ==========================

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void clearForm() {
        if ("PERSON".equals(comboType.getValue())) {
            tfUsername.clear();
            tfEmail.clear();
            tfPassword.clear();
            tfLastName.clear();
            tfFirstName.clear();
            tfOccupation.clear();
            tfEmpathy.clear();
        } else {
            tfUsername.clear();
            tfEmail.clear();
            tfPassword.clear();
            tfViteza.clear();
            tfRezistenta.clear();
            if (cbDuckType != null) cbDuckType.getSelectionModel().select("SWIMMING");
        }
    }

    private static String nv(String s) { return s == null ? "" : s; }
}
