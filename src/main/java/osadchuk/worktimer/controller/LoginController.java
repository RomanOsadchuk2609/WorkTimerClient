package osadchuk.worktimer.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import osadchuk.worktimer.Utils;
import osadchuk.worktimer.entity.SimpleTask;
import osadchuk.worktimer.util.ErrorDialogFactory;
import osadchuk.worktimer.util.TimerConstants;
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

@Slf4j
public class LoginController {

	private static final String CONNECTION_ERROR_MESSAGE = "Could not connect to\nthe server. Please, check\nyour Internet connection.";
	private static final String AUTHENTICATION_ERROR_MESSAGE = "Invalid username or\npassword.";

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
						showErrorDialog(TimerConstants.ERROR.CONNECTION, CONNECTION_ERROR_MESSAGE);
					} else if (!isAuthorized) {
						loginThread.interrupt();
						paneMain.setVisible(true);
						paneMain.setDisable(false);
						paneWait.setVisible(false);
						loginTextField.clear();
						passwordTextField.clear();
						loginTextField.setStyle("-jfx-focus-color: red; -jfx-unfocus-color: red");
						passwordTextField.setStyle("-jfx-focus-color: red; -jfx-unfocus-color: red");
						rememberMeCheckBox.setSelected(false);
						showErrorDialog(TimerConstants.ERROR.AUTHENTICATION, AUTHENTICATION_ERROR_MESSAGE);
					} else {
						loginThread.interrupt();
						Stage stage = (Stage) btnSignIn.getScene().getWindow();
						FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/home.fxml"));
						//Parent root = null;
						try {
							Parent root = loader.load();
							Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);
							stage.setScene(scene);
							stage.setResizable(false);
							stage.setIconified(false);
							stage.show();
							HomeController controller = loader.getController();
							createTrayIcon(stage, controller);
						} catch (IOException e) {
							log.error("Error: ", e);
						}
					}
				}
			}
			lastUpdate = now;
		}
	};

	@FXML
	void initialize() {
		setHostServices(Utils.hostServices);
		checkRememberedUser();
	}

	public void showErrorDialog(String header, String body) {
		ErrorDialogFactory.createErrorDialog(header, body, stackPane).show();
	}

	@FXML
	void onClickBtnSettings(ActionEvent event) {
		Stage stage = (Stage) btnSignIn.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/settings.fxml"));
		//FXMLLoader loader = new FXMLLoader(Utils.urlSettings);

		try {
			Parent root = loader.load();
			Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			log.error("Error: ", e);
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
	void onClickBtnSignIn(ActionEvent event) {
		Preferences pref;
		pref = Preferences.userNodeForPackage(LoginController.class);
		Utils.authToken = new UsernamePasswordAuthenticationToken(loginTextField.getText(), passwordTextField.getText());
		if (rememberMeCheckBox.isSelected()) {
			pref.put(TimerConstants.USERNAME, loginTextField.getText());
			pref.put(TimerConstants.PASSWORD, passwordTextField.getText());
		} else {
			pref.put(TimerConstants.USERNAME, TimerConstants.EMPTY_STRING);
			pref.put(TimerConstants.PASSWORD, TimerConstants.EMPTY_STRING);
		}
		paneMain.setDisable(true);
		paneWait.setVisible(true);
		waitSpinner = new JFXSpinner();
		waitSpinner.setLayoutX(10);
		waitSpinner.setLayoutY(10);
		paneWait.getChildren().add(waitSpinner);
        /*if (loginThread != null) {
            loginThread = new LoginThread();
        } else {
        }*/
		loginThread = new LoginThread();
		loginThread.start();
		at.start();

	}


	@FXML
	void onMouseTextField(MouseEvent event) {
		passwordTextField.setFocusTraversable(true);
	}

	@FXML
	void OnMouseClickPassword(MouseEvent event) {
		loginTextField.setStyle(TimerConstants.EMPTY_STRING);
		passwordTextField.setStyle(TimerConstants.EMPTY_STRING);
	}

	void checkRememberedUser() {
		Preferences pref;
		pref = Preferences.userNodeForPackage(LoginController.class);
		String username = pref.get(TimerConstants.USERNAME, TimerConstants.EMPTY_STRING);
		String password = pref.get(TimerConstants.PASSWORD, TimerConstants.EMPTY_STRING);
		if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
			loginTextField.setText(username);
			passwordTextField.setText(password);
			rememberMeCheckBox.setSelected(true);
		}
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
		log.debug("Login response: {}", loginResponse);
	}

	private class LoginThread extends Thread {
		public void run() {
			String response = null;
			try {
				login(Utils.authToken);
				log.debug("JSESSIONID = {}", Utils.JSESSIONID);
				List<NameValuePair> parameters = new ArrayList<>();
				parameters.add(new BasicNameValuePair(TimerConstants.USERNAME, loginTextField.getText()));
				response = HTTPRequest.getResponseFromGet(Utils.serverIpAddress + "api/simple_tasks/by_username", parameters, Utils.JSESSIONID);
				log.debug("api/simple_tasks/by_username response: {}", response);

				if (response == null || response.equals("302") || response.equals("401")) {
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
				}
			} catch (IOException e) {
				log.error("Error: ", e);
				isAuthorized = null;
			} catch (URISyntaxException e) {
				log.error("Error: ", e);
			}
		}
	}

	private void createTrayIcon(final Stage stage, HomeController controller) {
		if (SystemTray.isSupported()) {
			// get the SystemTray instance
			SystemTray tray = SystemTray.getSystemTray();
			// load an image
			java.awt.Image image = null;
			try {
				image = ImageIO.read(getClass().getResource("/public/img/icon.png"));
			} catch (IOException ex) {
				log.error("Error: ", ex);
			}

			stage.setOnCloseRequest(t -> {
				if (controller.isWorkStarted()) {
					hide(stage);
				} else {
					controller.stopApp();
				}
			});

			// create an action listener to listen for default action executed on the tray icon
			final ActionListener closeListener = e -> {
				if (!controller.isWorkStarted()) {
					controller.stopApp();
				}
			};

			ActionListener showListener = e -> Platform.runLater(stage::show);

			MouseListener mouseListener = new MouseListener() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					Platform.runLater(() -> {
						if (stage.isShowing() && controller.isWorkStarted()) {
							hide(stage);
						} else {
							stage.show();
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
			trayIcon = new TrayIcon(image, TimerConstants.APP.NAME, popup);
			trayIcon.addActionListener(showListener);
			trayIcon.addMouseListener(mouseListener);
			trayIcon.setImageAutoSize(true);
			try {
				tray.add(trayIcon);
				controller.setTray(tray, trayIcon);
			} catch (AWTException e) {
				log.error("Error: ", e);
			}
		}
	}

	private void showProgramIsMinimizedMsg() {
		if (firstTime) {
			trayIcon.displayMessage("Some message.",
					"Some other message.",
					TrayIcon.MessageType.INFO);
			firstTime = false;
		}
	}

	private void hide(final Stage stage) {
		Platform.runLater(() -> {
			if (SystemTray.isSupported()) {
				stage.hide();
				//showProgramIsMinimizedMsg();
			} /*else {
                Platform.exit();
            }*/
		});
	}

	private void setUserIcon(String base64userIcon) {
		Utils.userIcon = Utils.getImageFromBase64(base64userIcon, 40, 40);
	}

	private HostServicesDelegate getHostServices() {
		return hostServices;
	}

	private void setHostServices(HostServicesDelegate hostServices) {
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
}

