/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

// TODO: verify return values (i.e., false)

// TODO: Redesign so that we don't have to scroll, and so that we don't have to rely on the back button

// TODO: Provide configurable configuration?

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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

public class Chooser extends ScreenAdapter {
    final Vector3 curr = new Vector3();

    final Vector3 last = new Vector3(-1, -1, -1);

    final Vector3 delta = new Vector3();

    /**
     * A helper class for tracking where the buttons are, since we don't have an
     * easy way to know which lines are touched. TODO: replace with just
     * rectangles in an indexed array...
     */
    class LevelSprite {
        Rectangle r;

        String t;

        int l;

        LevelSprite(int x, int y, int w, int h, int level) {
            r = new Rectangle(x, y, w, h);
            l = level;
            t = "" + l;
        }
    }

    /**
     * All the level boxes we drew
     */
    LevelSprite[] levels;

    /**
     * The camera we will use
     */
    OrthographicCamera mCamera;

    /**
     * For rendering
     */
    SpriteBatch mSpriteBatch;

    BitmapFont mFont;

    ShapeRenderer mShapeRender;

    // TODO: externalize these constants?
    static final int bWidth = 60;

    static final int bHeight = 60;

    static final int hGutter = 15;

    static final int vGutter = 15;

    float cameraCapY;

    public Chooser(LOL game) {
        int numLevels = LOL.sGame.mConfig.getNumLevels();

        levels = new LevelSprite[numLevels];

        // figure out number of rows and columns...
        int camWidth = LOL.sGame.mConfig.getScreenWidth();
        int camHeight = LOL.sGame.mConfig.getScreenHeight();

        int vpad = camHeight;

        // we want to have gutter, box, gutter, box, ..., where the last box (+
        // margin) is scroll space
        int columns = camWidth / (hGutter + bWidth) - 1;
        int rows = numLevels / columns + ((numLevels % columns > 0) ? 1 : 0);

        // now make data for tracking the boxes
        for (int i = 0; i < numLevels; ++i) {
            int mycol = i % columns;
            int myrow = rows - i / columns - 1;
            levels[i] = new LevelSprite(hGutter + mycol * (bWidth + hGutter), vGutter + myrow
                    * (bHeight + vGutter) + vpad, bWidth, bHeight, 1 + i);
        }

        // figure out the boundary for the camera
        cameraCapY = levels[0].r.y + bHeight - camHeight / 2 + vGutter;
        // configure the camera
        mCamera = new OrthographicCamera(camWidth, camHeight);
        mCamera.position.set(camWidth / 2, cameraCapY, 0);

        // create a font
        mFont = Media.getFont(LOL.sGame.mConfig.getDefaultFontFace(), 30);

        // and our renderers
        mSpriteBatch = new SpriteBatch();
        mShapeRender = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        manageTouches();

        int camWidth = LOL.sGame.mConfig.getScreenWidth();
        int camHeight = LOL.sGame.mConfig.getScreenHeight();

        // keep camera in foreground layer bounds
        mCamera.position.x = camWidth / 2;
        if (mCamera.position.y < camHeight / 2)
            mCamera.position.y = camHeight / 2;
        else if (mCamera.position.y > cameraCapY)
            mCamera.position.y = cameraCapY;
        mCamera.update();

        // clear the screen
        GLCommon gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw squares...
        mShapeRender.setProjectionMatrix(mCamera.combined);
        mShapeRender.begin(ShapeType.Line);
        mShapeRender.setColor(Color.BLUE);
        for (LevelSprite ls : levels) {
            mShapeRender.rect(ls.r.x, ls.r.y, ls.r.width, ls.r.height);
            mShapeRender.rect(ls.r.x + 1, ls.r.y + 1, ls.r.width - 2, ls.r.height - 2);
        }
        mShapeRender.end();

        mShapeRender.begin(ShapeType.Filled);
        mShapeRender.setColor(.4f, .4f, .4f, 0.9f);
        int unlocked = LOL.sGame.readUnlocked();
        for (LevelSprite ls : levels) {
            if (ls.l > unlocked && !LOL.sGame.mConfig.getUnlockMode()) {
                mShapeRender.rect(ls.r.x + 2, ls.r.y + 2, ls.r.width - 4, ls.r.height - 4);
            }
        }
        mShapeRender.end();

        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();
        for (LevelSprite ls : levels) {
            float x = mFont.getBounds(ls.t).width;
            float y = mFont.getBounds(ls.t).height;
            mFont.draw(mSpriteBatch, ls.t, ls.r.x + bWidth / 2 - x / 2, ls.r.y + bHeight - y);
        }
        mSpriteBatch.end();
    }

    // Here's a quick and dirty way to manage multitouch via polling
    boolean[] lastTouches = new boolean[4];

    void manageTouches() {
        // poll for touches
        // assume no more than 4 simultaneous touches
        boolean[] touchStates = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            touchStates[i] = Gdx.input.isTouched(i);
            float x = Gdx.input.getX(i);
            float y = Gdx.input.getY(i);
            if (touchStates[i] && lastTouches[i]) {
                touchDragged((int)x, (int)y, i);
            } else if (touchStates[i] && !lastTouches[i]) {
                touchDown((int)x, (int)y, i, 0);
            } else if (!touchStates[i] && lastTouches[i]) {
                touchUp((int)x, (int)y, i, 0);
            }
            lastTouches[i] = touchStates[i];
        }
    }

    public boolean touchDown(int x, int y, int pointer, int newParam) {
        mCamera.unproject(curr.set(x, y, 0));
        int unlocked = LOL.sGame.readUnlocked();
        for (LevelSprite ls : levels) {
            if (ls.l <= unlocked || LOL.sGame.mConfig.getUnlockMode()) {
                if (ls.r.contains(curr.x, curr.y)) {
                    LOL.sGame.doPlayLevel(ls.l);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer) {
        mCamera.unproject(curr.set(x, y, 0));
        if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
            mCamera.unproject(delta.set(last.x, last.y, 0));
            delta.sub(curr);
            mCamera.position.add(delta.x, delta.y, 0);
        }
        last.set(x, y, 0);
        return false;
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        // clear drag event
        last.set(-1, -1, -1);
        return false;
    }
}
