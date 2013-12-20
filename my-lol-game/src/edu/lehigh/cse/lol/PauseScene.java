
package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class PauseScene {
    ArrayList<Renderable> _sprites = new ArrayList<Renderable>();

    boolean _visible = true;

    Rectangle _pauseRectangle;
    
    /**
     * For handling touches
     */
    private Vector3 _v = new Vector3();
    
    private static PauseScene getCurrPauseScene() {
        PauseScene ps = Level._currLevel._pauseScene;
        if (ps != null)
            return ps;
        ps = new PauseScene();
        Level._currLevel.suspendTouch();
        Level._currLevel._pauseScene = ps;
        return ps;
    }

    
    public static void addText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        getCurrPauseScene()._sprites.add(Util.makeText(x, y, text, red, green, blue,
                fontName, size));
    }

    public static void addCenteredText(String text, int red, int green, int blue, String fontName,
            int size) {
        getCurrPauseScene()._sprites.add(Util.makeCenteredText(text, red, green, blue,
                fontName, size));
    }

    public static void addImage(String imgName, int x, int y, int width, int height) {
        getCurrPauseScene()._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    public static void addBackButton(String imgName, int x, int y, int width, int height) {
        getCurrPauseScene()._pauseRectangle = new Rectangle(x, y, width, height);
        getCurrPauseScene()._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }
    
    boolean render(SpriteBatch _spriteRender) {
        // if the pop-up scene is not visible, do nothing
        if (!_visible)
            return false;
        // if there's a touch, return
        //
        // TODO: need to make this more nuanced if we want to add buttons...
        if (Gdx.input.justTouched()) {
            Level._currLevel._hudCam.unproject(_v.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (_pauseRectangle.contains(_v.x, _v.y)) {
                LOL._game.handleBack();
                _visible = false;
                return false;
            }
            _visible = false;
            return false;
        }
        // OK, we should render the scene...

        // clear screen and draw sprites... we can use hudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        _spriteRender.setProjectionMatrix(Level._currLevel._hudCam.combined);
        _spriteRender.begin();
        for (Renderable r : _sprites)
            r.render(_spriteRender, 0);
        _spriteRender.end();
        return true;
    }
}
