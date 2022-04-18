package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

public class SimpleSetting extends Setting
{
    public static final String JSON_VALUE = "value";

    private final ObjectProperty<Object>  valueProperty;
    private final StringConverter<Object> converter;

    protected SimpleSetting(Type type, StringConverter<Object> converter, Object initialValue)
    {
        super(type);
        this.converter = converter;
        valueProperty = new SimpleObjectProperty<>(initialValue);
    }

    @Override
    public void setValue(Object newValue)
    {
        valueProperty.set(newValue);
    }
    @Override
    public ObservableValue<Object> getValueBinding()
    {
        return valueProperty;
    }

    @Override
    protected void load(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasString(JSON_VALUE);
        valueProperty.set(converter.fromString(object.get(JSON_VALUE).getAsString()));
    }

    @Override
    public JSONObject getJSONObject()
    {
        JSONObject object = super.getJSONObject();
        object.addProperty(JSON_VALUE, converter.toString(valueProperty.get()));
        return object;
    }
}
