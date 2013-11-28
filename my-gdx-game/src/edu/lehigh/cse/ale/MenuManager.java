package edu.lehigh.cse.ale;

import com.badlogic.gdx.utils.Timer;

import edu.lehigh.cse.ale.GameLevel.PendingEvent;

// this will probably roll into score

public class MenuManager
{

    /*
     * INTERNAL CLASSES
     */

    /*
     * HELP LEVELS
     */

    /*
     * MANAGE WINNING AND LOSING LEVELS
     */

    /**
     * When a level ends in failure, this is how we shut it down, print a
     * message, and then let the user resume it
     * 
     * @param loseText
     *            Text to print when the level is lost
     */
    static void loseLevel(String loseText)
    {
        // Prevent multiple calls from behaving oddly
        if (Level._gameOver)
            return;
        Level._gameOver = true;

        // Run the level-complete trigger
        ALE._game.levelCompleteTrigger(false);
        
        if (Level._loseSound != null)
            Level._loseSound.play();

        // drop everything from the hud
        GameLevel._currLevel._controls.clear();
        
        // TODO: For now, we'll just (ab)use the setPopUp feature... need to make it more orthogonal eventually...
        // 
        // NB: we can call setpopupimage too, which would make this all "just work" for ALE, though still not orthogonal 
        PopUpScene.setPopUp(loseText, 255, 255, 255, 32);
        if (Level._backgroundYouLost != null) {
            PopUpScene.setPopUpImage(Media.getImage(Level._backgroundYouLost), 0, 0, ALE._game._config.getScreenWidth(), ALE._game._config.getScreenHeight());
        }
        // NB: timers really need to be stored somewhere, so we can stop/start them without resorting to this coarse mechanism
        Timer.instance().clear();
        GameLevel._currLevel.addTouchEvent(0, 0, ALE._game._config.getScreenWidth(),
                ALE._game._config.getScreenHeight(), true, new PendingEvent()
                {
                    public void go()
                    {
                        PopUpScene._showPopUp = false;
                        ALE._game.doPlayLevel(ALE._currLevel);
                    }
                });
    }

    /**
     * When a level is won, this is how we end the scene and allow a transition
     * to the next level
     */
    static void winLevel()
    {
        // Prevent multiple calls from behaving oddly
        if (Level._gameOver)
            return;
        Level._gameOver = true;

        // Run the level-complete trigger
        ALE._game.levelCompleteTrigger(true);
        
        if (Level._winSound != null)
            Level._winSound.play();

        if (_unlocklevel == _currLevel) {
            _unlocklevel++;
            ALE.saveUnlocked();
        }

        // drop everything from the hud
        GameLevel._currLevel._controls.clear();
        
        // TODO: For now, we'll just (ab)use the setPopUp feature... need to make it more orthogonal eventually...
        // 
        // NB: we can call setpopupimage too, which would make this all "just work" for ALE, though still not orthogonal 
        PopUpScene.setPopUp(Level._textYouWon, 255, 255, 255, 32);
        if (Level._backgroundYouWon != null) {
            PopUpScene.setPopUpImage(Media.getImage(Level._backgroundYouWon), 0, 0, ALE._game._config.getScreenWidth(), ALE._game._config.getScreenHeight());
        }
        // NB: timers really need to be stored somewhere, so we can stop/start them without resorting to this coarse mechanism
        Timer.instance().clear();
        GameLevel._currLevel.addTouchEvent(0, 0, ALE._game._config.getScreenWidth(),
                ALE._game._config.getScreenHeight(), true, new PendingEvent()
                {
                    public void go()
                    {
                        PopUpScene._showPopUp = false;
                        if (ALE._currLevel == ALE._game._config.getNumLevels())
                            ALE._game.doChooser();
                        else {
                            ALE._currLevel++;
                            ALE._game.doPlayLevel(ALE._currLevel);
                        }
                    }
                });
    }

    /*
     * INTERNAL CLASSES
     */

    /**
     * Modes of the game: we can be showing the main screen, the help screens,
     * the level chooser, or a playable level
     */
    enum Modes
    {
        SPLASH, HELP, CHOOSE, PLAY
    };

    /*
     * BASIC FUNCITONALITY
     */

    /**
     * The _current mode of the program
     */
    static Modes                _mode;

    /**
     * The _current level being played
     */
    static int                  _currLevel;

    /**
     * A font for drawing text
     */
    // static Font _menuFont;

    /**
     * An invisible image. We overlay this on buttons so that we have more
     * control of the size and shape of the
     * touchable region.
     */
    // static TiledTextureRegion _invis;

