package edu.lehigh.cse.lol;

// TODO: comments, and clean up naming, verify return values (i.e., false)

// TODO: Redesign so that we don't have to scroll, and so that we don't have to rely on the back button

// TODO: Provide configurable configuration?

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Chooser implements Screen
{
    // for managing the camera...

    // TODO: this first vector is redundant with touchVec
    final Vector3 curr  = new Vector3();

    final Vector3 last  = new Vector3(-1, -1, -1);

    final Vector3 delta = new Vector3();

    /**
     * A helper class for tracking where the buttons are, since we don't have an
     * easy way to know which lines are touched.
     * 
     * TODO: replace with just rectangles in an indexed array...
     */
    class LevelSprite
    {
        Rectangle r;

        String    t;

        int       l;

        LevelSprite(int x, int y, int w, int h, int level)
        {
            r = new Rectangle(x, y, w, h);
            l = level;
            t = "" + l;
        }
    }

    /**
     * All the level boxes we drew
     */
    LevelSprite[]      levels;

    /**
     * Since we're going to create other screens via this screen, we need a
     * reference to the game...
     */
    LOL                _game;

    /**
     * The camera we will use
     */
    OrthographicCamera _camera;

    /**
     * For rendering
     */
    SpriteBatch        _batcher;

    BitmapFont         _font;

    ShapeRenderer      _srend;

    // TODO: externalize these constants?
    static final int   bWidth  = 60;

    static final int   bHeight = 60;

    static final int   hGutter = 15;

    static final int   vGutter = 15;

    float              cameraCapY;

    public Chooser(LOL game)
    {
        // save a reference to the game
        _game = game;

        int numLevels = _game._config.getNumLevels();

        
        levels = new LevelSprite[numLevels];


        // figure out number of rows and columns...
        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();

        int vpad = camHeight;

        
        // we want to have gutter, box, gutter, box, ..., where the last box (+
        // margin) is scroll space
        int columns = camWidth / (hGutter + bWidth) - 1;
        int rows = numLevels / columns + ((numLevels % columns > 0) ? 1 : 0);

        // now make data for tracking the boxes
        for (int i = 0; i < numLevels; ++i) {
            int mycol = i % columns;
            int myrow = rows - i / columns - 1;
            levels[i] = new LevelSprite(hGutter + mycol * (bWidth + hGutter), vGutter + myrow * (bHeight + vGutter) + vpad,
                    bWidth, bHeight, 1 + i);
        }

        // figure out the boundary for the camera
        cameraCapY = levels[0].r.y + bHeight - camHeight / 2 + vGutter;
        Gdx.app.log("cap", ""+cameraCapY);
        // configure the camera
        _camera = new OrthographicCamera(camWidth, camHeight);
        _camera.position.set(camWidth / 2, cameraCapY, 0);

        // create a font
        _font = Media.getFont("arial.ttf", 30);

        // and our renderers
        _batcher = new SpriteBatch();
        _srend = new ShapeRenderer();
    }

    @Override
    public void render(float delta)
    {
        manageTouches();

        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();

        // keep camera in foreground layer bounds
        _camera.position.x = camWidth / 2;
        if (_camera.position.y < camHeight / 2)
            _camera.position.y = camHeight / 2;
        else if (_camera.position.y > cameraCapY)
            _camera.position.y = cameraCapY;
        _camera.update();

        // clear the screen
        GLCommon gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw squares...
        _srend.setProjectionMatrix(_camera.combined);
        _srend.begin(ShapeType.Line);
        _srend.setColor(Color.BLUE);
        for (LevelSprite ls : levels) {
            _srend.rect(ls.r.x, ls.r.y, ls.r.width, ls.r.height);
            _srend.rect(ls.r.x + 1, ls.r.y + 1, ls.r.width - 2, ls.r.height - 2);
        }
        _srend.end();

        _srend.begin(ShapeType.Filled);
        _srend.setColor(.4f, .4f, .4f, 0.9f);
        for (LevelSprite ls : levels) {
            if (ls.l > LOL._game._unlockLevel && !_game._config.getDeveloperUnlock()) {
                _srend.rect(ls.r.x + 2, ls.r.y + 2, ls.r.width - 4, ls.r.height - 4);
            }
        }
        _srend.end();

        _batcher.setProjectionMatrix(_camera.combined);
        _batcher.begin();
        for (LevelSprite ls : levels) {
            float x = _font.getBounds(ls.t).width;
            float y = _font.getBounds(ls.t).height;
            _font.draw(_batcher, ls.t, ls.r.x + bWidth / 2 - x / 2, ls.r.y + bHeight - y);
        }
        _batcher.end();
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
        // TODO Auto-generated method stub
    }

    // Here's a quick and dirty way to manage multitouch via polling
    boolean[] lastTouches = new boolean[4];

    void manageTouches()
    {
        // poll for touches
        // assume no more than 4 simultaneous touches
        boolean[] touchStates = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            touchStates[i] = Gdx.input.isTouched(i);
            float x = Gdx.input.getX(i);
            float y = Gdx.input.getY(i);
            if (touchStates[i] && lastTouches[i]) {
                touchDragged((int) x, (int) y, i);
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

    public boolean touchDown(int x, int y, int pointer, int newParam)
    {
        // translate the touch into _touchVec
        _camera.unproject(curr.set(x, y, 0));
        for (LevelSprite ls : levels) {
            if (ls.l <= LOL._game._unlockLevel || _game._config.getDeveloperUnlock()) {
                if (ls.r.contains(curr.x, curr.y)) {
                    _game.doPlayLevel(ls.l);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer)
    {
        _camera.unproject(curr.set(x, y, 0));
        if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
            _camera.unproject(delta.set(last.x, last.y, 0));
            delta.sub(curr);
            _camera.position.add(delta.x, delta.y, 0);
        }
        last.set(x, y, 0);
        return false;
    }

    public boolean touchUp(int x, int y, int pointer, int button)
    {
        // clear drag event
        last.set(-1, -1, -1);
        return false;
    }
}
