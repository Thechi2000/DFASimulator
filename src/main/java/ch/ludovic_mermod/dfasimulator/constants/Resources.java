package ch.ludovic_mermod.dfasimulator.constants;

public class Resources
{
    /**
     * @param name name of the sought file
     * @return the path to the file in the resource directory
     */
    public static String get(String name)
    {
        return "resources/" + name;
    }
    public static String getStyle(String name)
    {
        return Resources.class.getResource("/style/" + name + ".css").toExternalForm();
    }
}
