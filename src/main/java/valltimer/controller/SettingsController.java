package valltimer.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import valltimer.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.prefs.Preferences;

public class SettingsController {

    @FXML
    private JFXButton btnBack, buttonSaveChanges;

    @FXML
    private JFXTextField serverIpTF, serverPortTF, serverNameTF;

    private String primaryName;

    @FXML
    void initialize(){
        System.out.println(Utils.serverIpAddress);
        loadSettings();
    }

    @FXML
    void onClickBtnBack(ActionEvent event) {

        Stage stage = (Stage) btnBack.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root, 325, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void loadSettings(){
        String ip="", port="", name="";
        Preferences pref;
        pref = Preferences.userNodeForPackage(SettingsController.class);
        ip = pref.get("ip","");
        port = pref.get("port","");
        name = pref.get("name","");
        boolean ipIsNull=false, portIsNull=false, nameIsNull=false;

        if (ip==null || ip.isEmpty()) ipIsNull=true;
        if (port==null || port.isEmpty()) portIsNull=true;
        if (name==null || name.isEmpty()) portIsNull=true;

        if (ipIsNull || portIsNull || nameIsNull){
            try {
                Properties settings = new Properties();
                /*File file = new File(getClass().getResource("/public/config/settings.conf").toURI());
                settings.load(new FileInputStream(file));*/
                settings.load(getClass().getResourceAsStream("/public/config/settings.conf"));

                if (ipIsNull) ip = settings.getProperty("ip");
                if (portIsNull) port = settings.getProperty("port");
                if (nameIsNull) name = settings.getProperty("name");

            } catch (IOException /* | URISyntaxException*/ e) {
                e.printStackTrace();
            }
        }

        serverIpTF.setPromptText(ip);
        serverPortTF.setPromptText(port);
        if (name.equals("None")){
            serverNameTF.setPromptText("None");
            primaryName="";
        }
        else {
            serverNameTF.setPromptText(name);
            primaryName=name;
        }

    }

    @FXML
    void onClickButtonSave(ActionEvent event) {
        String newIp = serverIpTF.getText();
        String newPort = serverPortTF.getText();
        String newName = serverNameTF.getText();


        Preferences pref;
        pref = Preferences.userNodeForPackage(SettingsController.class);
        String name = pref.get("name","");

        if (newIp!=null && !newIp.isEmpty()){
            saveIP(newIp);
        }
        if (newPort!=null && !newPort.isEmpty()){
            savePort(newPort);
        }
        if (newName!=null /*&& !primaryName.equals(newName)*/ && !newName.isEmpty()){
            saveName(newName.trim());
        }
    }

    private void saveIP(String newIp){
        Preferences pref;
        pref = Preferences.userNodeForPackage(SettingsController.class);
        pref.put("ip",newIp);
        serverIpTF.clear();
        serverIpTF.setPromptText(newIp);
        Utils.setServerInfo();
        System.out.println(Utils.serverIpAddress);

    }

    private void savePort(String newPort){
        Preferences pref;
        pref = Preferences.userNodeForPackage(SettingsController.class);
        pref.put("port",newPort);
        serverPortTF.clear();
        serverPortTF.setPromptText(newPort);
        Utils.setServerInfo();
        System.out.println(Utils.serverIpAddress);
    }

    private void saveName(String newName){
        Preferences pref;
        pref = Preferences.userNodeForPackage(SettingsController.class);
        serverNameTF.clear();
        if (newName.isEmpty()){
            serverNameTF.setPromptText("None");
            pref.put("name","None");
            primaryName="";
        }
        else {
            pref.put("name",newName);
            serverNameTF.setPromptText(newName);
            primaryName=newName;
        }
        Utils.setServerInfo();
        System.out.println(Utils.serverIpAddress);
    }

}
