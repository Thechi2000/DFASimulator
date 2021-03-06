package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONable;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.Utils;
import javafx.beans.value.ObservableValue;

import java.util.function.Supplier;

public abstract class Setting<T> implements JSONable
{
    public static final String JSON_TYPE = ".type";

    private         ObservableValue<T> valueBinding;
    protected final Type               type;

    protected Setting(Type type)
    {
        this.type = type;
    }

    public static Setting<?> loadFromJSON(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasString(JSON_TYPE);
        Setting<?> setting = Type.valueOf(object.get(JSON_TYPE).getAsString()).factory.get();
        setting.load(object);
        return setting;
    }

    protected abstract void load(JSONObject object) throws IOManager.CorruptedFileException;
    @Override
    public JSONObject getJSONObject()
    {
        JSONObject object = new JSONObject();
        object.addProperty(JSON_TYPE, type.toString());
        return object;
    }

    public abstract void setValue(T newValue);
    protected abstract ObservableValue<T> computeBinding();

    public ObservableValue<T> getValueBinding()
    {
        if (valueBinding == null) valueBinding = computeBinding();
        return valueBinding;
    }

    public Type getType()
    {
        return type;
    }

    public enum Type
    {
        DOUBLE(() -> new SimpleSetting<>(Type.valueOf("DOUBLE"), Utils.stringConverter(Object::toString, Double::parseDouble), 0d)),
        BOOLEAN(() -> new SimpleSetting<>(Type.valueOf("BOOLEAN"), Utils.stringConverter(Object::toString, Boolean::parseBoolean), false)),
        FONT(() -> new FontSetting()),
        COLOR(() -> new ColorSetting());

        public final Supplier<Setting<?>> factory;

        Type(Supplier<Setting<?>> factory) {this.factory = factory;}
    }
}
