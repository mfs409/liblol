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
    Route _myRoute;

    float _routeVelocity;

    boolean _routeLoop;

    Vector2 _routeVec = new Vector2();

    boolean _routeDone;

    int _nextRouteGoal;

    PhysicsSprite _entity;

    void haltRoute() {
        _routeDone = true;
        // NB: third parameter doesn't matter, because the entity isn't a static
        // body, so its bodytype won't change.
        _entity.setAbsoluteVelocity(0, 0, false);
    }

    RouteDriver(Route route, float velocity, boolean loop, PhysicsSprite entity) {
        _myRoute = route;
        _routeVelocity = velocity;
        _routeLoop = loop;
        _entity = entity;

        // this is how we initialize a route driver:
        // first, move to the starting point
        _entity._physBody.setTransform(_myRoute.mXIndices[0] + _entity._width / 2,
                _myRoute.mYIndices[0] + _entity._height / 2, 0);
        // second, indicate that we are working on goal #1, and set velocity
        startRoute();
        // and indicate that we aren't all done yet
        _routeDone = false;

    }

    void startRoute() {
        _nextRouteGoal = 1;
        _routeVec.x = _myRoute.mXIndices[_nextRouteGoal] - _entity.getXPosition();
        _routeVec.y = _myRoute.mYIndices[_nextRouteGoal] - _entity.getYPosition();
        _routeVec.nor();
        _routeVec.scl(_routeVelocity);
        _entity._physBody.setLinearVelocity(_routeVec);
    }

    /**
     * Internal method for figuring out where we need to go next when driving a
     * route
     */
    void drive() {
        // quit if we're done and we don't loop
        if (_routeDone)
            return;
        // if we haven't passed the goal, keep going. we tell if we've passed
        // the goal by comparing the magnitudes
        // of
        // the vectors from source to here and from goal to here
        float sx = _myRoute.mXIndices[_nextRouteGoal - 1] - _entity.getXPosition();
        float sy = _myRoute.mYIndices[_nextRouteGoal - 1] - _entity.getYPosition();
        float gx = _myRoute.mXIndices[_nextRouteGoal] - _entity.getXPosition();
        float gy = _myRoute.mYIndices[_nextRouteGoal] - _entity.getYPosition();
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
        if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
            _nextRouteGoal++;
            if (_nextRouteGoal == _myRoute.mPoints) {
                // reset if it's a loop, else terminate Route
                if (_routeLoop) {
                    _entity._physBody.setTransform(_myRoute.mXIndices[0] + _entity._width / 2,
                            _myRoute.mYIndices[0] + _entity._height / 2, 0);
                    startRoute();
                    return;
                } else {
                    _routeDone = true;
                    _entity._physBody.setLinearVelocity(0, 0);
                    return;
                }
            } else {
                // advance to next point
                _routeVec.x = _myRoute.mXIndices[_nextRouteGoal] - _entity.getXPosition();
                _routeVec.y = _myRoute.mYIndices[_nextRouteGoal] - _entity.getYPosition();
                _routeVec.nor();
                _routeVec.scl(_routeVelocity);
                _entity._physBody.setLinearVelocity(_routeVec);
                return;
            }
        }
        // NB: if we get here, we didn't need to change the velocity
    }
}
