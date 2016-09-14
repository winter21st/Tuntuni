/*
 * Copyright 2016 Sudipto Chandra.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tuntuni.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.tuntuni.Core;
import org.tuntuni.connection.Client;

/**
 * The controller for the main scene of this application.
 * <p>
 * It has two parts. One is side-bar. Another is the body. The side-bar is
 * positioned at the right side and collapsible. The body is divided into two
 * parts. One for text messaging. Another for video chatting.
 * </p>
 *
 */
public class MainController implements Initializable {

    @FXML
    private ListView userList;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button profileButton;

    private double userListPrefWidth;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // set main controller to core
        Core.instance().main(this);

        // change of user name
        profileButton.setText(Core.instance().user().username());
        Core.instance().user().usernameProperty().addListener((ov, o, n) -> {
            Platform.runLater(() -> profileButton.setText(n));
        });

        // change of avatar image
        final ImageView profileImage = (ImageView) profileButton.getGraphic();
        ChangeListener updateAvatar = (ov, o, n) -> {
            Image avatar = Core.instance().user().getAvatarImage(32, 32);
            profileImage.setImage(avatar);
        };
        updateAvatar.changed(null, null, null);
        Core.instance().user().avatarProperty().addListener(updateAvatar);

        // change of user list change
        userListPrefWidth = userList.getPrefWidth();
        Core.instance().scanner().userListProperty().addListener((ov, o, n) -> {
            Platform.runLater(() -> buildUserList(n.values()));
        });
    }

    @FXML
    private void handleProfileAction(ActionEvent event) {
        Core.instance().profile().setClient(null);
        userList.getSelectionModel().clearSelection();
        selectProfile();
    }

    private void buildUserList(Collection<Client> values) {
        // clear previous items
        userList.getItems().clear();
        // add all items
        values.stream().forEach((client) -> {
            UserItem item = UserItem.createInstance(client);
            userList.getItems().add(item);
        });
        // hide user list if empty
        if (userList.getItems().isEmpty()) {
            userList.setPrefWidth(0.0);
        } else {
            userList.setPrefWidth(userListPrefWidth);
        }
        // refresh profile view to show last user information
        Core.instance().profile().refresh();
    }

    public void showUser(Client client) {
        Platform.runLater(() -> {
            Core.instance().profile().setClient(client);
            Core.instance().messaging().setClient(client);
            Core.instance().videocall().setClient(client);
            selectProfile();
        });
    }

    /**
     * Select and show the profile tab
     */
    public void selectProfile() {
        tabPane.getSelectionModel().select(0);
    }

    /**
     * Select and show the messaging tab
     */
    public void selectMessaging() {
        tabPane.getSelectionModel().select(1);
    }

    /**
     * Select and show the videocall tab
     */
    public void selectVideoCall() {
        tabPane.getSelectionModel().select(2);
    }
}
