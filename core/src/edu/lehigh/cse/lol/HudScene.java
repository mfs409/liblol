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

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Hud is a heads-up display.  It provides a means for putting things on the "Phone Screen" instead
 * of "in the world".
 */
public class HudScene extends LolScene {
    /// The set of all controls that have toggle handlers.  We need this, so we can "lift" toggles
    /// on screen change evenrs
    final ArrayList<SceneActor> mToggleControls;

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     *
     * @param media  All image and sound assets for the game
     * @param config The game-wide configuration
     */
    HudScene(Media media, Config config) {
        super(media, config);
        mToggleControls = new ArrayList<>();
    }

    /**
     * Draw the Hud
     *
     * @param sb    The spritebatch to use when drawing
     * @param delta The time since the last render
     */
    boolean render(SpriteBatch sb, float delta) {
        mCamera.update();

        // Advance the physics world by 1/45 of a second (1/45 is the recommended rate)
        mWorld.step(1 / 45f, 8, 3);

        // Render all actors and text
        sb.setProjectionMatrix(mCamera.combined);
        sb.begin();
        for (ArrayList<Renderable> a : mRenderables) {
            for (Renderable r : a) {
                r.render(sb, delta);
            }
        }
        sb.end();

        // TODO: box2d shape renderer for controls2
        if (mConfig.mShowDebugBoxes) {
        }
        return true;
    }

    /**
     * Simulate all toggle controls being up-pressed.  This is useful when we load a pre/post/pause
     * scene
     *
     * @param touchX The X location at which the simulated "up" happened
     * @param touchY The Y location at which the simulated "up" happened
     */
    void liftAllButtons(float touchX, float touchY) {
        for (SceneActor c : mToggleControls) {
            if (c.mIsTouchable) {
                c.mToggleHandler.go(true, touchX, touchY);
            }
        }
    }

    /**
     * Reset the Hud
     */
    void reset() {
        mToggleControls.clear();
        super.reset();
    }

    /**
     * Respond to a Tap event
     *
     * @param screenX  The X coordinate of the tap, in screen coordinates
     * @param screenY  The Y coordinate of the tap, in screen coordinates
     * @param worldCam The main screen's camera
     * @return True if the event was handled, false otherwise
     */
    boolean handleTap(float screenX, float screenY, OrthographicCamera worldCam) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // convert screenX/screenY to main world coordinates before passing to the handler.  The
        // handler is provided by the programmer, and operates on the world, so it needs world
        // coordinates
        worldCam.unproject(mTouchVec.set(screenX, screenY, 0));
        // call the handler
        return mHitActor != null && mHitActor.mTapHandler != null &&
                mHitActor.mTapHandler.go(mTouchVec.x, mTouchVec.y);
    }

    /**
     * Respond to a Pan event
     *
     * @param screenX  The X coordinate of the pan, in screen coordinates
     * @param screenY  The Y coordinate of the pan, in screen coordinates
     * @param deltaX   The change in X since the last pan event
     * @param deltaY   The change in Y since the last pan event
     * @param worldCam The main screen's camera
     * @return True if the event was handled, false otherwise
     */
    boolean handlePan(float screenX, float screenY, float deltaX, float deltaY,
                      OrthographicCamera worldCam) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // convert screenX/screenY to main world coordinates before passing to the handler.  The
        // handler is provided by the programmer, and operates on the world, so it needs world
        // coordinates
        worldCam.unproject(mTouchVec.set(screenX, screenY, 0));
        // call the handler
        return mHitActor != null && ((SceneActor) mHitActor).mPanHandler != null &&
                ((SceneActor) mHitActor).mPanHandler.go(mTouchVec.x, mTouchVec.y, deltaX, deltaY);
    }

    /**
     * Respond to a Pan Stop event
     *
     * @param screenX  The X coordinate of the pan stop, in screen coordinates
     * @param screenY  The Y coordinate of the pan stop, in screen coordinates
     * @param worldCam The main screen's camera
     * @return True if the event was handled, false otherwise
     */
    boolean handlePanStop(float screenX, float screenY, OrthographicCamera worldCam) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // convert screenX/screenY to main world coordinates before passing to the handler.  The
        // handler is provided by the programmer, and operates on the world, so it needs world
        // coordinates
        worldCam.unproject(mTouchVec.set(screenX, screenY, 0));
        // call the handler
        return mHitActor != null && ((SceneActor) mHitActor).mPanStopHandler != null &&
                ((SceneActor) mHitActor).mPanStopHandler.go(mTouchVec.x, mTouchVec.y);
    }

    /**
     * Respond to a zoom (pinch) event
     *
     * @param initialDistance The original distance between the two fingers
     * @param distance        The current distance between the two fingers
     * @return True if the event was handled, false otherwise
     */
    boolean handleZoom(float initialDistance, float distance) {
        // NB: This code is a bit funny... we had a previous down, which set mTouchVec, so we just
        //     re-use it.  This is probably not correct if the player is using two hands at once.

        // Find the actor who was involved in the zoom, and run its handler
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        return mHitActor != null && ((SceneActor) mHitActor).mZoomHandler != null &&
                ((SceneActor) mHitActor).mZoomHandler.go(initialDistance, distance);
    }

    /**
     * Respond to a Down event
     *
     * @param screenX  The X coordinate of the Down, in screen coordinates
     * @param screenY  The Y coordinate of the Down, in screen coordinates
     * @param worldCam The main screen's camera
     * @return True if the event was handled, false otherwise
     */
    boolean handleDown(float screenX, float screenY, OrthographicCamera worldCam) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // convert screenX/screenY to main world coordinates before passing to the handler.  The
        // handler is provided by the programmer, and operates on the world, so it needs world
        // coordinates
        worldCam.unproject(mTouchVec.set(screenX, screenY, 0));
        // first, try to use a Toggle handler
        if (mHitActor != null && mHitActor.mToggleHandler != null) {
            if (mHitActor.mToggleHandler.go(false, mTouchVec.x, mTouchVec.y))
                return true;
        }
        // if that fails, try to pass to a Down handler
        return mHitActor != null && ((SceneActor) mHitActor).mDownHandler != null &&
                ((SceneActor) mHitActor).mDownHandler.go(mTouchVec.x, mTouchVec.y);
    }

    /**
     * Respond to an Up event
     *
     * @param screenX  The X coordinate of the Up, in screen coordinates
     * @param screenY  The Y coordinate of the Up, in screen coordinates
     * @param worldCam The main screen's camera
     * @return True if the event was handled, false otherwise
     */
    boolean handleUp(float screenX, float screenY, OrthographicCamera worldCam) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // convert screenX/screenY to main world coordinates before passing to the handler.  The
        // handler is provided by the programmer, and operates on the world, so it needs world
        // coordinates
        worldCam.unproject(mTouchVec.set(screenX, screenY, 0));
        // call the handler
        return mHitActor != null && mHitActor.mToggleHandler != null &&
                mHitActor.mToggleHandler.go(true, mTouchVec.x, mTouchVec.y);
    }
}
