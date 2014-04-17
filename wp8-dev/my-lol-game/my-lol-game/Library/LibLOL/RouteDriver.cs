using System;
using System.Diagnostics;
using Microsoft.Xna.Framework;

namespace LibLOL
{
    // Conversion complete!
    class RouteDriver
    {
        private Route mRoute;

        private PhysicsSprite mEntity;

        private float mRouteVelocity;

        private bool mRouteLoop;

        private Vector2 mRouteVec = new Vector2();

        private bool mRouteDone;

        private int mNextRouteGoal;

        internal RouteDriver(Route route, float velocity, bool loop, PhysicsSprite entity)
        {
            this.mRoute = route;
            mRouteVelocity = velocity;
            mRouteLoop = loop;
            this.mEntity = entity;
            StartRoute();
            mRouteDone = false;

        }

        internal void HaltRoute()
        {
            mRouteDone = true;
            mEntity.SetAbsoluteVelocity(0, 0, false);
        }

        private void StartRoute()
        {
            Vector2 pos = new Vector2(mRoute.mXIndices[0] + mEntity.mSize.X / 2,
                mRoute.mYIndices[0] + mEntity.mSize.Y / 2);
            mEntity.mBody.SetTransform(pos, 0f);
            mNextRouteGoal = 1;
            mRouteVec.X = mRoute.mXIndices[mNextRouteGoal] - mEntity.GetXPosition();
            mRouteVec.Y = mRoute.mYIndices[mNextRouteGoal] - mEntity.GetYPosition();
            mRouteVec.Normalize();
            mRouteVec.X *= mRouteVelocity;
            mRouteVec.Y *= mRouteVelocity;
            mEntity.mBody.LinearVelocity = mRouteVec;
        }

        internal void Drive()
        {
            if (mRouteDone)
            {
                return;
            }

            float sx = mRoute.mXIndices[mNextRouteGoal - 1] - mEntity.GetXPosition();
            float sy = mRoute.mYIndices[mNextRouteGoal - 1] - mEntity.GetYPosition();
            float gx = mRoute.mXIndices[mNextRouteGoal] - mEntity.GetXPosition();
            float gy = mRoute.mYIndices[mNextRouteGoal] - mEntity.GetYPosition();
            
            bool sameXSign = (sx >= 0 && gx >= 0) || (sx <= 0 && gx <= 0);
            bool sameYSign = (sy >= 0 && gy >= 0) || (sy <= 0 && gy <= 0);

            //Debug.WriteLine("sx: " + sx + ", sy: " + sy + ", gx: " + gx + ", gy: " + gy + " " + (sameXSign && sameYSign));
            
            if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign))
            {
                mNextRouteGoal++;
                if (mNextRouteGoal == mRoute.mPoints)
                {
                    if (mRouteLoop)
                    {
                        StartRoute();
                    }
                    else
                    {
                        mRouteDone = true;
                        mEntity.mBody.LinearVelocity = new Vector2(0, 0);
                    }
                }
                else
                {
                    mRouteVec.X = mRoute.mXIndices[mNextRouteGoal] - mEntity.GetXPosition();
                    mRouteVec.Y = mRoute.mYIndices[mNextRouteGoal] - mEntity.GetYPosition();
                    mRouteVec.Normalize();
                    mRouteVec.X *= mRouteVelocity;
                    mRouteVec.Y *= mRouteVelocity;
                    mEntity.mBody.LinearVelocity = mRouteVec;
                }
            }
        }
    }
}
