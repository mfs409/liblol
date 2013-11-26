package edu.lehigh.cse.ale;

// TODO: this is going to be the hardest part...

// TODO: I don't understand something about cameras, because font rendering on
// Android differs from font rendering on Desktop

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

public class GameLevel implements MyScreen
{

    // for now, just one list of everything we need to render...
    public ArrayList<PhysicsSprite> _sprites;

    public ArrayList<PhysicsSprite> _deletions;

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
        _deletions = new ArrayList<PhysicsSprite>();

        // reset tilt control...
        Tilt.reset();
        Score.reset();
    }

    @Override
    public void render(float delta)
    {

        if (_gameOver) {

            // NB: if the game is over, don't advance the physics world!
            // next we clear the color buffer and set the camera matrices
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            _hudCam.update();
            int camWidth = _game._config.getScreenWidth();
            int camHeight = _game._config.getScreenHeight();

            _spriteRender.setProjectionMatrix(_hudCam.combined);//.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            _spriteRender.begin();
            // TODO: there's something screwy when we run this on a phone... it
            // works fine on desktop though...
            BitmapFont f = Media.getFont("arial.ttf", 30);
            String msg = Level._textYouWon;
            float w = f.getBounds(msg).width;
            float h = f.getBounds(msg).height;
            // f.draw(_spriteRender, msg, Gdx.graphics.getWidth() / 2 - w / 2, Gdx.graphics.getHeight() / 2 + h / 2);
            f.draw(_spriteRender, msg, camWidth / 2 - w / 2, camHeight / 2 + h / 2);
            _spriteRender.end();
            return;
        }

        if (_showPopUp) {
            // next we clear the color buffer and set the camera matrices
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            _hudCam.update();

            _spriteRender.setProjectionMatrix(_hudCam.combined);//.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            _spriteRender.begin();
            doPopUp();
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

        // handle deletions outside of the world step loop
        for (PhysicsSprite ps : _deletions) {
            ps.remove();
        }
        _deletions.clear();

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
         * _world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
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

    @Override
    public boolean touchDown(int x, int y, int pointer, int newParam)
    {
        if (_gameOver) {
            _game.doPlayLevel(ALE._currLevel + 1);
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

    boolean _showPopUp;

    String  _popupText;

    float   _popupRed;

    float   _popupGreen;

    float   _popupBlue;

    int     _popupSize;

    void setPopUp(String msg, int red, int green, int blue, int size)
    {
        _popupText = msg;
        _popupRed = red;
        _popupGreen = green;
        _popupBlue = blue;
        _popupRed /= 256;
        _popupGreen /= 256;
        _popupBlue /= 256;
        _popupSize = size;
        _showPopUp = true;
    }

    TextureRegion _popUpImgTr;

    float         _popUpImgX;

    float         _popUpImgY;

    float         _popUpImgW;

    float         _popUpImgH;

    void setPopUpImage(TextureRegion tr, float x, float y, float width, float height)
    {
        _popUpImgTr = tr;
        _popUpImgX = x;
        _popUpImgY = y;
        _popUpImgW = width;
        _popUpImgH = height;
        _showPopUp = true;
    }

    void doPopUp()
    {
        if (_popUpImgTr != null) {
            // TODO: width and height are a problem here...
            _spriteRender.draw(_popUpImgTr, _popUpImgX, _popUpImgY, 0, 0, _popUpImgW, _popUpImgH, 1, 1, 0);
        }
        // TODO: there's something screwy when we run this on a phone... it
        // works fine on desktop though...
        if (_popupText != null) {
            int camWidth = _game._config.getScreenWidth();
            int camHeight = _game._config.getScreenHeight();

            BitmapFont f = Media.getFont("arial.ttf", _popupSize);
            String msg = _popupText;
            float w = f.getMultiLineBounds(msg).width;
            float h = f.getMultiLineBounds(msg).height;
            f.setColor(_popupRed, _popupGreen, _popupBlue, 1);
            f.drawMultiLine(_spriteRender, msg, camWidth / 2 - w / 2, camHeight / 2 + h
                    / 2);
        }
    }
}
