package ch.ludovic_mermod.dfasimulator.json;

import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("unused")
public class JSONPrimitive extends JSONElement
{
    private final Object value;

    public JSONPrimitive(Boolean bool)
    {
        value = bool;
    }
    public JSONPrimitive(Number number)
    {
        value = number;
    }
    public JSONPrimitive(String string)
    {
        value = string;
    }
    public JSONPrimitive(Character c)
    {
        value = c;
    }

    public JSONPrimitive(JsonPrimitive primitive)
    {
        Object val = null;
        if (primitive.isBoolean()) val = primitive.getAsBoolean();
        if (primitive.isNumber())
        {
            val = primitive.getAsNumber();
        }
        if (primitive.isString()) val = primitive.getAsString();
        value = val;
    }

    private static boolean isIntegral(JSONPrimitive primitive)
    {
        return primitive.value instanceof Number number && (number instanceof BigInteger || number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte);
    }

    public JSONPrimitive deepCopy()
    {
        return this;
    }

    public boolean getAsBoolean()
    {
        return isBoolean() ? (Boolean) value : Boolean.parseBoolean(getAsString());
    }
    public Number getAsNumber()
    {
        return isNumber() ? (Number) value : null;
    }
    public String getAsString()
    {
        return isNumber() ? getAsNumber().toString() : isBoolean() ? ((Boolean) value).toString() : (String) value;
    }

    public double getAsDouble()
    {
        return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
    }
    public float getAsFloat()
    {
        return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
    }
    public long getAsLong()
    {
        return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
    }
    public int getAsInt()
    {
        return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
    }
    public byte getAsByte()
    {
        return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
    }
    public BigDecimal getAsBigDecimal()
    {
        return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
    }
    public BigInteger getAsBigInteger()
    {
        return value instanceof BigInteger ? (BigInteger) value : new BigInteger(value.toString());
    }
    public short getAsShort()
    {
        return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
    }
    public char getAsCharacter()
    {
        return getAsString().charAt(0);
    }

    public boolean isBoolean()
    {
        return value instanceof Boolean;
    }
    public boolean isNumber()
    {
        return value instanceof Number;
    }
    public boolean isString()
    {
        return value instanceof String;
    }

    public int hashCode()
    {
        if (value == null)
        {
            return 31;
        }
        else
        {
            long l;
            if (isIntegral(this))
            {
                l = getAsNumber().longValue();
                return (int) (l ^ l >>> 32);
            }
            else if (value instanceof Number)
            {
                l = Double.doubleToLongBits(getAsNumber().doubleValue());
                return (int) (l ^ l >>> 32);
            }
            else
            {
                return value.hashCode();
            }
        }
    }
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        else if (obj != null && getClass() == obj.getClass())
        {
            JSONPrimitive other = (JSONPrimitive) obj;
            if (value == null)
            {
                return other.value == null;
            }
            else if (isIntegral(this) && isIntegral(other))
            {
                return getAsNumber().longValue() == other.getAsNumber().longValue();
            }
            else if (value instanceof Number && other.value instanceof Number)
            {
                double a = getAsNumber().doubleValue();
                double b = other.getAsNumber().doubleValue();
                return a == b || Double.isNaN(a) && Double.isNaN(b);
            }
            else
            {
                return value.equals(other.value);
            }
        }
        else
            return false;
    }

    @Override
    public String toString()
    {
        return isString() ? String.format("\"%s\"", getAsString()) : value.toString();
    }
}
