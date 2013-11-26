package edu.lehigh.cse.ale;

public class Score
{
    /*
     * BASIC SUPPORT
     */

    /**
     * Track the number of _heroes that have been created
     */
    static int _heroesCreated;

    /**
     * Track the number of _heroes that have been removed/defeated
     */
    static int _heroesDefeated;

    /**
     * Count of the goodies (with score 1) that have been collected in this
     * level
     * 
     * TODO: switch to a vector of goodie types
     */
    static int _goodiesCollected1;

    /**
     * Count of the goodies (with score 2) that have been collected in this
     * level
     */
    static int _goodiesCollected2;

    /**
     * Count of the goodies (with score 3) that have been collected in this
     * level
     */
    static int _goodiesCollected3;

    /**
     * Count of the goodies (with score 4) that have been collected in this
     * level
     */
    static int _goodiesCollected4;

    /**
     * Number of _heroes who have arrived at any destination yet
     */
    static int _destinationArrivals;

    /**
     * Count the number of enemies that have been created
     */
    static int _enemiesCreated;

    /**
     * Count the enemies that have been defeated
     */
    static int _enemiesDefeated;

    /**
     * Reset score info when a new level is created
     */
    static void reset()
    {
        // reset the hero statistics
        _heroesCreated = 0;
        _heroesDefeated = 0;
        // reset goodie statistics
        _goodiesCollected1 = 0;
        _goodiesCollected2 = 0;
        _goodiesCollected3 = 0;
        _goodiesCollected4 = 0;

        // reset destination statistics
        _destinationArrivals = 0;

        // reset enemy statistics
        _enemiesCreated = 0;
        _enemiesDefeated = 0;
    }

    /*
     * METHODS CALLED WHEN AN ENTITY IS REMOVED
     */

    /**
     * Use this to inform the level that a hero has been defeated
     * 
     * @param e
     *            The enemy who defeated the hero
     */
    static void defeatHero(Enemy e)
    {
        // TODO
        /*
         * _heroesDefeated++;
         * if (_heroesDefeated == Score._heroesCreated)
         * MenuManager.loseLevel(e._onDefeatHeroText != "" ? e._onDefeatHeroText
         * : Level._textYouLost);
         */
    }

    /**
     * Use this to inform the level that a goodie has been collected by the hero
     * 
     * @param g
     *            The goodie that was collected
     * 
     * @deprecated Use onGoodieCollected[1-4]() instead
     */
    @Deprecated
    static void onGoodieCollected(Goodie g)
    {
        /*
         * // Update any/all goodie counts
         * _goodiesCollected1 += g._score1;
         * _goodiesCollected2 += g._score2;
         * _goodiesCollected3 += g._score3;
         * _goodiesCollected4 += g._score4;
         * 
         * // possibly win the level, but only if we win on goodie count and all
         * four counts are high enoug
         * if (Level._victoryType != Level.VictoryType.GOODIECOUNT)
         * return;
         * if ((Level._victoryGoodie1Count <= _goodiesCollected1)
         * && (Level._victoryGoodie2Count <= _goodiesCollected2)
         * && (Level._victoryGoodie3Count <= _goodiesCollected3)
         * && (Level._victoryGoodie4Count <= _goodiesCollected4))
         * {
         * MenuManager.winLevel();
         * }
         */
    }

    /**
     * Use this to inform the level that a hero has reached the destination
     * 
     * @param d
     *            The destination that the hero reached
     */
    static void onDestinationArrive()
    {
        // check if the level is complete
        _destinationArrivals++;
        if ((Level._victoryType == Level.VictoryType.DESTINATION) && (_destinationArrivals >= Level._victoryHeroCount))
            MenuManager.winLevel();
    }

    /**
     * Internal method for handling whenever an enemy is defeated
     */
    static void onDefeatEnemy()
    {
        /*
         * // update the count of defeated enemies
         * _enemiesDefeated++;
         * 
         * // if we win by defeating enemies, see if we've defeated enough of
         * them:
         * boolean win = false;
         * if (Level._victoryType == Level.VictoryType.ENEMYCOUNT) {
         * // -1 means "defeat all enemies"
         * if (Level._victoryEnemyCount == -1)
         * win = _enemiesDefeated == _enemiesCreated;
         * else
         * win = _enemiesDefeated >= Level._victoryEnemyCount;
         * }
         * if (win)
         * MenuManager.winLevel();
         */
    }

    /*
     * MANUAL SCORE MANIPULATION
     */

    /**
     * Manually set the number of goodies collected.
     * 
     * @param value
     *            The number to increment the number of goodies collected by.
     * 
     * @deprecated Use setGoodiesCollected[1-4]() instead
     */
    @Deprecated
    public static void setGoodiesCollected(int value)
    {
        _goodiesCollected1 = value;
    }

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     * 
     * @param value
     *            The number to increment the number of goodies collected by.
     */
    public static void setGoodiesCollected1(int value)
    {
        _goodiesCollected1 = value;
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     * 
     * @param value
     *            The number to increment the number of goodies collected by.
     */
    public static void setGoodiesCollected2(int value)
    {
        _goodiesCollected2 = value;
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     * 
     * @param value
     *            The number to increment the number of goodies collected by.
     */
    public static void setGoodiesCollected3(int value)
    {
        _goodiesCollected3 = value;
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     * 
     * @param value
     *            The number to increment the number of goodies collected by.
     */
    public static void setGoodiesCollected4(int value)
    {
        _goodiesCollected4 = value;
    }

    /**
     * Manually increment the number of goodies collected.
     * 
     * @deprecated Use incrementGoodiesCollected[1-4]() instead
     */
    @Deprecated
    public static void incrementGoodiesCollected()
    {
        _goodiesCollected1++;
    }

    /**
     * Manually increment the number of goodies of type 1 that have been
     * collected.
     */
    public static void incrementGoodiesCollected1()
    {
        _goodiesCollected1++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public static void incrementGoodiesCollected2()
    {
        _goodiesCollected2++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public static void incrementGoodiesCollected3()
    {
        _goodiesCollected3++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public static void incrementGoodiesCollected4()
    {
        _goodiesCollected4++;
    }

    /**
     * Getter for number of goodies collected.
     * 
     * @deprecated Use getGoodiesCollected[1-4]() instead.
     * 
     * @return The number of goodies collected.
     */
    @Deprecated
    public static int getGoodiesCollected()
    {
        return _goodiesCollected1;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected1()
    {
        return _goodiesCollected1;
    }

    /**
     * Getter for number of goodies of type 2 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected2()
    {
        return _goodiesCollected2;
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected3()
    {
        return _goodiesCollected3;
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     * 
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected4()
    {
        return _goodiesCollected4;
    }
}