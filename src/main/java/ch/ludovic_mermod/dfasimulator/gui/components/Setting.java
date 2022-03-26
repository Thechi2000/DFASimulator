package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Setting extends HBox
{
    private final JSONObject             settingJson;
    private final ObjectProperty<Object> value;

    public Setting(String id)
    {
        settingJson = Constants.getSetting(id);
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(30);

        value = new SimpleObjectProperty<>();

        Text nameText = new Text();
        Strings.bind("settings." + id, nameText.textProperty());
        getChildren().add(nameText);

        switch (settingJson.get(".type").getAsString())
        {
            case "double":
                TextField input = new TextField();
                Strings.bind("settings_pane.double_input_prompt", input.promptTextProperty());
                getChildren().add(input);

                input.textProperty().addListener((o, ov, nv) -> {
                    if (nv.matches(Constants.DOUBLE_PATTERN.pattern()))
                        value.set(Double.valueOf(nv));
                });
                input.setText(Constants.getDoubleValue(id).toString());
                break;

            case "boolean":
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().addListener((o, ov, nv) -> value.set(ov));
                checkBox.setSelected(Constants.getBooleanValue(id));
                getChildren().add(checkBox);
                break;

            case "color":
                ColorPicker picker = new ColorPicker();
                picker.valueProperty().addListener((o, ov, nv) -> value.set(nv));
                picker.setValue(Constants.getColorValue(id));
                getChildren().add(picker);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + settingJson.get(".type"));
        }
    }
}
