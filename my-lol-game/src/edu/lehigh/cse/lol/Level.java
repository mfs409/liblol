package edu.lehigh.cse.lol;

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

// TODO: verify true/false returns from all things called from this... I don't think all our events are being chained
// correctly

// TODO: does zoom work with parallax?

// TODO: remove static fields (here and everywhere)?

// TODO: clean up comments

// TODO: zoom doesn't work right with bounds of big screens and chase heroes

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Controls.Control;

public class Level implements Screen
{
    /**
     * Custom camera that can do parallax... taken directly from GDX tests
     */
    class ParallaxCamera extends OrthographicCamera
    {
        private Matrix4 parallaxView     = new Matrix4();

        private Matrix4 parallaxCombined = new Matrix4();

        private Vector3 tmp              = new Vector3();

        private Vector3 tmp2             = new Vector3();

        /**
         * The constructor simply forwards to the OrthographicCamera constructor
         * 
         * @param viewportWidth
         *            Width of the camera
         * @param viewportHeight
         *            Height of the camera
         */
        public ParallaxCamera(float viewportWidth, float viewportHeight)
        {
            super(viewportWidth, viewportHeight);
        }

        public Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY)
        {
            update();
            tmp.set(position);
            tmp.x *= parallaxX;
            tmp.y *= parallaxY;

            parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
            parallaxCombined.set(projection);
            Matrix4.mul(parallaxCombined.val, parallaxView.val);
            return parallaxCombined;
        }
    }

    /**
     * Wrapper for actions that we generate and then want handled during the render loop
     */
    abstract static class Action
    {
        abstract void go();
    }

    interface Renderable
    {
        void render(SpriteBatch sb, float elapsed);
    }

    // Eventually we need an array of 5 different renderables, for the 5
    // indices. Note that we push some other logic into the render loop via
    // these...
    public ArrayList<Renderable> _sprites;

    public ArrayList<Renderable> _pix_minus_two;

    // Two types of events: those that run on the next render, and those that
    // run on every render. Note that events do not have a sprite or render
    // action attached to them
    public ArrayList<Action>     _oneTimeEvents;

    public ArrayList<Action>     _repeatEvents;

    // TODO: can we get by with just one list, where there are Control objects,
    // each of which has optional render, touchDown, and a touchUp methods?
    public ArrayList<Control>    _controls;

    /*
     * BASIC LEVEL CONFIGURATION
     */

    // the cameras
    OrthographicCamera           _gameCam;

    OrthographicCamera           _hudCam;

    ParallaxCamera               _bgCam;

    // box2d debug renderer
    private Box2DDebugRenderer   _debugRender;

    // a spritebatch and a font for text rendering and a Texture to draw our
    // boxes
    private SpriteBatch          _spriteRender;

    // debug only
    private ShapeRenderer        _shapeRender;

    // This is the sprite that the camera chases
    PhysicsSprite                _chase;

    // box2d world
    public World                 _world;

    static Level                 _currLevel;

    LOL                          _game;

    int                          _camBoundX;

    int                          _camBoundY;

    PreScene                     _preScene;

    PostScene                    _postScene = new PostScene();

    PauseScene                   _pauseScene;

    public Level(int width, int height, LOL game)
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

        _gameCam = new OrthographicCamera(_game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO,
                _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO);
        _gameCam.position.set(width / 2, height / 2, 0);
        // default camera position is with (0,0) in the bottom left...
        _gameCam.position.set(_game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2,
                _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2, 0);
        _gameCam.zoom = 1;

        // the hudcam is easy... it's the size of the screen, and is independent of the world
        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();
        _hudCam = new OrthographicCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // the bgcam is like the hudcam
        _bgCam = new ParallaxCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // next we create a renderer for drawing sprites
        _spriteRender = new SpriteBatch();

        // next we create the box2d debug renderer, and the shape debug renderer
        _debugRender = new Box2DDebugRenderer();
        _shapeRender = new ShapeRenderer();

        // A place for everything we draw... need 5 eventually
        _sprites = new ArrayList<Renderable>();
        _pix_minus_two = new ArrayList<Renderable>();

        // collections for all the event handlers
        _oneTimeEvents = new ArrayList<Action>();
        _repeatEvents = new ArrayList<Action>();

        // TODO: refactor... these are for displaying hud stuff, and for detecting touches of hud stuff
        _controls = new ArrayList<Control>();

        // reset tilt control...
        Tilt.reset();

        // reset scores
        Score.reset();

        callback = new QueryCallback()
        {

            @Override
            public boolean reportFixture(Fixture fixture)
            {
                // if the hit point is inside the fixture of the body we report it
                if (fixture.testPoint(_touchVec.x, _touchVec.y)) {
                    PhysicsSprite hs = (PhysicsSprite) fixture.getBody().getUserData();
                    if (hs._visible) {
                        _hitSprite = hs;
                        return false;
                    }
                }
                return true;
            }
        };

        // TODO: remove this, or flip a debug switch:
        Controls.addFPS(400, 15, 200, 200, 100, 12);

        Background.reset();
    }

    /*
     * MUSIC MANAGEMENT
     */

    Music   _music;

    boolean _musicPlaying = false;

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
        manageTouches();
        playMusic();

        // are we supposed to show a pre-scene?
        if (_preScene != null && _preScene.render(_spriteRender, _game))
            return;

        // are we supposed to show a pause-scene?
        if (_pauseScene != null && _pauseScene.render(_spriteRender, _game))
            return;

        // is the game over, such that we should be showing a post-scene?
        if (_postScene != null && _postScene.render(_spriteRender, _game))
            return;

        // handle accelerometer stuff... note that accelerometer is effectively disabled during a popup.
        Tilt.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it seems like we should be using delta instead
        // of 1/45f
        _world.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        for (Action pe : _oneTimeEvents)
            pe.go();
        _oneTimeEvents.clear();

        // now do repeat events
        for (Action pe : _repeatEvents)
            pe.go();

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(Background._bgRed, Background._bgGreen, Background._bgBlue, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // prepare the main camera... we do it here, so that the parallax code knows where to draw...
        adjustCamera();
        _gameCam.update();

        // do the parallax background
        Background.doParallaxLayers(_spriteRender);

        // next we render each box via the SpriteBatch
        _spriteRender.setProjectionMatrix(_gameCam.combined);
        _spriteRender.begin();

        for (Renderable r : _pix_minus_two)
            r.render(_spriteRender, delta);
        for (Renderable r : _sprites) {
            r.render(_spriteRender, delta);
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
        for (Control c : _controls) {
            c.render(_spriteRender);
        }
        _spriteRender.end();

        // render debug boxes for hud buttons
        if (_game._config.showDebugBoxes()) {
            _shapeRender.setProjectionMatrix(_hudCam.combined);
            _shapeRender.begin(ShapeType.Line);
            _shapeRender.setColor(Color.RED);
            for (Control pe : _controls)
                if (pe._range != null)
                    _shapeRender.rect(pe._range.x, pe._range.y, pe._range.width, pe._range.height);
            _shapeRender.end();
        }
    }

    void adjustCamera()
    {
        if (_chase == null)
            return;
        float x = _chase._physBody.getWorldCenter().x + _chase._cameraOffset.x;
        float y = _chase._physBody.getWorldCenter().y + _chase._cameraOffset.y;
        // if x or y is too close to 0,0, stick with minimum acceptable values
        if (x < _game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2)
            x = _game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2;
        if (y < _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2)
            y = _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2;

        // if x or y is too close to MAX,MAX, stick with max acceptable values
        if (x > _camBoundX - _game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2)
            x = _camBoundX - _game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2;
        if (y > _camBoundY - _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2)
            y = _camBoundY - _game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2;

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
        // TODO: stop music before showing PostScene?
    }

    private Vector3 _touchVec   = new Vector3();

    PhysicsSprite   _hitSprite  = null;

    // instantiate the callback here, so we don't irritate the GC
    QueryCallback   callback;

    // Here's a quick and dirty way to manage multitouch via polling
    boolean[]       lastTouches = new boolean[4];

    void manageTouches()
    {
        // poll for touches... we assume no more than 4 simultaneous touches
        boolean[] touchStates = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            touchStates[i] = Gdx.input.isTouched(i);
            float x = Gdx.input.getX(i);
            float y = Gdx.input.getY(i);
            if (touchStates[i] && lastTouches[i]) {
                touchMove((int) x, (int) y, i);
            }
            else if (touchStates[i] && !lastTouches[i]) {
                touchDown((int) x, (int) y, i, 0);
            }
            else if (!touchStates[i] && lastTouches[i]) {
                touchUp((int) x, (int) y, i, 0);
            }
            lastTouches[i] = touchStates[i];
        }
    }

    public boolean touchDown(int x, int y, int pointer, int button)
    {
        // swallow the touch if the popup is showing...
        if ((_preScene != null && _preScene._visible) || (_postScene != null && _postScene._visible)
                || (_pauseScene != null && _pauseScene._visible))
            return false;

        // check for HUD touch:
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable && pe._range.contains(_touchVec.x, _touchVec.y)) {
                // now convert the touch to world coordinates and pass to the control (useful for vector throw)
                _gameCam.unproject(_touchVec.set(x, y, 0));
                pe.onDownPress(_touchVec);
                return false;
            }
        }
        // check for sprite touch
        // translate the mouse coordinates to world coordinates
        _gameCam.unproject(_touchVec.set(x, y, 0));

        // ask the world which bodies are within the given bounding box around
        // the mouse pointer
        _hitSprite = null;
        _world.QueryAABB(callback, _touchVec.x - 0.1f, _touchVec.y - 0.1f, _touchVec.x + 0.1f, _touchVec.y + 0.1f);

        // if we hit something we forward an event to it
        if (_hitSprite != null) {
            Gdx.app.log("hit", "x =" + _touchVec.x + ", y =" + _touchVec.y);
            _hitSprite.handleTouchDown(_touchVec.x, _touchVec.y);
            return false;
        }
        else {
            Gdx.app.log("nohit", "x =" + _touchVec.x + ", y =" + _touchVec.y);
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._currentPokeSprite != null)
            PhysicsSprite._currentPokeSprite.finishPoke(_touchVec.x, _touchVec.y);
        if (PhysicsSprite._pokeVelocityEntity != null) {
            if (!PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, true, false, false))
                return false;
        }
        else if (PhysicsSprite._pokePathEntity != null)
            PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, true, false, false);
        // deal with scribbles
        else if (Level._scribbleMode) {
            if (Level._scribbleMode) {
                _gameCam.unproject(_touchVec.set(x, y, 0));
                Obstacle.doScribbleDown(_touchVec.x, _touchVec.y);
                return false;
            }

        }
        return false;
    }

    // TODO: we should have a collection of handlers instead of this nastiness...
    public boolean touchMove(int x, int y, int pointer)
    {
        _gameCam.unproject(_touchVec.set(x, y, 0));
        // deal with scribble?
        if (Level._scribbleMode) {
            Obstacle.doScribbleDrag(_touchVec.x, _touchVec.y);
            return false;
        }
        // deal with drag?
        if (_hitSprite != null) {
            if (_hitSprite.handleTouchDrag(_touchVec.x, _touchVec.y))
                return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokePathEntity != null) {
            if (!PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, true, false))
                return false;
        }
        if (PhysicsSprite._pokeVelocityEntity != null) {
            if (!PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, false, true, false))
                return false;
        }
        return true;
    }

    public boolean touchUp(int x, int y, int pointer, int button)
    {
        // check for HUD touch:
        //
        // TODO: is this order correct? Should HUD always come first?
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable) {
                if (pe._range.contains(_touchVec.x, _touchVec.y)) {
                    pe.onUpPress();
                    return false;
                }
            }
        }
        _gameCam.unproject(_touchVec.set(x, y, 0));
        if (Level._scribbleMode) {
            Obstacle.doScribbleUp();
            return false;
        }
        if (PhysicsSprite._flickEntity != null) {
            PhysicsSprite.flickDone(_touchVec.x, _touchVec.y);
            return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokePathEntity != null) {
            if (PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, false, true))
                return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokeVelocityEntity != null) {
            if (PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, false, false, true))
                return false;
        }
        return false;
    }

    /**
     * these are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you
     * can get the number of enemies down to 0
     */
    enum VictoryType
    {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

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
        _currLevel = new Level(width, height, LOL._game);
        _gameOver = false;
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

    /**
     * Track if we are playing (false) or not
     */
    static boolean _gameOver;

    /**
     * Track if we are in scribble mode or not
     */
    static boolean _scribbleMode       = false;

    /*
     * WINNING AND LOSING
     */

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-1 goodies that must be collected
     */
    static int[]   _victoryGoodieCount = new int[4];

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of enemies that must be defeated
     */
    static int     _victoryEnemyCount;

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
     */
    static public void setVictoryGoodies(int type1, int type2, int type3, int type4)
    {
        _victoryType = VictoryType.GOODIECOUNT;
        _victoryGoodieCount[0] = type1;
        _victoryGoodieCount[1] = type2;
        _victoryGoodieCount[2] = type4;
        _victoryGoodieCount[3] = type4;
    }

    /*
     * SOUND
     */

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
    public static void setTimerTrigger(final int timerId, float howLong)
    {
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                if (!Level._gameOver)
                    LOL._game.onTimeTrigger(timerId, LOL._game._currLevel);
            }
        }, howLong);
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
    public static void setEnemyTimerTrigger(final int timerId, float howLong, final Enemy e)
    {
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                if (!Level._gameOver)
                    LOL._game.onEnemyTimeTrigger(timerId, LOL._game._currLevel, e);
            }
        }, howLong);
    }

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
    // TODO: this is called from below and from Controls.countdown
    static void loseLevel()
    {
        // Prevent multiple calls from behaving oddly
        if (Level._gameOver)
            return;
        Level._gameOver = true;

        // Run the level-complete trigger
        LOL._game.levelCompleteTrigger(false);

        // drop everything from the hud
        Level._currLevel._controls.clear();

        if (Level._currLevel._postScene != null) {
            Level._currLevel._postScene.setWin(false);
        }
        // NB: timers really need to be stored somewhere, so we can stop/start
        // them without resorting to this coarse mechanism
        Timer.instance().clear();
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
        LOL._game.levelCompleteTrigger(true);

        if (LOL._game._unlockLevel == LOL._game._currLevel) {
            LOL._game._unlockLevel++;
            LOL._game.saveUnlocked();
        }

        // drop everything from the hud
        Level._currLevel._controls.clear();

        // TODO: For now, we'll just (ab)use the setPopUp feature... need to
        // make it more orthogonal eventually...
        //
        // NB: we can call setpopupimage too, which would make this all
        // "just work" for ALE, though still not orthogonal
        Level._currLevel._postScene.setWin(true);
        // NB: timers really need to be stored somewhere, so we can stop/start
        // them without resorting to this coarse mechanism
        Timer.instance().clear();
    }
}
