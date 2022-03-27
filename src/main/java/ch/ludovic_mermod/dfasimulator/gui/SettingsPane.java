package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.gui.components.Setting;
import ch.ludovic_mermod.dfasimulator.gui.components.SettingGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class SettingsPane extends ScrollPane
{
    public SettingsPane()
    {
        setContent(new SettingGroup(""));
    }
}
