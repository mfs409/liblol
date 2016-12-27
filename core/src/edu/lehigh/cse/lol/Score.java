package edu.lehigh.cse.lol;

import com.badlogic.gdx.utils.Timer;

/**
 * Score encapsulates the data used by a Level to track the player's progress.
 * There are four things tracked: the number of heroes created and destroyed,
 * the number of enemies created and destroyed, the number of heroes at
 * destinations, and the number of (each type of) goodie that has been
 * collected. Apart from storing the counts, this class provides a public
 * interface for manipulating the goodie counts, and a set of internal
 * convenience methods for updating values and checking for win/lose. It also
 * manages the mode of the level (i.e., what must be done to finish the level...
 * collecting goodies, reaching a destination, etc).
 */
class Score {
    /**
     * This is the number of goodies that must be collected, if we're in
     * GOODIECOUNT mode
     */
    final int[] mVictoryGoodieCount = new int[4];
    /**
     * Track the number of heroes that have been created
     */
    int mHeroesCreated = 0;
    /**
     * Count of the goodies that have been collected in this level
     */
    int[] mGoodiesCollected = new int[]{0, 0, 0, 0};
    /**
     * Count the number of enemies that have been created
     */
    int mEnemiesCreated = 0;
    /**
     * Count the enemies that have been defeated
     */
    int mEnemiesDefeated = 0;
    /**
     * Track if the level has been lost (true) or the game is still being played
     * (false)
     */
    boolean mGameOver;
    /**
     * In levels that have a lose-on-timer feature, we store the timer here, so
     * that we can extend the time left to complete a game
     * <p>
     * NB: -1 indicates the timer is not active
     */
    float mLoseCountDownRemaining = -100;

    /**
     * Text to display when a Lose Countdown completes
     */
    String mLoseCountDownText = "";
    /**
     * This is the same as CountDownRemaining, but for levels where the hero
     * wins by lasting until time runs out.
     */
    float mWinCountRemaining = -100;
    /**
     * Text to ddisplay when a Win Countdown completes
     */
    String mWinCountText = "";
    /**
     * This is a stopwatch, for levels where we count how long the game has been
     * running
     */
    float mStopWatchProgress = -100;
    /**
     * This is how far the hero has traveled
     */
    int mDistance;
    /**
     * Track the number of heroes that have been removed/defeated
     */
    int mHeroesDefeated = 0;
    /**
     * Number of heroes who have arrived at any destination yet
     */
    int mDestinationArrivals = 0;
    /**
     * Describes how a level is won.
     */
    Level.VictoryType mVictoryType = Level.VictoryType.DESTINATION;
    /**
     * This is the number of heroes who must reach destinations, if we're in
     * DESTINATION mode
     */
    int mVictoryHeroCount;
    /**
     * This is the number of enemies that must be defeated, if we're in
     * ENEMYCOUNT mode. -1 means "all of them"
     */
    int mVictoryEnemyCount;

    /**
     * Use this to inform the level that a hero has been defeated
     *
     * @param e The enemy who defeated the hero
     */
    void defeatHero(Enemy e) {
        mHeroesDefeated++;
        if (mHeroesDefeated == mHeroesCreated) {
            // possibly change the end-of-level text
            if (!e.mOnDefeatHeroText.equals(""))
                mLevel.getLoseScene().setDefaultText(e.mOnDefeatHeroText);
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
            mGoodiesCollected[i] += g.mScore[i];

        // possibly win the level, but only if we win on goodie count and all
        // four counts are high enough
        if (mVictoryType != Level.VictoryType.GOODIECOUNT)
            return;
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= mVictoryGoodieCount[i] <= mGoodiesCollected[i];
        if (match)
            endLevel(true);
    }

    /**
     * Use this to inform the level that a hero has reached a destination
     */
    void onDestinationArrive() {
        // check if the level is complete
        mDestinationArrivals++;
        if ((mVictoryType == Level.VictoryType.DESTINATION) && (mDestinationArrivals >= mVictoryHeroCount))
            endLevel(true);
    }

    /**
     * Internal method for handling whenever an enemy is defeated
     */
    void onDefeatEnemy() {
        // update the count of defeated enemies
        mEnemiesDefeated++;

        // if we win by defeating enemies, see if we've defeated enough of them:
        boolean win = false;
        if (mVictoryType == Level.VictoryType.ENEMYCOUNT) {
            // -1 means "defeat all enemies"
            if (mVictoryEnemyCount == -1)
                win = mEnemiesDefeated == mEnemiesCreated;
            else
                win = mEnemiesDefeated >= mVictoryEnemyCount;
        }
        if (win)
            endLevel(true);
    }

    Level mLevel;

    Score(Level level) {
        mLevel = level;
    }

    /// When the level is won or lost, this is where we store the event that needs to run
    LolAction mEndGameEvent;

    /// Code to run when a level is won
    LolCallback mWinCallback;

    /// Code to run when a level is lost
    LolCallback mLoseCallback;

    /**
     * When a level ends, we run this code to shut it down, print a message, and
     * then let the user resume play
     *
     * @param win true if the level was won, false otherwise
     */
    void endLevel(final boolean win) {
        if (mEndGameEvent == null)
            mEndGameEvent = new LolAction() {
                @Override
                public void go() {
                    // Safeguard: only call this method once per level
                    if (mGameOver)
                        return;
                    mGameOver = true;

                    // Run the level-complete callback
                    if (win && mWinCallback != null)
                        mWinCallback.onEvent();
                    else if (!win && mLoseCallback != null)
                        mLoseCallback.onEvent();

                    // if we won, unlock the next level
                    if (win)
                        mLevel.mGame.unlockNext();

                    // drop everything from the hud
                    mLevel.mHud.reset();

                    // clear any pending timers
                    Timer.instance().clear();

                    // display the PostScene, which provides a pause before we
                    // retry/start the next level
                    if (win)
                        mLevel.mWinScene.show();
                    else
                        mLevel.mLoseScene.show();
                }
            };
    }

}

