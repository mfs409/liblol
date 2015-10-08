/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package com.me.mylolgame;

import com.badlogic.gdx.math.Vector2;

import edu.lehigh.cse.lol.Actor;
import edu.lehigh.cse.lol.Animation;
import edu.lehigh.cse.lol.Background;
import edu.lehigh.cse.lol.Control;
import edu.lehigh.cse.lol.Destination;
import edu.lehigh.cse.lol.Display;
import edu.lehigh.cse.lol.Effect;
import edu.lehigh.cse.lol.Enemy;
import edu.lehigh.cse.lol.Facts;
import edu.lehigh.cse.lol.Foreground;
import edu.lehigh.cse.lol.Goodie;
import edu.lehigh.cse.lol.Hero;
import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.LolCallback;
import edu.lehigh.cse.lol.LoseScene;
import edu.lehigh.cse.lol.Obstacle;
import edu.lehigh.cse.lol.PauseScene;
import edu.lehigh.cse.lol.Physics;
import edu.lehigh.cse.lol.PreScene;
import edu.lehigh.cse.lol.ProjectilePool;
import edu.lehigh.cse.lol.Route;
import edu.lehigh.cse.lol.Score;
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.Svg;
import edu.lehigh.cse.lol.Tilt;
import edu.lehigh.cse.lol.Util;
import edu.lehigh.cse.lol.WinScene;

/**
 * Levels is where all of the code goes for describing the different levels of
 * the game. If you know how to create methods and classes, you're free to make
 * the big "if" statement in this code simply call to your classes and methods.
 * Otherwise, put your code directly into the parts of the "if" statement.
 */
public class Levels implements ScreenManager {

    /**
     * We currently have 92 levels, each of which is described in part of the
     * following function.
     */
    public void display(int whichLevel) {
        /*
         * In this level, all we have is a hero (the green ball) who needs to
         * make it to the destination (a mustard colored ball). The game is
         * configured to use tilt to control the hero.
         */
        if (whichLevel == 1) {
            // set the screen to 48 meters wide by 32 meters high... this is
            // important, because Config.java says the screen is 480x320, and
            // LOL likes a 20:1 pixel to meter ratio. If we went smaller than
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
            Hero h = Hero.makeAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();

            // draw a circular destination, and indicate that the level is won
            // when the hero reaches the destination. "mustardball.png" must be
            // registered in registerMedia()
            Destination.makeAsCircle(29, 26, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /*
         * In this level, we make the play a bit smoother by adding a bounding
         * box and changing the way that LibLOL interacts with the player
         */
        else if (whichLevel == 2) {
            // start by setting everything up just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 26, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // add a bounding box so the hero can't fall off the screen
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // change the text that we display when the level is won
            WinScene.get().setDefaultWinText("Good job!");

            // add a pop-up message that shows for one second at the
            // beginning of the level. The '50, 50' indicates the bottom left
            // corner of the text we display. 255,255,255 represents the red,
            // green, and blue components of the text color (the color will be
            // white). We'll write our text in the Arial font, with a size of 32
            // pt. The "\n" in the middle of the text causes a line break. Note
            // that "arial.ttf" must be in your android game's assets folder.
            PreScene.get().addText("Reach the destination\nto win this level.", 50, 50, 255, 255, 255, "arial.ttf", 32);
        }

        /*
         * In this level, we change the physics from level 2 so that things roll
         * and bounce a little bit more nicely.
         */
        else if (whichLevel == 3) {
            // These lines should be familiar after the last two levels
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // give the hero some density and friction, so that it can roll when
            // it encounters a wall... notice that once it has density, it has
            // mass, and it moves a lot slower...
            h.setPhysics(1, 0, 0.6f);

            // the bounding box now also has nonzero density, elasticity, and
            // friction... you should check out what happens if the friction
            // stays at 0.
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Let's draw our message in the center of the screen this time
            PreScene.get().addText("Reach the destination\nto win this level.", 255, 255, 255, "arial.ttf", 32);
            // And let's say that instead of touching the message to make it go
            // away, we'll have it go away automatically after 2 seconds
            PreScene.get().setExpire(2);
            // Note that we're going back to the default PostScene text...
        }

        /*
         * It's confusing to have multiple heroes in a level, but we can... this
         * shows how to have multiple destinations and heroes
         */
        else if (whichLevel == 4) {
            // standard stuff...
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // now let's draw two heroes who can both move by tilting, and
            // who both have density and friction. Note that we lower the
            // density, so they move faster
            Hero h1 = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            Hero h2 = Hero.makeAsCircle(14, 7, 3, 3, "greenball.png");
            h2.setPhysics(.1f, 0, 0.6f);
            h2.setMoveByTilting();

            // notice that now we will make two destinations, each of which
            // defaults to only holding ONE hero, but we still need to get two
            // heroes to destinations in order to complete the level
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Destination.makeAsCircle(29, 26, 2, 2, "mustardball.png");
            Score.setVictoryDestination(2);

            // Let's show msg1.png instead of text. Note that we had to
            // register it in registerMedia(), and that we're stretching it
            // slightly, since its dimensions are 460x320
            PreScene.get().addImage("msg1.png", 0, 0, 960, 640);
        }

        /*
         * This level demonstrates that we can have many heroes that can reach
         * the same destination. It also shows our first sound effect
         */
        else if (whichLevel == 5) {
            // standard stuff...
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h1 = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            Hero h2 = Hero.makeAsCircle(14, 7, 3, 3, "greenball.png");
            h2.setPhysics(.1f, 0, 0.6f);
            h2.setMoveByTilting();
            PreScene.get().addText("All heroes must\nreach the destination", 255, 255, 255, "arial.ttf", 32);

            // now let's make a destination, but indicate that it can hold TWO
            // heroes
            Destination d = Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setHeroCount(2);

            // let's also say that whenever a hero reaches the destination, a
            // sound will play
            d.setArrivalSound("hipitch.ogg");

            // Notice that this line didn't change from level 4
            Score.setVictoryDestination(2);
        }

        /*
         * Tilt can be used to control velocity, instead of applying forces to
         * the entities on the screen. It doesn't always work well, but it's a
         * nice option to have...
         */
        else if (whichLevel == 6) {
            // standard stuff...
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            PreScene.get().addText("A different way\nto use tilt.", 255, 255, 255, "arial.ttf", 32);

            // change the behavior or tilt
            Tilt.setAsVelocity(true);
        }

        /*
         * This level adds an enemy, to demonstrate that we can make it possible
         * to lose a level
         */
        else if (whichLevel == 7) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            // Notice that we changed the font size and color
            PreScene.get().addText("Avoid the enemy and\nreach the destination", 25, 255, 255, "arial.ttf", 20);

            // draw an enemy... we don't need to give it physics for now...
            Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");

            // turn off the win and lose scenes... whether the player wins or
            // loses, we'll just start the appropriate level. Be sure to test
            // the game by losing *and* winning!
            WinScene.get().disable();
            LoseScene.get().disable();
        }

        /*
         * This level explores a bit more of what we can do with enemies, by
         * having an enemy with a fixed path.
         */
        else if (whichLevel == 8) {
            // configure a basic level, just like the start of level 2:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            PreScene.get().addText("Avoid the enemy and\nreach the destination", 255, 255, 255, "arial.ttf", 20);

            // put some extra text on the PreScene.get()
            PreScene.get().addText("(the enemy is red)", 5, 5, 50, 200, 122, "arial.ttf", 10);

            // draw an enemy
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");

            // attach a path to the enemy. It starts at (25, 25) and moves to
            // (25, 2). This means it has *2* points on its route. Notice that
            // since it loops, it is not going to gracefully move back to its
            // starting point. Also note that the first point is the same as the
            // enemy's original position. If it wasn't, then there would be an
            // odd glitch at the beginning of the level.
            e.setRoute(new Route(2).to(25, 25).to(25, 2), 10, true);

            // Note that when the level is lost, the default lose text will be
            // displayed on a PostScene
        }

        /*
         * This level explores a bit more of what we can do with paths.
         */
        else if (whichLevel == 9) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            PreScene.get()
                    .addText("Avoid the enemy and\nreach the destination", 50, 50, 255, 255, 255, "arial.ttf", 20);

            // draw an enemy that can move
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            // This time, we add a third point, which is the same as the
            // starting point. This will give us a nicer sort of movement. Also
            // note the diagonal movement.
            e.setRoute(new Route(3).to(25, 25).to(12, 2).to(25, 25), 2, true);
            // note that any number of points is possible... you could have
            // extremely complex Routes!
        }

        /*
         * We can make enemies move via tilt. We can also configure some other
         * kinds of sounds
         */
        else if (whichLevel == 10) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(0.1f, 0, 0.6f);
            h.setMoveByTilting();
            PreScene.get().addImage("msg2.png", 0, 0, 960, 640);

            // let's make the destination rotate:
            Destination d = Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setRotationSpeed(1);
            Score.setVictoryDestination(1);

            // draw an enemy who moves via tilt
            Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(1.0f, 0.3f, 0.6f);
            e3.setMoveByTilting();

            // configure some sounds to play on win and lose. Of course, all
            // these sounds must be registered!
            WinScene.get().setSound("winsound.ogg");
            LoseScene.get().setSound("losesound.ogg");

            // set background music
            Level.setMusic("tune.ogg");

            // custom text for when the level is lost
            LoseScene.get().setDefaultText("Better luck next time...");
        }

        /*
         * This shows that it is possible to make a level that is larger than a
         * screen. It also shows that there is a "heads up display" that can be
         * used for providing information and touchable controls
         */
        else if (whichLevel == 11) {
            // make the level really big
            Level.configure(400, 300);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 400, 300, "red.png", 0, 0, 0);

            // put the hero and destination far apart
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(329, 281, 10, 10, "mustardball.png");
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
            Control.addZoomOutButton(0, 0, 480, 640, "", 8);
            Control.addZoomInButton(480, 0, 480, 640, "", .25f);

