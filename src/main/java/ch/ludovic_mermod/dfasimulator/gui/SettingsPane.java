package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.components.SettingGroup;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SettingsPane extends BorderPane
{
    private final SettingGroup settingGroup;
    public SettingsPane()
    {
        settingGroup = new SettingGroup("");

        Button ok = new Button();
        Strings.bind("ok", ok.textProperty());
        ok.setOnAction(event -> {
            saveChanges();
            getScene().getWindow().hide();
        });

        Button apply = new Button();
        Strings.bind("apply", apply.textProperty());
        apply.setOnAction(event -> saveChanges());

        Button cancel = new Button();
        Strings.bind("cancel", cancel.textProperty());
        cancel.setOnAction(event -> getScene().getWindow().hide());

        HBox buttonsBox = new HBox(ok, apply, cancel);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        setCenter(new ScrollPane(settingGroup));
        setBottom(buttonsBox);
    }

    private void saveChanges()
    {
        settingGroup.saveChanges();
        Constants.saveToFile("settings.json");
    }
}
