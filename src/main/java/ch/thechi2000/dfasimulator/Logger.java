package ch.thechi2000.dfasimulator;

import java.util.ResourceBundle;

public class Logger implements System.Logger
{
    @Override
    public String getName()
    {
        return "logger";
    }

    @Override
    public boolean isLoggable(Level level)
    {
        return true;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown)
    {
        System.out.printf("[%s] Throwable: %s\n", level, msg);
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params)
    {
        System.out.printf("[%s] %s\n", level, String.format(format, params));
    }
}
