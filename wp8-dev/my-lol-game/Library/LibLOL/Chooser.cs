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

        public Chooser(Game game) : base(game)
        {
            Lol g = (Lol)game;
            ChooserConfiguration cc = g.mChooserConfig;
            int levelsPerChooser = cc.GetColumns() * cc.GetRows();
            int totalLevels = g.mConfig.GetNumLevels();
            mImage = Media.GetImage(cc.GetBackgroundName());
            if (cc.GetMusicName() != null)
            {
                mMusic = Media.GetMusic(cc.GetMusicName());
            }
            mBack = new Button(cc.GetBackButtonX(), cc.GetBackButtonY(), cc.GetBackButtonWidth(),
                cc.GetBackButtonHeight(), 0, cc.GetBackButtonName());

            if (g.mCurrLevelNum > levelsPerChooser)
            {
                mPrev = new Button(cc.GetPrevButtonX(), cc.GetPrevButtonY(), cc.GetPrevButtonWidth(),
                        cc.GetPrevButtonHeight(), 0, cc.GetPrevButtonName());
            }
            if (g.mCurrLevelNum + levelsPerChooser - 1 < totalLevels)
            {
                mNext = new Button(cc.GetNextButtonX(), cc.GetNextButtonY(), cc.GetNextButtonWidth(),
                        cc.GetNextButtonHeight(), 0, cc.GetNextButtonName());
            }
            int first = g.mCurrLevelNum;
            if (first > 0)
            {
                first--;
            }
            first = first - (first % levelsPerChooser) + 1;
            int last = Math.Min(totalLevels, first + levelsPerChooser - 1);

        }
    }
}
