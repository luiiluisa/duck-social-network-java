package ui.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Duck;
import model.User;
import network.DuckSocialNetwork;
import service.FriendshipService;

import java.util.List;

/**
 * Fereastra pentru Communities:
 *  - numarul de comunitati
 *  - cea mai sociabila comunitate
 */
public class CommunitiesController {

    @FXML
    private Label labelNumCommunities;

    @FXML
    private TableView<User> tableCommunity;

    @FXML
    private TableColumn<User, Long> colCommId;

    @FXML
    private TableColumn<User, String> colCommUsername;

    @FXML
    private TableColumn<User, String> colCommEmail;

    @FXML
    private TableColumn<User, String> colCommType;

    private final ObservableList<User> communityModel = FXCollections.observableArrayList();

    private DuckSocialNetwork app;
    private FriendshipService friendshipService;

    @FXML
    public void initialize() {
        app = DuckSocialNetwork.getInstance();
        friendshipService = app.getFriendshipService();

        colCommId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCommUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCommEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCommType.setCellValueFactory(cd -> {
            User u = cd.getValue();
            String t = (u instanceof Duck) ? "DUCK" : "PERSON";
            return new SimpleStringProperty(t);
        });

        tableCommunity.setItems(communityModel);
    }

    //nr de comunitati
    @FXML
    private void handleShowNumCommunities() {
        int nr = friendshipService.countCommunities();
        labelNumCommunities.setText(String.valueOf(nr));
    }

    //cea mai sociabila comunitatee
    @FXML
    private void handleShowMostSociable() {
        List<User> best = friendshipService.mostSociableCommunity();
        communityModel.setAll(best);
    }
}
