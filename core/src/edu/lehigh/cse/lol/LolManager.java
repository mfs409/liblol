package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Timer;

import java.util.TreeMap;

/**
 * LolManager encapsulates the states and transitions of the game.  To do so, we must track several
 * things:
 * - The state of the active level (Chooser, Help, Splash, Store, Play)
 * - The 'which' of active and inactive levels (e.g., are we on Store screen #4)
 * - The score of that level (if it is a Play level)
 * - The currently configured scenes for the active level
 * <p>
 * The Manager object handles scores, screen management, and transitions among screens
 * <p>
 */
class LolManager {
    /// A reference to the top-level game object
    private final Lol mGame;
    /// A reference to the game configuration object
    private final Config mConfig;
    /// The set of loaded assets
    private final Media mMedia;
    /// TODO
    private final Level mLevel;

    /// The physics world in which all actors exist
    MainScene mWorld;
    /// A heads-up display, for writing LolText and Control objects
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

    /// Store string/integer pairs that get reset whenever we restart the program
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


    LolManager(Config config, Media media, Lol game) {
        mGame = game;
        mConfig = config;
        mMedia = media;
        // Set up the API, so that any user code we call is able to reach this object
        mLevel = new Level(mConfig, mMedia, mGame);

        createScenes();
        mSessionFacts = new TreeMap<>();

        // set current mode states
        for (int i = 0; i < 5; ++i)
            mModeStates[i] = 1;

        resetScores();
    }

    void resetScores() {
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

    void createScenes() {
        // Create the eight different scenes and a score object
        mWorld = new MainScene(mConfig, mMedia, mGame);
        mWinScene = QuickScene.makeWinScene(mWorld, mMedia, mConfig);
        mLoseScene = QuickScene.makeLoseScene(mWorld, mMedia, mConfig);
        mPreScene = QuickScene.makePreScene(mWorld, mMedia, mConfig);
        mPauseScene = QuickScene.makePauseScene(mWorld, mMedia, mConfig);
        mHud = new HudScene(mMedia, mConfig);
        mBackground = new ParallaxScene(mConfig);
        mForeground = new ParallaxScene(mConfig);
    }

    /**
     * Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
     * screen, if any.
     */
    void setScreen() {
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
    void unlockNext() {
        if (mGame.getGameFact(mConfig, "unlocked", 1) <= mModeStates[PLAY])
            mGame.putGameFact(mConfig, "unlocked", mModeStates[PLAY] + 1);
    }

    void advanceLevel() {
        if (mModeStates[PLAY] == mConfig.mNumLevels) {
            doChooser(1);
        } else {
            mModeStates[PLAY]++;
            doPlay(mModeStates[PLAY]);
        }
    }

    void repeatLevel() {
        doPlay(mModeStates[PLAY]);
    }

    /**
     * Use this to load the splash screen
     */
    void doSplash() {
        // reset state of all screens
        for (int i = 0; i < 5; ++i)
            mModeStates[i] = 1;
        mMode = SPLASH;
        setScreen();
        mConfig.mSplash.display(1, mLevel);
    }

    /**
     * Use this to load the level-chooser screen. Note that when the chooser is
     * disabled, we jump straight to level 1.
     *
     * @param whichChooser The chooser screen to create
     */
    void doChooser(int whichChooser) {
        // if chooser disabled, then we either called this from splash, or from
        // a game level
        if (!mConfig.mEnableChooser) {
            if (mMode == PLAY) {
                doSplash();
            } else {
                doPlay(mModeStates[PLAY]);
            }
            return;
        }
        // the chooser is not disabled... save the choice of level, configureGravity
        // it, and show it.
        mMode = CHOOSER;
        mModeStates[CHOOSER] = whichChooser;
        setScreen();
        mConfig.mChooser.display(whichChooser, mLevel);
    }

    /**
     * Use this to load a playable level.
     *
     * @param which The index of the level to load
     */
    void doPlay(int which) {
        mModeStates[PLAY] = which;
        mMode = PLAY;
        setScreen();
        resetScores();
        mConfig.mLevels.display(which, mLevel);
    }

    /**
     * Use this to load a help level.
     *
     * @param which The index of the help level to load
     */
    void doHelp(int which) {
        mModeStates[HELP] = which;
        mMode = HELP;
        setScreen();
        mConfig.mHelp.display(which, mLevel);
    }

    /**
     * Use this to load a screen of the store.
     *
     * @param which The index of the help level to load
     */
    void doStore(int which) {
        mModeStates[STORE] = which;
        mMode = STORE;
        setScreen();
        mConfig.mStore.display(which, mLevel);
    }

    /**
     * Use this to quit the game
     */
    void doQuit() {
        mWorld.stopMusic();
        Gdx.app.exit();
    }

    /// This is the number of goodies that must be collected, if we're in GOODIECOUNT mode
    int[] mVictoryGoodieCount;

    /// Track the number of heroes that have been created
    int mHeroesCreated;

    /**
     * Count of the goodies that have been collected in this level
     */
    int[] mGoodiesCollected;
    /**
     * Count the number of enemies that have been created
     */
    int mEnemiesCreated;
    /**
     * Count the enemies that have been defeated
     */
    int mEnemiesDefeated;
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
    float mLoseCountDownRemaining;

    /**
     * Text to display when a Lose Countdown completes
     */
    String mLoseCountDownText;
    /**
     * This is the same as CountDownRemaining, but for levels where the hero
     * wins by lasting until time runs out.
     */
    float mWinCountRemaining;
    /**
     * Text to ddisplay when a Win Countdown completes
     */
    String mWinCountText;
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
    int mHeroesDefeated;
    /**
     * Number of heroes who have arrived at any destination yet
     */
    int mDestinationArrivals;
    /**
     * Describes how a level is won.
     */
    VictoryType mVictoryType;
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

    /// When the level is won or lost, this is where we store the event that needs to run
    LolAction mEndGameEvent;

    /// Code to run when a level is won
    LolCallback mWinCallback;

    /// Code to run when a level is lost
    LolCallback mLoseCallback;

    /**
     * Use this to inform the level that a hero has been defeated
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
     * Use this to inform the level that a goodie has been collected by a hero
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
                        mGame.mManager.unlockNext();

                    // drop everything from the hud
                    mGame.mManager.mHud.reset();

                    // clear any pending timers
                    Timer.instance().clear();

                    // display the PostScene, which provides a pause before we
                    // retry/start the next level
                    if (win)
                        mGame.mManager.mWinScene.show();
                    else
                        mGame.mManager.mLoseScene.show();
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

    void onRender() {
        // Check the countdown timers
        if (mLoseCountDownRemaining != -100) {
            mLoseCountDownRemaining -= Gdx.graphics.getDeltaTime();
            if (mLoseCountDownRemaining < 0) {
                if (mLoseCountDownText != "")
                    mLoseScene.setDefaultText(mLoseCountDownText);
                endLevel(false);
            }
        }
        if (mWinCountRemaining != -100) {
            mWinCountRemaining -= Gdx.graphics.getDeltaTime();
            if (mWinCountRemaining < 0) {
                if (mWinCountText != "")
                    mWinScene.setDefaultText(mWinCountText);
                endLevel(true);
            }
        }
        if (mStopWatchProgress != -100) {
            mStopWatchProgress += Gdx.graphics.getDeltaTime();
        }
    }

    /**
     * When the back key is pressed, or when we are simulating the back key
     * being pressed (e.g., a back button), this code runs.
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
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (mMode == CHOOSER || mMode == HELP || mMode == STORE) {
            doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            doChooser(mModeStates[CHOOSER]);
        }
    }
}