    /**
     * Initialize the manager to a clean state
     */
    /*
     * static void configure()
     * {
     * // get number of unlocked levels
     * readUnlocked();
     * 
     * // set the default display mode
     * _currLevel = -1;
     * _mode = Modes.SPLASH;
     * _currHelp = 0;
     * 
     * // load an invisible png for use in menus, and configure a font
     * BitmapTextureAtlas bta = new
     * BitmapTextureAtlas(ALE._self.getTextureManager(), 2, 2,
     * TextureOptions.DEFAULT);
     * _invis = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bta,
     * ALE._self, "invis.png", 0, 0, 1, 1);
     * ALE._self.getEngine().getTextureManager().loadTexture(bta);
     * bta = new BitmapTextureAtlas(ALE._self.getTextureManager(), 256, 256,
     * TextureOptions.DEFAULT);
     * _menuFont = new Font(ALE._self.getFontManager(), bta,
     * Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32,
     * true, Color.WHITE);
     * ALE._self.getTextureManager().loadTexture(bta);
     * ALE._self.getFontManager().loadFont(_menuFont);
     * }
     * 
     * /*
     * HELP LEVELS
     */

    /**
     * Track the _current help scene being displayed
     */
    static private int          _currHelp;

    /**
     * Advance to the next help scene
     */
    /*
     * static void nextHelp()
     * {
     * if (_currHelp < Configuration.getHelpScenes()) {
     * _mode = Modes.HELP;
     * _currHelp++;
     * ALE._self.configureHelpScene(_currHelp);
     * ALE._self.getEngine().setScene(ALE._self._helpScene._current);
     * }
     * else {
     * _currHelp = 0;
     * _mode = Modes.SPLASH;
     * ALE._self.getEngine().setScene(Splash.draw(_menuFont));
     * }
     * }
     * 
     * /*
     * SAVING PROGRESS THROUGH LEVELS
     */

    /**
     * ID of the highest level that is unlocked
     */
    static int                  _unlocklevel = 1;

    /**
     * The name of the file that stores how many levels are unlocked
     */
    static private final String LOCKFILE     = "LOCKFILE";

    /**
     * save the value of 'unlocked' so that the next time we play, we don't have
     * to start at level 0
     */
    /*
     * static void saveUnlocked()
     * {
     * // to write a file, we create a fileoutputstream using the file name.
     * // Then we make a dataoutputstream from the fileoutputstream.
     * // Then we write to the dataoutputstream
     * try {
     * FileOutputStream fos = ALE._self.openFileOutput(LOCKFILE,
     * Context.MODE_PRIVATE);
     * DataOutputStream dos = new DataOutputStream(fos);
     * dos.writeInt(_unlocklevel);
     * dos.close();
     * fos.close();
     * }
     * catch (IOException e) {
     * }
     * }
     * 
     * /**
     * read the _current value of 'unlocked' to know how many levels to unlock
     */
    /*
     * static void readUnlocked()
     * {
     * // try to open the file. If we can't, then just set unlocked to 1 and
     * // return. Otherwise, read the int and update _unlocklevel
     * try {
     * // set the initial value of unlocked
     * _unlocklevel = 1;
     * 
     * // open the file and read the int
     * FileInputStream fos = ALE._self.openFileInput(LOCKFILE);
     * DataInputStream dos = new DataInputStream(fos);
     * _unlocklevel = dos.readInt();
     * fos.close();
     * }
     * catch (IOException e) {
     * _unlocklevel = 1;
     * return;
     * }
     * }
     * 
     * /*
     * KEYBOARD INTERACTION
     */

    /**
     * Handle back button presses by changing the screen that is being displayed
     * 
     * @return always true, since we always handle the event
     */
    /*
     * static boolean onBack()
     * {
     * // if we're looking at main menu, then exit
     * if (_mode == Modes.SPLASH) {
     * Splash.stopMusic();
     * ALE._self.finish();
     * return true;
     * }
     * // if we're looking at the chooser or help, switch to the splash screen
     * if (_mode == Modes.CHOOSE || _mode == Modes.HELP) {
     * _mode = Modes.SPLASH;
     * _currHelp = 0;
     * ALE._self.getEngine().setScene(Splash.draw(_menuFont));
     * return true;
     * }
     * // ok, we're looking at a game scene... switch to chooser
     * 
     * // reset the hero statistics
     * // TODO: this is not well encapsulated right now
     * Score._heroesCreated = 0;
     * Score._heroesDefeated = 0;
     * Level._heroes.clear();
     * if (Level._lastHero != null)
     * Level._lastHero._sprite.clearUpdateHandlers();
     * Level._lastHero = null;
     * 
     * _mode = Modes.CHOOSE;
     * ALE._self.getEngine().setScene(Chooser.draw(_menuFont));
     * return true;
     * }
     * 
     * /*
     * MANAGE WINNING AND LOSING LEVELS
     */

