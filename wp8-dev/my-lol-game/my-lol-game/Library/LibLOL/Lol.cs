using System;
using System.Diagnostics;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public abstract class Lol : Game
    {
        // Emulating Java's System.nanoTime()
        internal static Stopwatch GlobalGameTime;

        private Modes mMode;

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


        // Currently for testing right now.
        GraphicsDeviceManager graphics;
        SpriteBatch spriteBatch;
        PhysicsSprite test, test2;

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
            base.Initialize();
        }

        protected override void LoadContent()
        {
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
            Level.sCurrent.mWorld.Step(1 / 60f);
            foreach (Action a in Level.sCurrent.mOneTimeEvents)
            {
                a();
            }
            Level.sCurrent.mOneTimeEvents.Clear();
            test.Update(gameTime);
            test2.Update(gameTime);
            base.Update(gameTime);
        }

        protected override void Draw(GameTime gameTime)
        {
            spriteBatch.Begin();
            test.Draw(spriteBatch, gameTime);
            test2.Draw(spriteBatch, gameTime);
            spriteBatch.End();
            base.Draw(gameTime);
        }
    }
}
