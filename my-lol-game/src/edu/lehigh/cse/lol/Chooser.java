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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * The Chooser is a screen that gives the player a choice of levels of the game
 * to play.
 */
public class Chooser extends ScreenAdapter {

    /**
     * The "Previous Chooser Screen" button
     */
    private Button mPrev;

    /**
     * The "Next Chooser Screen" button
     */
    private Button mNext;

    /**
     * The "Back To Splash" button
     */
    private final Button mBack;

    /**
     * The image to display as the background
     */
    private final TextureRegion[] mImage;

    /**
     * The music to play
     */
    private Music mMusic;

    /**
     * Track if the music is actually playing
     */
    private boolean mMusicPlaying;

    /**
     * For tracking touches
     */
    private final Vector3 mV = new Vector3();

    /**
     * All the level boxes we drew
     */
    private final Button[] levels;

    /**
     * The camera we will use
     */
    private final OrthographicCamera mCamera;

    /**
     * For rendering
     */
    private final SpriteBatch mSpriteBatch;

    /**
     * For making text
     */
    private final BitmapFont mFont;

    /**
     * For debug rendering
     */
    private final ShapeRenderer mShapeRender;

    /**
     * A helper class for tracking where the buttons are
     */
    private class Button {
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
    }

    /**
     * Construct a chooser for the currently selected set of levels, and prepare
     * all the buttons
     */
    public Chooser() {
        // start by getting the pieces of configuration that we use over and
        // over again
        ChooserConfiguration cc = Lol.sGame.mChooserConfig;
        int levelsPerChooser = cc.getColumns() * cc.getRows();
        int totalLevels = Lol.sGame.mConfig.getNumLevels();

        // set up the background image and music
        mImage = Media.getImage(cc.getBackgroundName());
        if (cc.getMusicName() != null)
            mMusic = Media.getMusic(cc.getMusicName());

        // always make the back button
        mBack = new Button(cc.getBackButtonX(), cc.getBackButtonY(), cc.getBackButtonWidth(),
                cc.getBackButtonHeight(), 0, cc.getBackButtonName());

        // make the previous button if we aren't drawing the first set of
        // choices
        if (Lol.sGame.mCurrLevelNum > levelsPerChooser) {
            mPrev = new Button(cc.getPrevButtonX(), cc.getPrevButtonY(), cc.getPrevButtonWidth(),
                    cc.getPrevButtonHeight(), 0, cc.getPrevButtonName());
        }

        // make the next button if we aren't drawing the last set of choices
        if (Lol.sGame.mCurrLevelNum + levelsPerChooser - 1 < totalLevels) {
            mNext = new Button(cc.getNextButtonX(), cc.getNextButtonY(), cc.getNextButtonWidth(),
                    cc.getNextButtonHeight(), 0, cc.getNextButtonName());
        }

        // figure out the first level to draw on this chooser. Note that '0' is
        // a possible value of mCurrLevelNum when we come straight from Splash,
        // so we must handle it
        int first = Lol.sGame.mCurrLevelNum;
        if (first > 0)
            first--;
        first = first - (first % levelsPerChooser) + 1;
        // figure out the last level to draw on this chooser
        int last = Math.min(totalLevels, first + levelsPerChooser - 1);

        // get screen dimensions, and figure out the *top* left corner of the
        // first level button
        int camWidth = Lol.sGame.mConfig.getScreenWidth();
        int camHeight = Lol.sGame.mConfig.getScreenHeight();
        int top = camHeight - cc.getTopMargin();
        int left = cc.getLeftMargin();

        // button dimensions
        int bWidth = cc.getLevelButtonWidth();
        int bHeight = cc.getLevelButtonHeight();
        // padding
        int hGutter = cc.getHPadding();
        int vGutter = cc.getBPadding();
        // position of next button
        int mytop = top - bHeight;
        int myleft = left;
        // now let's make buttons for the levels
        levels = new Button[levelsPerChooser];
        int index = 0;
        for (int i = first; i <= last; ++i) {
            // move down a row?
            if (i % cc.getColumns() == 1 && i != first) {
                mytop = mytop - bHeight - vGutter;
                myleft = left;
            }
            levels[index] = new Button(myleft, mytop, bWidth, bHeight, i, cc.getLevelButtonName());
            myleft = myleft + bWidth + hGutter;
            index++;
        }

        // configure the camera
        mCamera = new OrthographicCamera(camWidth, camHeight);
        mCamera.position.set(camWidth / 2, camHeight / 2, 0);

        // create a font
        mFont = Media.getFont(cc.getLevelFont(), cc.getLevelFontSize());
        mFont.setColor(((float)cc.getLevelFontRed()) / 255, ((float)cc.getLevelFontGreen()) / 255,
                ((float)cc.getLevelFontBlue()) / 255, 1);
        // and create our renderers
        mSpriteBatch = new SpriteBatch();
        mShapeRender = new ShapeRenderer();
    }

    /**
     * Start the music if it's not already playing
     */
    private void playMusic() {
        if (!mMusicPlaying && mMusic != null) {
            mMusicPlaying = true;
            mMusic.play();
        }
    }

