/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

abstract public class QuickScene {
    /**
     * When we draw clickable buttons on a QuickScene, this is how we know where
     * the buttons are and what to do when they are clicked
     */
    private class QuickSceneButton {
        /// The region that can be clicked
        Rectangle mRect;

        /// The callback to run when this button is pressed
        LolCallback mCallback;
    }

    /// The level to which this QuickScene is attached
    protected MainScene mWorld;

    /// The text and pictures to display
    private final ArrayList<Renderable> mSprites = new ArrayList<>();

    /// All buttons on the scene are stored here
    private final ArrayList<QuickSceneButton> mButtons = new ArrayList<>();

    /// A local vector for handling touches
    private final Vector3 mTmpVec = new Vector3();

    /// A debug renderer, for drawing outlines of shapes
    private final ShapeRenderer mShapeRender = new ShapeRenderer();

    /// A flag for disabling the scene, so we can keep it from displaying
    protected boolean mDisable;

    /// Track if the Scene is visible. Initially it is not.
    boolean mVisible;

    /// Sound to play when the scene is displayed
    protected Sound mSound;

    /// Time that the Scene started being shown, so we can update timers
    protected long mDisplayTime;

    /// True if we must click in order to clear the scene
    protected boolean mClickToClear = true;

    /// Some default text that we might want to display
    protected String mText;

    /// This camera is for drawing controls that sit above the world
    OrthographicCamera mHudCam;

    /**
     * Construct a QuickScene by giving it a level
     *
     * @param level
     */
    QuickScene(MainScene level, Config config) {
        mWorld = level;
        int width = config.mWidth;
        int height = config.mHeight;

        mHudCam = new OrthographicCamera(width, height);
        mHudCam.position.set(width / 2, height / 2, 0);
    }

    /**
     * Pause the timer when this screen is shown
     */
    void suspendClock() {
        // pause the timer
        Timer.instance().stop();
        mDisplayTime = System.currentTimeMillis();
    }

