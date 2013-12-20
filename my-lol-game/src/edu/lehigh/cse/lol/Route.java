
package edu.lehigh.cse.lol;

/**
 * A Route specifies a set of points that an entity will move between at a fixed
 * speed.
 */
public class Route {
    /**
     * The X coordinates of the points in the route
     */
    float[] _xIndices;

    /**
     * The Y coordinates of the points in the route
     */
    float[] _yIndices;

    /**
     * The speed at which the entity should move along the route
     */
    float _velocity;

    /**
     * The maximum number of points in this route
     */
    int _size;

    /**
     * The current number of points that have been set
     */
    int _points;

    /**
     * Define a new path, by specifying the number of points in the path. Note
     * that all points in the path will be uninitialized until the "to" method
     * is called on this Route.
     * 
     * @param numberOfPoints number of points in the path
     */
    public Route(int numberOfPoints) {
        // NB: it doesn't make sense to have a route with only one point!
        assert (numberOfPoints > 1);
        _size = numberOfPoints;
        _xIndices = new float[_size];
        _yIndices = new float[_size];
    }

    /**
     * Add a new point to a path by giving (x,y) coordinates for where the
     * center of the object ought to move
     * 
     * @param x X value of the new coordinate
     * @param y Y value of the new coordinate
     */
    public Route to(float x, float y) {
        _xIndices[_points] = x;
        _yIndices[_points] = y;
        _points++;
        return this;
    }
}
