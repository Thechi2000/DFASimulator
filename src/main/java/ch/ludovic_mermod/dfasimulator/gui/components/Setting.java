package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.constants.settings.ColorSetting;
import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.regex.Pattern;

/**
 * Components for modifying a single parameter
 */
public class Setting extends HBox
{
    public static final Pattern DOUBLE_PATTERN = Pattern.compile("(\\d+\\.?\\d*|\\d*\\.\\d+)d?");

    private final ObjectProperty<Object> value;

    private final ch.ludovic_mermod.dfasimulator.constants.settings.Setting setting;
    private final String                                                    id;

    /**
     * Constructs a Setting component for the setting with the given id
     *
     * @param id id of the setting to represent
     */
    public Setting(String id)
    {
        this.id = id;
        setting = Settings.getSetting(id);

        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(30);

        value = new SimpleObjectProperty<>();
        String[] path = id.split("\\.");

        Text nameText = new Text();
        Strings.bind("settings." + path[path.length - 1], nameText.textProperty());
        getChildren().add(nameText);

        switch (setting.getType())
        {
            case DOUBLE ->
            {
                TextField input = new TextField();
                Strings.bind("settings_pane.double_input_prompt", input.promptTextProperty());
                getChildren().add(input);
                input.textProperty().addListener((o, ov, nv) -> {
                    if (nv.matches(DOUBLE_PATTERN.pattern()))
                        value.set(Double.valueOf(nv));
                });
                input.setText(Settings.getDoubleValue(id).toString());
            }

            case BOOLEAN ->
            {
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().addListener((o, ov, nv) -> value.set(nv));
                checkBox.setSelected(Settings.getBooleanValue(id));
                value.set(checkBox.isSelected());
                getChildren().add(checkBox);
            }

            case COLOR ->
            {
                ColorPicker darkPicker = new ColorPicker();
                ColorPicker lightPicker = new ColorPicker();

                ColorSetting colorSetting = (ColorSetting) setting;

                darkPicker.setValue(colorSetting.getDark());
                lightPicker.setValue(colorSetting.getLight());
                value.set(new Pair<>(colorSetting.getDark(), colorSetting.getLight()));

                darkPicker.valueProperty().addListener((o, ov, nv) -> value.set(new Pair<>(darkPicker.getValue(), lightPicker.getValue())));
                lightPicker.valueProperty().addListener((o, ov, nv) -> value.set(new Pair<>(darkPicker.getValue(), lightPicker.getValue())));

                getChildren().addAll(lightPicker, darkPicker);
            }

            case FONT ->
            {
                ComboBox<String> fontName = new ComboBox<>(FXCollections.observableList(Font.getFontNames()));
                fontName.setVisibleRowCount(10);
                value.set(Settings.getFontValue(id));
                fontName.valueProperty().addListener((o, ov, nv) -> value.set(new Font(nv, ((Font) value.get()).getSize())));
                fontName.setValue(Settings.getFontValue(id).getName());

                TextField size = new TextField();
                Strings.bind("settings_pane.font_size", size.promptTextProperty());
                size.textProperty().addListener((o, ov, nv) -> {
                    if (nv.matches(DOUBLE_PATTERN.pattern()))
                        value.set(new Font(((Font) value.get()).getName(), Double.parseDouble(nv)));
                });
                size.setText(String.valueOf(Settings.getFontValue(id).getSize()));

                getChildren().addAll(fontName, size);
            }

            default -> throw new IllegalStateException("Unexpected value: " + setting.getType());
        }
    }

    /**
     * Save the current value to the Settings
     */
    public void saveChanges()
    {
        switch (setting.getType())
        {
            case COLOR ->
            {
                var colors = (Pair<Color, Color>) value.get();
                ((ColorSetting) setting).setValues(colors.getKey(), colors.getValue());
            }

            default -> setting.setValue(value.getValue());
        }

    }
}