    /**
     * When a level ends in failure, this is how we shut it down, print a
     * message, and then let the user resume it
     * 
     * @param loseText
     *            Text to print when the level is lost
     */
    /*
     * static void loseLevel(String loseText)
     * {
     * ALE._self.levelCompleteTrigger(false);
     * if (Level._gameOver)
     * return;
     * Level._gameOver = true;
     * if (Level._loseSound != null)
     * Level._loseSound.play();
     * 
     * Controls.resetHUD();
     * 
     * Level.hideAllHeroes();
     * // dim out the screen by putting a slightly transparent black rectangle
     * // on the HUD
     * Rectangle r = new Rectangle(0, 0, Configuration.getCameraWidth(),
     * Configuration.getCameraHeight(),
     * ALE._self.getVertexBufferObjectManager())
     * {
     * // When this _sprite is pressed, we re-create the level
     * 
     * @Override
     * public boolean onAreaTouched(TouchEvent e, float x, float y)
     * {
     * if (e.getAction() != TouchEvent.ACTION_DOWN)
     * return false;
     * 
     * // now draw the chooser screen
     * ALE._self.getEngine().clearUpdateHandlers();
     * ALE._self._camera.setHUD(new HUD());
     * ALE._self.configureLevel(_currLevel);
     * ALE._self.getEngine().setScene(Level._current);
     * if (Level._music != null)
     * Level._music.play();
     * 
     * // NB: we return true because we are acting on account of the
     * // touch, so we don't want to propoagate the touch to an
     * // underlying entity
     * return true;
     * }
     * };
     * r.setColor(0, 0, 0);
     * r.setAlpha(0.9f);
     * Controls._hud.registerTouchArea(r);
     * Controls._hud.attachChild(r);
     * Controls._timerActive = false;
     * 
     * // draw a background image?
     * if (Level._backgroundYouLost != null) {
     * AnimatedSprite as = new AnimatedSprite(0, 0,
     * Configuration.getCameraWidth(),
     * Configuration.getCameraHeight(),
     * Media.getImage(Level._backgroundYouLost),
     * ALE._self.getVertexBufferObjectManager());
     * Controls._hud.attachChild(as);
     * }
     * 
     * Text t = new Text(100, 100, _menuFont, loseText,
     * ALE._self.getVertexBufferObjectManager());
     * float w = t.getWidth();
     * float h = t.getHeight();
     * t.setPosition(Configuration.getCameraWidth() / 2 - w / 2,
     * Configuration.getCameraHeight() / 2 - h / 2);
     * Controls._hud.attachChild(t);
     * }
     * 
     * /**
     * When a level is won, this is how we end the scene and allow a transition
     * to the next level
     */
    /*
     * static void winLevel()
     * {
     * ALE._self.levelCompleteTrigger(true);
     * if (Level._gameOver)
     * return;
     * Level._gameOver = true;
     * if (Level._winSound != null)
     * Level._winSound.play();
     * 
     * if (_unlocklevel == _currLevel) {
     * _unlocklevel++;
     * saveUnlocked();
     * }
     * 
     * Level.hideAllHeroes();
     * // dim out the screen by putting a slightly transparent black rectangle
     * on the HUD
     * Rectangle r = new Rectangle(0, 0, Configuration.getCameraWidth(),
     * Configuration.getCameraHeight(),
     * ALE._self.getVertexBufferObjectManager())
     * {
     * // When the rectangle is pressed, we change the level to 1 and
     * // switch to the level picker mode, then we display the appropriate scene
     * 
     * @Override
     * public boolean onAreaTouched(TouchEvent e, float x, float y)
     * {
     * if (e.getAction() != TouchEvent.ACTION_DOWN)
     * return false;
     * 
     * // if we're out of levels, switch to the chooser
     * if (_currLevel == Configuration.getNumLevels()) {
     * _mode = Modes.CHOOSE;
     * if (Level._music != null && Level._music.isPlaying())
     * Level._music.pause();
     * ALE._self.getEngine().clearUpdateHandlers();
     * ALE._self.getEngine().setScene(Chooser.draw(_menuFont));
     * }
     * else {
     * _currLevel++;
     * if (Level._music != null && Level._music.isPlaying())
     * Level._music.pause();
     * ALE._self.getEngine().clearUpdateHandlers();
     * ALE._self._camera.setHUD(new HUD());
     * ALE._self.configureLevel(_currLevel);
     * ALE._self.getEngine().setScene(Level._current);
     * if (Level._music != null)
     * Level._music.play();
     * }
     * // NB: we return true because we are acting on account of the
     * // touch, so we don't want to propoagate the touch to an
     * // underlying entity
     * return true;
     * }
     * };
     * r.setColor(0, 0, 0);
     * r.setAlpha(0.9f);
     * Controls.resetHUD();
     * Controls._hud.attachChild(r);
     * Controls._hud.registerTouchArea(r);
     * Controls._timerActive = false;
     * 
     * // draw a background image?
     * if (Level._backgroundYouWon != null) {
     * AnimatedSprite as = new AnimatedSprite(0, 0,
     * Configuration.getCameraWidth(),
     * Configuration.getCameraHeight(), Media.getImage(Level._backgroundYouWon),
     * ALE._self.getVertexBufferObjectManager());
     * Controls._hud.attachChild(as);
     * }
     * 
     * Text t = new Text(100, 100, _menuFont, Level._textYouWon,
     * ALE._self.getVertexBufferObjectManager());
     * float w = t.getWidth();
     * float h = t.getHeight();
     * t.setPosition(Configuration.getCameraWidth() / 2 - w / 2,
     * Configuration.getCameraHeight() / 2 - h / 2);
     * Controls._hud.attachChild(t);
     * }
     */
}
