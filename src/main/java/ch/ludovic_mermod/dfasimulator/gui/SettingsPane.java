package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.gui.components.Setting;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class SettingsPane extends ScrollPane
{
    public SettingsPane()
    {
        VBox content = new VBox();
        Constants.keySet().forEach(k -> content.getChildren().add(new Setting(k)));
        setContent(content);
    }
}
