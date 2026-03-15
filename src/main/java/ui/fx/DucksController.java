package ui.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Duck;
import model.Enums.TipDuck;
import model.User;
import network.DuckSocialNetwork;

import java.util.List;

public class DucksController {

    @FXML
    private ComboBox<String> comboDuckType;

    @FXML
    private TableView<Duck> tableDucks;

    @FXML
    private TableColumn<Duck, Long> colId;

    @FXML
    private TableColumn<Duck, String> colUser;

    @FXML
    private TableColumn<Duck, String> colType;

    @FXML
    private TableColumn<Duck, Double> colSpeed;

    @FXML
    private TableColumn<Duck, Double> colStamina;

    @FXML
    private Button btnPrev;

    @FXML
    private Button btnNext;

    @FXML
    private Label lblPage;


    private final ObservableList<Duck> model = FXCollections.observableArrayList();

    // paginare
    private int page = 0;
    private final int PAGE_SIZE = 5;

    private DuckSocialNetwork app;

    @FXML
    public void initialize() {
        app = DuckSocialNetwork.getInstance();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSpeed.setCellValueFactory(new PropertyValueFactory<>("viteza"));
        colStamina.setCellValueFactory(new PropertyValueFactory<>("rezistenta"));

        colType.setCellValueFactory(cd -> {
            Duck d = cd.getValue();
            String txt = (d.getTip() == null) ? "-" : d.getTip().name();
            return new SimpleStringProperty(txt);
        });

        tableDucks.setItems(model);


        comboDuckType.getItems().addAll("ALL", "FLYING", "SWIMMING");
        comboDuckType.setValue("ALL");

        //filtrarea este dinamica
        comboDuckType.valueProperty().addListener((obs, oldVal, newVal) -> {
            page = 0;       // cand schimb filtrul, revin la pagina 0
            loadPage();
        });


        loadPage();
    }

    /**
     * incarca pagina curenta, ținand cont de filtrul selectat.
     */
    private void loadPage() {
        var userService = app.getUserService();

        List<Duck> ducksPage = userService.findDucksPage(page, PAGE_SIZE); //page-nrpag curente

        //filtram
        String selected = comboDuckType.getValue();
        final String filter = (selected == null) ? "ALL" : selected;

        var filtered = ducksPage.stream()
                .filter(d -> {
                    if ("ALL".equalsIgnoreCase(filter)) return true;
                    if ("FLYING".equalsIgnoreCase(filter) && d.getTip() == TipDuck.FLYING) return true;
                    if ("SWIMMING".equalsIgnoreCase(filter) && d.getTip() == TipDuck.SWIMMING) return true;
                    return false;
                })
                .toList();

        model.setAll(filtered);

        int total = userService.countDucks();

        //recalcularea paginilor
        if (!"ALL".equalsIgnoreCase(filter)) {
            total = (int) userService.findAll().stream()
                    .filter(u -> u instanceof Duck)
                    .map(u -> (Duck) u)
                    .filter(d -> {
                        if ("FLYING".equalsIgnoreCase(filter)) return d.getTip() == TipDuck.FLYING;
                        if ("SWIMMING".equalsIgnoreCase(filter)) return d.getTip() == TipDuck.SWIMMING;
                        return true;
                    })
                    .count();
        }

        //cate pagini ne trebuie
        int maxPage = (total == 0) ? 1 : ((total - 1) / PAGE_SIZE) + 1;

        //dezactivam butoatele daca e cazul
        btnPrev.setDisable(page == 0);
        btnNext.setDisable(page >= maxPage - 1);


        lblPage.setText("Page " + (page + 1) + " / " + maxPage);
    }

    @FXML
    private void nextPage() {
        page++;
        loadPage();
    }

    @FXML
    private void prevPage() {
        if (page > 0) {
            page--;
            loadPage();
        }
    }
}
