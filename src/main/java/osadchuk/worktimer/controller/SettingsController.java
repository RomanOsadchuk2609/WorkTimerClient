package osadchuk.worktimer.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import osadchuk.worktimer.Utils;
import osadchuk.worktimer.util.TimerConstants;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;

@Slf4j
public class SettingsController {
    private static final String SERVER_IP_ADDRESS_LOG_MESSAGE = "Server IP address: {}";

    @FXML
    private JFXButton btnBack, buttonSaveChanges;

    @FXML
    private JFXTextField serverIpTF;

    @FXML
    private JFXTextField serverPortTF;

    @FXML
    private JFXTextField serverProtocolTF;

    private Map<TimerConstants.PROPERTY, String> propertyMap = new EnumMap<>(TimerConstants.PROPERTY.class);

    @FXML
    void initialize() {
        log.debug(SERVER_IP_ADDRESS_LOG_MESSAGE, Utils.serverIpAddress);
        propertyMap = Utils.loadPropertymap();
        showProperties();
    }

    @FXML
    void onClickBtnBack(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
        Parent root;
        try {
            root = loader.load();
            Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            log.error("Failed to load parent screen", e);
        }
    }

    private void showProperties() {
        serverProtocolTF.clear();
        serverProtocolTF.setPromptText(propertyMap.get(TimerConstants.PROPERTY.PROTOCOL));
        serverIpTF.clear();
        serverIpTF.setPromptText(propertyMap.get(TimerConstants.PROPERTY.IP_ADDRESS));
        serverPortTF.clear();
        serverPortTF.setPromptText(propertyMap.get(TimerConstants.PROPERTY.PORT));
    }

    @FXML
    void onClickButtonSave(ActionEvent event) {
        String newProtocol = serverProtocolTF.getText();
        String newIp = serverIpTF.getText();
        String newPort = serverPortTF.getText();
        if (newProtocol != null && !newProtocol.isEmpty()) {
            saveProperty(TimerConstants.PROPERTY.PROTOCOL, newProtocol);
        }
        if (newIp != null && !newIp.isEmpty()) {
            saveProperty(TimerConstants.PROPERTY.IP_ADDRESS, newIp);
        }
        if (newPort != null && !newPort.isEmpty()) {
            saveProperty(TimerConstants.PROPERTY.PORT, newPort);
        }
        log.debug(SERVER_IP_ADDRESS_LOG_MESSAGE, Utils.serverIpAddress);
    }

    private void saveProperty(TimerConstants.PROPERTY property, String value) {
        Preferences pref = Preferences.userNodeForPackage(SettingsController.class);
        pref.put(property.getName(), value);
        propertyMap.put(property, value);
        showProperties();
        Utils.setServerInfo(propertyMap);
    }
}
