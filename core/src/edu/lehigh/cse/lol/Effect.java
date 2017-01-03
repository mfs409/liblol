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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Effects provide a way of describing visual artifacts on the screen that do not have a physics
 * aspect.  An Effect is really just a wrapper around a LibGDX particle system.
 */
public class Effect extends Renderable {
    /// The active particle system
    ParticleEffect mParticleEffect;
    /// Should the effect repeat after it has run its course?
    private boolean mRepeat;
    /// Is the effect active?
    private boolean mEnabled = true;

    /**
     * Construct an effect.  The constructor is not public, so that programmers will have to use
     * factory methods to actually create an effect.
     */
    Effect() {
    }

    /**
     * Every time the world advances by a timestep, we call this code to update the particle effect
     * and draw it.
     */
    @Override
    void onRender(SpriteBatch sb, float delta) {
        // do nothing if the effect is disabled
        if (!mEnabled)
            return;
        // render a particle effect
        if (mParticleEffect != null) {
            mParticleEffect.update(Gdx.graphics.getDeltaTime());
            mParticleEffect.draw(sb);
            // deal with effect completion
            if (mParticleEffect.isComplete())
                if (mRepeat)
                    mParticleEffect.reset();
                else
                    mEnabled = false;
        }
    }

    /**
     * Indicate whether the effect should repeat after it completes, or if it should stop
     *
     * @param repeat True if the effect should repeat, false otherwise
     */
    public void setRepeat(boolean repeat) {
        mRepeat = repeat;
    }

    /**
     * Indicate whether the effect is active right now or not
     *
     * @param enabled True to enable the effect, false otherwise
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    /**
     * Reset the effect, so that it starts over
     */
    public void reset() {
        if (mParticleEffect != null)
            mParticleEffect.reset();
    }
}
