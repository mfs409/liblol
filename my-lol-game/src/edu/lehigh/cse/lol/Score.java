
package edu.lehigh.cse.lol;

import com.badlogic.gdx.utils.Timer;

import edu.lehigh.cse.lol.Level.Action;

/**
 * Score is used by Level to track the progress through a level. There are four
 * things tracked: the number of heroes created and destroyed, the number of
 * enemies created and destroyed, the number of heroes at destinations, and the
 * number of (each type of) goodie that has been collected. Apart from storing
 * the counts, this class provides a public interface for manipulating the
 * goodie counts, and a set of internal convenience methods for updating values
 * and checking for win/lose. It also manages the mode of the level (i.e., what
 * must be done to finish the level... collecting goodies, reaching a
 * destination, etc).
 */
public class Score {
    /*
     * COUNTERS
     */

    /**
     * Track the number of heroes that have been created
     */
    int _heroesCreated = 0;

    /**
     * Track the number of heroes that have been removed/defeated
     */
    int _heroesDefeated = 0;

    /**
     * Count of the goodies that have been collected in this level
     */
    int[] _goodiesCollected = new int[] {
            0, 0, 0, 0
    };

    /**
     * Number of heroes who have arrived at any destination yet
     */
    int _destinationArrivals = 0;

    /**
     * Count the number of enemies that have been created
     */
    int _enemiesCreated = 0;

    /**
     * Count the enemies that have been defeated
     */
    int _enemiesDefeated = 0;

    /*
     * WIN MODE OF THE CURRENT LEVEL
     */

    /**
     * these are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you can reach a certain
     * number of enemies defeated Technically, there's also 'survive for x
     * seconds', but that doesn't need special support
     */
    enum VictoryType {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

    /**
     * Describes how a level is won
     */
    VictoryType _victoryType = VictoryType.DESTINATION;

    /**
     * This is the number of heroes who must reach destinations, if we're in
     * DESTINATION mode
     */
    int _victoryHeroCount;

    /**
     * This is the number of goodies that must be collected, if we're in
     * GOODIECOUNT mode
     */
    int[] _victoryGoodieCount = new int[4];

    /**
     * This is the number of enemies that must be defeated, if we're in
     * ENEMYCOUNT mode. -1 means "all of them"
     */
    int _victoryEnemyCount;

    /**
     * Track if the level has been lost (true) or the game is still being played
     * (false)
     */
    boolean _gameOver = false;

    /*
     * INTERNAL INTERFACE
     */

    /**
     * Use this to inform the level that a hero has been defeated
     * 
     * @param e The enemy who defeated the hero
     */
    void defeatHero(Enemy e) {
        _heroesDefeated++;
        if (_heroesDefeated == _heroesCreated) {
            if (e._onDefeatHeroText != "")
                PostScene.setDefaultLoseText(e._onDefeatHeroText);
            endLevel(false);
        }
    }

    /**
     * Use this to inform the level that a goodie has been collected by a hero
     * 
     * @param g The goodie that was collected
     */
    void onGoodieCollected(Goodie g) {
        // Update goodie counts
        for (int i = 0; i < 4; ++i)
            _goodiesCollected[i] += g._score[i];

        // possibly win the level, but only if we win on goodie count and all
        // four counts are high enough
        if (_victoryType != VictoryType.GOODIECOUNT)
            return;
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= _victoryGoodieCount[i] <= _goodiesCollected[i];
        if (match)
            endLevel(true);
    }

    /**
     * Use this to inform the level that a hero has reached a destination
     * 
     * @param d The destination that the hero reached
     */
    void onDestinationArrive() {
        // check if the level is complete
        _destinationArrivals++;
        if ((_victoryType == VictoryType.DESTINATION)
                && (_destinationArrivals >= _victoryHeroCount))
            endLevel(true);
    }

    /**
     * Internal method for handling whenever an enemy is defeated
     */
    void onDefeatEnemy() {
        // update the count of defeated enemies
        _enemiesDefeated++;

        // if we win by defeating enemies, see if we've defeated enough of them:
        boolean win = false;
        if (_victoryType == VictoryType.ENEMYCOUNT) {
            // -1 means "defeat all enemies"
            if (_victoryEnemyCount == -1)
                win = _enemiesDefeated == _enemiesCreated;
            else
                win = _enemiesDefeated >= _victoryEnemyCount;
        }
        if (win)
            endLevel(true);
    }

