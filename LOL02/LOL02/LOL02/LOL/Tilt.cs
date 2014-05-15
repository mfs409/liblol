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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Devices.Sensors;

namespace LOL
{
    /**
     * Tilt provides a mechanism for moving entities on the screen. To use tilt, you
     * must enable() it for a level, and also indicate that some entities move via
     * tilting. Tilt has two flavors: tilt can cause gravitational effects, where a
     * sustained tilt causes acceleration (this is the default), or it can cause
     * sprites to move with a fixed velocity. Be careful when using tilt. Different
     * phones' accelerometers vary in terms of sensitivity. It is possible to set
     * multipliers and/or caps on the effect of Tilt, but these may not suffice to
     * make your game playable and enjoyable.
     */
    public class Tilt
    {
        /**
         * Magnitude of the maximum gravity the accelerometer can create
         */
        private Vector2 mGravityMax = Vector2.Zero;

        /**
         * Track if we have an override for gravity to be translated into velocity
         */
        private bool mTiltVelocityOverride;

        /**
         * A multiplier to make gravity change faster or slower than the
         * accelerometer default
         */
        private float mMultiplier = 1;

        /**
         * List of entities that change behavior based on tilt
         */
        public List<PhysicsSprite> mAccelEntities = new List<PhysicsSprite>();

        /**
         * Accelerometer input
         */
        protected Accelerometer accel;

        /**
         * Accelerometer readings
         */
        float ax = 0, ay = 0, az = 0;
        
        /**
         * Constructor
         */
        public Tilt ()
        {
            accel = new Accelerometer();
            accel.CurrentValueChanged += new EventHandler<SensorReadingEventArgs<AccelerometerReading>>(AccelUpdate);
        }

        /**
         * Called when the Accelerometer values change
         * @param o the sending object
         * @param e the accelerometer readings
         */
        protected void AccelUpdate (Object o, SensorReadingEventArgs<AccelerometerReading> e)
        {
            ax = (float) e.SensorReading.Acceleration.X;
            ay = (float) e.SensorReading.Acceleration.Y;
            az = (float) e.SensorReading.Acceleration.Z;
        }

        /**
         * The main render loop calls this to determine what to do when there is a
         * phone tilt
         */
        public void handleTilt() {
            if (mGravityMax == Vector2.Zero)
                return;

            // these temps are for storing the accelerometer forces we measure
            float xGravity = 0;
            float yGravity = 0;

            // if we're on a phone, read from the accelerometer device, taking into
            // account the rotation of the device
                
                if (Lol.sGame.Window.CurrentOrientation == DisplayOrientation.Default) {
                    xGravity = -ax;
                    yGravity = -ay;
                } else if (Lol.sGame.Window.CurrentOrientation == DisplayOrientation.LandscapeRight) {
                    xGravity = ay;
                    yGravity = -ax;
                } else if (Lol.sGame.Window.CurrentOrientation == DisplayOrientation.Portrait) {
                    xGravity = ax;
                    yGravity = ay;
                } else if (Lol.sGame.Window.CurrentOrientation == DisplayOrientation.LandscapeLeft) {
                    xGravity = -ay;
                    yGravity = ax;
                }
            //}

            // if we're on a computer, we simulate tilt with the arrow keys
            // TODO: Use #if WINDOWS or macros to determine if code is a desktop app or phone app
            /*else {
                if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT))
                    xGravity = -15f;
                else if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT))
                    xGravity = 15f;
                else if (Gdx.input.isKeyPressed(Keys.DPAD_UP))
                    yGravity = 15f;
                else if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN))
                    yGravity = -15f;
            }*/

            // Apply the gravity multiplier
            xGravity *= mMultiplier;
            yGravity *= mMultiplier;

            // ensure x is within the -GravityMax.x : GravityMax.x range
            xGravity = (xGravity > Physics.PIXEL_METER_RATIO * mGravityMax.X) ? Physics.PIXEL_METER_RATIO
                    * mGravityMax.X
                    : xGravity;
            xGravity = (xGravity < Physics.PIXEL_METER_RATIO * -mGravityMax.X) ? Physics.PIXEL_METER_RATIO
                    * -mGravityMax.X
                    : xGravity;

            // ensure y is within the -GravityMax.y : GravityMax.y range
            yGravity = (yGravity > Physics.PIXEL_METER_RATIO * mGravityMax.Y) ? Physics.PIXEL_METER_RATIO
                    * mGravityMax.Y
                    : yGravity;
            yGravity = (yGravity < Physics.PIXEL_METER_RATIO * -mGravityMax.Y) ? Physics.PIXEL_METER_RATIO
                    * -mGravityMax.Y
                    : yGravity;

            // If we're in 'velocity' mode, apply the accelerometer reading to each
            // entity as a fixed velocity
            if (mTiltVelocityOverride) {
                // if X is clipped to zero, set each entity's Y velocity, leave X
                // unchanged
                if (mGravityMax.X == 0) {
                    foreach (PhysicsSprite gfo in mAccelEntities)
                        if (gfo.mBody.Awake)
                            gfo.UpdateVelocity(gfo.mBody.LinearVelocity.X, yGravity);
                }
                // if Y is clipped to zero, set each entitiy's X velocity, leave Y
                // unchanged
                else if (mGravityMax.Y == 0) {
                    foreach (PhysicsSprite gfo in mAccelEntities)
                        if (gfo.mBody.Awake)
                            gfo.UpdateVelocity(xGravity, gfo.mBody.LinearVelocity.Y);
                }
                // otherwise we set X and Y velocity
                else {
                    foreach (PhysicsSprite gfo in mAccelEntities)
                        if (gfo.mBody.Awake)
                            gfo.UpdateVelocity(xGravity, yGravity);
                }
            }
            // when not in velocity mode, apply the accelerometer reading to each
            // entity as a force
            else {
                foreach (PhysicsSprite gfo in mAccelEntities)
                {
                    gfo.AddVelocity(xGravity, yGravity, true);
                    if (gfo.mBody.Awake)
                        gfo.mBody.ApplyForce(new Vector2(xGravity, yGravity));
                }
            }
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Turn on accelerometer support so that tilt can control entities in this
         * level
         * 
         * @param xGravityMax Max X force that the accelerometer can produce
         * @param yGravityMax Max Y force that the accelerometer can produce
         */
        public static void enable(float xGravityMax, float yGravityMax)
        {
            Level.sCurrent.mTilt.mGravityMax = new Vector2(xGravityMax, yGravityMax);
            Level.sCurrent.mTilt.accel.Start();
        }

        /**
         * This method lets us change the behavior of tilt, so that instead of
         * applying a force, we directly set the velocity of objects using the
         * accelerometer data.
         * 
         * @param toggle This should usually be false. Setting it to true means that
         *            tilt does not cause forces upon objects, but instead the tilt
         *            of the phone directly sets velocities
         */
        public static void setAsVelocity(bool toggle)
        {
            Level.sCurrent.mTilt.mTiltVelocityOverride = toggle;
        }

        /**
         * Use this to make the accelerometer more or less responsive, by
         * multiplying accelerometer values by a constant.
         * 
         * @param multiplier The constant that should be multiplied by the
         *            accelerometer data. This can be a fraction, like 0.5f, to make
         *            the accelerometer less sensitive
         */
        public static void setGravityMultiplier(float multiplier)
        {
            Level.sCurrent.mTilt.mMultiplier = multiplier;
        }
    }
}
