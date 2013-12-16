package edu.lehigh.cse.lol;

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

// TODO: verify true/false returns from all things called from this... I don't think all our events are being chained
// correctly

// TODO: does zoom work with parallax?

// TODO: zoom doesn't work right with bounds of big screens and chase heroes

// TODO: hud onhold is untested

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Controls.Control;
import edu.lehigh.cse.lol.Util.Action;
import edu.lehigh.cse.lol.Util.ParallaxCamera;
import edu.lehigh.cse.lol.Util.Renderable;

/**
 * A Level is a playable portion of the game. Levels can be infinite, or they can have an end goal.
 * 
 * Level has two components. One is the part that is visible to the game designer, which involves some
 * limited control over the camera and music, and the ability to request that custom code run after a fixed amount of
 * time. These timers can also be attached to a specific enemy, if desired.
 * 
 * Internally, Level is responsible for managing a set of cameras used to display everything that appears on the screen.
 * It is also responsible for keeping track of everything on the screen (game entities and Controls).
 */
public class Level extends ScreenAdapter
{
    /*
     * FIELDS FOR MANAGING GAME STATE
     */

    /**
     * The music, if any
     */
    Music                      _music;

    /**
     * Whether the music is playing or not
     */
    boolean                    _musicPlaying  = false;

    /**
     * A reference to the score object, for tracking winning and losing
     */
    Score                      _score         = new Score();

    /**
     * A reference to the tilt object, for managing how tilts are handled
     */
    Tilt                       _tilt          = new Tilt();

    /**
     * The physics world in which all entities interact
     */
    World                      _world;

    /**
     * The set of Parallax backgrounds
     */
    Background                 _background    = new Background();

    /**
     * The scene to show when the level is created (if any)
     */
    PreScene                   _preScene;

    /**
     * The scene to show when the level is won or lost
     */
    PostScene                  _postScene     = new PostScene();

    /**
     * The scene to show when the level is paused (if any)
     */
    PauseScene                 _pauseScene;

    /**
     * Track if we are in scribble mode or not
     */
    boolean                    _scribbleMode  = false;

    /*
     * COLLECTIONS OF DRAWABLE ENTITIES/PICTURES/TEXT AND CONTROLS
     */

    /**
     * The level -2 sprites
     */
    ArrayList<Renderable>      _pix_minus_two = new ArrayList<Renderable>();

    /**
     * The level 0 sprites
     */
    ArrayList<Renderable>      _sprites       = new ArrayList<Renderable>();

    /**
     * The controls / heads-up-display
     */
    ArrayList<Control>         _controls      = new ArrayList<Control>();

    /*
     * COLLECTIONS OF EVENTS THAT MUST BE PROCESSED
     */

    /**
     * Events that get processed on the next render, then discarded
     */
    ArrayList<Action>          _oneTimeEvents = new ArrayList<Action>();

    /**
     * Events that get processed on every render
     */
    ArrayList<Action>          _repeatEvents  = new ArrayList<Action>();

    /*
     * FIELDS FOR CAMERAS AND RENDERING
     */

    /**
     * This camera is for drawing entities that exist in the physics world
     */
    OrthographicCamera         _gameCam;

    /**
     * This camera is for drawing controls that sit above the world
     */
    OrthographicCamera         _hudCam;

    /**
     * This camera is for drawing parallax backgrounds that go behind the world
     */
    ParallaxCamera             _bgCam;

    /**
     * This is the sprite that the camera chases
     */
    PhysicsSprite              _chase;

    /**
     * The X bound of the camera
     */
    int                        _camBoundX;

    /**
     * The Y bound of the camera
     */
    int                        _camBoundY;

    /**
     * The debug renderer, for printing circles and boxes for each entity
     */
    private Box2DDebugRenderer _debugRender   = new Box2DDebugRenderer();

    /**
     * The spritebatch for drawing all texture regions and fonts
     */
    private SpriteBatch        _spriteRender  = new SpriteBatch();

    /**
     * The debug shape renderer, for putting boxes around HUD entities
     */
    private ShapeRenderer      _shapeRender   = new ShapeRenderer();

    /*
     * FIELDS FOR MANAGING TOUCH
     */

    /**
     * We use this to avoid garbage collection when converting screen touches to camera coordinates
     */
    private Vector3            _touchVec      = new Vector3();

    /**
     * When there is a touch of an entity in the physics world, this is how we find it
     */
    PhysicsSprite              _hitSprite     = null;

