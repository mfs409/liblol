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

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import edu.lehigh.cse.lol.Lol.GestureScreen;

/**
 * HelpLevel provides an interface for drawing to the help screens of a game
 */
public class HelpLevel implements GestureScreen {

    /**
     * The background color of the help level
     */
    private final Color mColor = new Color();

    /**
     * All the sprites that need to be drawn
     */
    private final ArrayList<Lol.Renderable> mSprites = new ArrayList<Lol.Renderable>();

    /**
     * The camera to use when drawing
     */
    private final OrthographicCamera mHelpCam;

    /**
     * The spritebatch to use when drawing
     */
    private final SpriteBatch mSb;

    /**
     * In LOL, we avoid having the game designer construct objects. To that end,
     * the HelpLevel is accessed through a singleton.
     */
    static HelpLevel sCurrentLevel;

    /**
     * When the game designer creates a help level, she uses configure, which
     * calls this to create the internal context
     */
    private HelpLevel() {
        // save the static context
        sCurrentLevel = this;

        // set up the camera
        int camWidth = Lol.sGame.mConfig.getScreenWidth();
        int camHeight = Lol.sGame.mConfig.getScreenHeight();
        mHelpCam = new OrthographicCamera(camWidth, camHeight);
        mHelpCam.position.set(camWidth / 2, camHeight / 2, 0);

        // set up the renderer
        mSb = new SpriteBatch();
    }

    /**
     * The main render loop for Help Levels. There's nothing fancy here
     * 
     * @param delta
     *            The time that has transpired since the last render
     */
    @Override
    public void render(float delta) {
        // Poll for a new touch (down-press)
        // On down-press, either advance to the next help scene, or return to
        // the splash screen
        if (Gdx.input.justTouched()) {
            if (Lol.sGame.mCurrHelpNum < Lol.sGame.mConfig.getNumHelpScenes()) {
                Lol.sGame.mCurrHelpNum++;
                Lol.sGame.doHelpLevel(Lol.sGame.mCurrHelpNum);
                return;
            }
            Lol.sGame.doSplash();
            return;
        }

        // render all sprites
        Gdx.gl.glClearColor(mColor.r, mColor.g, mColor.b, mColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mHelpCam.update();
        mSb.setProjectionMatrix(mHelpCam.combined);
        mSb.begin();
        for (Lol.Renderable c : mSprites)
            c.render(mSb, 0);
        mSb.end();
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Configure a help level by setting its background color
     * 
     * @param red
     *            The red component of the background color (0-255)
     * @param green
     *            The green component of the background color (0-255)
     * @param blue
     *            The blue component of the background color (0-255)
     */
    public static void configure(int red, int green, int blue) {
        sCurrentLevel = new HelpLevel();
        sCurrentLevel.mColor.r = ((float) red) / 256;
        sCurrentLevel.mColor.g = ((float) green) / 256;
        sCurrentLevel.mColor.b = ((float) blue) / 256;
    }

    /**
     * Draw a picture on the current help scene Note: the order in which this is
     * called relative to other entities will determine whether they go under or
     * over this picture.
     * 
     * @param x
     *            X coordinate of bottom left corner
     * @param y
     *            Y coordinate of bottom left corner
     * @param width
     *            Width of the picture
     * @param height
     *            Height of this picture
     * @param imgName
     *            Name of the picture to display
     */
    public static void drawPicture(final int x, final int y, final int width,
            final int height, String imgName) {
        // set up the image to display
        sCurrentLevel.mSprites.add(Util.makePicture(x, y, width, height,
                imgName));
    }

    /**
     * Print a message on the current help scene. This version of the addText
     * method uses the default font.
     * 
     * @param x
     *            X coordinate of text
     * @param y
     *            Y coordinate of text
     * @param message
     *            The message to display
     */
    static public void drawText(int x, int y, String message) {
        sCurrentLevel.mSprites.add(Util.makeText(x, y, message,
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontSize()));
    }

    /**
     * Print a message on the current help scene. This version of the addText
     * method allows the programmer to specify the appearance of the font
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param message
     *            The message to display
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param fontname
     *            The name of the font file to use
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    static public void drawText(final int x, final int y, final String message,
            final int red, final int green, final int blue, String fontName,
            int size) {
        sCurrentLevel.mSprites.add(Util.makeText(x, y, message, red, green,
                blue, fontName, size));
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
            Vector2 pointer1, Vector2 pointer2) {
        // TODO Auto-generated method stub
        return false;
    }
}
