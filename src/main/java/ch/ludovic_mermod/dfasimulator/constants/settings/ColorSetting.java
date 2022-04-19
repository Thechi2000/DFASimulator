package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

public class ColorSetting extends Setting<Color>
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
    public void setValue(Color newValue)
    {
        (Settings.getBooleanValue("dark_mode") ? darkProperty : lightProperty).set(newValue);
    }
    @Override
    public ObservableValue<Color> computeBinding()
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

    public void setValues(Color dark, Color light)
    {
        darkProperty.set(dark);
        lightProperty.set(light);
    }
    public Color getDark() {return darkProperty.get();}
    public Color getLight() {return lightProperty.get();}
}
