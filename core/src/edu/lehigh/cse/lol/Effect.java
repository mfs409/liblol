package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Effects provide a way of describing visual artifacts on the screen
 * that do not have a physics aspect, but which otherwise fit into the
 * physics world
 */
public class Effect implements Renderable {
    /**
     * If this effect is a Particle System, then mParticleEffect is the object
     * that describes the effect
     */
    ParticleEffect mParticleEffect;

    /**
     * Should the effect repeat after it has run its course?
     */
    private boolean mRepeat;

    /**
     * Is the effect active?
     */
    private boolean mEnabled = true;

    /**
     * By default, the programmer cannot create effects directly.  Instead, use the create...()
     * factory methods.
     */
    Effect(){};

    /*
     * OVERRIDES FROM LOL.RENDERABLE
     */

    /**
     * Every time the world advances by a timestep, we call this code. It
     * updates the particle effect and draws it.
     *
     * NB: User code should never call this.
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
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

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Indicate whether the effect should repeat after it completes, or if it should stop
     * @param repeat
     */
    public void setRepeat(boolean repeat) {
        mRepeat = repeat;
    }

    /**
     * Indicate whether the effect is active right now or not
     * @param enabled True to enableTilt the effect, false otherwise
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