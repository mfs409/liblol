/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;

import java.util.TreeMap;

/**
 * LolManager encapsulates the states and transitions of the game.  To do so, we must track several
 * things:
 * <ul>
 * <li>The state of the active level (Chooser, Help, Splash, Store, Play)</li>
 * <li>The 'which' of active and inactive levels (e.g., are we on Store screen #4)</li>
 * <li> The score of that level (if it is a Play level)</li>
 * <li>The currently configured scenes for the active level</li>
 * </ul>
 */
class LolManager {
    /// A reference to the top-level game object
    private final Lol mGame;
    /// A reference to the game configuration object
    private final Config mConfig;
    /// The set of loaded assets
    private final Media mMedia;
    /// The object that comprises the public API
    private final Level mLevel;

    /// The physics world in which all actors exist
    MainScene mWorld;
    /// A heads-up display
    HudScene mHud;
    /// The scene to show when the level is created (if any)
    QuickScene mPreScene;
    /// The scene to show when the level is won
    QuickScene mWinScene;
    /// The scene to show when the level is lost
    QuickScene mLoseScene;
    /// The scene to show when the level is paused (if any)
    QuickScene mPauseScene;
    /// The background layers
    ParallaxScene mBackground;
    /// The foreground layers
    ParallaxScene mForeground;

    /// Store string/integer pairs that get reset whenever we restart the program, but which persist
    /// across levels
    final TreeMap<String, Integer> mSessionFacts;

    /// Modes of the game, for use by the state machine.  We can be showing the main splash
    /// screen, the help screens, the level chooser, the store, or a playable level
    final private int SPLASH = 0;
    final private int HELP = 1;
    final private int CHOOSER = 2;
    final private int STORE = 3;
    final private int PLAY = 4;
    /// The current state (e.g., are we showing a STORE)
    private int mMode;
    /// The level within each mode (e.g., we are in PLAY scene 4, and will return to CHOOSER 2)
    private int mModeStates[] = new int[5];

    /// This is the number of goodies that must be collected, if we're in GOODIECOUNT mode
    int[] mVictoryGoodieCount;
    /// Track the number of heroes that have been created
    int mHeroesCreated;
    /// Count of the goodies that have been collected in this level
    int[] mGoodiesCollected;
    /// Count the number of enemies that have been created
    int mEnemiesCreated;
    /// Count the enemies that have been defeated
    int mEnemiesDefeated;
    /// Track if the level has been lost (true) or the game is still being played (false)
    boolean mGameOver;
    /// In levels that have a lose-on-timer feature, we store the timer here, so that we can extend
    /// the time left to complete a game
    ///
    /// NB: -1 indicates the timer is not active
    float mLoseCountDownRemaining;
    /// Text to display when a Lose Countdown completes
    String mLoseCountDownText;
    /// Time that must pass before the level ends in victory
    float mWinCountRemaining;
    ///  Text to display when a Win Countdown completes
    String mWinCountText;
    /// This is a stopwatch, for levels where we count how long the game has been running
    float mStopWatchProgress;
    /// This is how far the hero has traveled
    int mDistance;
    /// Track the number of heroes that have been removed/defeated
    private int mHeroesDefeated;
    /// Number of heroes who have arrived at any destination yet
    private int mDestinationArrivals;
    /// Describes how a level is won.
    VictoryType mVictoryType;
    /// This is the number of heroes who must reach destinations, if we're in DESTINATION mode
    int mVictoryHeroCount;
    /// The number of enemies that must be defeated, if we're in ENEMYCOUNT mode. -1 means "all"
    int mVictoryEnemyCount;
    /// When the level is won or lost, this is where we store the event that needs to run
    LolAction mEndGameEvent;
    /// Code to run when a level is won
    LolAction mWinCallback;
    /// Code to run when a level is lost
    LolAction mLoseCallback;

    /**
     * Construct the LolManager, build the scenes, set up the state machine, and clear the scores.
     *
     * @param config The game-wide configuration
     * @param media  All image and sound assets for the game
     * @param game   A reference to the top-level game object
     */
    LolManager(Config config, Media media, Lol game) {
        mGame = game;
        mConfig = config;
        mMedia = media;
        // Set up the API, so that any user code we call is able to reach this object
        mLevel = new Level(mConfig, mMedia, mGame);
        // build scenes and facts
        createScenes();
        mSessionFacts = new TreeMap<>();
        // set current mode states, and reset the scores
        for (int i = 0; i < 5; ++i)
            mModeStates[i] = 1;
        resetScores();
    }

