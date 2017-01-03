package edu.lehigh.cse.lol;

/**
 * The ScreenManager interface provides a way for programmers to tell LibLOL how to configure a
 * screen.  The codes for building the Splash, Help, Store, Config, and Levels should each be a
 * ScreenManager.
 */
public interface ScreenManager {
    /**
     * Configure a Screen, so that it can be displayed and used by the game player.
     *
     * @param index The numerical index of the screen to display. Your code should use an
     *              <code>if</code> statement to decide what screen to display based on the value of
     *              <code>index</code>
     * @param level The actual level to configure.  Calling methods of <code>level</code> is
     *              the way to start putting stuff into the level.
     */
    void display(int index, Level level);
}
