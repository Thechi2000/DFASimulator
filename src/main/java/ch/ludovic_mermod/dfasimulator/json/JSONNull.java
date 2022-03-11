package ch.ludovic_mermod.dfasimulator.json;

public class JSONNull extends JSONElement
{
    public static JSONNull INSTANCE = new JSONNull();
    private JSONNull()
    {
    }

    @Override
    public JSONElement deepCopy()
    {
        return INSTANCE;
    }

    @Override
    public String toString()
    {
        return "null";
    }
}