    /**
     * This callback is used to get a touched entity from the physics world
     */
    QueryCallback              _callback;

    /**
     * Our polling-based multitouch uses this array to track the previous state of 4 fingers
     */
    boolean[]                  lastTouches    = new boolean[4];

    /**
     * The LOL interface requires that game designers don't have to construct Level manually. To make it work, we store
     * the current Level here
     */
    static Level               _currLevel;

    /**
     * Construct a level. This is mostly using defaults, so the main work is in camera setup
     * 
     * @param width
     *            The width of the level, in meters
     * @param height
     *            The height of the level, in meters
     */
    Level(int width, int height)
    {
        // save the singleton and camera bounds
        _currLevel = this;
        _camBoundX = width;
        _camBoundY = height;

        // set up the game camera, centered on the world
        _gameCam = new OrthographicCamera(LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO,
                LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO);
        _gameCam.position.set(LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2,
                LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2, 0);
        _gameCam.zoom = 1;

        // set up the heads-up display
        int camWidth = LOL._game._config.getScreenWidth();
        int camHeight = LOL._game._config.getScreenHeight();
        _hudCam = new OrthographicCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // the background camera is like the hudcam
        _bgCam = new ParallaxCamera(camWidth, camHeight);
        _bgCam.position.set(camWidth / 2, camHeight / 2, 0);

        // set up the callback for finding out who in the physics world was touched
        _callback = new QueryCallback()
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

        // When debug mode is on, print the frames per second
        if (LOL._game._config.showDebugBoxes())
            Controls.addFPS(400, 15, 200, 200, 100, 12);
    }

    /*
     * INTERNAL INTERFACE: MUSIC
     */

    /**
     * If the level has music attached to it, this starts playing it
     */
    void playMusic()
    {
        if (!_musicPlaying && _music != null) {
            _musicPlaying = true;
            _music.play();
        }
    }

