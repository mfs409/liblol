/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.utils.Timer;

import edu.lehigh.cse.lol.internals.LolAction;

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
public class Score {
    /**
     * This is the number of goodies that must be collected, if we're in
     * GOODIECOUNT mode
     */
    private final int[] mVictoryGoodieCount = new int[4];
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
     */
    float mCountDownRemaining;
    /**
     * This is the same as CountDownRemaining, but for levels where the hero
     * wins by lasting until time runs out.
     */
    float mWinCountRemaining;
    /**
     * This is a stopwatch, for levels where we count how long the game has been
     * running
     */
    float mStopWatchProgress;
    /**
     * This is how far the hero has traveled
     */
    int mDistance;
    /**
     * Track the number of heroes that have been removed/defeated
     */
    private int mHeroesDefeated = 0;
    /**
     * Number of heroes who have arrived at any destination yet
     */
    private int mDestinationArrivals = 0;
    /**
     * Describes how a level is won.
     */
    private VictoryType mVictoryType = VictoryType.DESTINATION;
    /**
     * This is the number of heroes who must reach destinations, if we're in
     * DESTINATION mode
     */
    private int mVictoryHeroCount;
    /**
     * This is the number of enemies that must be defeated, if we're in
     * ENEMYCOUNT mode. -1 means "all of them"
     */
    private int mVictoryEnemyCount;

    /**
     * Manually increment the number of goodies of type 1 that have been
     * collected.
     */
    public static void incrementGoodiesCollected1() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public static void incrementGoodiesCollected2() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public static void incrementGoodiesCollected3() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public static void incrementGoodiesCollected4() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected1() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0];
    }

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected1(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0] = value;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Getter for number of goodies of type 2 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected2() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1];
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected2(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1] = value;
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected3() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2];
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected3(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2] = value;
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected4() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3];
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected4(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3] = value;
    }

    /**
     * Indicate that the level is won by defeating all the enemies. This version
     * is useful if the number of enemies isn't known, or if the goal is to
     * defeat all enemies before more are are created.
     */
    static public void setVictoryEnemyCount() {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by defeating a certain number of enemies
     *
     * @param howMany The number of enemies that must be defeated to win the level
     */
    static public void setVictoryEnemyCount(int howMany) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryEnemyCount = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     *
     * @param v1 Number of type-1 goodies that must be collected to win the
     *           level
     * @param v2 Number of type-2 goodies that must be collected to win the
     *           level
     * @param v3 Number of type-3 goodies that must be collected to win the
     *           level
     * @param v4 Number of type-4 goodies that must be collected to win the
     *           level
     */
    static public void setVictoryGoodies(int v1, int v2, int v3, int v4) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.GOODIECOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[0] = v1;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[1] = v2;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[2] = v3;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[3] = v4;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach
     * destinations
     *
     * @param howMany Number of heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.DESTINATION;
        Lol.sGame.mCurrentLevel.mScore.mVictoryHeroCount = howMany;
    }

    /**
     * Change the amount of time left in a countdown timer
     *
     * @param delta The amount of time to add before the timer expires
     */
    public static void updateTimerExpiration(float delta) {
        Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining += delta;
    }

    /**
     * Report the total distance the hero has traveled
     */
    public static int getDistance() {
        return Lol.sGame.mCurrentLevel.mScore.mDistance;
    }

    /**
     * Report the stopwatch value
     */
    public static int getStopwatch() {
        return (int) Lol.sGame.mCurrentLevel.mScore.mStopWatchProgress;
    }

    /**
     * Report the number of enemies that have been defeated
     */
    public static int getEnemiesDefeated() {
        return Lol.sGame.mCurrentLevel.mScore.mEnemiesDefeated;
    }

    /**
     * Force the level to end in victory
     *
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public static void winLevel() {
        Lol.sGame.mCurrentLevel.mScore.endLevel(true);
    }

    /**
     * Force the level to end in defeat
     *
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public static void loseLevel() {
        Lol.sGame.mCurrentLevel.mScore.endLevel(false);
    }

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
                LoseScene.get().setDefaultText(e.mOnDefeatHeroText);
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
        if (mVictoryType != VictoryType.GOODIECOUNT)
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
        if ((mVictoryType == VictoryType.DESTINATION) && (mDestinationArrivals >= mVictoryHeroCount))
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
        if (mVictoryType == VictoryType.ENEMYCOUNT) {
            // -1 means "defeat all enemies"
            if (mVictoryEnemyCount == -1)
                win = mEnemiesDefeated == mEnemiesCreated;
            else
                win = mEnemiesDefeated >= mVictoryEnemyCount;
        }
        if (win)
            endLevel(true);
    }

    /**
     * When a level ends, we run this code to shut it down, print a message, and
     * then let the user resume play
     *
     * @param win true if the level was won, false otherwise
     */
    void endLevel(final boolean win) {
        if (Lol.sGame.mCurrentLevel.mEndGameEvent == null)
            Lol.sGame.mCurrentLevel.mEndGameEvent = new LolAction() {
                @Override
                public void go() {
                    // Safeguard: only call this method once per level
                    if (mGameOver)
                        return;
                    mGameOver = true;

                    // Run the level-complete callback
                    if (win && Lol.sGame.mCurrentLevel.mWinCallback != null)
                        Lol.sGame.mCurrentLevel.mWinCallback.onEvent();
                    else if (!win && Lol.sGame.mCurrentLevel.mLoseCallback != null)
                        Lol.sGame.mCurrentLevel.mLoseCallback.onEvent();

                    // if we won, unlock the next level
                    if (win && Facts.getGameFact("unlocked", 1) <= Lol.sGame.mModeStates[Lol.PLAY])
                        Facts.putGameFact("unlocked", Lol.sGame.mModeStates[Lol.PLAY] + 1);

                    // drop everything from the hud
                    Lol.sGame.mCurrentLevel.mControls.clear();
                    Lol.sGame.mCurrentLevel.mDisplays.clear();

                    // clear any pending timers
                    Timer.instance().clear();

                    // display the PostScene, which provides a pause before we
                    // retry/start the next level
                    if (win)
                        Lol.sGame.mCurrentLevel.mWinScene.show();
                    else
                        Lol.sGame.mCurrentLevel.mLoseScene.show();
                }
            };
    }

    /**
     * These are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you can reach a certain
     * number of enemies defeated Technically, there's also 'survive for x
     * seconds', but that doesn't need special support
     */
    enum VictoryType {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    }
}
