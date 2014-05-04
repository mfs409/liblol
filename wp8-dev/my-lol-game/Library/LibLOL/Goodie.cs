using System;
using System.Diagnostics;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
{
    // Uncomment code to be done
    public class Goodie : PhysicsSprite
    {
        internal int[] mScore = new int[4];

        internal int mStrengthBoost = 0;

        internal double mInvincibilityDuration;

        private Goodie(float width, float height, string imgName) : base(imgName, width, height)
        {
            mScore[0] = 1;
            mScore[1] = 0;
            mScore[2] = 0;
            mScore[3] = 0;
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
        }

        public static Goodie MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Goodie g = new Goodie(width, height, imgName);
            g.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            g.CollisionEffect = false;
            // Level.sCurrent.AddSprite(g, 0);
            return g;
        }

        public static Goodie MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Goodie g = new Goodie(width, height, imgName);
            g.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            g.CollisionEffect = false;
            // Level.sCurrent.AddSprite(g, 0);
            return g;
        }

        public void SetScore(int v1, int v2, int v3, int v4)
        {
            mScore[0] = v1;
            mScore[1] = v2;
            mScore[2] = v3;
            mScore[3] = v4;
        }

        /*public void SetStrengthBoost(int boost)
        {
            mStrengthBoost = boost;
        }*/

        public int StrengthBoost
        {
            set { mStrengthBoost = value; }
        }

        /*public void SetInvicibilityDuration(double duration)
        {
            Debug.Assert(duration >= 0);
            mInvincibilityDuration = duration;
        }*/

        public double InvicibilityDuration
        {
            set
            {
                Debug.Assert(value >= 0);
                mInvincibilityDuration = value;
            }
        }
    }
}
