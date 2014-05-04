using System;

namespace LibLOL
{
    public class Score
    {
        internal int mHeroesCreated = 0;

        internal int mHeroesDefeated = 0;

        internal int[] mGoodiesCollected = new int[] {
            0, 0, 0, 0
        };

        private int mDestinationArrivals = 0;

        internal int mEnemiesCreated = 0;

        internal int mEnemiesDefeated = 0;

        private VictoryType mVictoryType = VictoryType.DESTINATION;

        private int mVictoryHeroCount;

        private int[] mVictoryGoodieCount = new int[4];

        private int mVictoryEnemyCount;

        internal bool mGameOver;

        internal enum VictoryType
        {
            DESTINATION, GOODIECOUNT, ENEMYCOUNT
        };

        internal float mCountDownRemaining;

        internal float mWinCountRemaining;

        internal float mStopWatchProgress;

        internal int mDistance;

        internal void DefeatHero(Enemy e)
        {
            mHeroesDefeated++;
            if (mHeroesCreated == mHeroesDefeated)
            {
                if (!e.mOnDefeatHeroText.Equals(""))
                {
                    //PostScene.SetDefaultLoseText(e.mOnDefeatHeroText);
                }
                EndLevel(false);
            }
        }

        internal void OnGoodieCollected(Goodie g)
        {
            for (int i = 0; i < 4; ++i)
            {
                mGoodiesCollected[i] = g.mScore[i];
            }

            if (mVictoryType != VictoryType.GOODIECOUNT)
            {
                return;
            }
            bool match = true;
            for (int i = 0; i < 4; ++i)
            {
                match &= mVictoryGoodieCount[i] <= mGoodiesCollected[i];
            }
            if (match)
            {
                EndLevel(true);
            }
        }

        internal void OnDestinationArrive()
        {
            mDestinationArrivals++;
            if ((mVictoryType == VictoryType.DESTINATION)
                && (mDestinationArrivals >= mVictoryHeroCount))
            {
                EndLevel(true);
            }
        }

        internal void OnDefeatEnemy()
        {
            mEnemiesDefeated++;
            bool win = false;

            if (mVictoryType == VictoryType.ENEMYCOUNT)
            {
                if (mVictoryEnemyCount == -1)
                {
                    win = mEnemiesDefeated == mEnemiesCreated;
                }
                else
                {
                    win = mEnemiesDefeated >= mVictoryEnemyCount;
                }
            }
            if (win)
            {
                EndLevel(true);
            }
        }

        internal void EndLevel(bool win)
        {
            if (Level.sCurrent.mEndGameEvent == null)
            {
                Level.sCurrent.mEndGameEvent = delegate()
                {
                    if (mGameOver)
                    {
                        return;
                    }
                    mGameOver = true;

                    Lol.sGame.LevelCompleteTrigger(Lol.sGame.mCurrLevelNum, win);

                    //if (win && ReadUnlocked() == Lol.sGame.mCurrLevelNum)
                    //  SaveUnlocked(Lol.sGame.mCurrLevelNum + 1);

                    //Level.sCurrent.mControls.Clear();

                    Timer.Instance.Clear();

                    //Level.sCurrent.mPostScene.SetWin(win);
                };
            }
        }

        public static int GoodiesCollected1
        {
            set { Level.sCurrent.mScore.mGoodiesCollected[0] = value; }
            get { return Level.sCurrent.mScore.mGoodiesCollected[0]; }
        }

        public static int GoodiesCollected2
        {
            set { Level.sCurrent.mScore.mGoodiesCollected[1] = value; }
            get { return Level.sCurrent.mScore.mGoodiesCollected[1]; }
        }

        public static int GoodiesCollected3
        {
            set { Level.sCurrent.mScore.mGoodiesCollected[2] = value; }
            get { return Level.sCurrent.mScore.mGoodiesCollected[2]; }
        }

        public static int GoodiesCollected4
        {
            set { Level.sCurrent.mScore.mGoodiesCollected[3] = value; }
            get { return Level.sCurrent.mScore.mGoodiesCollected[3]; }
        }

        public static void SetVictoryEnemyCount(int howMany = -1)
        {
            Level.sCurrent.mScore.mVictoryType = VictoryType.ENEMYCOUNT;
            Level.sCurrent.mScore.mVictoryEnemyCount = howMany;
        }

        public static void SetVictoryGoodies(int v1, int v2, int v3, int v4)
        {
            Level.sCurrent.mScore.mVictoryType = VictoryType.GOODIECOUNT;
            Level.sCurrent.mScore.mVictoryGoodieCount[0] = v1;
            Level.sCurrent.mScore.mVictoryGoodieCount[1] = v2;
            Level.sCurrent.mScore.mVictoryGoodieCount[2] = v3;
            Level.sCurrent.mScore.mVictoryGoodieCount[3] = v4;
        }

        public static void SetVictoryDestination(int howMany)
        {
            Level.sCurrent.mScore.mVictoryType = VictoryType.DESTINATION;
            Level.sCurrent.mScore.mVictoryHeroCount = howMany;
        }

        public static void UpdateTimerExpiration(float delta)
        {
            Level.sCurrent.mScore.mCountDownRemaining += delta;
        }

        public static int Distance
        {
            get { return Level.sCurrent.mScore.mDistance; }
        }

        public static int Stopwatch
        {
            get { return (int)Level.sCurrent.mScore.mStopWatchProgress; }
        }

        public static int EnemiesDefeated
        {
            get { return Level.sCurrent.mScore.mEnemiesDefeated; }
        }
    }
}
