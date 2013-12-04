package edu.lehigh.cse.ale;

// TODO: handle public stuff once we get rid of screens package

// TODO: are we going to support LiveEdit?

// TODO: rename from ALE to something else... lol (Lehigh Overlay for LibGDX)?

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;

public abstract class ALE implements ApplicationListener, InputProcessor
{

    /*
     * CONFIGURATION
     */

    ALEConfiguration    _config;

    SplashConfiguration _splashConfig;

    abstract public SplashConfiguration splashConfig();

    abstract public ALEConfiguration config();

    /*
     * CORE DECLARATIVE STUFF
     */

    abstract public void nameResources();

    abstract public void configureLevel(int whichLevel);

    abstract public void configureHelpScene(int whichScene);

    /*
     * OPTIONAL EVENT METHODS
     */
    abstract public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h);

    abstract public void onTouchTrigger(int id, int whichLevel, Obstacle o);

    abstract public void onTimeTrigger(int id, int whichLevel);

    abstract public void onEnemyTimeTrigger(int id, int whichLevel, Enemy e);

    abstract public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e);

    abstract public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e);

    abstract public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p);

    abstract public void levelCompleteTrigger(boolean win);

    abstract public void onControlPressTrigger(int id, int whichLevel);

    /*
     * INITIALIZATION
     */

    static ALE _game;

    @Override
    public void create()
    {
        _game = this;
        // get configuration
        _config = config();
        _splashConfig = splashConfig();

        // for handling back presses
        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);

        // get number of unlocked levels
        readUnlocked();

        // Load Resources
        nameResources();

        // show the splash screen
        doSplash();
    }

    /*
     * NAVIGATION
     */

    /**
     * Modes of the game: we can be showing the main screen, the help screens,
     * the level chooser, or a playable level
     */
    public enum Modes
    {
        SPLASH, HELP, CHOOSE, PLAY
    };

    /**
     * The current mode of the program
     */
    public static Modes _mode;

    /**
     * The _current level being played
     */
    static int          _currLevel;

    /**
     * Track the _current help scene being displayed
     */
    static private int  _currHelp;

    public void doSplash()
    {
        // set the default display mode
        _currLevel = 0;
        _mode = Modes.SPLASH;
        _currHelp = 0;
        setScreen(new Splash(this));
    }

    public void doChooser()
    {
        _currLevel = 0;
        _currHelp = 0;
        _mode = Modes.CHOOSE;
        setScreen(new Chooser(this));
    }

    public void doPlayLevel(int which)
    {
        _currLevel = which;
        _currHelp = 0;
        _mode = Modes.PLAY;
        configureLevel(which);
        setScreen(Level._currLevel);
    }

    public void doHelpLevel()
    {
    }

    public void doQuit()
    {
        _screen.dispose();
        Gdx.app.exit();
    }

    /**
     * Advance to the next help scene
     */
    @Deprecated
    void nextHelp()
    {
        if (_currHelp < _config.getNumHelpScenes()) {
            _mode = Modes.HELP;
            _currHelp++;
            // configure the next help scene, and show it.
            // ALE._self.configureHelpScene(_currHelp);
            // ALE._self.getEngine().setScene(ALE._self._helpScene._current);
        }
        else {
            _currHelp = 0;
            _mode = Modes.SPLASH;
            // TODO: should we be destroying, or re-showing, the Screen?
            setScreen(new Splash(this));
        }
    }

    /**
     * Advance to the next level and show it
     */
    @Deprecated
    void nextLevel()
    {
        if (_currLevel < _config.getNumLevels()) {
            _currLevel++;
            // show it
        }
        else {
            _currLevel = 0;
            _mode = Modes.CHOOSE;
            setScreen(new Chooser(this));
        }
    }

    /*
     * SCREEN MANAGEMENT/FORWARDING
     */

    private MyScreen _screen;

    @Override
    public void dispose()
    {
        if (_screen == null)
            return;
        _screen.hide();
        _screen.dispose();

        // TODO: dispose of all fonts, textureregions, etc...
        
        // if we get rid of all references to fonts, we'll recreate
        // appropriately for the new gdx context on resume
        Media._fonts.clear(); 
    }

    @Override
    public void pause()
    {
        if (_screen != null)
            _screen.pause();
    }

    @Override
    public void resume()
    {
        if (_screen != null)
            _screen.resume();
    }

    @Override
    public void render()
    {
        if (_screen != null)
            _screen.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height)
    {
        if (_screen != null)
            _screen.resize(width, height);
    }

    /**
     * Sets the current screen. {@link Screen#hide()} is called on any old
     * screen, and {@link Screen#show()} is called on the new screen, if any.
     * 
     * @param screen
     *            may be {@code null}
     */
    public void setScreen(MyScreen screen)
    {
        if (_screen != null)
            _screen.hide();
        _screen = screen;
        if (_screen != null) {
            _screen.show();
            _screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    /*
     * SAVING PROGRESS THROUGH LEVELS
     */

    /**
     * ID of the highest level that is unlocked
     */
    public static int           _unlockLevel;

    /**
     * The name of the app preference
     * 
     * TODO: before releasing a game, you should modify this... maybe put it in config?
     */
    static private final String PREFS = "edu.lehigh.cse.ale.prefs";

    /**
     * save the value of 'unlocked' so that the next time we play, we don't have
     * to start at level 0
     */
    static void saveUnlocked()
    {
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        prefs.putInteger("unlock", _unlockLevel);
        prefs.flush();
    }

    /**
     * read the _current value of 'unlocked' to know how many levels to unlock
     */
    static void readUnlocked()
    {
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        _unlockLevel = prefs.getInteger("unlock", 1);
    }

    /*
     * KEYPRESS FUNCTIONALITY
     */

    @Override
    public boolean keyDown(int keycode)
    {
        // ESCAPE is just for desktop
        if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
            // if we're looking at main menu, then exit
            if (_mode == Modes.SPLASH) {
                _screen.dispose();
                Gdx.app.exit();
                return false;
            }
            // if we're looking at the chooser or help, switch to the splash
            // screen
            if (_mode == Modes.CHOOSE || _mode == Modes.HELP) {
                doSplash();
                return false;
            }
            // ok, we're looking at a game scene... switch to chooser

            // reset the hero statistics
            // TODO: this is not well encapsulated right now
            // Score._heroesCreated = 0;
            // Score._heroesDefeated = 0;
            // Level._heroes.clear();
            // if (Level._lastHero != null)
            // Level._lastHero._sprite.clearUpdateHandlers();
            // Level._lastHero = null;

            _mode = Modes.CHOOSE;
            setScreen(new Chooser(this));
            return false;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    /*
     * MANAGE TOUCH INTERACTION
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (_screen != null)
            return _screen.touchDown(screenX, screenY, pointer, button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if (_screen != null)
            return _screen.touchUp(screenX, screenY, pointer, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        if (_screen != null)
            return _screen.touchDragged(screenX, screenY, pointer);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }

}