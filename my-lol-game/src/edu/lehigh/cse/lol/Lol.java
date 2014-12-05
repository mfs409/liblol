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

// TODO: verify chooser and level music stops on Android events

// TODO: verify that flipped animations work correctly, even when they change while flipped

// TODO: Hero animation sequences could use work... we can lose information (e.g., if
// invincibility runs out while jumping), and we don't have invincible+X or jump+crawl
// animation

// TODO: Make sure we have good error messages for common mistakes (filenames, animation, routes)

// TODO: consider making sprite sheets more useful (i.e., cut out arbitrary regions)

// TODO: make panning return to the chasesprite more nicely

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

/**
 * A Lol object is the outermost container for all of the functionality of the
 * game. It implements ApplicationListener (through Game), which provides hooks
 * for GDX to render the game, stop it, resume it, etc.
 * 
 * Lol is not responsible for doing anything significant. It keeps track of
 * which screen is currently in use, and forwards (through Game) to that screen.
 * Splash screens, Choosers, Help, and playable Levels each implement Screen, so
 * that they can do the real work.
 */
public abstract class Lol extends Game {
    /**
     * This interface is used to store items that can be rendered
     */
    interface Renderable {
        /**
         * Render something to the screen
         * 
         * @param sb
         *            The SpriteBatch to use for rendering
         * @param elapsed
         *            The time since the last render
         */
        void render(SpriteBatch sb, float elapsed);
    }

    /**
     * The current mode of the program
     */
    private Modes mMode;

    /**
     * The current level being played
     */
    int mCurrLevelNum;

    /**
     * Track the current help scene being displayed
     */
    int mCurrHelpNum;

    /**
     * A reference to the game object
     */
    static Lol sGame;

    /**
     * This variable lets us track whether the user pressed 'back' on an
     * android, or 'escape' on the desktop. We are using polling, so we swallow
     * presses that aren't preceded by a release. In that manner, holding 'back'
     * can't exit all the way out... you must press 'back' repeatedly, once for
     * each screen to revert.
     */
    boolean mKeyDown;

    /**
     * The configuration of the game is accessible through this
     */
    LolConfiguration mConfig;

    /**
     * The configuratoin of the chooser screen is accessible through this
     */
    ChooserConfiguration mChooserConfig;

    /**
     * Modes of the game: we can be showing the main screen, the help screens,
     * the level chooser, or a playable level
     */
    private enum Modes {
        SPLASH, HELP, CHOOSE, PLAY
    };

    /**
     * Use this to load the splash screen
     */
    void doSplash() {
        // set the default display mode
        mCurrLevelNum = 0;
        mCurrHelpNum = 0;
        mMode = Modes.SPLASH;
        setScreen(new Splash());
    }

    /**
     * Use this to load the level-chooser screen. Note that when the chooser is
     * disabled, we jump straight to level 1.
     */
    void doChooser() {
        // if chooser disabled, then we either called this from splash, or from
        // a game level
        if (!mChooserConfig.showChooser()) {
            if (mMode == Modes.PLAY) {
                doSplash();
            } else {
                doPlayLevel(mCurrLevelNum == 0 ? 1 : mCurrLevelNum);
            }
            return;
        }
        mCurrHelpNum = 0;
        mMode = Modes.CHOOSE;
        setScreen(new Chooser());
    }

    /**
     * Use this to load a playable level.
     * 
     * @param which
     *            The index of the level to load
     */
    void doPlayLevel(int which) {
        mCurrLevelNum = which;
        mCurrHelpNum = 0;
        mMode = Modes.PLAY;
        configureLevel(which);
        setScreen(Level.sCurrent);
    }

    /**
     * Use this to load a help level.
     * 
     * @param which
     *            The index of the help level to load
     */
    void doHelpLevel(int which) {
        mCurrHelpNum = which;
        mCurrLevelNum = 0;
        mMode = Modes.HELP;
        configureHelpScene(which);
        setScreen(HelpLevel.sCurrentLevel);
    }

    /**
     * Use this to quit the app
     */
    void doQuit() {
        getScreen().dispose();
        Gdx.app.exit();
    }

    /**
     * Vibrate the phone for a fixed amount of time. Note that this only
     * vibrates the phone if the configuration says that vibration should be
     * permitted.
     * 
     * @param millis
     *            The amount of time to vibrate
     */
    void vibrate(int millis) {
        if (mConfig.getVibration())
            Gdx.input.vibrate(millis);
    }

    /**
     * We can use this method from the render loop to poll for back presses
     */
    private void handleKeyDown() {
        // if neither BACK nor ESCAPE is being pressed, do nothing, but
        // recognize future presses
        if (!Gdx.input.isKeyPressed(Keys.BACK) && !Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            mKeyDown = false;
            return;
        }
        // if they key is being held down, ignore it
        if (mKeyDown)
            return;
        // recognize a new back press as being a 'down' press
        mKeyDown = true;
        handleBack();
    }

