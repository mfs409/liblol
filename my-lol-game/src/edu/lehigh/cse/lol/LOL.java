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

// TODO: Should we allow the creation of arbitrary polygons?

// TODO: verify that flipped animations work correctly, even when they change while flipped

// TODO: the unlock mechanism is untested

// TODO: aggressively comment Chooser and MyLolGame (up to level 39)

// TODO: hero-enemy triggers and hero-goodie triggers would allow neat animation effects

// TODO: we're too dependent on the 'back' key on android phones right now... consider having a universal 'pause'
// feature, and on-screen 'back' buttons that go with it? (Status: only Chooser is a serious issue)

// TODO: Chooser.java should be redesigned into multiple screens

// TODO: Hero animation sequences could use work.  The problem is that goodie count animation information 
// can be lost if we animate, then return from the animation.  Furthermore, we don't have support for 
// invincible+X animation, or jump+crawl animation

// TODO: add jump-to-defeat enemies

// TODO: consider adding a wrapper to expose Box2d collision groups?

// TODO: consider making the public interface use pixels instead of meters?

// TODO: I'm not thrilled with the current animateByGoodieCount mechanism

// TODO: Make sure we have good error messages for common mistakes (filenames, animation, routes)

// TODO: Demo projectile setCollisionOk?

// TODO: demo Enemy collide triggers with delay

// TODO: consider making sprite sheets more useful

// TODO: Hover has a zoom bug

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class LOL extends Game {
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
    static LOL sGame;

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
    LOLConfiguration mConfig;

    /**
     * The configuration of the splash screen is accessible through this
     */
    SplashConfiguration mSplashConfig;

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
        mMode = Modes.SPLASH;
        mCurrHelpNum = 0;
        setScreen(new Splash());
    }

    /**
     * Use this to load the level-chooser screen. Note that when a game has only
     * one level, we'll never draw the level-picker screen, thereby mimicing the
     * behavior of "infinite" games.
     */
    void doChooser() {
        if (mConfig.getNumLevels() == 1) {
            if (mCurrLevelNum == 1)
                doSplash();
            else
                doPlayLevel(1);
            return;
        }
        mCurrLevelNum = 0;
        mCurrHelpNum = 0;
        mMode = Modes.CHOOSE;
        setScreen(new Chooser(this));
    }

    /**
     * Use this to load a playable level.
     * 
     * @param which The index of the level to load
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
     * @param which The index of the help level to load
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
     * @param millis The amount of time to vibrate
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

    /**
     * save the value of 'unlocked' so that the next time we play, we don't have
     * to start at level 0
     * 
     * @param value The value to save as the most recently unlocked level
     */
    void saveUnlocked(int value) {
        Preferences prefs = Gdx.app.getPreferences(mConfig.getStorageKey());
        prefs.putInteger("unlock", value);
        prefs.flush();
    }

    /**
     * read the current value of 'unlocked' to know how many levels to unlock
     */
    int readUnlocked() {
        Preferences prefs = Gdx.app.getPreferences(mConfig.getStorageKey());
        return prefs.getInteger("unlock", 1);
    }

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        sGame = this;
        // get configuration
        mConfig = config();
        mSplashConfig = splashConfig();
        mChooserConfig = chooserConfig();

        // for handling back presses
        Gdx.input.setCatchBackKey(true);

        // get number of unlocked levels
        readUnlocked();

        // Load Resources
        nameResources();

        // show the splash screen
        doSplash();
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
     * The programmer configures the splash screen by implementing this method,
     * and returning a SplashConfiguration object
     */
    abstract public LOLConfiguration config();

    /**
     * The programmer configures the chooser screen by implementing this method,
     * and returning a ChooserConfiguration object
     */
    abstract public ChooserConfiguration chooserConfig();

    /**
     * The programmer configures the splash screen by implementing this method,
     * and returning a SplashConfiguration object
     */
    abstract public SplashConfiguration splashConfig();

    /**
     * Register any sound or image files to be used by the game
     */
    abstract public void nameResources();

    /**
     * Describe how to draw the levels of the game
     * 
     * @param whichLevel The number of the level being drawn
     */
    abstract public void configureLevel(int whichLevel);

    /**
     * Describe how to draw the help scenes
     * 
     * @param whichScene The number of the help scene being drawn
     */
    abstract public void configureHelpScene(int whichScene);

    /**
     * When a Hero collides with an Obstacle for which a HeroCollideTrigger has
     * been set, this code will run
     * 
     * @param id The number assigned to the Obstacle's HeroCollideTrigger
     * @param whichLevel The current level
     * @param o The obstacle involved in the collision
     * @param h The hero involved in the collision
     */
    abstract public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h);

    /**
     * When the player touches an entity that has a TouchTrigger attached to it,
     * this code will run
     * 
     * @param id The number assigned to the entity's TouchTrigger
     * @param whichLevel The current level
     * @param o The entity involved in the collision
     */
    abstract public void onTouchTrigger(int id, int whichLevel, PhysicsSprite o);

    /**
     * When the player requests a TimerTrigger, and the required time passes,
     * this code will run
     * 
     * @param id The number assigned to the TimerTrigger
     * @param whichLevel The current level
     */
    abstract public void onTimeTrigger(int id, int whichLevel);

    /**
     * When a player requests an EnemyTimerTrigger, and the required time
     * passes, and the enemy is still visible, this code will run
     * 
     * @param id The number assigned to the EnemyTimerTrigger
     * @param whichLevel The current level
     * @param e The enemy to which the timer was attached
     */
    abstract public void onEnemyTimeTrigger(int id, int whichLevel, Enemy e);

    /**
     * When an enemy is defeated, this code will run if the enemy has an
     * EnemyDefeatTrigger
     * 
     * @param id The number assigned to this trigger
     * @param whichLevel The current level
     * @param e The enemy who was defeated
     */
    abstract public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e);

    /**
     * When an obstacle collides with an enemy, if the obstacle has an
     * EnemyCollideTrigger, then this code will run.
     * 
     * @param id The number assigned to this trigger
     * @param whichLevel The current level
     * @param o The obstacle involved in the collision
     * @param e The enemy involved in the collision
     */
    abstract public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e);

    /**
     * When a projectile collides with an obstacle, if the obstacle has a
     * ProjectileCollideTrigger, then this code will run
     * 
     * @param id The number assigned to this trigger
     * @param whichLevel The current level
     * @param o The obstacle involved in the collision
     * @param p The projectile involved in the collision
     */
    abstract public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p);

    /**
     * When a level finishes, this code will run
     * 
     * @param whichLevel The current level
     * @param win True if the level was won, false otherwise
     */
    abstract public void levelCompleteTrigger(int whichLevel, boolean win);

    /**
     * When a Control is pressed, for which there is a ControlTrigger, this code
     * will run.
     * 
     * @param id The number assigned to this trigger
     * @param whichLevel The current level
     */
    abstract public void onControlPressTrigger(int id, int whichLevel);
}

interface Renderable {
    void render(SpriteBatch sb, float elapsed);
}
