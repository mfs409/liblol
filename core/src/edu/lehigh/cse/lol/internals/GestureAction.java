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

package edu.lehigh.cse.lol.internals;

import com.badlogic.gdx.math.Vector3;

/**
 * When there is a gesture on the screen, we will convert the event's
 * coordinates to world coordinates, then use this to handle it. This object can
 * be attached to actors, Controls, or to the Level itself, to specify a handler
 * for certain events.
 *
 * Note: typically, one will override certain methods of this class to get the
 * desired behavior
 */
public class GestureAction {
    /**
     * We offer a HOLD/RELEASE gesture. This flag tells us if we're in a hold
     * event.
     */
    public boolean mHolding;

    /**
     * Handle a drag event
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onDrag(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a down press (hopefully to turn it into a hold/release)
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onDown(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle an up press (hopefully to turn it into a release)
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onUp(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a tap event
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onTap(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a pan event
     *
     * @param touchVec The x/y/z world coordinates of the touch
     * @param deltaX   the change in X scale, in screen coordinates
     * @param deltaY   the change in Y scale, in screen coordinates
     */
    public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
        return false;
    }

    /**
     * Handle a pan stop event
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onPanStop(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a fling event
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean onFling(Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a toggle event. This is usually built from a down and an up.
     *
     * @param touchVec The x/y/z coordinates of the touch
     */
    public boolean toggle(boolean isUp, Vector3 touchVec) {
        return false;
    }

    /**
     * Handle a zoom event
     *
     * @param initialDistance The distance between fingers when the pinch started
     * @param distance        The current distance between fingers
     */
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }
}