using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework.GamerServices;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Microsoft.Xna.Framework.Input.Touch;
using Microsoft.Xna.Framework.Media;
using LOL;

namespace LOL02
{
    /// <summary>
    /// This is the main type for your game
    /// </summary>
    public class Game1 : Lol
    {
        protected GraphicsDeviceManager graphics;
        protected SpriteBatch spriteBatch;
        public ScreenManager mgr;

        public Game1()
        {
            mgr = new ScreenManager();

            // TODO: Move to Lol class init code
            Screen = mgr;

            graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "Content";

            // Frame rate is 30 fps by default for Windows Phone.
            TargetElapsedTime = TimeSpan.FromTicks(333333);

            // Extend battery life under lock.
            InactiveSleepTime = TimeSpan.FromSeconds(1);
        }

        // WARNING: DO NOT TOUCH THIS METHOD!
        protected override void Update(GameTime gameTime)
        {
            // Allows the game to exit
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back == ButtonState.Pressed)
                this.Exit();
            mgr.Update(gameTime);
            base.Update(gameTime);
        }

        // WARNING: DO NOT TOUCH THIS METHOD!
        protected override void Draw(GameTime gameTime)
        {
            mgr.Draw(gameTime);
            base.Draw(gameTime);
        }

        /// <summary>
        /// Allows the game to perform any initialization it needs to before starting to run.
        /// This is where it can query for any required services and load any non-graphic
        /// related content.  Calling base.Initialize will enumerate through any components
        /// and initialize them as well.
        /// </summary>
        protected override void Initialize()
        {
            // TODO: Add your initialization logic here
            SpriteFont sf = Content.Load<SpriteFont>("Default");
            
            base.Initialize();
            create();
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            // Create a new SpriteBatch, which can be used to draw textures.
            spriteBatch = new SpriteBatch(GraphicsDevice);

            // TODO: use this.Content to load your game content here
        }

        /// <summary>
        /// UnloadContent will be called once per game and is the place to unload
        /// all content.
        /// </summary>
        protected override void UnloadContent()
        {
            // TODO: Unload any non ContentManager content here
        }


        /**
         * Configure all the images and sounds used by our game
         */
        public override void nameResources() {
            // load regular (non-animated) images
            Media.registerImage("greenball");
            Media.registerImage("mustardball");
            Media.registerImage("red");
            Media.registerImage("leftarrow");
            Media.registerImage("rightarrow");
            Media.registerImage("backarrow");
            Media.registerImage("redball");
            Media.registerImage("blueball");
            Media.registerImage("purpleball");
            Media.registerImage("msg1");
            Media.registerImage("msg2");
            Media.registerImage("fade");
            Media.registerImage("greyball");
            Media.registerImage("leveltile");

            // load the image we show on the main screen
            Media.registerImage("splash");

            // load the image we show on the chooser screen
            Media.registerImage("chooser");

            // load background images
            Media.registerImage("mid");
            Media.registerImage("front");
            Media.registerImage("back");

            // load animated images (a.k.a. Sprite Sheets)
            Media.registerAnimatableImage("stars", 8, 1);
            Media.registerAnimatableImage("flystar", 2, 1);
            Media.registerAnimatableImage("starburst", 4, 1);
            Media.registerAnimatableImage("colorstar", 8, 1);

            // load sounds
            Media.registerSound("hipitch");
            Media.registerSound("lowpitch");
            Media.registerSound("losesound");
            Media.registerSound("slowdown");
            Media.registerSound("woowoowoo");
            Media.registerSound("fwapfwap");
            Media.registerSound("winsound");

            // load background music
            Media.registerMusic("tune", true);
        }

        /**
         * Describe how to draw the first scene that displays when the game app is
         * started
         */
        public override void configureSplash() {
            // Describe the regions of the screen that correspond to the play, help,
            // and quit buttons. If you are having trouble figuring these out, note
            // that clicking on the splash screen will display xy coordinates in the
            // Console to help
            Splash.drawPlayButton(Util.ax(192), Util.ay(91,52), Util.ax(93), Util.ah(52));
            Splash.drawHelpButton(Util.ax(48), Util.ay(93,40), Util.ax(80), Util.ah(40));
            Splash.drawQuitButton(Util.ax(363), Util.ay(93,39), Util.ax(69), Util.ah(39));

            // Provide a name for the background image
            Splash.setBackground("splash");

            // Provide a name for the music file
            Splash.setMusic("tune");
        }

        /**
         * Describe how to draw the initial state of each level of our game
         * 
         * @param whichLevel The level to be drawn
         */
        public override void configureLevel(int whichLevel) {
            /*
             * In this level, all we have is a hero (the green ball) who needs to
             * make it to the destination (a mustard colored ball). The game is
             * configured to use tilt to control the hero.
             */
            if (whichLevel == 1) {
                // set the screen to 48 meters wide by 32 meters high... this is
                // important, because Config.java says the screen is 480x320, and
                // LOL likes a 10:1 pixel to meter ratio. If we went smaller than
                // 48x32, things would get really weird. And, of course, if you make
                // your screen resolution higher in Config.java, these numbers would
                // need to get bigger.
                //
                // Level.configure MUST BE THE FIRST LINE WHEN DRAWING A LEVEL!!!
                Level.configure(48, 32);
                // there is no default gravitational force
                Physics.Configure(0, 0);

                // in this level, we'll use tilt to move some things around. The
                // maximum force that tilt can exert on anything is +/- 10 in the X
                // dimension, and +/- 10 in the Y dimension
                Tilt.enable(10, 10);

                // now let's create a hero, and indicate that the hero can move by
                // tilting the phone. "greenball" must be registered in
                // the registerMedia() method, which is also in this file. It must
                // also be in your android game's assets folder.
                Hero h = Hero.MakeAsCircle(4, 17, 3, 3, "greenball");
                h.SetMoveByTilting();

                // draw a circular destination, and indicate that the level is won
                // when the hero reaches the destination. "mustardball" must be
                // registered in registerMedia()
                Destination.MakeAsCircle(29, 26, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
            }

        /*
         * In this level, we make the play a bit smoother by adding a bounding
         * box and changing the way that LibLOL interacts with the player
         */
            else if (whichLevel == 2)
            {
                // start by setting everything up just like in level 1
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Hero h = Hero.MakeAsCircle(4, 17, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 26, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // add a bounding box so the hero can't fall off the screen
                Util.drawBoundingBox(0, 0, 48, 32, "red", 0, 0, 0);

                // change the text that we display when the level is won
                PostScene.setDefaultWinText("Good job!");

                // add a pop-up message that shows for one second at the
                // beginning of the level. The '50, 50' indicates the bottom left
                // corner of the text we display. 255,255,255 represents the red,
                // green, and blue components of the text color (the color will be
                // white). We'll write our text in the Arial font, with a size of 32
                // pt. The "\n" in the middle of the text causes a line break. Note
                // that "Default" must be in your android game's assets folder.
                PreScene.addText("Reach the destination\nto win this level.", 50, 50, 255, 255, 255,
                        "Default", 32);
            }

            /*
             * In this level, we change the physics from level 2 so that things roll
             * and bounce a little bit more nicely.
             */
            else if (whichLevel == 3)
            {
                // These lines should be familiar after the last two levels
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // give the hero some density and friction, so that it can roll when
                // it encounters a wall... notice that once it has density, it has
                // mass, and it moves a lot slower...
                h.SetPhysics(1, 0, 0.6f);

                // the bounding box now also has nonzero density, elasticity, and
                // friction... you should check out what happens if the friction
                // stays at 0.
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // Let's draw our message in the center of the screen this time
                PreScene.addText("Reach the destination\nto win this level.", 255, 255, 255,
                        "Default", 32);
                // And let's say that instead of touching the message to make it go
                // away, we'll have it go away automatically after 2 seconds
                PreScene.setExpire(2);
                // Note that we're going back to the default PostScene text...
            }

            /*
             * It's confusing to have multiple heroes in a level, but we can... this
             * shows how to have multiple destinations and heroes
             */
            else if (whichLevel == 4)
            {
                // standard stuff...
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // now let's draw two heroes who can both move by tilting, and
                // who both have density and friction. Note that we lower the
                // density, so they move faster
                Hero h1 = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h1.SetPhysics(.1f, 0, 0.6f);
                h1.SetMoveByTilting();
                Hero h2 = Hero.MakeAsCircle(14, 7, 3, 3, "greenball");
                h2.SetPhysics(.1f, 0, 0.6f);
                h2.SetMoveByTilting();

                // notice that now we will make two destinations, each of which
                // defaults to only holding ONE hero, but we still need to get two
                // heroes to destinations in order to complete the level
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Destination.MakeAsCircle(29, 26, 2, 2, "mustardball");
                Score.setVictoryDestination(2);

                // Let's show msg1 instead of text. Note that we had to
                // register it in registerMedia(), and that we're stretching it
                // slightly, since its dimensions are 460x320
                PreScene.addImage("msg1", 0, 0, 480, 320);
            }

            /*
             * This level demonstrates that we can have many heroes that can reach
             * the same destination. It also shows our first sound effect
             */
            else if (whichLevel == 5)
            {
                // standard stuff...
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h1 = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h1.SetPhysics(.1f, 0, 0.6f);
                h1.SetMoveByTilting();
                Hero h2 = Hero.MakeAsCircle(14, 7, 3, 3, "greenball");
                h2.SetPhysics(.1f, 0, 0.6f);
                h2.SetMoveByTilting();
                PreScene.addText("All heroes must\nreach the destination", 255, 255, 255, "Default",
                        32);

                // now let's make a destination, but indicate that it can hold TWO
                // heroes
                Destination d = Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                d.HeroCount = 2;

                // let's also say that whenever a hero reaches the destination, a
                // sound will play
                d.ArrivalSound = "hipitch";

                // Notice that this line didn't change from level 4
                Score.setVictoryDestination(2);
            }

            /*
             * Tilt can be used to control velocity, instead of applying forces to
             * the entities on the screen. It doesn't always work well, but it's a
             * nice option to have...
             */
            else if (whichLevel == 6)
            {
                // standard stuff...
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                PreScene.addText("A different way\nto use tilt.", 255, 255, 255, "Default", 32);

                // change the behavior or tilt
                Tilt.setAsVelocity(true);
            }

            /*
             * This level adds an enemy, to demonstrate that we can make it possible
             * to lose a level
             */
            else if (whichLevel == 7)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                // Notice that we changed the font size and color
                PreScene.addText("Avoid the enemy and\nreach the destination", 25, 255, 255,
                        "Default", 20);

                // draw an enemy... we don't need to give it physics for now...
                Enemy.MakeAsCircle(25, 25, 2, 2, "redball");

                // turn off the postscene... whether the player wins or loses, we'll
                // just start the appropriate level. Be sure to test the game by
                // losing *and* winning!
                PostScene.disable();
            }

            /*
             * This level explores a bit more of what we can do with enemies, by
             * having an enemy with a fixed path.
             */
            else if (whichLevel == 8)
            {
                // configure a basic level, just like the start of level 2:
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 27, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                PreScene.addText("Avoid the enemy and\nreach the destination", 255, 255, 255,
                        "Default", 20);

                // put some extra text on the PreScene
                PreScene.addText("(the enemy is red)", 5, 5, 50, 200, 122, "Default", 10);

                // draw an enemy
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");

                // attach a path to the enemy. It starts at (25, 25) and moves to
                // (25, 2). This means it has *2* points on its route. Notice that
                // since it loops, it is not going to gracefully move back to its
                // starting point. Also note that the first point is the same as the
                // enemy's original position. If it wasn't, then there would be an
                // odd glitch at the beginning of the level.
                e.SetRoute(new Route(2).to(25, 25).to(25, 2), 10, true);

                // Note that when the level is lost, the default lose text will be
                // displayed on a PostScene
            }

            /*
             * This level explores a bit more of what we can do with paths.
             */
            else if (whichLevel == 9)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                PreScene.addText("Avoid the enemy and\nreach the destination", 50, 50, 255, 255, 255,
                        "Default", 20);

                // draw an enemy that can move
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                // This time, we add a third point, which is the same as the
                // starting point. This will give us a nicer sort of movement. Also
                // note the diagonal movement.
                e.SetRoute(new Route(3).to(25, 25).to(12, 2).to(25, 25), 2, true);
                // note that any number of points is possible... you could have
                // extremely complex Routes!
            }

            /*
             * We can make enemies move via tilt. We can also configure some other
             * kinds of sounds
             */
            else if (whichLevel == 10)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(0.1f, 0, 0.6f);
                h.SetMoveByTilting();
                PreScene.addImage("msg2", 10, 10, 460, 320);

                // let's make the destination rotate:
                Destination d = Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                d.SetRotationSpeed(1);
                Score.setVictoryDestination(1);

                // draw an enemy who moves via tilt
                Enemy e3 = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e3.SetPhysics(1.0f, 0.3f, 0.6f);
                e3.SetMoveByTilting();

                // configure some sounds to play on win and lose. Of course, all
                // these sounds must be registered!
                PostScene.setWinSound("winsound");
                PostScene.setLoseSound("losesound");

                // set background music
                Level.setMusic("tune");

                // custom text for when the level is lost
                PostScene.setDefaultLoseText("Better luck next time...");
            }

            /*
             * This shows that it is possible to make a level that is larger than a
             * screen. It also shows that there is a "heads up display" that can be
             * used for providing information and touchable controls
             */
            else if (whichLevel == 11)
            {
                // make the level really big
                Level.configure(400, 300);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 400, 300, "red", 0, 0, 0);

