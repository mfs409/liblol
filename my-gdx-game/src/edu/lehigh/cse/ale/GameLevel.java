package edu.lehigh.cse.ale;

// TODO: this is going to be the hardest part...

// TODO: I don't understand something about cameras, because font rendering on
// Android differs from font rendering on Desktop

// TODO: should this be merged with Level? I'm inclined toward "no", simply
// because we don't want to expose all this in games

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

public class GameLevel implements MyScreen
{
    /**
     * A simple wrapper for when we want stuff to happen
     */
    abstract static class PendingEvent
    {
        abstract void go();
        boolean _done;
        Rectangle _range;
        boolean _onlyOnce;
        void disable() {_done = true;}
        void enable() {_done = false;}
    }
    
    // for now, just one list of everything we need to render...
    public ArrayList<PhysicsSprite> _sprites;

    public ArrayList<PendingEvent> _events;

    public ArrayList<PendingEvent> _controls;
    
    
    void addTouchEvent(float x, float y, float width,
            float height, boolean onlyOnce, PendingEvent action)
    {
        action._range = new Rectangle(x, y, width, height);
        action._onlyOnce = onlyOnce;
        action.enable();
        _controls.add(action);
    }
    
    /*
     * INTERNAL CLASSES
     */

    /**
     * these are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you can get the number of
     * enemies down to 0
     */
    enum VictoryType
    {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

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
    private OrthographicCamera _gameCam;

    private OrthographicCamera _hudCam;

    // box2d debug renderer
    private Box2DDebugRenderer _debugRender;

    // a spritebatch and a font for text rendering and a Texture to draw our
    // boxes
    private SpriteBatch        _spriteRender;

    // box2d world
    public World               _world;

    boolean                    _gameOver;

    static GameLevel           _currLevel;

    ALE                        _game;

    public GameLevel(int width, int height, ALE game)
    {
        _game = game;
        _currLevel = this;
        // setup the camera. In Box2D we operate on a
        // meter scale, pixels won't do it. So we use
        // an orthographic camera with a viewport of
        // 48 meters in width and 32 meters in height.
        // We also position the camera so that it
        // looks at (0,16) (that's where the middle of the
        // screen will be located).
        _gameCam = new OrthographicCamera(width, height);
        _gameCam.position.set(width / 2, height / 2, 0);

        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();
        _hudCam = new OrthographicCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // next we create the box2d debug renderer
        _debugRender = new Box2DDebugRenderer();

        // next we create a SpriteBatch and a font
        _spriteRender = new SpriteBatch();

        // A place for everything we draw...
        _sprites = new ArrayList<PhysicsSprite>();
        _events = new ArrayList<PendingEvent>();
        _controls = new ArrayList<PendingEvent>();
        
        // reset tilt control...
        Tilt.reset();
        
        // reset scores
        Score.reset();        
    }

    @Override
    public void render(float delta)
    {
        /*
        if (_gameOver) {
            // NB: if the game is over, don't advance the physics world!
            // next we clear the color buffer and set the camera matrices
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            _hudCam.update();
            int camWidth = _game._config.getScreenWidth();
            int camHeight = _game._config.getScreenHeight();

            _spriteRender.setProjectionMatrix(_hudCam.combined);// .getProjectionMatrix().setToOrtho2D(0,
                                                                // 0,
                                                                // Gdx.graphics.getWidth(),
                                                                // Gdx.graphics.getHeight());
            _spriteRender.begin();
            BitmapFont f = Media.getFont("arial.ttf", 30);
            String msg = Level._textYouWon;
            float w = f.getBounds(msg).width;
            float h = f.getBounds(msg).height;
            f.draw(_spriteRender, msg, camWidth / 2 - w / 2, camHeight / 2 + h / 2);
            _spriteRender.end();
            return;
        }
    */
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
        _gameCam.update();

        // next we render each box via the SpriteBatch. for this we have to set
        // the projection matrix of the spritebatch to the camera's combined
        // matrix. This will make the spritebatch work in world coordinates

        _spriteRender.getProjectionMatrix().set(_gameCam.combined);
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
        _debugRender.render(_world, _gameCam.combined);

        // TODO: this is the render loop in Level1:
        /*
         * GLCommon gl = Gdx.gl;
         * 
         * gl.glClearColor(0, 0, 0, 1);
         * 
         * gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
         * 
         * _camera.update();
         * 
         * _debugRenderer.render(_world, _camera.combined);
         * 
         * _world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS,
         * BOX_POSITION_ITERATIONS);
         */

    }

    @Override
    public void resize(int width, int height)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void show()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose()
    {
    }

    private Vector3 _touchVec = new Vector3();
    @Override
    public boolean touchDown(int x, int y, int pointer, int newParam)
    {
        // TODO: factor this out correctly
        if (_gameOver) {
            _game.doPlayLevel(ALE._currLevel + 1);
        }
        for (PendingEvent pe : _controls) {
            if (!pe._done) {
                _hudCam.unproject(_touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                if (pe._range.contains(_touchVec.x, _touchVec.y)) {
                    if (pe._onlyOnce)
                        pe.disable();
                    pe.go();
                }
            }
        }
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
