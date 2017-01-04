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

import com.badlogic.gdx.math.Vector2;

/**
 * A Route specifies a set of points that an actor will move between at a fixed speed.
 */
public class Route {
    /// The X coordinates of the points in the route
    private float[] mXIndices;
    /// The Y coordinates of the points in the route
    private float[] mYIndices;
    /// The current number of points that have been set
    private int mPoints;

    /**
     * Define a new path by specifying the number of points in the path.  The points in the path
     * will be uninitialized until the "to" method is called on this Route.
     *
     * @param numberOfPoints number of points in the path.  There should be at least two points
     */
    public Route(int numberOfPoints) {
        // NB: it doesn't make sense to have a route with only one point!
        assert (numberOfPoints > 1);
        mXIndices = new float[numberOfPoints];
        mYIndices = new float[numberOfPoints];
    }

    /**
     * Add a new point to a path by giving (coordinates for where the center of the actor goes next
     *
     * @param x X value of the new coordinate
     * @param y Y value of the new coordinate
     */
    public Route to(float x, float y) {
        mXIndices[mPoints] = x;
        mYIndices[mPoints] = y;
        mPoints++;
        return this;
    }

    /**
     * Driver is an internal class, used by LOL to determine placement for a WorldActor whose motion
     * is controlled by a Route.
     */
    static class Driver {
        /// The route that is being applied
        private final Route mRoute;
        /// The actor to which the route is being applied
        private final BaseActor mActor;
        /// The speed at which the actor moves along the route
        private final float mRouteVelocity;
        /// When the actor reaches the end of the route, should it start again?
        private final boolean mRouteLoop;
        /// A temp for computing positions
        private final Vector2 mRouteVec = new Vector2();
        /// Is the route still running?
        private boolean mRouteDone;
        /// Index of the next point in the route
        private int mNextRouteGoal;

        /**
         * Constructing a route driver also starts the route
         *
         * @param route    The route to apply
         * @param velocity The speed at which the actor moves
         * @param loop     Should the route repeat when it completes?
         * @param actor    The actor to which the route should be applied
         */
        Driver(Route route, float velocity, boolean loop, BaseActor actor) {
            mRoute = route;
            mRouteVelocity = velocity;
            mRouteLoop = loop;
            mActor = actor;
            // kick off the route, indicate that we aren't all done yet
            startRoute();
            mRouteDone = false;
        }

        /**
         * Stop a route, and stop the actor too
         */
        void haltRoute() {
            mRouteDone = true;
            mActor.setAbsoluteVelocity(0, 0);
        }

        /**
         * Begin running a route
         */
        private void startRoute() {
            // move to the starting point
            mActor.mBody.setTransform(mRoute.mXIndices[0] + mActor.mSize.x / 2,
                    mRoute.mYIndices[0] + mActor.mSize.y / 2, 0);
            // set up our next goal, start moving toward it
            mNextRouteGoal = 1;
            mRouteVec.x = mRoute.mXIndices[mNextRouteGoal] - mActor.getXPosition();
            mRouteVec.y = mRoute.mYIndices[mNextRouteGoal] - mActor.getYPosition();
            // normalize and scale the vector, then apply the velocity
            mRouteVec.nor();
            mRouteVec.scl(mRouteVelocity);
            mActor.mBody.setLinearVelocity(mRouteVec);
        }

        /**
         * Figure out where we need to go next when driving a route
         */
        void drive() {
            // quit if we're done and we don't loop
            if (mRouteDone)
                return;
            // if we haven't passed the goal, keep going. we tell if we've passed the goal by
            // comparing the magnitudes of the vectors from source (s) to here and from goal (g) to
            // here
            float sx = mRoute.mXIndices[mNextRouteGoal - 1] - mActor.getXPosition();
            float sy = mRoute.mYIndices[mNextRouteGoal - 1] - mActor.getYPosition();
            float gx = mRoute.mXIndices[mNextRouteGoal] - mActor.getXPosition();
            float gy = mRoute.mYIndices[mNextRouteGoal] - mActor.getYPosition();
            boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
            boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
            if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
                mNextRouteGoal++;
                if (mNextRouteGoal == mRoute.mPoints) {
                    // reset if it's a loop, else terminate Route
                    if (mRouteLoop) {
                        startRoute();
                    } else {
                        mRouteDone = true;
                        mActor.mBody.setLinearVelocity(0, 0);
                    }
                } else {
                    // advance to next point
                    mRouteVec.x = mRoute.mXIndices[mNextRouteGoal] - mActor.getXPosition();
                    mRouteVec.y = mRoute.mYIndices[mNextRouteGoal] - mActor.getYPosition();
                    mRouteVec.nor();
                    mRouteVec.scl(mRouteVelocity);
                    mActor.mBody.setLinearVelocity(mRouteVec);
                }
            }
            // NB: 'else keep going at current velocity'
        }
    }
}
