package ch.ludovic_mermod.dfasimulator.utils;

/**
 * Represents a Mutex (or a lock)
 */
public class Mutex
{
    private boolean isLocked = false;

    /**
     * Lock the mutex
     */
    public void lock()
    {
        isLocked = true;
    }
    /**
     * Unlock the mutes
     */
    public void unlock()
    {
        isLocked = false;
    }

    /**
     * @return whether the mutex is locked
     */
    public boolean isLocked()
    {
        return isLocked;
    }
}
