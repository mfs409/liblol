using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Devices.Sensors;

namespace LOL
{
    public class Tilt
    {
        /**
         * Magnitude of the maximum gravity the accelerometer can create
         */
        private Vector2 mGravityMax;

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
        List<PhysicsSprite> mAccelEntities = new List<PhysicsSprite>();

        // Accelerometer readings
        protected Accelerometer accel;
        float ax, ay, az;
        
        public Tilt ()
        {
            accel = new Accelerometer();
            accel.ReadingChanged += new EventHandler<AccelerometerReadingEventArgs>(AccelUpdate);
            accel.Start();
        }

        protected void AccelUpdate (Object o, AccelerometerReadingEventArgs e)
        {
            ax = (float) e.X;
            ay = (float) e.Y;
            az = (float) e.Z;
        }

        /**
         * The main render loop calls this to determine what to do when there is a
         * phone tilt
         */
        public void handleTilt() {
            if (mGravityMax == null)
                return;

            // these temps are for storing the accelerometer forces we measure
            float xGravity = 0;
            float yGravity = 0;

            // if we're on a phone, read from the accelerometer device, taking into
            // account the rotation of the device
                
                if (Lol.Window.CurrentOrientation == DisplayOrientation.Default) {
                    xGravity = -ax;
                    yGravity = -ay;
                } else if (Lol.Window.CurrentOrientation == DisplayOrientation.LandscapeRight) {
                    xGravity = ay;
                    yGravity = -ax;
                } else if (Lol.Window.CurrentOrientation == DisplayOrientation.Portrait) {
                    xGravity = ax;
                    yGravity = ay;
                } else if (Lol.Window.CurrentOrientation == DisplayOrientation.LandscapeLeft) {
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
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(gfo.mBody.getLinearVelocity().X, yGravity);
                }
                // if Y is clipped to zero, set each entitiy's X velocity, leave Y
                // unchanged
                else if (mGravityMax.Y == 0) {
                    foreach (PhysicsSprite gfo in mAccelEntities)
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(xGravity, gfo.mBody.getLinearVelocity().Y);
                }
                // otherwise we set X and Y velocity
                else {
                    foreach (PhysicsSprite gfo in mAccelEntities)
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(xGravity, yGravity);
                }
            }
            // when not in velocity mode, apply the accelerometer reading to each
            // entity as a force
            else {
                foreach (PhysicsSprite gfo in mAccelEntities)
                    if (gfo.mBody.isActive())
                        gfo.mBody.applyForceToCenter(xGravity, yGravity, true);
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
