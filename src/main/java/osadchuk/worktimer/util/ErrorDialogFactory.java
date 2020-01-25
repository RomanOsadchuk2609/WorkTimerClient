package osadchuk.worktimer.util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ErrorDialogFactory {
    public static JFXDialog createErrorDialog(String header, String body, StackPane stackPane) {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label(header));
        content.setBody(new Text(body));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton(TimerConstants.OKAY);
        button.setOnAction(event -> dialog.close());
        content.setActions(button);
        dialog.setOverlayClose(false);
        return dialog;
    }
}
