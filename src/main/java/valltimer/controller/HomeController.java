package valltimer.controller;

import com.google.gson.Gson;
import com.jfoenix.controls.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import sun.misc.BASE64Encoder;
import valltimer.Utils;
import valltimer.entity.PrimitiveUser;
import valltimer.entity.Task;
import valltimer.entity.Timer;
import valltimer.model.NotSentData;
import valltimer.model.TreeViewHelper;
import valltimer.webRequest.HTTPRequest;

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
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeController {
    @FXML
    private VBox vbox;
    @FXML
    private TreeView<?> projectsTasksTreeView;
    @FXML
    private JFXTreeView<?> jfxTreeView;
    @FXML
    private JFXButton btnNext,btnSearch, chatButtonBack;
    @FXML
    private Hyperlink logOutLink;
    @FXML
    private Label labelTaskChoose,labelNoTasks,labelNoUsers, labelWorkStarted,
            labelUsername, labelTime,labelTask,labelStartTime,labelTaskName, chatUsernameLabel;
    @FXML
    private ImageView userImage, screenView;
    @FXML
    private JFXTextField usernameSearchTextField;
    @FXML
    private StackPane stackPane;
    @FXML
    private JFXListView<JFXToolbar> foundUsersList;
    @FXML
    private AnchorPane peopleAnchorPane, timerPane, chatPane;

    private TreeViewHelper treeViewHelper;

    private String lastSearchedUsername="";
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

    public void setTray(SystemTray systemTray, TrayIcon trayIcon){
        this.systemTray = systemTray;
        this.trayIcon = trayIcon;
    }

    @FXML
    void initialize() throws IOException, URISyntaxException {
        //Platform.setImplicitExit(false);
        new SendNotSentDataThread().start();

        isCheating = false;
        sendScreenShot=false;
        randomScreenshot=false;
        screenShotInterval = 5*60*1000;//5 minutes


        Properties settings = new Properties();
            /*File file = new File(getClass().getResource("/public/config/settings.conf").toURI());
            settings.load(new FileInputStream(file));*/
        settings.load(getClass().getResourceAsStream("/public/config/settings.conf"));
        sendScreenShot=Boolean.valueOf(settings.getProperty("isScreenshotsSending"));
        randomScreenshot=Boolean.valueOf(settings.getProperty("randomScreenshot"));
        initialScreenshotInterval=Long.valueOf(settings.getProperty("screenShotInterval"));
        screenShotInterval = initialScreenshotInterval;
        System.out.println("screenShotInterval = "+screenShotInterval);



        if (randomScreenshot){
            screenShotInterval = Math.random()*initialScreenshotInterval;
        }

        Utils.homeController=this;
        lastTaskUpdate = new Date();
        lastUtilsIsWorkStartedValue=false;
        Image image = new Image("/public/img/icon_warning.png");
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

        List<PrimitiveUser> primitiveUsers = new ArrayList<>(Arrays.asList(
                new PrimitiveUser(2,"user 1"),
                new PrimitiveUser(3,"user 2"),
                new PrimitiveUser(4,"user 3"),
                new PrimitiveUser(5,"user 4"),
                new PrimitiveUser(6,"user 5"),
                new PrimitiveUser(7,"user 6"),
                new PrimitiveUser(8,"user 7"),
                new PrimitiveUser(9,"user 8")
        ));
        showFoundUsers(primitiveUsers);




        at.start();

    }

    private AnimationTimer at = new AnimationTimer() {
        long lastUpdate = 0;
        @Override
        public void handle(long now) {
            if (now - lastUpdate >1_000_000){
                if (new Date().getTime()-lastTaskUpdate.getTime()>(1*60*1000) && !isWorkStarted){// > 1 minute
                    lastTaskUpdate = new Date();
                    try {
                        Utils.updateSimpleTaskList(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    //showTaskTreeView();
                }
                if (!isWorkStarted) {
                    labelWorkStarted.setVisible(false);
                    logOutLink.setDisable(false);
                    if (!btnNext.getText().equals("Refresh")){
                        btnNext.setText("Start work");
                        logOutLink.setDisable(false);
                        vbox.setVisible(true);
                        btnNext.setDisable(false);
                        TreeItem<?> treeItem = jfxTreeView.getSelectionModel().getSelectedItem();

                        if (treeItem!=null && treeItem.getValue() instanceof Task){
                            btnNext.setDisable(false);
                        }
                        else {
                            btnNext.setDisable(true);
                        }
                    }
                    else {
                        btnNext.setDisable(false);
                    }
                }

                if(isWorkStarted){
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
                    SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    labelTime.setText(currentTime.format(timeFormat));
                    if (startDate != null) {
                        labelStartTime.setText(formater.format(startDate));
                    }
                    String screenShotBase64=null;

                    Date screenTime = new Date();

                    if (screenTime.getTime()-lastStopRequestTime.getTime() >1000*60*1.5){
                        isCheating = true;
                        punishCheater();
                    }
                    else if (screenTime.getTime()- lastStopRequestTime.getTime() >1000*60) {
                        //Sending POST request about finishing work
                        lastStopRequestTime = screenTime;
                        Date endDate = getWorkedDate(startDate);
                        //new StopWorkThread(timerId,endDate.getTime()).start();
                        List<NameValuePair> parametersPost = new ArrayList<>();
                        System.out.println(startDate + " = " + startDate.getTime());
                        System.out.println(endDate + " = " + endDate.getTime());
                        parametersPost.add(new BasicNameValuePair("timer_id", timerId + ""));
                        parametersPost.add(new BasicNameValuePair("endtime", endDate.getTime() + ""));

                        String response  = null;
                        try {
                            response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/stop",parametersPost,Utils.JSESSIONID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        //String response = "";
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

                    if (!isCheating && sendScreenShot && (screenTime.getTime()-lastScreenShotTime.getTime() >screenShotInterval)){
                        try {
                            String lastScreenShotBase64 = screenShotBase64;
                            screenShotBase64 = null;
                            screenShotBase64 = getBase64ScreenShot();
                            if (screenShotBase64 != null){
                                Date date = new Date();
                                List<NameValuePair> parametersPost = new ArrayList<>();
                                parametersPost.add(new BasicNameValuePair("timer_id", timerId+""));
                                parametersPost.add(new BasicNameValuePair("screenshot", screenShotBase64));
                                parametersPost.add(new BasicNameValuePair("date", date.getTime()+""));

                                String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/save_screenshot",parametersPost,Utils.JSESSIONID);
                                Pattern number = Pattern.compile("\\d+");
                                boolean isNumber = false;
                                if (response!= null){
                                    Matcher matcher = number.matcher(response);
                                    isNumber = matcher.matches();
                                }
                                if (response==null || isNumber){
                                    saveDataAndGoToLoginScreen(date);
                                }
                                if (randomScreenshot){
                                    screenShotInterval = Math.random()*initialScreenshotInterval;
                                }
                                lastScreenShotTime = screenTime;
                            }
                            else {
                                Date date = new Date();
                                List<NameValuePair> parametersPost = new ArrayList<>();
                                parametersPost.add(new BasicNameValuePair("timer_id", timerId+""));
                                parametersPost.add(new BasicNameValuePair("screenshot", lastScreenShotBase64));
                                parametersPost.add(new BasicNameValuePair("date", lastScreenShotTime.getTime()+""));

                                String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/save_screenshot",parametersPost,Utils.JSESSIONID);
                                Pattern number = Pattern.compile("\\d+");
                                boolean isNumber = false;
                                if (response!= null){
                                    Matcher matcher = number.matcher(response);
                                    isNumber = matcher.matches();
                                }
                                if (response==null || isNumber){
                                    saveDataAndGoToLoginScreen(date);
                                }
                                System.out.println(Utils.IDENTICAL_SCREENSHOTS);
                            }

                        } catch (AWTException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            lastUpdate = now;
        }
    };

    @FXML
    void onClickBtnNext(ActionEvent event) throws IOException, URISyntaxException {
        if (btnNext.getText().equals("Refresh")){
            refresh();
        }
        else {
            if (!isWorkStarted){
                startTime = LocalTime.now();
                startDate = new Date();
                TreeItem<?> treeItem = jfxTreeView.getSelectionModel().getSelectedItem();
                selectedTask = (Task)treeItem.getValue();
                labelTaskName.setText(selectedTask.getTaskName());
                lastScreenShotTime = new Date();
                lastStopRequestTime = new Date();
                //new StartWorkThread(selectedTask.getPerformerId(),startDate.getTime()).start();
                List<NameValuePair> parametersGet = new ArrayList<>();
                parametersGet.add(new BasicNameValuePair("performer_id",selectedTask.getPerformerId()+""));
                parametersGet.add(new BasicNameValuePair("starttime",startDate.getTime()+""));
                System.out.println(selectedTask.getPerformerId());
                System.out.println(startTime);
                String response  = null;
                try {
                    response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/create",parametersGet,Utils.JSESSIONID);
                    Pattern number = Pattern.compile("\\d+");
                    boolean isNumber = false;
                    if (response!= null){
                        Matcher matcher = number.matcher(response);
                        isNumber = matcher.matches();
                    }
                    if (response==null || isNumber) {
                        goToLoginScreenWithError(Utils.INTERNET_ERROR);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                System.out.println(response);
                if (response != null) {
                    startWork(response);
                }
            }
            else {
                //Sending POST request about finishing work
                Date endDate = this.getWorkedDate(startDate);
                //new StopWorkThread(timerId,endDate.getTime()).start();
                List<NameValuePair> parametersPost = new ArrayList<>();
                System.out.println(startDate+" = "+startDate.getTime());
                System.out.println(endDate+" = "+endDate.getTime());
                parametersPost.add(new BasicNameValuePair("timer_id", timerId+""));
                parametersPost.add(new BasicNameValuePair("endtime", endDate.getTime()+""));

                String response  = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/stop",parametersPost,Utils.JSESSIONID);
                Pattern number = Pattern.compile("\\d+");
                boolean isNumber = false;
                if (response!= null){
                    Matcher matcher = number.matcher(response);
                    isNumber = matcher.matches();
                }
                if (response==null || isNumber){
                    saveDataAndGoToLoginScreen(endDate);
                }
                else {
                    timerPane.setVisible(false);
                    logOutLink.setDisable(false);
                    isWorkStarted=false;
                    showFinishWorkDialog(selectedTask.getTaskName(),workedTime);

                }
            }
        }
    }

    @FXML
    void onClickLogOut(ActionEvent event) throws IOException, ClassNotFoundException {
        if (systemTray!=null && trayIcon!=null){
            systemTray.remove(trayIcon);
        }
        Stage stage = (Stage) logOutLink.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        controller.checkRememberedUser();
        Scene scene = new Scene(root, 325, 450);

        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
            }
        });
        stage.show();
    }

    @FXML
    void onClickBtnSearch(ActionEvent event) {
        /*if (usernameSearchTextField.getText().isEmpty() || lastSearchedUsername.equals(usernameSearchTextField.getText())){
            return;
        }
        List<PrimitiveUser> primitiveUsers = null;
        try {

            List<NameValuePair> parameters = new ArrayList<>();
            lastSearchedUsername = usernameSearchTextField.getText();
            parameters.add(new BasicNameValuePair("username", lastSearchedUsername));
            String response = HTTPRequest.getResponseFromGet(Utils.serverIpAddress+"timer/get_users_by_username", parameters,Utils.JSESSIONID);
            primitiveUsers = Utils.getListOfPrimitiveUsersFromJson(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        showFoundUsers(primitiveUsers);*/
    }

    private void refresh() throws IOException, URISyntaxException {
        Utils.updateSimpleTaskList(true);
    }

    private void showFoundUsers(List<PrimitiveUser> primitiveUsers){
        foundUsersList.getItems().clear();
        if (primitiveUsers==null || primitiveUsers.isEmpty()){
            labelNoUsers.setVisible(true);
            foundUsersList.setVisible(false);
        }
        else {
            labelNoUsers.setVisible(false);
            foundUsersList.setVisible(true);
            HomeController thisController = this;
            for (PrimitiveUser primitiveUser:primitiveUsers){
                JFXToolbar toolbar = new JFXToolbar();

                /*File file = new File("../img/default_user_avatar.png");
                Image image = new Image(file.toURI().toString());*/
                //Image image = Utils.getImageFromBase64(primitiveUser.getBase64photo(),30,30);
                ImageView imageViewLeft = new ImageView(Utils.userIcon);
                imageViewLeft.setFitWidth(30);
                imageViewLeft.setFitHeight(30);
                Circle circle = new Circle(15);
                circle.setCenterX(15);
                circle.setCenterY(15);
                imageViewLeft.setClip(circle);
                toolbar.setLeftItems(imageViewLeft);

                Label labelCenter =  new Label(primitiveUser.getName());
                labelCenter.setAlignment(Pos.CENTER_LEFT);
                labelCenter.setStyle("-fx-pref-width: 245");

                toolbar.setCenter(labelCenter);

                toolbar.setStyle(".jfx-tool-bar-list-item");

                toolbar.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        chatPane.setVisible(true);
                        chatUsernameLabel.setText(labelCenter.getText());
                    }
                });
                foundUsersList.getItems().add(toolbar);
            }
        }

    }

    @FXML
    void obClickChatButtonBack(ActionEvent event) {
        chatPane.setVisible(false);
    }

    public void showTaskTreeView(){

        ArrayList<TreeItem> tasks = treeViewHelper.getTasks();

        if (tasks != null) {
            vbox.setVisible(true);
            labelNoTasks.setVisible(false);
            labelWorkStarted.setVisible(false);
            TreeItem rootItem = new TreeItem("Projects");
            rootItem.getChildren().addAll(tasks);
            jfxTreeView.setRoot(rootItem);
            btnNext.setDisable(true);
            btnNext.setText("Start work");
        }
        else {
            vbox.setVisible(false);
            labelNoTasks.setVisible(true);
            btnNext.setText("Refresh");
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
        BufferedImage screenShot2 = robot.createScreenCapture(allScreenBounds);
        BufferedImage resizedScreenshot = Utils.resizeImage(screenShot,170,300);
        Image image = SwingFXUtils.toFXImage(resizedScreenshot, null);

        screenView.setImage(image);

        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(screenShot, "jpg", bos);

            //ImageIO.write(screenShot, "png", new File("temp.png"));
            byte[] imageBytes = bos.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        };

        if (screenShotHashCode == 0 || screenShotHashCode != imageString.hashCode()){
            screenShotHashCode = imageString.hashCode();
            return imageString;
        }
        else {
            goToLoginScreenWithError(Utils.IDENTICAL_SCREENSHOTS);
            return null;
        }
    }

    public Date getWorkedDate(Date startDate){

        Instant instant = Instant.ofEpochMilli(startDate.getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        localDateTime = localDateTime.plusSeconds(workedTime.getSecond());
        localDateTime = localDateTime.plusMinutes(workedTime.getMinute());
        localDateTime = localDateTime.plusHours(workedTime.getHour());
        instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Date endDate = Date.from(instant);
        return endDate;
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

    public void startWork(String timerResponse){
        isWorkStarted=true;
        logOutLink.setDisable(true);
        timerPane.setVisible(true);

        Timer timer = Utils.getTimerFromJson(timerResponse);
        lastScreenShotTime = new Date();
        lastStopRequestTime = new Date();
        timerId = timer.getId();
        isStarted=true;
    }

    private class SendNotSentDataThread extends Thread {

        @Override
        public void run() {
            super.run();

            Preferences pref = Preferences.userNodeForPackage(HomeController.class);
            Gson gson = new Gson();
            String notSentDataJson =pref.get("notSentData","");
            if (notSentDataJson!=null && !notSentDataJson.isEmpty()){
                NotSentData notSentData = gson.fromJson(notSentDataJson,NotSentData.class);

                List<NameValuePair> parametersPost = new ArrayList<>();
                parametersPost.add(new BasicNameValuePair("timer_id", notSentData.getTimerId()+""));
                parametersPost.add(new BasicNameValuePair("endtime", notSentData.getDate()+""));

                String response = null;
                try {
                    response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress+"timer/stop",parametersPost,Utils.JSESSIONID);
                    Pattern number = Pattern.compile("\\d+");
                    boolean isNumber = false;
                    if (response!= null){
                        Matcher matcher = number.matcher(response);
                        isNumber = matcher.matches();
                    }
                    if (response==null || isNumber){
                        goToLoginScreenWithError(Utils.INTERNET_ERROR_IN_WORKING);
                    }
                    else {
                        pref.put("notSentData","");
                        System.out.println("Data was sent successfully!");
                    }
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }

            }
            else {

                System.out.println("There are no data to send!");
            }
        }

    }

    private void punishCheater(){
        System.out.println("********************USER IS CHEATING!!!********************");
        if (systemTray!=null && trayIcon!=null){
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

    private void saveDataAndGoToLoginScreen(Date endDate){
        Preferences pref = Preferences.userNodeForPackage(SettingsController.class);
        Gson gson = new Gson();
        NotSentData notSentData = new NotSentData(timerId,endDate.getTime());
        String notSentDataJson = gson.toJson(notSentData);
        pref.put("notSentData",notSentDataJson);

        goToLoginScreenWithError(Utils.INTERNET_ERROR_IN_WORKING);
    }

    private void goToLoginScreenWithError(String error){
        if (systemTray!=null && trayIcon!=null){
            systemTray.remove(trayIcon);
        }
        if (at!=null){
            at.stop();
        }
        Stage stage = (Stage) btnNext.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));
        //Parent root = null;
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 325, 450);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            LoginController controller = loader.getController();
            controller.showConnectionErrorDialog(error);

            scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent ev) {
                    System.exit(0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showFinishWorkDialog(String taskname, LocalTime workedTime){
        String time = "";
        if (workedTime.getHour()>0)
            time+=workedTime.getHour()+"h ";
        if (workedTime.getMinute()>0)
            time+=workedTime.getMinute()+"m ";
        time+=workedTime.getSecond()+"s";
        Label labelTaskName = new Label();
        labelTaskName.setMaxWidth(240);
        labelTaskName.setText(taskname);
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(labelTaskName);
        content.setBody(new Text("You've worked on the task for\n"+time+"."));
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

    public void showTaskUpdateDialog(){
        Label labelTaskName = new Label();
        labelTaskName.setMaxWidth(240);
        labelTaskName.setText("Task Update");
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(labelTaskName);
        content.setBody(new Text("Your tasks was updated!"));//Your tasks have been updated!
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

    public void stopApp(){
        if (at != null) {
            at.stop();
        }
        System.exit(0);
        //Platform.exit();
    }

}
