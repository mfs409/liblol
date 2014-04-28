using System;

using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
{
    public class Destination : PhysicsSprite
    {
        internal int mCapacity;

        internal int mHolding;

        internal int[] mActivation = new int[4];

        internal SoundEffect mArrivalSound;

        private Destination(float width, float height, String imgName) : base(imgName, width, height)
        {
            mCapacity = 1;
            mHolding = 0;
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
        }

        public static Destination MakeAsBox(float x, float y, float width, float height, String imgName)
        {
            Destination d = new Destination(width, height, imgName);
            d.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            d.SetCollisionEffect(false);
            // Level.sCurrent.AddSprite(d, 0);
            return d;
        }

        public static Destination MakeAsCircle(float x, float y, float width, float height, String imgName)
        {
            float radius = Math.Max(width, height);
            Destination d = new Destination(width, height, imgName);
            d.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius);
            d.SetCollisionEffect(false);
            // Level.sCurrent.AddSprite(d, 0);
            return d;
        }

        public void SetActivationScore(int score1, int score2, int score3, int score4)
        {
            mActivation[0] = score1;
            mActivation[1] = score2;
            mActivation[2] = score3;
            mActivation[3] = score4;
        }

        public void SetHeroCount(int heroes)
        {
            mCapacity = heroes;
        }

        public void SetArrivalSound(String soundName)
        {

        }
    }
}
