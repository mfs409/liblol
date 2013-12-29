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
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * The splash screen is the first thing the user sees when playing the game. It
 * has buttons for playing, getting help, and quitting. It is configured through
 * a SplashConfiguration object.
 */
public class Splash extends ScreenAdapter {
    /**
     * The camera for displaying the scene
     */
    private final OrthographicCamera mCamera;

    /**
     * A rectangle for tracking the location of the play button
     */
    private final Rectangle mPlay;

    /**
     * A rectangle for tracking the location of the help button
     */
    private Rectangle mHelp;

    /**
     * A rectangle for tracking the location of the quit button
     */
    private final Rectangle mQuit;

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
    private final TextureRegion[] mImage;

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
    public Splash() {
        // configure the camera, center it on the screen
        int w = Lol.sGame.mConfig.getScreenWidth();
        int h = Lol.sGame.mConfig.getScreenHeight();
        mCamera = new OrthographicCamera(w, h);
        mCamera.position.set(w / 2, h / 2, 0);

        // set up the play, help, and quit buttons
        SplashConfiguration sc = Lol.sGame.mSplashConfig;
        mPlay = new Rectangle(sc.getPlayX(), sc.getPlayY(), sc.getPlayWidth(), sc.getPlayHeight());
        if (Lol.sGame.mConfig.getNumHelpScenes() > 0) {
            mHelp = new Rectangle(sc.getHelpX(), sc.getHelpY(), sc.getHelpWidth(),
                    sc.getHelpHeight());
        }
        mQuit = new Rectangle(sc.getQuitX(), sc.getQuitY(), sc.getQuitWidth(), sc.getQuitHeight());

        // get the background image and music
        mImage = Media.getImage(sc.getBackgroundImage());
        if (Lol.sGame.mSplashConfig.getMusic() != null)
            mMusic = Media.getMusic(sc.getMusic());
    }

    /**
     * Start the music if it's not already playing
     */
    public void playMusic() {
        if (!mMusicPlaying && mMusic != null) {
            mMusicPlaying = true;
            mMusic.play();
        }
    }

    /**
     * Pause the music if it's playing
     */
    public void pauseMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.pause();
        }
    }

    /**
     * Stop the music if it's playing
     */
    public void stopMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.stop();
        }
    }

    /**
     * Draw the splash screen
     * 
     * @param delta time since the screen was last displayed
     */
    @Override
    public void render(float delta) {
        // make sure the music is playing
        playMusic();

        // If there is a new down-touch, figure out if it was to a button
        if (Gdx.input.justTouched()) {
            // translate the touch into camera coordinates
            mCamera.unproject(mV.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            // DEBUG: print the location of the touch... this is really useful
            // when trying to figure out the coordinates of the rectangles
            if (Lol.sGame.mConfig.showDebugBoxes()) {
                Gdx.app.log("touch", "(" + mV.x + ", " + mV.y + ")");
            }
            // check if the touch was inside one of our buttons, and act
            // accordingly
            if (mQuit.contains(mV.x, mV.y)) {
                stopMusic();
                Lol.sGame.doQuit();
            }
            if (mPlay.contains(mV.x, mV.y)) {
                stopMusic();
                Lol.sGame.doChooser();
            }
            if (mHelp != null && mHelp.contains(mV.x, mV.y)) {
                stopMusic();
                Lol.sGame.doHelpLevel(1);
            }
        }

        // now draw the screen...
        GLCommon gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mCamera.update();
        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();
        mSpriteBatch.enableBlending();
        if (mImage != null)
            mSpriteBatch.draw(mImage[0], 0, 0, Lol.sGame.mConfig.getScreenWidth(),
                    Lol.sGame.mConfig.getScreenHeight());
        mSpriteBatch.end();

        // DEBUG: show where the buttons' boxes are
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            mShapeRender.setProjectionMatrix(mCamera.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            mShapeRender.rect(mPlay.x, mPlay.y, mPlay.width, mPlay.height);
            mShapeRender.rect(mHelp.x, mHelp.y, mHelp.width, mHelp.height);
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
}
