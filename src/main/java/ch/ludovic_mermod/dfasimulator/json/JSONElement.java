package ch.ludovic_mermod.dfasimulator.json;

import ch.ludovic_mermod.dfasimulator.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class JSONElement
{
    protected static void bindListeners(JSONElement element, JSONElement caller, Set<ChildUpdateListener> listeners)
    {
        if (element instanceof JSONObject)
        {
            ((JSONObject) element).addListener((o, ov, nv) -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONObject) element).addListener((SetChangeListener<? super Map.Entry<String, JSONElement>>) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONObject) element).addListener((ChildUpdateListener) c -> listeners.forEach(l -> l.onChildUpdate(c)));
        }

        if (element instanceof JSONArray)
        {
            ((JSONArray) element).addListener((ListChangeListener<? super JSONElement>) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONArray) element).addListener((InvalidationListener) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONArray) element).addListener((ChildUpdateListener) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
        }
    }

    protected static void unbindListeners(JSONElement element, JSONElement caller, Set<ChildUpdateListener> listeners)
    {
        if (element instanceof JSONObject)
        {
            ((JSONObject) element).removeListener((o, ov, nv) -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONObject) element).removeListener((SetChangeListener<? super Map.Entry<String, JSONElement>>) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONObject) element).removeListener((ChildUpdateListener) c -> listeners.forEach(l -> l.onChildUpdate(c)));
        }

        if (element instanceof JSONArray)
        {
            ((JSONArray) element).removeListener((ListChangeListener<? super JSONElement>) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONArray) element).removeListener((InvalidationListener) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
            ((JSONArray) element).removeListener((ChildUpdateListener) change -> listeners.forEach(l -> l.onChildUpdate(caller)));
        }
    }

    public static JSONElement parse(String str)
    {
        return convert(JsonParser.parseString(str));
    }
    protected static JSONElement convert(JsonElement element)
    {
        if (element.isJsonObject()) return new JSONObject(element.getAsJsonObject());
        if (element.isJsonArray()) return new JSONArray(element.getAsJsonArray());
        if (element.isJsonPrimitive()) return new JSONPrimitive(element.getAsJsonPrimitive());
        if (element.isJsonNull()) return JSONNull.INSTANCE;
        return null;
    }

    public static JSONElement readFromFile(String filename)
    {
        try (BufferedReader in = new BufferedReader(new FileReader(filename)))
        {
            final String collect = in.lines().collect(Collectors.joining("\n"));
            return JSONElement.parse(collect);
        }
        catch (IOException e)
        {
            Main.logger.log(Level.SEVERE, "Could not parse JSON from file \"%s\"", filename);
            return new JSONObject();
        }
    }
    public void saveToFile(String filename)
    {
        File file = new File(filename);
        try
        {
            if (file.exists() || file.createNewFile())
                try (FileOutputStream o = new FileOutputStream(file))
                {
                    o.write(toString().getBytes(StandardCharsets.UTF_8));
                }
            else
                Main.log(Level.SEVERE, "Could not create file " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            Main.log(Level.SEVERE, "Could not save DFA", e);
        }
    }

    public abstract JSONElement deepCopy();
    public JSONObject getAsJSONObject()
    {
        if (this.isJSONObject())
        {
            return (JSONObject) this;
        }
        else
        {
            throw new IllegalStateException("Not a JSON Object: " + this);
        }
    }
    public JSONArray getAsJSONArray()
    {
        if (this.isJSONArray())
        {
            return (JSONArray) this;
        }
        else
        {
            throw new IllegalStateException("Not a JSON Array: " + this);
        }
    }
    public JSONPrimitive getAsJSONPrimitive()
    {
        if (this.isJSONPrimitive())
        {
            return (JSONPrimitive) this;
        }
        else
        {
            throw new IllegalStateException("Not a JSON Primitive: " + this);
        }
    }
    public JSONNull getAsJSONNull()
    {
        if (this.isJSONNull())
        {
            return (JSONNull) this;
        }
        else
        {
            throw new IllegalStateException("Not a JSON Null: " + this);
        }
    }
    public boolean isJSONArray()
    {
        return this instanceof JSONArray;
    }
    public boolean isJSONObject()
    {
        return this instanceof JSONObject;
    }
    public boolean isJSONPrimitive()
    {
        return this instanceof JSONPrimitive;
    }
    public boolean isJSONNull()
    {
        return this instanceof JSONNull;
    }

    public boolean getAsBoolean()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public Number getAsNumber()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public String getAsString()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public double getAsDouble()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public float getAsFloat()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public long getAsLong()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public int getAsInt()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public byte getAsByte()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public BigDecimal getAsBigDecimal()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public BigInteger getAsBigInteger()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }
    public short getAsShort()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public char getAsCharacter()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    @FunctionalInterface
    public interface ChildUpdateListener
    {
        void onChildUpdate(JSONElement child);
    }
}