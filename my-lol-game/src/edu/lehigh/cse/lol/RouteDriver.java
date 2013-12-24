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

import com.badlogic.gdx.math.Vector2;

class RouteDriver {
    Route mMyRoute;

    float mRouteVelocity;

    boolean mRouteLoop;

    Vector2 mRouteVec = new Vector2();

    boolean mRouteDone;

    int mNextRouteGoal;

    PhysicsSprite mEntity;

    void haltRoute() {
        mRouteDone = true;
        // NB: third parameter doesn't matter, because the entity isn't a static
        // body, so its bodytype won't change.
        mEntity.setAbsoluteVelocity(0, 0, false);
    }

    RouteDriver(Route route, float velocity, boolean loop, PhysicsSprite entity) {
        mMyRoute = route;
        mRouteVelocity = velocity;
        mRouteLoop = loop;
        mEntity = entity;

        // this is how we initialize a route driver:
        // first, move to the starting point
        mEntity.mBody.setTransform(mMyRoute.mXIndices[0] + mEntity.mWidth / 2,
                mMyRoute.mYIndices[0] + mEntity.mHeight / 2, 0);
        // second, indicate that we are working on goal #1, and set velocity
        startRoute();
        // and indicate that we aren't all done yet
        mRouteDone = false;

    }

    void startRoute() {
        mNextRouteGoal = 1;
        mRouteVec.x = mMyRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
        mRouteVec.y = mMyRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
        mRouteVec.nor();
        mRouteVec.scl(mRouteVelocity);
        mEntity.mBody.setLinearVelocity(mRouteVec);
    }

    /**
     * Internal method for figuring out where we need to go next when driving a
     * route
     */
    void drive() {
        // quit if we're done and we don't loop
        if (mRouteDone)
            return;
        // if we haven't passed the goal, keep going. we tell if we've passed
        // the goal by comparing the magnitudes
        // of
        // the vectors from source to here and from goal to here
        float sx = mMyRoute.mXIndices[mNextRouteGoal - 1] - mEntity.getXPosition();
        float sy = mMyRoute.mYIndices[mNextRouteGoal - 1] - mEntity.getYPosition();
        float gx = mMyRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
        float gy = mMyRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
        if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
            mNextRouteGoal++;
            if (mNextRouteGoal == mMyRoute.mPoints) {
                // reset if it's a loop, else terminate Route
                if (mRouteLoop) {
                    mEntity.mBody.setTransform(mMyRoute.mXIndices[0] + mEntity.mWidth / 2,
                            mMyRoute.mYIndices[0] + mEntity.mHeight / 2, 0);
                    startRoute();
                    return;
                } else {
                    mRouteDone = true;
                    mEntity.mBody.setLinearVelocity(0, 0);
                    return;
                }
            } else {
                // advance to next point
                mRouteVec.x = mMyRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
                mRouteVec.y = mMyRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
                mRouteVec.nor();
                mRouteVec.scl(mRouteVelocity);
                mEntity.mBody.setLinearVelocity(mRouteVec);
                return;
            }
        }
        // NB: if we get here, we didn't need to change the velocity
    }
}