                // put the hero and destination far apart
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(329, 281, 10, 10, "mustardball");
                Score.setVictoryDestination(1);

                // We want to be sure that no matter what, the player can see the
                // hero. We achieve this by having the camera follow the hero:
                Level.setCameraChase(h);

                // add zoom buttons. We are using blank images, which means that the
                // buttons will be invisible... that's nice, because we can make the
                // buttons big (covering the left and right halves of the screen).
                // When debug rendering is turned on, we'll be able to see a red
                // outline of the two rectangles. You could also use images (that
                // you registered, of course), but if you did, you'd either need to
                // make them small, or make them semi-transparent.
                Controls.addZoomOutButton(0, 0, 240, 320, "", 8);
                Controls.addZoomInButton(240, 0, 240, 320, "", .25f);

                PreScene.addText("Press left to zoom out\nright to zoom in", 255, 255, 255,
                        "Default", 32);
            }

            /*
             * this level introduces obstacles, and also shows the difference
             * between "box" and "circle" physics
             */
            else if (whichLevel == 12)
            {
                // configure a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // add a hero and destination
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // let's draw an obstacle whose underlying shape is a box, but whose
                // picture is a circle. This can be odd... our hero can roll around
                // an invisible corner on this obstacle. When debug rendering is
                // turned on (in Config.java), you'll be able to see the true shape
                // of the obstacle.
                Obstacle o1 = Obstacle.MakeAsBox(0, 0, 3.5f, 3.5f, "purpleball");
                o1.SetPhysics(1, 0, 1);

                // now let's draw an obstacle whose shape and picture are both
                // circles. The hero rolls around this nicely.
                Obstacle o2 = Obstacle.MakeAsCircle(10, 10, 3.5f, 3.5f, "purpleball");
                o2.SetPhysics(1, 0, 1);

                // draw a wall using circle physics and a stretched rectangular
                // picture. This wall will do really funny things
                Obstacle o3 = Obstacle.MakeAsCircle(20, 25, 6, 0.5f, "red");
                o3.SetPhysics(1, 0, 1);

                // draw a rectangular wall the right way, as a box
                Obstacle o4 = Obstacle.MakeAsBox(34, 2, 0.5f, 20, "red");
                o4.SetPhysics(1, 0, 1);

                PreScene.addText("An obstacle's appearance may\nnot match its physics", 255, 255, 255,
                        "Default", 32);
            }

            /*
             * this level just plays around with physics a little bit, to show how
             * friction and elasticity can do interesting things.
             */
            else if (whichLevel == 13)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("These obstacles have\ndifferent physics\nparameters", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                // Colliding the hero with these obstacles can have interesting
                // effects
                Obstacle o1 = Obstacle.MakeAsCircle(0, 0, 3.5f, 3.5f, "purpleball");
                o1.SetPhysics(0, 100, 0);
                Obstacle o2 = Obstacle.MakeAsCircle(10, 10, 3.5f, 3.5f, "purpleball");
                o2.SetPhysics(10, 0, 100);
            }

            /*
             * This level introduces goodies. Goodies are something that we collect.
             * We can make the collection of goodies lead to changes in the behavior
             * of the game, and in this example, the collection of goodies "enables"
             * a destination.
             */
            else if (whichLevel == 14)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                PreScene.addText("You must collect\ntwo blue balls", 255, 255, 255, "Default", 32);

                // Add some stationary goodies. Note that the default is
                // for goodies to not cause a change in the hero's behavior at the
                // time when a collision occurs... this is often called being a
                // "sensor"... it means that collisions are still detected by the
                // code, but they don't cause changes in momentum
                //
                // Note that LibLOL allows goodies to have one of 4 "types". By
                // default, collecting a goodie increases the "type 1" score by 1.
                Goodie.MakeAsCircle(0, 30, 2, 2, "blueball");
                Goodie.MakeAsCircle(0, 15, 2, 2, "blueball");

                // here we create a destination. Note that we now set its activation
                // score to 2, so that you must collect two goodies before the
                // destination will "work"
                Destination d = Destination.MakeAsCircle(29, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                // we must provide an activation score for each of the 4 types of
                // goodies
                d.SetActivationScore(2, 0, 0, 0);

                // let's put a display on the screen to see how many type-1 goodies
                // we've collected. Since the second parameter is "2", we'll display
                // the count as "X/2 Goodies" instead of "X Goodies"
                Controls.addGoodieCount(1, 2, "Goodies", 220, 280, "Default", 255, 255, 255, 20);
            }

            /*
             * earlier, we saw that enemies could move along a Route. So can any
             * other entity, so we'll move destinations, goodies, and obstacles,
             * too.
             */
            else if (whichLevel == 15)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Every entity can move...", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(44, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // make a destination that moves, and that requires one goodie to be
                // collected before it works
                Destination d = Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                d.SetActivationScore(1, 0, 0, 0);
                d.SetRoute(new Route(3).to(29, 6).to(29, 26).to(29, 6), 4, true);
                Score.setVictoryDestination(1);

                // make an obstacle that moves
                Obstacle o = Obstacle.MakeAsBox(0, 0, 3.5f, 3.5f, "purpleball");
                o.SetPhysics(0, 100, 0);
                o.SetRoute(new Route(3).to(0, 0).to(10, 10).to(0, 0), 2, true);

                // make a goodie that moves
                Goodie g = Goodie.MakeAsCircle(5, 5, 2, 2, "blueball");
                g.SetRoute(new Route(5).to(5, 5).to(5, 25).to(25, 25).to(9, 9).to(5, 5), 10, true);

                // draw a goodie counter in light blue (60, 70, 255) with a 12-point
                // font
                Controls.addGoodieCount(1, 0, "Goodies", 220, 280, "Default", 60, 70, 255, 12);
            }

            /*
             * Sometimes, we don't want a destination, we just want to say that the
             * player wins by collecting enough goodies. This level also shows that
             * we can set a time limit for the level, and we can pause the game.
             */
            else if (whichLevel == 16)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Collect all\nblue balls\nto win", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 20, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // draw 5 goodies
                Goodie.MakeAsCircle(.5f, .5f, 2, 2, "blueball");
                Goodie.MakeAsCircle(5.5f, 1.5f, 2, 2, "blueball");
                Goodie.MakeAsCircle(10.5f, 2.5f, 2, 2, "blueball");
                Goodie.MakeAsCircle(15.5f, 3.5f, 2, 2, "blueball");
                Goodie.MakeAsCircle(20.5f, 4.5f, 2, 2, "blueball");

                // indicate that we win by collecting enough goodies
                Score.setVictoryGoodies(5, 0, 0, 0);

                // put the goodie count on the screen
                Controls.addGoodieCount(1, 5, "Goodies", 220, 280, "Default", 60, 70, 255, 12);

                // put a simple countdown on the screen
                Controls.addCountdown(15, "Time Up!", 400, 50);

                // let's also add a screen for pausing the game. In a real game,
                // every level should have a button for pausing the game, and the
                // pause scene should have a button for going back to the main
                // menu... we'll show how to do that later.
                PauseScene.addText("Game Paused", 255, 255, 255, "Default", 32);
                Controls.addPauseButton(0, 300, 20, 20, "red");
            }

            /*
             * This level shows how "obstacles" need not actually impede the hero's
             * movement. Here, we attach "damping factors" to the hero, which let us
             * make the hero speed up or slow down based on interaction with the
             * obstacle. This level also adds a stopwatch. Stopwatches don't have
             * any meaning, but they are nice to have anyway...
             */
            else if (whichLevel == 17)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Obstacles as zoom\nstrips, friction pads\nand repellers", 255, 255,
                        255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // add a stopwatch... note that there are two ways to add a
                // stopwatch, the other of which allows for configuring the font
                Controls.addStopwatch(50, 50);

                // Create a pause scene that has a back button on it, and a button
                // for pausing the level
                PauseScene.addText("Game Paused", 255, 255, 255, "Default", 32);
                PauseScene.addBackButton("red", 0, 300, 20, 20);
                Controls.addPauseButton(0, 300, 20, 20, "red");

                // now draw three obstacles. Note that they have different dampening
                // factors. one important thing to notice is that since we place
                // these on the screen *after* we place the hero on the screen, the
                // hero will go *under* these things.

                // this obstacle's dampening factor means that on collision, the
                // hero's velocity is multiplied by -1... he bounces off at an
                // angle.
                Obstacle o = Obstacle.MakeAsCircle(10, 10, 3.5f, 3.5f, "purpleball");
                o.Damp = -1;

                // this obstacle accelerates the hero... it's like a turbo booster
                o = Obstacle.MakeAsCircle(20, 10, 3.5f, 3.5f, "purpleball");
                o.Damp = 5;

                // this obstacle slows the hero down... it's like running on
                // sandpaper. Note that the hero only slows down on initial
                // collision, not while going under it.
                o = Obstacle.MakeAsBox(30, 10, 3.5f, 3.5f, "purpleball");
                o.SetRotationSpeed(2);
                o.Damp = 0.2f;
            }

            /*
             * This level shows that it is possible to give heroes and enemies
             * different strengths, so that a hero doesn't disappear after a single
             * collision. It also shows that when an enemy defeats a hero, we can
             * customize the message that prints
             */
            else if (whichLevel == 18)
            {
                // set up a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("The hero can defeat \nup to two enemies...", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // draw a hero and give it strength of 10. The default is for
                // enemies to have "2" units of damage, and heroes to have "1" unit
                // of strength, so that any collision defeats the hero without
                // removing the enemy.
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                h.Strength = 10;

                // draw a strength meter to show this hero's strength
                Controls.addStrengthMeter("Strength", 220, 280, h);

                // our first enemy stands still:
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRotationSpeed(1);
                e.Damage = 4;
                // this text will be displayed if this enemy defeats the hero
                e.DefeatHeroText = "How did you hit me?";

                // our second enemy moves along a path
                e = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 10, true);
                e.Damage = 4;
                e.DefeatHeroText = "Stay out of my way";

                // our third enemy moves with tilt, which makes it hardest to avoid
                e = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e.SetPhysics(.1f, 0.3f, 0.6f);
                e.SetMoveByTilting();
                e.Damage = 4;
                e.DefeatHeroText = "You can't hide!";

                // be sure when testing this level to lose, with each enemy being
                // the last the hero collides with, so that you can see the
                // different messages
            }

            /*
             * This level shows that we can win a level by defeating all enemies
             */
            else if (whichLevel == 19)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("You have 10 seconds\nto defeat the enemies", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // give the hero enough strength that this will work...
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.Strength = 10;
                h.SetMoveByTilting();

                // draw a few enemies, and change their "damage" (the amount by
                // which they decrease the hero's strength)
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRotationSpeed(1);
                e.Damage = 4;
                e = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e.SetPhysics(.1f, 0.3f, 0.6f);
                e.SetMoveByTilting();
                e.Damage = 4;

                // put a countdown on the screen
                Controls.addCountdown(10, "Time Up!", 200, 25);

                // indicate that defeating all of the enemies is the way to win this
                // level
                Score.setVictoryEnemyCount();
            }

            /*
             * This level shows that a goodie can change the hero's strength, and
             * that we can win by defeating a specific number of enemies, instead of
             * all enemies.
             */
            else if (whichLevel == 20)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Collect blue balls\nto increse strength", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // our default hero only has "1" strength
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // our default enemy has "2" damage
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRotationSpeed(1);
                e.DisappearSound = "slowdown";

                // a second enemy
                e = Enemy.MakeAsCircle(35, 15, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);

                // this goodie gives an extra "5" strength:
                Goodie g = Goodie.MakeAsCircle(0, 30, 2, 2, "blueball");
                g.StrengthBoost = 5;
                g.DisappearSound = "woowoowoo";

                // Display the hero's strength
                Controls.addStrengthMeter("Strength", 220, 280, h);

                // win by defeating one enemy
                Score.setVictoryEnemyCount(1);
                PostScene.setDefaultWinText("Good enough...");
            }

            /*
             * this level introduces the idea of invincibility. Collecting the
             * goodie makes the hero invincible for a little while...
             */
            else if (whichLevel == 21)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("The blue ball will\nmake you invincible\nfor 15 seconds", 255, 255,
                        255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // draw a few enemies, and make them rotate
                for (int i = 0; i < 5; ++i)
                {
                    Enemy e = Enemy.MakeAsCircle(5 * i + 1, 25, 2, 2, "redball");
                    e.SetPhysics(1.0f, 0.3f, 0.6f);
                    e.SetRotationSpeed(1);
                }

                // this goodie makes us invincible
                Goodie g = Goodie.MakeAsCircle(30, 30, 2, 2, "blueball");
                g.InvicibilityDuration = 15;
                g.SetRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
                g.SetRotationSpeed(0.25f);

                // we'll still say you win by reaching the destination. Defeating
                // enemies is just for fun...
                Destination.MakeAsCircle(29, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // display a goodie count for type-1 goodies
                Controls.addGoodieCount(1, 0, "Goodies", 220, 280, "Default", 60, 70, 255, 12);

                // put a frames-per-second display on the screen. This is going to
                // look funny, because when debug mode is set (in Config.java), a
                // FPS will be shown on every screen anyway
                Controls.addFPS(400, 15, "Default", 200, 200, 100, 12);
            }

            /*
             * Some goodies can "count" for more than one point... they can even
             * count for negative points.
             */
            else if (whichLevel == 22)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Collect 'the right' \nblue balls to\nactivate destination", 255, 255,
                        255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination d = Destination.MakeAsCircle(29, 1, 2, 2, "mustardball");
                d.SetActivationScore(7, 0, 0, 0);
                Score.setVictoryDestination(1);

                // create some goodies with special scores. Note that we're still
                // only dealing with type-1 scores
                Goodie g1 = Goodie.MakeAsCircle(0, 30, 2, 2, "blueball");
                g1.SetScore(-2, 0, 0, 0);
                Goodie g2 = Goodie.MakeAsCircle(0, 15, 2, 2, "blueball");
                g2.SetScore(7, 0, 0, 0);

                // create some regular goodies
                Goodie.MakeAsCircle(30, 30, 2, 2, "blueball");
                Goodie.MakeAsCircle(35, 30, 2, 2, "blueball");

                // print a goodie count to show how the count goes up and down
                Controls.addGoodieCount(1, 0, "Progress", 220, 280, "Default", 60, 70, 255, 12);
            }

            /*
             * this level demonstrates that we can drag entities (in this case,
             * obstacles), and that we can make rotated obstacles. The latter could
             * be useful for having angled walls in a maze
             */
            else if (whichLevel == 23)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Rotating oblong obstacles\nand draggable obstacles", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // draw an obstacle that we can drag
                Obstacle o = Obstacle.MakeAsBox(0, 0, 3.5f, 3.5f, "purpleball");
                o.SetPhysics(0, 100, 0);
                o.SetCanDrag(true);

                // draw an obstacle that is oblong (due to its width and height) and
                // that is rotated. Note that this should be a box, or it will not
                // have the right underlying shape.
                o = Obstacle.MakeAsBox(12, 12, 3.5f, .5f, "purpleball");
                o.Rotation = 45;
            }

            /*
             * this level shows how we can use "poking" to move obstacles. In this
             * case, pressing an obstacle selects it, and pressing the screen moves
             * the obstacle to that location. Double-tapping an obstacle removes it.
             */
            else if (whichLevel == 24)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                PreScene.addText("Touch the obstacle\nto select, then" + "\ntouch to move it", 255,
                        255, 255, "Default", 32);

                // draw a picture on the default plane (0)... there are actually 5
                // planes (-2 through 2). Everything drawn on the same plane will be
                // drawn in order, so if we don't put this before the hero, the hero
                // will appear to go "under" the picture.
                Util.drawPicture(0, 0, 48, 32, "greenball", 0);

                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a pokeable obstacle
                Obstacle o = Obstacle.MakeAsBox(0, 0, 3.5f, 3.5f, "purpleball");
                o.SetPhysics(0, 100, 0);
                // '250' is a number of milliseconds. Two presses within 250
                // milliseconds will cause this obstacle to disappear, forever. Make
                // the number 0 if you want it to never disappear due to
                // double-touch.
                o.SetPokeToPlace(250);
            }

            /*
             * In this level, the enemy chases the hero
             */
            else if (whichLevel == 25)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("The enemy will chase you", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // create an enemy who chases the hero
                Enemy e3 = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e3.SetPhysics(.1f, 0.3f, 0.6f);
                e3.SetChaseSpeed(8, h, true, true);

                // draw a picture late within this block of code, but still cause
                // the picture to be drawn behind everything else by giving it a z
                // index of -1
                Util.drawPicture(0, 0, 48, 32, "greenball", -2);

                // We can change the z-index of anything... let's move the enemy to
                // -2. Since we do this after drawing the picture, it will still be
                // drawn on top of the picture, but we should also be able to see it
                // go under the destination.
                e3.ZIndex = -2;
            }

            /*
             * We can make obstacles play sounds either when we collide with them,
             * or touch them
             */
            else if (whichLevel == 26)
            {
                // set up a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Touch the purple ball \nor collide with it", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // set up our obstacle so that collision and touch make it play
                // sounds
                Obstacle o = Obstacle.MakeAsCircle(10, 10, 3.5f, 3.5f, "purpleball");
                o.SetPhysics(1, 0, 1);
                o.TouchSound = "lowpitch";
                o.SetCollideSound("hipitch", 2000);
            }

            /*
             * this hero rotates so that it faces in the direction of movement. This
             * can be useful in games where the perspective is from overhead, and
             * the hero is moving in any X or Y direction
             */
            else if (whichLevel == 27)
            {
                // set up a big screen
                Level.configure(4 * 48, 2 * 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("The star rotates in\nthe direction of movement", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red", 1, 0, 1);
                Destination.MakeAsCircle(29, 60, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // set up a hero who rotates in the direction of movement
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "stars");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetRotationByDirection();
                h.SetMoveByTilting();
                Level.setCameraChase(h);
            }

            /*
             * This level shows two things. The first is that a custom motion path
             * can allow things to violate the laws of physics and pass through
             * other things. The second is that motion paths can go off-screen.
             */
            else if (whichLevel == 28)
            {
                // set up a regular level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Reach the destination\nto win the game.", 255, 255, 255, "Default",
                        20);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(21.5f, 29, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(21.5f, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // this enemy starts from off-screen
                Enemy e = Enemy.MakeAsCircle(1, -20, 44, 44, "redball");
                e.DefeatHeroText = "Ha Ha Ha";
                e.SetRoute(new Route(3).to(1, -90).to(1, 26).to(1, -20), 30, true);
            }

            /*
             * This level shows that we can draw on the screen to create obstacles.
             * In truth, you'll probably want to change the code for this a lot, but
             * at least you'll know where to start!
             */
            else if (whichLevel == 29)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Draw on the screen\nto make obstacles appear", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(21.5f, 29, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(21.5f, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // turn on 'scribble mode'. Be sure to play with the last two
                // parameters
                Level.setScribbleMode("purpleball", 3, 1.5f, 1.5f, 0, 0, 0, true, 10);
            }

            /*
             * This level shows that we can "flick" things to move them. Notice that
             * we do not enable tilt! Instead, we specified that there is a default
             * gravity in the Y dimension pushing everything down. This is much like
             * gravity on earth. The only way to move things, then, is via flicking
             * them.
             */
            else if (whichLevel == 30)
            {
                // create a level with a constant force downward in the Y dimension
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Destination.MakeAsCircle(30, 10, 2.5f, 2.5f, "mustardball");
                Score.setVictoryDestination(1);

                // create a hero who we can flick
                Hero h = Hero.MakeAsCircle(4, 27, 3, 3, "stars");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetFlickable(1f);
                h.DisableRotation();
            }

            /*
             * this level introduces a new concept: side-scrolling games. Just like
             * in level 30, we have a constant force in the negative Y direction.
             * However, in this level, we say that tilt can produce forces in X but
             * not in Y. Thus we can tilt to move the hero left/right. Note, too,
             * that the hero will fall to the floor, since there is a constant
             * downward force, but there is not any mechanism to apply a Y force to
             * make it move back up.
             */
            else if (whichLevel == 31)
            {
                // make a long level but not a tall level, and provide a constant
                // downward force:
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                // turn on tilt, but only in the X dimension
                Tilt.enable(10, 0);
                PreScene.addText("Side scroller / tilt demo", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                Level.setCameraChase(h);
            }

            /*
             * In the previous level, it was hard to see that the hero was moving.
             * We can make a background layer to remedy this situation. Notice that
             * the background uses transparency to show the blue color for part of
             * the screen
             */
            else if (whichLevel == 32)
            {
                // start by repeating the previous level:
                Level.configure(30 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Side scroller / tilt demo", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 30 * 48, 32, "red", 1, 0, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(30 * 48 - 5, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                Level.setCameraChase(h);

                // now paint the background blue
                Background.setColor(23, 180, 255);

                // put in a picture that scrolls at half the speed of the hero in
                // the x direction. Note that background "layers" are all drawn
                // *before* anything that is drawn with a z index... so the
                // background will be behind the hero
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // make an obstacle that hovers in a fixed place. Note that hovering
                // and zoom do not work together nicely.
                Obstacle o = Obstacle.MakeAsCircle(10, 10, 5, 5, "blueball");
                o.SetHover(100, 100);

                // Add a meter to show how far the hero has traveled
                Controls.addDistanceMeter(" m", 5, 300, "Default", 255, 255, 255, 16, h);

                // Add some text about the previous best score. If you look in the
                // onLevelCompleteTrigger() code (far below in this file), you'll
                // see that when this level ends, we save the best score. Once the
                // score is saved, it is saved permanently on the phone, though
                // every re-execution on the desktop resets the best score.
                Util.drawText(30, 30, "best: " + Score.readPersistent("HighScore", 0) + "M", 0, 0, 0,
                        "Default", 12, 0);
            }

            /*
             * this level adds multiple background layers, and it also allows the
             * hero to jump via touch
             */
            else if (whichLevel == 33)
            {
                // set up a standard side scroller with tilt:
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press the hero to\nmake it jump", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);
                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a hero
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Level.setCameraChase(h);
                // this says that touching makes the hero jump
                h.SetTouchToJump();
                // this is the force of a jump. remember that up is positive.
                h.SetJumpImpulses(0, 10);
                // the sound to play when we jump
                h.JumpSound = "fwapfwap";

                // set up our background again, but add a few more layers
                Background.setColor(23, 180, 255);
                // this layer has a scroll factor of 0... it won't move
                Background.addHorizontalLayer(0, 1, "back", 0);
                // this layer moves at half the speed of the hero
                Background.addHorizontalLayer(.5f, 1, "mid", 0);
                // this layer is faster than the hero
                Background.addHorizontalLayer(1.25f, 1, "front", 20);
            }

            /*
             * tilt doesn't always work so nicely in side scrollers. An alternative
             * is for the hero to have a fixed rate of motion. Another issue was
             * that you had to touch the hero itself to make it jump. Now, we use an
             * invisible button so touching any part of the screen makes the hero
             * jump.
             */
            else if (whichLevel == 34)
            {
                // set up a side scroller, but don't turn on tilt
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press anywhere to jump", 255, 255, 255, "Default", 32);
                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // note: the bounding box does not have friction, and neither does
                // the hero
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 0, 0, 0);

                // make a hero, and ensure that it doesn't rotate
                Hero h = Hero.MakeAsCircle(2, 0, 3, 7, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0);
                // give the hero a fixed velocity
                h.AddVelocity(25, 0, false);
                // center the camera a little ahead of the hero, so he is not
                // centered
                h.SetCameraOffset(15, 0);
                // enable jumping
                h.SetJumpImpulses(0, 10);
                Level.setCameraChase(h);
                // set up the background
                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // draw a jump button that covers the whole screen
                Controls.addJumpButton(0, 0, 480, 320, "", h);

                // if the hero jumps over the destination, we have a problem. To fix
                // it, let's put an invisible enemy right after the destination, so
                // that if the hero misses the destination, it hits the enemy and we
                // can start over. Of course, we could just do the destination like
                // this instead, but this is more fun...
                Enemy.MakeAsBox(130, 0, .5f, 32, "");
            }

            /*
             * the default is that once a hero jumps, it can't jump again until it
             * touches an obstacle (floor or wall). Here, we enable multiple jumps.
             * Coupled with a small jump impulse, this makes jumping feel more like
             * swimming or controlling a helicopter.
             */
            else if (whichLevel == 35)
            {
                Level.configure(3 * 48, 38);
                Physics.Configure(0, -10);
                PreScene.addText("Multi-jump is enabled", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 38, "red", 1, 0, 0);
                Hero h = Hero.MakeAsBox(2, 0, 3, 7, "greenball");
                h.DisableRotation();
                h.SetPhysics(1, 0, 0);
                h.AddVelocity(5, 0, false);
                Level.setCameraChase(h);
                h.SetCameraOffset(15, 0);
                // the hero now has multijump, with small jumps:
                h.SetMultiJumpOn();
                h.SetJumpImpulses(0, 6);

                // this is all the same as before, to include the invisible enemy
                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);
                Controls.addJumpButton(0, 0, 480, 320, "", h);
                Destination.MakeAsCircle(120, 31, 2, 2, "mustardball");
                Enemy.MakeAsBox(130, 0, .5f, 38, "");
                Score.setVictoryDestination(1);
            }

            /*
             * This level shows that we can make a hero move based on how we touch
             * the screen
             */
            else if (whichLevel == 36)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, 0);
                PreScene.addText("Press screen borders\nto move the hero", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);
                Hero h = Hero.MakeAsCircle(2, 0, 3, 3, "stars");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                Level.setCameraChase(h);

                // this lets the hero flip its image when it moves backwards
                h.SetCanFaceBackwards();

                Destination.MakeAsCircle(120, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // let's draw an enemy, just in case anyone wants to try to go to
                // the top left corner
                Enemy.MakeAsCircle(3, 27, 3, 3, "redball");

                // draw some buttons for moving the hero
                Controls.addLeftButton(0, 50, 50, 220, "", 15, h);
                Controls.addRightButton(430, 50, 50, 220, "", 15, h);
                Controls.addUpButton(50, 270, 380, 50, "", 15, h);
                Controls.addDownButton(50, 0, 380, 50, "", 15, h);
            }

            /*
             * In the last level, we had complete control of the hero's movement.
             * Here, we give the hero a fixed velocity, and only control its up/down
             * movement.
             */
            else if (whichLevel == 37)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, 0);
                PreScene.addText("Press screen borders\nto move up and down", 255, 255, 255,
                        "Default", 32);
                // The box and hero should not have friction
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 0);
                Destination.MakeAsCircle(120, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0);
                h.AddVelocity(10, 0, false);

                Level.setCameraChase(h);

                // draw an enemy to avoid, and one at the end
                Enemy.MakeAsCircle(53, 28, 3, 3, "redball");
                Enemy.MakeAsBox(130, 0, .5f, 32, "");

                // draw the up/down controls
                Controls.addDownButton(50, 0, 380, 50, "", 15, h);
                Controls.addUpButton(50, 270, 380, 50, "", 15, h);
            }

            /*
             * this level demonstrates crawling heroes. We can use this to simulate
             * crawling, ducking, rolling, spinning, etc. Note, too, that we can use
             * it to make the hero defeat certain enemies via crawl.
             */
            else if (whichLevel == 38)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press the screen\nto crawl", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 0);
                Destination.MakeAsCircle(120, 0, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
                Hero h = Hero.MakeAsBox(2, 1, 3, 7, "greenball");
                h.SetPhysics(.1f, 0, 0);
                h.AddVelocity(5, 0, false);
                Level.setCameraChase(h);
                // to enable crawling, we just draw a crawl button on the screen
                Controls.addCrawlButton(0, 0, 480, 320, "", h);

                // make an enemy who we can defeat by colliding with it while
                // crawling
                Enemy e = Enemy.MakeAsCircle(110, 1, 5, 5, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetDefeatByCrawl();
            }

            /*
             * We can make a hero start moving only when it is pressed. This can
             * even let the hero hover until it is pressed. We could also use this
             * to have a game where the player puts obstacles in place, then starts
             * the hero moving.
             */
            else if (whichLevel == 39)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press the hero\nto start moving\n", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 0);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                Destination.MakeAsCircle(120, 0, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a hero who doesn't start moving until it is touched
                //
                // note that this hero is a box, and the hero is "norotate". You
                // will probably get strange behaviors if you choose any other
                // options
                Hero h = Hero.MakeAsBox(2, 1, 3, 7, "greenball");
                h.DisableRotation();
                h.SetPhysics(1, 0, 0);
                h.SetTouchAndGo(10, 0);
                Level.setCameraChase(h);
            }

            /*
             * LibLOL has limited support for SVG. If you draw a picture in Inkscape
             * or another SVG tool, and it only consists of lines, then you can
             * import it into your game as an obstacle. Drawing a picture on top of
             * the obstacle is probably a good idea, though we don't bother in this
             * level
             */
            else if (whichLevel == 40)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                Tilt.setAsVelocity(true);
                PreScene.addText("Obstacles can\nbe drawn from SVG\nfiles", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 1);

                // make a hero who can jump
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetJumpImpulses(0, 20);
                h.SetTouchToJump();
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                // draw an obstacle from SVG
                Svg.importLineDrawing("shape.svg", 1, 0, 0, 2f, .5f, 25f, 15f);

                // notice that we can only get to the destination by jumping from
                // *on top of* the obstacle
                Destination.MakeAsCircle(120, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);
            }

            /*
             * In a side-scrolling game, it is useful to be able to change the
             * hero's speed either permanently or temporarily. In LibLOL, we can use
             * a collision between a hero and an obstacle to achieve this effect.
             */
            else if (whichLevel == 41)
            {
                Level.configure(10 * 48, 32);
                Physics.Configure(0, 0);
                PreScene.addText("Speed boosters and reducers", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 10 * 480, 320, "", 1, 0, 1);

                Hero h = Hero.MakeAsCircle(2, 0, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                h.AddVelocity(10, 0, false);
                Level.setCameraChase(h);

                Destination.MakeAsCircle(450, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // place a speed-up obstacle that lasts for 2 seconds
                Obstacle o1 = Obstacle.MakeAsCircle(40, 1, 4, 4, "purpleball");
                o1.SetSpeedBoost(20, 0, 2);

                // place a slow-down obstacle that lasts for 3 seconds
                Obstacle o2 = Obstacle.MakeAsCircle(120, 1, 4, 6, "purpleball");
                o2.SetSpeedBoost(-9, 0, 3);

                // place a permanent +3 speedup obstacle... the -1 means "forever"
                Obstacle o3 = Obstacle.MakeAsCircle(240, 1, 4, 4, "purpleball");
                o3.SetSpeedBoost(20, 0, -1);

            }

            /*
             * this is a very gross level, which exists just to show that
             * backgrounds can scroll vertically.
             */
            else if (whichLevel == 42)
            {
                // set up a level where tilt only makes the hero move up and down
                Level.configure(48, 4 * 32);
                Physics.Configure(0, 0);
                Tilt.enable(0, 10);
                PreScene.addText("Vertical scroller demo", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 4 * 32, "red", 1, 0, 1);

                Hero h = Hero.MakeAsCircle(2, 120, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                Destination.MakeAsBox(0, 2, 48, 1, "mustardball");
                Score.setVictoryDestination(1);

                // set up vertical scrolling backgrounds
                Background.setColor(255, 0, 255);
                Background.addVerticalLayer(1, 0, "back", 0);
                Background.addVerticalLayer(1, .5f, "mid", 0);
                Background.addVerticalLayer(1, 1, "front", 0);
            }

            /*
             * the next few levels demonstrate support for throwing projectiles. In
             * this level, we throw projectiles by touching the hero. Here, the
             * projectile always goes in the same direction
             */
            else if (whichLevel == 43)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Press the hero\nto make it throw\nprojectiles", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // create a hero, and indicate that touching it makes it throw
                // projectiles
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetTouchToThrow(h);
                h.SetMoveByTilting();

                // configure a pool of projectiles. We say that there can be no more
                // than 3 projectiles in flight at any time, that the projectiles
                // fly straight up, and that they fly out of the top of the hero.
                ProjectilePool.configure(3, 1, 1, "greyball", 0, 10, 1.5f, 3, 1, 0, true);
            }

            /*
             * This is another demo of how throwing projectiles works. Like the
             * previous demo, it doesn't actually use projectiles for anything, it
             * is just to show how to get some different behaviors in terms of how
             * the projectiles move. In this case, we show that we can limit the
             * distance that projectiles travel, and that we can put a control on
             * the HUD for throwing projectiles
             */
            else if (whichLevel == 44)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press anywhere\nto throw a gray\nball", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 30, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Destination.MakeAsCircle(120, 0, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // set up a pool of projectiles, but now once the projectiles travel
                // more than 25 meters, they disappear
                ProjectilePool.configure(100, 1, 1, "greyball", 30, 0, 3, 1.5f, 1, 0, true);
                ProjectilePool.setRange(25);

                // add a button for throwing projectiles. Notice that this butotn
                // keeps throwing as long as it is held, but we've capped it to
                // throw no more than once per 100 milliseconds
                Controls.addThrowButton(0, 0, 480, 320, "", h, 100);
                Level.setCameraChase(h);
            }

            /*
             * this level demonstrates that we can defeat enemies by throwing
             * projectiles at them
             */
            else if (whichLevel == 45)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                PreScene.addText("Defeat all enemies\nto win", 255, 255, 255, "Default", 32);

                Hero h = Hero.MakeAsCircle(4, 27, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // set up our projectiles... note that now projectiles each do 2
                // units of damage
                ProjectilePool.configure(3, .4f, .1f, "greyball", 0, 10, .2f, -.5f, 2, 0, true);

                // draw a few enemies... note that they have different amounts of
                // damage, so it takes different numbers of projectiles to defeat
                // them.
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRotationSpeed(1);
                for (int i = 1; i < 20; i += 5)
                {
                    Enemy ee = Enemy.MakeAsCircle(i, i + 5, 2, 2, "redball");
                    ee.SetPhysics(1.0f, 0.3f, 0.6f);
                    ee.Damage = i;
                }

                // win by defeating enemies, of course!
                Score.setVictoryEnemyCount();

                // this button only throws one projectile per press...
                Controls.addSingleThrowButton(0, 0, 480, 320, "", h);
            }

            /*
             * This level shows how to throw projectiles in a variety of directions.
             */
            else if (whichLevel == 46)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press anywhere\nto throw a ball", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                Destination.MakeAsCircle(120, 0, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // draw a button for throwing projectiles in many directions...
                // again, note that if we hold the button, it keeps throwing
                Controls.addVectorThrowButton(0, 0, 480, 320, "", h, 100);

                // set up our pool of projectiles. The main challenge here is that
                // the farther from the hero we press, the faster the projectile
                // goes, so we multiply the velocity by .8 to slow it down a bit
                ProjectilePool.configure(100, 1, 1, "greyball", 30, 0, 0, 0, 1, 0, true);
                ProjectilePool.setProjectileVectorDampeningFactor(.8f);
                ProjectilePool.setRange(30);
            }

            /*
             * this level shows that with the "vector" projectiles, we can still
             * have gravity affect the projectiles. This is very good for
             * basketball-style games.
             */
            else if (whichLevel == 47)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press anywhere\nto throw a ball", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(0, 0, 0.6f);
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                Destination.MakeAsCircle(120, 0, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // we use a "single throw" button so that holding doesn't throw more
                // projectiles.
                Controls.addVectorSingleThrowButton(0, 0, 480, 320, "", h);

                // we turn on projectile gravity, and then we enable collisions for
                // projectiles. This means that when a projectile collides with
                // something, it will transfer its momentum to that thing, if that
                // thing is moveable. This is a step toward our goal of being able
                // to bounce a basketball off of a backboard, but it's not quite
                // enough...
                ProjectilePool.configure(100, 1, 1, "greyball", 30, 0, 1.5f, 1.5f, 1, 0, true);
                ProjectilePool.setProjectileVectorDampeningFactor(.8f);
                ProjectilePool.setRange(40);
                ProjectilePool.setProjectileGravityOn();
                ProjectilePool.enableCollisionsForProjectiles();

                // This next line is interesting... it lets projectiles collide with
                // each other without disappearing
                ProjectilePool.setCollisionOk();

                // Draw an obstacle... this is like our backboard, but we're putting
                // it in a spot that's more useful for testing than for playing a
                // game
                Obstacle o = Obstacle.MakeAsBox(10, 20, 2, 2, "red");

                // now comes the tricky part... we want to make it so that when the
                // ball hits the obstacle (the backboard), it doesn't disappear. The
                // only time a projectile does not disappear when hitting an
                // obstacle is when you provide custom code to run on a
                // projectile/obstacle collision... in that case, you are
                // responsible for removing the projectile (or for not removing it).
                // That being the case, we can set a "trigger" to run custom code
                // when the projectile and obstacle collide, and then just have the
                // custom code do nothing.

                // this line says when a projectile and obstacle collide, if the
                // goodie counts are at least 0,0,0,0, then run the
                // "onProjectileCollideTrigger()" method that appears later in this
                // file. When onProjectileCollideTrigger() runs, it will have an id
                // of 7, because we arbitrarily picked 7 as the id... for this
                // simple example, our use of an id isn't important, but for more
                // complex games, having different ids can be really useful.
                o.SetProjectileCollisionTrigger(7, 0, 0, 0, 0);
            }

            /*
             * This level shows how we can attach a timer to an enemy. When the
             * timer runs out, if the enemy is still visible, then some custom code
             * will run. We can use this to simulate cancer cells or fire on a
             * building. The value of attaching the timer to the enemy is that we
             * can change the game state at the position where the enemy is. One
             * team even had an enemy that dropped goodies at its current location.
             * Note that the timer only runs once... you'll need to make a new timer
             * from within the code that runs when the timer expires.
             */
            else if (whichLevel == 48)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 0);
                PreScene.addText("Throw balls at \nthe enemies before\nthey reproduce", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetTouchToThrow(h);
                h.SetMoveByTilting();

                // configure a pool of projectiles... now we have a sound that plays
                // when a projectile is thrown, and another for when it disappears
                ProjectilePool.configure(100, .5f, .5f, "greyball", 0, 10, 2, -.5f, 1, 0, true);
                ProjectilePool.setThrowSound("fwapfwap");
                ProjectilePool.setProjectileDisappearSound("slowdown");

                // draw an enemy that makes a sound when it disappears
                Enemy e = Enemy.MakeAsCircle(23, 20, 1, 1, "redball");
                e.DisappearSound = "lowpitch";

                // request that in 2 seconds, if the enemy is still visible,
                // onEnemyTimerTrigger() will run, with id == 2. Be sure to look at
                // the onEnemyTimerTrigger code (below) for more information.
                Level.setEnemyTimerTrigger(2, 2, e);

                // win by defeating enemies
                Score.setVictoryEnemyCount();

                // put a count of defeated enemies on the screen
                Controls.addDefeatedCount(0, "Enemies Defeated", 20, 20);
            }

            /*
             * This level shows that we can have moveable enemies that reproduce. Be
             * careful... it is possible to make a lot of enemies, really quickly
             */
            else if (whichLevel == 49)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("These enemies are\nreally tricky", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                Destination.MakeAsCircle(29, 29, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make our initial enemy
                Enemy e = Enemy.MakeAsCircle(23, 2, 1, 1, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetMoveByTilting();

                // set a timer trigger on the enemy. warning: "6" is going to lead
                // to lots of enemies eventually, and there's no way to defeat them
                // in this level! Again, be sure to look at onEnemyTimerTrigger()
                // below.
                Level.setEnemyTimerTrigger(6, 2, e);
            }

            /*
             * this level shows simple animation. Every entity can have a default
             * animation.
             */
            else if (whichLevel == 50)
            {
                // set up a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Make a wish!", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // this hero will be animated:
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "stars");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // this says that we scroll through the 0, 1, 2, and 3 cells of the
                // image, and we show each for 200 milliseconds. This is the "easy"
                // animation mechanism, where every cell is shown for the same
                // amount of time
                h.DefaultAnimation = new Animation("stars", 200, true, 0, 1, 2, 3);
            }

            /*
             * this level introduces jumping animations and disappearance animations
             */
            else if (whichLevel == 51)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press the hero to\nmake it jump", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);

                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a hero, and give it two animations: one for when it is in
                // the air, and another for the rest of the time.
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "stars");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetJumpImpulses(0, 20);
                h.SetTouchToJump();
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                // this is the more complex form of animation... we show the
                // different cells for different lengths of time
                h.DefaultAnimation = new Animation("stars", 4, true).to(0, 150).to(1, 200)
                        .to(2, 300).to(3, 350);
                // we can use the complex form to express the simpler animation, of
                // course
                h.JumpAnimation = new Animation("stars", 4, true).to(4, 200).to(5, 200).to(6, 200)
                        .to(7, 200);

                // create a goodie that has a disappearance animation. When the
                // goodie is ready to disappear, we'll remove it, and then we'll run
                // the disappear animation. That means that we can make it have any
                // size we want, but we need to offset it from the (defunct)
                // goodie's position. Note, too, that the final cell is blank, so
                // that we don't leave a residue on the screen.
                Goodie g = Goodie.MakeAsCircle(15, 9, 5, 5, "stars");
                g.SetDisappearAnimation(new Animation("starburst", 4, false).to(2, 200).to(1, 200)
                        .to(0, 200).to(3, 200), 1, 0, 5, 5);
            }

            /*
             * this level shows that projectiles can be animated, and that we can
             * animate the hero while it throws a projectile
             */
            else if (whichLevel == 52)
            {
                // set up a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Press the hero\nto make it\nthrow a ball", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // set up our hero
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "colorstar");
                h.SetPhysics(1, 0, 0.6f);
                h.SetTouchToThrow(h);
                h.SetMoveByTilting();

                // set up an animation when the hero throws:
                h.ThrowAnimation = new Animation("colorstar", 2, false).to(3, 100).to(4, 500);

                // make a projectile pool and give an animation pattern for the
                // projectiles
                ProjectilePool.configure(100, 1, 1, "flystar", 0, 10, 0, -.5f, 1, 0, true);
                ProjectilePool.setAnimation(new Animation("flystar", 100, true, 0, 1));
            }

            /*
             * This level explores invincibility animation. While we're at it, we
             * make some enemies that aren't affected by invincibility, and some
             * that can even damage the hero while it is invincible.
             */
            else if (whichLevel == 53)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("The blue ball will\nmake you invincible\nfor 15 seconds", 50, 50,
                        255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Destination.MakeAsCircle(29, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make an animated hero, and give it an invincibility animation
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "colorstar");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                h.DefaultAnimation = new Animation("colorstar", 4, true).to(0, 300).to(1, 300)
                        .to(2, 300).to(3, 300);
                h.InvicibleAnimation = new Animation("colorstar", 4, true).to(4, 100).to(5, 100)
                        .to(6, 100).to(7, 100);

                // make some enemies
                for (int i = 0; i < 5; ++i)
                {
                    Enemy e2 = Enemy.MakeAsCircle(5 * i + 1, 25, 2, 2, "redball");
                    e2.SetPhysics(1.0f, 0.3f, 0.6f);
                    e2.SetRotationSpeed(1);
                    e2.Damage = 4;
                    e2.DisappearSound = "hipitch";

                    // The first enemy we create will harm the hero even if the hero
                    // is invincible
                    if (i == 0)
                        e2.SetImmuneToInvicibility();
                    // the second enemy will not be harmed by invincibility, but
                    // won't harm an invincible hero
                    if (i == 1)
                        e2.SetResistInvicibility();
                }
                // neat trick: this enemy does zero damage, but slows the hero down.
                Enemy e = Enemy.MakeAsCircle(30, 20, 2, 2, "redball");
                e.SetPhysics(10, 0.3f, 0.6f);
                e.SetMoveByTilting();
                e.Damage = 0;

                // add a goodie that makes the hero invincible
                Goodie g = Goodie.MakeAsCircle(30, 30, 2, 2, "blueball");
                g.InvicibilityDuration = 15;
                g.SetRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
                g.SetRotationSpeed(0.25f);
                Controls.addGoodieCount(1, 0, "Goodies", 220, 280, "Default", 60, 70, 255, 12);

                // draw a picture when the level is won, and don't print text...
                // this particular picture isn't very useful
                PostScene.addWinImage("fade", 0, 0, 480, 320);
                PostScene.setDefaultWinText("");
            }

            /*
             * demonstrate crawl animation, and also show that on multitouch phones,
             * we can "crawl" in the air while jumping.
             */
            else if (whichLevel == 54)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press the left side of\nthe screen to crawl\n"
                        + "or the right side\nto jump.", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 0);

                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a hero with fixed velocity, and give it crawl and jump
                // animations
                Hero h = Hero.MakeAsBox(2, 1, 3, 7, "stars");
                h.SetPhysics(1, 0, 0);
                h.AddVelocity(15, 0, false);
                h.CrawlAnimation = new Animation("stars", 4, true).to(0, 100).to(1, 300)
                        .to(2, 300).to(3, 100);
                h.JumpAnimation = new Animation("stars", 4, true).to(4, 200).to(5, 200).to(6, 200)
                        .to(7, 200);

                // enable hero jumping and crawling
                h.SetJumpImpulses(0, 15);
                Controls.addJumpButton(0, 0, 240, 320, "", h);
                Controls.addCrawlButton(241, 0, 480, 320, "", h);

                // add an enemy we can defeat via crawling, just for fun. It should
                // be defeated even by a "jump crawl"
                Enemy e = Enemy.MakeAsCircle(110, 1, 5, 5, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetDefeatByCrawl();

                // include a picture on the "try again" screen
                PostScene.addLoseImage("fade", 0, 0, 480, 320);
                PostScene.setDefaultLoseText("Oh well...");
                Level.setCameraChase(h);
            }

            /*
             * This isn't quite the same as animation, but it's nice. We can
             * indicate that a hero's image changes depending on its strength. This
             * can, for example, allow a hero to change (e.g., get healthier) by
             * swapping through images as goodies are collected, or allow the hero
             * to switch its animation depending on how many enemies it has collided
             * with
             */
            else if (whichLevel == 55)
            {
                // set up a basic level with a bunch of goodies
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // Since colorstar has 8 frames, and we're displaying frame 0 as
                // "health == 0", let's add 7 more goodies, each of which adds 1 to
                // the hero's strength.
                for (int i = 0; i < 7; ++i)
                {
                    Goodie g = Goodie.MakeAsCircle(5 + 2 * i, 5 + 2 * i, 2, 2, "blueball");
                    g.StrengthBoost = 1;
                }

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make 8 enemies, each with strength == 1. This means we can lose
                // the level, and that we can test moving our strength all the way
                // up to 7, and all the way back down to 0.
                for (int i = 0; i < 8; ++i)
                {
                    Enemy e = Enemy.MakeAsCircle(5 + 2 * i, 1 + 2 * i, 2, 2, "redball");
                    e.Damage = 1;
                }

                // Note: colorstar has 8 cells...
                Hero h = Hero.MakeAsCircle(4, 27, 3, 3, "colorstar");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // Be sure to look at onStrengthChangeTrigger. As the hero's
                // strength moves up and down, its image will change.
            }

            /*
             * demonstrate that obstacles can defeat enemies, and that we can use
             * this feature to have obstacles that only defeat certain "marked"
             * enemies
             */
            else if (whichLevel == 56)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                // increase the speed at which tilt affects velocity
                Tilt.setGravityMultiplier(3);
                PreScene.addText("You can defeat\ntwo enemies with\nthe blue ball", 255, 255, 255,
                        "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, 0, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(1, 0, 0.6f);
                h.SetMoveByTilting();

                // put an enemy defeated count on the screen, in red with a small
                // font
                Controls.addDefeatedCount(2, "Enemies Defeated", 20, 20, "Default", 255, 0, 0, 10);

                // make a moveable obstacle that can defeat enemies
                Obstacle o = Obstacle.MakeAsCircle(10, 2, 4, 4, "blueball");
                o.SetPhysics(.1f, 0, 0.6f);
                o.SetMoveByTilting();
                // this says that we don't need to collect any goodies before this
                // obstacle defeats enemies (0,0,0,0), and that when this obstacle
                // collides with any enemy, the onEnemyCollideTrigger() code will
                // run, with id == 14. Notice, too, that there will be a half second
                // delay before the code runs.
                o.SetEnemyCollisionTrigger(14, 0, 0, 0, 0, .5f);

                // make a small obstacle that can also defeat enemies, but doesn't
                // disappear
                Obstacle o2 = Obstacle.MakeAsCircle(.5f, .5f, 2, 2, "blueball");
                o2.SetPhysics(1, 0, 0.6f);
                o2.SetMoveByTilting();
                o2.SetEnemyCollisionTrigger(1, 0, 0, 0, 0, 0);

                // make four enemies
                Enemy e = Enemy.MakeAsCircle(40, 2, 4, 4, "redball");
                e.SetPhysics(1, 0, 0.6f);
                e.SetMoveByTilting();
                Enemy e1 = Enemy.MakeAsCircle(30, 2, 4, 4, "redball");
                e1.SetPhysics(1, 0, 0.6f);
                Enemy e2 = Enemy.MakeAsCircle(40, 22, 2, 2, "redball");
                e2.SetPhysics(1, 0, 0.6f);
                e2.SetMoveByTilting();
                Enemy e3 = Enemy.MakeAsCircle(40, 12, 4, 4, "redball");
                e3.SetPhysics(1, 0, 0.6f);
                e3.SetMoveByTilting();

                // now let's put a note into e2 and e3
                e2.TextInfo = "small";
                e3.TextInfo = "big";

                // win by defeating enemies
                Score.setVictoryEnemyCount(2);

                // be sure to look at onEnemyCollideTrigger to see how this level
                // will play out.
            }

            /*
             * 57 this level shows an odd way of moving the hero. There's friction
             * on the floor, so it can only move by tilting while the hero is in the
             * air
             */
            else if (whichLevel == 57)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press the hero to\nmake it jump", 255, 255, 255, "Default", 32);
                // note: the floor has friction
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);

                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // make a box hero with friction... it won't roll on the floor, so
                // it's stuck!
                Hero h = Hero.MakeAsBox(2, 2, 3, 3, "stars");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 5);
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                // the hero *can* jump...
                h.SetTouchToJump();
                h.SetJumpImpulses(0, 15);

                // draw a background
                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);
            }

            /*
             * this level shows that we can put an obstacle on the screen and use it
             * to make the hero throw projectiles. It also shows that we can make
             * entities that shrink over time... growth is possible too, with a
             * negative value.
             */
            else if (whichLevel == 58)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(4, 5, 3, 3, "greenball");
                h.SetPhysics(1, 0, 0.6f);
                h.SetMoveByTilting();

                // make an obstacle that causes the hero to throw Projectiles when
                // touched
                Obstacle o = Obstacle.MakeAsCircle(43, 27, 5, 5, "purpleball");
                o.CollisionEffect = false;
                o.SetTouchToThrow(h);

                // set up our projectiles
                ProjectilePool.configure(3, 1, 1, "colorstar", 0, 15, 0, 0, 2, 0, true);
                ProjectilePool.setNumberOfProjectiles(20);
                // there are only 20... throw them carefully

                // Allow the projectile image to be chosen randomly from a sprite
                // sheet
                ProjectilePool.setImageSource("colorstar");

                // show how many shots are left
                Controls.addProjectileCount("projectiles left", 5, 300, "Default", 255, 255, 255, 12);

                // draw a bunch of enemies to defeat
                Enemy e = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e.SetPhysics(1.0f, 0.3f, 0.6f);
                e.SetRotationSpeed(1);
                for (int i = 1; i < 20; i += 5)
                    Enemy.MakeAsCircle(i, i + 8, 2, 2, "redball");

                // draw a few obstacles that shrink over time, to show that circles
                // and boxes work, we can shrink the X and Y rates independently,
                // and we can opt to center things as they shrink or grow
                Obstacle floor = Obstacle.MakeAsBox(2, 3, 42, 3, "red");
                floor.SetShrinkOverTime(1, 1, true);

                Obstacle roof = Obstacle.MakeAsBox(24, 30, 1, 1, "red");
                roof.SetShrinkOverTime(-1, 0, false);

                Obstacle ball1 = Obstacle.MakeAsCircle(40, 8, 8, 8, "purpleball");
                ball1.SetShrinkOverTime(1, 2, true);

                Obstacle ball2 = Obstacle.MakeAsCircle(40, 16, 8, 8, "purpleball");
                ball2.SetShrinkOverTime(2, 1, false);

                Score.setVictoryEnemyCount(5);
            }

            /*
             * @level: 59 this level shows that we can make a hero in the air
             * rotate. Rotation doesn't do anything, but it looks nice...
             */
            else if (whichLevel == 59)
            {
                // make a simple level
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // warning: this hero is just out of the hero's reach when the hero
                // jumps... you'll have to hit the side wall and jump again to reach
                // it!
                Destination.MakeAsCircle(46, 8, 2.5f, 2.5f, "mustardball");
                Score.setVictoryDestination(1);

                // make the hero jumpable, so that we can see it spin in the air
                Hero h = Hero.MakeAsCircle(4, 27, 3, 3, "stars");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                h.SetJumpImpulses(0, 10);
                h.SetTouchToJump();

                // add rotation buttons
                Controls.addRotateButton(0, 240, 80, 80, "", -.5f, h);
                Controls.addRotateButton(380, 240, 80, 80, "", .5f, h);
            }

            /**
             * we can attach movement buttons to any moveable entity, so in this
             * case, we attach it to an obstacle to get an arkanoid-like effect.
             */
            else if (whichLevel == 60)
            {
                // make a simple level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 0, 0, 0);

                Destination.MakeAsCircle(30, 10, 2.5f, 2.5f, "mustardball");
                Score.setVictoryDestination(1);

                // make a hero who is always moving... note there is no friction,
                // anywhere, and the hero is elastic... it won't ever stop...
                Hero h = Hero.MakeAsCircle(4, 4, 3, 3, "greenball");
                h.SetPhysics(0, 1, 0);
                h.AddVelocity(0, 10, false);

                // make an obstacle and then connect it to some controls
                Obstacle o = Obstacle.MakeAsBox(2, 30.9f, 4, 1, "red");
                o.SetPhysics(100, 1, 0);
                Controls.addLeftButton(0, 0, 240, 320, "", 5, o);
                Controls.addRightButton(240, 0, 240, 320, "", 5, o);
            }

            /*
             * this level demonstrates that things can appear and disappear on
             * simple timers
             */
            else if (whichLevel == 61)
            {
                // set up a basic level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Things will appear \nand disappear...", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // create an enemy that will quietly disappear after 2 seconds
                Enemy e1 = Enemy.MakeAsCircle(25, 25, 2, 2, "redball");
                e1.SetPhysics(1.0f, 0.3f, 0.6f);
                e1.SetRotationSpeed(1);
                e1.SetDisappearDelay(2, true);

                // create an enemy that will appear after 3 seconds
                Enemy e2 = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                e2.SetPhysics(1.0f, 0.3f, 0.6f);
                e2.SetRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 3, true);
                e2.AppearDelay = 3;
            }

            /*
             * This level demonstrates the use of timer triggers. We can use timers
             * to make more of the level appear over time. In this case, we'll chain
             * the timer triggers together, so that we can get more and more things
             * to develop. Be sure to look at the onTimerTrigger code to see how the
             * rest of this level works.
             * @demonstrates: destinations and goodies with fixed velocities
             * @demonstrates: enemy who disappears when it is touched
             * @demonstrates: enemy who can be dragged around
             * @demonstrates: timer triggers
             */
            else if (whichLevel == 62)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                PreScene.addText("There's nothing to\ndo... yet", 255, 255, 255, "Default", 20);

                // note: there's no destination yet, but we still say it's how to
                // win... we'll get a destination in this level after a few timers
                // run...
                Score.setVictoryDestination(1);

                // now set a timer trigger. after three seconds, the
                // onTimerTrigger() method will run, with level=62 and id=0
                Level.setTimerTrigger(0, 3);
            }

            /*
             * This level shows triggers that run on a collision between hero and
             * obstacle. In this case, it lets us draw out the next part of the
             * level later, instead of drawing the whole thing right now. In a real
             * level, we'd draw a few screens at a time, and not put the trigger
             * obstacle at the end of a screen, so that we'd never see the drawing
             * of stuff taking place, but for this demo, that's actually a nice
             * effect. Be sure to look at onCollideTrigger for more details.
             */
            else if (whichLevel == 63)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Keep going right!", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 29, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                Controls.addGoodieCount(1, 0, "Goodies", 220, 280, "Default", 60, 70, 255, 12);
                Score.setVictoryDestination(1);

                // this obstacle is a collision trigger... when the hero hits it,
                // the next part of the level appears, via onHeroCollideTrigger().
                // Note, too, that it disappears when the hero hits it, so we can
                // play a sound if we want...
                Obstacle o = Obstacle.MakeAsBox(30, 0, 1, 32, "purpleball");
                o.SetPhysics(1, 0, 1);
                // the trigger id is 0, there is no delay, and no goodies are needed
                // before it works
                o.SetHeroCollisionTrigger(0, 0, 0, 0, 0, 0);
                o.DisappearSound = "hipitch";
            }

            /*
             * this level demonstrates triggers that happen when we touch an
             * obstacle. Be sure to look at the onTouchTrigger() method for more
             * details
             */
            else if (whichLevel == 64)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Activate and then \ntouch the obstacle", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                // make a destination... notice that it needs a lot more goodies
                // than are on the screen...
                Destination d = Destination.MakeAsCircle(29, 1, 2, 2, "mustardball");
                d.SetActivationScore(3, 0, 0, 0);
                Score.setVictoryDestination(1);

                // draw an obstacle, make it a touch trigger, and then draw the
                // goodie we need to get in order to activate the obstacle
                Obstacle o = Obstacle.MakeAsCircle(10, 5, 3, 3, "purpleball");
                o.SetPhysics(1, 0, 1);
                // we'll give this trigger the id "39", just for fun
                o.SetTouchTrigger(39, 1, 0, 0, 0, true);
                o.DisappearSound = "hipitch";

                Goodie g = Goodie.MakeAsCircle(0, 30, 2, 2, "blueball");
                g.DisappearSound = "lowpitch";
            }

            /*
             * this level shows how to use enemy defeat triggers. There are four
             * ways to defeat an enemy, so we enable all mechanisms in this level,
             * to see if they all work to cause enemy triggers to run the
             * onEnemyTrigger code. Another important point here is that the IDs
             * don't need to be unique for *any* triggers. We can use the same ID
             * every time...
             */
            else if (whichLevel == 65)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 0, 0, 0);

                // give the hero strength, so that we can use him to defeat an enemy
                // as a test of enemy triggers
                Hero h = Hero.MakeAsCircle(12, 12, 4, 4, "greenball");
                h.Strength = 3;
                h.SetMoveByTilting();
                h.InvicibleAnimation = new Animation("colorstar", 4, true).to(4, 100).to(5, 100)
                        .to(6, 100).to(7, 100);

                // a goodie, so we can do defeat by invincibility
                Goodie g1 = Goodie.MakeAsCircle(20, 29, 2, 3, "purpleball");
                g1.InvicibilityDuration = 15;

                // enable throwing projectiles, so that we can test enemy triggers
                // again
                h.SetTouchToThrow(h);
                ProjectilePool.configure(100, 1, 1, "greyball", 30, 0, 4, 2, 1, 0, true);

                // add an obstacle that has an enemy collision trigger, so it can
                // defeat enemies
                Obstacle o = Obstacle.MakeAsCircle(30, 10, 5, 5, "blueball");
                o.SetPhysics(1000, 0, 0);
                o.SetCanDrag(false);
                o.SetEnemyCollisionTrigger(0, 0, 0, 0, 0, 0);

                // now draw our enemies... we need enough to be able to test that
                // all four defeat mechanisms work.
                Enemy e1 = Enemy.MakeAsCircle(5, 5, 1, 1, "redball");
                e1.SetDefeatTrigger(0);

                Enemy e2 = Enemy.MakeAsCircle(5, 5, 2, 2, "redball");
                e2.SetDefeatTrigger(0);
                e2.TextInfo = "weak";

                Enemy e3 = Enemy.MakeAsCircle(40, 3, 1, 1, "redball");
                e3.SetDefeatTrigger(0);

                Enemy e4 = Enemy.MakeAsCircle(25, 25, 1, 1, "redball");
                e4.SetDefeatTrigger(0);
                e4.SetDisappearOnTouch();

                Enemy e5 = Enemy.MakeAsCircle(25, 29, 1, 1, "redball");
                e5.SetDefeatTrigger(0);

                // win by defeating enemies
                Score.setVictoryEnemyCount();
            }

            /*
             * This level shows that we can resize a hero on the fly, and change its
             * image. We use a collision trigger to cause the effect. Furthermore,
             * we can increment scores inside of the trigger code, which lets us
             * activate the destination on an obstacle collision
             */
            else if (whichLevel == 66)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Only stars can reach\nthe destination", 255, 255, 255, "Default",
                        20);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 29, 3, 3, "greenball");
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                Controls.addGoodieCount(1, 0, "Goodies", 220, 280, "Default", 60, 70, 255, 12);

                // the destination won't work until some goodies are collected...
                Destination d = Destination.MakeAsBox(46, 2, 2, 2, "colorstar");
                d.SetActivationScore(4, 1, 3, 0);
                Score.setVictoryDestination(1);

                // Colliding with this star will make the hero into a star... see
                // onHeroCollideTrigger for details
                Obstacle o = Obstacle.MakeAsBox(30, 0, 3, 3, "stars");
                o.SetPhysics(1, 0, 1);
                o.SetHeroCollisionTrigger(0, 0, 0, 0, 0, 2);
            }

            /*
             * This level shows how to use countdown timers to win a level, tests
             * some color features, and introduces a vector throw mechanism with
             * fixed velocity
             */
            else if (whichLevel == 67)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press anywhere\nto throw a ball", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                // Here's a simple pause button and pause scene
                PauseScene.addText("Game Paused", 255, 255, 255, "Default", 32);
                Controls.addPauseButton(0, 300, 20, 20, "red");

                // draw a hero, and a button for throwing projectiles in many
                // directions. Note that this is going to look like an "asteroids"
                // game, with a hero covering the bottom of the screen, so that
                // anything that falls to the bottom counts against the player
                Hero h = Hero.MakeAsBox(1, 0, 46, 1, "greenball");
                Controls.addVectorThrowButton(0, 0, 480, 320, "", h, 100);

                // set up our pool of projectiles, then set them to have a fixed
                // velocity when using the vector throw mechanism
                ProjectilePool.configure(100, 1, 1, "greyball", 30, 0, 0, 1, 1, 0, true);
                ProjectilePool.setRange(50);
                ProjectilePool.setFixedVectorThrowVelocity(5);

                // we're going to win by "surviving" for 25 seconds... with no
                // enemies, that shouldn't be too hard
                Controls.addWinCountdown(25, 28, 250, "Default", 192, 192, 192, 16);
                // just to play it safe, let's say that we win on destination...
                // this ensures that collecting goodies or defeating enemies won't
                // accidentally cause us to win. Of course, with no destination,
                // there's no way to win now, except surviving.
                Score.setVictoryDestination(1);
            }

            /*
             * We can make a hero hover, and then have it stop hovering when it is
             * flicked or moved via "touchToMove". This demonstrates the effect via
             * flick. It also shows that an enemy (or obstacle/goodie/destination)
             * can fall due to gravity.
             */
            else if (whichLevel == 68)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Flick the hero into the destination", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsBox(21, 23, 3, 3, "greenball");
                h.SetHover(21, 23);
                h.SetFlickable(0.7f);

                // place an enemy, let it fall
                Enemy e = Enemy.MakeAsCircle(31, 25, 3, 3, "redball");
                e.SetCanFall();

                Destination.MakeAsCircle(25, 25, 5, 5, "mustardball");
                Score.setVictoryDestination(1);
            }

            /*
             * The default behavior is for a hero to be able to jump any time it
             * collides with an obstacle. This isn't, of course, the smartest way to
             * do things, since a hero in the air shouldn't jump. One way to solve
             * the problem is by altering the presolve code in Physics.java. Another
             * approach, which is much simpler, is to mark some walls so that the
             * hero doesn't have jump re-enabled upon a collision.
             */
            else if (whichLevel == 69)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press the hero to\nmake it jump", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);

                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.SetPhysics(.5f, 0, 0.6f);
                h.SetMoveByTilting();
                h.SetTouchToJump();
                h.SetJumpImpulses(0, 15);
                Level.setCameraChase(h);

                // hero can jump while on this obstacle
                Obstacle.MakeAsBox(10, 3, 10, 1, "red");

                // hero can't jump while on this obstacle
                Obstacle o = Obstacle.MakeAsBox(40, 3, 10, 1, "red");
                o.ReJump = false;
            }

            /*
             * When something chases an entity, we might not want it to chase in
             * both the X and Y dimensions... this shows how we can chase in a
             * single direction.
             */
            else if (whichLevel == 70)
            {
                // set up a simple level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("You can walk through the wall", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "stars");
                h.SetMoveByTilting();

                Destination.MakeAsCircle(42, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // These obstacles chase the hero, but only in one dimension
                Obstacle e = Obstacle.MakeAsCircle(0, 0, 1, 1, "red");
                e.SetChaseSpeed(15, h, false, true);
                e.CollisionEffect = true;
                Obstacle e2 = Obstacle.MakeAsCircle(0, 0, 1, 1, "red");
                e2.SetChaseSpeed(15, h, true, false);
                e2.CollisionEffect = true;

                // Here's a wall, and a movable round obstacle
                Obstacle o = Obstacle.MakeAsBox(40, 1, .5f, 20, "red");
                Obstacle o2 = Obstacle.MakeAsCircle(8, 8, 2, 2, "blueball");
                o2.SetMoveByTilting();

                // The hero can pass through this wall, because both have the same
                // passthrough value
                h.PassThrough = 7;
                o.PassThrough = 7;
            }

            /*
             * PokeToPlace is nice, but sometimes it's nicer to use Poke to cause
             * movement to the destination, instead of an immediate jump.
             */
            else if (whichLevel == 71)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 0, 0, 0);
                PreScene.addText("Poke the hero, then\n where you want it\nto go.", 255, 255, 255,
                        "Default", 32);

                // This hero moves via poking. the "false" means that we don't have
                // to poke hero, poke location, poke hero, poke location, ...
                // Instead, we can poke hero, poke location, poke location. the
                // first "true" means that as we drag our finger, the hero will
                // change its direction of travel. The second "true" means the hero
                // will stop immediately when we release our finger.
                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "stars");
                h.SetCanFaceBackwards();
                h.SetPokePath(4, false, true, true);

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // sometimes a control needs to have a large touchable area, but a
                // small image. One way to do it is to make an invisible control,
                // then put a picture on top of it. This next line shows how to draw
                // a picture on the HUD
                Controls.addImage(40, 40, 40, 40, "red");
            }

            /*
             * It can be useful to make a Hero stick to an obstacle. As an example,
             * if the hero should stand on a platform that moves along a route, then
             * we will want the hero to "stick" to it, even as the platform moves
             * downward.
             */
            else if (whichLevel == 72)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press screen borders\nto move the hero", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, 0, 1);
                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetJumpImpulses(0, 15);
                h.SetTouchToJump();
                // give a little friction, to help the hero stick to platforms
                h.SetPhysics(2, 0, .5f);

                // create a destination
                Destination.MakeAsCircle(20, 15, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // This obstacle is sticky on top... Jump onto it and watch what
                // happens
                Obstacle o = Obstacle.MakeAsBox(10, 5, 8, .5f, "red");
                o.SetRoute(new Route(5).to(10, 5).to(5, 15).to(10, 25).to(15, 15).to(10, 5), 5, true);
                o.SetPhysics(100, 0, .1f);
                o.SetSticky(true, false, false, false);

                // This obstacle is not sticky... it's not nearly as much fun
                Obstacle o2 = Obstacle.MakeAsBox(30, 5, 8, .5f, "red");
                o2.SetRoute(new Route(5).to(30, 5).to(25, 15).to(30, 25).to(45, 15).to(30, 5), 5, true);
                o2.SetPhysics(100, 0, 1f);

                // draw some buttons for moving the hero
                Controls.addLeftButton(0, 50, 50, 220, "", 5, h);
                Controls.addRightButton(430, 50, 50, 220, "", 5, h);
            }

            /*
             * When using "vector" projectiles, if the projectile isn't a circle we
             * might want to rotate it in the direction of travel. Also, this level
             * shows how to do walls that can be passed through in one direction.
             */
            else if (whichLevel == 73)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Press anywhere\nto shoot a laserbeam", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0.6f);
                h.SetMoveByTilting();

                Destination.MakeAsCircle(42, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // draw a button for throwing projectiles in many directions. It
                // only covers half the screen, to show how such an effect would
                // behave
                Controls.addVectorThrowButton(0, 0, 240, 320, "", h, 100);

                // set up a pool of projectiles with fixed velocity, and with
                // rotation
                ProjectilePool.configure(100, .1f, 3, "red", 30, 0, 0, 0, 1, 0, false);
                ProjectilePool.setFixedVectorThrowVelocity(10);
                ProjectilePool.setRotateVectorThrow();

                // create a box that is easy to fall into, but hard to get out of,
                // by making its sides each "one-sided"
                Obstacle bottom = Obstacle.MakeAsBox(10, 10, 10, .2f, "red");
                bottom.OneSided = 2;
                Obstacle left = Obstacle.MakeAsBox(10, 10, .2f, 10, "red");
                left.OneSided = 1;
                Obstacle right = Obstacle.MakeAsBox(20, 10, .2f, 10, "red");
                right.OneSided = 3;
                Obstacle top = Obstacle.MakeAsBox(10, 25, 10, .2f, "red");
                top.OneSided = 0;
            }

            /*
             * This level shows how to use multiple types of goodie scores
             */
            else if (whichLevel == 74)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Green, Red, and Grey\nballs are goodies", 255, 255, 255, "Default",
                        32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "stars");
                h.SetMoveByTilting();

                // the destination requires lots of goodies of different types
                Destination d = Destination.MakeAsCircle(42, 31, 2, 2, "mustardball");
                d.SetActivationScore(1, 1, 3, 0);
                Score.setVictoryDestination(1);

                Controls.addGoodieCount(1, 0, "blue", 10, 110, "Default", 0, 255, 255, 16);
                Controls.addGoodieCount(2, 0, "green", 10, 140, "Default", 0, 255, 255, 16);
                Controls.addGoodieCount(3, 0, "red", 10, 170, "Default", 0, 255, 255, 16);

                Controls.addCountdown(100, "", 250, 30);

                // draw the goodies
                for (int i = 0; i < 3; ++i)
                {
                    Goodie b = Goodie.MakeAsCircle(10 * i, 30, 2, 2, "blueball");
                    b.SetScore(1, 0, 0, 0);
                    Goodie g = Goodie.MakeAsCircle(10 * i + 2.5f, 30, 1, 1, "greenball");
                    g.SetScore(0, 1, 0, 0);
                    Goodie r = Goodie.MakeAsCircle(10 * i + 6, 30, 1, 1, "redball");
                    r.SetScore(0, 0, 1, 0);
                }

                // When the hero collides with this obstacle, we'll increase the
                // time remaining. See onHeroCollideTrigger()
                Obstacle o = Obstacle.MakeAsBox(40, 0, 5, 200, "red");
                o.SetHeroCollisionTrigger(0, 1, 1, 1, 0, 0);
            }

            /*
             * this level shows passthrough objects and chase again, to help
             * demonstrate how chase works
             */
            else if (whichLevel == 75)
            {
                // set up a simple level
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("You can walk through the wall", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "stars");
                h.SetMoveByTilting();
                h.PassThrough = 7; // make sure obstacle has same value

                // the destination requires lots of goodies of different types
                Destination.MakeAsCircle(42, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // the enemy chases the hero, but can't get through the wall
                Enemy e = Enemy.MakeAsCircle(42, 1, 5, 4, "red");
                e.SetChaseSpeed(1, h, true, true);

                Obstacle o = Obstacle.MakeAsBox(40, 1, .5f, 20, "red");
                o.PassThrough = 7;
            }

            /*
             * We can have a control that increases the hero's speed while pressed,
             * and decreases it upon release
             */
            else if (whichLevel == 76)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, 10);
                PreScene.addText("Press anywhere to speed up", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 0);

                Destination.MakeAsCircle(120, 31, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Hero h = Hero.MakeAsBox(2, 25, 3, 7, "greenball");
                h.DisableRotation();
                h.SetPhysics(1, 0, 0);
                // give the hero a fixed velocity
                h.AddVelocity(4, 0, false);
                // center the camera a little ahead of the hero
                h.SetCameraOffset(15, 0);
                Level.setCameraChase(h);

                // set up the background
                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // draw a turbo boost button that covers the whole screen... make
                // sure its "up" speeds match the hero velocity
                Controls.addTurboButton(0, 0, 480, 320, "", 15, 0, 4, 0, h);
            }

            /*
             * Sometimes, we want to make the hero move when we press a control, but
             * when we release we don't want an immediate stop. This shows how to
             * get that effect.
             */
            else if (whichLevel == 77)
            {
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                PreScene.addText("Press anywhere to start moving", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 0);

                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                Hero h = Hero.MakeAsBox(2, 1, 3, 7, "greenball");
                h.SetCameraOffset(15, 0);
                Level.setCameraChase(h);

                Background.setColor(23, 180, 255);
                Background.addHorizontalLayer(.5f, 1, "mid", 0);

                // This control has a dampening effect, so that on release, the hero
                // slowly stops
                Controls.addDampenedMotionButton(0, 0, 480, 320, "", 10, 0, 4, h);
            }

            /*
             * One-sided obstacles can be trigger obstacles. This allows, among
             * other things, games like doodle jump. This level shows how it all
             * interacts.
             */
            else if (whichLevel == 78)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("One-sided + Triggers", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(.1f, 0, 0);
                h.SetMoveByTilting();
                h.SetJumpImpulses(0, 15);
                h.SetTouchToJump();

                Destination.MakeAsCircle(42, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // create a platform that we can jump through from above
                Obstacle platform = Obstacle.MakeAsBox(10, 5, 10, .2f, "red");
                platform.OneSided = 2;
                // Set a trigger, then re-enable the platform's collision effect.
                // Be sure to check onHeroCollideTrigger
                platform.SetHeroCollisionTrigger(0, 0, 0, 0, 0, 0);
                platform.CollisionEffect = true;

                // make the z index of the platform -1, so that the hero (index 0)
                // will be drawn on top of the box, not under it
                platform.ZIndex = -1;
            }

            /*
             * This level fleshes out some more poke-to-move stuff. Now we'll say
             * that once a hero starts moving, the player must re-poke the hero
             * before it can be given a new destination. Also, the hero will keep
             * moving after the screen is released.
             */
            else if (whichLevel == 79)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 0, 0, 0);
                PreScene.addText("Poke the hero, then\n where you want it\nto go.", 255, 255, 255,
                        "Default", 32);

                Hero h = Hero.MakeAsCircle(4, 7, 3, 3, "greenball");
                h.SetMoveByTilting();
                h.SetPokePath(4, true, false, false);

                Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // A trigger control is a way to run arbitrary code whenever the
                // control is pressed. This is something of a catch-all for any sort
                // of behavior we might want. See onControlPressTrigger().
                Controls.addTriggerControl(40, 40, 40, 40, "red", 747);
            }

            /*
             * Sometimes we need to manually force an entity to be immune to
             * gravity.
             */
            else if (whichLevel == 80)
            {
                Level.configure(48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Testing Gravity Defy?", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetPhysics(1, 0, 0.6f);
                h.SetMoveByTilting();
                h.SetJumpImpulses(0, 15);
                h.SetTouchToJump();

                Destination d = Destination.MakeAsCircle(42, 14, 2, 2, "mustardball");
                // note: it must not be immune to physics (third parameter true), or
                // it will pass through the bounding box, but we do want it to move
                // and not fall downward
                d.SetAbsoluteVelocity(-2, 0, false);
                d.SetGravityDefy();
                Score.setVictoryDestination(1);
            }

            /*
             * Test to show that we can have obstacles with a polygon shape
             */
            else if (whichLevel == 81)
            {
                Level.configure(48, 32);
                Physics.Configure(0, 0);
                Tilt.enable(10, 10);
                PreScene.addText("Testing Polygons", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 48, 32, "red", 1, .3f, 1);

                Hero h = Hero.MakeAsCircle(2, 2, 3, 3, "greenball");
                h.DisableRotation();
                h.SetMoveByTilting();

                Destination.MakeAsCircle(42, 14, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // create a polygon obstacle
                Obstacle o = Obstacle.MakeAsPolygon(10, 10, 2, 5, "blueball", -1, 2, -1, 0, 0, -3,
                        1, 0, 1, 1);
                o.SetShrinkOverTime(1, 1, true);
            }

            /*
             * A place for playing with a side-scrolling platformer that has lots of
             * features
             */
            else if (whichLevel == 82)
            {
                // set up a standard side scroller with tilt:
                Level.configure(3 * 48, 32);
                Physics.Configure(0, -10);
                Tilt.enable(10, 0);
                PreScene.addText("Press the hero to\nmake it jump", 255, 255, 255, "Default", 32);
                Util.drawBoundingBox(0, 0, 3 * 48, 32, "red", 1, 0, 1);
                Destination.MakeAsCircle(120, 1, 2, 2, "mustardball");
                Score.setVictoryDestination(1);

                // set up a simple jumping hero
                Hero h = Hero.MakeAsBox(5, 0, 2, 6, "greenball");
                h.SetJumpImpulses(0, 15);
                h.SetTouchToJump();
                h.SetMoveByTilting();
                Level.setCameraChase(h);

                // This enemy can be defeated by jumping. Note that the hero's
                // bottom must be higher than the enemy's middle point, or the jump
                // won't defeat the enemy.
                Enemy e = Enemy.MakeAsCircle(15, 0, 5, 5, "redball");
                e.SetDefeatByJump();
            }

        }

        /**
         * Describe how each help scene ought to be drawn. Every game must implement
         * this method to describe how each help scene should appear. Note that you
         * *must* specify the maximum number of help scenes for your game in the
         * Config.java file. If you specify "0", then you can leave this code blank.
         * 
         * @param whichScene The help scene being drawn. The game engine will set
         *            this value to indicate which scene needs to be drawn.
         */
        public override void configureHelpScene(int whichScene) {
            // Note: this is not very good help right now. It's just a demo

            // Our first scene describes the color coding that we use for the
            // different entities in the game
            if (whichScene == 1) {
                HelpLevel.configure(0, 0, 0);
                HelpLevel.drawText(50, 240, "The levels of this game\ndemonstrate LOL features");

                HelpLevel.drawPicture(50, 200, 30, 30, "greenball");
                HelpLevel.drawText(100, 200, "You control the hero");

                HelpLevel.drawPicture(50, 160, 30, 30, "blueball");
                HelpLevel.drawText(100, 160, "Collect these goodies");

                HelpLevel.drawPicture(50, 120, 30, 30, "redball");
                HelpLevel.drawText(100, 120, "Avoid or defeat enemies");

                HelpLevel.drawPicture(50, 80, 30, 30, "mustardball");
                HelpLevel.drawText(100, 80, "Reach the destination");

                HelpLevel.drawPicture(50, 40, 30, 30, "purpleball");
                HelpLevel.drawText(100, 40, "These are walls");

                HelpLevel.drawPicture(50, 0, 30, 30, "greyball");
                HelpLevel.drawText(100, 0, "Throw projectiles");
            }
            // Our second help scene is just here to show that it is possible to
            // have more than one help scene.
            else if (whichScene == 2) {
                HelpLevel.configure(255, 255, 0);
                HelpLevel.drawText(100, 150, "Be sure to read the MyLolGame.java code\n"
                        + "while you play, so you can see\n" + "how everything works", 55, 110, 165,
                        "Default", 14);
            }
        }

        /**
         * If a game uses Obstacles that are triggers, it must provide this to
         * specify what to do when such an obstacle is hit by a hero. The idea
         * behind this mechanism is that it allows the creation of more things in
         * the game, but only after the game has reached a particular state. The
         * most obvious example is 'infinite' levels. There, it is impossible to
         * draw the entire scene, so instead one can place an invisible, full-length
         * TriggerObstacle at some point in the scene, and then when that obstacle
         * is hit, this code will run.
         * 
         * @param id The ID of the obstacle that was hit by the hero
         * @param whichLevel The current level
         * @param obstacle The obstacle that the hero just collided with
         * @param hero The hero who collided with the obstacle
         */
        public override void onHeroCollideTrigger(int id, int whichLevel, Obstacle obstacle, Hero hero) {
            // Code to run on level 63 for hero/obstacle collisions:
            if (whichLevel == 63) {
                // the first trigger just causes us to make a new obstacle a little
                // farther out
                if (id == 0) {
                    // get rid of the obstacle we just collided with
                    obstacle.Remove(false);
                    // make a goodie
                    Goodie.MakeAsCircle(45, 1, 2, 2, "blueball");
                    // make an obstacle that is a trigger, but that doesn't work
                    // until the goodie count is 1
                    Obstacle oo = Obstacle.MakeAsBox(60, 0, 1, 32, "purpleball");
                    oo.SetHeroCollisionTrigger(1, 1, 0, 0, 0, 0);
                }
                // The second trigger works the same way
                else if (id == 1) {
                    obstacle.Remove(false);
                    Goodie.MakeAsCircle(75, 21, 2, 2, "blueball");

                    Obstacle oo = Obstacle.MakeAsBox(90, 0, 1, 32, "purpleball");
                    oo.SetHeroCollisionTrigger(2, 2, 0, 0, 0, 0);
                }
                // same for the third trigger
                else if (id == 2) {
                    obstacle.Remove(false);
                    Goodie.MakeAsCircle(105, 1, 2, 2, "blueball");

                    Obstacle oo = Obstacle.MakeAsBox(120, 0, 1, 32, "purpleball");
                    oo.SetHeroCollisionTrigger(3, 3, 0, 0, 0, 0);
                }
                // The fourth trigger draws the destination
                else if (id == 3) {
                    obstacle.Remove(false);
                    // print a message and pause the game, via PauseScene
                    PauseScene.addText("The destination is\nnow available", 255, 255, 255, "Default",
                            32);
                    Destination.MakeAsCircle(120, 20, 2, 2, "mustardball");
                }
            }
            // in level 66, we use obstacle/hero collisions to change the goodie
            // count, and change the hero appearance
            else if (whichLevel == 66) {
                // here's a simple way to increment a goodie count
                Score.incrementGoodiesCollected2();
                // here's a way to set a goodie count
                Score.setGoodiesCollected3(3);
                // here's a way to read and write a goodie count
                Score.setGoodiesCollected1(4 + Score.getGoodiesCollected1());
                // get rid of the star, so we know it's been used
                obstacle.Remove(true);
                // resize the hero, and change its image
                hero.Resize(hero.XPosition, hero.YPosition, 5, 5);
                hero.SetImage("stars", 0);
            }
            // on level 74, we use a collision as an excuse to add more time before
            // time's up.
            else if (whichLevel == 74) {
                // add 15 seconds to the timer
                Score.updateTimerExpiration(15);
                obstacle.Remove(true);
            }
            // on level 78, we make the hero jump by giving it an upward velocity.
            // Note that the obstacle is one-sided, so this will only run when the
            // hero comes down onto the platform, not when he goes up through it.
            else if (whichLevel == 78) {
                hero.SetAbsoluteVelocity(hero.XVelocity, 5, false);
                return;
            }
        }

        /**
         * If a game uses entities that are touch triggers, it must provide this to
         * specify what to do when such an entity is touched by the user. The idea
         * behind this mechanism is that it allows the creation of more interactive
         * games, since there can be items to unlock, treasure chests to open, and
         * other such behaviors.
         * 
         * @param id The ID of the obstacle that was hit by the hero
         * @param whichLevel The current level
         * @param entity The entity that was touched
         */
        public override void onTouchTrigger(int id, int whichLevel, PhysicsSprite entity) {
            // In level 64, we draw a bunch of goodies when the obstacle is touched.
            // This is supposed to be like having a treasure chest open up.
            if (whichLevel == 64) {
                if (id == 39) {
                    // note: we could draw a picture of an open chest in the
                    // obstacle's place, or even use a disappear animation whose
                    // final frame looks like an open treasure chest.
                    entity.Remove(false);
                    for (int i = 0; i < 3; ++i)
                        Goodie.MakeAsCircle(9 * i, 20 - i, 2, 2, "blueball");
                }
            }
        }

        /**
         * If a game uses timer triggers, it must provide this to specify what to do
         * when a timer expires.
         * 
         * @param id The ID of the timer
         * @param whichLevel The current level
         */
        public override void onTimerTrigger(int id, int whichLevel) {
            // here's the code for level 62
            if (whichLevel == 62) {
                // after first trigger, print a message, draw an enemy, register a
                // new timer
                if (id == 0) {
                    // put up a pause scene to interrupt gameplay
                    PauseScene.addText("Ooh... a draggable enemy", 255, 255, 0, "Default", 12);
                    PauseScene.show();

                    // make a draggable enemy
                    Enemy e3 = Enemy.MakeAsCircle(35, 25, 2, 2, "redball");
                    e3.SetPhysics(1.0f, 0.3f, 0.6f);
                    e3.SetCanDrag(true);

                    // set up a new timer, with id == 1
                    Level.setTimerTrigger(1, 3);
                }
                // after second trigger, draw an enemy who disappears on touch,
                // and register a new timer
                else if (id == 1) {
                    // clear the pause scene, then put new text on it
                    PauseScene.reset();
                    PauseScene.addText("Touch the enemy and it will go away", 255, 0, 255, "Default",
                            12);
                    PauseScene.show();
                    // add an enemy that is touch-to-defeat
                    Enemy e4 = Enemy.MakeAsCircle(35, 5, 2, 2, "redball");
                    e4.SetPhysics(1.0f, 0.3f, 0.6f);
                    e4.SetDisappearOnTouch();
                    // set another timer with id == 2
                    Level.setTimerTrigger(2, 3);
                }
                // after third trigger, draw an enemy, a goodie, and a destination,
                // all with fixed velocities
                else if (id == 2) {
                    PauseScene.addText("Now you can see the rest of the level", 255, 255, 0,
                            "Default", 12);
                    PauseScene.show();
                    Destination d = Destination.MakeAsCircle(29, 6, 2, 2, "mustardball");
                    d.AddVelocity(-.5f, -1, false);

                    Enemy e5 = Enemy.MakeAsCircle(35, 15, 2, 2, "redball");
                    e5.SetPhysics(1.0f, 0.3f, 0.6f);
                    e5.AddVelocity(4, 4, false);

                    Goodie gg = Goodie.MakeAsCircle(10, 10, 2, 2, "blueball");
                    gg.AddVelocity(5, 5, false);
                }
            }
        }

        /**
         * If you want to have enemy timertriggers, then you must override this to
         * define what happens when the timer expires
         * 
         * @param id The id that was assigned to the timer that exired
         * @param whichLevel The current level
         * @param enemy The enemy that was connected to the timer
         */
        public override void onEnemyTimerTrigger(int id, int whichLevel, Enemy enemy) {
            // Code for level 48's EnemyTimerTrigger
            if (whichLevel == 48) {
                // we're simulating cancer cells that can reproduce a fixed number
                // of times. The ID here represents the number of remaining
                // reproductions for the current enemy (e), so that we don't
                // reproduce forever (note that we could, if we wanted to...)

                // make an enemy just like "e", but to the left
                Enemy left = Enemy.MakeAsCircle(enemy.XPosition - 2 * id, enemy.YPosition + 2
                        * id, enemy.Width, enemy.Height, "redball");
                left.DisappearSound = "lowpitch";

                // make an enemy just like "e", but to the right
                Enemy right = Enemy.MakeAsCircle(enemy.XPosition + 2 * id, enemy.YPosition
                        + 2 * id, enemy.Width, enemy.Height, "redball");
                right.DisappearSound = "lowpitch";

                // if there are reproductions left, then have e and its two new
                // children all reproduce in 2 seconds
                if (id > 0) {
                    Level.setEnemyTimerTrigger(id - 1, 2, left);
                    Level.setEnemyTimerTrigger(id - 1, 2, enemy);
                    Level.setEnemyTimerTrigger(id - 1, 2, right);
                }
            }
            // Code for level 49's EnemyTimerTrigger
            else if (whichLevel == 49) {
                // in this case, every enemy will produce one offspring on each
                // timer
                Enemy e2 = Enemy.MakeAsCircle(enemy.XPosition, enemy.YPosition,
                        enemy.Width, enemy.Height, "redball");
                e2.SetPhysics(1.0f, 0.3f, 0.6f);
                e2.SetMoveByTilting();
                // make more enemies?
                if (id > 0) {
                    Level.setEnemyTimerTrigger(id - 1, 2, enemy);
                    Level.setEnemyTimerTrigger(id - 1, 2, e2);
                }
            }
        }

        /**
         * If a game has Enemies that have 'defeatTrigger' set, then when any of
         * those enemies are defeated, this code will run
         * 
         * @param id The ID of the enemy that was defeated by the hero
         * @param whichLevel The current level
         * @param enemy The enemy that was defeated
         */
        public override void onEnemyDefeatTrigger(int id, int whichLevel, Enemy enemy) {
            // in level 65, whenever we defeat an enemy, we pause the game, print a
            // message, and then put a goodie at a random location
            if (whichLevel == 65) {
                // always reset the pausescene, in case it has something on it from
                // before...
                PauseScene.reset();
                PauseScene.addText("good job, here's a prize", 88, 226, 160, "Default", 16);
                PauseScene.show();
                // use random numbers to figure out where to draw a goodie as a
                // reward... picking in the range 0-46,0-30 ensures that with width
                // and height of 2, the goodie stays on screen
                Goodie.MakeAsCircle(Util.getRandom(46), Util.getRandom(30), 2, 2, "blueball");
            }
        }

        /**
         * If you want to have EnemyCollide triggers, then you must override this to
         * define what happens when an enemy hits the obstacle
         * 
         * @param id The ID of the trigger
         * @param whichLevel The current level
         * @param obstacle The obstacle involved in the collision
         * @param enemy The enemy involved in the collision
         */
        public override void onEnemyCollideTrigger(int id, int whichLevel, Obstacle obstacle, Enemy enemy) {
            // this is the code for level 56, to handle collisions between obstacles
            // and enemies
            if (whichLevel == 56) {
                // only the small obstacle can defeat small enemies
                if (enemy.TextInfo == "small" && id == 1) {
                    enemy.Defeat(true);
                }
                // both obstacles can defeat big enemies, but the big obstacle will
                // disappear
                if (enemy.TextInfo == "big") {
                    enemy.Defeat(true);
                    if (id == 14) {
                        obstacle.Remove(true);
                    }
                }
            }
            // this is the code for level 65... if the obstacle collides with the
            // "weak" enemy, we defeat the enemy.
            else if (whichLevel == 65) {
                if (enemy.TextInfo == "weak") {
                    enemy.Defeat(true);
                }
            }
        }

        /**
         * If you want to have Obstacle/Projectile triggers, then you must override
         * this to define what happens when a projectile hits the obstacle
         * 
         * @param id The ID of the trigger
         * @param whichLevel The current level
         * @param obstacle The obstacle involved in the collision
         * @param projectile The projectile involved in the collision
         */
        public override void onProjectileCollideTrigger(int id, int whichLevel, Obstacle obstacle,
                Projectile projectile) {
            if (whichLevel == 47) {
                if (id == 7) {
                    // don't do anything... we want the projectile to stay on the
                    // screen!
                } else {
                    // the ID is not 7... remove the projectile without making a
                    // projectile disappear sound.
                    projectile.Remove(true);
                }
            }
        }

        /**
         * If you want to do something when the level ends (like record a high
         * score), you will need to override this method
         * 
         * @param whichLevel The current level
         * @param win true if the level was won, false otherwise
         */
        public override void levelCompleteTrigger(int whichLevel, bool win) {
            // if we are on level 32, see if this is the farthest the hero has ever
            // traveled, and if so, update the persistent score
            if (whichLevel == 32) {
                int oldBest = Score.readPersistent("HighScore", 0);
                if (oldBest < Score.getDistance()) {
                    Score.savePersistent("HighScore", Score.getDistance());
                }
            }
        }

        /**
         * If you use TriggerControls, you must override this to define what happens
         * when the control is pressed
         * 
         * @param id The id that was assigned to the Control
         * @param whichLevel The current level
         */
        public override void onControlPressTrigger(int id, int whichLevel) {
            // for lack of anything better to do, we'll just pause the game
            if (whichLevel == 79) {
                if (id == 747) {
                    PauseScene.addText("Current score " + Score.getGoodiesCollected1(), 255, 255, 255,
                            "Default", 20);
                    PauseScene.show();
                }
            }
        }

        /**
         * Whenever a hero's strength changes due to a collision with a goodie or
         * enemy, this is called. The most common use is to change the hero's
         * appearance.
         * 
         * @param whichLevel The current level
         * @param h The hero involved in the collision
         */
        public override void onStrengthChangeTrigger(int whichLevel, Hero h) {
            if (whichLevel == 55) {
                // get the hero's strength. Since the hero isn't dead, the strength
                // is at least 1. Since there are 7 strength booster goodies, the
                // strength is at most 8.
                int s = h.Strength;
                // set the hero's image index to (s-1), i.e., one of the indices in
                // the range 0..7, depending on strength
                h.SetImage("colorstar", s - 1);
            }

        }

        /**
         * Mandatory method. Don't change this.
         */
        public override LolConfiguration lolConfig() {
            return new LolConfig();
        }

        /**
         * Mandatory method. Don't change this.
         */
        public override ChooserConfiguration chooserConfig() {
            return new ChooserConfig();
        }
    }
}
