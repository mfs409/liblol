package edu.lehigh.cse.lol;

/**
 * Score is used by Level to track the progress through a level. There are four things tracked: the number of heroes
 * created and destroyed, the number of enemies created and destroyed, the number of heroes at destinations, and the
 * number of (each type of) goodie that has been collected.
 * 
 * Apart from storing the counts, this class provides a public interface for manipulating the goodie counts, and a set
 * of internal convenience methods for updating values and checking for win/lose.
 */
public class Score
{
    /*
     * COUNTERS
     */
    
    /**
     * Track the number of heroes that have been created
     */
    int   _heroesCreated       = 0;

    /**
     * Track the number of heroes that have been removed/defeated
     */
    int   _heroesDefeated      = 0;

    /**
     * Count of the goodies that have been collected in this level
     */
    int[] _goodiesCollected    = new int[] { 0, 0, 0, 0 };

    /**
     * Number of heroes who have arrived at any destination yet
     */
    int   _destinationArrivals = 0;

    /**
     * Count the number of enemies that have been created
     */
    int   _enemiesCreated      = 0;

    /**
     * Count the enemies that have been defeated
     */
    int   _enemiesDefeated     = 0;

    /*
     * INTERNAL INTERFACE
     */

    /**
     * Use this to inform the level that a hero has been defeated
     * 
     * @param e
     *            The enemy who defeated the hero
     */
    void defeatHero(Enemy e)
    {
        _heroesDefeated++;
        if (_heroesDefeated == _heroesCreated) {
            if (e._onDefeatHeroText != "")
                PostScene.setDefaultLoseText(e._onDefeatHeroText);
            Level.loseLevel();
        }
    }

    /**
     * Use this to inform the level that a goodie has been collected by a hero
     * 
     * @param g
     *            The goodie that was collected
     */
    void onGoodieCollected(Goodie g)
    {
        // Update goodie counts
        for (int i = 0; i < 4; ++i)
            _goodiesCollected[i] += g._score[i];

        // possibly win the level, but only if we win on goodie count and all
        // four counts are high enough
        if (Level._victoryType != Level.VictoryType.GOODIECOUNT)
            return;
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= Level._victoryGoodieCount[i] <= _goodiesCollected[i];
        if (match)
            Level.winLevel();
    }

    /**
     * Use this to inform the level that a hero has reached a destination
     * 
     * @param d
     *            The destination that the hero reached
     */
    void onDestinationArrive()
    {
        // check if the level is complete
        _destinationArrivals++;
        if ((Level._victoryType == Level.VictoryType.DESTINATION) && (_destinationArrivals >= Level._victoryHeroCount))
            Level.winLevel();
    }

    /**
     * Internal method for handling whenever an enemy is defeated
     */
    void onDefeatEnemy()
    {
        // update the count of defeated enemies
        _enemiesDefeated++;

        // if we win by defeating enemies, see if we've defeated enough of them:
        boolean win = false;
        if (Level._victoryType == Level.VictoryType.ENEMYCOUNT) {
            // -1 means "defeat all enemies"
            if (Level._victoryEnemyCount == -1)
                win = _enemiesDefeated == _enemiesCreated;
            else
                win = _enemiesDefeated >= Level._victoryEnemyCount;
        }
        if (win)
            Level.winLevel();
    }

    /*
     * PUBLIC INTERFACE FOR MANAGING GOODIE COUNTS
     */

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     * 
     * @param value
     *            The new value
     */
    public static void setGoodiesCollected1(int value)
    {
        Level._currLevel._score._goodiesCollected[0] = value;
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     * 
     * @param value
     *            The new value
     */
    public static void setGoodiesCollected2(int value)
    {
        Level._currLevel._score._goodiesCollected[1] = value;
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     * 
     * @param value
     *            The new value
     */
    public static void setGoodiesCollected3(int value)
    {
        Level._currLevel._score._goodiesCollected[2] = value;
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     * 
     * @param value
     *            The new value
     */
    public static void setGoodiesCollected4(int value)
    {
        Level._currLevel._score._goodiesCollected[3] = value;
    }

    /**
     * Manually increment the number of goodies of type 1 that have been
     * collected.
     */
    public static void incrementGoodiesCollected1()
    {
        Level._currLevel._score._goodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public static void incrementGoodiesCollected2()
    {
        Level._currLevel._score._goodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public static void incrementGoodiesCollected3()
    {
        Level._currLevel._score._goodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public static void incrementGoodiesCollected4()
    {
        Level._currLevel._score._goodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected1()
    {
        return Level._currLevel._score._goodiesCollected[0];
    }

    /**
     * Getter for number of goodies of type 2 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected2()
    {
        return Level._currLevel._score._goodiesCollected[1];
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected3()
    {
        return Level._currLevel._score._goodiesCollected[2];
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected4()
    {
        return Level._currLevel._score._goodiesCollected[3];
    }
}