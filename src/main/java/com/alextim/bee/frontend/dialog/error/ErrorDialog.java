package com.alextim.bee.frontend.dialog.error;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j

public class ErrorDialog extends Dialog<Boolean> {

    private final String FXML_FILE = "newError.fxml";
    private final String TITLE = "Ошибка";

    public ErrorDialog(String header, String body) {
        log.info("ErrorDialog");
        init(header, body);

        setTitle(TITLE);
    }


    @SneakyThrows
    private void init(String header, String body) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_FILE));
        getDialogPane().setContent(loader.load());
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        ErrorController controller = loader.getController();
        if(header != null) {
            initByThrowable(controller, header, body);
            ((Button)getDialogPane().lookupButton(ButtonType.APPLY)).setText("OK");
        }

        setResultConverter(buttonType -> {
            if(buttonType != null && buttonType.equals(ButtonType.APPLY))
                return controller.getResult();
            return null;
        });

        setResizable(true);
    }

    private void initByThrowable(ErrorController controller, String header, String body) {
        controller.setHeader(header);
        controller.setDescription(body);
    }
}
