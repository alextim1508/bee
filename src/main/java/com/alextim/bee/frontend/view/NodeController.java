package com.alextim.bee.frontend.view;

import com.alextim.bee.RootController;
import com.alextim.bee.frontend.MainWindow;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.frontend.MainWindow.PARENT_KEY;
import static com.alextim.bee.frontend.MainWindow.ROOT_KEY;

public abstract class NodeController implements Initializable, Adoptable {


    protected RootController rootController;
    protected MainWindow mainWindow;

    protected abstract String getName();

    @Override
    public void adopt(RootController rootController, MainWindow mainWindow, String name, NodeController controller) {
        this.rootController = rootController;
        this.mainWindow = mainWindow;
        this.rootController.addChild(name, controller);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RootController root = (RootController) resources.getObject(ROOT_KEY);
        MainWindow mainWindow = (MainWindow) resources.getObject(PARENT_KEY);
        adopt(root, mainWindow, getName(), this);
    }
}
