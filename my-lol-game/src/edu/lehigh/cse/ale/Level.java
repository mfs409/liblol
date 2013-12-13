package edu.lehigh.cse.ale;

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

// TODO: verify true/false returns from all things called from this... I don't think all our events are being chained
// correctly

// TODO: do we want to fixed-step the physics world?

// TODO: does zoom work with parallax?

// TODO: remove static fields (here and everywhere)?

// TODO: clean up comments

// TODO: zoom doesn't work right with bounds of big screens and chase heroes

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

import edu.lehigh.cse.ale.Controls.Control;

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
     * INTERNAL CLASSES
     */

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

    ALE                          _game;

    int                          _camBoundX;

    int                          _camBoundY;

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
        if (PreScene.show(_spriteRender, _game))
            return;

        // is the game over, such that we should be showing a post-scene?
        if (PostScene.show(_spriteRender, _game))
            return;

        // handle accelerometer stuff... note that accelerometer is effectively disabled during a popup.
        Tilt.handleTilt();

        // TODO: do we want to do fixed steps? See Level1.java?

        // first we update the world. For simplicity we use the delta time
        // provided by the Graphics instance. Normally you'll want to fix the
        // time step.
        _world.step(delta, 8, 3);
        // float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

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
        // should we forward to PreScene or PostScene?
        if (Gdx.input.justTouched()) {
            if (!PreScene.onTouch(x, y))
                return false;
            if (!PostScene.onTouch(x, y))
                return false;
        }

        // swallow the touch if the popup is showing...
        if (PreScene._showPopUp || PostScene._showPopUp)
            return false;

        // check for HUD touch:
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable && pe._range.contains(_touchVec.x, _touchVec.y)) {
                // now convert the touch to world coordinates and pass to the control (useful for vector throw)
                _touchVec.set(x, y, 0);
                _gameCam.unproject(_touchVec);
                pe.onDownPress(_touchVec);
                return false;
            }
        }
        // check for sprite touch
        // translate the mouse coordinates to world coordinates
        _touchVec.set(x, y, 0);
        _gameCam.unproject(_touchVec);

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
                _touchVec.set(x, y, 0);
                _gameCam.unproject(_touchVec);
                Obstacle.doScribbleDown(_touchVec.x, _touchVec.y);
                return false;
            }

        }
        return false;
    }

    // TODO: we should have a collection of handlers instead of this nastiness...
    public boolean touchMove(int x, int y, int pointer)
    {
        // deal with scribble?
        if (Level._scribbleMode) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
            Obstacle.doScribbleDrag(_touchVec.x, _touchVec.y);
            return false;
        }
        // deal with drag?
        if (_hitSprite != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
            if (_hitSprite.handleTouchDrag(_touchVec.x, _touchVec.y))
                return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokePathEntity != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
            if (!PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, true, false))
                return false;
        }

        if (PhysicsSprite._pokeVelocityEntity != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
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
        for (Control pe : _controls) {
            if (pe._isTouchable) {
                _hudCam.unproject(_touchVec.set(x, y, 0));
                if (pe._range.contains(_touchVec.x, _touchVec.y)) {
                    pe.onUpPress();
                    return false;
                }
            }
        }

        if (Level._scribbleMode) {
            Obstacle.doScribbleUp();
            return false;
        }
        if (PhysicsSprite._flickEntity != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
            PhysicsSprite.flickDone(_touchVec.x, _touchVec.y);
            return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokePathEntity != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
            if (PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, false, true))
                return false;
        }
        // no sprite touch... if we have a poke entity, use it
        if (PhysicsSprite._pokeVelocityEntity != null) {
            _touchVec.set(x, y, 0);
            _gameCam.unproject(_touchVec);
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
     * Text to display when the _current level is won
     */
    static String _textYouWon;

    /**
     * Text to display when the _current level is lost
     */
    static String _textYouLost;

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

        PreScene.reset();
        PostScene.reset();

        // TODO: make it so there is no popup if these are null
        // TODO: move to postscene
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
     * Track if we are playing (false) or not
     */
    static boolean _gameOver;

    /**
     * Track if we are in scribble mode or not
     */
    static boolean _scribbleMode = false;

    /*
     * WINNING AND LOSING
     */

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-1 goodies that must be collected
     */
    static int     _victoryGoodie1Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-2 goodies that must be collected
     */
    static int     _victoryGoodie2Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-3 goodies that must be collected
     */
    static int     _victoryGoodie3Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of type-4 goodies that must be collected
     */
    static int     _victoryGoodie4Count;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of enemies that must be defeated
     */
    static int     _victoryEnemyCount;

    /**
     * Name of the _background image for the "you won" message
     */
    static String  _backgroundYouWon;

    /**
     * Name of the _background image for the "you lost" message
     */
    static String  _backgroundYouLost;

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
        _victoryGoodie1Count = type1;
        _victoryGoodie2Count = type2;
        _victoryGoodie3Count = type4;
        _victoryGoodie4Count = type4;
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
    Sound        _loseSound;

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
        _currLevel._loseSound = s;
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
     * static public void configure(int width, int height, float initXGravity, float initYGravity)
     * {
     * // create a scene and a _physics world
     * _current = new Scene();
     * _gameOver = false;
     * _initXGravity = initXGravity;
     * _initYGravity = initYGravity;
     * Tilt.reset();
     * _width = width;
     * _height = height;
     * 
     * ALE._self._camera.setBoundsEnabled(true);
     * ALE._self._camera.setBounds(0, 0, width, height);
     * 
     * _physics = new FixedStepPhysicsWorld(60, new Vector2(_initXGravity, _initYGravity), false)
     * {
     * // the trick here is that if there is *either* a horizontal or
     * // vertical _background, we need to update it
     * 
     * @Override
     * public void onUpdate(float pSecondsElapsed)
     * {
     * for (PhysicsSprite g : _noGravity)
     * g.defyGravity();
     * super.onUpdate(pSecondsElapsed);
     * if (Background._background != null)
     * Background._background.setParallaxValue(ALE._self._camera.getCenterX() / Background._backgroundScrollFactor);
     * if (Background._vertBackground != null)
     * Background._vertBackground.setParallaxValue(ALE._self._camera.getCenterY() / Background._backgroundScrollFactor);
     * }
     * };
     * 
     * // set handlers and listeners
     * _current.registerUpdateHandler(_physics);
     * _physics.setContactListener(ALE._self);
     * 
     * // reset the score
     * Score.reset();
     * 
     * // clear list of heroes
     * _heroes.clear();
     * if (_lastHero != null)
     * _lastHero._sprite.clearUpdateHandlers();
     * _lastHero = null;
     * 
     * // clear antigravity entities
     * _noGravity.clear();
     * 
     * // turn off scribble mode
     * _scribbleMode = false;
     * 
     * // reset the factories
     * Controls.resetHUD();
     * 
     * // set up defaults
     * ALE._self.configAccelerometer(false);
     * setVictoryDestination(1);
     * ALE._self._camera.setZoomFactorDirect(1);
     * 
     * // reset text
     * _textYouWon = "Next Level";
     * _textYouLost = "Try Again";
     * 
     * // Null out fields...
     * _winSound = null;
     * _loseSound = null;
     * _music = null;
     * Background._background = null;
     * Background._vertBackground = null;
     * _backgroundYouWon = null;
     * _backgroundYouLost = null;
     * }
     * 
     * /*
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
                    ALE._game.onTimeTrigger(timerId, ALE._game._currLevel);
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
                    ALE._game.onEnemyTimeTrigger(timerId, ALE._game._currLevel, e);
            }
        }, howLong);
    }
}