    /**
     * Reset all scores.  This should be called at the beginning of every level.
     */
    private void resetScores() {
        mVictoryGoodieCount = new int[4];
        mHeroesCreated = 0;
        mGoodiesCollected = new int[]{0, 0, 0, 0};
        mEnemiesCreated = 0;
        mEnemiesDefeated = 0;
        mGameOver = false;
        mLoseCountDownRemaining = -100;
        mLoseCountDownText = "";
        mWinCountRemaining = -100;
        mWinCountText = "";
        mStopWatchProgress = -100;
        mDistance = 0;
        mHeroesDefeated = 0;
        mDestinationArrivals = 0;
        mVictoryType = VictoryType.DESTINATION;
        mVictoryHeroCount = 0;
        mVictoryEnemyCount = 0;
        mEndGameEvent = null;
        mWinCallback = null;
        mLoseCallback = null;
    }

    /**
     * Create all scenes for a playable level.
     */
    private void createScenes() {
        // Create the easy scenes
        mWorld = new MainScene(mConfig, mMedia);
        mHud = new HudScene(mMedia, mConfig);
        mBackground = new ParallaxScene(mConfig);
        mForeground = new ParallaxScene(mConfig);
        // the win/lose/pre/pause scenes are a little bit complicated
        mWinScene = new QuickScene(mMedia, mConfig, mConfig.mDefaultWinText);
        mWinScene.setDismissAction(new LolAction() {
            @Override
            public void go() {
                advanceLevel();
            }
        });
        mLoseScene = new QuickScene(mMedia, mConfig, mConfig.mDefaultLoseText);
        mLoseScene.setDismissAction(new LolAction() {
            @Override
            public void go() {
                repeatLevel();
            }
        });
        mPreScene = new QuickScene(mMedia, mConfig, "");
        mPreScene.setShowAction(null);
        mPauseScene = new QuickScene(mMedia, mConfig, "");
        mPauseScene.setAsPauseScene();
    }

    /**
     * Before we call programmer code to load a new scene, we call this to ensure that everything is
     * in a clean state.
     */
    private void onScreenChange() {
        mWorld.pauseMusic();
        createScenes();
        // When debug mode is on, print the frames per second
        if (mConfig.mShowDebugBoxes)
            mLevel.addDisplay(800, 15, mConfig.mDefaultFontFace, mConfig.mDefaultFontColor, 12, "fps: ", "", mLevel.DisplayFPS, 2);
    }

    /**
     * If the level that follows this level has not yet been unlocked, unlock it.
     * <p>
     * NB: we only track one value for locking/unlocking, so this actually unlocks all levels up to
     * and including the level after the current level.
     */
    private void unlockNext() {
        if (Lol.getGameFact(mConfig, "unlocked", 1) <= mModeStates[PLAY])
            Lol.putGameFact(mConfig, "unlocked", mModeStates[PLAY] + 1);
    }

    /**
     * Move forward to the next level, if there is one, and otherwise go back to the chooser.
     */
    void advanceLevel() {
        // Make sure to stop the music!
        mWorld.stopMusic();
        if (mModeStates[PLAY] == mConfig.mNumLevels) {
            doChooser(1);
        } else {
            mModeStates[PLAY]++;
            doPlay(mModeStates[PLAY]);
        }
    }

    /**
     * Start a level over again.
     */
    void repeatLevel() {
        doPlay(mModeStates[PLAY]);
    }

    /**
     * Load the splash screen
     */
    void doSplash() {
        for (int i = 0; i < 5; ++i)
            mModeStates[i] = 1;
        mMode = SPLASH;
        onScreenChange();
        mConfig.mSplash.display(1, mLevel);
    }

    /**
     * Load the level-chooser screen. If the chooser is disabled, jump straight to level 1.
     *
     * @param index The chooser screen to create
     */
    void doChooser(int index) {
        // if chooser disabled, then we either called this from splash, or from a game level
        if (!mConfig.mEnableChooser) {
            if (mMode == PLAY) {
                doSplash();
            } else {
                doPlay(mModeStates[PLAY]);
            }
            return;
        }
        // the chooser is not disabled... save the choice of level, configure it, and show it.
        mMode = CHOOSER;
        mModeStates[CHOOSER] = index;
        onScreenChange();
        mConfig.mChooser.display(index, mLevel);
    }