    /**
     * Pause the music if it's playing
     */
    private void pauseMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.pause();
        }
    }

    /**
     * Stop the music if it's playing
     */
    private void stopMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.stop();
        }
    }

    /**
     * Render the chooser
     * 
     * @param delta The time since the last call to render
     */
    @Override
    public void render(float delta) {
        // make sure music is playing, and check for touches
        playMusic();
        if (Gdx.input.justTouched())
            touchDown(Gdx.input.getX(0), Gdx.input.getY(0));

        // update the camera
        mCamera.update();

        // clear the screen
        GL20 gl = Gdx.gl;
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();
        // draw the background image
        if (mImage != null)
            mSpriteBatch.draw(mImage[0], 0, 0, Lol.sGame.mConfig.getScreenWidth(),
                    Lol.sGame.mConfig.getScreenHeight());
        // draw back/prev/next
        mSpriteBatch.draw(mBack.mTr[0], mBack.mRect.x, mBack.mRect.y, mBack.mRect.width,
                mBack.mRect.height);
        if (mPrev != null && mPrev.mTr != null)
            mSpriteBatch.draw(mPrev.mTr[0], mPrev.mRect.x, mPrev.mRect.y, mPrev.mRect.width,
                    mPrev.mRect.height);
        if (mNext != null && mNext.mTr != null)
            mSpriteBatch.draw(mNext.mTr[0], mNext.mRect.x, mNext.mRect.y, mNext.mRect.width,
                    mNext.mRect.height);

        // draw the level buttons
        int unlocked = Math.max(1, Facts.getGameFact("unlocked"));
        for (Button ls : levels) {
            if (ls != null) {
                // draw picture
                mSpriteBatch.draw(ls.mTr[0], ls.mRect.x, ls.mRect.y, ls.mRect.width,
                        ls.mRect.height);
                // draw overlay text
                String txt = ls.mLevel + "";
                if (ls.mLevel > unlocked && !Lol.sGame.mConfig.getUnlockMode())
                    txt = Lol.sGame.mChooserConfig.getLevelLockText();
                float x = mFont.getBounds(txt).width;
                float y = mFont.getBounds(txt).height;
                mFont.draw(mSpriteBatch, txt, ls.mRect.x + ls.mRect.width / 2 - x / 2, ls.mRect.y
                        + ls.mRect.height / 2 + y / 2);
            }
        }
        mSpriteBatch.end();

        // DEBUG: show the buttons' boxes
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            // draw squares...
            mShapeRender.setProjectionMatrix(mCamera.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.GRAY);

            mShapeRender.rect(mBack.mRect.x, mBack.mRect.y, mBack.mRect.width, mBack.mRect.height);
            if (mPrev != null)
                mShapeRender.rect(mPrev.mRect.x, mPrev.mRect.y, mPrev.mRect.width,
                        mPrev.mRect.height);
            if (mNext != null)
                mShapeRender.rect(mNext.mRect.x, mNext.mRect.y, mNext.mRect.width,
                        mNext.mRect.height);

            for (Button ls : levels) {
                if (ls != null) {
                    mShapeRender.rect(ls.mRect.x, ls.mRect.y, ls.mRect.width, ls.mRect.height);
                }
            }
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

    /**
     * Handle a screen touch by figuring out what button was pressed, and then
     * taking action
     * 
     * @param x The X coordinate of the touch
     * @param y The Y coordinate of the touch
     */
    private void touchDown(int x, int y) {
        ChooserConfiguration cc = Lol.sGame.mChooserConfig;
        // get the coordinates of the touch
        mCamera.unproject(mV.set(x, y, 0));
        // DEBUG: display touch coordinates
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            Gdx.app.log("touch", "(" + mV.x + ", " + mV.y + ")");
        }
        // handle 'back' presses
        if (mBack.mRect.contains(mV.x, mV.y)) {
            Lol.sGame.handleBack();
            return;
        }
        // handle 'previous screen' requests
        if (mPrev != null && mPrev.mRect.contains(mV.x, mV.y)) {
            Lol.sGame.mCurrLevelNum -= (cc.getColumns() * cc.getRows());
            Lol.sGame.doChooser();
            return;
        }
        // handle 'next screen' requests
        if (mNext != null && mNext.mRect.contains(mV.x, mV.y)) {
            // special case for when we came straight from the Splash screen
            if (Lol.sGame.mCurrLevelNum == 0)
                Lol.sGame.mCurrLevelNum = 1;
            Lol.sGame.mCurrLevelNum += (cc.getColumns() * cc.getRows());
            Lol.sGame.doChooser();
            return;
        }

        // check for press to an unlocked level
        int unlocked = Math.max(1, Facts.getGameFact("unlocked"));
        for (Button ls : levels) {
            if (ls != null && (ls.mLevel <= unlocked || Lol.sGame.mConfig.getUnlockMode())) {
                if (ls.mRect.contains(mV.x, mV.y))
                    Lol.sGame.doPlayLevel(ls.mLevel);
            }
        }
    }
}
