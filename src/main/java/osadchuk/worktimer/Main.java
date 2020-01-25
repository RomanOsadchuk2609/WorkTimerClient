package osadchuk.worktimer;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);

        String appId = "WorkTimer";
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(appId);
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

        HostServicesDelegate hostServices = HostServicesFactory.getInstance(this);
        Utils.hostServices = hostServices;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/public/fxml/login.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("WorkTimer");
        Scene scene = new Scene(root, 325, 450);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);

    }


}
