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
            /*GameScreen s1 = new GameScreen();
            s1.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Red);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "ONE", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            GameScreen s2 = new GameScreen();
            s2.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Green);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "TWO", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            GameScreen s3 = new GameScreen();
            s3.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Blue);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "THREE", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            int screen = 0;
            GameScreen.UpdateDelegate update1 = delegate(GameTime gameTime)
            {
                if (TouchPanel.GetState().Count > 0)
                {
                    mgr.Display((screen++) % 3);
                }
            };
            s1.fUpdate = update1;
            s2.fUpdate = update1;
            s3.fUpdate = update1;

            mgr.Add(s1);
            mgr.Add(s2);
            mgr.Add(s3);
            mgr.Display(0);*/
            //mgr.Display();
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
            Splash.drawPlayButton(Util.ax(192), Util.ay(91), Util.ax(93), Util.ay(52));
            Splash.drawHelpButton(Util.ax(48), Util.ay(93), Util.ax(80), Util.ay(40));
            Splash.drawQuitButton(Util.ax(363), Util.ay(93), Util.ax(69), Util.ay(39));

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
                Physics.configure(0, 0);

                // in this level, we'll use tilt to move some things around. The
                // maximum force that tilt can exert on anything is +/- 10 in the X
                // dimension, and +/- 10 in the Y dimension
                Tilt.enable(10, 10);

                // now let's create a hero, and indicate that the hero can move by
                // tilting the phone. "greenball.png" must be registered in
                // the registerMedia() method, which is also in this file. It must
                // also be in your android game's assets folder.
                /*Hero h = Hero.makeAsCircle(4, 17, 3, 3, "greenball.png");
                h.setMoveByTilting();

                // draw a circular destination, and indicate that the level is won
                // when the hero reaches the destination. "mustardball.png" must be
                // registered in registerMedia()
                Destination.makeAsCircle(29, 26, 2, 2, "mustardball.png");
                Score.setVictoryDestination(1);*/
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
            /*if (whichLevel == 63) {
                // the first trigger just causes us to make a new obstacle a little
                // farther out
                if (id == 0) {
                    // get rid of the obstacle we just collided with
                    obstacle.remove(false);
                    // make a goodie
                    Goodie.makeAsCircle(45, 1, 2, 2, "blueball.png");
                    // make an obstacle that is a trigger, but that doesn't work
                    // until the goodie count is 1
                    Obstacle oo = Obstacle.makeAsBox(60, 0, 1, 32, "purpleball.png");
                    oo.setHeroCollisionTrigger(1, 1, 0, 0, 0, 0);
                }
                // The second trigger works the same way
                else if (id == 1) {
                    obstacle.remove(false);
                    Goodie.makeAsCircle(75, 21, 2, 2, "blueball.png");

                    Obstacle oo = Obstacle.makeAsBox(90, 0, 1, 32, "purpleball.png");
                    oo.setHeroCollisionTrigger(2, 2, 0, 0, 0, 0);
                }
                // same for the third trigger
                else if (id == 2) {
                    obstacle.remove(false);
                    Goodie.makeAsCircle(105, 1, 2, 2, "blueball.png");

                    Obstacle oo = Obstacle.makeAsBox(120, 0, 1, 32, "purpleball.png");
                    oo.setHeroCollisionTrigger(3, 3, 0, 0, 0, 0);
                }
                // The fourth trigger draws the destination
                else if (id == 3) {
                    obstacle.remove(false);
                    // print a message and pause the game, via PauseScene
                    PauseScene.addText("The destination is\nnow available", 255, 255, 255, "arial.ttf",
                            32);
                    Destination.makeAsCircle(120, 20, 2, 2, "mustardball.png");
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
                obstacle.remove(true);
                // resize the hero, and change its image
                hero.resize(hero.getXPosition(), hero.getYPosition(), 5, 5);
                hero.setImage("stars.png", 0);
            }
            // on level 74, we use a collision as an excuse to add more time before
            // time's up.
            else if (whichLevel == 74) {
                // add 15 seconds to the timer
                Score.updateTimerExpiration(15);
                obstacle.remove(true);
            }
            // on level 78, we make the hero jump by giving it an upward velocity.
            // Note that the obstacle is one-sided, so this will only run when the
            // hero comes down onto the platform, not when he goes up through it.
            else if (whichLevel == 78) {
                hero.setAbsoluteVelocity(hero.getXVelocity(), 5, false);
                return;
            }*/
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
            /*if (whichLevel == 64) {
                if (id == 39) {
                    // note: we could draw a picture of an open chest in the
                    // obstacle's place, or even use a disappear animation whose
                    // final frame looks like an open treasure chest.
                    entity.remove(false);
                    for (int i = 0; i < 3; ++i)
                        Goodie.makeAsCircle(9 * i, 20 - i, 2, 2, "blueball.png");
                }
            }*/
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
            /*if (whichLevel == 62) {
                // after first trigger, print a message, draw an enemy, register a
                // new timer
                if (id == 0) {
                    // put up a pause scene to interrupt gameplay
                    PauseScene.addText("Ooh... a draggable enemy", 255, 255, 0, "arial.ttf", 12);
                    PauseScene.show();

                    // make a draggable enemy
                    Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
                    e3.setPhysics(1.0f, 0.3f, 0.6f);
                    e3.setCanDrag(true);

                    // set up a new timer, with id == 1
                    Level.setTimerTrigger(1, 3);
                }
                // after second trigger, draw an enemy who disappears on touch,
                // and register a new timer
                else if (id == 1) {
                    // clear the pause scene, then put new text on it
                    PauseScene.reset();
                    PauseScene.addText("Touch the enemy and it will go away", 255, 0, 255, "arial.ttf",
                            12);
                    PauseScene.show();
                    // add an enemy that is touch-to-defeat
                    Enemy e4 = Enemy.makeAsCircle(35, 5, 2, 2, "redball.png");
                    e4.setPhysics(1.0f, 0.3f, 0.6f);
                    e4.setDisappearOnTouch();
                    // set another timer with id == 2
                    Level.setTimerTrigger(2, 3);
                }
                // after third trigger, draw an enemy, a goodie, and a destination,
                // all with fixed velocities
                else if (id == 2) {
                    PauseScene.addText("Now you can see the rest of the level", 255, 255, 0,
                            "arial.ttf", 12);
                    PauseScene.show();
                    Destination d = Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
                    d.addVelocity(-.5f, -1, false);

                    Enemy e5 = Enemy.makeAsCircle(35, 15, 2, 2, "redball.png");
                    e5.setPhysics(1.0f, 0.3f, 0.6f);
                    e5.addVelocity(4, 4, false);

                    Goodie gg = Goodie.makeAsCircle(10, 10, 2, 2, "blueball.png");
                    gg.addVelocity(5, 5, false);
                }
            }*/
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
            /*if (whichLevel == 48) {
                // we're simulating cancer cells that can reproduce a fixed number
                // of times. The ID here represents the number of remaining
                // reproductions for the current enemy (e), so that we don't
                // reproduce forever (note that we could, if we wanted to...)

                // make an enemy just like "e", but to the left
                Enemy left = Enemy.makeAsCircle(enemy.getXPosition() - 2 * id, enemy.getYPosition() + 2
                        * id, enemy.getWidth(), enemy.getHeight(), "redball.png");
                left.setDisappearSound("lowpitch.ogg");

                // make an enemy just like "e", but to the right
                Enemy right = Enemy.makeAsCircle(enemy.getXPosition() + 2 * id, enemy.getYPosition()
                        + 2 * id, enemy.getWidth(), enemy.getHeight(), "redball.png");
                right.setDisappearSound("lowpitch.ogg");

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
                Enemy e2 = Enemy.makeAsCircle(enemy.getXPosition(), enemy.getYPosition(),
                        enemy.getWidth(), enemy.getHeight(), "redball.png");
                e2.setPhysics(1.0f, 0.3f, 0.6f);
                e2.setMoveByTilting();
                // make more enemies?
                if (id > 0) {
                    Level.setEnemyTimerTrigger(id - 1, 2, enemy);
                    Level.setEnemyTimerTrigger(id - 1, 2, e2);
                }
            }*/
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
            /*if (whichLevel == 65) {
                // always reset the pausescene, in case it has something on it from
                // before...
                PauseScene.reset();
                PauseScene.addText("good job, here's a prize", 88, 226, 160, "arial.ttf", 16);
                PauseScene.show();
                // use random numbers to figure out where to draw a goodie as a
                // reward... picking in the range 0-46,0-30 ensures that with width
                // and height of 2, the goodie stays on screen
                Goodie.makeAsCircle(Util.getRandom(46), Util.getRandom(30), 2, 2, "blueball.png");
            }*/
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
            /*if (whichLevel == 56) {
                // only the small obstacle can defeat small enemies
                if (enemy.getInfoText() == "small" && id == 1) {
                    enemy.defeat(true);
                }
                // both obstacles can defeat big enemies, but the big obstacle will
                // disappear
                if (enemy.getInfoText() == "big") {
                    enemy.defeat(true);
                    if (id == 14) {
                        obstacle.remove(true);
                    }
                }
            }
            // this is the code for level 65... if the obstacle collides with the
            // "weak" enemy, we defeat the enemy.
            else if (whichLevel == 65) {
                if (enemy.getInfoText() == "weak") {
                    enemy.defeat(true);
                }
            }*/
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
            /*if (whichLevel == 47) {
                if (id == 7) {
                    // don't do anything... we want the projectile to stay on the
                    // screen!
                } else {
                    // the ID is not 7... remove the projectile without making a
                    // projectile disappear sound.
                    projectile.remove(true);
                }
            }
             */
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
            /*if (whichLevel == 32) {
                int oldBest = Score.readPersistent("HighScore", 0);
                if (oldBest < Score.getDistance()) {
                    Score.savePersistent("HighScore", Score.getDistance());
                }
            }*/
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
            /*if (whichLevel == 79) {
                if (id == 747) {
                    PauseScene.addText("Current score " + Score.getGoodiesCollected1(), 255, 255, 255,
                            "arial.ttf", 20);
                    PauseScene.show();
                }
            }*/
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
            /*if (whichLevel == 55) {
                // get the hero's strength. Since the hero isn't dead, the strength
                // is at least 1. Since there are 7 strength booster goodies, the
                // strength is at most 8.
                int s = h.getStrength();
                // set the hero's image index to (s-1), i.e., one of the indices in
                // the range 0..7, depending on strength
                h.setImage("colorstar.png", s - 1);
            }*/

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