    /**
     * When the back key is pressed, or when we are simulating the back key
     * being pressed (e.g., a back button), this code runs.
     */
    void handleBack() {
        // clear all timers, just in case...
        Timer.instance().clear();
        // if we're looking at main menu, then exit
        if (mMode == Modes.SPLASH) {
            dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (mMode == Modes.CHOOSE || mMode == Modes.HELP) {
            doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            doChooser();
        }
    }

    /*
     * APPLICATIONLISTENER (GAME) OVERRIDES
     */

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        sGame = this;

        // reset session facts
        Facts.resetSessionFacts();

        // get configuration
        mConfig = lolConfig();
        mChooserConfig = chooserConfig();

        // for handling back presses
        Gdx.input.setCatchBackKey(true);

        // Load Resources
        nameResources();

        // show the splash screen
        doSplash();

        // configure the volume
        if (Facts.getGameFact("volume") == -1)
            Facts.putGameFact("volume", 1);
    }

    /**
     * This is an internal method for quitting a game. User code should never
     * call this.
     */
    @Override
    public void dispose() {
        super.dispose();

        // dispose of all fonts, textureregions, etc...
        //
        // It appears that GDX manages all textures for images and fonts, as
        // well as all sounds and music files. That
        // being the case, the only thing we need to be careful about is that we
        // get rid of any references to fonts that
        // might be hanging around
        Media.onDispose();
    }

    /**
     * This is an internal method for drawing game levels. User code should
     * never call this.
     */
    @Override
    public void render() {
        // Check for back press
        handleKeyDown();
        // Draw the current scene
        super.render();
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Set the next level to play
     */
    public void setNextLevel(int nextLevel) {
        mCurrLevelNum = nextLevel;
    }

    /**
     * The programmer configures the game by implementing this method, and
     * returning a LolConfiguration object
     */
    abstract public LolConfiguration lolConfig();

    /**
     * The programmer configures the chooser screen by implementing this method,
     * and returning a ChooserConfiguration object
     */
    abstract public ChooserConfiguration chooserConfig();

    /**
     * Register any sound or image files to be used by the game
     */
    abstract public void nameResources();

    /**
     * Describe how to draw the levels of the game
     * 
     * @param whichLevel
     *            The number of the level being drawn
     */
    abstract public void configureLevel(int whichLevel);

    /**
     * Describe how to draw the help scenes
     * 
     * @param whichScene
     *            The number of the help scene being drawn
     */
    abstract public void configureHelpScene(int whichScene);

    /**
     * Describe how to draw the splash scene
     */
    abstract public void configureSplash();

    /**
     * When a Hero collides with an Obstacle for which a HeroCollideCallback has
     * been set, this code will run
     * 
     * @param id
     *            The number assigned to the Obstacle's HeroCollideCallback
     * @param whichLevel
     *            The current level
     * @param o
     *            The obstacle involved in the collision
     * @param h
     *            The hero involved in the collision
     */
    abstract public void onHeroCollideCallback(int id, int whichLevel, Obstacle o, Hero h);

    /**
     * When the player touches an entity that has a TouchCallback attached to
     * it, this code will run
     * 
     * @param id
     *            The number assigned to the entity's TouchCallback
     * @param whichLevel
     *            The current level
     * @param o
     *            The entity involved in the collision
     */
    abstract public void onTouchCallback(int id, int whichLevel, PhysicsSprite o);

    /**
     * When an enemy is defeated, this code will run if the enemy has an
     * EnemyDefeatCallback
     * 
     * @param id
     *            The number assigned to this callback
     * @param whichLevel
     *            The current level
     * @param e
     *            The enemy who was defeated
     */
    abstract public void onEnemyDefeatCallback(int id, int whichLevel, Enemy e);

    /**
     * When an obstacle collides with an enemy, if the obstacle has an
     * EnemyCollideCallback, then this code will run.
     * 
     * @param id
     *            The number assigned to this callback
     * @param whichLevel
     *            The current level
     * @param o
     *            The obstacle involved in the collision
     * @param e
     *            The enemy involved in the collision
     */
    abstract public void onEnemyCollideCallback(int id, int whichLevel, Obstacle o, Enemy e);

    /**
     * When a projectile collides with an obstacle, if the obstacle has a
     * ProjectileCollideCallback, then this code will run
     * 
     * @param id
     *            The number assigned to this callback
     * @param whichLevel
     *            The current level
     * @param o
     *            The obstacle involved in the collision
     * @param p
     *            The projectile involved in the collision
     */
    abstract public void onProjectileCollideCallback(int id, int whichLevel, Obstacle o, Projectile p);

    /**
     * When a level finishes, this code will run
     * 
     * @param whichLevel
     *            The current level
     * @param win
     *            True if the level was won, false otherwise
     */
    abstract public void onLevelCompleteCallback(int whichLevel, boolean win);

    /**
     * When a Control is pressed, for which there is a ControlCallback that
     * takes an entity and value, this code will run.
     * 
     * @param id
     *            The number assigned to this callback
     * @param val
     *            The value that was sent by the control
     * @param entity
     *            The entity to modify via this control press
     * @param whichLevel
     *            The current level
     */
    abstract public void onControlPressEntityCallback(int id, float val, PhysicsSprite entity, int whichLevel);

    /**
     * When a hero collides with a goodie or enemy, and it leads to the hero's
     * strength changing, we can opt to run this code.
     * 
     * @param whichLevel
     *            The current level
     * @param h
     *            The hero whose strength just changed
     */
    abstract public void onStrengthChangeCallback(int whichLevel, Hero h);

    /**
     * When a PauseScene button is pressed, this code will run.
     * 
     * @param whichLevel
     *            The current level
     * @param id
     *            The number assigned to this PauseScene button
     */
    abstract public void onPauseSceneCallback(int whichLevel, int id);
}