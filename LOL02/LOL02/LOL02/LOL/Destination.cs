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
using Microsoft.Xna.Framework.Audio;
using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    /**
     * Destinations are entities that the hero should try to reach. When a hero
     * reaches a destination, the hero disappears, and the score updates.
     */
    public class Destination : PhysicsSprite
    {
        /**
         * number of heroes who can fit at /this/ destination
         */
        internal int mCapacity;

        /**
         * number of heroes already in /this/ destination
         */
        internal int mHolding;

        /**
         * number of type each type of goodies that must be collected before this
         * destination accepts any heroes
         */
        internal int[] mActivation = new int[4];

        /**
         * Sound to play when a hero arrives at this destination
         */
        internal SoundEffect mArrivalSound;

        /**
         * Create a destination. This is an internal method, the game designer
         * should not use this.
         * 
         * @param width Width of this destination
         * @param height Height of this destination
         * @param imgName Name of the image to display
         */
        private Destination(float width, float height, string imgName) : base(imgName, width, height)
        {
            mCapacity = 1;
            mHolding = 0;
        }

        /**
         * Destinations are the last collision detection entity, so their collision
         * detection code does nothing.
         * 
         * @param other Other object involved in this collision
         * @param contact A description of the collision between this destination
         *            and the other entity
         */
        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
            
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Make a destination that has an underlying rectangular shape.
         * 
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom right corner
         * @param width The width of the destination
         * @param height The height of the destination
         * @param imgName The name of the image to display
         * @return The destination, so that it can be modified further
         */
        public static Destination MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Destination d = new Destination(width, height, imgName);
            d.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            d.CollisionEffect = false;
            Level.sCurrent.addSprite(d, 0);
            return d;
        }

        /**
         * Make a destination that has an underlying circular shape.
         * 
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom right corner
         * @param width The width of the destination
         * @param height The height of the destination
         * @param imgName The name of the image to display
         * @return The destination, so that it can be modified further
         */
        public static Destination MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Destination d = new Destination(width, height, imgName);
            d.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            d.CollisionEffect = false;
            Level.sCurrent.addSprite(d, 0);
            return d;
        }

        /**
         * Change the number of goodies that must be collected before the
         * destination accepts any heroes (the default is 0,0,0,0)
         * 
         * @param score1 The number of type-1 goodies that must be collected.
         * @param score2 The number of type-2 goodies that must be collected.
         * @param score3 The number of type-3 goodies that must be collected.
         * @param score4 The number of type-4 goodies that must be collected.
         */
        public void SetActivationScore(int score1, int score2, int score3, int score4)
        {
            mActivation[0] = score1;
            mActivation[1] = score2;
            mActivation[2] = score3;
            mActivation[3] = score4;
        }

        /**
         * Change the number of heroes that can be accepted by this destination (the
         * default is 1)
         */
        public int HeroCount
        {
            set { mCapacity = value; }
        }

        /**
         * Specify the sound to play when a hero arrives at this destination
         */
        public string ArrivalSound
        {
            set { mArrivalSound = Media.getSound(value); }
        }
    }
}
