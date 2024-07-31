package com.alextim.bee.frontend.view;


import com.alextim.bee.RootController;
import com.alextim.bee.frontend.MainWindow;

public interface Adoptable {
    void adopt(RootController parent, MainWindow mainWindow, String name, NodeController controller);
}
