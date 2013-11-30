package edu.lehigh.cse.ale;

// TODO: this is going to be the hardest part...

// TODO: should this be merged with Level? I'm inclined toward "no", simply
// because we don't want to expose all this in games, but it sure does seem like we have lots of overlap

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

// TODO: do we want to fixed-step the physics world?

// STATUS: this will keep evolving as we keep making other levels work

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class GameLevel implements MyScreen
{
    /**
     * A simple wrapper for when we want stuff to happen
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

    // for now, just one list of everything we need to render...
    public ArrayList<PhysicsSprite> _sprites;

    public ArrayList<PendingEvent>  _events;

    public ArrayList<PendingEvent>  _controls;

    public ArrayList<PhysicsSprite> _routes;
    
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

    /**
     * Width of this level
     */
    int                        _width;

    /**
     * Height of this level
     */
    int                        _height;

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

    static GameLevel           _currLevel;

    ALE                        _game;

    int _camBoundX;
    int _camBoundY;
    
    public GameLevel(int width, int height, ALE game)
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
        _controls = new ArrayList<PendingEvent>();
        _routes = new ArrayList<PhysicsSprite>();

        // reset tilt control...
        Tilt.reset();

        // reset scores
        Score.reset();
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

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        adjustCamera();
        _gameCam.update();

        // next we render each box via the SpriteBatch. for this we have to set
        // the projection matrix of the spritebatch to the camera's combined
        // matrix. This will make the spritebatch work in world coordinates

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

        // next we use the debug renderer. Note that we simply apply the camera
        // again and then call the renderer. the camera.apply() call is actually
        // not needed as the opengl matrices are already set by the spritebatch
        // which in turn uses the camera matrices :)
        if (_game._config.showDebugBoxes())
            _debugRender.render(_world, _gameCam.combined);

        // render the hud
        _hudCam.update();
        _spriteRender.setProjectionMatrix(_hudCam.combined);
        _spriteRender.begin();

        for (PendingEvent pe : _controls) {
            if (pe.tr != null)
                _spriteRender.draw(pe.tr, pe._range.x, pe._range.y, 0,0, pe._range.width, pe._range.height, 1, 1, 0);
        }
        _spriteRender.end();

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

    @Override
    public boolean touchDown(int x, int y, int pointer, int newParam)
    {
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
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer)
    {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button)
    {
        return false;
    }
}
