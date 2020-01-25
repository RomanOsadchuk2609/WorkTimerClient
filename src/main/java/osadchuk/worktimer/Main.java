package osadchuk.worktimer;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import osadchuk.worktimer.util.TimerConstants;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(TimerConstants.APP.NAME);
            alreadyRunning = false;
            System.out.println("alreadyRunning = false;");
        } catch (AlreadyLockedException e) {
            alreadyRunning = true;
            System.out.println("alreadyRunning = true;");
        }
        if (alreadyRunning) {
            Platform.exit();
            // Start sequence here
        }

        Utils.hostServices = HostServicesFactory.getInstance(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle(TimerConstants.APP.NAME);
        Scene scene = new Scene(root, TimerConstants.APP.WIDTH, TimerConstants.APP.HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }
}