    /**
     * Internal method to draw a QuickScene
     *
     * @param sb The SpriteBatch used to draw the text and pictures
     * @return true if the PauseScene was drawn, false otherwise
     */
    boolean render(SpriteBatch sb, HudScene hud) {
        // if the scene is not visible, do nothing
        if (!mVisible)
            return false;

        // clear screen and draw images/text via HudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        hud.mHudCam.update();
        sb.setProjectionMatrix(hud.mHudCam.combined);
        sb.begin();
        for (Renderable r : mSprites)
            r.render(sb, 0);
        sb.end();

        // DEBUG: show where the buttons' boxes are
        if (mWorld.mConfig.mShowDebugBoxes) {
            mShapeRender.setProjectionMatrix(hud.mHudCam.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (QuickSceneButton b : mButtons)
                mShapeRender.rect(b.mRect.x, b.mRect.y, b.mRect.width, b.mRect.height);
            mShapeRender.end();
        }

        return true;
    }

    /**
     * Handler to run when the screen is tapped while the scene is being
     * displayed
     */
    boolean onTap(float x, float y, Lol game) {
        // ignore if not visible
        if (!mVisible)
            return false;

        // check for taps to the buttons
        mHudCam.unproject(mTmpVec.set(x, y, 0));
        for (QuickSceneButton b : mButtons) {
            if (b.mRect.contains(mTmpVec.x, mTmpVec.y)) {
                dismiss();
                b.mCallback.onEvent();
                return true;
            }
        }

        // hide the scene only if it's click-to-clear
        if (mClickToClear) {
            dismiss();
            game.liftAllButtons(mTmpVec);
        }
        return true;
    }


    /**
     * Set the sound to play when the screen is displayed
     *
     * @param soundName Name of the sound file to play
     */
    public void setSound(String soundName) {
        mSound = mWorld.mMedia.getSound(soundName);
    }

    /**
     * Add some text to the scene, and center it vertically and horizontally
     *
     * @param text     The text to display
     * @param fontName The font file to use
     * @param size     The size of the text
     */
    public void addText(String text, String textColor, String fontName, int size) {
        mSprites.add(mWorld.makeText(text, textColor, fontName, size));
    }

    /**
     * Add some text to the scene, at an exact location
     *
     * @param text     The text to display
     * @param x        X coordinate of the text
     * @param y        Y coordinate of the text
     * @param fontName The font file to use
     * @param size     The size of the text
     */
    public void addText(String text, int x, int y, String fontColor, String fontName, int size) {
        mSprites.add(mWorld.makeText(x, y, text, fontColor, fontName, size));
    }

    /**
     * Indicate that this scene should not be displayed
     */
    public void disable() {
        mDisable = true;
    }

    /**
     * Add an image to the scene
     *
     * @param imgName The file name for the image to display
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     */
    public void addImage(String imgName, int x, int y, int width, int height) {
        mSprites.add(mWorld.makePicture(x, y, width, height, imgName));
    }

    /**
     * Draw a picture on the scene, but indicate that touching the picture will
     * cause the level to stop playing, and control to return to the chooser.
     *
     * @param imgName The name of the image file that should be displayed
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom left corner
     * @param width   The width of the image
     * @param height  The height of the image
     */
    public void addBackButton(String imgName, int x, int y, int width, int height) {
        QuickSceneButton b = new QuickSceneButton();
        b.mRect = new Rectangle(x, y, width, height);
        b.mCallback = new LolCallback() {
            @Override
            public void onEvent() {
                mVisible = false;
                mWorld.mGame.handleBack();
            }
        };
        mButtons.add(b);
        mSprites.add(mWorld.makePicture(x, y, width, height, imgName));
    }

    /**
     * Place a new touchable button on the scene. When the button is pressed,
     * the scene will be closed, and the callback will run
     *
     * @param x        The X coordinate of the bottom left corner
     * @param y        The Y coordinate of the bottom left corner
     * @param width    The width of the image
     * @param height   The height of the image
     * @param callback The code to run when the button is pressed
     */
    public void addCallbackButton(int x, int y, int width, int height, LolCallback callback) {
        QuickSceneButton b = new QuickSceneButton();
        b.mRect = new Rectangle(x, y, width, height);
        b.mCallback = callback;
        mButtons.add(b);
    }

    /**
     * Indicate that tapping the non-button parts of the scene shouldn't return
     * immediately to the game.
     */
    public void suppressClearClick() {
        mClickToClear = false;
    }

    /**
     * The default is for a PreScene to show until the user touches it to
     * dismiss it. To have the PreScene disappear after a fixed time instead,
     * use this.
     *
     * @param duration The time, in seconds, before the PreScene should disappear.
     */
    public void setExpire(float duration) {
        if (duration > 0) {
            mClickToClear = false;
            // resume timers, or this won't work
            Timer.instance().start();
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    dismiss();
                }
            }, duration);
        }
    }

    /**
     * Set the text that should be drawn, centered, when the level is lost
     *
     * @param text The text to display. Use "" to disable
     */
    public void setDefaultText(String text) {
        mText = text;
    }

    /**
     * Reset a scene, so we can change what is on it.  Only useful for the scenes we show when
     * pausing the game.
     */
    public void reset() {
        mSprites.clear();
        mButtons.clear();
        mDisable = false;
        mVisible = false;
        mSound = null;
        mDisplayTime = 0;
        mClickToClear = true;
        mText = "";
    }

    /**
     * This is the code to start showing a scene
     */
    abstract public void show();

    /**
     * This is the code to remove a scene. It is specific to they type of scene
     * being displayed
     */
    abstract public void dismiss();

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are at the end of a
     * level that has been won
     *
     * @param mLevel The current level
     * @return a QuickScene
     */
    static QuickScene makeWinScene(MainScene mLevel, Config config) {
        QuickScene quickScene = new QuickScene(mLevel, config) {
            @Override
            public void show() {
                // if WinScene is disabled for this level, just move to the next level
                if (mDisable) {
                    dismiss();
                    return;
                }

                // make the PostScene visible
                mVisible = true;

                // The default text to display can change at the last second, so we
                // don't compute it until right here... also, play music
                if (mSound != null)
                    mSound.play(Lol.getGameFact(mWorld.mConfig, "volume", 1));
                addText(mText, "#FFFFFF", mWorld.mConfig.mDefaultFontFace, mWorld.mConfig.mDefaultFontSize);
            }

            @Override
            public void dismiss() {
                mVisible = false;

                // we turn off music here, so that music plays during the PostScene
                mWorld.stopMusic();

                // go to next level (or chooser)
                mWorld.mGame.advanceLevel();
            }
        };
        quickScene.mText = mLevel.mConfig.mDefaultWinText;
        return quickScene;
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are pausing a level
     *
     * @param mLevel The current level
     * @return a QuickScene
     */
    static QuickScene makePauseScene(MainScene mLevel, Config config) {
        QuickScene quickScene = new QuickScene(mLevel, config) {
            @Override
            public void show() {
                Timer.instance().stop();
                mVisible = true;
                mDisplayTime = System.currentTimeMillis();
                if (mSound != null)
                    mSound.play(Lol.getGameFact(mWorld.mConfig, "volume", 1));
            }

            /// TODO: this should be in the super method, and then we should add more behaviors.  win and lose scene timers won't work right now.
            @Override
            public void dismiss() {
                // clear the pauseScene (be sure to resume timers)
                mVisible = false;
                if (mClickToClear) {
                    long showTime = System.currentTimeMillis() - mDisplayTime;
                    Timer.instance().delay(showTime);
                    Timer.instance().start();
                }
            }
        };
        return quickScene;
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are at the end of a
     * level that has been lost
     *
     * @param mLevel The current level
     * @return a QuickScene
     */
    static QuickScene makeLoseScene(MainScene mLevel, Config config) {
        QuickScene quickScene = new QuickScene(mLevel, config) {
            @Override
            public void show() {
                // if LoseScene is disabled for this level, just restart the level
                if (mDisable) {
                    dismiss();
                    return;
                }

                // make the LoseScene visible
                mVisible = true;

                // The default text to display can change at the last second, so we
                // don't compute it until right here... also, play music
                if (mSound != null) {
                    mSound.play(Lol.getGameFact(mWorld.mConfig, "volume", 1));
                }
                addText(mText, "#FFFFFF", mWorld.mConfig.mDefaultFontFace, mWorld.mConfig.mDefaultFontSize);
            }

            @Override
            public void dismiss() {
                mVisible = false;
                mWorld.mGame.repeatLevel();
            }
        };
        quickScene.mText = mLevel.mConfig.mDefaultLoseText;
        return quickScene;
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are about to start a level
     *
     * @param mLevel The current level
     * @return a QuickScene
     */
    static QuickScene makePreScene(MainScene mLevel, Config config) {
        QuickScene quickScene = new QuickScene(mLevel, config) {
            /**
             * Show is a no-op, because the default behavior is good enough
             */
            @Override
            public void show() {
            }

            /**
             * To dismiss, we just hide the scene.
             */
            @Override
            public void dismiss() {
                mVisible = false;
                if (mClickToClear) {
                    long showTime = System.currentTimeMillis() - mDisplayTime;
                    Timer.instance().delay(showTime);
                    Timer.instance().start();
                }
            }
        };
        return quickScene;
    }
}
