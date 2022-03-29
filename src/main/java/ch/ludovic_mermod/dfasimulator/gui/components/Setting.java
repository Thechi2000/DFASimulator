package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Components for modifying a single parameter
 */
public class Setting extends HBox
{
    private final ObjectProperty<Object> value;

    private final String     id;
    private final JSONObject jsonSetting;

    /**
     * Constructs a Setting component for the setting with the given id
     * @param id id of the setting to represent
     */
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
                checkBox.selectedProperty().addListener((o, ov, nv) -> value.set(nv));
                checkBox.setSelected(Constants.getBooleanValue(id));
                value.set(checkBox.isSelected());
                getChildren().add(checkBox);
            }

            case "color" -> {
                ColorPicker picker = new ColorPicker();
                picker.valueProperty().addListener((o, ov, nv) -> value.set(nv));
                picker.setValue(Constants.getColorValue(id));
                getChildren().add(picker);
            }

            case "font" -> {
                ChoiceBox<String> fontName = new ChoiceBox<>(FXCollections.observableList(Font.getFontNames()));
                value.set(Constants.getFontValue(id));
                fontName.valueProperty().addListener((o, ov, nv) -> value.set(new Font(nv, ((Font) value.get()).getSize())));
                fontName.setValue(Constants.getFontValue(id).getName());

                TextField size = new TextField();
                Strings.bind("settings_pane.font_size", size.promptTextProperty());
                size.textProperty().addListener((o, ov, nv) -> {
                    if (nv.matches(Constants.DOUBLE_PATTERN.pattern()))
                        value.set(new Font(((Font) value.get()).getName(), Double.parseDouble(nv)));
                    System.out.println("f");
                });
                size.setText(String.valueOf(Constants.getFontValue(id).getSize()));

                getChildren().addAll(fontName, size);
            }

            default -> throw new IllegalStateException("Unexpected value: " + Constants.getSetting(id).get(".type"));
        }
    }

    /**
     * Save the current value to the Constants
     */
    public void saveChanges()
    {
        switch (jsonSetting.get(".type").getAsString())
        {
            case "double" -> {
                Double val = (Double) value.get();
                Constants.getDouble(id).set(val);
                jsonSetting.addProperty(".value", val);
            }
            case "boolean" -> {
                Boolean val = (Boolean) value.get();
                Constants.getBoolean(id).set(val);
                jsonSetting.addProperty(".value", val);
            }
            case "color" -> {
                Color val = (Color) value.get();
                Constants.getColor(id).set(val);
                jsonSetting.addProperty(".value", val.toString());
            }
            case "font" -> {
                Font val = (Font) value.get();
                Constants.getFont(id).set(val);
                jsonSetting.addProperty(".name", val.getName());
                jsonSetting.addProperty(".size", val.getSize());
            }
        }
    }
}
