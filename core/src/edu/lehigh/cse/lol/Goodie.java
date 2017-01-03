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

import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Goodies are actors that a hero can collect.
 * <p>
 * Collecting a goodie has three possible consequences: it can change the score, it can change the
 * hero's strength, and it can make the hero invincible
 */
public class Goodie extends WorldActor {
    /// The "score" of this goodie... it is the amount that will be added to the score when the
    /// goodie is collected. This is different than a hero's strength because this actually bumps
    /// the score, which in turn lets us have "super goodies" that turn on callback obstacles.
    int[] mScore = new int[4];
    ///  How much strength does the hero get by collecting this goodie
    int mStrengthBoost = 0;
    ///  How long will the hero be invincible if it collects this goodie
    float mInvincibilityDuration = 0;

    /**
     * Create a basic Goodie.  The goodie won't yet have any physics attached to it.
     *
     * @param game    The currently active game
     * @param scene   The scene into which the destination is being placed
     * @param width   width of this Goodie
     * @param height  height of this Goodie
     * @param imgName image to use for this Goodie
     */
    Goodie(Lol game, MainScene scene, float width, float height, String imgName) {
        super(game, scene, imgName, width, height);
        mScore[0] = 1;
        mScore[1] = 0;
        mScore[2] = 0;
        mScore[3] = 0;
    }

    /**
     * Code to run when a Goodie collides with a WorldActor.
     * <p>
     * NB: Goodies are at the end of the collision hierarchy, so we don't do anything when
     * they are in a collision that hasn't already been handled by a higher-ranked WorldActor.
     *
     * @param other   Other object involved in this collision
     * @param contact A description of the contact that caused this collision
     */
    @Override
    void onCollide(WorldActor other, Contact contact) {
    }

    /**
     * Set the score of this goodie.
     *
     * This indicates how many points the goodie is worth... each value can be positive or negative
     *
     * @param v1 The number of points that are added to the 1st score when the goodie is collected
     * @param v2 The number of points that are added to the 2nd score when the goodie is collected
     * @param v3 The number of points that are added to the 3rd score when the goodie is collected
     * @param v4 The number of points that are added to the 4th score when the goodie is collected
     */
    public void setScore(int v1, int v2, int v3, int v4) {
        mScore[0] = v1;
        mScore[1] = v2;
        mScore[2] = v3;
        mScore[3] = v4;
    }

    /**
     * Indicate how much strength the hero gains by collecting this goodie
     *
     * @param boost Amount of strength to add (can be negative)
     */
    public void setStrengthBoost(int boost) {
        mStrengthBoost = boost;
    }

    /**
     * Indicate how long the hero will be invincible after collecting this goodie
     *
     * @param duration Amount of time the hero will be invincible. Note that for a hero who is
     *                 currently invincible, this value will be /added/ to the hero's remaining
     *                 invincibility time
     */
    public void setInvincibilityDuration(float duration) {
        if (duration < 0)
            duration = 0;
        mInvincibilityDuration = duration;
    }
}
