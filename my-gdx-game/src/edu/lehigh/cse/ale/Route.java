package edu.lehigh.cse.ale;

// TODO: are we setting center points or bottom-left points?  This will matter to pokepath stuff

// TODO: clean up comments

public class Route
{
    float [] _xIndices;
    float [] _yIndices;
    float _velocity;
    int _size;
    int _points;
    
    /**
     * Define a new path, by specifying the number of points in the path
     * 
     * @param numberOfPoints
     *            number of points in the path
     */
    public Route(int numberOfPoints)
    {
        // NB: it doesn't make sense to have a route with only one point!
        assert(numberOfPoints > 1);
        _size = numberOfPoints;
        _xIndices = new float[_size];
        _yIndices = new float[_size];
    }

    /**
     * Add a new point to a path by giving (x,y) coordinates for where the center of the object ought to move
     * 
     * @param x
     *            X value of the new coordinate
     * 
     * @param y
     *            Y value of the new coordinate
     */
    public Route to(float x, float y)
    {
        _xIndices[_points] = x;
        _yIndices[_points] = y;
        _points++;
        return this;
    }
}
