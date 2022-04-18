package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

public class ColorSetting extends Setting
{
    public static final String JSON_LIGHT = "light";
    public static final String JSON_DARK  = "dark";

    private final ObjectProperty<Color> lightProperty;
    private final ObjectProperty<Color> darkProperty;

    protected ColorSetting()
    {
        super(Type.COLOR);
        lightProperty = new SimpleObjectProperty<>(Color.BLACK);
        darkProperty = new SimpleObjectProperty<>(Color.BLACK);
    }

    @Override
    void setValue(Object newValue)
    {
        if (!(newValue instanceof Color))
            throw new IllegalArgumentException(String.format("Invalid value set in ColorSetting %s", newValue.getClass().getName()));

        (Settings.getBooleanValue("dark_mode") ? darkProperty : lightProperty).set((Color) newValue);
    }
    @Override
    ObservableValue<Object> getValueBinding()
    {
        ObservableValue<Boolean> darkMode = Settings.getBoolean("dark_mode");
        return CustomBindings.create(() -> darkMode.getValue() ? darkProperty.get() : lightProperty.get(), darkMode, darkProperty, lightProperty);
    }

    @Override
    public JSONObject getJSONObject()
    {
        var object = super.getJSONObject();
        object.addProperty(JSON_LIGHT, lightProperty.get().toString());
        object.addProperty(JSON_DARK, darkProperty.get().toString());
        return object;
    }

    @Override
    protected void load(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasString(JSON_LIGHT);
        object.checkHasString(JSON_DARK);

        lightProperty.set(Color.valueOf(object.get(JSON_LIGHT).getAsString()));
        darkProperty.set(Color.valueOf(object.get(JSON_DARK).getAsString()));
    }
}
