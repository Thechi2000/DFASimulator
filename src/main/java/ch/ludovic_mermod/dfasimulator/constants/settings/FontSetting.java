package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Font;

public class FontSetting extends Setting
{
    public static final String JSON_NAME = "name";
    public static final String JSON_SIZE = "size";

    private final ObjectProperty<Font> fontProperty;

    protected FontSetting()
    {
        super(Type.FONT);
        fontProperty = new SimpleObjectProperty<>(Font.getDefault());
    }

    @Override
    protected void load(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasString(JSON_NAME);
        object.checkHasNumber(JSON_SIZE);

        fontProperty.set(Font.font(object.get(JSON_NAME).getAsString(), object.get(JSON_SIZE).getAsDouble()));
    }

    @Override
    public void setValue(Object newValue)
    {
        if (!(newValue instanceof Font))
            throw new IllegalArgumentException(String.format("Invalid value set in FontSetting %s", newValue.getClass().getName()));

        fontProperty.set((Font) newValue);
    }

    @Override
    public ObservableValue<Object> getValueBinding()
    {
        return CustomBindings.create(fontProperty::get, fontProperty);
    }

    @Override
    public JSONObject getJSONObject()
    {
        var object = super.getJSONObject();
        object.addProperty(JSON_NAME, fontProperty.get().getName());
        object.addProperty(JSON_SIZE, fontProperty.get().getSize());
        return object;
    }
}