    /**
     * When a level ends, we run this code to shut it down, print a message, and
     * then let the user resume play
     * 
     * @param win /true/ if the level was won, /false/ otherwise
     */
    void endLevel(final boolean win) {
        if (Level._currLevel._endGameEvent == null)
        Level._currLevel._endGameEvent = new Action(){
            @Override
            public void go() {
                // Safeguard: only call this method once per level
                if (_gameOver)
                    return;
                _gameOver = true;

                // Run the level-complete trigger
                LOL._game.levelCompleteTrigger(win);

                // if we won, unlock the next level
                if (win && LOL._game.readUnlocked() == LOL._game._currLevel)
                    LOL._game.saveUnlocked(LOL._game._currLevel + 1);

                // drop everything from the hud
                Level._currLevel._controls.clear();

                // clear any pending timers
                Timer.instance().clear();

                // display the PostScene, which provides a pause before we retry/start
                // the next level
                Level._currLevel._postScene.setWin(win);                
            }};
    }

    /*
     * PUBLIC INTERFACE FOR MANAGING GOODIE COUNTS
     */

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     * 
     * @param value The new value
     */
    public static void setGoodiesCollected1(int value) {
        Level._currLevel._score._goodiesCollected[0] = value;
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     * 
     * @param value The new value
     */
    public static void setGoodiesCollected2(int value) {
        Level._currLevel._score._goodiesCollected[1] = value;
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     * 
     * @param value The new value
     */
    public static void setGoodiesCollected3(int value) {
        Level._currLevel._score._goodiesCollected[2] = value;
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     * 
     * @param value The new value
     */
    public static void setGoodiesCollected4(int value) {
        Level._currLevel._score._goodiesCollected[3] = value;
    }

    /**
     * Manually increment the number of goodies of type 1 that have been
     * collected.
     */
    public static void incrementGoodiesCollected1() {
        Level._currLevel._score._goodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public static void incrementGoodiesCollected2() {
        Level._currLevel._score._goodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public static void incrementGoodiesCollected3() {
        Level._currLevel._score._goodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public static void incrementGoodiesCollected4() {
        Level._currLevel._score._goodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected1() {
        return Level._currLevel._score._goodiesCollected[0];
    }

    /**
     * Getter for number of goodies of type 2 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected2() {
        return Level._currLevel._score._goodiesCollected[1];
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected3() {
        return Level._currLevel._score._goodiesCollected[2];
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected4() {
        return Level._currLevel._score._goodiesCollected[3];
    }

    /**
     * Indicate that the level is won by defeating all the enemies This version
     * is useful if the number of enemies isn't known, or if the goal is to
     * defeat enemies before more are are created.
     */
    static public void setVictoryEnemyCount() {
        Level._currLevel._score._victoryType = VictoryType.ENEMYCOUNT;
        Level._currLevel._score._victoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by defeating a certain number of enemies
     * 
     * @param howMany The number of enemies that must be defeated to win the
     *            level
     */
    static public void setVictoryEnemyCount(int howMany) {
        Level._currLevel._score._victoryType = VictoryType.ENEMYCOUNT;
        Level._currLevel._score._victoryEnemyCount = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param v1 Number of type-1 goodies that must be collected to win the
     *            level
     * @param v2 Number of type-2 goodies that must be collected to win the
     *            level
     * @param v3 Number of type-3 goodies that must be collected to win the
     *            level
     * @param v4 Number of type-4 goodies that must be collected to win the
     *            level
     */
    static public void setVictoryGoodies(int v1, int v2, int v3, int v4) {
        Level._currLevel._score._victoryType = VictoryType.GOODIECOUNT;
        Level._currLevel._score._victoryGoodieCount[0] = v1;
        Level._currLevel._score._victoryGoodieCount[1] = v2;
        Level._currLevel._score._victoryGoodieCount[2] = v3;
        Level._currLevel._score._victoryGoodieCount[3] = v4;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach
     * destinations
     * 
     * @param howMany Number of heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany) {
        Level._currLevel._score._victoryType = VictoryType.DESTINATION;
        Level._currLevel._score._victoryHeroCount = howMany;
    }
}
