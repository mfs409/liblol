using System;
using System.Diagnostics;


namespace LibLOL
{
    // Conversion complete!
    public class Route
    {
        internal float[] mXIndices;

        internal float[] mYIndices;

        internal float mVelocity;

        internal int mSize;

        internal int mPoints;

        public Route(int numberOfPoints)
        {
            Debug.Assert(numberOfPoints > 1);
            mSize = numberOfPoints;
            mXIndices = new float[mSize];
            mYIndices = new float[mSize];
        }

        public Route To(float x, float y)
        {
            mXIndices[mPoints] = x;
            mYIndices[mPoints] = y;
            ++mPoints;
            return this;
        }
    }
}
