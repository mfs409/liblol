using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

namespace LOL
{
    public class Route
    {
        /**
         * The X coordinates of the points in the route
         */
        public float[] mXIndices;

        /**
         * The Y coordinates of the points in the route
         */
        public float[] mYIndices;

        /**
         * The speed at which the entity should move along the route
         */
        public float mVelocity;

        /**
         * The maximum number of points in this route
         */
        public int mSize;

        /**
         * The current number of points that have been set
         */
        public int mPoints;

        /**
         * Define a new path, by specifying the number of points in the path. Note
         * that all points in the path will be uninitialized until the "to" method
         * is called on this Route.
         * 
         * @param numberOfPoints number of points in the path
         */
        public Route(int numberOfPoints)
        {
            // NB: it doesn't make sense to have a route with only one point!
            Debug.Assert(numberOfPoints > 1);
            mSize = numberOfPoints;
            mXIndices = new float[mSize];
            mYIndices = new float[mSize];
        }

        /**
         * Add a new point to a path by giving (x,y) coordinates for where the
         * center of the object ought to move
         * 
         * @param x X value of the new coordinate
         * @param y Y value of the new coordinate
         */
        public Route to(float x, float y)
        {
            mXIndices[mPoints] = x;
            mYIndices[mPoints] = y;
            mPoints++;
            return this;
        }
    }
}