    /**
     * If the level has music attached to it, this pauses it
     */
    void pauseMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.pause();
        }
    }

    /**
     * If the level has music attached to it, this stops it
     */
    void stopMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.stop();
        }
    }

    /*
     * INTERNAL INTERFACE: RENDERING AND CAMERAS
     */

    /**
     * This code is called every 1/45th of a second to update the game state and re-draw the screen
     */
    @Override
    public void render(float delta)
    {
        // Make sure the music is playing... Note that we start music before the PreScene shows
        playMusic();

        // Handle pauses due to pre, pause, or post scenes... Note that these handle their own screen touches
        if (_preScene != null && _preScene.render(_spriteRender))
            return;
        if (_pauseScene != null && _pauseScene.render(_spriteRender))
            return;
        if (_postScene != null && _postScene.render(_spriteRender))
            return;

        // check for any scene touches that should generate new events to process
        manageTouches();

        // handle accelerometer stuff... note that accelerometer is effectively disabled during a popup... we could
        // change that by moving this to the top, but that's probably not going to produce logical behavior
        Level._currLevel._tilt.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it seems like we should be using /delta/
        // instead of 1/45f
        _world.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement or screen touches
        for (Action pe : _oneTimeEvents)
            pe.go();
        _oneTimeEvents.clear();

        // handle repeat events
        for (Action pe : _repeatEvents)
            pe.go();

        // The world is now static for this time step... we can display it!

        // clear the screen
        Gdx.gl.glClearColor(_background._c.r, _background._c.g, _background._c.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // prepare the main camera... we do it here, so that the parallax code knows where to draw...
        adjustCamera();
        _gameCam.update();

        // draw parallax backgrounds
        _background.renderLayers(_spriteRender);

        // Render the entities in order from z=-2 through z=2
        _spriteRender.setProjectionMatrix(_gameCam.combined);
        _spriteRender.begin();
        for (Renderable r : _pix_minus_two)
            r.render(_spriteRender, delta);
        for (Renderable r : _sprites)
            r.render(_spriteRender, delta);
        _spriteRender.end();

        // DEBUG: draw outlines of physics entities
        if (LOL._game._config.showDebugBoxes())
            _debugRender.render(_world, _gameCam.combined);

        // draw Controls
        _hudCam.update();
        _spriteRender.setProjectionMatrix(_hudCam.combined);
        _spriteRender.begin();
        for (Control c : _controls)
            c.render(_spriteRender);
        _spriteRender.end();

        // DEBUG: render Controls' outlines
        if (LOL._game._config.showDebugBoxes()) {
            _shapeRender.setProjectionMatrix(_hudCam.combined);
            _shapeRender.begin(ShapeType.Line);
            _shapeRender.setColor(Color.RED);
            for (Control pe : _controls)
                if (pe._range != null)
                    _shapeRender.rect(pe._range.x, pe._range.y, pe._range.width, pe._range.height);
            _shapeRender.end();
        }
    }

    /**
     * Whenever we hide the level, be sure to turn off the music
     */
    @Override
    public void hide()
    {
        pauseMusic();
    }

    /**
     * Whenever we dispose of the level, be sure to turn off the music
     */
    @Override
    public void dispose()
    {
        stopMusic();
    }

    /**
     * If the camera is supposed to follow an Entity, this code will handle updating the camera accordingly
     */
    private void adjustCamera()
    {
        if (_chase == null)
            return;
        // figure out the entity's position
        float x = _chase._physBody.getWorldCenter().x + _chase._cameraOffset.x;
        float y = _chase._physBody.getWorldCenter().y + _chase._cameraOffset.y;
        // if x or y is too close to 0,0, stick with minimum acceptable values
        if (x < LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2)
            x = LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2;
        if (y < LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2)
            y = LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2;

        // if x or y is too close to MAX,MAX, stick with max acceptable values
        if (x > _camBoundX - LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2)
            x = _camBoundX - LOL._game._config.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2;
        if (y > _camBoundY - LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2)
            y = _camBoundY - LOL._game._config.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2;

        // update the camera position
        _gameCam.position.set(x, y, 0);
    }

    /*
     * INTERNAL INTERFACE: TOUCH CONTROLS
     */

    /**
     * LOL uses polling to detect multitouch, touchdown, touchup, and touchhold/touchdrag. This method, called from
     * render, will track up to 4 simultaneous touches and forward them for proper processing.
     */
    private void manageTouches()
    {
        // poll for touches... we assume no more than 4 simultaneous touches
        boolean[] touchStates = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            // we compare the current state to the prior state, to detect down, up, or move/hold. Note that we don't
            // distinguish between move and hold, but that's OK.
            touchStates[i] = Gdx.input.isTouched(i);
            float x = Gdx.input.getX(i);
            float y = Gdx.input.getY(i);
            // if there is a touch, call the appropriate method
            if (touchStates[i] && lastTouches[i])
                touchMove((int) x, (int) y);
            else if (touchStates[i] && !lastTouches[i])
                touchDown((int) x, (int) y);
            else if (!touchStates[i] && lastTouches[i])
                touchUp((int) x, (int) y);
            lastTouches[i] = touchStates[i];
        }
    }

    /**
     * When there is a down press, this code handles it
     * 
     * @param x
     *            The X location of the press, in screen coordinates
     * @param y
     *            The Y location of the press, in screen coordinates
     * @return
     */
    private void touchDown(int x, int y)
    {
        // check for HUD touch first...
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable && pe._range.contains(_touchVec.x, _touchVec.y)) {
                // now convert the touch to world coordinates and pass to the control (useful for vector throw)
                _gameCam.unproject(_touchVec.set(x, y, 0));
                pe.onDownPress(_touchVec);
                return;
            }
        }

        // check for sprite touch, by looking at gameCam coordinates... on touch, hitSprite will change
        _hitSprite = null;
        _gameCam.unproject(_touchVec.set(x, y, 0));
        _world.QueryAABB(_callback, _touchVec.x - 0.1f, _touchVec.y - 0.1f, _touchVec.x + 0.1f, _touchVec.y + 0.1f);
        if (_hitSprite != null) {
            _hitSprite.handleTouchDown(_touchVec.x, _touchVec.y);
            return;
        }

        // There are a variety of screen touch handlers we might need to invoke. They're all one-off calls from here...
        // it's not the best design, but it works for now

        // poke sprites
        if (PhysicsSprite._currentPokeSprite != null) {
            PhysicsSprite._currentPokeSprite.finishPoke(_touchVec.x, _touchVec.y);
        }
        // poke velocity
        else if (PhysicsSprite._pokeVelocityEntity != null) {
            PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, true, false, false);
        }
        // poke path
        else if (PhysicsSprite._pokePathEntity != null) {
            PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, true, false, false);
        }
        // deal with scribbles
        else if (Level._currLevel._scribbleMode) {
            if (Level._currLevel._scribbleMode) {
                _gameCam.unproject(_touchVec.set(x, y, 0));
                Obstacle.doScribbleDown(_touchVec.x, _touchVec.y);
                return;
            }
        }
    }

    /**
     * When a finger moves on the screen, this code handles it
     * 
     * @param x
     *            The X location of the press, in screen coordinates
     * @param y
     *            The Y location of the press, in screen coordinates
     * @return
     */
    private void touchMove(int x, int y)
    {
        // check for HUD touch first...
        // TODO: HUD onHold not tested!
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable && pe._range.contains(_touchVec.x, _touchVec.y)) {
                // now convert the touch to world coordinates and pass to the control (useful for vector throw)
                _gameCam.unproject(_touchVec.set(x, y, 0));
                pe.onHold(_touchVec);
                return;
            }
        }

        // We don't currently support Move within a Sprite, only on the screen. These screen handlers are all one-off
        // calls from here.
        _gameCam.unproject(_touchVec.set(x, y, 0));
        // deal with scribble?
        if (Level._currLevel._scribbleMode) {
            Obstacle.doScribbleDrag(_touchVec.x, _touchVec.y);
        }
        // deal with drag?
        else if (_hitSprite != null) {
            if (_hitSprite.handleTouchDrag(_touchVec.x, _touchVec.y))
                return;
        }
        // move a poke entity?
        else if (PhysicsSprite._pokePathEntity != null) {
            if (!PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, true, false))
                return;
        }
        // move a poke velocity entity?
        else if (PhysicsSprite._pokeVelocityEntity != null) {
            PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, false, true, false);
        }
    }

    /**
     * When a finger is removed from the screen, this code handles it
     * 
     * @param x
     *            The X location of the press, in screen coordinates
     * @param y
     *            The Y location of the press, in screen coordinates
     * @return
     */
    void touchUp(int x, int y)
    {
        // check for HUD touch first
        _hudCam.unproject(_touchVec.set(x, y, 0));
        for (Control pe : _controls) {
            if (pe._isTouchable) {
                if (pe._range.contains(_touchVec.x, _touchVec.y)) {
                    pe.onUpPress();
                    return;
                }
            }
        }

        // Up presses are not handled by entities, only by the screen, via a bunch of one-off handlers
        _gameCam.unproject(_touchVec.set(x, y, 0));
        // Deal with scribbles
        if (Level._currLevel._scribbleMode) {
            Obstacle.doScribbleUp();
            return;
        }
        // handle flicks
        if (PhysicsSprite._flickEntity != null) {
            PhysicsSprite.flickDone(_touchVec.x, _touchVec.y);
            return;
        }
        // Pokepath movement
        if (PhysicsSprite._pokePathEntity != null) {
            if (PhysicsSprite.finishPokePath(_touchVec.x, _touchVec.y, false, false, true))
                return;
        }
        // pokevelocity movement
        if (PhysicsSprite._pokeVelocityEntity != null) {
            if (PhysicsSprite.finishPokeVelocity(_touchVec.x, _touchVec.y, false, false, true))
                return;
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Create a new empty level, and configure its camera
     * 
     * @param width
     *            width of the camera
     * @param height
     *            height of the camera
     */
    public static void configure(int width, int height)
    {
        _currLevel = new Level(width, height);
    }

    /**
     * Identify the entity that the camera should try to keep on screen at all times
     * 
     * @param ps
     *            The entity the camera should chase
     */
    public static void setCameraChase(PhysicsSprite ps)
    {
        _currLevel._chase = ps;
    }

    /**
     * Set the background music for this level
     * 
     * @param musicName
     *            Name of the Music file to play
     */
    public static void setMusic(String musicName)
    {
        Music m = Media.getMusic(musicName);
        _currLevel._music = m;
    }

    /**
     * Specify that you want some code to run after a fixed amount of time passes.
     * 
     * @param timerId
     *            A (possibly) unique identifier for this timer
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
                if (!Level._currLevel._score._gameOver)
                    LOL._game.onTimeTrigger(timerId, LOL._game._currLevel);
            }
        }, howLong);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time passes, and that it should return a specific
     * enemy to the programmer's code
     * 
     * @param timerId
     *            A (possibly) unique identifier for this timer
     * @param howLong
     *            How long to wait before the timer code runs
     * @param enemy
     *            The enemy that should be passed along
     */
    public static void setEnemyTimerTrigger(final int timerId, float howLong, final Enemy enemy)
    {
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                if (!Level._currLevel._score._gameOver)
                    LOL._game.onEnemyTimeTrigger(timerId, LOL._game._currLevel, enemy);
            }
        }, howLong);
    }
}
