package osadchuk.worktimer.controller;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.controls.JFXTreeView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import osadchuk.worktimer.Utils;
import osadchuk.worktimer.entity.PrimitiveUser;
import osadchuk.worktimer.entity.Task;
import osadchuk.worktimer.entity.TimeLogDTO;
import osadchuk.worktimer.model.NotSentData;
import osadchuk.worktimer.model.TreeViewHelper;
import osadchuk.worktimer.util.ErrorDialogFactory;
import osadchuk.worktimer.util.TimerConstants;
import osadchuk.worktimer.webRequest.HTTPRequest;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HomeController {
	@FXML
	private VBox vbox;
	@FXML
	private TreeView<?> projectsTasksTreeView;
	@FXML
	private JFXTreeView<?> jfxTreeView;
	@FXML
	private JFXButton btnNext;
	@FXML
	private Hyperlink logOutLink;
	@FXML
	private Label labelTaskChoose, labelNoTasks, labelWorkStarted,
			labelUsername, labelTime, labelStartTime, labelTaskName;
	@FXML
	private ImageView userImage, screenView;
	@FXML
	private StackPane stackPane;
	@FXML
	private AnchorPane peopleAnchorPane, timerPane;

	private TreeViewHelper treeViewHelper;
	private String lastSearchedUsername = "";
	private Date lastTaskUpdate;
	private boolean lastUtilsIsWorkStartedValue, isStarted, isWorkStarted, sendScreenShot, randomScreenshot, isCheating;
	private Date startDate, lastScreenShotTime, lastStopRequestTime;
	private LocalTime workedTime;
	private double screenShotInterval;
	private long initialScreenshotInterval, timerId;
	private LocalTime startTime;
	private Task selectedTask;
	private TrayIcon trayIcon;
	private SystemTray systemTray;
	private int screenShotHashCode = 0;

	public void setTray(SystemTray systemTray, TrayIcon trayIcon) {
		this.systemTray = systemTray;
		this.trayIcon = trayIcon;
	}

	@FXML
	void initialize() throws IOException {
		new SendNotSentDataThread().start();

		isCheating = false;
		sendScreenShot = false;
		randomScreenshot = false;
		screenShotInterval = 5 * 60 * 1000;//5 minutes

		Properties settings = new Properties();
		settings.load(getClass().getResourceAsStream("/public/config/settings.conf"));
		sendScreenShot = Boolean.parseBoolean(settings.getProperty("isScreenshotsSending"));
		randomScreenshot = Boolean.parseBoolean(settings.getProperty("randomScreenshot"));
		initialScreenshotInterval = Long.parseLong(settings.getProperty("screenShotInterval"));
		screenShotInterval = initialScreenshotInterval;
		System.out.println("screenShotInterval = " + screenShotInterval);

		if (randomScreenshot) {
			screenShotInterval = Math.random() * initialScreenshotInterval;
		}

		Utils.homeController = this;
		lastTaskUpdate = new Date();
		lastUtilsIsWorkStartedValue = false;
		userImage.setImage(Utils.userIcon);
		Circle circle = new Circle(20);
		circle.setCenterX(20);
		circle.setCenterY(20);
		userImage.setClip(circle);
		labelUsername.setText(Utils.username);
		labelTaskChoose.setText("Please, choose a task\nto start working!");
		labelTaskChoose.setTextAlignment(TextAlignment.CENTER);
		treeViewHelper = new TreeViewHelper();
		showTaskTreeView();
		vbox.setFocusTraversable(false);
		jfxTreeView.setFocusTraversable(false);

		at.start();
	}

	private AnimationTimer at = new AnimationTimer() {
		long lastUpdate = 0;

		@Override
		public void handle(long now) {
			if (now - lastUpdate > 1_000_000) {
				if (new Date().getTime() - lastTaskUpdate.getTime() > (60 * 1000) && !isWorkStarted) {// > 1 minute
					lastTaskUpdate = new Date();
					try {
						Utils.updateSimpleTaskList(false);
					} catch (IOException | URISyntaxException e) {
						log.error("Could not update tasks. ", e);
					}
				}
				if (!isWorkStarted) {
					labelWorkStarted.setVisible(false);
					logOutLink.setDisable(false);
					if (!btnNext.getText().equals(TimerConstants.REFRESH)) {
						btnNext.setText("Start work");
						logOutLink.setDisable(false);
						vbox.setVisible(true);
						btnNext.setDisable(false);
						TreeItem<?> treeItem = jfxTreeView.getSelectionModel().getSelectedItem();

						if (treeItem != null && treeItem.getValue() instanceof Task) {
							btnNext.setDisable(false);
						} else {
							btnNext.setDisable(true);
						}
					} else {
						btnNext.setDisable(false);
					}
				}

				if (isWorkStarted) {
					lastTaskUpdate = new Date();
					btnNext.setText("Stop work");
					btnNext.setDisable(false);
					logOutLink.setDisable(true);
					LocalTime currentTime = LocalTime.now();
					currentTime = currentTime.minusSeconds(startTime.getSecond());
					currentTime = currentTime.minusMinutes(startTime.getMinute());
					currentTime = currentTime.minusHours(startTime.getHour());
					workedTime = currentTime;
					DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
					SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
					labelTime.setText(currentTime.format(timeFormat));
					if (startDate != null) {
						labelStartTime.setText(formatter.format(startDate));
					}
					String screenShotBase64 = null;

					Date screenTime = new Date();

					if (screenTime.getTime() - lastStopRequestTime.getTime() > 1000 * 60 * 1.5) {
						isCheating = true;
						punishCheater();
					} else if (screenTime.getTime() - lastStopRequestTime.getTime() > 1000 * 60) {
						//TODO: extract time into property
//					} else if (screenTime.getTime() - lastStopRequestTime.getTime() > 1000 * 15) {
						//Sending POST request about finishing work
						lastStopRequestTime = screenTime;
						Date endDate = getWorkedDate(startDate);
						List<NameValuePair> parametersPost = new ArrayList<>();
						log.debug("{} = {}", startDate, startDate.getTime());
						log.debug("{} = {}", endDate, endDate.getTime());
						parametersPost.add(new BasicNameValuePair("time_log_id", String.valueOf(timerId)));
						parametersPost.add(new BasicNameValuePair("end_time", String.valueOf(endDate.getTime())));

						String response = null;
						try {
							response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + TimerConstants.URL.TIME_LOG_STOP,
									parametersPost, Utils.JSESSIONID);
							log.debug("time_log/stop Response: {}", response);
						} catch (IOException | URISyntaxException e) {
							log.error("Could not stop time log. ", e);
						}
						validateResponse(response, endDate);
					}

					if (!isCheating && sendScreenShot && (screenTime.getTime() - lastScreenShotTime.getTime() > screenShotInterval)) {
						try {
							String lastScreenShotBase64 = screenShotBase64;
							screenShotBase64 = null;
							screenShotBase64 = getBase64ScreenShot();
							Date date = new Date();
							List<NameValuePair> parametersPost = new ArrayList<>();
							parametersPost.add(new BasicNameValuePair("time_log_id", String.valueOf(timerId)));
							parametersPost.add(new BasicNameValuePair("screenshot", screenShotBase64 != null
									? screenShotBase64
									: lastScreenShotBase64));
							parametersPost.add(new BasicNameValuePair("date", String.valueOf(date.getTime())));
							if (screenShotBase64 != null) {
								String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + "time_log/save_screenshot",
										parametersPost, Utils.JSESSIONID);
								validateResponse(response, date);
								if (randomScreenshot) {
									screenShotInterval = Math.random() * initialScreenshotInterval;
								}
								lastScreenShotTime = screenTime;
							} else {
								String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + "time_log/save_screenshot",
										parametersPost, Utils.JSESSIONID);
								validateResponse(response, date);
								log.debug(Utils.IDENTICAL_SCREENSHOTS);
							}
						} catch (AWTException | IOException | URISyntaxException e) {
							log.error("Could not send screenshot: ", e);
						}
					}
				}
			}
			lastUpdate = now;
		}
	};

	private void validateResponse(String response, Date endDate) {
		Pattern number = Pattern.compile("\\d+");
		boolean isNumber = false;
		if (response != null) {
			Matcher matcher = number.matcher(response);
			isNumber = matcher.matches();
		}
		if (response == null || isNumber) {
			saveDataAndGoToLoginScreen(endDate);
		}
	}

	@FXML
	void onClickBtnNext(ActionEvent event) throws IOException, URISyntaxException {
		if (btnNext.getText().equals(TimerConstants.REFRESH)) {
			refresh();
		} else {
			if (!isWorkStarted) {
				startTime = LocalTime.now();
				startDate = new Date();
				TreeItem<?> treeItem = jfxTreeView.getSelectionModel().getSelectedItem();
				selectedTask = (Task) treeItem.getValue();
				labelTaskName.setText(selectedTask.getTaskName());
				lastScreenShotTime = new Date();
				lastStopRequestTime = new Date();
				List<NameValuePair> parametersGet = new ArrayList<>();
				parametersGet.add(new BasicNameValuePair("user_id", String.valueOf(selectedTask.getUserId())));
				parametersGet.add(new BasicNameValuePair("task_id", String.valueOf(selectedTask.getId())));
				parametersGet.add(new BasicNameValuePair("start_time", String.valueOf(startDate.getTime())));
				log.debug("PerformerId: {}", selectedTask.getUserId());
				log.debug("StartTime: {}", startTime);
				String response = null;
				try {
					response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + TimerConstants.URL.TIME_LOG_START,
							parametersGet, Utils.JSESSIONID);
					Pattern number = Pattern.compile("\\d+");
					boolean isNumber = false;
					if (response != null) {
						Matcher matcher = number.matcher(response);
						isNumber = matcher.matches();
					}
					if (response == null || isNumber) {
						goToLoginScreenWithError(Utils.INTERNET_ERROR);
					}
				} catch (IOException | URISyntaxException e) {
					log.error("Error:", e);
				}
				log.debug("time_log/create Response: {}", response);
				if (response != null) {
					startWork(response);
				}
			} else {
				//Sending POST request about finishing work
				Date endDate = this.getWorkedDate(startDate);
				//new StopWorkThread(timerId,endDate.getTime()).start();
				List<NameValuePair> parametersPost = new ArrayList<>();
				log.debug("{} = {}", startDate, startDate.getTime());
				log.debug("{} = {}", endDate, endDate.getTime());
				parametersPost.add(new BasicNameValuePair("time_log_id", String.valueOf(timerId)));
				parametersPost.add(new BasicNameValuePair("end_time", String.valueOf(endDate.getTime())));

				String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + TimerConstants.URL.TIME_LOG_STOP,
						parametersPost, Utils.JSESSIONID);
				log.debug("time_log/stop Response: {}", response);
				Pattern number = Pattern.compile("\\d+");
				boolean isNumber = false;
				if (response != null) {
					Matcher matcher = number.matcher(response);
					isNumber = matcher.matches();
				}
				if (response == null || isNumber) {
					saveDataAndGoToLoginScreen(endDate);
				} else {
					timerPane.setVisible(false);
					logOutLink.setDisable(false);
					isWorkStarted = false;
					showFinishWorkDialog(selectedTask.getTaskName(), workedTime);
				}
			}
		}
	}

	@FXML
	void onClickLogOut(ActionEvent event) throws IOException {
		if (systemTray != null && trayIcon != null) {
			systemTray.remove(trayIcon);
		}
		Stage stage = (Stage) logOutLink.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
		Parent root = loader.load();
		LoginController controller = loader.getController();

		controller.checkRememberedUser();
		Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);

		stage.setScene(scene);
		stage.setResizable(false);

		stage.setOnCloseRequest(t -> Platform.exit());
		stage.show();
	}

	private void refresh() throws IOException, URISyntaxException {
		Utils.updateSimpleTaskList(true);
	}

	public void showTaskTreeView() {

		List<TreeItem<Task>> tasks = treeViewHelper.getTasks();

		if (tasks != null) {
			vbox.setVisible(true);
			labelNoTasks.setVisible(false);
			labelWorkStarted.setVisible(false);
			TreeItem rootItem = new TreeItem(TimerConstants.TREE_VIEW_HEADER);
			rootItem.getChildren().addAll(tasks);
			jfxTreeView.setRoot(rootItem);
			btnNext.setDisable(true);
			btnNext.setText("Start work");
		} else {
			vbox.setVisible(false);
			labelNoTasks.setVisible(true);
			btnNext.setText(TimerConstants.REFRESH);
			btnNext.setDisable(false);
		}
	}

	public String getBase64ScreenShot() throws AWTException {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();

		Rectangle allScreenBounds = new Rectangle();
		for (GraphicsDevice screen : screens) {
			Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
			allScreenBounds.width += screenBounds.width;
			allScreenBounds.height = Math.max(allScreenBounds.height, screenBounds.height);
		}

		Robot robot = new Robot();
		BufferedImage screenShot = robot.createScreenCapture(allScreenBounds);
		BufferedImage resizedScreenshot = Utils.resizeImage(screenShot, 170, 300);
		Image image = SwingFXUtils.toFXImage(resizedScreenshot, null);

		screenView.setImage(image);

		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(screenShot, "jpg", bos);
			byte[] imageBytes = bos.toByteArray();
			BASE64Encoder encoder = new BASE64Encoder();
			imageString = encoder.encode(imageBytes);
			bos.close();
		} catch (IOException e) {
			log.error("Error: ", e);
		}

		if (screenShotHashCode == 0 || screenShotHashCode != imageString.hashCode()) {
			screenShotHashCode = imageString.hashCode();
			return imageString;
		} else {
			goToLoginScreenWithError(Utils.IDENTICAL_SCREENSHOTS);
			return null;
		}
	}

	private Date getWorkedDate(Date startDate) {
		Instant instant = Instant.ofEpochMilli(startDate.getTime());
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		localDateTime = localDateTime.plusSeconds(workedTime.getSecond());
		localDateTime = localDateTime.plusMinutes(workedTime.getMinute());
		localDateTime = localDateTime.plusHours(workedTime.getHour());
		instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public Label getLabelUsername() {
		return labelUsername;
	}

	public void setLabelUsername(Label labelUsername) {
		this.labelUsername = labelUsername;
	}

	public AnchorPane getPeopleAnchorPane() {
		return peopleAnchorPane;
	}

	public void setPeopleAnchorPane(AnchorPane peopleAnchorPane) {
		this.peopleAnchorPane = peopleAnchorPane;
	}

	public boolean isWorkStarted() {
		return isWorkStarted;
	}

	public void setWorkStarted(boolean workStarted) {
		isWorkStarted = workStarted;
	}

	public void startWork(String timeLogResponse) {
		isWorkStarted = true;
		logOutLink.setDisable(true);
		timerPane.setVisible(true);
		TimeLogDTO timeLogDTO = Utils.getTimeLogFromJson(timeLogResponse);
		lastScreenShotTime = new Date();
		lastStopRequestTime = new Date();
		timerId = timeLogDTO.getId();
		isStarted = true;
	}

	private class SendNotSentDataThread extends Thread {

		@Override
		public void run() {
			super.run();

			Preferences pref = Preferences.userNodeForPackage(HomeController.class);
			Gson gson = new Gson();
			String notSentDataJson = pref.get(TimerConstants.NOT_SENT_DATA, TimerConstants.EMPTY_STRING);
			if (notSentDataJson != null && !notSentDataJson.isEmpty()) {
				NotSentData notSentData = gson.fromJson(notSentDataJson, NotSentData.class);

				List<NameValuePair> parametersPost = new ArrayList<>();
				parametersPost.add(new BasicNameValuePair("time_log_id", String.valueOf(notSentData.getTimerId())));
				parametersPost.add(new BasicNameValuePair("end_time", String.valueOf(notSentData.getDate())));
				try {
					String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + TimerConstants.URL.TIME_LOG_STOP, parametersPost, Utils.JSESSIONID);
					log.debug("time_log/stop Response: {}", response);
					Pattern number = Pattern.compile("\\d+");
					boolean isNumber = false;
					if (response != null) {
						Matcher matcher = number.matcher(response);
						isNumber = matcher.matches();
					}
					if (response == null || isNumber) {
						goToLoginScreenWithError(Utils.INTERNET_ERROR_IN_WORKING);
					} else {
						pref.put("notSentData", "");
						log.info("Data was sent successfully!");
					}
				} catch (IOException | URISyntaxException e) {
					log.error("Error during sending saved data: ", e);
				}

			} else {
				log.info("There are no data to send!");
			}
		}

	}

	private void punishCheater() {
		System.out.println("********************USER IS CHEATING!!!********************");
		if (systemTray != null && trayIcon != null) {
			systemTray.remove(trayIcon);
		}
        /*Stage stage = (Stage) btnNext.getScene().getWindow();
        Preferences pref = Preferences.userNodeForPackage(SettingsController.class);
        Gson gson = new Gson();
        NotSentData notSentData = new NotSentData(timerId,lastScreenShotTime.getTime());
        String notSentDataJson = gson.toJson(notSentData);
        pref.put("notSentData",notSentDataJson);*/
		goToLoginScreenWithError(Utils.CHEATING_ERROR);
	}

	private void saveDataAndGoToLoginScreen(Date endDate) {
		Preferences pref = Preferences.userNodeForPackage(SettingsController.class);
		Gson gson = new Gson();
		NotSentData notSentData = new NotSentData(timerId, endDate.getTime());
		String notSentDataJson = gson.toJson(notSentData);
		pref.put("notSentData", notSentDataJson);
		goToLoginScreenWithError(Utils.INTERNET_ERROR_IN_WORKING);
	}

	private void goToLoginScreenWithError(String error) {
		if (systemTray != null && trayIcon != null) {
			systemTray.remove(trayIcon);
		}
		if (at != null) {
			at.stop();
		}
		Stage stage = (Stage) btnNext.getScene().getWindow();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
		try {
			Parent root = loader.load();
			Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
			LoginController controller = loader.getController();
			controller.showErrorDialog(TimerConstants.EMPTY_STRING, error);

			scene.getWindow().setOnCloseRequest(ev -> System.exit(0));
		} catch (IOException e) {
			log.error("Error: ", e);
		}
	}

	public void showFinishWorkDialog(String taskName, LocalTime workedTime) {
		String time = "";
		if (workedTime.getHour() > 0) {
			time += workedTime.getHour() + "h ";
		}
		if (workedTime.getMinute() > 0) {
			time += workedTime.getMinute() + "m ";
		}
		time += workedTime.getSecond() + "s";
		String message = "You've worked on the task for\n" + time + ".";
		ErrorDialogFactory.createErrorDialog(taskName, message, stackPane).show();
	}

	public void showTaskUpdateDialog() {
		ErrorDialogFactory.createErrorDialog("Task Update", "Your tasks was updated!", stackPane).show();
	}

	void stopApp() {
		if (at != null) {
			at.stop();
		}
		System.exit(0);
	}
}
