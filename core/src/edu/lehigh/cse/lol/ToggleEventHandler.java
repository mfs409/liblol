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
 * ToggleEventHandler is a wrapper for code that ought to run in response to a toggle event.
 */
public abstract class ToggleEventHandler {
    /// A flag to control whether the event is allowed to execute or not
    public boolean mIsActive = true;

    /// The actor who generated this touch event
    public BaseActor mSource = null;

    /// A flag to track if we are in a 'hold' state
    public boolean isHolding = false;

    /**
     * The go() method encapsulates the code that should be run in response to a toggle event.
     *
     * @param isUp           True of the source is being released, false otherwise
     * @param eventPositionX The screen X coordinate of the touch
     * @param eventPositionY The screen Y coordinate of the touch
     */
    abstract public boolean go(boolean isUp, float eventPositionX, float eventPositionY);
}
