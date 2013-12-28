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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Chooser extends ScreenAdapter {

    /**
     * The "Previous Chooser Screen" button
     */
    Button mPrev;

    /**
     * The "Next Chooser Screen" button
     */
    Button mNext;

    /**
     * The "Back To Splash" button
     */
    Button mBack;

    /**
     * The image to display
     */
    TextureRegion[] mImage;

    /**
     * The music to play
     */
    Music mMusic;

    /**
     * Track if the music is actually playing
     */
    boolean mMusicPlaying;

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
     * For tracking touches
     */
    private final Vector3 mTouchVec = new Vector3();

    /**
     * A helper class for tracking where the buttons are, since we don't have an
     * easy way to know which lines are touched.
     */
    class Button {
        /**
         * The rectangle that describes the coordinates of this button
         */
        Rectangle mRect;

        /**
         * The level to run when this is pressed (use 0 for prev/next/back)
         */
        int mLevel;

        /**
         * The image to display for this button
         */
        TextureRegion[] mTr;

        /**
         * Construct by defining the rectangle, level, and image
         * 
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param w The width of the button
         * @param h The height of the button
         * @param level The level to play when this is pressed
         * @param imgName The image to display behind the text for this button
         */
        Button(int x, int y, int w, int h, int level, String imgName) {
            mRect = new Rectangle(x, y, w, h);
            mLevel = level;
            mTr = Media.getImage(imgName);
        }

        void render(SpriteBatch sb, BitmapFont bf) {
        }
    }

    /**
     * All the level boxes we drew
     */
    Button[] levels;

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

    int bWidth;

    int bHeight;

    int hGutter;

    int vGutter;

    public Chooser(LOL game) {

        Gdx.app.log("num", "" + LOL.sGame.mCurrLevelNum);
        ChooserConfiguration cc = LOL.sGame.mChooserConfig;

        bWidth = cc.getLevelButtonWidth();
        bHeight = cc.getLevelButtonHeight();
        hGutter = cc.getHPadding();
        vGutter = cc.getBPadding();

        // get the background image and music
        mImage = Media.getImage(cc.getBackgroundName());
        if (cc.getMusicName() != null)
            mMusic = Media.getMusic(cc.getMusicName());

        mBack = new Button(cc.getBackButtonX(), cc.getBackButtonY(), cc.getBackButtonWidth(),
                cc.getBackButtonHeight(), 0, cc.getBackButtonName());
        mPrev = new Button(cc.getPrevButtonX(), cc.getPrevButtonY(), cc.getPrevButtonWidth(),
                cc.getPrevButtonHeight(), 0, cc.getPrevButtonName());
        mNext = new Button(cc.getNextButtonX(), cc.getNextButtonY(), cc.getNextButtonWidth(),
                cc.getNextButtonHeight(), 0, cc.getNextButtonName());

        int numLevels = LOL.sGame.mConfig.getNumLevels();


        int maxnum = cc.getColumns() * cc.getRows();
        int first = LOL.sGame.mCurrLevelNum;
        if (first > 0) first--;
        first -= first % maxnum;
        first += 1;
        int q = first + maxnum - 1;
        Gdx.app.log("note", "first=" + first + " maxnum=" + maxnum + " q=" + q);

        int last = Math.min(numLevels, q);
        Gdx.app.log("last", "" + last);

        levels = new Button[maxnum];
        // figure out number of rows and columns...
        int camWidth = LOL.sGame.mConfig.getScreenWidth();
        int camHeight = LOL.sGame.mConfig.getScreenHeight();

        int top = camHeight - cc.getTopMargin();
        int left = cc.getLeftMargin();

        int next = 0;
        int mytop = top - bHeight;
        int myleft = left;
        for (int i = first; i <= last; ++i) {
            if (i % cc.getColumns() == 1 && i != first) {
                Gdx.app.log("h", "newline");
                mytop = mytop - bHeight - vGutter;
                myleft = left;
            }
            levels[next] = new Button(myleft, mytop, bWidth, bHeight, i, cc.getLevelButtonName());
            myleft = myleft + bWidth + hGutter;
            next++;
        }

        // configure the camera
        mCamera = new OrthographicCamera(camWidth, camHeight);
        mCamera.position.set(camWidth / 2, camHeight / 2, 0);

        // create a font
        mFont = Media.getFont(cc.getLevelFont(), cc.getLevelFontSize());

        // and our renderers
        mSpriteBatch = new SpriteBatch();
        mShapeRender = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        playMusic();
        manageTouches();

        mCamera.update();

        // clear the screen
        GLCommon gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw squares...
        mShapeRender.setProjectionMatrix(mCamera.combined);
        mShapeRender.begin(ShapeType.Line);
        mShapeRender.setColor(Color.BLUE);

        mShapeRender.rect(mBack.mRect.x, mBack.mRect.y, mBack.mRect.width, mBack.mRect.height);
        mShapeRender.rect(mPrev.mRect.x, mPrev.mRect.y, mPrev.mRect.width, mPrev.mRect.height);
        mShapeRender.rect(mNext.mRect.x, mNext.mRect.y, mNext.mRect.width, mNext.mRect.height);

        for (Button ls : levels) {
            if (ls != null) {
                mShapeRender.rect(ls.mRect.x, ls.mRect.y, ls.mRect.width, ls.mRect.height);
                mShapeRender.rect(ls.mRect.x + 1, ls.mRect.y + 1, ls.mRect.width - 2,
                        ls.mRect.height - 2);
            }
        }
        mShapeRender.end();

        mShapeRender.begin(ShapeType.Filled);
        mShapeRender.setColor(.4f, .4f, .4f, 0.9f);
        int unlocked = LOL.sGame.readUnlocked();
        for (Button ls : levels) {
            if (ls != null) {
                if (ls.mLevel > unlocked && !LOL.sGame.mConfig.getUnlockMode()) {
                    mShapeRender.rect(ls.mRect.x + 2, ls.mRect.y + 2, ls.mRect.width - 4,
                            ls.mRect.height - 4);
                }
            }
        }
        mShapeRender.end();

        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();

        if (mImage != null)
            mSpriteBatch.draw(mImage[0], 0, 0, LOL.sGame.mConfig.getScreenWidth(),
                    LOL.sGame.mConfig.getScreenHeight());

        if (mBack.mTr != null)
            mSpriteBatch.draw(mBack.mTr[0], mBack.mRect.x, mBack.mRect.y, mBack.mRect.width,
                    mBack.mRect.height);
        if (mPrev.mTr != null)
            mSpriteBatch.draw(mPrev.mTr[0], mPrev.mRect.x, mPrev.mRect.y, mPrev.mRect.width,
                    mPrev.mRect.height);
        if (mNext.mTr != null)
            mSpriteBatch.draw(mNext.mTr[0], mNext.mRect.x, mNext.mRect.y, mNext.mRect.width,
                    mNext.mRect.height);

        for (Button ls : levels) {
            if (ls != null) {
                float x = mFont.getBounds(ls.mLevel + "").width;
                float y = mFont.getBounds(ls.mLevel + "").height;
                mFont.draw(mSpriteBatch, ls.mLevel + "", ls.mRect.x + bWidth / 2 - x / 2,
                        ls.mRect.y + bHeight - y);
            }
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
                // touchDragged((int)x, (int)y, i);
            } else if (touchStates[i] && !lastTouches[i]) {
                touchDown((int)x, (int)y, i, 0);
            } else if (!touchStates[i] && lastTouches[i]) {
                // touchUp((int)x, (int)y, i, 0);
            }
            lastTouches[i] = touchStates[i];
        }
    }

    private boolean touchDown(int x, int y, int pointer, int newParam) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        ChooserConfiguration cc = LOL.sGame.mChooserConfig;
        if (mBack.mRect.contains(mTouchVec.x, mTouchVec.y)) {
            Gdx.app.log("hey", "back");
            LOL.sGame.handleBack();
        }
        if (mPrev.mRect.contains(mTouchVec.x, mTouchVec.y)) {
            Gdx.app.log("hey", "prev");
            LOL.sGame.mCurrLevelNum -= (cc.getColumns() * cc.getRows());
            LOL.sGame.doChooser();
        }
        if (mNext.mRect.contains(mTouchVec.x, mTouchVec.y)) {
            Gdx.app.log("hey", "Next");
            if (LOL.sGame.mCurrLevelNum == 0)
                LOL.sGame.mCurrLevelNum = 1;
            LOL.sGame.mCurrLevelNum += (cc.getColumns() * cc.getRows());
            LOL.sGame.doChooser();
        }

        int unlocked = LOL.sGame.readUnlocked();
        for (Button ls : levels) {
            if (ls != null) {
                if (ls.mLevel <= unlocked || LOL.sGame.mConfig.getUnlockMode()) {
                    if (ls.mRect.contains(mTouchVec.x, mTouchVec.y)) {
                        LOL.sGame.doPlayLevel(ls.mLevel);
                        return true;
                    }
                }
            }
        }
        return false;
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
