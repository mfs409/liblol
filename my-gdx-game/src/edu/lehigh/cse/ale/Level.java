package edu.lehigh.cse.ale;

// STATUS: in progress

import java.util.ArrayList;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Level
{
    /**
     * these are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you
     * can get the number of enemies down to 0
     */
    // TODO: duplicate with GameLevel?
    enum VictoryType
    {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

    /**
     * Text to display when the _current level is won
     */
    static String    _textYouWon;

    /**
     * Text to display when the _current level is lost
     */
    static String    _textYouLost;

    /**
     * Specify the text to display when the _current level is won
     * 
     * @param text
     *            The text to display
     */
    public static void setWinText(String text)
    {
        _textYouWon = text;
    }

    /**
     * Specify the text to display when the _current level is lost
     * 
     * @param text
     *            The text to display
     */
    public static void setLoseText(String text)
    {
        _textYouLost = text;
    }

    /**
     * Create a new empty level, and set its camera
     * 
     * @param width
     *            width of the camera
     * @param height
     *            height of the camera
     */
    public static void configure(int width, int height)
    {
        GameLevel._currLevel = new GameLevel(width, height, ALE._game);
        _gameOver = false;
        // TODO: make orthogonal
        PopUpScene._popUpImgTr = null;
        PopUpScene._popupText = null;
        // TODO: make it so there is no popup if these are null
        Level._textYouWon = "Next Level";
        Level._textYouLost = "Try Again";
    }

    /**
     * Describes how a level is won
     */
    static VictoryType _victoryType;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of heroes who must reach destinations
     */
    static int         _victoryHeroCount;

    /**
     * Indicate that the level is won by having a certain number of _heroes
     * reach destinations
     * 
     * @param howMany
     *            Number of _heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany)
    {
        _victoryType = VictoryType.DESTINATION;
        _victoryHeroCount = howMany;
    }

    public static void setCameraChase(PhysicsSprite ps)
    {
        GameLevel._currLevel._chase = ps;
    }

    /*
     * INTERNAL CLASSES
     */

    /**
     * Width of this level
     */
    static int                   _width;

    /**
     * Height of this level
     */
    static int                   _height;

    /**
     * Basic world gravity in X dimension. Usually 0.
     */
    static float                    _initXGravity;

    /**
     * Basic world gravity in Y dimension. Usually 0, unless we have a side scroller with jumping
     */
    static float                    _initYGravity;

    /**
     * Track if we are playing (false) or not
     */
    static boolean               _gameOver;

    /**
     * Store all heroes, so that we can hide them all at the end of a level
     */
    static ArrayList<Hero> _heroes       = new ArrayList<Hero>();

    /**
     * Track everything that defies gravity
     */
    static ArrayList<PhysicsSprite> _noGravity = new ArrayList<PhysicsSprite>();
    
    /**
     * Track the last hero that was created
     * 
     * In levels with only one hero (most games), this lets us keep track of the hero to operate with when we jump,
     * crawl, throw, etc
     */
    static Hero            _lastHero;

    /**
     * Track if we are in scribble mode or not
     */
    static boolean         _scribbleMode = false;

     /**
     * Register a hero with the _current level
     * 
     * @param hero
     *            The hero to register
     */
    /*
    static void onHeroCreate(Hero hero)
    {
        // add the hero to the scene, add to list of _heroes
        _current.attachChild(hero._sprite);
        _heroes.add(hero);

        // log that we made a hero
        Score._heroesCreated++;

        // Let the camera follow this hero, save it as most recently created hero
        ALE._self._camera.setChaseEntity(hero._sprite);
        _lastHero = hero;
    }

    /*
     * WINNING AND LOSING
     */


    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-1 goodies that must be collected
     */
    static int         _victoryGoodie1Count;
    
    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-2 goodies that must be collected
     */
    static int         _victoryGoodie2Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-3 goodies that must be collected
     */
    static int         _victoryGoodie3Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-4 goodies that must be collected
     */
    static int         _victoryGoodie4Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of enemies that must be defeated
     */
    static int         _victoryEnemyCount;

    /**
     * Name of the _background image for the "you won" message
     */
    static String      _backgroundYouWon;

    /**
     * Name of the _background image for the "you lost" message
     */
    static String      _backgroundYouLost;



    /**
     * Indicate that the level is won by destroying all the enemies
     * 
     * This version is useful if the number of enemies isn't known, or if the goal is to defeat enemies before more are
     * are created.
     */
    static public void setVictoryEnemyCount()
    {
        _victoryType = VictoryType.ENEMYCOUNT;
        _victoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by destroying all the enemies
     * 
     * @param howMany
     *            The number of enemies that must be defeated to win the level
     */
    static public void setVictoryEnemyCount(int howMany)
    {
        _victoryType = VictoryType.ENEMYCOUNT;
        _victoryEnemyCount = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of goodies that must be collected to win the level
     * 
     * @deprecated Use setVictoryGoodies[1-4]() instead
     */
    @Deprecated
    static public void setVictoryGoodies(int howMany)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodie1Count = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of type-1 goodies that must be collected to win the level
     */
    static public void setVictoryGoodies1(int howMany)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodie1Count = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of type-2 goodies that must be collected to win the level
     */
    static public void setVictoryGoodies2(int howMany)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodie2Count = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of type-3 goodies that must be collected to win the level
     */
    static public void setVictoryGoodies3(int howMany)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodie3Count = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of type-4 goodies that must be collected to win the level
     */
    static public void setVictoryGoodies4(int howMany)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodie4Count = howMany;
    }

    /**
     * Specify the name of the image to use as the background when printing a message that the current level was won
     * 
     * @param imgName
     *            The name of the image... be sure to register it first!
     */
    public static void setBackgroundWinImage(String imgName)
    {
        _backgroundYouWon = imgName;
    }

    /**
     * Specify the name of the image to use as the background when printing a message that the _current level was lost
     * 
     * @param imgName
     *            The name of the image... be sure to register it first!
     */
    public static void setBackgroundLoseImage(String imgName)
    {
        _backgroundYouLost = imgName;
    }

    /*
     * SOUND
     */

    /**
     * Sound to play when the level is won
     */
    static Sound _winSound;

    /**
     * Sound to play when the level is lost
     */
    static Sound _loseSound;

    /**
     * Background _music for this level
     */
    static Music _music;

    /**
     * Set the sound to play when the level is won
     * 
     * @param soundName
     *            Name of the sound file to play
     */
    public static void setWinSound(String soundName)
    {
        Sound s = Media.getSound(soundName);
        _winSound = s;
    }

    /**
     * Set the sound to play when the level is lost
     * 
     * @param soundName
     *            Name of the sound file to play
     */
    public static void setLoseSound(String soundName)
    {
        Sound s = Media.getSound(soundName);
        _loseSound = s;
    }

    /**
     * Set the _background _music for this level
     * 
     * @param musicName
     *            Name of the sound file to play
     */
    public static void setMusic(String musicName)
    {
        Music m = Media.getMusic(musicName);
        GameLevel._currLevel._music = m;
    }

    /*
     * LEVEL MANAGEMENT
     */

    /**
     * Reset the _current level to a blank slate
     * 
     * This should be called whenever starting to create a new playable level
     * 
     * @param width
     *            Width of the new scene
     * @param height
     *            Height of the new scene
     * @param initXGravity
     *            default gravity in the X dimension. Usually 0
     * @param initYGravity
     *            default gravity in the Y dimension. 0 unless the game is a side-scroller with jumping
     */
    /*
    static public void configure(int width, int height, float initXGravity, float initYGravity)
    {
        // create a scene and a _physics world
        _current = new Scene();
        _gameOver = false;
        _initXGravity = initXGravity;
        _initYGravity = initYGravity;
        Tilt.reset();
        _width = width;
        _height = height;

        ALE._self._camera.setBoundsEnabled(true);
        ALE._self._camera.setBounds(0, 0, width, height);

        _physics = new FixedStepPhysicsWorld(60, new Vector2(_initXGravity, _initYGravity), false)
        {
            // the trick here is that if there is *either* a horizontal or
            // vertical _background, we need to update it
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                for (PhysicsSprite g : _noGravity)
                    g.defyGravity();
                super.onUpdate(pSecondsElapsed);
                if (Background._background != null)
                    Background._background.setParallaxValue(ALE._self._camera.getCenterX() / Background._backgroundScrollFactor);
                if (Background._vertBackground != null)
                    Background._vertBackground.setParallaxValue(ALE._self._camera.getCenterY() / Background._backgroundScrollFactor);
            }
        };

        // set handlers and listeners
        _current.registerUpdateHandler(_physics);
        _physics.setContactListener(ALE._self);

        // reset the score
        Score.reset();
        
        // clear list of heroes
        _heroes.clear();
        if (_lastHero != null)
            _lastHero._sprite.clearUpdateHandlers();
        _lastHero = null;

        // clear antigravity entities
        _noGravity.clear();
        
        // turn off scribble mode
        _scribbleMode = false;

        // reset the factories
        Controls.resetHUD();

        // set up defaults
        ALE._self.configAccelerometer(false);
        setVictoryDestination(1);
        ALE._self._camera.setZoomFactorDirect(1);

        // reset text
        _textYouWon = "Next Level";
        _textYouLost = "Try Again";

        // Null out fields...
        _winSound = null;
        _loseSound = null;
        _music = null;
        Background._background = null;
        Background._vertBackground = null;
        _backgroundYouWon = null;
        _backgroundYouLost = null;
    }

    /*
     * TIMER TRIGGERS
     */
    
    /**
     * Specify that you want some code to run after a fixed amount of time passes.
     * 
     * @param timerId
     *            A unique identifier for this timer
     * @param howLong
     *            How long to wait before the timer code runs
     */
    /*
    public static void setTimerTrigger(int timerId, float howLong)
    {
        final int id = timerId;
        TimerHandler t = new TimerHandler(howLong, false, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler th)
            {
                if (!Level._gameOver)
                    ALE._self.onTimeTrigger(id, MenuManager._currLevel);
            }
        });
        Level._current.registerUpdateHandler(t);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time passes, and that it should return a specific
     * enemy to the programmer's code
     * 
     * @param timerId
     *            A unique identifier for this timer
     * @param howLong
     *            How long to wait before the timer code runs
     * @param e
     *            Enemy to be modified
     */
    /*
    public static void setEnemyTimerTrigger(int timerId, float howLong, Enemy e)
    {
        final int id = timerId;
        final Enemy ee = e;
        TimerHandler t = new TimerHandler(howLong, false, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler th)
            {
                if (!Level._gameOver)
                    ALE._self.onEnemyTimeTrigger(id, MenuManager._currLevel, ee);
            }
        });
        Level._current.registerUpdateHandler(t);
    }
    */
}