            PreScene.get().addText("Press left to zoom out\nright to zoom in", 255, 255, 255, "arial.ttf", 32);
        }

        /*
         * this level introduces obstacles, and also shows the difference
         * between "box" and "circle" physics
         */
        else if (whichLevel == 12) {
            // configure a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // add a hero and destination
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // let's draw an obstacle whose underlying shape is a box, but whose
            // picture is a circle. This can be odd... our hero can roll around
            // an invisible corner on this obstacle. When debug rendering is
            // turned on (in Config.java), you'll be able to see the true shape
            // of the obstacle.
            Obstacle o1 = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(1, 0, 1);

            // now let's draw an obstacle whose shape and picture are both
            // circles. The hero rolls around this nicely.
            Obstacle o2 = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(1, 0, 1);

            // draw a wall using circle physics and a stretched rectangular
            // picture. This wall will do really funny things
            Obstacle o3 = Obstacle.makeAsCircle(20, 25, 6, 0.5f, "red.png");
            o3.setPhysics(1, 0, 1);

            // draw a rectangular wall the right way, as a box
            Obstacle o4 = Obstacle.makeAsBox(34, 2, 0.5f, 20, "red.png");
            o4.setPhysics(1, 0, 1);

            PreScene.get().addText("An obstacle's appearance may\nnot match its physics", 255, 255, 255, "arial.ttf",
                    32);
        }

        /*
         * this level just plays around with physics a little bit, to show how
         * friction and elasticity can do interesting things.
         */
        else if (whichLevel == 13) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("These obstacles have\ndifferent physics\nparameters", 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            // Colliding the hero with these obstacles can have interesting
            // effects
            Obstacle o1 = Obstacle.makeAsCircle(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(0, 100, 0);
            Obstacle o2 = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(10, 0, 100);
        }

        /*
         * This level introduces goodies. Goodies are something that we collect.
         * We can make the collection of goodies lead to changes in the behavior
         * of the game, and in this example, the collection of goodies "enables"
         * a destination.
         */
        else if (whichLevel == 14) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            PreScene.get().addText("You must collect\ntwo blue balls", 255, 255, 255, "arial.ttf", 32);

            // Add some stationary goodies. Note that the default is
            // for goodies to not cause a change in the hero's behavior at the
            // time when a collision occurs... this is often called being a
            // "sensor"... it means that collisions are still detected by the
            // code, but they don't cause changes in momentum
            //
            // Note that LibLOL allows goodies to have one of 4 "types". By
            // default, collecting a goodie increases the "type 1" score by 1.
            Goodie.makeAsCircle(0, 30, 2, 2, "blueball.png");
            Goodie.makeAsCircle(0, 15, 2, 2, "blueball.png");

            // here we create a destination. Note that we now set its activation
            // score to 2, so that you must collect two goodies before the
            // destination will "work"
            Destination d = Destination.makeAsCircle(29, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            // we must provide an activation score for each of the 4 types of
            // goodies
            d.setActivationScore(2, 0, 0, 0);

            // let's put a display on the screen to see how many type-1 goodies
            // we've collected. Since the second parameter is "2", we'll display
            // the count as "X/2 Goodies" instead of "X Goodies"
            Display.addGoodieCount(1, 2, " Goodies", 220, 280, "arial.ttf", 255, 0, 255, 20);
        }

        /*
         * earlier, we saw that enemies could move along a Route. So can any
         * other entity, so we'll move destinations, goodies, and obstacles,
         * too.
         */
        else if (whichLevel == 15) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Every entity can move...", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(44, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // make a destination that moves, and that requires one goodie to be
            // collected before it works
            Destination d = Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setActivationScore(1, 0, 0, 0);
            d.setRoute(new Route(3).to(29, 6).to(29, 26).to(29, 6), 4, true);
            Score.setVictoryDestination(1);

            // make an obstacle that moves
            Obstacle o = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setRoute(new Route(3).to(0, 0).to(10, 10).to(0, 0), 2, true);

            // make a goodie that moves
            Goodie g = Goodie.makeAsCircle(5, 5, 2, 2, "blueball.png");
            g.setRoute(new Route(5).to(5, 5).to(5, 25).to(25, 25).to(9, 9).to(5, 5), 10, true);

            // draw a goodie counter in light blue (60, 70, 255) with a 12-point
            // font
            Display.addGoodieCount(1, 0, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);
        }

        /*
         * Sometimes, we don't want a destination, we just want to say that the
         * player wins by collecting enough goodies. This level also shows that
         * we can set a time limit for the level, and we can pause the game.
         */
        else if (whichLevel == 16) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Collect all\nblue balls\nto win", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 20, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw 5 goodies
            Goodie.makeAsCircle(.5f, .5f, 2, 2, "blueball.png");
            Goodie.makeAsCircle(5.5f, 1.5f, 2, 2, "blueball.png");
            Goodie.makeAsCircle(10.5f, 2.5f, 2, 2, "blueball.png");
            Goodie.makeAsCircle(15.5f, 3.5f, 2, 2, "blueball.png");
            Goodie.makeAsCircle(20.5f, 4.5f, 2, 2, "blueball.png");

            // indicate that we win by collecting enough goodies
            Score.setVictoryGoodies(5, 0, 0, 0);

            // put the goodie count on the screen
            Display.addGoodieCount(1, 5, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);

            // put a simple countdown on the screen
            Display.addCountdown(15, "Time Up!", 400, 50);

            // let's also add a screen for pausing the game. In a real game,
            // every level should have a button for pausing the game, and the
            // pause scene should have a button for going back to the main
            // menu... we'll show how to do that later.
            PauseScene.get().addText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            Control.addPauseButton(0, 300, 20, 20, "red.png");
        }

        /*
         * This level shows how "obstacles" need not actually impede the hero's
         * movement. Here, we attach "damping factors" to the hero, which let us
         * make the hero speed up or slow down based on interaction with the
         * obstacle. This level also adds a stopwatch. Stopwatches don't have
         * any meaning, but they are nice to have anyway...
         */
        else if (whichLevel == 17) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Obstacles as zoom\nstrips, friction pads\nand repellers", 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // add a stopwatch... note that there are two ways to add a
            // stopwatch, the other of which allows for configuring the font
            Display.addStopwatch(50, 50);

            // Create a pause scene that has a back button on it, and a button
            // for pausing the level. Note that the background image must come
            // first
            PauseScene.get().addImage("fade.png", 0, 0, 960, 640);
            PauseScene.get().addText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            PauseScene.get().addBackButton("greyball.png", 0, 300, 20, 20);
            Control.addPauseButton(0, 300, 20, 20, "red.png");

            // now draw three obstacles. Note that they have different dampening
            // factors. one important thing to notice is that since we place
            // these on the screen *after* we place the hero on the screen, the
            // hero will go *under* these things.

            // this obstacle's dampening factor means that on collision, the
            // hero's velocity is multiplied by -1... he bounces off at an
            // angle.
            Obstacle o = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPad(-1);

            // this obstacle accelerates the hero... it's like a turbo booster
            o = Obstacle.makeAsCircle(20, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPad(5);

            // this obstacle slows the hero down... it's like running on
            // sandpaper. Note that the hero only slows down on initial
            // collision, not while going under it.
            o = Obstacle.makeAsBox(30, 10, 3.5f, 3.5f, "purpleball.png");
            o.setRotationSpeed(2);
            o.setPad(0.2f);
        }

        /*
         * This level shows that it is possible to give heroes and enemies
         * different strengths, so that a hero doesn't disappear after a single
         * collision. It also shows that when an enemy defeats a hero, we can
         * customize the message that prints
         */
        else if (whichLevel == 18) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The hero can defeat \nup to two enemies...", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a hero and give it strength of 10. The default is for
            // enemies to have "2" units of damage, and heroes to have "1" unit
            // of strength, so that any collision defeats the hero without
            // removing the enemy.
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setStrength(10);

            // draw a strength meter to show this hero's strength
            Display.addStrengthMeter(" Strength", 220, 280, h);

            // our first enemy stands still:
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            // this text will be displayed if this enemy defeats the hero
            e.setDefeatHeroText("How did you hit me?");

            // our second enemy moves along a path
            e = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 10, true);
            e.setDamage(4);
            e.setDefeatHeroText("Stay out of my way");

            // our third enemy moves with tilt, which makes it hardest to avoid
            e = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(.1f, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(4);
            e.setDefeatHeroText("You can't hide!");

            // be sure when testing this level to lose, with each enemy being
            // the last the hero collides with, so that you can see the
            // different messages
        }

        /*
         * This level shows that we can win a level by defeating all enemies
         */
        else if (whichLevel == 19) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("You have 10 seconds\nto defeat the enemies", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // give the hero enough strength that this will work...
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setStrength(10);
            h.setMoveByTilting();

            // draw a few enemies, and change their "damage" (the amount by
            // which they decrease the hero's strength)
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            e = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(.1f, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(4);

            // put a countdown on the screen
            Display.addCountdown(10, "Time Up!", 200, 25);

            // indicate that defeating all of the enemies is the way to win this
            // level
            Score.setVictoryEnemyCount();
        }

        /*
         * This level shows that a goodie can change the hero's strength, and
         * that we can win by defeating a specific number of enemies, instead of
         * all enemies.
         */
        else if (whichLevel == 20) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Collect blue balls\nto increse strength", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // our default hero only has "1" strength
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // our default enemy has "2" damage
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDisappearSound("slowdown.ogg");

            // a second enemy
            e = Enemy.makeAsCircle(35, 15, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);

            // this goodie gives an extra "5" strength:
            Goodie g = Goodie.makeAsCircle(0, 30, 2, 2, "blueball.png");
            g.setStrengthBoost(5);
            g.setDisappearSound("woowoowoo.ogg");

            // Display the hero's strength
            Display.addStrengthMeter(" Strength", 220, 280, h);

            // win by defeating one enemy
            Score.setVictoryEnemyCount(1);
            WinScene.get().setDefaultWinText("Good enough...");
        }

        /*
         * this level introduces the idea of invincibility. Collecting the
         * goodie makes the hero invincible for a little while...
         */
        else if (whichLevel == 21) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The blue ball will\nmake you invincible\nfor 15 seconds", 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw a few enemies, and make them rotate
            for (int i = 0; i < 5; ++i) {
                Enemy e = Enemy.makeAsCircle(5 * i + 1, 25, 2, 2, "redball.png");
                e.setPhysics(1.0f, 0.3f, 0.6f);
                e.setRotationSpeed(1);
            }

            // this goodie makes us invincible
            Goodie g = Goodie.makeAsCircle(30, 30, 2, 2, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);

            // we'll still say you win by reaching the destination. Defeating
            // enemies is just for fun...
            Destination.makeAsCircle(29, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // display a goodie count for type-1 goodies
            Display.addGoodieCount(1, 0, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);

            // put a frames-per-second display on the screen. This is going to
            // look funny, because when debug mode is set (in Config.java), a
            // FPS will be shown on every screen anyway
            Display.addFPS(400, 15, "arial.ttf", 200, 200, 100, 12);
        }

        /*
         * Some goodies can "count" for more than one point... they can even
         * count for negative points.
         */
        else if (whichLevel == 22) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Collect 'the right' \nblue balls to\nactivate destination", 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination d = Destination.makeAsCircle(29, 1, 2, 2, "mustardball.png");
            d.setActivationScore(7, 0, 0, 0);
            Score.setVictoryDestination(1);

            // create some goodies with special scores. Note that we're still
            // only dealing with type-1 scores
            Goodie g1 = Goodie.makeAsCircle(0, 30, 2, 2, "blueball.png");
            g1.setScore(-2, 0, 0, 0);
            Goodie g2 = Goodie.makeAsCircle(0, 15, 2, 2, "blueball.png");
            g2.setScore(7, 0, 0, 0);

            // create some regular goodies
            Goodie.makeAsCircle(30, 30, 2, 2, "blueball.png");
            Goodie.makeAsCircle(35, 30, 2, 2, "blueball.png");

            // print a goodie count to show how the count goes up and down
            Display.addGoodieCount(1, 0, " Progress", 220, 280, "arial.ttf", 60, 70, 255, 12);
        }

        /*
         * this level demonstrates that we can drag entities (in this case,
         * obstacles), and that we can make rotated obstacles. The latter could
         * be useful for having angled walls in a maze
         */
        else if (whichLevel == 23) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get()
                    .addText("Rotating oblong obstacles\nand draggable obstacles", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw obstacles that we can drag
            Obstacle o = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setCanDrag(true);
            Obstacle o2 = Obstacle.makeAsBox(7, 0, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(0, 100, 0);
            o2.setCanDrag(true);

            // draw an obstacle that is oblong (due to its width and height) and
            // that is rotated. Note that this should be a box, or it will not
            // have the right underlying shape.
            o = Obstacle.makeAsBox(12, 12, 3.5f, .5f, "purpleball.png");
            o.setRotation(45);
        }

        /*
         * this level shows how we can use "poking" to move obstacles. In this
         * case, pressing an obstacle selects it, and pressing the screen moves
         * the obstacle to that location. Double-tapping an obstacle removes it.
         */
        else if (whichLevel == 24) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            PreScene.get().addText("Touch the obstacle\nto select, then" + "\ntouch to move it", 255, 255, 255,
                    "arial.ttf", 32);

            // draw a picture on the default plane (0)... there are actually 5
            // planes (-2 through 2). Everything drawn on the same plane will be
            // drawn in order, so if we don't put this before the hero, the hero
            // will appear to go "under" the picture.
            Util.drawPicture(0, 0, 48, 32, "greenball.png", 0);

            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a pokeable obstacle
            Obstacle o = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            // '250' is a number of milliseconds. Two presses within 250
            // milliseconds will cause this obstacle to disappear, forever. Make
            // the number 0 if you want it to never disappear due to
            // double-touch.
            o.setPokeToPlace(250);
        }

        /*
         * In this level, the enemy chases the hero
         */
        else if (whichLevel == 25) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The enemy will chase you", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // create an enemy who chases the hero
            Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(.1f, 0.3f, 0.6f);
            e3.setChaseSpeed(8, h, true, true);

            // draw a picture late within this block of code, but still cause
            // the picture to be drawn behind everything else by giving it a z
            // index of -1
            Util.drawPicture(0, 0, 48, 32, "greenball.png", -2);

            // We can change the z-index of anything... let's move the enemy to
            // -2. Since we do this after drawing the picture, it will still be
            // drawn on top of the picture, but we should also be able to see it
            // go under the destination.
            e3.setZIndex(-2);
        }

        /*
         * We can make obstacles play sounds either when we collide with them,
         * or touch them
         */
        else if (whichLevel == 26) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Touch the purple ball \nor collide with it", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up our obstacle so that collision and touch make it play
            // sounds
            Obstacle o = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setTouchSound("lowpitch.ogg");
            o.setCollideSound("hipitch.ogg", 2000);
        }

        /*
         * this hero rotates so that it faces in the direction of movement. This
         * can be useful in games where the perspective is from overhead, and
         * the hero is moving in any X or Y direction
         */
        else if (whichLevel == 27) {
            // set up a big screen
            Level.configure(4 * 48, 2 * 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The star rotates in\nthe direction of movement", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 60, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            Level.setCameraChase(h);
        }

        /*
         * This level shows two things. The first is that a custom motion path
         * can allow things to violate the laws of physics and pass through
         * other things. The second is that motion paths can go off-screen.
         */
        else if (whichLevel == 28) {
            // set up a regular level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Reach the destination\nto win the game.", 255, 255, 255, "arial.ttf", 20);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(21.5f, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(21.5f, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // this enemy starts from off-screen
            Enemy e = Enemy.makeAsCircle(1, -20, 44, 44, "redball.png");
            e.setDefeatHeroText("Ha Ha Ha");
            e.setRoute(new Route(3).to(1, -90).to(1, 26).to(1, -20), 30, true);
        }

        /*
         * This level shows that we can draw on the screen to create obstacles.
         *
         * This is also our first exposure to "callbacks".  A "callback" is a way of providing code
         * that runs in response to some event.  We use a callback to customize the obstacles that
         * are drawn to the screen in response to scribbles.
         */
        else if (whichLevel == 29) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Draw on the screen\nto make obstacles appear", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(21.5f, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(21.5f, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // turn on 'scribble mode'... this says "draw a purple ball that is 1.5x1.5 at the
            // location where the scribble happened, but only do it if we haven't drawn anything in
            // 10 milliseconds."  It also says "when an obstacle is drawn, do some stuff to the
            // obstacle".  If you don't want any of this functionality, you can replace the whole
            // "new LolCallback..." region of code with "null".
            Level.setScribbleMode("purpleball.png", 1.5f, 1.5f, 10, new LolCallback(){
                @Override
                public void onEvent() {
                    // each time we draw an obstacle, it will be visible to this code as the
                    // callback's "attached Actor".  We'll change its elasticity, make it disappear
                    // after 10 seconds, and make it so that the obstacles aren't stationary
                    mAttachedActor.setPhysics(0, 2, 0);
                    mAttachedActor.setDisappearDelay(10, true);
                    mAttachedActor.setCanFall();
                }
            });
        }

        /*
         * This level shows that we can "flick" things to move them. Notice that
         * we do not enable tilt! Instead, we specified that there is a default
         * gravity in the Y dimension pushing everything down. This is much like
         * gravity on earth. The only way to move things, then, is via flicking
         * them.
         */
        else if (whichLevel == 30) {
            // create a level with a constant force downward in the Y dimension
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a hero who we can flick
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setFlickable(1f);
            h.disableRotation();

            Obstacle o = Obstacle.makeAsCircle(8, 27, 3, 3, "purpleball.png");
            o.setFlickable(.5f);
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
        else if (whichLevel == 31) {
            // make a long level but not a tall level, and provide a constant
            // downward force:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            // turn on tilt, but only in the X dimension
            Tilt.enable(10, 0);
            PreScene.get().addText("Side scroller / tilt demo", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);
        }

        /*
         * In the previous level, it was hard to see that the hero was moving.
         * We can make a background layer to remedy this situation. Notice that
         * the background uses transparency to show the blue color for part of
         * the screen
         */
        else if (whichLevel == 32) {
            // start by repeating the previous level:
            Level.configure(30 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Side scroller / tilt demo", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 30 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(30 * 48 - 5, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);

            // now paint the background blue
            Background.setColor(23, 180, 255);

            // put in a picture that scrolls at half the speed of the hero in
            // the x direction. Note that background "layers" are all drawn
            // *before* anything that is drawn with a z index... so the
            // background will be behind the hero
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // make an obstacle that hovers in a fixed place. Note that hovering
            // and zoom do not work together nicely.
            Obstacle o = Obstacle.makeAsCircle(10, 10, 5, 5, "blueball.png");
            o.setHover(100, 100);

            // Add a meter to show how far the hero has traveled
            Display.addDistanceMeter(" m", 5, 300, "arial.ttf", 255, 0, 255, 16, h);

            // Add some text about the previous best score.
            Util.drawText(30, 30, "best: " + Facts.getGameFact("HighScore32", 0) + "M", 0, 0, 0, "arial.ttf", 12, 0);

            // when this level ends, we save the best score. Once the
            // score is saved, it is saved permanently on the phone, though
            // every re-execution on the desktop resets the best score. Note
            // that we save the score whether we win or lose.
            LolCallback sc = new LolCallback() {
                public void onEvent() {
                    int oldBest = Facts.getGameFact("HighScore32", 0);
                    if (oldBest < Score.getDistance())
                        Facts.putGameFact("HighScore32", Score.getDistance());
                }
            };

            Level.setWinCallback(sc);
            Level.setLoseCallback(sc);
        }

        /*
         * this level adds multiple background layers, and it also allows the
         * hero to jump via touch
         */
        else if (whichLevel == 33) {
            // set up a standard side scroller with tilt:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press the hero to\nmake it jump", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Level.setCameraChase(h);
            // this says that touching makes the hero jump
            h.setTouchToJump();
            // this is the force of a jump. remember that up is positive.
            h.setJumpImpulses(0, 10);
            // the sound to play when we jump
            h.setJumpSound("fwapfwap.ogg");

            // set up our background again, but add a few more layers
            Background.setColor(23, 180, 255);
            // this layer has a scroll factor of 0... it won't move
            Background.addHorizontalLayer(0, 1, "back.png", 0, 960, 640);
            // this layer moves at half the speed of the hero
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 480, 320);
            // this layer is faster than the hero
            Background.addHorizontalLayer(1.25f, 1, "front.png", 20, 454, 80);
        }

        /*
         * tilt doesn't always work so nicely in side scrollers. An alternative
         * is for the hero to have a fixed rate of motion. Another issue was
         * that you had to touch the hero itself to make it jump. Now, we use an
         * invisible button so touching any part of the screen makes the hero
         * jump.
         */
        else if (whichLevel == 34) {
            // set up a side scroller, but don't turn on tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press anywhere to jump", 255, 255, 255, "arial.ttf", 32);
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // note: the bounding box does not have friction, and neither does
            // the hero
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 0, 0, 0);

            // make a hero, and ensure that it doesn't rotate
            Hero h = Hero.makeAsCircle(2, 0, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            // give the hero a fixed velocity
            h.addVelocity(25, 0, false);
            // center the camera a little ahead of the hero, so he is not
            // centered
            h.setCameraOffset(15, 0);
            // enable jumping
            h.setJumpImpulses(0, 10);
            Level.setCameraChase(h);
            // set up the background
            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // draw a jump button that covers the whole screen
            Control.addJumpButton(0, 0, 960, 640, "", h);

            // if the hero jumps over the destination, we have a problem. To fix
            // it, let's put an invisible enemy right after the destination, so
            // that if the hero misses the destination, it hits the enemy and we
            // can start over. Of course, we could just do the destination like
            // this instead, but this is more fun...
            Enemy.makeAsBox(130, 0, .5f, 32, "");
        }

        /*
         * the default is that once a hero jumps, it can't jump again until it
         * touches an obstacle (floor or wall). Here, we enable multiple jumps.
         * Coupled with a small jump impulse, this makes jumping feel more like
         * swimming or controlling a helicopter.
         */
        else if (whichLevel == 35) {
            // Note: we can go above the trees
            Level.configure(3 * 48, 38);
            Physics.configure(0, -10);
            PreScene.get().addText("Multi-jump is enabled", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 38, "red.png", 1, 0, 0);
            Hero h = Hero.makeAsBox(2, 0, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            h.addVelocity(5, 0, false);
            Level.setCameraChase(h);
            h.setCameraOffset(15, 0);
            // the hero now has multijump, with small jumps:
            h.setMultiJumpOn();
            h.setJumpImpulses(0, 6);

            // this is all the same as before, to include the invisible enemy
            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);
            Control.addJumpButton(0, 0, 960, 640, "", h);
            Destination.makeAsCircle(120, 31, 2, 2, "mustardball.png");
            Enemy.makeAsBox(130, 0, .5f, 38, "");
            Score.setVictoryDestination(1);
        }

        /*
         * This level shows that we can make a hero move based on how we touch
         * the screen
         */
        else if (whichLevel == 36) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            PreScene.get().addText("Press screen borders\nto move the hero", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 0, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            Level.setCameraChase(h);

            // this lets the hero flip its image when it moves backwards
            h.setDefaultAnimation(new Animation("stars.png", 200, true, 0, 0));
            h.setDefaultReverseAnimation(new Animation("stars_flipped.png", 200, true, 7, 7));

            Destination.makeAsCircle(120, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // let's draw an enemy, just in case anyone wants to try to go to
            // the top left corner
            Enemy.makeAsCircle(3, 27, 3, 3, "redball.png");

            // draw some buttons for moving the hero
            Control.addLeftButton(0, 100, 100, 440, "", 15, h);
            Control.addRightButton(860, 100, 100, 440, "", 15, h);
            Control.addUpButton(100, 540, 760, 100, "", 15, h);
            Control.addDownButton(100, 0, 760, 100, "", 15, h);
        }

        /*
         * In the last level, we had complete control of the hero's movement.
         * Here, we give the hero a fixed velocity, and only control its up/down
         * movement.
         */
        else if (whichLevel == 37) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            PreScene.get().addText("Press screen borders\nto move up and down", 255, 255, 255, "arial.ttf", 32);
            // The box and hero should not have friction
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            Destination.makeAsCircle(120, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(10, 0, false);

            Level.setCameraChase(h);

            // draw an enemy to avoid, and one at the end
            Enemy.makeAsCircle(53, 28, 3, 3, "redball.png");
            Enemy.makeAsBox(130, 0, .5f, 32, "");

            // draw the up/down controls
            Control.addDownButton(100, 0, 760, 100, "", 15, h);
            Control.addUpButton(100, 540, 760, 100, "", 15, h);
        }

        /*
         * this level demonstrates crawling heroes. We can use this to simulate
         * crawling, ducking, rolling, spinning, etc. Note, too, that we can use
         * it to make the hero defeat certain enemies via crawl.
         */
        else if (whichLevel == 38) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press the screen\nto crawl", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);
            Destination.makeAsCircle(120, 0, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(5, 0, false);
            Level.setCameraChase(h);
            // to enable crawling, we just draw a crawl button on the screen
            Control.addCrawlButton(0, 0, 960, 640, "", h);

            // make an enemy who we can defeat by colliding with it while
            // crawling
            Enemy e = Enemy.makeAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();
        }

        /*
         * We can make a hero start moving only when it is pressed. This can
         * even let the hero hover until it is pressed. We could also use this
         * to have a game where the player puts obstacles in place, then starts
         * the hero moving.
         */
        else if (whichLevel == 39) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press the hero\nto start moving\n", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            Destination.makeAsCircle(120, 0, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero who doesn't start moving until it is touched
            //
            // note that this hero is a box, and the hero is "norotate". You
            // will probably get strange behaviors if you choose any other
            // options
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            h.setTouchAndGo(10, 0);
            Level.setCameraChase(h);
        }

        /*
         * LibLOL has limited support for SVG. If you draw a picture in Inkscape
         * or another SVG tool, and it only consists of lines, then you can
         * import it into your game as an obstacle. Drawing a picture on top of
         * the obstacle is probably a good idea, though we don't bother in this
         * level
         */
        else if (whichLevel == 40) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Tilt.setAsVelocity(true);
            PreScene.get().addText("Obstacles can\nbe drawn from SVG\nfiles", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            // make a hero who can jump
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // draw an obstacle from SVG
            Svg.importLineDrawing("shape.svg", 2f, .5f, 25f, 15f, new Svg.ActorCallback() {
                @Override
                public void handle(Actor line) {
                    // This code is run each time a line of the SVG is drawn.  When we get a line,
                    // we'll give it some density and friction.  Remember that the line is
                    // actually a rotated obstacle
                    line.setPhysics(1, 0, .1f);
                }
            });

            // provide a destination
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /*
         * In a side-scrolling game, it is useful to be able to change the
         * hero's speed either permanently or temporarily. In LibLOL, we can use
         * a collision between a hero and an obstacle to achieve this effect.
         */
        else if (whichLevel == 41) {
            Level.configure(10 * 48, 32);
            Physics.configure(0, 0);
            PreScene.get().addText("Speed boosters and reducers", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 10 * 480, 32, "", 1, 0, 1);

            Hero h = Hero.makeAsCircle(2, 0, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.addVelocity(10, 0, false);
            Level.setCameraChase(h);

            Destination.makeAsCircle(450, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // place a speed-up obstacle that lasts for 2 seconds
            Obstacle o1 = Obstacle.makeAsCircle(40, 1, 4, 4, "purpleball.png");
            o1.setSpeedBoost(20, 0, 2);

            // place a slow-down obstacle that lasts for 3 seconds
            Obstacle o2 = Obstacle.makeAsCircle(120, 1, 4, 6, "purpleball.png");
            o2.setSpeedBoost(-9, 0, 3);

            // place a permanent +3 speedup obstacle... the -1 means "forever"
            Obstacle o3 = Obstacle.makeAsCircle(240, 1, 4, 4, "purpleball.png");
            o3.setSpeedBoost(20, 0, -1);
        }

        /*
         * this is a very gross level, which exists just to show that
         * backgrounds can scroll vertically.
         */
        else if (whichLevel == 42) {
            // set up a level where tilt only makes the hero move up and down
            Level.configure(48, 4 * 32);
            Physics.configure(0, 0);
            Tilt.enable(0, 10);
            PreScene.get().addText("Vertical scroller demo", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 4 * 32, "red.png", 1, 0, 1);

            Hero h = Hero.makeAsCircle(2, 120, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Level.setCameraChase(h);

            Destination.makeAsBox(0, 2, 48, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up vertical scrolling backgrounds
            Background.setColor(255, 0, 255);
            Background.addVerticalLayer(1, 0, "back.png", 0, 960, 640);
            Background.addVerticalLayer(1, .5f, "mid.png", 0, 960, 640);
            Background.addVerticalLayer(1, 1, "front.png", 0, 454, 80);
        }

        /*
         * the next few levels demonstrate support for throwing projectiles. In
         * this level, we throw projectiles by touching the hero. Here, the
         * projectile always goes in the same direction
         */
        else if (whichLevel == 43) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Press the hero\nto make it throw\nprojectiles", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a hero, and indicate that touching it makes it throw
            // projectiles
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            // when the hero is touched, a projectile will
            // fly straight up, out of the top of the hero.
            h.setTouchToThrow(h, 1.5f, 3, 0, 10);
            h.setMoveByTilting();

            // configure a pool of projectiles. We say that there can be no more
            // than 3 projectiles in flight at any time.
            ProjectilePool.configure(3, 1, 1, "greyball.png", 1, 0, true);
        }

        /*
         * This is another demo of how throwing projectiles works. Like the
         * previous demo, it doesn't actually use projectiles for anything, it
         * is just to show how to get some different behaviors in terms of how
         * the projectiles move. In this case, we show that we can limit the
         * distance that projectiles travel, and that we can put a control on
         * the HUD for throwing projectiles
         */
        else if (whichLevel == 44) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press anywhere\nto throw a gray\nball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 30, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 0, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a pool of projectiles, but now once the projectiles travel
            // more than 25 meters, they disappear
            ProjectilePool.configure(100, 1, 1, "greyball.png", 1, 0, true);
            ProjectilePool.setRange(25);

            // add a button for throwing projectiles. Notice that this butotn
            // keeps throwing as long as it is held, but we've capped it to
            // throw no more than once per 100 milliseconds
            Control.addThrowButton(0, 0, 960, 640, "", h, 100, 3, 1.5f, 30, 0);
            Level.setCameraChase(h);
        }

        /*
         * this level demonstrates that we can defeat enemies by throwing
         * projectiles at them
         */
        else if (whichLevel == 45) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            PreScene.get().addText("Defeat all enemies\nto win", 255, 255, 255, "arial.ttf", 32);

            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // set up our projectiles... note that now projectiles each do 2
            // units of damage
            ProjectilePool.configure(3, .4f, .1f, "greyball.png", 2, 0, true);

            // draw a few enemies... note that they have different amounts of
            // damage, so it takes different numbers of projectiles to defeat
            // them.
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5) {
                Enemy ee = Enemy.makeAsCircle(i, i + 5, 2, 2, "redball.png");
                ee.setPhysics(1.0f, 0.3f, 0.6f);
                ee.setDamage(i);
            }

            // win by defeating enemies, of course!
            Score.setVictoryEnemyCount();

            // this button only throws one projectile per press...
            Control.addSingleThrowButton(0, 0, 960, 640, "", h, .2f, -.5f, 0, 10);
        }

        /*
         * This level shows how to throw projectiles in a variety of directions.
         */
        else if (whichLevel == 46) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press anywhere\nto throw a ball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Level.setCameraChase(h);

            Destination.makeAsCircle(120, 0, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions...
            // again, note that if we hold the button, it keeps throwing
            Control.addDirectionalThrowButton(0, 0, 960, 640, "", h, 0, 0, 0);

            // set up our pool of projectiles. The main challenge here is that
            // the farther from the hero we press, the faster the projectile
            // goes, so we multiply the velocity by .8 to slow it down a bit
            ProjectilePool.configure(100, 1, 1, "greyball.png", 1, 0, true);
            ProjectilePool.setProjectileVectorDampeningFactor(.8f);
            ProjectilePool.setRange(30);
        }

        /*
         * this level shows that with the "vector" projectiles, we can still
         * have gravity affect the projectiles. This is very good for
         * basketball-style games.
         */
        else if (whichLevel == 47) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press anywhere\nto throw a ball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(0, 0, 0.6f);
            h.setMoveByTilting();
            Level.setCameraChase(h);

            Destination.makeAsCircle(120, 0, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // we use a "single throw" button so that holding doesn't throw more
            // projectiles.
            Control.addDirectionalSingleThrowButton(0, 0, 960, 640, "", h, 1.5f, 1.5f);

            // we turn on projectile gravity, and then we enable collisions for
            // projectiles. This means that when a projectile collides with
            // something, it will transfer its momentum to that thing, if that
            // thing is moveable. This is a step toward our goal of being able
            // to bounce a basketball off of a backboard, but it's not quite
            // enough...
            ProjectilePool.configure(100, 1, 1, "greyball.png", 1, 0, true);
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
            Obstacle o = Obstacle.makeAsBox(10, 20, 2, 2, "red.png");

            // now comes the tricky part... we want to make it so that when the
            // ball hits the obstacle (the backboard), it doesn't disappear. The
            // only time a projectile does not disappear when hitting an
            // obstacle is when you provide custom code to run on a
            // projectile/obstacle collision... in that case, you are
            // responsible for removing the projectile (or for not removing it).
            // That being the case, we can set a "callback" to run custom code
            // when the projectile and obstacle collide, and then just have the
            // custom code do nothing.

            // this line says when a projectile and obstacle collide, if the
            // goodie counts are at least 0,0,0,0, then run the
            // callback code.
            o.setProjectileCollisionCallback(0, 0, 0, 0, new LolCallback() {
                public void onEvent() {
                }
            });
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
        else if (whichLevel == 48) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 0);
            PreScene.get().addText("Throw balls at \nthe enemies before\nthey reproduce", 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setTouchToThrow(h, 2, -.5f, 0, 10);
            h.setMoveByTilting();

            // configure a pool of projectiles... now we have a sound that plays
            // when a projectile is thrown, and another for when it disappears
            ProjectilePool.configure(100, .5f, .5f, "greyball.png", 1, 0, true);
            ProjectilePool.setThrowSound("fwapfwap.ogg");
            ProjectilePool.setProjectileDisappearSound("slowdown.ogg");

            // draw an enemy that makes a sound when it disappears
            Enemy e = Enemy.makeAsCircle(23, 20, 1, 1, "redball.png");
            e.setDisappearSound("lowpitch.ogg");

            // This is tricky. We want to make the enemy reproduce. To that end,
            // we're going to set up a Callback that runs in a few seconds. The
            // tricky things are that (a) we don't want an enemy to reproduce if
            // the enemy has been defeated; (b) we want to limit the number of
            // times any enemy reproduces, so that reproductions don't get out
            // of hand; and (c) we want the reproduced enemies to also
            // reproduce.

            // We're going to make a SimpleCallback object to encapsulate the
            // code that we want to run. The trick is that SimpleCallback has a
            // field called "intVal", which is just an integer that we can use
            // however we want, and it has a field called "attachedSprite",
            // which is a way of keeping track of the entity (in this case, an
            // enemy) with which the callback is associated.
            LolCallback sc = new LolCallback() {
                public void onEvent() {
                    // only reproduce the enemy if it is visible
                    if (mAttachedActor.getVisible()) {
                        // make an enemy to the left of and above attachedSprite
                        Enemy left = Enemy.makeAsCircle(mAttachedActor.getXPosition() - 2 * mIntVal,
                                mAttachedActor.getYPosition() + 2 * mIntVal, mAttachedActor.getWidth(),
                                mAttachedActor.getHeight(), "redball.png");
                        left.setDisappearSound("lowpitch.ogg");

                        // make an enemy to the right of and above
                        // attachedSprite
                        Enemy right = Enemy.makeAsCircle(mAttachedActor.getXPosition() + 2 * mIntVal,
                                mAttachedActor.getYPosition() + 2 * mIntVal, mAttachedActor.getWidth(),
                                mAttachedActor.getHeight(), "redball.png");
                        right.setDisappearSound("lowpitch.ogg");

                        // if there are reproductions left, then have
                        // attachedSprite and its two new children all reproduce
                        // in 2 seconds
                        if (mIntVal > 0) {
                            // first, do the parent
                            mIntVal--;
                            // on the next line, 'this' refers to the
                            // SimpleCallback object
                            Level.setTimerCallback(2, this);
                            // to do the children, create a new callback for
                            // each of them. We can 'clone' this SimpleCallback
                            // and then just change the attachedSprite, so that
                            // we don't have to re-write this code.
                            LolCallback l = this.clone();
                            l.mAttachedActor = left;
                            Level.setTimerCallback(2, l);
                            LolCallback r = this.clone();
                            r.mAttachedActor = right;
                            Level.setTimerCallback(2, r);
                        }
                    }
                }
            };
            sc.mIntVal = 2;
            sc.mAttachedActor = e;

            // request that in 2 seconds, if the enemy is still visible,
            // onTimerCallback() will run, with id == 2. Be sure to look at
            // the onTimerCallback code (below) for more information. Note that
            // there are two versions of the function, and this uses the second!
            Level.setTimerCallback(2, sc);

            // win by defeating enemies
            Score.setVictoryEnemyCount();

            // put a count of defeated enemies on the screen
            Display.addDefeatedCount(0, " Enemies Defeated", 20, 20);
        }

        /*
         * This level shows that we can have moveable enemies that reproduce. Be
         * careful... it is possible to make a lot of enemies, really quickly
         */
        else if (whichLevel == 49) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("These enemies are\nreally tricky", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            Destination.makeAsCircle(29, 29, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make our initial enemy
            Enemy e = Enemy.makeAsCircle(23, 2, 1, 1, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setMoveByTilting();

            // set a timer callback on the enemy. warning: "6" is going to lead
            // to lots of enemies eventually, and there's no way to defeat them
            // in this level! Again, be sure to look at onEnemyTimerCallback()
            // below.
            LolCallback sc = new LolCallback() {
                public void onEvent() {
                    // Make the new enemy
                    Enemy e2 = Enemy.makeAsCircle(mAttachedActor.getXPosition(), mAttachedActor.getYPosition(),
                            mAttachedActor.getWidth(), mAttachedActor.getHeight(), "redball.png");
                    e2.setPhysics(1.0f, 0.3f, 0.6f);
                    e2.setMoveByTilting();
                    // make more enemies?
                    if (mIntVal > 0) {
                        mIntVal--;
                        Level.setTimerCallback(2, this);
                        LolCallback c2 = this.clone();
                        c2.mAttachedActor = e2;
                        Level.setTimerCallback(2, c2);
                    }
                }
            };
            sc.mAttachedActor = e;
            sc.mIntVal = 6;
            Level.setTimerCallback(2, sc);
        }

        /*
         * this level shows simple animation. Every entity can have a default
         * animation.
         */
        else if (whichLevel == 50) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Animations", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // this hero will be animated:
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // this says that we scroll through the 0, 1, 2, and 3 cells of the
            // image, and we show each for 200 milliseconds. This is the "easy"
            // animation mechanism, where every cell is shown for the same
            // amount of time
            h.setDefaultAnimation(new Animation("stars.png", 200, true, 0, 1, 2, 3));
        }

        /*
         * this level introduces jumping animations and disappearance animations
         */
        else if (whichLevel == 51) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press the hero to\nmake it jump", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero, and give it two animations: one for when it is in
            // the air, and another for the rest of the time.
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // this is the more complex form of animation... we show the
            // different cells for different lengths of time
            h.setDefaultAnimation(new Animation("stars.png", 4, true).to(0, 150).to(1, 200).to(2, 300).to(3, 350));
            // we can use the complex form to express the simpler animation, of
            // course
            h.setJumpAnimation(new Animation("stars.png", 4, true).to(4, 200).to(5, 200).to(6, 200).to(7, 200));

            // create a goodie that has a disappearance animation. When the
            // goodie is ready to disappear, we'll remove it, and then we'll run
            // the disappear animation. That means that we can make it have any
            // size we want, but we need to offset it from the (defunct)
            // goodie's position. Note, too, that the final cell is blank, so
            // that we don't leave a residue on the screen.
            Goodie g = Goodie.makeAsCircle(15, 9, 5, 5, "stars.png");
            g.setDisappearAnimation(new Animation("starburst.png", 4, false).to(2, 200).to(1, 200).to(0, 200)
                    .to(3, 200), 1, 0, 5, 5);
        }

        /*
         * this level shows that projectiles can be animated, and that we can
         * animate the hero while it throws a projectile
         */
        else if (whichLevel == 52) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Press the hero\nto make it\nthrow a ball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up our hero
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "colorstar.png");
            h.setPhysics(1, 0, 0.6f);
            h.setTouchToThrow(h, 0, -.5f, 0, 10);
            h.setMoveByTilting();

            // set up an animation when the hero throws:
            h.setThrowAnimation(new Animation("colorstar.png", 2, false).to(3, 100).to(4, 500));

            // make a projectile pool and give an animation pattern for the
            // projectiles
            ProjectilePool.configure(100, 1, 1, "flystar.png", 1, 0, true);
            ProjectilePool.setAnimation(new Animation("flystar.png", 100, true, 0, 1));
        }

        /*
         * This level explores invincibility animation. While we're at it, we
         * make some enemies that aren't affected by invincibility, and some
         * that can even damage the hero while it is invincible.
         */
        else if (whichLevel == 53) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The blue ball will\nmake you invincible\nfor 15 seconds", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Destination.makeAsCircle(29, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make an animated hero, and give it an invincibility animation
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setDefaultAnimation(new Animation("colorstar.png", 4, true).to(0, 300).to(1, 300).to(2, 300).to(3, 300));
            h.setInvincibleAnimation(new Animation("colorstar.png", 4, true).to(4, 100).to(5, 100).to(6, 100)
                    .to(7, 100));

            // make some enemies
            for (int i = 0; i < 5; ++i) {
                Enemy e = Enemy.makeAsCircle(5 * i + 1, 25, 2, 2, "redball.png");
                e.setPhysics(1.0f, 0.3f, 0.6f);
                e.setRotationSpeed(1);
                e.setDamage(4);
                e.setDisappearSound("hipitch.ogg");

                // The first enemy we create will harm the hero even if the hero
                // is invincible
                if (i == 0)
                    e.setImmuneToInvincibility();
                // the second enemy will not be harmed by invincibility, but
                // won't harm an invincible hero
                if (i == 1)
                    e.setResistInvincibility();
            }
            // neat trick: this enemy does zero damage, but slows the hero down.
            Enemy e = Enemy.makeAsCircle(30, 20, 2, 2, "redball.png");
            e.setPhysics(10, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(0);

            // add a goodie that makes the hero invincible
            Goodie g = Goodie.makeAsCircle(30, 30, 2, 2, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);
            Display.addGoodieCount(1, 0, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);

            // draw a picture when the level is won, and don't print text...
            // this particular picture isn't very useful
            WinScene.get().addImage("fade.png", 0, 0, 960, 640);
            WinScene.get().setDefaultWinText("");
        }

        /*
         * demonstrate crawl animation, and also show that on multitouch phones,
         * we can "crawl" in the air while jumping.
         */
        else if (whichLevel == 54) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press the left side of\nthe screen to crawl\n" + "or the right side\nto jump.",
                    255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);

            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero with fixed velocity, and give it crawl and jump
            // animations
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "stars.png");
            h.setPhysics(1, 0, 0);
            h.addVelocity(15, 0, false);
            h.setCrawlAnimation(new Animation("stars.png", 4, true).to(0, 100).to(1, 300).to(2, 300).to(3, 100));
            h.setJumpAnimation(new Animation("stars.png", 4, true).to(4, 200).to(5, 200).to(6, 200).to(7, 200));

            // enable hero jumping and crawling
            h.setJumpImpulses(0, 15);
            Control.addJumpButton(0, 0, 480, 640, "", h);
            Control.addCrawlButton(480, 0, 480, 640, "", h);

            // add an enemy we can defeat via crawling, just for fun. It should
            // be defeated even by a "jump crawl"
            Enemy e = Enemy.makeAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();

            // include a picture on the "try again" screen
            LoseScene.get().addImage("fade.png", 0, 0, 960, 640);
            LoseScene.get().setDefaultText("Oh well...");
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
        else if (whichLevel == 55) {
            // set up a basic level with a bunch of goodies
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Since colorstar.png has 8 frames, and we're displaying frame 0 as
            // "health == 0", let's add 7 more goodies, each of which adds 1 to
            // the hero's strength.
            for (int i = 0; i < 7; ++i) {
                Goodie g = Goodie.makeAsCircle(5 + 2 * i, 5 + 2 * i, 2, 2, "blueball.png");
                g.setStrengthBoost(1);
            }

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make 8 enemies, each with strength == 1. This means we can lose
            // the level, and that we can test moving our strength all the way
            // up to 7, and all the way back down to 0.
            for (int i = 0; i < 8; ++i) {
                Enemy e = Enemy.makeAsCircle(5 + 2 * i, 1 + 2 * i, 2, 2, "redball.png");
                e.setDamage(1);
            }

            // Note: colorstar.png has 8 cells...
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // provide some code to run when the hero's strength changes
            h.setStrengthChangeCallback(new LolCallback() {
                public void onEvent() {
                    // get the hero's strength. Since the hero isn't dead, the
                    // strength is at least 1. Since there are 7 strength
                    // booster goodies, the strength is at most 8.
                    int s = ((Hero) mAttachedActor).getStrength();
                    // set the hero's image index to (s-1), i.e., one of the
                    // indices in the range 0..7, depending on strength
                    mAttachedActor.setImage("colorstar.png", s - 1);

                }
            });
        }

        /*
         * demonstrate that obstacles can defeat enemies, and that we can use
         * this feature to have obstacles that only defeat certain "marked"
         * enemies
         */
        else if (whichLevel == 56) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            // increase the speed at which tilt affects velocity
            Tilt.setGravityMultiplier(3);
            PreScene.get().addText("You can defeat\ntwo enemies with\nthe blue ball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // put an enemy defeated count on the screen, in red with a small
            // font
            Display.addDefeatedCount(2, " Enemies Defeated", 20, 20, "arial.ttf", 255, 0, 0, 10);

            // make a moveable obstacle that can defeat enemies
            Obstacle o = Obstacle.makeAsCircle(10, 2, 4, 4, "blueball.png");
            o.setPhysics(.1f, 0, 0.6f);
            o.setMoveByTilting();
            // this says that we don't need to collect any goodies before this
            // obstacle defeats enemies (0,0,0,0), and that when this obstacle
            // collides with any enemy, the onEnemyCollideCallback() code will
            // run, with id == 14. Notice, too, that there will be a half second
            // delay before the code runs.
            o.setEnemyCollisionCallback(0, 0, 0, 0, .5f, new LolCallback() {
                public void onEvent() {
                    // This obstacle can only defeat the big enemy, and it
                    // disappears when it defeats the enemy
                    if (mCollideActor.getInfoText().equals("big")) {
                        ((Enemy) mCollideActor).defeat(true);
                        mAttachedActor.remove(true);
                    }

                }
            });

            // make a small obstacle that can also defeat enemies, but doesn't
            // disappear
            Obstacle o2 = Obstacle.makeAsCircle(.5f, .5f, 2, 2, "blueball.png");
            o2.setPhysics(1, 0, 0.6f);
            o2.setMoveByTilting();
            o2.setEnemyCollisionCallback(0, 0, 0, 0, 0, new LolCallback() {
                public void onEvent() {
                    ((Enemy) mCollideActor).defeat(true);
                }
            });

            // make four enemies
            Enemy e = Enemy.makeAsCircle(40, 2, 4, 4, "redball.png");
            e.setPhysics(1, 0, 0.6f);
            e.setMoveByTilting();
            Enemy e1 = Enemy.makeAsCircle(30, 2, 4, 4, "redball.png");
            e1.setPhysics(1, 0, 0.6f);
            Enemy e2 = Enemy.makeAsCircle(40, 22, 2, 2, "redball.png");
            e2.setPhysics(1, 0, 0.6f);
            e2.setMoveByTilting();
            Enemy e3 = Enemy.makeAsCircle(40, 12, 4, 4, "redball.png");
            e3.setPhysics(1, 0, 0.6f);
            e3.setMoveByTilting();

            // now let's put a note into e2 and e3
            e2.setInfoText("small");
            e3.setInfoText("big");

            // win by defeating enemies
            Score.setVictoryEnemyCount(2);

            // be sure to look at onEnemyCollideCallback to see how this level
            // will play out.
        }

        /*
         * this level shows an odd way of moving the hero. There's friction on
         * the floor, so it can only move by tilting while the hero is in the
         * air
         */
        else if (whichLevel == 57) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press the hero to\nmake it jump", 255, 255, 255, "arial.ttf", 32);
            // note: the floor has friction
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a box hero with friction... it won't roll on the floor, so
            // it's stuck!
            Hero h = Hero.makeAsBox(2, 2, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 5);
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // the hero *can* jump...
            h.setTouchToJump();
            h.setJumpImpulses(0, 15);

            // draw a background
            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);
        }

        /*
         * this level shows that we can put an obstacle on the screen and use it
         * to make the hero throw projectiles. It also shows that we can make
         * entities that shrink over time... growth is possible too, with a
         * negative value.
         */
        else if (whichLevel == 58) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(4, 5, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // make an obstacle that causes the hero to throw Projectiles when
            // touched
            Obstacle o = Obstacle.makeAsCircle(43, 27, 5, 5, "purpleball.png");
            o.setCollisionsEnabled(false);
            o.setTouchToThrow(h, 1.5f, 1.5f, 0, 15);

            // set up our projectiles
            ProjectilePool.configure(3, 1, 1, "colorstar.png", 2, 0, true);
            ProjectilePool.setNumberOfProjectiles(20);
            // there are only 20... throw them carefully

            // Allow the projectile image to be chosen randomly from a sprite
            // sheet
            ProjectilePool.setImageSource("colorstar.png");

            // show how many shots are left
            Display.addProjectileCount(" projectiles left", 5, 300, "arial.ttf", 255, 0, 255, 12);

            // draw a bunch of enemies to defeat
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5)
                Enemy.makeAsCircle(i, i + 8, 2, 2, "redball.png");

            // draw a few obstacles that shrink over time, to show that circles
            // and boxes work, we can shrink the X and Y rates independently,
            // and we can opt to center things as they shrink or grow
            Obstacle floor = Obstacle.makeAsBox(2, 3, 42, 3, "red.png");
            floor.setShrinkOverTime(1, 1, true);

            Obstacle roof = Obstacle.makeAsBox(24, 30, 1, 1, "red.png");
            roof.setShrinkOverTime(-1, 0, false);

            Obstacle ball1 = Obstacle.makeAsCircle(40, 8, 8, 8, "purpleball.png");
            ball1.setShrinkOverTime(1, 2, true);

            Obstacle ball2 = Obstacle.makeAsCircle(40, 16, 8, 8, "purpleball.png");
            ball2.setShrinkOverTime(2, 1, false);

            Score.setVictoryEnemyCount(5);
        }

        /*
         * this level shows that we can make a hero in the air rotate. Rotation
         * doesn't do anything, but it looks nice...
         */
        else if (whichLevel == 59) {
            // make a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            PreScene.get().addText("Press to rotate the hero", 255, 255, 255, "arial.ttf", 32);

            // warning: this destination is just out of the hero's reach when
            // the hero
            // jumps... you'll have to hit the side wall and jump again to reach
            // it!
            Destination.makeAsCircle(46, 8, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);

            // make the hero jumpable, so that we can see it spin in the air
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 10);
            h.setTouchToJump();

            // add rotation buttons
            Control.addRotateButton(0, 480, 160, 160, "", -.5f, h);
            Control.addRotateButton(760, 480, 160, 160, "", .5f, h);
        }

        /**
         * we can attach movement buttons to any moveable entity, so in this
         * case, we attach it to an obstacle to get an arkanoid-like effect.
         */
        else if (whichLevel == 60) {
            // make a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            Destination.makeAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero who is always moving... note there is no friction,
            // anywhere, and the hero is elastic... it won't ever stop...
            Hero h = Hero.makeAsCircle(4, 4, 3, 3, "greenball.png");
            h.setPhysics(0, 1, .1f);
            h.addVelocity(0, 10, false);

            // make an obstacle and then connect it to some controls
            Obstacle o = Obstacle.makeAsBox(2, 30.9f, 4, 1, "red.png");
            o.setPhysics(100, 1, .1f);
            Control.addLeftButton(0, 0, 480, 640, "", 5, o);
            Control.addRightButton(480, 0, 480, 640, "", 5, o);
        }

        /*
         * this level demonstrates that things can appear and disappear on
         * simple timers
         */
        else if (whichLevel == 61) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Things will appear \nand disappear...", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // create an enemy that will quietly disappear after 2 seconds
            Enemy e1 = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e1.setPhysics(1.0f, 0.3f, 0.6f);
            e1.setRotationSpeed(1);
            e1.setDisappearDelay(2, true);

            // create an enemy that will appear after 3 seconds
            Enemy e2 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e2.setPhysics(1.0f, 0.3f, 0.6f);
            e2.setRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 3, true);
            e2.setAppearDelay(3);
        }

        /*
         * This level demonstrates the use of timer callbacks. We can use timers
         * to make more of the level appear over time. In this case, we'll chain
         * the timer callbacks together, so that we can get more and more things
         * to develop. Be sure to look at the onTimerCallback code to see how
         * the rest of this level works.
         */
        else if (whichLevel == 62) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            PreScene.get().addText("There's nothing to\ndo... yet", 255, 255, 255, "arial.ttf", 20);

            // note: there's no destination yet, but we still say it's how to
            // win... we'll get a destination in this level after a few timers
            // run...
            Score.setVictoryDestination(1);

            // set a timer callback. after three seconds, the callback will run
            Level.setTimerCallback(2, new LolCallback() {
                public void onEvent() {
                    // put up a pause scene to interrupt gameplay
                    PauseScene.get().reset();
                    PauseScene.get().addText("Ooh... a draggable enemy", 255, 255, 0, "arial.ttf", 12);
                    PauseScene.get().show();
                    // make a draggable enemy
                    Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
                    e3.setPhysics(1.0f, 0.3f, 0.6f);
                    e3.setCanDrag(true);
                }
            });

            // set another callback that runs after 6 seconds (note: time
            // doesn't count while the PauseScene is showing...)
            Level.setTimerCallback(6, new LolCallback() {
                public void onEvent() {
                    // clear the pause scene, then put new text on it
                    PauseScene.get().reset();
                    PauseScene.get().addText("Touch the enemy and it will go away", 255, 0, 255, "arial.ttf", 12);
                    PauseScene.get().show();
                    // add an enemy that is touch-to-defeat
                    Enemy e4 = Enemy.makeAsCircle(35, 5, 2, 2, "redball.png");
                    e4.setPhysics(1.0f, 0.3f, 0.6f);
                    e4.setDisappearOnTouch();
                }
            });

            // set a callback that runs after 9 seconds. Though it's not
            // necessary in this case, we're going to make the callback an
            // explicit object. This can be useful, as we'll see later on.
            Level.setTimerCallback(9, new LolCallback() {
                public void onEvent() {
                    // draw an enemy, a goodie, and a destination, all with
                    // fixed velocities
                    PauseScene.get().reset();
                    PauseScene.get().addText("Now you can see the rest of the level", 255, 255, 0, "arial.ttf", 12);
                    PauseScene.get().show();
                    Destination d = Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
                    d.addVelocity(-.5f, -1, false);

                    Enemy e5 = Enemy.makeAsCircle(35, 15, 2, 2, "redball.png");
                    e5.setPhysics(1.0f, 0.3f, 0.6f);
                    e5.addVelocity(4, 4, false);

                    Goodie gg = Goodie.makeAsCircle(10, 10, 2, 2, "blueball.png");
                    gg.addVelocity(5, 5, false);
                }
            });

            // Lastly, we can make a timer callback that runs over and over
            // again. This one starts after 2 seconds, then runs every second.
            Level.setTimerCallback(2, 1, new LolCallback() {
                public void onEvent() {
                    // note that every SimpleCallback has a field called
                    // "intVal" that is initially 0. By using and then modifying
                    // that field inside of the timer code, we can ensure that
                    // each execution of the timer is slightly different, even
                    // if the game state hasn't changed.
                    Obstacle.makeAsCircle(mIntVal % 48, mIntVal / 48, 1, 1, "purpleball.png");
                    mIntVal++;
                }
            });
        }

        /*
         * This level shows callbacks that run on a collision between hero and
         * obstacle. In this case, it lets us draw out the next part of the
         * level later, instead of drawing the whole thing right now. In a real
         * level, we'd draw a few screens at a time, and not put the callback
         * obstacle at the end of a screen, so that we'd never see the drawing
         * of stuff taking place, but for this demo, that's actually a nice
         * effect. Be sure to look at onCollideCallback for more details.
         */
        else if (whichLevel == 63) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Keep going right!", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Level.setCameraChase(h);

            Display.addGoodieCount(1, 0, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);
            Score.setVictoryDestination(1);

            // this obstacle is a collision callback... when the hero hits it,
            // the next part of the level appears, via onHeroCollideCallback().
            // Note, too, that it disappears when the hero hits it, so we can
            // play a sound if we want...
            Obstacle o = Obstacle.makeAsBox(30, 0, 1, 32, "purpleball.png");
            o.setPhysics(1, 0, 1);
            // the callback id is 0, there is no delay, and no goodies are
            // needed before it works
            o.setHeroCollisionCallback(0, 0, 0, 0, 0, new LolCallback() {
                public void onEvent() {
                    // get rid of the obstacle we just collided with
                    mAttachedActor.remove(false);
                    // make a goodie
                    Goodie.makeAsCircle(45, 1, 2, 2, "blueball.png");
                    // make an obstacle that is a callback, but that doesn't
                    // work until the goodie count is 1
                    Obstacle oo = Obstacle.makeAsBox(60, 0, 1, 32, "purpleball.png");

                    // we're going to chain a bunch of callbacks together, and
                    // the best way to do that is to make a single callback that
                    // behaves differently based on the value of the callback's
                    // intVal field.
                    LolCallback sc2 = new LolCallback() {
                        public void onEvent() {
                            // The second callback works the same way
                            if (mIntVal == 0) {
                                mAttachedActor.remove(false);
                                Goodie.makeAsCircle(75, 21, 2, 2, "blueball.png");

                                Obstacle oo = Obstacle.makeAsBox(90, 0, 1, 32, "purpleball.png");
                                oo.setHeroCollisionCallback(2, 0, 0, 0, 0, this);
                                mIntVal = 1;
                            }
                            // same for the third callback
                            else if (mIntVal == 1) {
                                mAttachedActor.remove(false);
                                Goodie.makeAsCircle(105, 1, 2, 2, "blueball.png");

                                Obstacle oo = Obstacle.makeAsBox(120, 0, 1, 32, "purpleball.png");
                                oo.setHeroCollisionCallback(3, 0, 0, 0, 0, this);
                                mIntVal = 2;
                            }
                            // The fourth callback draws the destination
                            else if (mIntVal == 2) {
                                mAttachedActor.remove(false);
                                // print a message and pause the game, via
                                // PauseScene
                                PauseScene.get().addText("The destination is\nnow available", 255, 255, 255,
                                        "arial.ttf", 32);
                                Destination.makeAsCircle(120, 20, 2, 2, "mustardball.png");
                            }

                        }
                    };
                    oo.setHeroCollisionCallback(1, 0, 0, 0, 0, sc2);
                }
            });
            o.setDisappearSound("hipitch.ogg");
        }

        /*
         * this level demonstrates callbacks that happen when we touch an
         * obstacle. Be sure to look at the onTouchCallback() method for more
         * details
         */
        else if (whichLevel == 64) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Activate and then \ntouch the obstacle", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // make a destination... notice that it needs a lot more goodies
            // than are on the screen...
            Destination d = Destination.makeAsCircle(29, 1, 2, 2, "mustardball.png");
            d.setActivationScore(3, 0, 0, 0);
            Score.setVictoryDestination(1);

            // draw an obstacle, make it a touch callback, and then draw the
            // goodie we need to get in order to activate the obstacle
            Obstacle o = Obstacle.makeAsCircle(10, 5, 3, 3, "purpleball.png");
            o.setPhysics(1, 0, 1);
            // we'll give this callback the id "39", just for fun
            o.setTouchCallback(1, 0, 0, 0, true, new LolCallback() {
                public void onEvent() {
                    // note: we could draw a picture of an open chest in the
                    // obstacle's place, or even use a disappear animation whose
                    // final frame looks like an open treasure chest.
                    mAttachedActor.remove(false);
                    for (int i = 0; i < 3; ++i)
                        Goodie.makeAsCircle(9 * i, 20 - i, 2, 2, "blueball.png");
                }
            });
            o.setDisappearSound("hipitch.ogg");

            Goodie g = Goodie.makeAsCircle(0, 30, 2, 2, "blueball.png");
            g.setDisappearSound("lowpitch.ogg");
        }

        /*
         * this level shows how to use enemy defeat callbacks. There are four
         * ways to defeat an enemy, so we enable all mechanisms in this level,
         * to see if they all work to cause enemy callbacks to run the
         * onEnemyCallback code. Another important point here is that the IDs
         * don't need to be unique for *any* callbacks. We can use the same ID
         * every time...
         */
        else if (whichLevel == 65) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // give the hero strength, so that we can use him to defeat an enemy
            // as a test of enemy callbacks
            Hero h = Hero.makeAsCircle(12, 12, 4, 4, "greenball.png");
            h.setStrength(3);
            h.setMoveByTilting();
            h.setInvincibleAnimation(new Animation("colorstar.png", 4, true).to(4, 100).to(5, 100).to(6, 100)
                    .to(7, 100));

            // a goodie, so we can do defeat by invincibility
            Goodie g1 = Goodie.makeAsCircle(20, 29, 2, 3, "purpleball.png");
            g1.setInvincibilityDuration(15);

            // enable throwing projectiles, so that we can test enemy callbacks
            // again
            h.setTouchToThrow(h, 4, 2, 30, 0);
            ProjectilePool.configure(100, 1, 1, "greyball.png", 1, 0, true);

            // add an obstacle that has an enemy collision callback, so it can
            // defeat enemies
            Obstacle o = Obstacle.makeAsCircle(30, 10, 5, 5, "blueball.png");
            o.setPhysics(1000, 0, 0);
            o.setCanDrag(false);
            o.setEnemyCollisionCallback(0, 0, 0, 0, 0, new LolCallback() {
                public void onEvent() {
                    if (mCollideActor.getInfoText().equals("weak")) {
                        ((Enemy) mCollideActor).defeat(true);
                    }
                }
            });

            // now draw our enemies... we need enough to be able to test that
            // all four defeat mechanisms work. Note that we attach defeat
            // callback code to each of them.
            LolCallback sc = new LolCallback() {
                public void onEvent() {
                    // always reset the pausescene, in case it has something on
                    // it from before...
                    PauseScene.get().reset();
                    PauseScene.get().addText("good job, here's a prize", 88, 226, 160, "arial.ttf", 16);
                    PauseScene.get().show();
                    // use random numbers to figure out where to draw a goodie
                    // as a reward... picking in the range 0-46,0-30 ensures
                    // that with width and height of 2, the goodie stays on
                    // screen
                    Goodie.makeAsCircle(Util.getRandom(46), Util.getRandom(30), 2, 2, "blueball.png");
                }
            };
            Enemy e1 = Enemy.makeAsCircle(5, 5, 1, 1, "redball.png");
            e1.setDefeatCallback(sc);

            Enemy e2 = Enemy.makeAsCircle(5, 5, 2, 2, "redball.png");
            e2.setDefeatCallback(sc);
            e2.setInfoText("weak");

            Enemy e3 = Enemy.makeAsCircle(40, 3, 1, 1, "redball.png");
            e3.setDefeatCallback(sc);

            Enemy e4 = Enemy.makeAsCircle(25, 25, 1, 1, "redball.png");
            e4.setDefeatCallback(sc);
            e4.setDisappearOnTouch();

            Enemy e5 = Enemy.makeAsCircle(25, 29, 1, 1, "redball.png");
            e5.setDefeatCallback(sc);

            // win by defeating enemies
            Score.setVictoryEnemyCount();
        }

        /*
         * This level shows that we can resize a hero on the fly, and change its
         * image. We use a collision callback to cause the effect. Furthermore,
         * we can increment scores inside of the callback code, which lets us
         * activate the destination on an obstacle collision
         */
        else if (whichLevel == 66) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Only stars can reach\nthe destination", 255, 255, 255, "arial.ttf", 20);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            Display.addGoodieCount(1, 0, " Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);

            // the destination won't work until some goodies are collected...
            Destination d = Destination.makeAsBox(46, 2, 2, 2, "colorstar.png");
            d.setActivationScore(4, 1, 3, 0);
            Score.setVictoryDestination(1);

            // Colliding with this star will make the hero into a star... see
            // onHeroCollideCallback for details
            Obstacle o = Obstacle.makeAsBox(30, 0, 3, 3, "stars.png");
            o.setPhysics(1, 0, 1);
            o.setHeroCollisionCallback(0, 0, 0, 0, 1, new LolCallback() {
                public void onEvent() {
                    // here's a simple way to increment a goodie count
                    Score.incrementGoodiesCollected2();
                    // here's a way to set a goodie count
                    Score.setGoodiesCollected3(3);
                    // here's a way to read and write a goodie count
                    Score.setGoodiesCollected1(4 + Score.getGoodiesCollected1());
                    // get rid of the star, so we know it's been used
                    mAttachedActor.remove(true);
                    // resize the hero, and change its image
                    mCollideActor.resize(mCollideActor.getXPosition(), mCollideActor.getYPosition(), 5, 5);
                    mCollideActor.setImage("stars.png", 0);
                }
            });
        }

        /*
         * This level shows how to use countdown timers to win a level, tests
         * some color features, and introduces a vector throw mechanism with
         * fixed velocity
         */
        else if (whichLevel == 67) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press anywhere\nto throw a ball", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Here's a simple pause button and pause scene
            PauseScene.get().addText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            Control.addPauseButton(0, 300, 20, 20, "red.png");

            // draw a hero, and a button for throwing projectiles in many
            // directions. Note that this is going to look like an "asteroids"
            // game, with a hero covering the bottom of the screen, so that
            // anything that falls to the bottom counts against the player
            Hero h = Hero.makeAsBox(1, 0, 46, 1, "greenball.png");
            Control.addDirectionalThrowButton(0, 0, 960, 640, "", h, 100, 0, 1);

            // set up our pool of projectiles, then set them to have a fixed
            // velocity when using the vector throw mechanism
            ProjectilePool.configure(100, 1, 1, "greyball.png", 1, 0, true);
            ProjectilePool.setRange(50);
            ProjectilePool.setFixedVectorThrowVelocity(5);

            // we're going to win by "surviving" for 25 seconds... with no
            // enemies, that shouldn't be too hard
            Display.addWinCountdown(25, 28, 250, "arial.ttf", 192, 192, 192, 16);
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
        else if (whichLevel == 68) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Flick the hero into the destination", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsBox(21, 23, 3, 3, "greenball.png");
            h.setHover(21, 23);
            h.setFlickable(0.7f);

            // place an enemy, let it fall
            Enemy e = Enemy.makeAsCircle(31, 25, 3, 3, "redball.png");
            e.setCanFall();

            Destination.makeAsCircle(25, 25, 5, 5, "mustardball.png");
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
        else if (whichLevel == 69) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press the hero to\nmake it jump", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.5f, 0, 0.6f);
            h.setMoveByTilting();
            h.setTouchToJump();
            h.setJumpImpulses(0, 15);
            Level.setCameraChase(h);

            // hero can jump while on this obstacle
            Obstacle.makeAsBox(10, 3, 10, 1, "red.png");

            // hero can't jump while on this obstacle
            Obstacle o = Obstacle.makeAsBox(40, 3, 10, 1, "red.png");
            o.setReJump(false);
        }

        /*
         * When something chases an entity, we might not want it to chase in
         * both the X and Y dimensions... this shows how we can chase in a
         * single direction.
         */
        else if (whichLevel == 70) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("You can walk through the wall", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setMoveByTilting();

            Destination.makeAsCircle(42, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // These obstacles chase the hero, but only in one dimension
            Obstacle e = Obstacle.makeAsCircle(0, 0, 1, 1, "red.png");
            e.setChaseSpeed(15, h, false, true);
            e.setCollisionsEnabled(true);
            Obstacle e2 = Obstacle.makeAsCircle(0, 0, 1, 1, "red.png");
            e2.setChaseSpeed(15, h, true, false);
            e2.setCollisionsEnabled(true);

            // Here's a wall, and a movable round obstacle
            Obstacle o = Obstacle.makeAsBox(40, 1, .5f, 20, "red.png");
            Obstacle o2 = Obstacle.makeAsCircle(8, 8, 2, 2, "blueball.png");
            o2.setMoveByTilting();

            // The hero can pass through this wall, because both have the same
            // passthrough value
            h.setPassThrough(7);
            o.setPassThrough(7);
        }

        /*
         * PokeToPlace is nice, but sometimes it's nicer to use Poke to cause
         * movement to the destination, instead of an immediate jump.
         */
        else if (whichLevel == 71) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            PreScene.get().addText("Poke the hero, then\n where you want it\nto go.", 255, 255, 255, "arial.ttf", 32);

            // This hero moves via poking. the "false" means that we don't have
            // to poke hero, poke location, poke hero, poke location, ...
            // Instead, we can poke hero, poke location, poke location. the
            // first "true" means that as we drag our finger, the hero will
            // change its direction of travel. The second "true" means the hero
            // will stop immediately when we release our finger.
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "stars.png");
            h.setDefaultAnimation(new Animation("stars.png", 200, true, 0, 0));
            h.setDefaultReverseAnimation(new Animation("stars_flipped.png", 200, true, 7, 7));
            h.setPokePath(4, false);

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // sometimes a control needs to have a large touchable area, but a
            // small image. One way to do it is to make an invisible control,
            // then put a picture on top of it. This next line shows how to draw
            // a picture on the HUD
            Control.addImage(40, 40, 40, 40, "red.png");
        }

        /*
         * It can be useful to make a Hero stick to an obstacle. As an example,
         * if the hero should stand on a platform that moves along a route, then
         * we will want the hero to "stick" to it, even as the platform moves
         * downward.
         */
        else if (whichLevel == 72) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press screen borders\nto move the hero", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            // give a little friction, to help the hero stick to platforms
            h.setPhysics(2, 0, .5f);

            // create a destination
            Destination.makeAsCircle(20, 15, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // This obstacle is sticky on top... Jump onto it and watch what
            // happens
            Obstacle o = Obstacle.makeAsBox(10, 5, 8, .5f, "red.png");
            o.setRoute(new Route(5).to(10, 5).to(5, 15).to(10, 25).to(15, 15).to(10, 5), 5, true);
            o.setPhysics(100, 0, .1f);
            o.setSticky(true, false, false, false);

            // This obstacle is not sticky... it's not nearly as much fun
            Obstacle o2 = Obstacle.makeAsBox(30, 5, 8, .5f, "red.png");
            o2.setRoute(new Route(5).to(30, 5).to(25, 15).to(30, 25).to(45, 15).to(30, 5), 5, true);
            o2.setPhysics(100, 0, 1f);

            // draw some buttons for moving the hero
            Control.addLeftButton(0, 100, 100, 440, "", 5, h);
            Control.addRightButton(860, 100, 100, 440, "", 5, h);
        }

        /*
         * When using "vector" projectiles, if the projectile isn't a circle we
         * might want to rotate it in the direction of travel. Also, this level
         * shows how to do walls that can be passed through in one direction.
         */
        else if (whichLevel == 73) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Press anywhere\nto shoot a laserbeam", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            Destination.makeAsCircle(42, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions. It
            // only covers half the screen, to show how such an effect would
            // behave
            Control.addDirectionalThrowButton(0, 0, 480, 640, "", h, 100, 0, 0);

            // set up a pool of projectiles with fixed velocity, and with
            // rotation
            ProjectilePool.configure(100, .1f, 3, "red.png", 1, 0, false);
            ProjectilePool.setFixedVectorThrowVelocity(10);
            ProjectilePool.setRotateVectorThrow();

            // create a box that is easy to fall into, but hard to get out of,
            // by making its sides each "one-sided"
            Obstacle bottom = Obstacle.makeAsBox(10, 10, 10, .2f, "red.png");
            bottom.setOneSided(2);
            Obstacle left = Obstacle.makeAsBox(10, 10, .2f, 10, "red.png");
            left.setOneSided(1);
            Obstacle right = Obstacle.makeAsBox(20, 10, .2f, 10, "red.png");
            right.setOneSided(3);
            Obstacle top = Obstacle.makeAsBox(10, 25, 10, .2f, "red.png");
            top.setOneSided(0);
        }

        /*
         * This level shows how to use multiple types of goodie scores
         */
        else if (whichLevel == 74) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Green, Red, and Grey\nballs are goodies", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setMoveByTilting();

            // the destination requires lots of goodies of different types
            Destination d = Destination.makeAsCircle(42, 31, 2, 2, "mustardball.png");
            d.setActivationScore(1, 1, 3, 0);
            Score.setVictoryDestination(1);

            Display.addGoodieCount(1, 0, " blue", 10, 110, "arial.ttf", 0, 255, 255, 16);
            Display.addGoodieCount(2, 0, " green", 10, 140, "arial.ttf", 0, 255, 255, 16);
            Display.addGoodieCount(3, 0, " red", 10, 170, "arial.ttf", 0, 255, 255, 16);

            Display.addCountdown(100, "", 250, 30);

            // draw the goodies
            for (int i = 0; i < 3; ++i) {
                Goodie b = Goodie.makeAsCircle(10 * i, 30, 2, 2, "blueball.png");
                b.setScore(1, 0, 0, 0);
                Goodie g = Goodie.makeAsCircle(10 * i + 2.5f, 30, 1, 1, "greenball.png");
                g.setScore(0, 1, 0, 0);
                Goodie r = Goodie.makeAsCircle(10 * i + 6, 30, 1, 1, "redball.png");
                r.setScore(0, 0, 1, 0);
            }

            // When the hero collides with this obstacle, we'll increase the
            // time remaining. See onHeroCollideCallback()
            Obstacle o = Obstacle.makeAsBox(40, 0, 5, 200, "red.png");
            o.setHeroCollisionCallback(1, 1, 1, 0, 0, new LolCallback() {
                public void onEvent() {
                    // add 15 seconds to the timer
                    Score.updateTimerExpiration(15);
                    mAttachedActor.remove(true);
                }
            });
        }

        /*
         * this level shows passthrough objects and chase again, to help
         * demonstrate how chase works
         */
        else if (whichLevel == 75) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("You can walk through the wall", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setMoveByTilting();
            h.setPassThrough(7); // make sure obstacle has same value

            // the destination requires lots of goodies of different types
            Destination.makeAsCircle(42, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // the enemy chases the hero, but can't get through the wall
            Enemy e = Enemy.makeAsCircle(42, 1, 5, 4, "red.png");
            e.setChaseSpeed(1, h, true, true);

            Obstacle o = Obstacle.makeAsBox(40, 1, .5f, 20, "red.png");
            o.setPassThrough(7);
        }

        /*
         * We can have a control that increases the hero's speed while pressed,
         * and decreases it upon release
         */
        else if (whichLevel == 76) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, 10);
            PreScene.get().addText("Press anywhere to speed up", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            Destination.makeAsCircle(120, 31, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Hero h = Hero.makeAsBox(2, 25, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            // give the hero a fixed velocity
            h.addVelocity(4, 0, false);
            // center the camera a little ahead of the hero
            h.setCameraOffset(15, 0);
            Level.setCameraChase(h);

            // set up the background
            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // draw a turbo boost button that covers the whole screen... make
            // sure its "up" speeds match the hero velocity
            Control.addTurboButton(0, 0, 960, 640, "", 15, 0, 4, 0, h);
        }

        /*
         * Sometimes, we want to make the hero move when we press a control, but
         * when we release we don't want an immediate stop. This shows how to
         * get that effect.
         */
        else if (whichLevel == 77) {
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press anywhere to start moving", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            h.setCameraOffset(15, 0);
            Level.setCameraChase(h);

            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0, 960, 640);

            // This control has a dampening effect, so that on release, the hero
            // slowly stops
            Control.addDampenedMotionButton(0, 0, 960, 640, "", 10, 0, 4, h);
        }

        /*
         * One-sided obstacles can be callback obstacles. This allows, among
         * other things, games like doodle jump. This level shows how it all
         * interacts.
         */
        else if (whichLevel == 78) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("One-sided + Callbacks", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();

            Destination.makeAsCircle(42, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a platform that we can jump through from above
            Obstacle platform = Obstacle.makeAsBox(10, 5, 10, .2f, "red.png");
            platform.setOneSided(2);
            // Set a callback, then re-enable the platform's collision effect.
            // Be sure to check onHeroCollideCallback
            platform.setHeroCollisionCallback(0, 0, 0, 0, 0, new LolCallback() {
                public void onEvent() {
                    mCollideActor.setAbsoluteVelocity(mCollideActor.getXVelocity(), 5, false);
                }
            });
            platform.setCollisionsEnabled(true);

            // make the z index of the platform -1, so that the hero (index 0)
            // will be drawn on top of the box, not under it
            platform.setZIndex(-1);
        }

        /*
         * This level fleshes out some more poke-to-move stuff. Now we'll say
         * that once a hero starts moving, the player must re-poke the hero
         * before it can be given a new destination. Also, the hero will keep
         * moving after the screen is released. We will also show the Fact
         * interface.
         */
        else if (whichLevel == 79) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            PreScene.get().addText("Poke the hero, then\n where you want it\nto go.", 255, 255, 255, "arial.ttf", 32);

            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            h.setFingerChase(4, false, true);

            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // A callback control is a way to run arbitrary code whenever the
            // control is pressed. This is something of a catch-all for any sort
            // of behavior we might want. See onControlPressCallback().
            Control.addCallbackControl(40, 40, 40, 40, "red.png", new LolCallback() {
                public void onEvent() {
                    PauseScene.get().reset();
                    PauseScene.get().addText("Current score " + Score.getGoodiesCollected1(), 255, 255, 255,
                            "arial.ttf", 20);
                    PauseScene.get().show();
                    Score.incrementGoodiesCollected1();
                }
            });

            Display.addLevelFact("level test", 240, 40, "arial.ttf", 0, 0, 0, 12, "-", ".");
            Display.addSessionFact("session test", 240, 80, "arial.ttf", 0, 0, 0, 12, "-", ".");
            Display.addGameFact("game test", 240, 120, "arial.ttf", 0, 0, 0, 12, "-", ".");
            Control.addCallbackControl(40, 90, 40, 40, "red.png", new LolCallback() {
                public void onEvent() {
                    Facts.putLevelFact("level test", 1 + Facts.getLevelFact("level test", -1));
                }
            });
            Control.addCallbackControl(40, 140, 40, 40, "red.png", new LolCallback() {
                public void onEvent() {
                    Facts.putSessionFact("session test", 1 + Facts.getSessionFact("session test", -1));
                }
            });
            Control.addCallbackControl(40, 190, 40, 40, "red.png", new LolCallback() {
                public void onEvent() {
                    Facts.putGameFact("game test", 1 + Facts.getGameFact("game test", -1));
                }
            });
        }

        /*
         * Sometimes we need to manually force an entity to be immune to
         * gravity.
         */
        else if (whichLevel == 80) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Testing Gravity Defy?", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();

            Destination d = Destination.makeAsCircle(42, 14, 2, 2, "mustardball.png");
            // note: it must not be immune to physics (third parameter true), or
            // it will pass through the bounding box, but we do want it to move
            // and not fall downward
            d.setAbsoluteVelocity(-2, 0, false);
            d.setGravityDefy();
            Score.setVictoryDestination(1);
        }

        /*
         * Test to show that we can have obstacles with a polygon shape
         */
        else if (whichLevel == 81) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Testing Polygons", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setMoveByTilting();

            Destination.makeAsCircle(42, 14, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a polygon obstacle
            Obstacle o = Obstacle.makeAsPolygon(10, 10, 2, 5, "blueball.png", -1, 2, -1, 0, 0, -3, 1, 0, 1, 1);
            o.setShrinkOverTime(1, 1, true);
        }

        /*
         * A place for playing with a side-scrolling platformer that has lots of
         * features
         */
        else if (whichLevel == 82) {
            // set up a standard side scroller with tilt:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.get().addText("Press the hero to\nmake it jump", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a simple jumping hero
            Hero h = Hero.makeAsBox(5, 0, 2, 6, "greenball.png");
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // This enemy can be defeated by jumping. Note that the hero's
            // bottom must be higher than the enemy's middle point, or the jump
            // won't defeat the enemy.
            Enemy e = Enemy.makeAsCircle(15, 0, 5, 5, "redball.png");
            e.setDefeatByJump();
        }

        /*
         * Demonstrate the ability to set up paddles that rotate back and forth
         */
        else if (whichLevel == 83) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Avoid revolving obstacles", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero h = Hero.makeAsCircle(5, 0, 2, 6, "greenball.png");
            h.setMoveByTilting();

            // Note: you must give density to the revolving part...
            Obstacle revolving = Obstacle.makeAsBox(20, 10, 2, 8, "red.png");
            revolving.setPhysics(1, 0, 0);
            Obstacle anchor = Obstacle.makeAsBox(20, 19, 2, 2, "blueball.png");

            revolving.setRevoluteJoint(anchor, 0, 0, 0, 6);
            revolving.setRevoluteJointLimits(1.7f, -1.7f);
            revolving.setRevoluteJointMotor(4, Float.POSITIVE_INFINITY);
            Destination.makeAsCircle(40, 30, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /*
         * Demonstrate panning to view more of the level
         */
        else if (whichLevel == 84) {
            // set up a big screen
            Level.configure(4 * 48, 2 * 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The star rotates in\nthe direction of movement", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 60, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // zoom buttons
            Control.addZoomOutButton(0, 0, 480, 640, "", 8);
            Control.addZoomInButton(480, 0, 480, 640, "", .25f);

            // turn on panning
            Control.addPanControl(0, 0, 960, 640, "");
        }

        /*
         * Demonstrate pinch-to-zoom, and also demonstrate one-time callback
         * controls
         */
        else if (whichLevel == 85) {
            // set up a big screen
            Level.configure(4 * 48, 2 * 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("The star rotates in\nthe direction of movement", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 60, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // turn on pinch zoomg
            Control.addPinchZoomControl(0, 0, 960, 640, "", 8, .25f);

            // add a one-time callback control
            Control.addOneTimeCallbackControl(40, 40, 40, 40, "blueball.png", "greenball.png", new LolCallback() {
                public void onEvent() {
                    PauseScene.get().addText("you can only pause once...", 255, 255, 255, "arial.ttf", 20);
                    PauseScene.get().show();
                }
            });
        }

        /*
         * Demonstrate some advanced controls
         */
        else if (whichLevel == 86) {
            // set up a screen
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 30, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            Level.setCameraChase(h);
            h.setDamping(1);
            h.setAngularDamping(1);
            // when the hero stops, we'll run code that turns the hero red
            h.setStopCallback(new LolCallback() {
                public void onEvent() {
                    // NB: the setStopCallback call sets the callback's
                    // attachedSprite to the hero.
                    mAttachedActor.setImage("red.png", 0);
                }
            });

            // add some new controls for setting the rotation of the hero and
            // making the hero move based on a speed
            LolCallback rotatorSC = new LolCallback() {
                public void onEvent() {
                    // rotator... save the rotation and rotate the hero
                    mAttachedActor.setRotation(mFloatVal * (float) Math.PI / 180);
                    // multiply float val by 100 to preserve some decimal places
                    Facts.putLevelFact("rotation", (int) (100 * mFloatVal));
                }
            };
            rotatorSC.mAttachedActor = h;
            Control.addRotator(215, 135, 50, 50, "stars.png", 2, rotatorSC);
            LolCallback barSC = new LolCallback() {
                public void onEvent() {
                    // vertical bar... make the entity move
                    int rotation = Facts.getLevelFact("rotation", 0) / 100;
                    // create a unit vector
                    Vector2 v = new Vector2(1, 0);
                    v.scl(mFloatVal);
                    v.rotate(rotation + 90);
                    mAttachedActor.setDamping(2f);
                    mAttachedActor.setAbsoluteVelocity(v.x, v.y, false);
                }
            };
            barSC.mAttachedActor = h;
            Control.addVerticalBar(470, 0, 10, 320, "greenball.png", barSC);
        }

        /*
         * Weld joints
         */
        else if (whichLevel == 87) {
            // set up a screen
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 30, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero and fuse an obstacle to it
            Hero h = Hero.makeAsCircle(4, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Obstacle o = Obstacle.makeAsCircle(1, 1, 1, 1, "blueball.png");
            o.setCanFall();
            h.setWeldJoint(o, 3, 0, 0, 0, 45);
        }

        /*
         * Demonstrate that we can have callback buttons on PauseScenes
         */
        else if (whichLevel == 88) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.get().addText("Interactive Pause Scenes\n(click the red square)", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // Demonstrate the ability to chase while keeping existing velocity
            // in one direction
            Obstacle o = Obstacle.makeAsCircle(15, 15, 2, 2, "purpleball.png");
            o.setAbsoluteVelocity(5, 1, false);
            o.setChaseFixedMagnitude(h, 3, 0, false, true);

            // Create a pause scene that has a back button on it, and a button
            // for pausing the level
            PauseScene.get().addText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            PauseScene.get().addBackButton("red.png", 0, 600, 40, 40);
            PauseScene.get().addCallbackButton(10, 10, 20, 20, new LolCallback() {
                public void onEvent() {
                    Score.winLevel();
                }
            });
            PauseScene.get().addCallbackButton(190, 190, 20, 20, new LolCallback() {
                public void onEvent() {
                    Score.loseLevel();
                }
            });
            PauseScene.get().suppressClearClick();
            Control.addPauseButton(0, 300, 20, 20, "red.png");
        }

        /*
         * Use multiple heroes to combine positive and negative results
         */
        else if (whichLevel == 89) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // now let's draw two heroes who can both move by tilting, and
            // who both have density and friction. Note that we lower the
            // density, so they move faster
            Hero h1 = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            h1.setJumpImpulses(0, 10);
            h1.setTouchToJump();
            h1.setMustSurvive();
            Hero h2 = Hero.makeAsBox(0, 0, 48, .1f, "");
            h2.setMustSurvive();
            h1.setPassThrough(1);
            h2.setPassThrough(1);

            Enemy e1 = Enemy.makeAsCircle(29, 29, 1, 1, "redball.png");
            e1.setAbsoluteVelocity(0, -1, true);

            // notice that now we will make two destinations, each of which
            // defaults to only holding ONE hero, but we still need to get two
            // heroes to destinations in order to complete the level
            Destination.makeAsCircle(29, 6, 2, 2, "mustardball.png");
        }

        /*
         * Demonstrate that we can save entities so that we can access them from
         * a callback
         */
        else if (whichLevel == 90) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 5);
            PreScene.get().addText("Keep pressing until\na hero makes it to\nthe destination", 255, 255, 255,
                    "arial.ttf", 32);

            for (int i = 0; i < 10; ++i) {
                Hero h = Hero.makeAsBox(4 * i + 2, 0.1f, 2, 2, "greenball.png");
                h.setPhysics(1, 1, 5);
                Facts.putLevelActor("" + i, h);
            }

            Destination.makeAsCircle(29, 16, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // A callback control is a way to run arbitrary code whenever the
            // control is pressed. This is something of a catch-all for any sort
            // of behavior we might want. See onControlPressCallback().
            Control.addCallbackControl(0, 0, 960, 640, "", new LolCallback() {
                public void onEvent() {
                    for (int i = 0; i < 10; ++i) {
                        Actor p = Facts.getLevelActor("" + i);
                        p.setAbsoluteVelocity(5 - Util.getRandom(10), 10, false);
                    }
                }
            });
        }

        /**
         * Demo a truck, using distance and revolute joints
         */
        else if (whichLevel == 91) {
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero truck = Hero.makeAsBox(3, 3, 4, 1.5f, "red.png");
            truck.setPhysics(1, 0, 0);
            Obstacle head = Obstacle.makeAsCircle(4.5f, 4, 1, 1, "blueball.png");
            head.setPhysics(1, 0, 0);
            Obstacle backWheel = Obstacle.makeAsCircle(3, 2, 1.5f, 1.5f, "blueball.png");
            backWheel.setPhysics(3, 0, 1);
            Obstacle frontWheel = Obstacle.makeAsCircle(5.5f, 2, 1.5f, 1.5f, "blueball.png");
            frontWheel.setPhysics(3, 0, 1);

            backWheel.setRevoluteJoint(truck, -1.5f, -1, 0, 0);
            backWheel.setRevoluteJointMotor(-10f, 10f);
            frontWheel.setRevoluteJoint(truck, 1.5f, -1, 0, 0);
            frontWheel.setRevoluteJointMotor(-10f, 10f);

            // this is not how we want the head to look, but it makes for a nice
            // demo
            head.setDistanceJoint(truck, 0, 1, 0, 0);

            Destination.makeAsBox(47, 0, .1f, 32, "");
            Score.setVictoryDestination(1);
        }

        /**
         * Demonstrate how we can chain pausescenes together, and also show how to use particle
         * effects
         */
        else if (whichLevel == 92) {
            // start with a basic tilt-based side-scroller
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);

            // put some flame effects on a black background
            Background.setColor(0, 0, 0);
            for (int i = 5; i < 150; i += 15) {
                Effect e = Effect.makeParticleSystem("flame.txt", -2, i, 5);
                e.setRepeat(true);
            }

            // here's a weak attempt at snow
            Effect e = Effect.makeParticleSystem("snow.txt", 2, 15, 40);
            e.setRepeat(true);
            e = Effect.makeParticleSystem("snow.txt", 2, 55, 40);
            e.setRepeat(true);
            e = Effect.makeParticleSystem("snow.txt", 2, 85, 40);
            e.setRepeat(true);
            // the trick for getting one PauseScene's dismissal to result in another PauseScene
            // drawing right away is to use the PauseScene CallbackButton facility.  When the first
            // PauseScene is touched, we dismiss it and immediately draw another PauseScene

            // set up a simple PauseScene
            PauseScene.get().reset();
            PauseScene.get().addText("test", 255, 255, 255, "arial.ttf", 32);
            // this is the code to run when the *second* pausescene is touched.  Making it "final"
            // means that we can refer to it inside of the other callback
            final LolCallback sc2 = new LolCallback() {
                public void onEvent() {
                    PauseScene.get().dismiss();
                }
            };
            // this is the code to run when the *first* pausescene is touched
            LolCallback sc1 = new LolCallback() {
                public void onEvent() {
                    // clear the pausescene, draw another one
                    PauseScene.get().dismiss();
                    PauseScene.get().reset();
                    PauseScene.get().addText("test2", 255, 255, 255, "arial.ttf", 32);
                    PauseScene.get().addCallbackButton(0, 0, 960, 640, sc2);
                    PauseScene.get().show();
                }
            };
            // set the callback for the first pausescene, and show it
            PauseScene.get().addCallbackButton(0, 0, 960, 640, sc1);
            PauseScene.get().show();

            Control.addZoomOutButton(0, 0, 480, 640, "", 8);
            Control.addZoomInButton(480, 0, 480, 640, "", .25f);
        }

        // Show how to make an "infinite" level, and add a foreground layer
        else if (whichLevel == 93) {
            // set up a standard side scroller with tilt, but make it really really long:
            Level.configure(300000, 32);
            Physics.configure(0, -10);
            PreScene.get().addText("Press to make\nthe hero go up", 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 300000, 32, "red.png", 0, 0, 0);

            // make a hero
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            Level.setCameraChase(h);
            h.setAbsoluteVelocity(10, 0, false);
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);

            // touching the screen makes the hero go upwards
            Control.addUpButton(0, 0, 960, 640, "", 20, h);

            // set up our background, with a few layers
            Background.setColor(23, 180, 255);
            Background.addHorizontalLayer(0, 1, "back.png", 0, 960, 640);
            Foreground.addHorizontalLayer(.5f, 1, "mid.png", 0, 480, 320);
            Background.addHorizontalLayer(1.25f, 1, "front.png", 20, 454, 80);

            // we win by collecting 10 goodies...
            Score.setVictoryGoodies(10, 0, 0, 0);
            Display.addGoodieCount(1, 0, " goodies", 15, 600, "arial.ttf", 255, 255, 255, 20);

            // now set up an obstacle and attach a callback to it
            //
            // Note that the obstacle needs to be final or we can't access it within the callback
            final Obstacle trigger = Obstacle.makeAsBox(30, 0, 1, 32, "");
            LolCallback lc = new LolCallback(){
                /**
                 * Each time the hero hits the obstacle, we'll run this code to draw a new enemy
                 * and a new obstacle on the screen.  We'll randomize their placement just a bit.
                 * Also move the obstacle forward, so we can hit it again.
                 */
                public void onEvent() {
                    // make a random enemy and a random goodie.  Put them in X coordinates relative to the trigger
                    Enemy.makeAsCircle(trigger.getXPosition() + 40 + Util.getRandom(10), Util.getRandom(30), 2, 2, "redball.png");
                    Goodie.makeAsCircle(trigger.getXPosition() + 50 + Util.getRandom(10), Util.getRandom(30), 2, 2, "blueball.png");
                    // move the trigger so we can hit it again
                    trigger.setPosition(trigger.getXPosition() + 50, trigger.getYPosition());
                }
            };
            trigger.setHeroCollisionCallback(0,0,0,0,0,lc);
            // No transfer of momeuntum when the hero collides with the trigger
            trigger.setCollisionsEnabled(false);
        }
    }
}
