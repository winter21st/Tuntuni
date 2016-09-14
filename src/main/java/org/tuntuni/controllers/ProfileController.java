/*
 * Copyright 2016 Tuntuni.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.ExceptionDialog;
import org.tuntuni.Core;
import org.tuntuni.connection.Client;
import org.tuntuni.util.Database;
import org.tuntuni.util.FileService;

/**
 * To view or edit user's profile
 */
public class ProfileController implements Initializable {

    private Client mClient;

    @FXML
    private TextField userName;
    @FXML
    private TextField statusText;
    @FXML
    private TextArea aboutMe;
    @FXML
    private Button avatarButton;
    @FXML
    private ImageView avatarImage;

    @FXML
    private Button editNameButton;
    @FXML
    private Button editStatusButton;
    @FXML
    private Button editAboutMeButton;

    @FXML
    private GridPane aboutGridPane;
    @FXML
    private Button messageButton;
    @FXML
    private Button videoCallButton;

    private double initialGridWidth = 170;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Core.instance().profile(this);
        initialGridWidth = aboutGridPane.getColumnConstraints().get(1).getPrefWidth();

        editNameButton.setVisible(false);
        editStatusButton.setVisible(false);
        editAboutMeButton.setVisible(false);
        userName.textProperty().addListener((a, b, c) -> {
            boolean show = isDefaultProfile()
                    && !c.equals(Core.instance().user().username());
            editNameButton.setVisible(show);
        });
        aboutMe.textProperty().addListener((a, b, c) -> {
            boolean show = isDefaultProfile()
                    && !c.equals(Core.instance().user().aboutme());
            editAboutMeButton.setVisible(show);
        });
        statusText.textProperty().addListener((a, b, c) -> {
            boolean show = isDefaultProfile()
                    && !c.equals(Core.instance().user().status());
            editStatusButton.setVisible(show);
        });
    }

    public void setClient(Client client) {
        mClient = client;
        refresh();
    }

    public void refresh() {
        Platform.runLater(() -> {
            if (isDefaultProfile()) {
                loadDefaultProfile();
            } else {
                loadClientProfile();
            }
        });
    }

    public boolean isDefaultProfile() {
        return (mClient == null || !mClient.isConnected());
    }

    private void loadClientProfile() {
        // preliminary values
        final double width = avatarImage.getFitWidth();
        final double height = avatarImage.getFitHeight();
        final String user = mClient.getUserData().getUserName();
        final String status = mClient.getUserData().getStatus();
        final String about = mClient.getUserData().getAboutMe();
        final Image avatar = mClient.getUserData().getAvatar(width, height);

        // display all data
        userName.setText(user + " ");
        statusText.setText(status + " ");
        aboutMe.setText(about + " ");
        avatarImage.setImage(avatar);

        // make things appear
        ColumnConstraints cc = aboutGridPane.getColumnConstraints().get(1);
        cc.setMinWidth(initialGridWidth);
        cc.setMaxWidth(initialGridWidth);
        //messageButton.setVisible(true);
        //videoCallButton.setVisible(true);

        // set edit states of each controls
        aboutMe.setEditable(false);
        userName.setEditable(false);
        statusText.setEditable(false);
        userName.setCursor(Cursor.DEFAULT);
        statusText.setCursor(Cursor.DEFAULT);
        avatarButton.setCursor(Cursor.DEFAULT);
    }

    private void loadDefaultProfile() {
        // preliminary values
        final double width = avatarImage.getFitWidth();
        final double height = avatarImage.getFitHeight();
        final String user = Core.instance().user().username();
        final String status = Core.instance().user().status();
        final String about = Core.instance().user().aboutme();
        final Image avatar = Core.instance().user().getAvatarImage(width, height);

        // display all data
        userName.setText(user);
        statusText.setText(status);
        aboutMe.setText(about);
        avatarImage.setImage(avatar);

        // make things appear
        ColumnConstraints cc = aboutGridPane.getColumnConstraints().get(1);
        cc.setMinWidth(0);
        cc.setMaxWidth(0);
        //messageButton.setVisible(false);
        //videoCallButton.setVisible(false);

        // set edit states of each controls
        aboutMe.setEditable(true);
        userName.setEditable(true);
        statusText.setEditable(true);
        userName.setCursor(Cursor.TEXT);
        statusText.setCursor(Cursor.TEXT);
        avatarButton.setCursor(Cursor.TEXT);
    }

    @FXML
    private void handleSendMessage(ActionEvent event) {
        Core.instance().main().selectMessaging();
    }

    @FXML
    private void handleSendCall(ActionEvent event) {
        Core.instance().main().selectVideoCall();
    }

    @FXML
    private void changeAvatar(ActionEvent event) {
        if (mClient == null) {
            FileChooser fc = new FileChooser();
            File init = new File(Database.instance().get("Initial Directory"));
            if (init.exists()) {
                fc.setInitialDirectory(init);
            }
            fc.getExtensionFilters().setAll(
                    new FileChooser.ExtensionFilter("Image Files",
                            "*.png", "*.jpg", "*.bmp", "*.gif"));
            fc.setTitle("Choose your avatar...");
            File choosen = fc.showOpenDialog(Core.instance().stage());
            if (choosen != null) {
                changeAvatar(choosen);
                Database.instance().set("Initial Directory",
                        choosen.getParentFile().toString());
            }
        }
        refresh();
    }

    @FXML
    private void changeName() {
        Core.instance().user().username(userName.getText());
        editNameButton.setVisible(false);
    }

    @FXML
    private void changeStatus() {
        Core.instance().user().status(statusText.getText());
        editStatusButton.setVisible(false);
    }

    @FXML
    private void changeAboutMe() {
        Core.instance().user().aboutme(aboutMe.getText());
        editAboutMeButton.setVisible(false);
    }

    private void changeAvatar(File choosen) {
        try {
            String uploaded = FileService.instance().upload(choosen);
            Image image = FileService.instance().getImage(uploaded);
            Core.instance().user().avatar(uploaded);
        } catch (IOException ex) {
            ExceptionDialog dialog = new ExceptionDialog(ex);
            dialog.setTitle("Failed to upload image");
            dialog.showAndWait();
        }
    }

}
