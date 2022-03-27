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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Setting extends HBox
{
    private final ObjectProperty<Object> value;

    private final String     id;
    private final JSONObject jsonSetting;

    public Setting(String id)
    {
        this.id = id;
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(30);

        value = new SimpleObjectProperty<>();
        String[] path = id.split("\\.");

        Text nameText = new Text();
        Strings.bind("settings." + path[path.length - 1], nameText.textProperty());
        getChildren().add(nameText);

        jsonSetting = Constants.getSetting(id);
        switch (jsonSetting.get(".type").getAsString())
        {
            case "double" -> {
                TextField input = new TextField();
                Strings.bind("settings_pane.double_input_prompt", input.promptTextProperty());
                getChildren().add(input);
                input.textProperty().addListener((o, ov, nv) -> {
                    if (nv.matches(Constants.DOUBLE_PATTERN.pattern()))
                        value.set(Double.valueOf(nv));
                });
                input.setText(Constants.getDoubleValue(id).toString());
            }

            case "boolean" -> {
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().addListener((o, ov, nv) -> value.set(ov));
                checkBox.setSelected(Constants.getBooleanValue(id));
                getChildren().add(checkBox);
            }

            case "color" -> {
                ColorPicker picker = new ColorPicker();
                picker.valueProperty().addListener((o, ov, nv) -> value.set(nv));
                picker.setValue(Constants.getColorValue(id));
                getChildren().add(picker);
            }

            default -> throw new IllegalStateException("Unexpected value: " + Constants.getSetting(id).get(".type"));
        }
    }

    public void saveChanges()
    {
        switch (jsonSetting.get(".type").getAsString())
        {
            case "double" -> Constants.getDouble(id).set((Double) value.get());
            case "boolean" -> Constants.getBoolean(id).set((Boolean) value.get());
            case "color" -> Constants.getColor(id).set((Color) value.get());
        }
    }
}