    /**
     * Load a playable level
     *
     * @param index The index of the level to load
     */
    void doPlay(int index) {
        mModeStates[PLAY] = index;
        mMode = PLAY;
        onScreenChange();
        resetScores();
        mConfig.mLevels.display(index, mLevel);
    }

    /**
     * Load a help level
     *
     * @param index The index of the help level to load
     */
    void doHelp(int index) {
        mModeStates[HELP] = index;
        mMode = HELP;
        onScreenChange();
        mConfig.mHelp.display(index, mLevel);
    }

    /**
     * Load a screen of the store.
     *
     * @param index The index of the help level to load
     */
    void doStore(int index) {
        mModeStates[STORE] = index;
        mMode = STORE;
        onScreenChange();
        mConfig.mStore.display(index, mLevel);
    }

    /**
     * Quit the game
     */
    void doQuit() {
        mWorld.stopMusic();
        Gdx.app.exit();
    }


    /**
     * Indicate that a hero has been defeated
     *
     * @param enemy The enemy who defeated the hero
     */
    void defeatHero(Enemy enemy) {
        mHeroesDefeated++;
        if (mHeroesDefeated == mHeroesCreated) {
            // possibly change the end-of-level text
            if (!enemy.mOnDefeatHeroText.equals(""))
                mLoseScene.setDefaultText(enemy.mOnDefeatHeroText);
            endLevel(false);
        }
    }

    /**
     * Indicate that a goodie has been collected
     *
     * @param goodie The goodie that was collected
     */
    void onGoodieCollected(Goodie goodie) {
        // Update goodie counts
        for (int i = 0; i < 4; ++i)
            mGoodiesCollected[i] += goodie.mScore[i];
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
     * Indicate that a hero has reached a destination
     */
    void onDestinationArrive() {
        // check if the level is complete
        mDestinationArrivals++;
        if ((mVictoryType == VictoryType.DESTINATION) && (mDestinationArrivals >= mVictoryHeroCount))
            endLevel(true);
    }

    /**
     * Indicate that an enemy has been defeated
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
                        mWinCallback.go();
                    else if (!win && mLoseCallback != null)
                        mLoseCallback.go();

                    // if we won, unlock the next level
                    if (win)
                        mGame.mManager.unlockNext();

                    // drop everything from the hud
                    mGame.mManager.mHud.reset();

                    // clear any pending timers
                    Timer.instance().clear();

                    // display the PostScene before we retry/start the next level
                    if (win)
                        mGame.mManager.mWinScene.show();
                    else
                        mGame.mManager.mLoseScene.show();
                }
            };
    }

    /**
     * These are the ways you can complete a level: you can reach the destination, you can collect
     * enough stuff, or you can reach a certain number of enemies defeated.
     * <p>
     * Technically, there's also 'survive for x seconds', but that doesn't need special support
     */
    enum VictoryType {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    }

    /**
     * Update all timer counters associated with the current level
     */
    void updateTimeCounts() {
        // Check the countdown timers
        if (mLoseCountDownRemaining != -100) {
            mLoseCountDownRemaining -= Gdx.graphics.getDeltaTime();
            if (mLoseCountDownRemaining < 0) {
                if (!mLoseCountDownText.equals(""))
                    mLoseScene.setDefaultText(mLoseCountDownText);
                endLevel(false);
            }
        }
        if (mWinCountRemaining != -100) {
            mWinCountRemaining -= Gdx.graphics.getDeltaTime();
            if (mWinCountRemaining < 0) {
                if (!mWinCountText.equals(""))
                    mWinScene.setDefaultText(mWinCountText);
                endLevel(true);
            }
        }
        if (mStopWatchProgress != -100) {
            mStopWatchProgress += Gdx.graphics.getDeltaTime();
        }
    }

    /**
     * Code to run when the back key is pressed, or when we are simulating a back key pressed
     */
    void handleBack() {
        // clear all timers, just in case...
        Timer.instance().clear();
        // if we're looking at main menu, then exit
        if (mMode == SPLASH) {
            // TODO: return a bool, let game dispose of itself?
            mGame.dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash screen
        else if (mMode == CHOOSER || mMode == HELP || mMode == STORE) {
            doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            doChooser(mModeStates[CHOOSER]);
        }
    }
}

