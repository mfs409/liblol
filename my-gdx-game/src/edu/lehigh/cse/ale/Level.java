package edu.lehigh.cse.ale;

// TODO: this is going to be the hardest part...

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

// TODO: do we want to fixed-step the physics world?

// STATUS: in progress

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class Level implements MyScreen
{
    /**
     * A simple wrapper for when we want stuff to happen. We're going to abuse
     * this type as a generic way to do everything we need to do regarding
     * delayed actions, hud updates, buttons, etc.
     */
    abstract static class PendingEvent
    {
        abstract void go();

        boolean   _done;

        Rectangle _range;

        boolean   _onlyOnce;

        TextureRegion tr;
        
        void disable()
        {
            _done = true;
        }

        void enable()
        {
            _done = false;
        }
    }

    abstract static class HudEntity
    {
        boolean _enabled = true;
        abstract void render(SpriteBatch sb);
        // override this!
        void update(){}
    }
    
    // for now, just one list of everything we need to render...
    public ArrayList<PhysicsSprite> _sprites;

    // TODO: come up with a better plan for these various collections...
    public ArrayList<HudEntity> _spriteUpdates;
    
    public ArrayList<PendingEvent>  _events;

    public ArrayList<PendingEvent>  _controls;

    public ArrayList<PhysicsSprite> _routes;
    
    public ArrayList<HudEntity> _hudEntries;
    
    void addTouchEvent(float x, float y, float width, float height, boolean onlyOnce, PendingEvent action)
    {
        action._range = new Rectangle(x, y, width, height);
        action._onlyOnce = onlyOnce;
        action.enable();
        _controls.add(action);
    }

    /*
     * INTERNAL CLASSES
     */

    /*
     * BASIC LEVEL CONFIGURATION
     */

    // the camera
    OrthographicCamera _gameCam;

    OrthographicCamera _hudCam;

    // box2d debug renderer
    private Box2DDebugRenderer _debugRender;

    // a spritebatch and a font for text rendering and a Texture to draw our
    // boxes
    private SpriteBatch        _spriteRender;

    // debug only
    private ShapeRenderer _shapeRender;
    
    PhysicsSprite _chase;
    
    // box2d world
    public World               _world;

    static Level           _currLevel;

    ALE                        _game;

    int _camBoundX;
    int _camBoundY;
    
    public Level(int width, int height, ALE game)
    {
        _game = game;
        _currLevel = this;
        _camBoundX = width;
        _camBoundY = height;
        
        // TODO: update comments
        // setup the camera. In Box2D we operate on a
        // meter scale, pixels won't do it. So we use
        // an orthographic camera with a viewport of
        // 48 meters in width and 32 meters in height.
        // We also position the camera so that it
        // looks at (0,16) (that's where the middle of the
        // screen will be located).
        
        _gameCam = new OrthographicCamera(_game._config.getScreenWidth()/10, _game._config.getScreenHeight()/10);
        _gameCam.position.set(width / 2, height / 2, 0);
        // default camera position is with (0,0) in the bottom left...
        // TODO: externalize the '10' to a PIXELS_PER_METER constant
        _gameCam.position.set(_game._config.getScreenWidth()/10/2, _game._config.getScreenHeight()/10/2, 0);
        _gameCam.zoom = 1;

        // the hudcam is easy... it's the size of the screen, and is independent of the world
        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();
        _hudCam = new OrthographicCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // next we create the box2d debug renderer
        _debugRender = new Box2DDebugRenderer();

        // next we create a SpriteBatch and a font
        _spriteRender = new SpriteBatch();

        _shapeRender = new ShapeRenderer();
        
        // A place for everything we draw...
        _sprites = new ArrayList<PhysicsSprite>();
        _events = new ArrayList<PendingEvent>();
        _spriteUpdates = new ArrayList<HudEntity>();
        _controls = new ArrayList<PendingEvent>();
        _routes = new ArrayList<PhysicsSprite>();
        _hudEntries = new ArrayList<HudEntity>();
        
        // reset tilt control...
        Tilt.reset();

        // reset scores
        Score.reset();
        
        // TODO: remove this:
        Controls.addFPS(400, 15, 200, 200, 100, 12);
    }

    /*
     * MUSIC MANAGEMENT
     */

    Music _music;

    boolean      _musicPlaying = false;

    public void playMusic()
    {
        if (!_musicPlaying && _music != null) {
            _musicPlaying = true;
            _music.play();
        }
    }

    public void pauseMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.pause();
        }
    }

    public void stopMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.stop();
        }
    }

    @Override
    public void render(float delta)
    {
        playMusic();
        
        // Custom code path for when there is a popup... popups incude the text
        // we show when starting the level, and the text we show when ending the
        // level
        if (PopUpScene._showPopUp) {
            // next we clear the color buffer and set the camera matrices
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            _hudCam.update();
            _spriteRender.setProjectionMatrix(_hudCam.combined);
            _spriteRender.begin();
            PopUpScene.show(_spriteRender, _game);
            _spriteRender.end();
            Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
            return;
        }

        // handle accelerometer stuff
        Tilt.handleTilt();

        // handle routes
        for (PhysicsSprite ps : _routes) {
            if (ps._isRoute && ps._visible)
                ps.routeDriver();
        }
        
        // TODO: do we want to do fixed steps?  See Level1.java?
        
        // first we update the world. For simplicity we use the delta time
        // provided by the Graphics instance. Normally you'll want to fix the
        // time step.
        // long start = TimeUtils.nanoTime();
        _world.step(Gdx.graphics.getDeltaTime(), 8, 3);
        // float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

        // now handle any events that occurred on account of the world movement
        for (PendingEvent pe : _events)
            pe.go();
        _events.clear();

        // now do any sprite updates
        for (HudEntity he : _spriteUpdates)
            if (he._enabled)
                he.update();
        
        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        adjustCamera();
        _gameCam.update();

        // next we render each box via the SpriteBatch
        _spriteRender.setProjectionMatrix(_gameCam.combined);
        _spriteRender.begin();
        for (PhysicsSprite ps : _sprites) {
            if (ps._visible) {
                Vector2 pos = ps._physBody.getPosition();
                float angle = MathUtils.radiansToDegrees * ps._physBody.getAngle();
                _spriteRender.draw(ps._tr, pos.x - ps._width / 2, pos.y - ps._height / 2, ps._width / 2,
                        ps._height / 2, ps._width, ps._height, 1, 1, angle);
            }
        }
        _spriteRender.end();

        // next we use the debug renderer to draw outlines of physics entities
        if (_game._config.showDebugBoxes())
            _debugRender.render(_world, _gameCam.combined);

        // render the hud
        _hudCam.update();
        _spriteRender.setProjectionMatrix(_hudCam.combined);
        _spriteRender.begin();
        // do the buttons
        for (PendingEvent pe : _controls) {
            if (pe.tr != null)
                _spriteRender.draw(pe.tr, pe._range.x, pe._range.y, 0,0, pe._range.width, pe._range.height, 1, 1, 0);
        }
        // do other stuff (e.g., text)
        for (HudEntity he : _hudEntries)
            he.render(_spriteRender);        
        _spriteRender.end();

        // render debug boxes for hud buttons
        if (_game._config.showDebugBoxes()) {
            _shapeRender.setProjectionMatrix(_hudCam.combined);
            _shapeRender.begin(ShapeType.Line);
            _shapeRender.setColor(Color.RED);
            for (PendingEvent pe : _controls)
                _shapeRender.rect(pe._range.x, pe._range.y, pe._range.width, pe._range.height);
            _shapeRender.end();
        }
        
    }

    void adjustCamera()
    {
        if (_chase == null)
            return;
        float x = _chase._physBody.getWorldCenter().x;
        float y = _chase._physBody.getWorldCenter().y;
        // if x or y is too close to 0,0, stick with minimum acceptable values
        // TODO: remove constants
        if (x < _game._config.getScreenWidth()/10/2)
            x = _game._config.getScreenWidth()/10/2;
        if (y < _game._config.getScreenHeight()/10/2)
            y = _game._config.getScreenHeight()/10/2;
        
        // if x or y is too close to MAX,MAX, stick with max acceptable values
        // TODO: remove constants
        if (x > _camBoundX - _game._config.getScreenWidth()/10/2)
            x = _camBoundX - _game._config.getScreenWidth()/10/2;
        if (y > _camBoundY - _game._config.getScreenHeight()/10/2)
            y = _camBoundY - _game._config.getScreenHeight()/10/2;

        // update the camera
        _gameCam.position.set(x, y, 0); 
    }
    
    @Override
    public void resize(int width, int height)
    {
       }

    @Override
    public void show()
    {
       }

    @Override
    public void hide()
    {
        pauseMusic();
       }

    @Override
    public void pause()
    {
       }

    @Override
    public void resume()
    {
       }

    @Override
    public void dispose()
    {
        stopMusic();
    }

    private Vector3 _touchVec = new Vector3();

    PhysicsSprite hitSprite = null;
    // instantiate the callback here, so we don't irritate the GC
    QueryCallback callback = new QueryCallback() {

        @Override
        public boolean reportFixture(Fixture fixture) {
            // if the hit point is inside the fixture of the body // we report
            // it
            if (fixture.testPoint(_touchVec.x, _touchVec.y)) {
                hitSprite = (PhysicsSprite)fixture.getBody().getUserData();
                return false;
            } else
                return true;
        }
    };

    
    @Override
    public boolean touchDown(int x, int y, int pointer, int newParam)
    {
        // check for HUD touch:
        for (PendingEvent pe : _controls) {
            if (!pe._done) {
                _hudCam.unproject(_touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                if (pe._range.contains(_touchVec.x, _touchVec.y)) {
                    if (pe._onlyOnce)
                        pe.disable();
                    pe.go();
                    return false;
                }
            }
        }
        // check for sprite touch
        // translate the mouse coordinates to world coordinates
        _touchVec.set(x, y, 0);
        _gameCam.unproject(_touchVec);

        // ask the world which bodies are within the given // bounding box
        // around the mouse pointer
        hitSprite = null;
        _world.QueryAABB(callback, _touchVec.x - 0.1f, _touchVec.y - 0.1f,
                _touchVec.x + 0.1f, _touchVec.y + 0.1f);

        
        // if we hit something we create a new mouse joint
        // and attach it to the hit body.
        if (hitSprite != null) {
            Gdx.app.log("hit", "x ="+_touchVec.x+", y ="+_touchVec.y);
            hitSprite.handleTouchDown(_touchVec.x, _touchVec.y);
        }
        else {
            Gdx.app.log("nohit", "x ="+_touchVec.x+", y ="+_touchVec.y);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer)
    {
        if (hitSprite == null) 
            return false;

        _touchVec.set(x, y, 0);
        _gameCam.unproject(_touchVec);
        hitSprite.handleTouchDrag(_touchVec.x, _touchVec.y);
        
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button)
    {
        return false;
    }

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
        _currLevel = new Level(width, height, ALE._game);
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
        _currLevel._chase = ps;
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
        _currLevel._music = m;
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
