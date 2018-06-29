/*
 * Copyright 2018 Rohit Awate.
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

package com.rohitawate.everest.controllers;

import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea.HighlightMode;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.models.requests.DataDispatchRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.ResourceBundle;

/*
    Raw and Binary tabs are embedded in
    this FXML itself.
    URL encoded and Form tabs have special FXMLs.
 */
public class BodyTabController implements Initializable {
    @FXML
    ComboBox<String> rawInputTypeBox;
    @FXML
    Tab rawTab, binaryTab, formTab, urlTab;
    @FXML
    TextField filePathField;
    @FXML
    private VBox rawVBox;

    EverestCodeArea rawInputArea;
    FormDataTabController formDataTabController;
    URLTabController urlTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rawInputTypeBox.getItems().addAll("PLAIN TEXT", "JSON", "XML", "HTML");
        rawInputTypeBox.getSelectionModel().select(0);

        rawInputArea = new EverestCodeArea();
        ThemeManager.setSyntaxTheme(rawInputArea);
        rawInputArea.setPrefHeight(1500);   // Hack to make the EverestCodeArea stretch with the Composer
        rawVBox.getChildren().add(new VirtualizedScrollPane<>(rawInputArea));

        rawInputTypeBox.valueProperty().addListener(change -> {
            String type = rawInputTypeBox.getValue();
            HighlightMode mode;
            switch (type) {
                case "JSON":
                    mode = HighlightMode.JSON;
                    break;
                case "XML":
                    mode = HighlightMode.XML;
                    break;
                case "HTML":
                    mode = HighlightMode.HTML;
                    break;
                default:
                    mode = HighlightMode.PLAIN;
            }
            rawInputArea.setMode(mode);
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/FormDataTab.fxml"));
            formTab.setContent(loader.load());
            formDataTabController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/URLTab.fxml"));
            Parent formTabContent = loader.load();
            ThemeManager.setTheme(formTabContent);
            urlTab.setContent(formTabContent);
            urlTabController = loader.getController();
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not load URL tab.", e, LocalDateTime.now());
        }
    }

    /**
     * Returns a EverestRequest object initialized with the request rawBody.
     */
    public DataDispatchRequest getBasicRequest(String requestType) {
        DataDispatchRequest request = new DataDispatchRequest(requestType);

        // Raw and binary types get saved in Body.
        // Form and URL encoded types use tuple objects
        if (rawTab.isSelected()) {
            String contentType;
            switch (rawInputTypeBox.getValue()) {
                case "PLAIN TEXT":
                    contentType = MediaType.TEXT_PLAIN;
                    break;
                case "JSON":
                    contentType = MediaType.APPLICATION_JSON;
                    break;
                case "XML":
                    contentType = MediaType.APPLICATION_XML;
                    break;
                case "HTML":
                    contentType = MediaType.TEXT_HTML;
                    break;
                default:
                    contentType = MediaType.TEXT_PLAIN;
            }
            request.setContentType(contentType);
            request.setBody(rawInputArea.getText());
        } else if (formTab.isSelected()) {
            request.setStringTuples(formDataTabController.getStringTuples(true));
            request.setFileTuples(formDataTabController.getFileTuples(true));
            request.setContentType(MediaType.MULTIPART_FORM_DATA);
        } else if (binaryTab.isSelected()) {
            request.setBody(filePathField.getText());
            request.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else if (urlTab.isSelected()) {
            request.setStringTuples(urlTabController.getStringTuples(true));
            request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return request;
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a binary file to add to request...");
        Window dashboardWindow = filePathField.getScene().getWindow();
        String filePath;

        try {
            filePath = fileChooser.showOpenDialog(dashboardWindow).getAbsolutePath();
        } catch (NullPointerException NPE) {
            filePath = "";
        }

        filePathField.setText(filePath);
    }

    public DashboardState getState() {
        DashboardState state = new DashboardState();

        state.rawBodyType = rawInputTypeBox.getValue();
        state.rawBody = rawInputArea.getText();
        state.urlStringTuples = urlTabController.getStringTuples(false);
        state.formStringTuples = formDataTabController.getStringTuples(false);
        state.formFileTuples = formDataTabController.getFileTuples(false);
        state.binaryFilePath = filePathField.getText();

        if (rawTab.isSelected()) {
            switch (rawInputTypeBox.getValue()) {
                case "JSON":
                    state.contentType = MediaType.APPLICATION_JSON;
                    break;
                case "XML":
                    state.contentType = MediaType.APPLICATION_XML;
                    break;
                case "HTML":
                    state.contentType = MediaType.TEXT_HTML;
                    break;
                default:
                    state.contentType = MediaType.TEXT_PLAIN;
            }
        } else if (formTab.isSelected()) {
            state.contentType = MediaType.MULTIPART_FORM_DATA;
        } else if (urlTab.isSelected()) {
            state.contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            state.contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return state;
    }

    public void setState(DashboardState state) {
        // Adding Form tab's string tuples
        for (Entry<String, String> entry : state.formStringTuples.entrySet())
            formDataTabController.addStringField(entry.getKey(), entry.getValue());

        // Adding Form tab's file tuples
        for (Entry<String, String> entry : state.formFileTuples.entrySet())
            formDataTabController.addFileField(entry.getKey(), entry.getValue());

        // Adding URL tab's tuples
        for (Entry<String, String> entry : state.urlStringTuples.entrySet())
            urlTabController.addField(entry.getKey(), entry.getValue());

        setRawTab(state);

        filePathField.setText(state.binaryFilePath);
    }

    private void setRawTab(DashboardState state) {
        HighlightMode mode;

        switch (state.rawBodyType) {
            case "JSON":
                mode = HighlightMode.JSON;
                break;
            case "XML":
                mode = HighlightMode.XML;
                break;
            case "HTML":
                mode = HighlightMode.HTML;
                break;
            default:
                mode = HighlightMode.PLAIN;
        }

        rawInputArea.setText(state.rawBody, mode);
        rawInputTypeBox.getSelectionModel().select(state.rawBodyType);
    }
}
