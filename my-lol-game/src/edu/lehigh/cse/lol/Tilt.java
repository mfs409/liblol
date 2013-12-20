
package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

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
public class Tilt {
    /*
     * INTERNAL INTERFACE
     */

    /**
     * Maximum gravity the accelerometer can create in X dimension
     */
    private float _xGravityMax;

    /**
     * Maximum gravity the accelerometer can create in Y dimension
     */
    private float _yGravityMax;

    /**
     * Track if we have an override for gravity to be translated into velocity
     */
    private boolean _tiltVelocityOverride;

    /**
     * A multiplier to make gravity change faster or slower than the
     * accelerometer default
     */
    private float _gravityMultiplier = 1;

    /**
     * Track if tilt support is turned on
     */
    private boolean _enabled;

    /**
     * List of entities that change behavior based on tilt
     */
    ArrayList<PhysicsSprite> _accelEntities = new ArrayList<PhysicsSprite>();

    /**
     * The main render loop calls this to determine what to do when there is a
     * phone tilt
     */
    void handleTilt() {
        if (!_enabled)
            return;

        // these temps are for storing the forces we measure
        float xGravity = 0;
        float yGravity = 0;

        // if we're on a phone, read from the accelerometer device, taking into
        // account the rotation of the device
        ApplicationType appType = Gdx.app.getType();
        if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {
            float rot = Gdx.input.getRotation();
            if (rot == 0) {
                xGravity = Gdx.input.getAccelerometerX();
                yGravity = -Gdx.input.getAccelerometerY();
            } else if (rot == 90) {
                xGravity = Gdx.input.getAccelerometerY();
                yGravity = -Gdx.input.getAccelerometerX();
            } else if (rot == 180) {
                xGravity = -Gdx.input.getAccelerometerX();
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
        xGravity *= _gravityMultiplier;
        yGravity *= _gravityMultiplier;

        // ensure x is within the -xGravityMax : xGravityMax range
        xGravity = (xGravity > Physics.PIXEL_METER_RATIO * _xGravityMax) ? Physics.PIXEL_METER_RATIO
                * _xGravityMax
                : xGravity;
        xGravity = (xGravity < Physics.PIXEL_METER_RATIO * -_xGravityMax) ? Physics.PIXEL_METER_RATIO
                * -_xGravityMax
                : xGravity;

        // ensure y is within the -yGravityMax : yGravityMax range
        yGravity = (yGravity > Physics.PIXEL_METER_RATIO * _yGravityMax) ? Physics.PIXEL_METER_RATIO
                * _yGravityMax
                : yGravity;
        yGravity = (yGravity < Physics.PIXEL_METER_RATIO * -_yGravityMax) ? Physics.PIXEL_METER_RATIO
                * -_yGravityMax
                : yGravity;

        // If we're in 'velocity' mode, apply the accelerometer reading to each
        // entity as a fixed velocity
        if (_tiltVelocityOverride) {
            // if X is clipped to zero, set each entity's Y velocity, leave X
            // unchanged
            if (_xGravityMax == 0) {
                for (PhysicsSprite gfo : _accelEntities)
                    if (gfo._physBody.isActive())
                        gfo.updateVelocity(gfo._physBody.getLinearVelocity().x, yGravity);
            }
            // if Y is clipped to zero, set each entitiy's X velocity, leave Y
            // unchanged
            else if (_yGravityMax == 0) {
                for (PhysicsSprite gfo : _accelEntities)
                    if (gfo._physBody.isActive())
                        gfo.updateVelocity(xGravity, gfo._physBody.getLinearVelocity().y);
            }
            // otherwise we set X and Y velocity
            else {
                for (PhysicsSprite gfo : _accelEntities)
                    if (gfo._physBody.isActive())
                        gfo.updateVelocity(xGravity, yGravity);
            }
        }
        // when not in velocity mode, apply the accelerometer reading to each
        // entity as a force
        else {
            for (PhysicsSprite gfo : _accelEntities)
                if (gfo._physBody.isActive())
                    gfo._physBody.applyForceToCenter(xGravity, yGravity, true);
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
    public static void enable(float xGravityMax, float yGravityMax) {
        Level._currLevel._tilt._enabled = true;
        Level._currLevel._tilt._xGravityMax = xGravityMax;
        Level._currLevel._tilt._yGravityMax = yGravityMax;
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
    public static void setAsVelocity(boolean toggle) {
        Level._currLevel._tilt._tiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by
     * multiplying accelerometer values by a constant.
     * 
     * @param multiplier The constant that should be multiplied by the
     *            accelerometer data
     */
    public static void setGravityMultiplier(float multiplier) {
        Level._currLevel._tilt._gravityMultiplier = multiplier;
    }
}
