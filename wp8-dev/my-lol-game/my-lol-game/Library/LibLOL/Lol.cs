using System;
using System.Diagnostics;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public abstract class Lol : Game
    {
        // Emulating Java's System.nanoTime()
        public static Stopwatch GameTime;


        // Currently for testing right now.
        GraphicsDeviceManager graphics;
        SpriteBatch spriteBatch;
        PhysicsSprite test;

        static Lol()
        {
            GameTime = new Stopwatch();
            GameTime.Start();
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
            spriteBatch = new SpriteBatch(GraphicsDevice);
            Texture2D tex = this.Content.Load<Texture2D>("Graphics\\greenball");
            Media.sImages.Add("greenball", new Texture2D[] { tex });
            Level.CreateTestLevel(this);
            float xPos = this.GraphicsDevice.Viewport.TitleSafeArea.X;
            float yPos = this.GraphicsDevice.Viewport.TitleSafeArea.Y + this.GraphicsDevice.Viewport.TitleSafeArea.Height / 2;
            test = Hero.MakeAsBox(xPos, yPos, tex.Width, tex.Height, "greenball");
            Route route = new Route(4).To(300, 300).To(400, 300).To(100, 100).To(300, 300);
            test.SetRoute(route, 25, true);
            
            base.LoadContent();
        }

        protected override void Update(GameTime gameTime)
        {
            Level.sCurrent.mWorld.Step(1 / 60f);
            test.Update(gameTime);
            base.Update(gameTime);
        }

        protected override void Draw(GameTime gameTime)
        {
            spriteBatch.Begin();
            test.Draw(spriteBatch, gameTime);
            spriteBatch.End();
            base.Draw(gameTime);
        }
    }
}
