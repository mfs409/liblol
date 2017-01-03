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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

abstract public class QuickScene extends LolScene {
    /// A flag for disabling the scene, so we can keep it from displaying
    boolean mDisable;
    /// Track if the Scene is visible. Initially it is not.
    boolean mVisible;
    /// Sound to play when the scene is displayed
    Sound mSound;
    /// Time that the Scene started being shown, so we can update timers
    long mDisplayTime;
    /// True if we must click in order to clear the scene
    boolean mClickToClear;
    /// Some default text that we might want to display
    String mText;

    /**
     * Construct a QuickScene by giving it a level
     */
    private QuickScene(Media media, Config config) {
        super(media, config);
        mClickToClear = true;
        mText = "";
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
    boolean render(SpriteBatch sb, float delta) {
        // if the scene is not visible, do nothing
        if (!mVisible)
            return false;

        // clear screen and draw images/text via HudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mCamera.update();
        sb.setProjectionMatrix(mCamera.combined);
        sb.begin();
        for (ArrayList<Renderable> a : mRenderables) {
            for (Renderable r : a) {
                r.render(sb, delta);
            }
        }
        sb.end();

        // TODO: debug rendering?

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
        mHitActor = null;
        mCamera.unproject(mTouchVec.set(x, y, 0));
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f, mTouchVec.y + 0.1f);
        if (mHitActor != null && mHitActor.mTapHandler != null) {
            dismiss(); // TODO: make this the responsibility of the programmer?
            mHitActor.onTap(mTouchVec);
            return true;
        }

        // hide the scene only if it's click-to-clear
        if (mClickToClear) {
            dismiss();
            game.liftAllButtons(mTouchVec.x, mTouchVec.y);
        }
        return true;
    }

    /**
     * Set the sound to play when the screen is displayed
     *
     * @param soundName Name of the sound file to play
     */
    public void setSound(String soundName) {
        mSound = mMedia.getSound(soundName);
    }

    /**
     * Indicate that this scene should not be displayed
     */
    public void disable() {
        mDisable = true;
    }

    /**
     * Add a button that pauses the game (via a single tap) by causing a
     * PauseScene to be displayed. Note that you must configure a PauseScene, or
     * pressing this button will cause your game to crash.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public SceneActor addTapControl(float x, float y, float width, float height, String imgName, final TouchEventHandler action) {
        SceneActor c = new SceneActor(this, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        c.mTapHandler = action;
        action.mSource = c;
        addActor(c, 0);
        return c;
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
        mDisable = false;
        mVisible = false;
        mSound = null;
        mDisplayTime = 0;
        mClickToClear = true;
        mText = "";
        super.reset();
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
     * @return a QuickScene
     */
    static QuickScene makeWinScene(final MainScene level, Media media, final Config config) {
        QuickScene quickScene = new QuickScene(media, config) {
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
                    mSound.play(Lol.getGameFact(mConfig, "volume", 1));
                addTextCentered(config.mWidth / config.mPixelMeterRatio / 2, config.mHeight / config.mPixelMeterRatio / 2, config.mDefaultFontFace, config.mDefaultFontColor, config.mDefaultFontSize, "", "", new TextProducer() {
                    @Override
                    public String makeText() {
                        return mText;
                    }
                }, 0);
            }

            @Override
            public void dismiss() {
                mVisible = false;

                // we turn off music here, so that music plays during the PostScene
                level.stopMusic();

                // go to next level (or chooser)
                level.mGame.mManager.advanceLevel();
            }
        };
        quickScene.mText = config.mDefaultWinText;
        return quickScene;
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are pausing a level
     *
     * @return a QuickScene
     */
    static QuickScene makePauseScene(Media media, Config config) {
        return new QuickScene(media, config) {
            @Override
            public void show() {
                Timer.instance().stop();
                mVisible = true;
                mDisplayTime = System.currentTimeMillis();
                if (mSound != null)
                    mSound.play(Lol.getGameFact(mConfig, "volume", 1));
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
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are at the end of a
     * level that has been lost
     *
     * @return a QuickScene
     */
    static QuickScene makeLoseScene(final MainScene level, Media media, final Config config) {
        QuickScene quickScene = new QuickScene(media, config) {
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
                    mSound.play(Lol.getGameFact(mConfig, "volume", 1));
                }
                addTextCentered(config.mWidth / config.mPixelMeterRatio / 2, config.mHeight / config.mPixelMeterRatio / 2, config.mDefaultFontFace, config.mDefaultFontColor, config.mDefaultFontSize, "", "", new TextProducer() {
                    @Override
                    public String makeText() {
                        return mText;
                    }
                }, 0);
            }

            @Override
            public void dismiss() {
                mVisible = false;
                level.mGame.mManager.repeatLevel();
            }
        };
        quickScene.mText = config.mDefaultLoseText;
        return quickScene;
    }

    /**
     * Create a QuickScene that has the appropriate behaviors for when we are about to start a level
     *
     * @return a QuickScene
     */
    static QuickScene makePreScene(Media media, Config config) {
        return new QuickScene(media, config) {
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
    }
}
