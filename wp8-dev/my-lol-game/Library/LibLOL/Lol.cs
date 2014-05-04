using System;
using System.Diagnostics;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;

namespace LibLOL
{
    public abstract class Lol : Game
    {
        // Emulating Java's System.nanoTime()
        internal static Stopwatch GlobalGameTime;

        private Modes mMode;

        private GameScreen mCurrScreen;

        internal int mCurrLevelNum;

        internal int mCurrHelpNum;

        internal static Lol sGame;

        internal bool mKeyDown;

        internal LolConfiguration mConfig;

        internal ChooserConfiguration mChooserConfig;

        private enum Modes
        {
            SPLASH, HELP, CHOOSE, PLAY
        };

        internal void DoSplash()
        {
            mCurrLevelNum = 0;
            mCurrHelpNum = 0;
            mMode = Modes.SPLASH;
            //SetScreen(new Splash());
        }

        internal void DoChooser()
        {
            if (mConfig.GetNumLevels() == 1)
            {
                if (mCurrLevelNum == 1)
                {
                    DoSplash();
                }
                else
                {
                    DoPlayLevel(1);
                }
                return;
            }
            mCurrHelpNum = 0;
            mMode = Modes.CHOOSE;
            SetScreen(new Chooser(this));
        }

        internal void DoPlayLevel(int which)
        {
            mCurrLevelNum = which;
            mCurrHelpNum = 0;
            mMode = Modes.PLAY;
            ConfigureLevel(which);
            //SetScreen(Level.sCurrent);
        }

        internal void DoHelpLevel(int which)
        {
            mCurrHelpNum = which;
            mCurrLevelNum = 0;
            mMode = Modes.HELP;
            ConfigureHelpScene(which);
            //SetScreen(HelpLevel.sCurrent);
        }

        internal void DoQuit()
        {
            Exit();
        }

        private void SetScreen(GameScreen screen)
        {
            if (mCurrScreen != null) { mCurrScreen.Dispose(); }
            mCurrScreen = screen;
            Components.Add(screen);
        }

        private void HandleKeyDown()
        {
            // if neither BACK nor ESCAPE is being pressed, do nothing, but
            // recognize future presses
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back != ButtonState.Pressed)
            {
                mKeyDown = false;
                return;
            }
            // if they key is being held down, ignore it
            if (mKeyDown)
                return;
            // recognize a new back press as being a 'down' press
            mKeyDown = true;
            HandleBack();
        }

        internal void HandleBack()
        {
            Timer.Instance.Clear();

            if (mMode == Modes.SPLASH)
            {
                Dispose();
                Exit();
            }
            else if (mMode == Modes.CHOOSE || mMode == Modes.HELP)
            {
                DoSplash();
            }
            else
            {
                DoChooser();
            }
        }


        // Currently for testing right now.
        private GraphicsDeviceManager graphics;
        //SpriteBatch spriteBatch;
        //PhysicsSprite test, test2;

        static Lol()
        {
            GlobalGameTime = new Stopwatch();
            GlobalGameTime.Start();
        }

        public Lol()
        {
            graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "Content";
        }

        protected override void Initialize()
        {
            sGame = this;
            mConfig = LolConfig();
            mChooserConfig = ChooserConfig();
            base.Initialize();
        }

        protected override void Dispose(bool disposing)
        {
            base.Dispose(disposing);
            Media.OnDispose();
        }

        protected override void LoadContent()
        {
            NameResources();
            DoSplash();
            /*spriteBatch = new SpriteBatch(GraphicsDevice);
            Texture2D tex = this.Content.Load<Texture2D>("Graphics\\greenball");
            Texture2D xet = this.Content.Load<Texture2D>("Graphics\\blueball");
            Media.sImages.Add("greenball", new Texture2D[] { tex });
            Media.sImages.Add("blueball", new Texture2D[] { xet });
            Level.sCurrent = new Level(this);
            Physics.Configure(0, 0);
            float xPos = this.GraphicsDevice.Viewport.TitleSafeArea.X;
            float yPos = this.GraphicsDevice.Viewport.TitleSafeArea.Y + this.GraphicsDevice.Viewport.TitleSafeArea.Height / 2;
            test = Hero.MakeAsBox(xPos, yPos, tex.Width, tex.Height, "greenball");
            test2 = Destination.MakeAsBox(400, 300, xet.Width, xet.Height, "blueball");
            Route route = new Route(4).To(300, 300).To(400, 300).To(100, 100).To(300, 300);
            test.SetRoute(route, 25, true);*/
            
            base.LoadContent();
        }

        protected override void Update(GameTime gameTime)
        {
            //mCurrScreen.Update(gameTime);
            /*Level.sCurrent.mWorld.Step(1 / 60f);
            foreach (Action a in Level.sCurrent.mOneTimeEvents)
            {
                a();
            }
            Level.sCurrent.mOneTimeEvents.Clear();
            test.Update(gameTime);
            test2.Update(gameTime);*/
            base.Update(gameTime);
        }

        protected override void Draw(GameTime gameTime)
        {
            //mCurrScreen.Draw(gameTime);
            /*spriteBatch.Begin();
            test.Draw(spriteBatch, gameTime);
            test2.Draw(spriteBatch, gameTime);
            spriteBatch.End();*/
            base.Draw(gameTime);
        }

        public abstract LolConfiguration LolConfig();

        public abstract ChooserConfiguration ChooserConfig();

        public abstract void NameResources();

        public abstract void ConfigureLevel(int whichLevel);

        public abstract void ConfigureHelpScene(int whichScene);

        public abstract void ConfigureSplash();

        public abstract void OnHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h);

        public abstract void OnTouchTrigger(int id, int whichLevel, PhysicsSprite p);

        public abstract void OnTimerTrigger(int id, int whichLevel);

        public abstract void OnEnemyTimerTrigger(int id, int whichLevel, Enemy e);

        public abstract void OnEnemyDefeatTrigger(int id, int whichLevel, Enemy e);

        public abstract void OnEnemyCollideTrigger(int id, int whichLevel, Enemy e);

        public abstract void OnProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p);

        public abstract void LevelCompleteTrigger(int whichLevel, bool win);

        public abstract void OnControlPressTrigger(int id, int whichLevel);

        public abstract void OnStrengthChangeTrigger(int whichLevel, Hero h);
    }
}
