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

/**
 * A Route specifies a set of points that an actor will move between at a fixed
 * speed.
 */
public class Route {
    /**
     * The X coordinates of the points in the route
     */
    public float[] mXIndices;

    /**
     * The Y coordinates of the points in the route
     */
    public float[] mYIndices;
    /**
     * The current number of points that have been set
     */
    public int mPoints;
    /**
     * The speed at which the actor should move along the route
     */
    float mVelocity;
    /**
     * The maximum number of points in this route
     */
    int mSize;

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
    public Route to(float x, float y) {
        mXIndices[mPoints] = x;
        mYIndices[mPoints] = y;
        mPoints++;
        return this;
    }
}