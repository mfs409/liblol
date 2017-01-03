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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renderable is the base of all objects that can be displayed on the screen.  At its most simple
 * level, a Renderable is simply a function (<code>onRender</code>), and a flag to indicate whether
 * the object is currently active and enabled, or disabled.
 */
abstract class Renderable {
    /// Track if the object is currently allowed to be rendered. This is a proxy for "is important
    /// to the rest of the game" and when it is false, we don't run any updates on the object
    boolean mEnabled = true;

    /**
     * Specify whether this Renderable object is enabled or disabled.  When it is disabled, it
     * effectively does not exist in the game.
     *
     * @param val The new state (true for enabled, false for disabled)
     */
    public void setEnabled(boolean val) {
        mEnabled = val;
    }

    /**
     * Return the current enabled/disabled state of this Renderable
     *
     * @return The state of the renderable
     */
    public boolean getEnabled() {
        return mEnabled;
    }

    /**
     * Render something to the screen.  This doesn't do the actual rendering, instead it forwards
     * to the onRender function, but only if the object is enabled.
     *
     * @param sb      The SpriteBatch to use for rendering
     * @param elapsed The time since the last render
     */
    void render(SpriteBatch sb, float elapsed) {
        if (!mEnabled)
            return;
        onRender(sb, elapsed);
    }

    /**
     * User-provided code to run when a renderable object is enabled and ready to be rendered.
     *
     * @param sb      The SpriteBatch to use for rendering
     * @param elapsed The time since the last render
     */
    abstract void onRender(SpriteBatch sb, float elapsed);
}
