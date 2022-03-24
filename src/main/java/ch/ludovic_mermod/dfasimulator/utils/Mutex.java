package ch.ludovic_mermod.dfasimulator.utils;

public class Mutex
{
    private boolean isLocked = false;

    public void lock()
    {
        isLocked = true;
    }
    public void unlock()
    {
        isLocked = false;
    }
    public boolean isLocked()
    {
        return isLocked;
    }
}
