package osadchuk.worktimer.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import osadchuk.worktimer.Utils;
import osadchuk.worktimer.entity.SimpleTask;
import osadchuk.worktimer.webRequest.HTTPRequest;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Pane paneWait, paneMain;
    @FXML
    private StackPane stackPane;
    @FXML
    private JFXSpinner waitSpinner;
    @FXML
    private JFXPasswordField passwordTextField;
    @FXML
    private JFXTextField loginTextField;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private JFXButton btnSignUp, btnSignIn, btnSettings;
    @FXML
    private JFXCheckBox rememberMeCheckBox;

    private boolean firstTime;
    private TrayIcon trayIcon;
    private HostServicesDelegate hostServices;
    private LoginThread loginThread = null;
    private Boolean isAuthorized = null;


    private AnimationTimer at = new AnimationTimer() {
        long lastUpdate = 0;

        @Override
        public void handle(long now) {
            if (now - lastUpdate > 1_000_000) {
                if (!loginThread.isAlive()) {
                    at.stop();
                    if (isAuthorized == null) {
                        loginThread.interrupt();
                        paneMain.setVisible(true);
                        paneMain.setDisable(false);
                        paneWait.setVisible(false);

                        showConnectionErrorDialog("Could not connect to\nthe server. Please, check\nyour Internet connection.");

                    } else if (isAuthorized == false) {

                        loginThread.interrupt();
                        paneMain.setVisible(true);
                        paneMain.setDisable(false);
                        paneWait.setVisible(false);
                        loginTextField.clear();
                        passwordTextField.clear();
                        loginTextField.setStyle("-jfx-focus-color: red; -jfx-unfocus-color: red");
                        passwordTextField.setStyle("-jfx-focus-color: red; -jfx-unfocus-color: red");
                        rememberMeCheckBox.setSelected(false);

                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setHeading(new Label("Authentication Error!"));
                        content.setBody(new Text("Invalid username or\npassword."));
                        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
                        JFXButton button = new JFXButton("Okay");
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                dialog.close();
                            }
                        });
                        content.setActions(button);
                        dialog.setOverlayClose(false);
                        dialog.show();
                    } else {

                        loginThread.interrupt();
                        Stage stage = (Stage) btnSignIn.getScene().getWindow();

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/home.fxml"));
                        //Parent root = null;
                        try {
                            Parent root = loader.load();
                            Scene scene = new Scene(root, 325, 450);
                            stage.setScene(scene);
                            stage.setResizable(false);
                            stage.setIconified(false);
                            stage.show();
                            HomeController controller = loader.getController();
                            createTrayIcon(stage, controller);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            lastUpdate = now;
        }
    };

    @FXML
    void initialize() throws IOException, ClassNotFoundException {
        setHostServices(Utils.hostServices);
        checkRememberedUser();
    }

    public void showConnectionErrorDialog(String message) {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("Connection Error!"));
        content.setBody(new Text(message));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Okay");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        content.setActions(button);
        dialog.setOverlayClose(false);
        dialog.show();
    }

    @FXML
    void onClickBtnSettings(ActionEvent event) {
        Stage stage = (Stage) btnSignIn.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/settings.fxml"));
        //FXMLLoader loader = new FXMLLoader(Utils.urlSettings);

        // Parent root = null;
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 325, 450);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onClickForgotPassword(ActionEvent event) {
        getHostServices().showDocument(Utils.serverIpAddress + "forgot");
    }

    @FXML
    void OnClickBtnSignUp(ActionEvent event) {
        getHostServices().showDocument(Utils.serverIpAddress + "registration");
    }

    @FXML
    void onClickBtnSignIn(ActionEvent event) throws IOException, URISyntaxException {
        Preferences pref;
        pref = Preferences.userNodeForPackage(LoginController.class);
        Utils.authToken = new UsernamePasswordAuthenticationToken(loginTextField.getText(), passwordTextField.getText());
        if (rememberMeCheckBox.isSelected()) {
            pref.put("username", loginTextField.getText());
            pref.put("password", passwordTextField.getText());
        } else {

            pref.put("username", "");
            pref.put("password", "");
        }
        paneMain.setDisable(true);
        paneWait.setVisible(true);
        waitSpinner = new JFXSpinner();
        waitSpinner.setLayoutX(10);
        waitSpinner.setLayoutY(10);
        paneWait.getChildren().add(waitSpinner);
        if (loginThread != null) {
            loginThread = new LoginThread();
        } else {
            loginThread = null;
            loginThread = new LoginThread();
        }
        loginThread.start();
        at.start();

    }


    @FXML
    void onMouseTextField(MouseEvent event) {
        passwordTextField.setFocusTraversable(true);
    }

    @FXML
    void OnMouseClickPassword(MouseEvent event) {
        loginTextField.setStyle("");
        passwordTextField.setStyle("");
    }

    public void checkRememberedUser() throws IOException, ClassNotFoundException {


        Preferences pref;
        pref = Preferences.userNodeForPackage(LoginController.class);
        String username = pref.get("username", "");
        String password = pref.get("password", "");
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            loginTextField.setText(username);
            passwordTextField.setText(password);
            rememberMeCheckBox.setSelected(true);
        }
        //This give you the value of the preference
    }

    private void login(Authentication authToken) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(authToken);
        out.flush();
        byte[] authTokenBytes = bos.toByteArray();
        bos.close();
        BASE64Encoder encoder = new BASE64Encoder();
        String authTokenBase64 = encoder.encode(authTokenBytes);

        List<NameValuePair> parametersPost2 = new ArrayList<>();
        parametersPost2.add(new BasicNameValuePair("authToken", authTokenBase64));
        String loginResponse = HTTPRequest.login(Utils.serverIpAddress + "timer/login", parametersPost2);
        System.out.println(loginResponse);
    }

    private class LoginThread extends Thread {

        public void run() {
            String response = null;
            try {
                login(Utils.authToken);
                System.out.println(Utils.JSESSIONID);

                List<NameValuePair> parameters = new ArrayList<>();
                parameters.add(new BasicNameValuePair("username", loginTextField.getText()));
                response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + "api/simple_tasks/by_username", parameters, Utils.JSESSIONID);
                System.out.println(response);

                if (response.equals("302") || response.equals("401")) {
                    isAuthorized = false;
                    return;
                } else {
                    String base64userIcon = HTTPRequest.getResponseFromGet(Utils.serverIpAddress + "timer/get_user_icon", Utils.JSESSIONID);
                    setUserIcon(base64userIcon);
                    List<SimpleTask> list = Utils.getListOfSimpleTasksFromJson(response);
                    if (!list.isEmpty()) {
                        Utils.simpleTaskList = list;
                    } else {
                        Utils.simpleTaskList = null;
                    }
                    Utils.username = loginTextField.getText();
                    isAuthorized = true;

                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isAuthorized = null;
                return;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void setUserIcon(String base64userIcon) {
        Utils.userIcon = Utils.getImageFromBase64(base64userIcon, 40, 40);
    }

    public HostServicesDelegate getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServicesDelegate hostServices) {
        this.hostServices = hostServices;
    }

    public JFXPasswordField getPasswordTextField() {
        return passwordTextField;
    }

    public void setPasswordTextField(JFXPasswordField passwordTextField) {
        this.passwordTextField = passwordTextField;
    }

    public JFXTextField getLoginTextField() {
        return loginTextField;
    }

    public void setLoginTextField(JFXTextField loginTextField) {
        this.loginTextField = loginTextField;
    }


    public void createTrayIcon(final Stage stage, HomeController controller) {
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            java.awt.Image image = null;
            try {
                image = ImageIO.read(getClass().getResource("/public/img/icon.png"));
            } catch (IOException ex) {
                System.out.println(ex);
            }


            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    if (controller.isWorkStarted()) {
                        hide(stage);
                    } else {
                        controller.stopApp();
                    }
                }
            });


            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!controller.isWorkStarted()) {
                        controller.stopApp();
                    }
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };

            MouseListener mouseListener = new MouseListener() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            if (stage.isShowing() && controller.isWorkStarted()) {
                                hide(stage);
                            } else {
                                stage.show();
                            }
                        }
                    });
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {

                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {

                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {

                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {

                }
            };
            // create a popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            trayIcon = new TrayIcon(image, "VallTimer", popup);
            trayIcon.addActionListener(showListener);
            trayIcon.addMouseListener(mouseListener);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
                controller.setTray(tray, trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
    }

    public void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("Some message.",
                    "Some other message.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (SystemTray.isSupported()) {
                    stage.hide();
                    //showProgramIsMinimizedMsg();
                } /*else {
                    Platform.exit();
                }*/
            }
        });
    }
}

