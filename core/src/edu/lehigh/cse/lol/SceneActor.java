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

/**
 * SceneActor is any actor that can be put onto a QuickScene or the Hud.  They don't have all of
 * the interaction abilities of a WorldActor, but they do have more touch features than BaseActor.
 */
public class SceneActor extends BaseActor {
    /// Should we run code when this actor is touched?
    boolean mIsTouchable;

    /// callback when this actor receives a pan event
    PanEventHandler mPanHandler;

    /// callback when this actor receives a pan stop event
    TouchEventHandler mPanStopHandler;

    /// callback when this actor receives a zoom event
    TouchEventHandler mZoomHandler;

    /// callback when this actor receives a Down event
    TouchEventHandler mDownHandler;

    /**
     * Construct a SceneActor, but do not give it any physics yet
     *
     * @param scene   The scene into which this actor should be placed
     * @param imgName The image to show for this actor
     * @param width   The width of the actor's image and body, in meters
     * @param height  The height of the actor's image and body, in meters
     */
    SceneActor(LolScene scene, String imgName, float width, float height) {
        super(scene, imgName, width, height);
    }

    /**
     * Disable touch for this actor
     */
    public void disableTouch() {
        mIsTouchable = false;
    }

    /**
     * Enable touch for this actor
     */
    public void enableTouch() {
        mIsTouchable = true;
    }
}
