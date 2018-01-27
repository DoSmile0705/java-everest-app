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
package com.rohitawate.restaurant.dashboard;

import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.restaurant.models.RestaurantResponse;
import com.rohitawate.restaurant.requests.RequestManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
	@FXML
    private BorderPane dashboard;
	@FXML
	private TextField addressField;
	@FXML
	private ComboBox<String> httpMethodBox;
	@FXML
    private VBox responseBox;
    @FXML
    private HBox responseDetails;
    @FXML
    private TextArea responseArea;
    @FXML
    private Label statusCode, statusCodeDescription, responseTime, responseSize;

	private JFXSnackbar snackBar;
	private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
	private RequestManager requestManager;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        responseBox.getChildren().remove(0);
        httpMethodBox.getItems().addAll(httpMethods);
		httpMethodBox.setValue("GET");
		
		requestManager = new RequestManager();
		snackBar = new JFXSnackbar(dashboard);
	}

	@FXML
	private void sendAction() {
		try {
			String address = addressField.getText();
			if (address.equals("")) {
				snackBar.show("Please enter a valid address", 7000);
				return;
			}
            RestaurantResponse response;
			URL url = new URL(address);
			switch (httpMethodBox.getValue()) {
				case "GET":
					response = requestManager.get(url);
					break;
                default:
                    response = new RestaurantResponse();
			}
            responseArea.setText(response.getBody());
            if (responseBox.getChildren().size() != 2)
                responseBox.getChildren().add(0, responseDetails);
            statusCode.setText(Integer.toString(response.getStatusCode()));
            statusCodeDescription.setText(Response.Status.fromStatusCode(response.getStatusCode()).getReasonPhrase());
            responseTime.setText(Long.toString(response.getTime()) + " ms");
            responseSize.setText(Integer.toString(response.getSize()) + " B");
		} catch (MalformedURLException ex) {
			snackBar.show("Invalid URL. Please verify and try again.", 7000);
		} catch (IOException ex) {
			snackBar.show("Server did not respond", 7000);
		}
		
	}
}
