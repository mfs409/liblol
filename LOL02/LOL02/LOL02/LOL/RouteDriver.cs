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

namespace LOL
{
    /**
     * RouteDriver is an internal class, used by LOL to determine placement for an
     * Entity whose motion is controlled by a Route.
     */
    public class RouteDriver
    {
        /**
         * The route that is being applied
         */
        private Route mRoute;

        /**
         * The entity to which the route is being applied
         */
        private PhysicsSprite mEntity;

        /**
         * The speed at which the entity moves along the route
         */
        private float mRouteVelocity;

        /**
         * When the entity reaches the end of the route, should it start again?
         */
        private bool mRouteLoop;

        /**
         * A temp for computing position
         */
        private Vector2 mRouteVec = new Vector2();

        /**
         * Is the route still running?
         */
        private bool mRouteDone;

        /**
         * Index of the next point in the route
         */
        private int mNextRouteGoal;

        /**
         * The constructor actually gets the route motion started
         * 
         * @param route The route to apply
         * @param velocity The speed at which the entity moves
         * @param loop Should the route repeat when it completes?
         * @param entity The entity to which the route should be applied
         */
        public RouteDriver(Route route, float velocity, bool loop, PhysicsSprite entity) {
            mRoute = route;
            mRouteVelocity = velocity;
            mRouteLoop = loop;
            mEntity = entity;
            // kick off the route, indicate that we aren't all done yet
            startRoute();
            mRouteDone = false;
        }

        /**
         * Stop a route
         */
        public void haltRoute() {
            mRouteDone = true;
            // NB: third parameter doesn't matter, because the entity isn't a static
            // body, so its bodytype won't change.
            mEntity.SetAbsoluteVelocity(0, 0, false);
        }

        /**
         * Begin running a route
         */
        private void startRoute() {
            // move to the starting point
            mEntity.mBody.SetTransform(new Vector2(mRoute.mXIndices[0] + mEntity.mSize.X / 2, mRoute.mYIndices[0]
                    + mEntity.mSize.Y / 2), 0);
            // set up our next goal, start moving toward it
            mNextRouteGoal = 1;
            mRouteVec.X = mRoute.mXIndices[mNextRouteGoal] - mEntity.XPosition;
            mRouteVec.Y = mRoute.mYIndices[mNextRouteGoal] - mEntity.YPosition;
            mRouteVec.Normalize();
            mRouteVec *= mRouteVelocity;
            mEntity.mBody.LinearVelocity  = mRouteVec;
        }

        /**
         * Internal method for figuring out where we need to go next when driving a
         * route
         */
        public void drive() {
            // quit if we're done and we don't loop
            if (mRouteDone)
                return;
            // if we haven't passed the goal, keep going. we tell if we've passed
            // the goal by comparing the magnitudes of the vectors from source (s)
            // to here and from goal (g) to here
            float sx = mRoute.mXIndices[mNextRouteGoal - 1] - mEntity.XPosition;
            float sy = mRoute.mYIndices[mNextRouteGoal - 1] - mEntity.YPosition;
            float gx = mRoute.mXIndices[mNextRouteGoal] - mEntity.XPosition;
            float gy = mRoute.mYIndices[mNextRouteGoal] - mEntity.YPosition;
            bool sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
            bool sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
            //System.Diagnostics.Debug.WriteLine(sx+", "+sy+" -> "+gx+", "+gy);
                
            if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
                mNextRouteGoal++;
                if (mNextRouteGoal == mRoute.mPoints) {
                    // reset if it's a loop, else terminate Route
                    if (mRouteLoop) {
                        startRoute();
                    } else {
                        mRouteDone = true;
                        mEntity.mBody.LinearVelocity = new Vector2(0, 0);
                    }
                } else {
                    // advance to next point
                    mRouteVec.X = mRoute.mXIndices[mNextRouteGoal] - mEntity.XPosition;
                    mRouteVec.Y = mRoute.mYIndices[mNextRouteGoal] - mEntity.YPosition;
                    mRouteVec.Normalize();
                    mRouteVec *= mRouteVelocity;
                    mEntity.mBody.LinearVelocity = mRouteVec;
                }
            }
            // NB: 'else keep going at current velocity'
        }

    }
}
