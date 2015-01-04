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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Tilt provides a mechanism for moving actors on the screen. To use tilt, you
 * must enable it for a level, and also indicate that some actors move via
 * tilting. Tilt has two flavors: tilt can cause gravitational effects, where a
 * sustained tilt causes acceleration (this is the default), or it can cause
 * actors to move with a fixed velocity. Be careful when using tilt. Different
 * phones' accelerometers vary in terms of sensitivity. It is possible to set
 * multipliers and/or caps on the effect of Tilt, but these may not suffice to
 * make your game playable and enjoyable.
 */
public class Tilt {
    /**
     * List of actors that change behavior based on tilt
     */
    ArrayList<Actor> mAccelActors = new ArrayList<>();
    /**
     * Magnitude of the maximum gravity the accelerometer can create
     */
    private Vector2 mGravityMax;
    /**
     * Track if we have an override for gravity to be translated into velocity
     */
    private boolean mTiltVelocityOverride;
    /**
     * A multiplier to make gravity change faster or slower than the
     * accelerometer default
     */
    private float mMultiplier = 1;

    /**
     * Turn on accelerometer support so that tilt can control actors in this
     * level
     *
     * @param xGravityMax Max X force that the accelerometer can produce
     * @param yGravityMax Max Y force that the accelerometer can produce
     */
    public static void enable(float xGravityMax, float yGravityMax) {
        Lol.sGame.mCurrentLevel.mTilt.mGravityMax = new Vector2(xGravityMax, yGravityMax);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * This method lets us change the behavior of tilt, so that instead of
     * applying a force, we directly set the velocity of objects using the
     * accelerometer data.
     *
     * @param toggle This should usually be false. Setting it to true means that
     *               tilt does not cause forces upon objects, but instead the tilt
     *               of the phone directly sets velocities
     */
    public static void setAsVelocity(boolean toggle) {
        Lol.sGame.mCurrentLevel.mTilt.mTiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by
     * multiplying accelerometer values by a constant.
     *
     * @param multiplier The constant that should be multiplied by the accelerometer
     *                   data. This can be a fraction, like 0.5f, to make the
     *                   accelerometer less sensitive
     */
    public static void setGravityMultiplier(float multiplier) {
        Lol.sGame.mCurrentLevel.mTilt.mMultiplier = multiplier;
    }

    /**
     * The main render loop calls this to determine what to do when there is a
     * phone tilt
     */
    void handleTilt() {
        if (mGravityMax == null)
            return;

        // these temps are for storing the accelerometer forces we measure
        float xGravity = 0;
        float yGravity = 0;

        // if we're on a phone, read from the accelerometer device, taking into
        // account the rotation of the device
        ApplicationType appType = Gdx.app.getType();
        if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {
            float rot = Gdx.input.getRotation();
            if (rot == 0) {
                xGravity = -Gdx.input.getAccelerometerX();
                yGravity = -Gdx.input.getAccelerometerY();
            } else if (rot == 90) {
                xGravity = Gdx.input.getAccelerometerY();
                yGravity = -Gdx.input.getAccelerometerX();
            } else if (rot == 180) {
                xGravity = Gdx.input.getAccelerometerX();
                yGravity = Gdx.input.getAccelerometerY();
            } else if (rot == 270) {
                xGravity = -Gdx.input.getAccelerometerY();
                yGravity = Gdx.input.getAccelerometerX();
            }
        }
        // if we're on a computer, we simulate tilt with the arrow keys
        else {
            if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT))
                xGravity = -15f;
            else if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT))
                xGravity = 15f;
            else if (Gdx.input.isKeyPressed(Keys.DPAD_UP))
                yGravity = 15f;
            else if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN))
                yGravity = -15f;
        }

        // Apply the gravity multiplier
        xGravity *= mMultiplier;
        yGravity *= mMultiplier;

        // ensure x is within the -GravityMax.x : GravityMax.x range
        xGravity = (xGravity > Physics.PIXEL_METER_RATIO * mGravityMax.x) ? Physics.PIXEL_METER_RATIO * mGravityMax.x
                : xGravity;
        xGravity = (xGravity < Physics.PIXEL_METER_RATIO * -mGravityMax.x) ? Physics.PIXEL_METER_RATIO * -mGravityMax.x
                : xGravity;

        // ensure y is within the -GravityMax.y : GravityMax.y range
        yGravity = (yGravity > Physics.PIXEL_METER_RATIO * mGravityMax.y) ? Physics.PIXEL_METER_RATIO * mGravityMax.y
                : yGravity;
        yGravity = (yGravity < Physics.PIXEL_METER_RATIO * -mGravityMax.y) ? Physics.PIXEL_METER_RATIO * -mGravityMax.y
                : yGravity;

        // If we're in 'velocity' mode, apply the accelerometer reading to each
        // actor as a fixed velocity
        if (mTiltVelocityOverride) {
            // if X is clipped to zero, set each actor's Y velocity, leave X
            // unchanged
            if (mGravityMax.x == 0) {
                for (Actor gfo : mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(gfo.mBody.getLinearVelocity().x, yGravity);
            }
            // if Y is clipped to zero, set each actor's X velocity, leave Y
            // unchanged
            else if (mGravityMax.y == 0) {
                for (Actor gfo : mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(xGravity, gfo.mBody.getLinearVelocity().y);
            }
            // otherwise we set X and Y velocity
            else {
                for (Actor gfo : mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(xGravity, yGravity);
            }
        }
        // when not in velocity mode, apply the accelerometer reading to each
        // actor as a force
        else {
            for (Actor gfo : mAccelActors)
                if (gfo.mBody.isActive())
                    gfo.mBody.applyForceToCenter(xGravity, yGravity, true);
        }
    }
}
