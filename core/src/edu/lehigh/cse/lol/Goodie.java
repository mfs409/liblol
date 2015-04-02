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

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Goodies are actors in a game whose main purpose is for the hero to collect
 * them. Collecting a goodie has three possible consequences: it can lead to the
 * score changing, it can lead to the hero's strength changing, and it can lead
 * to the hero becoming invincible for some time.
 */
public class Goodie extends Actor {
    /**
     * The "score" of this goodie... it is the amount that will be added to the
     * score when this goodie is collected. This is different than a hero's
     * strength because this actually bumps the score, which in turn lets us
     * have "super goodies" that turn on callback obstacles.
     */
    int[] mScore = new int[4];

    /**
     * How much strength does the hero get by collecting this goodie
     */
    int mStrengthBoost = 0;

    /**
     * How long will the hero be invincible if it collects this goodie
     */
    float mInvincibilityDuration = 0;

    /**
     * Build a Goodie This should never be invoked directly. Instead, LOL game
     * designers should use the makeAsXYZ methods
     *
     * @param width   width of this Obstacle
     * @param height  height of this Obstacle
     * @param imgName image to use for this Obstacle
     */
    protected Goodie(float width, float height, String imgName) {
        super(imgName, width, height);
        mScore[0] = 1;
        mScore[1] = 0;
        mScore[2] = 0;
        mScore[3] = 0;
    }

    /**
     * Draw a goodie with an underlying box shape, and a default score of
     * [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public static Goodie makeAsBox(float x, float y, float width, float height, String imgName) {
        Goodie g = new Goodie(width, height, imgName);
        g.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        g.setCollisionsEnabled(false);
        Lol.sGame.mCurrentLevel.addActor(g, 0);
        return g;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Draw a goodie with an underlying circle shape, and a default score of
     * [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public static Goodie makeAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Goodie g = new Goodie(width, height, imgName);
        g.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        g.setCollisionsEnabled(false);
        Lol.sGame.mCurrentLevel.addActor(g, 0);
        return g;
    }

    /**
     * Draw a goodie with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The goodie, so that it can be further modified
     */
    public static Goodie makeAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Goodie g = new Goodie(width, height, imgName);
        g.setPolygonPhysics(0, 0, 0, BodyType.StaticBody, false, x, y, verts);
        g.setCollisionsEnabled(false);
        Lol.sGame.mCurrentLevel.addActor(g, 0);
        return g;
    }

    /**
     * Internal method: Goodie collision is always handled by the other actor
     * involved in the collision, so we leave this method blank
     *
     * @param other   Other object involved in this collision
     * @param contact A description of the contact that caused this collision
     */
    @Override
    void onCollide(Actor other, Contact contact) {
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie
     * is worth... each value can be positive or negative
     *
     * @param v1 The number of points that are added to the first score when
     *           the goodie is collected
     * @param v2 The number of points that are added to the second score when
     *           the goodie is collected
     * @param v3 The number of points that are added to the third score when
     *           the goodie is collected
     * @param v4 The number of points that are added to the fourth score when
     *           the goodie is collected
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
     * Indicate how long the hero will be invincible after collecting this
     * goodie
     *
     * @param duration Amount of time the hero will be invincible. Note that for a
     *                 hero who is currently invincible, this value will be /added/
     *                 to the hero's remaining invincibility time
     */
    public void setInvincibilityDuration(float duration) {
        assert (duration >= 0);
        mInvincibilityDuration = duration;
    }
}
