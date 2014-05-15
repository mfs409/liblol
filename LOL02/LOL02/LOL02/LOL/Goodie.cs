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

using System;
using System.Diagnostics;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    /**
     * Goodies are physical entities in a game whose main purpose is for the hero to
     * collect them. Collecting a goodie has three possible consequences: it can
     * lead to the score changing, it can lead to the hero's strength changing, and
     * it can lead to the hero becoming invincible for some time.
     */
    public class Goodie : PhysicsSprite
    {
        /**
         * The "score" of this goodie... it is the amount that will be added to the
         * score when this goodie is collected. This is different than a hero's
         * strength because this actually bumps the score, which in turn lets us
         * have "super goodies" that turn on trigger obstacles.
         */
        internal int[] mScore = new int[4];

        /**
         * How much strength does the hero get by collecting this goodie
         */
        internal int mStrengthBoost = 0;

        /**
         * How long will the hero be invincible if it collects this goodie
         */
        internal double mInvincibilityDuration;

        /**
         * Build a Goodie This should never be invoked directly. Instead, LOL game
         * designers should use the makeAsXYZ methods
         * 
         * @param x X position of bottom left corner
         * @param y Y position of bottom left corner
         * @param width width of this Obstacle
         * @param height height of this Obstacle
         * @param tr image to use for this Obstacle
         */
        private Goodie(float width, float height, string imgName) : base(imgName, width, height)
        {
            mScore[0] = 1;
            mScore[1] = 0;
            mScore[2] = 0;
            mScore[3] = 0;
        }

        /**
         * Internal method: Goodie collision is always handled by the other entity
         * involved in the collision, so we leave this method blank
         * 
         * @param other Other object involved in this collision
         * @param contact A description of the contact that caused this collision
         */
        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Draw a goodie with an underlying box shape, and a default score of
         * [1,0,0,0]
         * 
         * @param x X coordinate of bottom left corner
         * @param y Y coordinate of bottom left corner
         * @param width Width of the image
         * @param height Height of the image
         * @param imgName Name of image file to use
         * @return The goodie, so that it can be further modified
         */
        public static Goodie MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Goodie g = new Goodie(width, height, imgName);
            g.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            g.CollisionEffect = false;
            Level.sCurrent.addSprite(g, 0);
            return g;
        }

        /**
         * Draw a goodie with an underlying circle shape, and a default score of
         * [1,0,0,0]
         * 
         * @param x X coordinate of bottom left corner
         * @param y Y coordinate of bottom left corner
         * @param width Width of the image
         * @param height Height of the image
         * @param imgName Name of image file to use
         * @return The goodie, so that it can be further modified
         */
        public static Goodie MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Goodie g = new Goodie(width, height, imgName);
            g.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            g.CollisionEffect = false;
            Level.sCurrent.addSprite(g, 0);
            return g;
        }

        /**
         * Set the score of this goodie. This indicates how many points the goodie
         * is worth... each value can be positive or negative
         * 
         * @param v1 The number of points that are added to the first score when the
         *            goodie is collected
         * @param v2 The number of points that are added to the second score when
         *            the goodie is collected
         * @param v3 The number of points that are added to the third score when the
         *            goodie is collected
         * @param v4 The number of points that are added to the fourth score when
         *            the goodie is collected
         */
        public void SetScore(int v1, int v2, int v3, int v4)
        {
            mScore[0] = v1;
            mScore[1] = v2;
            mScore[2] = v3;
            mScore[3] = v4;
        }

        /**
         * Indicate how much strength the hero gains by collecting this goodie
         * 
         * @note Can be negative
         */
        public int StrengthBoost
        {
            set { mStrengthBoost = value; }
        }

        /**
         * Indicate how long the hero will be invincible after collecting this
         * goodie
         * 
         * @note For a hero who is currently invincible, this value will be /added/
         *       to the hero's remaining invincibility time
         */
        public double InvicibilityDuration
        {
            set
            {
                Debug.Assert(value >= 0);
                mInvincibilityDuration = value;
            }
        }
    }
}
