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

/**
 * SimpleCallback provides an easy way to describe code that should run in
 * response to special events, such as timers, screen presses, or collisions
 * between entities.
 * 
 * In the interest of simplicity, we use some public fields instead of getters
 * and setters. This also lets us get away with using the default constructor.
 */
public class SimpleCallback {
    /**
     * An integer that may be of use during the callback
     */
    public int intVal;

    /**
     * A float that may be of use during the callback
     */
    public float val;

    /**
     * For collision events or entity press events, this is the entity to which
     * the callback is attacheed
     */
    public PhysicsSprite attachedSprite;

    /**
     * For collision events, this is the other entity involved in the collision
     */
    public PhysicsSprite collideSprite;

    /**
     * This code will run in response to the event for which the callback is
     * registered
     */
    public void onEvent() {
    }
}
