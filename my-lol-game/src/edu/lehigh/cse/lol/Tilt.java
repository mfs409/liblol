package edu.lehigh.cse.lol;

// TODO: clean up comments

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

public class Tilt
{
    /*
     * BASIC FUNCTIONALITY
     */

    /**
     * Maximum gravity the accelerometer can create in X dimension
     */
    static private float            _xGravityMax;

    /**
     * Maximum gravity the accelerometer can create in Y dimension
     */
    static private float            _yGravityMax;

    /**
     * List of entities that change behavior based on tilt
     */
    static ArrayList<PhysicsSprite> _accelEntities = new ArrayList<PhysicsSprite>();

    /**
     * Track if we have an override for gravity to be translated into velocity
     */
    static boolean                  _tiltVelocityOverride;

    /**
     * A multiplier to make gravity change faster or slower than the
     * accelerometer default
     */
    private static float            _gravityMultiplier;

    /**
     * Track if accel support is turned on
     */
    private static boolean          _enabled;

    /**
     * Reset tilt configuration when a new level is created
     */
    static void reset()
    {
        _tiltVelocityOverride = false;
        _xGravityMax = 0;
        _yGravityMax = 0;
        _gravityMultiplier = 1;
        // clear the stuff we explicitly manage in the _physics world
        synchronized (_accelEntities) {
            _accelEntities.clear();
        }
        _enabled = false;
    }

    /**
     * Turn on accelerometer support so that tilt can control entities in this
     * level
     * 
     * @param xGravityMax
     *            Max X force that the accelerometer can produce
     * @param yGravityMax
     *            Max Y force that the accelerometer can produce
     */
    public static void enable(float xGravityMax, float yGravityMax)
    {
        _enabled = true;
        _xGravityMax = xGravityMax;
        _yGravityMax = yGravityMax;
    }

    /**
     * This method lets us change the behavior of tilt, so that instead of
     * applying a force, we directly set the velocity of objects using the
     * accelerometer data.
     * 
     * @param toggle
     *            This should usually be false. Setting it to true means that
     *            tilt does not cause forces upon objects, but instead the tilt
     *            of the phone directly sets velocities
     */
    public static void setAsVelocity(boolean toggle)
    {
        _tiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by
     * multiplying accelerometer values by a constant.
     * 
     * @param multiplier
     *            The constant that should be multiplied by the accelerometer
     *            data
     */
    public static void setGravityMultiplier(float multiplier)
    {
        _gravityMultiplier = multiplier;
    }

    /*
     * INTERNAL SUPPORT CODE
     */

    /**
     * A helper so that we don't need pools to handle the onAccelerometerChanged
     * use of Vector2 objects
     */
    static final Vector2 _oacVec = new Vector2();

    /**
     * When there is a phone tilt, this is run to adjust the forces on objects
     * in the _current level
     * 
     * @param info
     *            The acceleration data
     */
    static void handleTilt()
    {
        if (!_enabled)
            return;

        ApplicationType appType = Gdx.app.getType();

        float xGravity = 0;
        float yGravity = 0;

        // should work also with
        // Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)
        if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {

            // TODO: test this code in portrait mode, and test if upside-down
            // screens work (landscape and portrait)
            float rot = Gdx.input.getRotation();
            if (rot == 0) {
                xGravity = Gdx.input.getAccelerometerX();
                yGravity = -Gdx.input.getAccelerometerY();
            }
            else if (rot == 90) {
                xGravity = Gdx.input.getAccelerometerY();
                yGravity = -Gdx.input.getAccelerometerX();
            }
            else if (rot == 180) {
                xGravity = -Gdx.input.getAccelerometerX();
                yGravity = Gdx.input.getAccelerometerY();
            }
            else if (rot == 270) {
                xGravity = -Gdx.input.getAccelerometerY();
                yGravity = Gdx.input.getAccelerometerX();
            }
        }
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

        // get gravity from accelerometer
        // TODO: there is a hidden "times 10" multiplier in here... comment
        // it...
        xGravity *= _gravityMultiplier;
        yGravity *= _gravityMultiplier;

        // ensure -10 <= x <= 10
        xGravity = (xGravity > 10 * _xGravityMax) ? 10 * _xGravityMax : xGravity;
        xGravity = (xGravity < 10 * -_xGravityMax) ? 10 * -_xGravityMax : xGravity;

        // ensure -10 <= y <= 10
        yGravity = (yGravity > 10 * _yGravityMax) ? 10 * _yGravityMax : yGravity;
        yGravity = (yGravity < 10 * -_yGravityMax) ? 10 * -_yGravityMax : yGravity;

        synchronized (_accelEntities) {
            if (_tiltVelocityOverride) {
                // we need to be careful here... if we have a zero for the X or
                // Y
                // gravityMax, then in that dimension we should not just set
                // linear
                // velocity to the value we compute, or jumping won't work

                // we're going to assume that you wouldn't have xGravityMax ==
                // yGravityMax == 0

                if (_xGravityMax == 0) {
                    // Send the new gravity information to the _physics system
                    // by
                    // changing the velocity of each object
                    for (PhysicsSprite gfo : _accelEntities) {
                        if (gfo._physBody.isActive())
                            gfo.updateVelocity(gfo._physBody.getLinearVelocity().x, yGravity);
                    }
                }
                else if (_yGravityMax == 0) {
                    // Send the new gravity information to the _physics system
                    // by
                    // changing the velocity of each object
                    for (PhysicsSprite gfo : _accelEntities) {
                        if (gfo._physBody.isActive())
                            gfo.updateVelocity(xGravity, gfo._physBody.getLinearVelocity().y);
                    }
                }
                else {
                    // Send the new gravity information to the _physics system
                    // by
                    // changing the velocity of each object
                    for (PhysicsSprite gfo : _accelEntities) {
                        if (gfo._physBody.isActive())
                            gfo.updateVelocity(xGravity, yGravity);
                    }
                }
            }
            else {
                // Send the new gravity information to the _physics system by
                // applying a force to each object
                _oacVec.set(xGravity, yGravity);
                for (PhysicsSprite gfo : _accelEntities) {
                    if (gfo._physBody.isActive())
                        gfo._physBody.applyForce(_oacVec, gfo._physBody.getWorldCenter(), true);
                    // TODO: handle reverse face
                    // gfo.reverseFace(xGravity < 0);
                }
            }
        }
    }

}
