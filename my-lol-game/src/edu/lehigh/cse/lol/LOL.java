
package edu.lehigh.cse.lol;

// TODO: Have a mute button on HUD and pre/post/pause scenes?

// TODO: Do pre and post scenes need a "back to chooser" button?

// TODO: Should we allow the creation of arbitrary polygons?

// TODO: verify that flipped animations work correctly, even when they change while flipped

// TODO: does zoom work with parallax?

// TODO: the unlock mechanism is untested

// TODO: update comments for all files

// TODO: we're too dependent on the 'back' key on android phones right now... consider having a universal 'pause'
// feature, and on-screen 'back' buttons that go with it?

// TODO: remove static fields and methods throughout namespace whenever possible

// TODO: test Tilt in portrait mode, and test if upside-down screens work (landscape and portrait)

// TODO: should we allow drawing pngs over the SVG lines? If so, we'll need to have a height parameter

// TODO: put a license on every file?

// TODO: Hero animation sequences could use work.  The problem is that goodie count animation information 
// can be lost if we animate, then return from the animation.  Furthermore, we don't have support for 
// invincible+X animation, or jump+crawl animation

// TODO: add jump-to-defeat enemies

// TODO: add delays for hero-obstacle triggers?

// TODO: I'm not thrilled with how we're handling the random projectile sprites...

// TODO: consider adding goodie collect callbacks?

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class LOL extends Game {
    /**
     * Modes of the game: we can be showing the main screen, the help screens,
     * the level chooser, or a playable level
     */
    private enum Modes {
        SPLASH, HELP, CHOOSE, PLAY
    };

    /**
     * The current mode of the program
     */
    private Modes _mode;

    /**
     * The current level being played
     */
    int _currLevel;

    /**
     * Track the current help scene being displayed
     */
    int _currHelp;

    static LOL _game;

    /**
     * This variable lets us track whether the user pressed 'back' on an
     * android, or 'escape' on the desktop. We are using polling, so we swallow
     * presses that aren't preceded by a release. In that manner, holding 'back'
     * can't exit all the way out... you must press 'back' repeatedly, once for
     * each screen to revert.
     */
    boolean _keyDown;

    /**
     * The configuration of the game is accessible through this
     */
    LOLConfiguration _config;

    /**
     * The configuration of the splash screen is accessible through this
     */
    SplashConfiguration _splashConfig;

    /*
     * PUBLIC INTERFACE: GAME CONFIGURATION
     */

    /**
     * The programmer configures the splash screen by implementing this method,
     * and returning a SplashConfiguration object
     */
    abstract public LOLConfiguration config();

    /*
     * PUBLIC INTERFACE: SPLASH SCREEN CONFIGURATION
     */

    /**
     * The programmer configures the splash screen by implementing this method,
     * and returning a SplashConfiguration object
     */
    abstract public SplashConfiguration splashConfig();

    /*
     * PUBLIC INTERFACE: CORE GAME LAYOUT METHODS: RESOURCES, LEVELS, HELP
     */

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

    /*
     * PUBLIC INTERFACE: CORE EVENT METHODS
     */
    abstract public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h);

    abstract public void onTouchTrigger(int id, int whichLevel, PhysicsSprite o);

    abstract public void onTimeTrigger(int id, int whichLevel);

    abstract public void onEnemyTimeTrigger(int id, int whichLevel, Enemy e);

    abstract public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e);

    abstract public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e);

    abstract public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p);

    abstract public void levelCompleteTrigger(boolean win);

    abstract public void onControlPressTrigger(int id, int whichLevel);

    /*
     * INTERNAL INTERFACE: NAVIGATION BETWEEN SCENES
     */

    void doSplash() {
        // set the default display mode
        _currLevel = 0;
        _mode = Modes.SPLASH;
        _currHelp = 0;
        setScreen(new Splash());
    }

    void doChooser() {
        _currLevel = 0;
        _currHelp = 0;
        _mode = Modes.CHOOSE;
        setScreen(new Chooser(this));
    }

    void doPlayLevel(int which) {
        _currLevel = which;
        _currHelp = 0;
        _mode = Modes.PLAY;
        configureLevel(which);
        setScreen(Level._currLevel);
    }

    void doHelpLevel(int which) {
        _currHelp = which;
        _currLevel = 0;
        _mode = Modes.HELP;
        configureHelpScene(which);
        setScreen(HelpLevel._currLevel);
    }

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
        if (_config.getVibration())
            Gdx.input.vibrate(millis);
    }

    /**
     * We can use this method from the render loop to poll for back presses
     */
    private void handleKeyDown() {
        // if neither BACK nor ESCAPE is being pressed, do nothing, but
        // recognize future presses
        if (!Gdx.input.isKeyPressed(Keys.BACK) && !Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            _keyDown = false;
            return;
        }
        // if they key is being held down, ignore it
        if (_keyDown)
            return;
        // recognize a new back press as being a 'down' press
        _keyDown = true;
        handleBack();
    }

    void handleBack() {
        // if we're looking at main menu, then exit
        if (_mode == Modes.SPLASH) {
            dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (_mode == Modes.CHOOSE || _mode == Modes.HELP) {
            doSplash();
        } else {
            // ok, we're looking at a game scene... switch to chooser
            _mode = Modes.CHOOSE;
            setScreen(new Chooser(this));
        }
    }

    /*
     * INTERNAL INTERFACE: SAVING PROGRESS THROUGH LEVELS
     */

    /**
     * save the value of 'unlocked' so that the next time we play, we don't have
     * to start at level 0
     * 
     * @param value The value to save as the most recently unlocked level
     */
    void saveUnlocked(int value) {
        Preferences prefs = Gdx.app.getPreferences(_config.getStorageKey());
        prefs.putInteger("unlock", value);
        prefs.flush();
    }

    /**
     * read the _current value of 'unlocked' to know how many levels to unlock
     */
    int readUnlocked() {
        Preferences prefs = Gdx.app.getPreferences(_config.getStorageKey());
        return prefs.getInteger("unlock", 1);
    }

    /*
     * INTERNAL INTERFACE: INTERNAL METHODS
     */

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        _game = this;
        // get configuration
        _config = config();
        _splashConfig = splashConfig();

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
}

interface Renderable {
    void render(SpriteBatch sb, float elapsed);
}
