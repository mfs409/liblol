using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public class Chooser : GameScreen
    {

        private Button mPrev;

        private Button mNext;

        private Button mBack;

        private Texture2D[] mImage;

        private Music mMusic;

        private bool mMusicPlaying;

        private Vector3 mV = new Vector3();

        private Button[] mLevels;

        //private OrthographicCamera mCamera;

        private SpriteBatch mSpriteBatch;

        private Font mFont;

        //private ShapeRenderer mShapeRenderer;

        private class Button
        {
            internal Rectangle mRect;

            internal int mLevel;

            Texture2D[] mTr;

            internal Button(int x, int y, int w, int h, int level, string imgName)
            {
                mRect = new Rectangle(x, y, w, h);
                mLevel = level;
                mTr = Media.GetImage(imgName);
            }
        }


    }
}
