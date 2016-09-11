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

import com.badlogic.gdx.math.Vector3;

/**
 * LolCallback provides an easy way to describe code that should run in response
 * to special events, such as timers, screen presses, or collisions between
 * actors.
 *
 * In the interest of simplicity, we use some public fields instead of getters
 * and setters. This also lets us get away with using the default constructor.
 */
public abstract class LolCallback implements Cloneable {
    /**
     * An integer that may be of use during the callback
     */
    public int mIntVal;

    /**
     * A float that may be of use during the callback
     */
    public float mFloatVal;

    /**
     * For collision events or actor press events, this is the actor to which
     * the callback is attached
     */
    public Actor mAttachedActor;

    /**
     * For collision events, this is the other actor involved in the collision
     */
    public Actor mCollideActor;

    /**
     * When a callback runs in response to a screen touch, we may need to know the world coordinates of the down-press
     */
    public Vector3 mDownLocation;

    /**
     * When a callback runs in response to a screen touch, we may need to know the world coordinates of the up-press
     */
    public Vector3 mUpLocation;

    /**
     * When a callback runs in response to a screen touch, we may need to know the world coordinates of the press as it moves.
     */
    public Vector3 mMoveLocation;

    /**
     * Make a copy of the current LolCallback
     */
    public LolCallback clone() {
        // This code is trickier than one would think. The issue is
        // that we are doing this copy as a way of getting a new object with the
        // same overridden onEvent() method. That, in turn, means we need to
        // implement Cloneable.
        LolCallback callback;
        try {
            callback = (LolCallback) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen. If it does, we'll probably crash in the
            // caller
            e.printStackTrace();
            return null;
        }
        callback.mIntVal = this.mIntVal;
        callback.mFloatVal = this.mFloatVal;
        callback.mAttachedActor = this.mAttachedActor;
        callback.mCollideActor = this.mCollideActor;
        return callback;
    }

    /**
     * This code will run in response to the event for which the callback is
     * registered
     */
    abstract public void onEvent();
}
