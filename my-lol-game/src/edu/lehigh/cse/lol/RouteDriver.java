
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
        _entity._physBody.setTransform(_myRoute._xIndices[0] + _entity._width / 2,
                _myRoute._yIndices[0] + _entity._height / 2, 0);
        // second, indicate that we are working on goal #1, and set velocity
        startRoute();
        // and indicate that we aren't all done yet
        _routeDone = false;

    }

    void startRoute() {
        _nextRouteGoal = 1;
        _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
        _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
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
        float sx = _myRoute._xIndices[_nextRouteGoal - 1] - _entity.getXPosition();
        float sy = _myRoute._yIndices[_nextRouteGoal - 1] - _entity.getYPosition();
        float gx = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
        float gy = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
        if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
            _nextRouteGoal++;
            if (_nextRouteGoal == _myRoute._points) {
                // reset if it's a loop, else terminate Route
                if (_routeLoop) {
                    _entity._physBody.setTransform(_myRoute._xIndices[0] + _entity._width / 2,
                            _myRoute._yIndices[0] + _entity._height / 2, 0);
                    startRoute();
                    return;
                } else {
                    _routeDone = true;
                    _entity._physBody.setLinearVelocity(0, 0);
                    return;
                }
            } else {
                // advance to next point
                _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
                _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
                _routeVec.nor();
                _routeVec.scl(_routeVelocity);
                _entity._physBody.setLinearVelocity(_routeVec);
                return;
            }
        }
        // NB: if we get here, we didn't need to change the velocity
    }
}
