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
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

public class QuickScene extends LolScene {
    /// A flag for disabling the scene, so we can keep it from displaying
    private boolean mDisable;
    /// Track if the Scene is visible. Initially it is not.
    boolean mVisible;
    /// Sound to play when the scene is displayed
    private Sound mSound;
    /// Time that the Scene started being shown, so we can update timers
    private long mDisplayTime;
    /// True if we must click in order to clear the scene
    private boolean mClickToClear;
    /// Some default text that we might want to display
    private String mText;
    /// Scene-specific code to run when we show() a scene
    private LolAction mShowAction;
    /// Scene-specific code to run when we dismiss() a scene
    private LolAction mDismissAction;

    /**
     * Construct a QuickScene with default show and dismiss behaviors
     *
     * @param media       The media object, with all loaded sound and image files
     * @param config      The game-wide configuration
     * @param defaultText The default text to display, centered, on this QuickScene
     */
    QuickScene(Media media, Config config, String defaultText) {
        super(media, config);
        mClickToClear = true;
        mText = defaultText;

        // Set the default dismiss action to clear the screen and fix the timers
        mDismissAction = new LolAction() {
            @Override
            public void go() {
                if (mClickToClear) {
                    long showTime = System.currentTimeMillis() - mDisplayTime;
                    Timer.instance().delay(showTime);
                    Timer.instance().start();
                }
            }
        };

        // Set the default show action to put the default text on the screen
        mShowAction = new LolAction() {
            @Override
            public void go() {
                // If the scene has been disabled, just return
                if (mDisable) {
                    dismiss();
                    return;
                }
                // play the show sound
                if (mSound != null)
                    mSound.play(Lol.getGameFact(mConfig, "volume", 1));
                // The default text to display can change at the last second, so we compute it here
                addTextCentered(mConfig.mWidth / mConfig.mPixelMeterRatio / 2, mConfig.mHeight / mConfig.mPixelMeterRatio / 2,
                        mConfig.mDefaultFontFace, mConfig.mDefaultFontColor, mConfig.mDefaultFontSize, "", "", new TextProducer() {
                            @Override
                            public String makeText() {
                                return mText;
                            }
                        }, 0);
            }
        };
    }

    /**
     * Pause the timer when this screen is shown
     */
    void suspendClock() {
        Timer.instance().stop();
        mDisplayTime = System.currentTimeMillis();
    }

    /**
     * Render the QuickScene, or return false if it is not supposed to be shown
     *
     * @param sb    The SpriteBatch used to draw the text and pictures
     * @param delta The time since the last render
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
     * Handler to run when the screen is tapped while the scene is being displayed
     *
     * @param screenX The x coordinate on screen where the touch happened
     * @param screenY The y coordinate on screen where the touch happened
     * @param game    The top-level game object
     * @return True if the tap was handled, false otherwise
     */
    boolean onTap(float screenX, float screenY, Lol game) {
        // ignore if not visible
        if (!mVisible)
            return false;

        // check for taps to the buttons
        mHitActor = null;
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
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
     * Indicate that tapping the non-button parts of the scene shouldn't return immediately to the
     * game.
     */
    public void suppressClearClick() {
        mClickToClear = false;
    }

    /**
     * The default is for a Scene to show until the user touches it to dismiss it. To have the
     * Scene disappear after a fixed time instead, use this.
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
     * Set the text that should be drawn, centered, when the Scene is shown
     *
     * @param text The text to display. Use "" to disable
     */
    public void setDefaultText(String text) {
        mText = text;
    }

    /**
     * Reset a scene, so we can change what is on it.  Only useful for the scenes we might show more
     * than once (such as the PauseScene)
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
     * Show the scene
     */
    public void show() {
        mVisible = true;
        if (mShowAction != null)
            mShowAction.go();
    }

    /**
     * Stop showing the scene
     */
    public void dismiss() {
        mVisible = false;
        mDismissAction.go();
    }

    /**
     * Provide some custom code to run when the scene is dismissed
     *
     * @param dismissAction The code to run
     */
    void setDismissAction(LolAction dismissAction) {
        mDismissAction = dismissAction;
    }

    /**
     * Provide some custom code to run when the scene is dismissed
     *
     * @param showAction The code to run
     */
    void setShowAction(LolAction showAction) {
        mShowAction = showAction;
    }

    /**
     * If this scene is to be uased as a pause scene, then the code to run when we dismiss it needs
     * to re-enable any timers for the main game.
     */
    void setAsPauseScene() {
        mShowAction = new LolAction() {
            @Override
            public void go() {
                Timer.instance().stop();
                mDisplayTime = System.currentTimeMillis();
                if (mSound != null)
                    mSound.play(Lol.getGameFact(mConfig, "volume", 1));
            }
        };
    }
}
