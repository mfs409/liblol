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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * The splash screen is the first thing the user sees when playing the game. It
 * has buttons for playing, getting help, and quitting. It is configured through
 * a SplashConfiguration object.
 */
public class Splash extends ScreenAdapter implements GestureListener {

    /**
     * A static reference to the current splash screen, so that we can use a
     * static context to configure a Splash screen from the game developer's
     * main file.
     */
    private static Splash sCurrent;

    /**
     * The camera for displaying the scene
     */
    private final OrthographicCamera mCamera;

    /**
     * A rectangle for tracking the location of the play button
     */
    private Rectangle mPlay;

    /**
     * A rectangle for tracking the location of the help button
     */
    private Rectangle mHelp;

    /**
     * A rectangle for tracking the location of the quit button
     */
    private Rectangle mQuit;

    /**
     * For handling touches
     */
    private final Vector3 mV = new Vector3();

    /**
     * For rendering
     */
    private final SpriteBatch mSpriteBatch = new SpriteBatch();

    /**
     * For debug rendering
     */
    private final ShapeRenderer mShapeRender = new ShapeRenderer();

    /**
     * The image to display
     */
    private TextureRegion[] mImage;

    /**
     * The music to play
     */
    Music mMusic;

    /**
     * Track if the music is actually playing
     */
    boolean mMusicPlaying;

    /**
     * Basic configuration: get the image and music, configure the locations of
     * the play/help/quit buttons
     */
    Splash() {
        // configure the camera, center it on the screen
        int w = Lol.sGame.mConfig.getScreenWidth();
        int h = Lol.sGame.mConfig.getScreenHeight();
        mCamera = new OrthographicCamera(w, h);
        mCamera.position.set(w / 2, h / 2, 0);
        // save a reference
        sCurrent = this;
        // call user code to configure the objects
        Lol.sGame.configureSplash();
        // Subscribe to touch gestures
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    /**
     * Start the music if it's not already playing
     */
    void playMusic() {
        if (!mMusicPlaying && mMusic != null) {
            mMusicPlaying = true;
            mMusic.play();
        }
    }

    /**
     * Pause the music if it's playing
     */
    void pauseMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.pause();
        }
    }

    /**
     * Stop the music if it's playing
     */
    void stopMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.stop();
        }
    }

    /*
     * SCREEN OVERRIDES
     */
    
    /**
     * Draw the splash screen
     * 
     * @param delta
     *            time since the screen was last displayed
     */
    @Override
    public void render(float delta) {
        // make sure the music is playing
        playMusic();

        // NB: we no longer poll for screen events... the TAP gesture does
        // everything we need.

        // now draw the screen...
        GL20 gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mCamera.update();
        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();
        mSpriteBatch.enableBlending();
        if (mImage != null)
            mSpriteBatch.draw(mImage[0], 0, 0,
                    Lol.sGame.mConfig.getScreenWidth(),
                    Lol.sGame.mConfig.getScreenHeight());
        mSpriteBatch.end();

        // DEBUG: show where the buttons' boxes are
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            mShapeRender.setProjectionMatrix(mCamera.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            if (mPlay != null)
                mShapeRender.rect(mPlay.x, mPlay.y, mPlay.width, mPlay.height);
            if (mHelp != null)
                mShapeRender.rect(mHelp.x, mHelp.y, mHelp.width, mHelp.height);
            if (mQuit != null)
                mShapeRender.rect(mQuit.x, mQuit.y, mQuit.width, mQuit.height);
            mShapeRender.end();
        }
    }

    /**
     * When this scene goes away, make sure the music gets turned off
     */
    @Override
    public void dispose() {
        stopMusic();
    }

    /**
     * When this scene goes away, make sure the music gets turned off
     */
    @Override
    public void hide() {
        pauseMusic();
    }

    /*
     * GESTURE OVERRIDES
     */
    
    /**
     * Process a TAP event. A TAP is a down-then-up gesture.
     * 
     * @param x
     *            The x coordinate on the screen for where the touch happened
     * @param y
     *            The y coordinate on the screen for where the touch happened
     * @param count
     *            normally 1, but a double click leads to a 2
     * @param button
     *            Corresponds to left and right mouse buttons
     */
    @Override
    public boolean tap(float x, float y, int count, int button) {
        // translate the touch into camera coordinates
        mCamera.unproject(mV.set(x, y, 0));
        // DEBUG: print the location of the touch... this is really useful
        // when trying to figure out the coordinates of the rectangles
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            Gdx.app.log("tap", "(" + mV.x + ", " + mV.y + ")");
        }
        // check if the touch was inside one of our buttons, and act
        // accordingly
        if (mQuit != null && mQuit.contains(mV.x, mV.y)) {
            stopMusic();
            Lol.sGame.doQuit();
        }
        if (mPlay != null && mPlay.contains(mV.x, mV.y)) {
            stopMusic();
            Lol.sGame.doChooser();
        }
        if (mHelp != null && mHelp.contains(mV.x, mV.y)) {
            stopMusic();
            Lol.sGame.doHelpLevel(1);
        }
        // We handled the tap, so return true...
        return true;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    /**
     * Not used by Splash Screen 
     */
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
            Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
    
    /*
     * PUBLIC INTERFACE
     */

    /**
     * Describe the coordinates of the Play button, so that clicks to the
     * correct region of the splash screen will cause the chooser to be drawn or
     * the only level of the game to start playing
     * 
     * @param x
     *            The X coordinate of the bottom left corner of the button, in
     *            pixels
     * @param y
     *            The Y coordinate of the bottom left corner of the button, in
     *            pixels
     * @param width
     *            The width of the button, in pixels
     * @param height
     *            The height of the button, in pixels
     */
    public static void drawPlayButton(int x, int y, int width, int height) {
        sCurrent.mPlay = new Rectangle(x, y, width, height);
    }

    /**
     * Describe the coordinates of the Help button, so that clicks to the
     * correct region of the splash screen will cause the first help scene to be
     * drawn
     * 
     * @param x
     *            The X coordinate of the bottom left corner of the button, in
     *            pixels
     * @param y
     *            The Y coordinate of the bottom left corner of the button, in
     *            pixels
     * @param width
     *            The width of the button, in pixels
     * @param height
     *            The height of the button, in pixels
     */
    public static void drawHelpButton(int x, int y, int width, int height) {
        sCurrent.mHelp = new Rectangle(x, y, width, height);
    }

    /**
     * Describe the coordinates of the Quit button, so that clicks to the
     * correct region of the splash screen will cause the app to terminate
     * 
     * @param x
     *            The X coordinate of the bottom left corner of the button, in
     *            pixels
     * @param y
     *            The Y coordinate of the bottom left corner of the button, in
     *            pixels
     * @param width
     *            The width of the button, in pixels
     * @param height
     *            The height of the button, in pixels
     */
    public static void drawQuitButton(int x, int y, int width, int height) {
        sCurrent.mQuit = new Rectangle(x, y, width, height);
    }

    /**
     * Configure the music to play when the splash screen is showing
     * 
     * @param soundName
     *            The music file name. Be sure that it is registered!
     */
    public static void setMusic(String soundName) {
        sCurrent.mMusic = Media.getMusic(soundName);
    }

    /**
     * Configure the image to display as the background of the splash screen. It
     * should include your game name and text regions for Play and Quit, as well
     * as optional Help.
     * 
     * @param imgName
     *            The image file name. Be sure that it is registered!
     */
    public static void setBackground(String imgName) {
        sCurrent.mImage = Media.getImage(imgName);
    }
}
