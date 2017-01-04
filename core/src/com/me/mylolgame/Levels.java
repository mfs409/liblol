/**
 * This is free and unencumbered software released into the public domain.
 * <p/>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p/>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * For more information, please refer to <http://unlicense.org>
 */

package com.me.mylolgame;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;

import java.util.ArrayList;

import edu.lehigh.cse.lol.CollisionCallback;
import edu.lehigh.cse.lol.Destination;
import edu.lehigh.cse.lol.Effect;
import edu.lehigh.cse.lol.Enemy;
import edu.lehigh.cse.lol.Goodie;
import edu.lehigh.cse.lol.Hero;
import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.LolAction;
import edu.lehigh.cse.lol.LolActorEvent;
import edu.lehigh.cse.lol.Obstacle;
import edu.lehigh.cse.lol.Route;
import edu.lehigh.cse.lol.SceneActor;
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.TouchEventHandler;
import edu.lehigh.cse.lol.WorldActor;

/**
 * Levels is where all of the code goes for describing the different levels of
 * the game. If you know how to create methods and classes, you're free to make
 * the big "if" statement in this code simply call to your classes and methods.
 * Otherwise, put your code directly into the parts of the "if" statement.
 */
class Levels implements ScreenManager {

    /**
     * We currently have 94 levels, each of which is described in part of the
     * following function.
     */
    public void display(int index, final Level level) {
        /*
         * In this level, all we have is a hero (the green ball) who needs to
         * make it to the destination (a mustard colored ball). The game is
         * configured to use tilt to control the level.
         */
        if (index == 1) {
            // set the screen to 48 meters wide by 32 meters high... this is
            // important, because Config.java says the screen is 480x320, and
            // LOL likes a 20:1 pixel to meter ratio. If we went smaller than
            // 48x32, things would getLoseScene really weird. And, of course, if you make
            // your screen resolution higher in Config.java, these numbers would
            // need to getLoseScene bigger.
            //
            // level.configureGravity MUST BE THE FIRST LINE WHEN DRAWING A LEVEL!!!

            // there is no default gravitational force


            // in this level, we'll use tilt to move some things around. The
            // maximum force that tilt can exert on anything is +/- 10 in the X
            // dimension, and +/- 10 in the Y dimension
            level.enableTilt(10, 10);

            // now let's create a hero, and indicate that the hero can move by
            // tilting the phone. "greenball.png" must be registered in
            // the registerMedia() method, which is also in this file. It must
            // also be in your android game's assets folder.
            Hero h = level.makeHeroAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();

            // draw a circular destination, and indicate that the level is won
            // when the hero reaches the level. "mustardball.png" must be
            // registered in registerMedia()
            level.makeDestinationAsCircle(29, 26, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
        }

        /*
         * In this level, we make the play a bit smoother by adding a bounding
         * box and changing the way that LibLOL interacts with the player
         */
        else if (index == 2) {
            // start by setting everything up just like in level 1


            level.enableTilt(10, 10);
            Hero h = level.makeHeroAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 26, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // add a bounding box so the hero can't fall off the screen
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // change the text that we display when the level is won
            level.getWinScene().setDefaultText("Good job!");

            // add a pop-up message that shows for one second at the
            // beginning of the level. The '50, 50' indicates the bottom left
            // corner of the text we display. 255,255,255 represents the red,
            // green, and blue components of the text color (the color will be
            // white). We'll write our text in the Arial font, with a size of 32
            // pt. The "\n" in the middle of the text causes a line break. Note
            // that "arial.ttf" must be in your android game's assets folder.
            level.getPreScene().addText(50 / 20f, 50 / 20f, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Reach the destination\nto win this level."), 0);
        }

        /*
         * In this level, we change the physics from level 2 so that things roll
         * and bounce a little bit more nicely.
         */
        else if (index == 3) {
            // These lines should be familiar after the last two levels


            level.enableTilt(10, 10);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // give the hero some density and friction, so that it can roll when
            // it encounters a wall... notice that once it has density, it has
            // mass, and it moves a lot slower...
            h.setPhysics(1, 0, 0.6f);

            // the bounding box now also has nonzero density, elasticity, and
            // friction... you should check out what happens if the friction
            // stays at 0.
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Let's draw our message in the center of the screen this time
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Reach the destination\nto win this level."), 0);
            // And let's say that instead of touching the message to make it go
            // away, we'll have it go away automatically after 2 seconds
            level.getPreScene().setExpire(2);
            // Note that we're going back to the default PostScene text...
        }

        /*
         * It's confusing to have multiple heroes in a level, but we can... this
         * shows how to have multiple destinations and heroes
         */
        else if (index == 4) {
            // standard stuff...


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // now let's draw two heroes who can both move by tilting, and
            // who both have density and friction. Note that we lower the
            // density, so they move faster
            Hero h1 = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            Hero h2 = level.makeHeroAsCircle(14, 7, 3, 3, "greenball.png");
            h2.setPhysics(.1f, 0, 0.6f);
            h2.setMoveByTilting();

            // notice that now we will make two destinations, each of which
            // defaults to only holding ONE hero, but we still need to getLoseScene two
            // heroes to destinations in order to complete the level
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.makeDestinationAsCircle(29, 26, 2, 2, "mustardball.png");
            level.setVictoryDestination(2);

            // Let's show msg1.png instead of text. Note that we had to
            // register it in registerMedia(), and that we're stretching it
            // slightly, since its dimensions are 460x320
            level.getPreScene().makePicture(0, 0, 960 / 20f, 640 / 20f, "msg1.png", 0);
        }

        /*
         * This level demonstrates that we can have many heroes that can reach
         * the same level. It also shows our first sound effect
         */
        else if (index == 5) {
            // standard stuff...


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h1 = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            Hero h2 = level.makeHeroAsCircle(14, 7, 3, 3, "greenball.png");
            h2.setPhysics(.1f, 0, 0.6f);
            h2.setMoveByTilting();
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("All heroes must\nreach the destination"), 0);

            // now let's make a destination, but indicate that it can hold TWO
            // heroes
            Destination d = level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setHeroCount(2);

            // let's also say that whenever a hero reaches the destination, a
            // sound will play
            d.setArrivalSound("hipitch.ogg");

            // Notice that this line didn't change from level 4
            level.setVictoryDestination(2);
        }

        /*
         * Tilt can be used to control velocity, instead of applying forces to
         * the entities on the screen. It doesn't always work well, but it's a
         * nice option to have...
         */
        else if (index == 6) {
            // standard stuff...


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("A different way\nto use tilt."), 0);

            // change the behavior or tilt
            level.setTiltAsVelocity(true);
        }

        /*
         * This level adds an enemy, to demonstrate that we can make it possible
         * to lose a level
         */
        else if (index == 7) {
            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            // Notice that we changed the font size and color
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#19FFFF", 20, "", "", level.DisplayFixedText("Avoid the enemy and\nreach the destination"), 0);

            // draw an enemy... we don't need to give it physics for now...
            level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");

            // turn off the win and lose scenes... whether the player wins or
            // loses, we'll just start the appropriate level. Be sure to test
            // the game by losing *and* winning!
            level.getWinScene().disable();
            level.getLoseScene().disable();
        }

        /*
         * This level explores a bit more of what we can do with enemies, by
         * having an enemy with a fixed path.
         */
        else if (index == 8) {
            // configureGravity a basic level, just like the start of level 2:


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 27, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("Avoid the enemy and\nreach the destination"), 0);

            // put some extra text on the level.getLoseScene()
            level.getPreScene().addText(5 / 20f, 5 / 20f, "arial.ttf", "#32C87A", 10, "", "", level.DisplayFixedText("(the enemy is red)"), 0);

            // draw an enemy
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");

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
        else if (index == 9) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.getPreScene().addText(50 / 20f, 50 / 20f, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("Avoid the enemy and\nreach the destination"), 0);

            // draw an enemy that can move
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            // This time, we add a third point, which is the same as the
            // starting point. This will give us a nicer sort of movement. Also
            // note the diagonal movement.
            e.setRoute(new Route(3).to(25, 25).to(12, 2).to(25, 25), 2, true);
            // note that any number of points is possible... you could have
            // extremely complex Routes!
        }

        /*
         * We can make enemies move via tilt. We can also configureGravity some other
         * kinds of sounds
         */
        else if (index == 10) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(0.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.getPreScene().makePicture(0, 0, 960 / 20f, 640 / 20f, "msg2.png", 0);

            // let's make the destination rotate:
            Destination d = level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setRotationSpeed(1);
            level.setVictoryDestination(1);

            // draw an enemy who moves via tilt
            Enemy e3 = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(1.0f, 0.3f, 0.6f);
            e3.setMoveByTilting();

            // configureGravity some sounds to play on win and lose. Of course, all
            // these sounds must be registered!
            level.getWinScene().setSound("winsound.ogg");
            level.getLoseScene().setSound("losesound.ogg");

            // set background music
            level.setMusic("tune.ogg");

            // custom text for when the level is lost
            level.getLoseScene().setDefaultText("Better luck next time...");
        }

        /*
         * This shows that it is possible to make a level that is larger than a
         * screen. It also shows that there is a "heads up display" that can be
         * used for providing information and touchable controls
         */
        else if (index == 11) {
            // make the level really big
            level.setCameraBounds(400, 300);

            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 400, 300, "red.png", 0, 0, 0);

            // put the hero and destination far apart
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(329, 281, 10, 10, "mustardball.png");
            level.setVictoryDestination(1);

            // We want to be sure that no matter what, the player can see the
            // level. We achieve this by having the camera follow the hero:
            level.setCameraChase(h);

            // add zoom buttons. We are using blank images, which means that the
            // buttons will be invisible... that's nice, because we can make the
            // buttons big (covering the left and right halves of the screen).
            // When debug rendering is turned on, we'll be able to see a red
            // outline of the two rectangles. You could also use images (that
            // you registered, of course), but if you did, you'd either need to
            // make them small, or make them semi-transparent.
            level.addTapControl(0, 0, 480, 640, "", level.ZoomOutAction(8));
            level.addTapControl(480, 0, 480, 640, "", level.ZoomInAction(.25f));

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press left to zoom out\nright to zoom in"), 0);
        }

        /*
         * this level introduces obstacles, and also shows the difference
         * between "box" and "circle" physics
         */
        else if (index == 12) {
            // configureGravity a basic level


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // add a hero and destination
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // let's draw an obstacle whose underlying shape is a box, but whose
            // picture is a circle. This can be odd... our hero can roll around
            // an invisible corner on this obstacle. When debug rendering is
            // turned on (in Config.java), you'll be able to see the true shape
            // of the obstacle.
            Obstacle o1 = level.makeObstacleAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(1, 0, 1);

            // now let's draw an obstacle whose shape and picture are both
            // circles. The hero rolls around this nicely.
            Obstacle o2 = level.makeObstacleAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(1, 0, 1);

            // draw a wall using circle physics and a stretched rectangular
            // picture. This wall will do really funny things
            Obstacle o3 = level.makeObstacleAsCircle(20, 25, 6, 0.5f, "red.png");
            o3.setPhysics(1, 0, 1);

            // draw a rectangular wall the right way, as a box
            Obstacle o4 = level.makeObstacleAsBox(34, 2, 0.5f, 20, "red.png");
            o4.setPhysics(1, 0, 1);

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("An obstacle's appearance may\nnot match its physics"), 0);
        }

        /*
         * this level just plays around with physics a little bit, to show how
         * friction and elasticity can do interesting things.
         */
        else if (index == 13) {
            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("These obstacles have\ndifferent physics\nparameters"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            // Colliding the hero with these obstacles can have interesting
            // effects
            Obstacle o1 = level.makeObstacleAsCircle(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(0, 100, 0);
            Obstacle o2 = level.makeObstacleAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(10, 0, 100);
        }

        /*
         * This level introduces goodies. Goodies are something that we collect.
         * We can make the collection of goodies lead to changes in the behavior
         * of the game, and in this example, the collection of goodies "enables"
         * a level.
         */
        else if (index == 14) {
            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("You must collect\ntwo blue balls"), 0);

            // Add some stationary goodies. Note that the default is
            // for goodies to not cause a change in the hero's behavior at the
            // time when a collision occurs... this is often called being a
            // "sensor"... it means that collisions are still detected by the
            // code, but they don't cause changes in momentum
            //
            // Note that LibLOL allows goodies to have one of 4 "types". By
            // default, collecting a goodie increases the "type 1" score by 1.
            level.makeGoodieAsCircle(0, 30, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(0, 15, 2, 2, "blueball.png");

            // here we create a level. Note that we now set its activation
            // score to 2, so that you must collect two goodies before the
            // destination will "work"
            Destination d = level.makeDestinationAsCircle(29, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            // we must provide an activation score for each of the 4 types of
            // goodies
            d.setActivationScore(2, 0, 0, 0);

            // let's put a display on the screen to see how many type-1 goodies
            // we've collected. Since the second parameter is "2", we'll display
            // the count as "X/2 Goodies" instead of "X Goodies"
            level.addDisplay(220, 280, "arial.ttf", "#FF00FF", 20, "", "/2 Goodies", level.DisplayGoodies1, 2);
        }

        /*
         * earlier, we saw that enemies could move along a Route. So can any
         * other entity, so we'll move destinations, goodies, and obstacles,
         * too.
         */
        else if (index == 15) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Every entity can move..."), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(44, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // make a destination that moves, and that requires one goodie to be
            // collected before it works
            Destination d = level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            d.setActivationScore(1, 0, 0, 0);
            d.setRoute(new Route(3).to(29, 6).to(29, 26).to(29, 6), 4, true);
            level.setVictoryDestination(1);

            // make an obstacle that moves
            Obstacle o = level.makeObstacleAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setRoute(new Route(3).to(0, 0).to(10, 10).to(0, 0), 2, true);

            // make a goodie that moves
            Goodie g = level.makeGoodieAsCircle(5, 5, 2, 2, "blueball.png");
            g.setRoute(new Route(5).to(5, 5).to(5, 25).to(25, 25).to(9, 9).to(5, 5), 10, true);

            // draw a goodie counter in light blue (60, 70, 255) with a 12-point
            // font
            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Goodies", level.DisplayGoodies1, 2);
        }

        /*
         * Sometimes, we don't want a destination, we just want to say that the
         * player wins by collecting enough goodies. This level also shows that
         * we can set a time limit for the level, and we can pause the game.
         */
        else if (index == 16) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Collect all\nblue balls\nto win"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 20, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw 5 goodies
            level.makeGoodieAsCircle(.5f, .5f, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(5.5f, 1.5f, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(10.5f, 2.5f, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(15.5f, 3.5f, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(20.5f, 4.5f, 2, 2, "blueball.png");

            // indicate that we win by collecting enough goodies
            level.setVictoryGoodies(5, 0, 0, 0);

            // put the goodie count on the screen
            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", "/5 Goodies", level.DisplayGoodies1, 2);

            // put a simple countdown on the screen
            level.setLoseCountdown(15, "Time Up!");
            level.addDisplay(400, 50, "arial.ttf", "#000000", 32, "", "", level.DisplayLoseCountdown, 2);

            // let's also add a screen for pausing the game. In a real game,
            // every level should have a button for pausing the game, and the
            // pause scene should have a button for going back to the main
            // menu... we'll show how to do that later.
            level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Game Paused"), 0);
            level.addTapControl(0, 300, 20, 20, "red.png", level.PauseAction);
        }

        /*
         * This level shows how "obstacles" need not actually impede the hero's
         * movement. Here, we attach "damping factors" to the hero, which let us
         * make the hero speed up or slow down based on interaction with the
         * obstacle. This level also adds a stopwatch. Stopwatches don't have
         * any meaning, but they are nice to have anyway...
         */
        else if (index == 17) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Obstacles as zoom\nstrips, friction pads\nand repellers"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // add a stopwatch... note that there are two ways to add a
            // stopwatch, the other of which allows for configuring the font
            level.setStopwatch(0);
            level.addDisplay(50, 50, "arial.ttf", "#000000", 32, "", "", level.DisplayStopwatch, 2);

            // Create a pause scene that has a back button on it, and a button
            // for pausing the level. Note that the background image must come
            // first
            level.getPauseScene().makePicture(0, 0, 960 / 20f, 640 / 20f, "fade.png", 0);
            level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Game Paused"), 0);
            level.getPauseScene().addTapControl(0, 15, 1, 1, "greyball.png", new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    level.doBack();
                    return true;
                }
            }, 0);
            level.addTapControl(0, 15, 1, 1, "red.png", level.PauseAction);

            // now draw three obstacles. Note that they have different dampening
            // factors. one important thing to notice is that since we place
            // these on the screen *after* we place the hero on the screen, the
            // hero will go *under* these things.

            // this obstacle's dampening factor means that on collision, the
            // hero's velocity is multiplied by -1... he bounces off at an
            // angle.
            Obstacle o = level.makeObstacleAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPad(-1);

            // this obstacle accelerates the level... it's like a turbo booster
            o = level.makeObstacleAsCircle(20, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPad(5);

            // this obstacle slows the hero down... it's like running on
            // sandpaper. Note that the hero only slows down on initial
            // collision, not while going under it.
            o = level.makeObstacleAsBox(30, 10, 3.5f, 3.5f, "purpleball.png");
            o.setRotationSpeed(2);
            o.setPad(0.2f);
        }

        /*
         * This level shows that it is possible to give heroes and enemies
         * different strengths, so that a hero doesn't disappear after a single
         * collision. It also shows that when an enemy defeats a hero, we can
         * customize the message that prints
         */
        else if (index == 18) {
            // set up a basic level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The hero can defeat \nup to two enemies..."), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // draw a hero and give it strength of 10. The default is for
            // enemies to have "2" units of damage, and heroes to have "1" unit
            // of strength, so that any collision defeats the hero without
            // removing the enemy.
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setStrength(10);

            // draw a strength meter to show this hero's strength
            level.addDisplay(220, 280, "arial.ttf", "#000000", 32, "", " Strength", level.DisplayStrength(h), 2);

            // our first enemy stands still:
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            // this text will be displayed if this enemy defeats the hero
            e.setDefeatHeroText("How did you hit me?");

            // our second enemy moves along a path
            e = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 10, true);
            e.setDamage(4);
            e.setDefeatHeroText("Stay out of my way");

            // our third enemy moves with tilt, which makes it hardest to avoid
            e = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
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
        else if (index == 19) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("You have 10 seconds\nto defeat the enemies"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // give the hero enough strength that this will work...
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setStrength(10);
            h.setMoveByTilting();

            // draw a few enemies, and change their "damage" (the amount by
            // which they decrease the hero's strength)
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            e = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(.1f, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(4);

            // put a countdown on the screen
            level.setLoseCountdown(10, "Time Up!");
            level.addDisplay(200, 25, "arial.ttf", "#000000", 32, "", "", level.DisplayLoseCountdown, 2);

            // indicate that defeating all of the enemies is the way to win this
            // level
            level.setVictoryEnemyCount();
        }

        /*
         * This level shows that a goodie can change the hero's strength, and
         * that we can win by defeating a specific number of enemies, instead of
         * all enemies.
         */
        else if (index == 20) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Collect blue balls\nto increse strength"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // our default hero only has "1" strength
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // our default enemy has "2" damage
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDisappearSound("slowdown.ogg");

            // a second enemy
            e = level.makeEnemyAsCircle(35, 15, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);

            // this goodie gives an extra "5" strength:
            Goodie g = level.makeGoodieAsCircle(0, 30, 2, 2, "blueball.png");
            g.setStrengthBoost(5);
            g.setDisappearSound("woowoowoo.ogg");

            // Display the hero's strength
            level.addDisplay(220, 280, "arial.ttf", "#000000", 32, "", " Strength", level.DisplayStrength(h), 2);

            // win by defeating one enemy
            level.setVictoryEnemyCount(1);
            level.getWinScene().setDefaultText("Good enough...");
        }

        /*
         * this level introduces the idea of invincibility. Collecting the
         * goodie makes the hero invincible for a little while...
         */
        else if (index == 21) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The blue ball will\nmake you invincible\nfor 15 seconds"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw a few enemies, and make them rotate
            for (int i = 0; i < 5; ++i) {
                Enemy e = level.makeEnemyAsCircle(5 * i + 1, 25, 2, 2, "redball.png");
                e.setPhysics(1.0f, 0.3f, 0.6f);
                e.setRotationSpeed(1);
            }

            // this goodie makes us invincible
            Goodie g = level.makeGoodieAsCircle(30, 30, 2, 2, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);

            // we'll still say you win by reaching the level. Defeating
            // enemies is just for fun...
            level.makeDestinationAsCircle(29, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // display a goodie count for type-1 goodies
            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Goodies", level.DisplayGoodies1, 2);

            // put a frames-per-second display on the screen. This is going to
            // look funny, because when debug mode is set (in Config.java), a
            // FPS will be shown on every screen anyway
            level.addDisplay(400, 15, "arial.ttf", "#C8C864", 12, "", " fps", level.DisplayFPS, 2);
        }

        /*
         * Some goodies can "count" for more than one point... they can even
         * count for negative points.
         */
        else if (index == 22) {
            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Collect 'the right' \nblue balls to\nactivate destination"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination d = level.makeDestinationAsCircle(29, 1, 2, 2, "mustardball.png");
            d.setActivationScore(7, 0, 0, 0);
            level.setVictoryDestination(1);

            // create some goodies with special scores. Note that we're still
            // only dealing with type-1 scores
            Goodie g1 = level.makeGoodieAsCircle(0, 30, 2, 2, "blueball.png");
            g1.setScore(-2, 0, 0, 0);
            Goodie g2 = level.makeGoodieAsCircle(0, 15, 2, 2, "blueball.png");
            g2.setScore(7, 0, 0, 0);

            // create some regular goodies
            level.makeGoodieAsCircle(30, 30, 2, 2, "blueball.png");
            level.makeGoodieAsCircle(35, 30, 2, 2, "blueball.png");

            // print a goodie count to show how the count goes up and down
            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Progress", level.DisplayGoodies1, 2);
        }

        /*
         * this level demonstrates that we can drag entities (in this case,
         * obstacles), and that we can make rotated obstacles. The latter could
         * be useful for having angled walls in a maze
         */
        else if (index == 23) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Rotating oblong obstacles\nand draggable obstacles"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // draw obstacles that we can drag
            Obstacle o = level.makeObstacleAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setCanDrag(true);
            Obstacle o2 = level.makeObstacleAsBox(7, 0, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(0, 100, 0);
            o2.setCanDrag(true);

            // draw an obstacle that is oblong (due to its width and height) and
            // that is rotated. Note that this should be a box, or it will not
            // have the right underlying shape.
            o = level.makeObstacleAsBox(12, 12, 3.5f, .5f, "purpleball.png");
            o.setRotation(45);
        }

        /*
         * this level shows how we can use "poking" to move obstacles. In this
         * case, pressing an obstacle selects it, and pressing the screen moves
         * the obstacle to that location. Double-tapping an obstacle removes it.
         */
        else if (index == 24) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            level.getPreScene().addTextCentered(24, 26, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Touch the obstacle\nto select, then\ntouch to move it"), 0);

            // draw a picture on the default plane (0)... there are actually 5
            // planes (-2 through 2). Everything drawn on the same plane will be
            // drawn in order, so if we don't put this before the hero, the hero
            // will appear to go "under" the picture.
            level.drawPicture(0, 0, 48, 32, "greenball.png", 0);

            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a pokeable obstacle
            Obstacle o = level.makeObstacleAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
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
        else if (index == 25) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The enemy will chase you"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // create an enemy who chases the hero
            Enemy e3 = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(.1f, 0.3f, 0.6f);
            e3.setChaseSpeed(8, h, true, true);

            // draw a picture late within this block of code, but still cause
            // the picture to be drawn behind everything else by giving it a z
            // index of -1
            level.drawPicture(0, 0, 48, 32, "greenball.png", -2);

            // We can change the z-index of anything... let's move the enemy to
            // -2. Since we do this after drawing the picture, it will still be
            // drawn on top of the picture, but we should also be able to see it
            // go under the level.
            e3.setZIndex(-2);
        }

        /*
         * We can make obstacles play sounds either when we collide with them,
         * or touch them
         */
        else if (index == 26) {
            // set up a basic level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Touch the purple ball \nor collide with it"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up our obstacle so that collision and touch make it play
            // sounds
            Obstacle o = level.makeObstacleAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setTouchSound("lowpitch.ogg");
            o.setCollideSound("hipitch.ogg", 2000);
        }

        /*
         * this hero rotates so that it faces in the direction of movement. This
         * can be useful in games where the perspective is from overhead, and
         * the hero is moving in any X or Y direction
         */
        else if (index == 27) {
            // set up a big screen
            level.setCameraBounds(4 * 48, 2 * 32);

            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The star rotates in\nthe direction of movement"), 0);
            level.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(29, 60, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            level.setCameraChase(h);
        }

        /*
         * This level shows two things. The first is that a custom motion path
         * can allow things to violate the laws of physics and pass through
         * other things. The second is that motion paths can go off-screen.
         */
        else if (index == 28) {
            // set up a regular level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("Reach the destination\nto win the game."), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(21.5f, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(21.5f, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // this enemy starts from off-screen
            Enemy e = level.makeEnemyAsCircle(1, -20, 44, 44, "redball.png");
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
        else if (index == 29) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Draw on the screen\nto make obstacles appear"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(21.5f, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(21.5f, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // turn on 'scribble mode'... this says "draw a purple ball that is 1.5x1.5 at the
            // location where the scribble happened, but only do it if we haven't drawn anything in
            // 10 milliseconds."  It also says "when an obstacle is drawn, do some stuff to the
            // obstacle".  If you don't want any of this functionality, you can replace the whole
            // "new LolCallback..." region of code with "null".
            level.setScribbleMode("purpleball.png", 1.5f, 1.5f, 10, new LolActorEvent() {
                @Override
                public void go(WorldActor actor) {
                    // each time we draw an obstacle, it will be visible to this code as the
                    // callback's "attached WorldActor".  We'll change its elasticity, make it disappear
                    // after 10 seconds, and make it so that the obstacles aren't stationary
                    actor.setPhysics(0, 2, 0);
                    actor.setDisappearDelay(10, true);
                    actor.setCanFall();
                }
            });
        }

        /*
         * This level shows that we can "flick" things to move them. Notice that
         * we do not enableTilt tilt! Instead, we specified that there is a default
         * gravity in the Y dimension pushing everything down. This is much like
         * gravity on earth. The only way to move things, then, is via flicking
         * them.
         */
        else if (index == 30) {
            // create a level with a constant force downward in the Y dimension

            level.resetGravity(0, -10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            level.makeDestinationAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            level.setVictoryDestination(1);

            // create a hero who we can flick
            Hero h = level.makeHeroAsCircle(4, 27, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setFlickable(1f);
            h.disableRotation();

            Obstacle o = level.makeObstacleAsCircle(8, 27, 3, 3, "purpleball.png");
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
        else if (index == 31) {
            // make a long level but not a tall level, and provide a constant
            // downward force:
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            // turn on tilt, but only in the X dimension
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Side scroller / tilt demo"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.setCameraChase(h);
        }

        /*
         * In the previous level, it was hard to see that the hero was moving.
         * We can make a background layer to remedy this situation. Notice that
         * the background uses transparency to show the blue color for part of
         * the screen
         */
        else if (index == 32) {
            // start by repeating the previous level:
            level.setCameraBounds(30 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Side scroller / tilt demo"), 0);
            level.drawBoundingBox(0, 0, 30 * 48, 32, "red.png", 1, 0, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(30 * 48 - 5, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.setCameraChase(h);

            // now paint the background blue
            level.setBackgroundColor("#17B4FF");

            // put in a picture that scrolls at half the speed of the hero in
            // the x direction. Note that background "layers" are all drawn
            // *before* anything that is drawn with a z index... so the
            // background will be behind the hero
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // make an obstacle that hovers in a fixed place. Note that hovering
            // and zoom do not work together nicely.
            Obstacle o = level.makeObstacleAsCircle(10, 10, 5, 5, "blueball.png");
            o.setHover(100, 100);

            // Add a meter to show how far the hero has traveled
            level.addDisplay(5, 30, "arial.ttf", "#FF00FF", 16, "", " m", level.DisplayDistance(h), 2);

            // Add some text about the previous best score.
            level.addText(30, 30, "arial.ttf", "#000000", 12, "", "", level.DisplayFixedText("best: " + level.getGameFact("HighScore32", 0) + "M"), 0);

            // when this level ends, we save the best score. Once the
            // score is saved, it is saved permanently on the phone, though
            // every re-execution on the desktop resets the best score. Note
            // that we save the score whether we win or lose.
            LolAction sc = new LolAction() {
                public void go() {
                    int oldBest = level.getGameFact("HighScore32", 0);
                    if (oldBest < level.getDistance())
                        level.putGameFact("HighScore32", level.getDistance());
                }
            };

            level.setWinCallback(sc);
            level.setLoseCallback(sc);
        }

        /*
         * this level adds multiple background layers, and it also allows the
         * hero to jump via touch
         */
        else if (index == 33) {
            // set up a standard side scroller with tilt:
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero to\nmake it jump"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a hero
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.setCameraChase(h);
            // this says that touching makes the hero jump
            h.setTouchToJump();
            // this is the force of a jump. remember that up is positive.
            h.setJumpImpulses(0, 10);
            // the sound to play when we jump
            h.setJumpSound("fwapfwap.ogg");

            // set up our background again, but add a few more layers
            level.setBackgroundColor("#17B4FF");
            // this layer has a scroll factor of 0... it won't move
            level.addHorizontalBackgroundLayer(0, 1, "back.png", 0, 960, 640);
            // this layer moves at half the speed of the hero
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 480, 320);
            // this layer is faster than the hero
            level.addHorizontalBackgroundLayer(1.25f, 1, "front.png", 20, 454, 80);
        }

        /*
         * tilt doesn't always work so nicely in side scrollers. An alternative
         * is for the hero to have a fixed rate of motion. Another issue was
         * that you had to touch the hero itself to make it jump. Now, we use an
         * invisible button so touching any part of the screen makes the hero
         * jump.
         */
        else if (index == 34) {
            // set up a side scroller, but don't turn on tilt
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere to jump"), 0);
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // note: the bounding box does not have friction, and neither does
            // the hero
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 0, 0, 0);

            // make a hero, and ensure that it doesn't rotate
            Hero h = level.makeHeroAsCircle(2, 0, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            // give the hero a fixed velocity
            h.addVelocity(25, 0);
            // center the camera a little ahead of the hero, so he is not
            // centered
            h.setCameraOffset(15, 0);
            // enableTilt jumping
            h.setJumpImpulses(0, 10);
            level.setCameraChase(h);
            // set up the background
            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // draw a jump button that covers the whole screen
            level.addTapControl(0, 0, 960, 640, "", level.JumpAction(h));

            // if the hero jumps over the destination, we have a problem. To fix
            // it, let's put an invisible enemy right after the destination, so
            // that if the hero misses the destination, it hits the enemy and we
            // can start over. Of course, we could just do the destination like
            // this instead, but this is more fun...
            level.makeEnemyAsBox(130, 0, .5f, 32, "");
        }

        /*
         * the default is that once a hero jumps, it can't jump again until it
         * touches an obstacle (floor or wall). Here, we enableTilt multiple jumps.
         * Coupled with a small jump impulse, this makes jumping feel more like
         * swimming or controlling a helicopter.
         */
        else if (index == 35) {
            // Note: we can go above the trees
            level.setCameraBounds(3 * 48, 38);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Multi-jump is enabled"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 38, "red.png", 1, 0, 0);
            Hero h = level.makeHeroAsBox(2, 0, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            h.addVelocity(5, 0);
            level.setCameraChase(h);
            h.setCameraOffset(15, 0);
            // the hero now has multijump, with small jumps:
            h.setMultiJumpOn();
            h.setJumpImpulses(0, 6);

            // this is all the same as before, to include the invisible enemy
            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);
            level.addTapControl(0, 0, 960, 640, "", level.JumpAction(h));
            level.makeDestinationAsCircle(120, 31, 2, 2, "mustardball.png");
            level.makeEnemyAsBox(130, 0, .5f, 38, "");
            level.setVictoryDestination(1);
        }

        /*
         * This level shows that we can make a hero move based on how we touch
         * the screen
         */
        else if (index == 36) {
            level.setCameraBounds(3 * 48, 32);

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press screen borders\nto move the hero"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = level.makeHeroAsCircle(2, 0, 3, 3, "legstar1.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            level.setCameraChase(h);

            // this lets the hero flip its image when it moves backwards
            h.setDefaultAnimation(level.makeAnimation(200, true, "legstar1.png", "legstar1.png"));
            h.setDefaultReverseAnimation(level.makeAnimation(200, true, "fliplegstar8.png", "fliplegstar8.png"));

            level.makeDestinationAsCircle(120, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // let's draw an enemy, just in case anyone wants to try to go to
            // the top left corner
            level.makeEnemyAsCircle(3, 27, 3, 3, "redball.png");

            // draw some buttons for moving the hero
            level.addToggleButton(0, 100, 100, 440, "", level.makeXMotionAction(h, -15), level.makeXMotionAction(h, 0));
            level.addToggleButton(860, 100, 100, 440, "", level.makeXMotionAction(h, 15), level.makeXMotionAction(h, 0));
            level.addToggleButton(100, 540, 760, 100, "", level.makeYMotionAction(h, 15), level.makeYMotionAction(h, 0));
            level.addToggleButton(100, 0, 760, 100, "", level.makeYMotionAction(h, -15), level.makeYMotionAction(h, 0));
        }

        /*
         * In the last level, we had complete control of the hero's movement.
         * Here, we give the hero a fixed velocity, and only control its up/down
         * movement.
         */
        else if (index == 37) {
            level.setCameraBounds(3 * 48, 32);

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press screen borders\nto move up and down"), 0);
            // The box and hero should not have friction
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            level.makeDestinationAsCircle(120, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(10, 0);

            level.setCameraChase(h);

            // draw an enemy to avoid, and one at the end
            level.makeEnemyAsCircle(53, 28, 3, 3, "redball.png");
            level.makeEnemyAsBox(130, 0, .5f, 32, "");

            // draw the up/down controls
            level.addToggleButton(100, 540, 760, 100, "", level.makeYMotionAction(h, 15), level.makeYMotionAction(h, 0));
            level.addToggleButton(100, 0, 760, 100, "", level.makeYMotionAction(h, -15), level.makeYMotionAction(h, 0));
        }

        /*
         * this level demonstrates crawling heroes. We can use this to simulate
         * crawling, ducking, rolling, spinning, etc. Note, too, that we can use
         * it to make the hero defeat certain enemies via crawl.
         */
        else if (index == 38) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the screen\nto crawl"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);
            level.makeDestinationAsCircle(120, 0, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            Hero h = level.makeHeroAsBox(2, 1, 3, 7, "greenball.png");
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(5, 0);
            level.setCameraChase(h);
            // to enableTilt crawling, we just draw a crawl button on the screen
            level.addToggleButton(0, 0, 960, 640, "", level.makeCrawlToggle(h, true), level.makeCrawlToggle(h, false));

            // make an enemy who we can defeat by colliding with it while
            // crawling
            Enemy e = level.makeEnemyAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();
        }

        /*
         * We can make a hero start moving only when it is pressed. This can
         * even let the hero hover until it is pressed. We could also use this
         * to have a game where the player puts obstacles in place, then starts
         * the hero moving.
         */
        else if (index == 39) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero\nto start moving\n"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            level.makeDestinationAsCircle(120, 0, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a hero who doesn't start moving until it is touched
            //
            // note that this hero is a box, and the hero is "norotate". You
            // will probably getLoseScene strange behaviors if you choose any other
            // options
            Hero h = level.makeHeroAsBox(2, 1, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            h.setTouchAndGo(10, 0);
            level.setCameraChase(h);
        }

        /*
         * LibLOL has limited support for SVG. If you draw a picture in Inkscape
         * or another SVG tool, and it only consists of lines, then you can
         * import it into your game as an obstacle. Drawing a picture on top of
         * the obstacle is probably a good idea, though we don't bother in this
         * level
         */
        else if (index == 40) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.setTiltAsVelocity(true);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Obstacles can\nbe drawn from SVG\nfiles"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            // make a hero who can jump
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            level.setCameraChase(h);

            // draw an obstacle from SVG
            level.importLineDrawing("shape.svg", 2f, .5f, 25f, 15f, new LolActorEvent() {
                @Override
                public void go(WorldActor line) {
                    // This code is run each time a line of the SVG is drawn.  When we getLoseScene a line,
                    // we'll give it some density and friction.  Remember that the line is
                    // actually a rotated obstacle
                    line.setPhysics(1, 0, .1f);
                }
            });

            // provide a destination
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
        }

        /*
         * In a side-scrolling game, it is useful to be able to change the
         * hero's speed either permanently or temporarily. In LibLOL, we can use
         * a collision between a hero and an obstacle to achieve this effect.
         */
        else if (index == 41) {
            level.setCameraBounds(10 * 48, 32);

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Speed boosters and reducers"), 0);
            level.drawBoundingBox(0, 0, 10 * 480, 32, "", 1, 0, 1);

            Hero h = level.makeHeroAsCircle(2, 0, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.addVelocity(10, 0);
            level.setCameraChase(h);

            level.makeDestinationAsCircle(450, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // place a speed-up obstacle that lasts for 2 seconds
            Obstacle o1 = level.makeObstacleAsCircle(40, 1, 4, 4, "purpleball.png");
            o1.setSpeedBoost(20, 0, 2);

            // place a slow-down obstacle that lasts for 3 seconds
            Obstacle o2 = level.makeObstacleAsCircle(120, 1, 4, 6, "purpleball.png");
            o2.setSpeedBoost(-9, 0, 3);

            // place a permanent +3 speedup obstacle... the -1 means "forever"
            Obstacle o3 = level.makeObstacleAsCircle(240, 1, 4, 4, "purpleball.png");
            o3.setSpeedBoost(20, 0, -1);
        }

        /*
         * this is a very gross level, which exists just to show that
         * backgrounds can scroll vertically.
         */
        else if (index == 42) {
            // set up a level where tilt only makes the hero move up and down
            level.setCameraBounds(48, 4 * 32);

            level.enableTilt(0, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Vertical scroller demo"), 0);
            level.drawBoundingBox(0, 0, 48, 4 * 32, "red.png", 1, 0, 1);

            Hero h = level.makeHeroAsCircle(2, 120, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.setCameraChase(h);

            level.makeDestinationAsBox(0, 2, 48, 1, "mustardball.png");
            level.setVictoryDestination(1);

            // set up vertical scrolling backgrounds
            level.setBackgroundColor("#FF00FF");
            level.addVerticalBackgroundLayer(1, 0, "back.png", 0, 960, 640);
            level.addVerticalBackgroundLayer(1, .5f, "mid.png", 0, 960, 640);
            level.addVerticalBackgroundLayer(1, 1, "front.png", 0, 454, 80);
        }

        /*
         * the next few levels demonstrate support for throwing projectiles. In
         * this level, we throw projectiles by touching the level. Here, the
         * projectile always goes in the same direction
         */
        else if (index == 43) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero\nto make it throw\nprojectiles"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // create a hero, and indicate that touching it makes it throw
            // projectiles
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            // when the hero is touched, a projectile will
            // fly straight up, out of the top of the level.
            h.setTouchToThrow(h, 1.5f, 3, 0, 10);
            h.setMoveByTilting();

            // resetGravity a pool of projectiles. We say that there can be no more
            // than 3 projectiles in flight at any time.
            level.configureProjectiles(3, 1, 1, "greyball.png", 1, 0, true);
        }

        /*
         * This is another demo of how throwing projectiles works. Like the
         * previous demo, it doesn't actually use projectiles for anything, it
         * is just to show how to getLoseScene some different behaviors in terms of how
         * the projectiles move. In this case, we show that we can limit the
         * distance that projectiles travel, and that we can put a control on
         * the HUD for throwing projectiles
         */
        else if (index == 44) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere\nto throw a gray\nball"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 30, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(120, 0, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a pool of projectiles, but now once the projectiles travel
            // more than 25 meters, they disappear
            level.configureProjectiles(100, 1, 1, "greyball.png", 1, 0, true);
            level.setProjectileRange(25);

            // add a button for throwing projectiles. Notice that this butotn
            // keeps throwing as long as it is held, but we've capped it to
            // throw no more than once per 100 milliseconds
            level.addToggleButton(0, 0, 960, 640, "", level.makeRepeatThrow(h, 100, 3, 1.5f, 30, 0), null);
            level.setCameraChase(h);
        }

        /*
         * this level demonstrates that we can defeat enemies by throwing
         * projectiles at them
         */
        else if (index == 45) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Defeat all enemies\nto win"), 0);

            Hero h = level.makeHeroAsCircle(4, 27, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // set up our projectiles... note that now projectiles each do 2
            // units of damage
            level.configureProjectiles(3, .4f, .1f, "greyball.png", 2, 0, true);

            // draw a few enemies... note that they have different amounts of
            // damage, so it takes different numbers of projectiles to defeat
            // them.
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5) {
                Enemy ee = level.makeEnemyAsCircle(i, i + 5, 2, 2, "redball.png");
                ee.setPhysics(1.0f, 0.3f, 0.6f);
                ee.setDamage(i);
            }

            // win by defeating enemies, of course!
            level.setVictoryEnemyCount();

            // this button only throws one projectile per press...
            level.addTapControl(0, 0, 960, 640, "", level.ThrowFixedAction(h, .2f, -.5f, 0, 10));
        }

        /*
         * This level shows how to throw projectiles in a variety of directions.
         */
        else if (index == 46) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere\nto throw a ball"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.setCameraChase(h);

            level.makeDestinationAsCircle(120, 0, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions...
            // again, note that if we hold the button, it keeps throwing
            level.addDirectionalThrowButton(0, 0, 960, 640, "", h, 0, 0, 0);

            // set up our pool of projectiles. The main challenge here is that
            // the farther from the hero we press, the faster the projectile
            // goes, so we multiply the velocity by .8 to slow it down a bit
            level.configureProjectiles(100, 1, 1, "greyball.png", 1, 0, true);
            level.setProjectileVectorDampeningFactor(.8f);
            level.setProjectileRange(30);
        }

        /*
         * this level shows that with the "vector" projectiles, we can still
         * have gravity affect the projectiles. This is very good for
         * basketball-style games.
         */
        else if (index == 47) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere\nto throw a ball"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(0, 0, 0.6f);
            h.setMoveByTilting();
            level.setCameraChase(h);

            level.makeDestinationAsCircle(120, 0, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // we use a "single throw" button so that holding doesn't throw more
            // projectiles.
            level.addTapControl(0, 0, 960, 640, "", level.ThrowDirectionalAction(h, 1.5f, 1.5f));

            // we turn on projectile gravity, and then we enableTilt collisions for
            // projectiles. This means that when a projectile collides with
            // something, it will transfer its momentum to that thing, if that
            // thing is moveable. This is a step toward our goal of being able
            // to bounce a basketball off of a backboard, but it's not quite
            // enough...
            level.configureProjectiles(100, 1, 1, "greyball.png", 1, 0, true);
            level.setProjectileVectorDampeningFactor(.8f);
            level.setProjectileRange(40);
            level.setProjectileGravityOn();
            level.enableCollisionsForProjectiles();

            // This next line is interesting... it lets projectiles collide with
            // each other without disappearing
            level.setCollisionOkForProjectiles();

            // Draw an obstacle... this is like our backboard, but we're putting
            // it in a spot that's more useful for testing than for playing a
            // game
            Obstacle o = level.makeObstacleAsBox(10, 20, 2, 2, "red.png");

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
            o.setProjectileCollisionCallback(new CollisionCallback() {
                @Override
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
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
        else if (index == 48) {


            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Throw balls at \nthe enemies before\nthey reproduce"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setTouchToThrow(h, 2, -.5f, 0, 10);
            h.setMoveByTilting();

            // resetGravity a pool of projectiles... now we have a sound that plays
            // when a projectile is thrown, and another for when it disappears
            level.configureProjectiles(100, .5f, .5f, "greyball.png", 1, 0, true);
            level.setThrowSound("fwapfwap.ogg");
            level.setProjectileDisappearSound("slowdown.ogg");

            // draw an enemy that makes a sound when it disappears
            final Enemy e = level.makeEnemyAsCircle(23, 20, 2, 2, "redball.png");
            e.setDisappearSound("lowpitch.ogg");

            // This enemy will create mini-enemies until it is defeated
            LolAction sc = new LolAction() {
                public void go() {
                    int mIntVal = level.getLevelFact("counter", 0);
                    // only reproduce the enemy if it is visible
                    if (e.getEnabled()) {
                        // make an enemy to the left and up
                        Enemy left = level.makeEnemyAsCircle(e.getXPosition() - mIntVal,
                                e.getYPosition() + mIntVal, 1, 1, "redball.png");
                        left.setDisappearSound("lowpitch.ogg");

                        // make an enemy to the right of and above
                        // attachedSprite
                        Enemy right = level.makeEnemyAsCircle(e.getXPosition() + mIntVal,
                                e.getYPosition() + mIntVal, 1, 1, "redball.png");
                        right.setDisappearSound("lowpitch.ogg");
                        mIntVal++;
                        level.putLevelFact("counter", mIntVal);
                    }
                }
            };
            level.putLevelFact("counter", 0);

            // request that in 2 seconds, if the enemy is still visible,
            // onTimerCallback() will run, with id == 2. Be sure to look at
            // the onTimerCallback code (below) for more information. Note that
            // there are two versions of the function, and this uses the second!
            level.setTimerCallback(2, 2, sc);

            // win by defeating enemies
            level.setVictoryEnemyCount();

            // put a count of defeated enemies on the screen
            level.addDisplay(20, 20, "arial.ttf", "#000000", 32, "", "", level.DisplayEnemiesDefeated, 2);
        }

        /*
         * This level shows that we can have moveable enemies that reproduce. Be
         * careful... it is possible to make a lot of enemies, really quickly
         */
        else if (index == 49) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("These enemies are\nreally tricky"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            level.makeDestinationAsCircle(29, 29, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make our initial enemy
            Enemy e = level.makeEnemyAsCircle(23, 2, 1, 1, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setMoveByTilting();

            // Let's use a little bit of java here.  We will make an ArrayList to store all of the
            // enemies in the level.  Then, we can access it from the callback below, in order to
            // make some enemies reproduce
            final ArrayList<Enemy> enemies = new ArrayList<>();
            enemies.add(e);

            // set a timer callback on the level, to repeatedly spawn new enemies.
            // warning: "6" is going to lead to lots of enemies eventually, and there's no
            // way to defeat them in this level!
            LolAction sc = new LolAction() {
                public void go() {
                    ArrayList<Enemy> newEnemies = new ArrayList<>();
                    for (Enemy e : enemies) {
                        // Is the enemy visible / alive?
                        if (e.getEnabled()) {
                            // If this enemy has remaining reproductions
                            if (e.getInfoInt() > 0) {
                                // decrease remaining reproductions
                                e.setInfoInt(e.getInfoInt() - 1);

                                // reproduce the enemy
                                Enemy e2 = level.makeEnemyAsCircle(e.getXPosition(), e.getYPosition(),
                                        e.getWidth(), e.getHeight(), "redball.png");
                                e2.setPhysics(1.0f, 0.3f, 0.6f);
                                e2.setMoveByTilting();

                                // set the new enemy's reproductions, save it
                                e2.setInfoInt(e.getInfoInt());
                                newEnemies.add(e2);
                            }
                        }
                    }
                    // Add the new enemies to the list
                    enemies.addAll(newEnemies);
                }
            };
            level.setTimerCallback(2, 2, sc);
            // request 6 reproductions... that's going to be a lot!
            e.setInfoInt(6);
        }

        /*
         * this level shows simple animation. Every entity can have a default
         * animation.
         */
        else if (index == 50) {
            // set up a basic level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Animations"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // this hero will be animated:
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // this says that we scroll through the 0, 1, 2, and 3 cells of the
            // image, and we show each for 200 milliseconds. This is the "easy"
            // animation mechanism, where every cell is shown for the same
            // amount of time
            h.setDefaultAnimation(level.makeAnimation(200, true, "legstar1.png", "legstar2.png", "legstar3.png", "legstar4.png"));
        }

        /*
         * this level introduces jumping animations and disappearance animations
         */
        else if (index == 51) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero to\nmake it jump"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a hero, and give it two animations: one for when it is in
            // the air, and another for the rest of the time.
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            level.setCameraChase(h);

            // this is the more complex form of animation... we show the
            // different cells for different lengths of time
            h.setDefaultAnimation(level.makeAnimation(4, true).to("legstar1.png", 150).to("legstar2.png", 200).to("legstar3.png", 300).to("legstar4.png", 350));
            // we can use the complex form to express the simpler animation, of
            // course
            h.setJumpAnimation(level.makeAnimation(4, true).to("legstar4.png", 200).to("legstar6.png", 200).to("legstar7.png", 200).to("legstar8.png", 200));

            // create a goodie that has a disappearance animation. When the
            // goodie is ready to disappear, we'll remove it, and then we'll run
            // the disappear animation. That means that we can make it have any
            // size we want, but we need to offset it from the (defunct)
            // goodie's position. Note, too, that the final cell is blank, so
            // that we don't leave a residue on the screen.
            Goodie g = level.makeGoodieAsCircle(15, 9, 5, 5, "starburst3.png");
            g.setDisappearAnimation(level.makeAnimation(4, false).to("starburst3.png", 200).to("starburst2.png", 200).to("starburst1.png", 200)
                    .to("starburst4.png", 200), 1, 0, 5, 5);
        }

        /*
         * this level shows that projectiles can be animated, and that we can
         * animate the hero while it throws a projectile
         */
        else if (index == 52) {
            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero\nto make it\nthrow a ball"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up our hero
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "colorstar.png");
            h.setPhysics(1, 0, 0.6f);
            h.setTouchToThrow(h, 0, -.5f, 0, 10);
            h.setMoveByTilting();

            // set up an animation when the hero throws:
            h.setThrowAnimation(level.makeAnimation(2, false).to("colorstar4.png", 100).to("colorstar5.png", 500));

            // make a projectile pool and give an animation pattern for the
            // projectiles
            level.configureProjectiles(100, 1, 1, "flystar.png", 1, 0, true);
            level.setProjectileAnimation(level.makeAnimation(100, true, "flystar1.png", "flystar2.png"));
        }

        /*
         * This level explores invincibility animation. While we're at it, we
         * make some enemies that aren't affected by invincibility, and some
         * that can even damage the hero while it is invincible.
         */
        else if (index == 53) {


            level.enableTilt(10, 10);
            level.getPreScene().addText(50 / 20f, 50 / 20f, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The blue ball will\nmake you invincible\nfor 15 seconds"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            level.makeDestinationAsCircle(29, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make an animated hero, and give it an invincibility animation
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setDefaultAnimation(level.makeAnimation(4, true).to("colorstar1.png", 300).to("colorstar2.png", 300).to("colorstar3.png", 300).to("colorstar4.png", 300));
            h.setInvincibleAnimation(level.makeAnimation(4, true).to("colorstar5.png", 100).to("colorstar6.png", 100).to("colorstar7.png", 100).to("colorstar8.png", 100));

            // make some enemies
            for (int i = 0; i < 5; ++i) {
                Enemy e = level.makeEnemyAsCircle(5 * i + 1, 25, 2, 2, "redball.png");
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
            Enemy e = level.makeEnemyAsCircle(30, 20, 2, 2, "redball.png");
            e.setPhysics(10, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(0);

            // add a goodie that makes the hero invincible
            Goodie g = level.makeGoodieAsCircle(30, 30, 2, 2, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);
            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Goodies", level.DisplayGoodies1, 2);

            // draw a picture when the level is won, and don't print text...
            // this particular picture isn't very useful
            level.getWinScene().makePicture(0, 0, 960 / 20f, 640 / 20f, "fade.png", 0);
            level.getWinScene().setDefaultText("");
        }

        /*
         * demonstrate crawl animation, and also show that on multitouch phones,
         * we can "crawl" in the air while jumping.
         */
        else if (index == 54) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the left side of\nthe screen to crawl\nor the right side\nto jump."), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);

            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a hero with fixed velocity, and give it crawl and jump
            // animations
            Hero h = level.makeHeroAsBox(2, 1, 3, 7, "legstar1.png");
            h.setPhysics(1, 0, 0);
            h.addVelocity(15, 0);
            h.setCrawlAnimation(level.makeAnimation(4, true).to("legstar1.png", 100).to("legstar2.png", 300).to("legstar3.png", 300).to("legstar4.png", 100));
            h.setJumpAnimation(level.makeAnimation(4, true).to("legstar5.png", 200).to("legstar6.png", 200).to("legstar7.png", 200).to("legstar8.png", 200));

            // enable hero jumping and crawling
            h.setJumpImpulses(0, 15);
            level.addTapControl(0, 0, 480, 640, "", level.JumpAction(h));
            level.addToggleButton(480, 0, 480, 640, "", level.makeCrawlToggle(h, true), level.makeCrawlToggle(h, false));

            // add an enemy we can defeat via crawling, just for fun. It should
            // be defeated even by a "jump crawl"
            Enemy e = level.makeEnemyAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();

            // include a picture on the "try again" screen
            level.getLoseScene().makePicture(0, 0, 960 / 20f, 640 / 20f, "fade.png", 0);
            level.getLoseScene().setDefaultText("Oh well...");
            level.setCameraChase(h);
        }

        /*
         * This isn't quite the same as animation, but it's nice. We can
         * indicate that a hero's image changes depending on its strength. This
         * can, for example, allow a hero to change (e.g., getLoseScene healthier) by
         * swapping through images as goodies are collected, or allow the hero
         * to switch its animation depending on how many enemies it has collided
         * with
         */
        else if (index == 55) {
            // set up a basic level with a bunch of goodies


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Since colorstar.png has 8 frames, and we're displaying frame 0 as
            // "health == 0", let's add 7 more goodies, each of which adds 1 to
            // the hero's strength.
            for (int i = 0; i < 7; ++i) {
                Goodie g = level.makeGoodieAsCircle(5 + 2 * i, 5 + 2 * i, 2, 2, "blueball.png");
                g.setStrengthBoost(1);
            }

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make 8 enemies, each with strength == 1. This means we can lose
            // the level, and that we can test moving our strength all the way
            // up to 7, and all the way back down to 0.
            for (int i = 0; i < 8; ++i) {
                Enemy e = level.makeEnemyAsCircle(5 + 2 * i, 1 + 2 * i, 2, 2, "redball.png");
                e.setDamage(1);
            }

            // Note: colorstar.png has 8 cells...
            Hero h = level.makeHeroAsCircle(4, 27, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // provide some code to run when the hero's strength changes
            h.setStrengthChangeCallback(new LolActorEvent() {
                public void go(WorldActor actor) {
                    // get the hero's strength. Since the hero isn't dead, the
                    // strength is at least 1. Since there are 7 strength
                    // booster goodies, the strength is at most 8.
                    int s = ((Hero) actor).getStrength();
                    // set the hero's image index to (s-1), i.e., one of the
                    // indices in the range 0..7, depending on strength
                    actor.setImage("colorstar" + s + ".png");

                }
            });
        }

        /*
         * demonstrate that obstacles can defeat enemies, and that we can use
         * this feature to have obstacles that only defeat certain "marked"
         * enemies
         */
        else if (index == 56) {


            level.enableTilt(10, 10);
            // increase the speed at which tilt affects velocity
            level.setGravityMultiplier(3);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("You can defeat\ntwo enemies with\nthe blue ball"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // put an enemy defeated count on the screen, in red with a small
            // font
            level.addDisplay(20, 20, "arial.ttf", "#FF0000", 10, "", "/2 Enemies Defeated", level.DisplayEnemiesDefeated, 2);

            // make a moveable obstacle that can defeat enemies
            Obstacle o = level.makeObstacleAsCircle(10, 2, 4, 4, "blueball.png");
            o.setPhysics(.1f, 0, 0.6f);
            o.setMoveByTilting();
            // this says that we don't need to collect any goodies before this
            // obstacle defeats enemies (0,0,0,0), and that when this obstacle
            // collides with any enemy, the onEnemyCollideCallback() code will
            // run, with id == 14. Notice, too, that there will be a half second
            // delay before the code runs.
            o.setEnemyCollisionCallback(.5f, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    // This obstacle can only defeat the big enemy, and it
                    // disappears when it defeats the enemy
                    if (collideActor.getInfoText().equals("big")) {
                        ((Enemy) collideActor).defeat(true);
                        thisActor.remove(true);
                    }

                }
            });

            // make a small obstacle that can also defeat enemies, but doesn't
            // disappear
            Obstacle o2 = level.makeObstacleAsCircle(.5f, .5f, 2, 2, "blueball.png");
            o2.setPhysics(1, 0, 0.6f);
            o2.setMoveByTilting();
            o2.setEnemyCollisionCallback(0, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    ((Enemy) collideActor).defeat(true);
                }
            });

            // make four enemies
            Enemy e = level.makeEnemyAsCircle(40, 2, 4, 4, "redball.png");
            e.setPhysics(1, 0, 0.6f);
            e.setMoveByTilting();
            Enemy e1 = level.makeEnemyAsCircle(30, 2, 4, 4, "redball.png");
            e1.setPhysics(1, 0, 0.6f);
            Enemy e2 = level.makeEnemyAsCircle(40, 22, 2, 2, "redball.png");
            e2.setPhysics(1, 0, 0.6f);
            e2.setMoveByTilting();
            Enemy e3 = level.makeEnemyAsCircle(40, 12, 4, 4, "redball.png");
            e3.setPhysics(1, 0, 0.6f);
            e3.setMoveByTilting();

            // now let's put a note into e2 and e3
            e2.setInfoText("small");
            e3.setInfoText("big");

            // win by defeating enemies
            level.setVictoryEnemyCount(2);

            // be sure to look at onEnemyCollideCallback to see how this level
            // will play out.
        }

        /*
         * this level shows an odd way of moving the level. There's friction on
         * the floor, so it can only move by tilting while the hero is in the
         * air
         */
        else if (index == 57) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero to\nmake it jump"), 0);
            // note: the floor has friction
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // make a box hero with friction... it won't roll on the floor, so
            // it's stuck!
            Hero h = level.makeHeroAsBox(2, 2, 3, 3, "legstar1.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 5);
            h.setMoveByTilting();
            level.setCameraChase(h);

            // the hero *can* jump...
            h.setTouchToJump();
            h.setJumpImpulses(0, 15);

            // draw a background
            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);
        }

        /*
         * this level shows that we can put an obstacle on the screen and use it
         * to make the hero throw projectiles. It also shows that we can make
         * entities that shrink over time... growth is possible too, with a
         * negative value.
         */
        else if (index == 58) {

            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(4, 5, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // make an obstacle that causes the hero to throw Projectiles when
            // touched
            Obstacle o = level.makeObstacleAsCircle(43, 27, 5, 5, "purpleball.png");
            o.setCollisionsEnabled(false);
            o.setTouchToThrow(h, 1.5f, 1.5f, 0, 15);

            // set up our projectiles
            level.configureProjectiles(3, 1, 1, "colorstar.png", 2, 0, true);
            level.setNumberOfProjectiles(20);
            // there are only 20... throw them carefully

            // Allow the projectile image to be chosen randomly from a sprite
            // sheet
            level.setProjectileImageSource("colorstar.png");

            // show how many shots are left
            level.addDisplay(5, 300, "arial.ttf", "#FF00FF", 12, "", " projectiles left", level.DisplayRemainingProjectiles, 2);

            // draw a bunch of enemies to defeat
            Enemy e = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5)
                level.makeEnemyAsCircle(i, i + 8, 2, 2, "redball.png");

            // draw a few obstacles that shrink over time, to show that circles
            // and boxes work, we can shrink the X and Y rates independently,
            // and we can opt to center things as they shrink or grow
            Obstacle floor = level.makeObstacleAsBox(2, 3, 42, 3, "red.png");
            floor.setShrinkOverTime(1, 1, true);

            Obstacle roof = level.makeObstacleAsBox(24, 30, 1, 1, "red.png");
            roof.setShrinkOverTime(-1, 0, false);

            Obstacle ball1 = level.makeObstacleAsCircle(40, 8, 8, 8, "purpleball.png");
            ball1.setShrinkOverTime(1, 2, true);

            Obstacle ball2 = level.makeObstacleAsCircle(40, 16, 8, 8, "purpleball.png");
            ball2.setShrinkOverTime(2, 1, false);

            level.setVictoryEnemyCount(5);
        }

        /*
         * this level shows that we can make a hero in the air rotate. Rotation
         * doesn't do anything, but it looks nice...
         */
        else if (index == 59) {
            // make a simple level

            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press to rotate the hero"), 0);

            // warning: this destination is just out of the hero's reach when
            // the hero
            // jumps... you'll have to hit the side wall and jump again to reach
            // it!
            level.makeDestinationAsCircle(46, 8, 2.5f, 2.5f, "mustardball.png");
            level.setVictoryDestination(1);

            // make the hero jumpable, so that we can see it spin in the air
            Hero h = level.makeHeroAsCircle(4, 27, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 10);
            h.setTouchToJump();

            // add rotation buttons
            level.addToggleButton(0, 480, 160, 160, "", level.makeRotator(h, -.05f), level.makeRotator(h, -.05f));
            level.addToggleButton(760, 480, 160, 160, "", level.makeRotator(h, .05f), level.makeRotator(h, .05f));
        }

        /**
         * we can attach movement buttons to any moveable entity, so in this
         * case, we attach it to an obstacle to get an arkanoid-like effect.
         */
        else if (index == 60) {
            // make a simple level


            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            level.makeDestinationAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            level.setVictoryDestination(1);

            // make a hero who is always moving... note there is no friction,
            // anywhere, and the hero is elastic... it won't ever stop...
            Hero h = level.makeHeroAsCircle(4, 4, 3, 3, "greenball.png");
            h.setPhysics(0, 1, .1f);
            h.addVelocity(0, 10);

            // make an obstacle and then connect it to some controls
            Obstacle o = level.makeObstacleAsBox(2, 30.9f, 4, 1, "red.png");
            o.setPhysics(100, 1, .1f);
            level.addToggleButton(0, 0, 480, 640, "", level.makeXMotionAction(o, -5), level.makeXMotionAction(o, 0));
            level.addToggleButton(480, 0, 480, 640, "", level.makeXMotionAction(o, 5), level.makeXMotionAction(o, 0));
        }

        /*
         * this level demonstrates that things can appear and disappear on
         * simple timers
         */
        else if (index == 61) {
            // set up a basic level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Things will appear \nand disappear..."), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // create an enemy that will quietly disappear after 2 seconds
            Enemy e1 = level.makeEnemyAsCircle(25, 25, 2, 2, "redball.png");
            e1.setPhysics(1.0f, 0.3f, 0.6f);
            e1.setRotationSpeed(1);
            e1.setDisappearDelay(2, true);

            // create an enemy that will appear after 3 seconds
            Enemy e2 = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
            e2.setPhysics(1.0f, 0.3f, 0.6f);
            e2.setRoute(new Route(3).to(35, 25).to(15, 25).to(35, 25), 3, true);
            e2.setAppearDelay(3);
        }

        /*
         * This level demonstrates the use of timer callbacks. We can use timers
         * to make more of the level appear over time. In this case, we'll chain
         * the timer callbacks together, so that we can getLoseScene more and more things
         * to develop. Be sure to look at the onTimerCallback code to see how
         * the rest of this level works.
         */
        else if (index == 62) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("There's nothing to\ndo... yet"), 0);

            // note: there's no destination yet, but we still say it's how to
            // win... we'll getLoseScene a destination in this level after a few timers
            // run...
            level.setVictoryDestination(1);

            // set a timer callback. after three seconds, the callback will run
            level.setTimerCallback(2, new LolAction() {
                public void go() {
                    // put up a pause scene to interrupt gameplay
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFF00", 12, "", "", level.DisplayFixedText("Ooh... a draggable enemy"), 0);
                    level.getPauseScene().show();
                    // make a draggable enemy
                    Enemy e3 = level.makeEnemyAsCircle(35, 25, 2, 2, "redball.png");
                    e3.setPhysics(1.0f, 0.3f, 0.6f);
                    e3.setCanDrag(true);
                }
            });

            // set another callback that runs after 6 seconds (note: time
            // doesn't count while the PauseScene is showing...)
            level.setTimerCallback(6, new LolAction() {
                public void go() {
                    // clear the pause scene, then put new text on it
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FF00FF", 12, "", "", level.DisplayFixedText("Touch the enemy and it will go away"), 0);
                    level.getPauseScene().show();
                    // add an enemy that is touch-to-defeat
                    Enemy e4 = level.makeEnemyAsCircle(35, 5, 2, 2, "redball.png");
                    e4.setPhysics(1.0f, 0.3f, 0.6f);
                    e4.setDisappearOnTouch();
                }
            });

            // set a callback that runs after 9 seconds. Though it's not
            // necessary in this case, we're going to make the callback an
            // explicit object. This can be useful, as we'll see later on.
            level.setTimerCallback(9, new LolAction() {
                public void go() {
                    // draw an enemy, a goodie, and a destination, all with
                    // fixed velocities
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFF00", 12, "", "", level.DisplayFixedText("Now you can see the rest of the level"), 0);
                    level.getPauseScene().show();
                    Destination d = level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
                    d.addVelocity(-.5f, -1);

                    Enemy e5 = level.makeEnemyAsCircle(35, 15, 2, 2, "redball.png");
                    e5.setPhysics(1.0f, 0.3f, 0.6f);
                    e5.addVelocity(4, 4);

                    Goodie gg = level.makeGoodieAsCircle(10, 10, 2, 2, "blueball.png");
                    gg.addVelocity(5, 5);
                }
            });

            // Lastly, we can make a timer callback that runs over and over
            // again. This one starts after 2 seconds, then runs every second.
            level.putLevelFact("spawnLoc", 0);
            level.setTimerCallback(2, 1, new LolAction() {
                public void go() {
                    int spawnLoc = level.getLevelFact("spawnLoc", 0);
                    // note that every SimpleCallback has a field called
                    // "intVal" that is initially 0. By using and then modifying
                    // that field inside of the timer code, we can ensure that
                    // each execution of the timer is slightly different, even
                    // if the game state hasn't changed.
                    level.makeObstacleAsCircle(spawnLoc % 48, spawnLoc / 48, 1, 1, "purpleball.png");
                    spawnLoc++;
                    level.putLevelFact("spawnLoc", spawnLoc);
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
        else if (index == 63) {
            level.setCameraBounds(3 * 48, 32);

            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Keep going right!"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.setCameraChase(h);

            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Goodies", level.DisplayGoodies1, 2);
            level.setVictoryDestination(1);

            // this obstacle is a collision callback... when the hero hits it,
            // the next part of the level appears, via onHeroCollideCallback().
            // Note, too, that it disappears when the hero hits it, so we can
            // play a sound if we want...
            Obstacle o = level.makeObstacleAsBox(30, 0, 1, 32, "purpleball.png");
            o.setPhysics(1, 0, 1);
            // NB: we use a level fact to track how far we've come
            level.putLevelFact("crossings", 0);
            // the callback id is 0, there is no delay, and no goodies are
            // needed before it works
            o.setHeroCollisionCallback(0, 0, 0, 0, 0, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    // getLoseScene rid of the obstacle we just collided with
                    thisActor.remove(false);
                    // make a goodie
                    level.makeGoodieAsCircle(45, 1, 2, 2, "blueball.png");
                    // make an obstacle that is a callback, but that doesn't
                    // work until the goodie count is 1
                    Obstacle oo = level.makeObstacleAsBox(60, 0, 1, 32, "purpleball.png");

                    // we're going to chain a bunch of callbacks together, and
                    // the best way to do that is to make a single callback that
                    // behaves differently based on the value of the callback's
                    // intVal field.
                    CollisionCallback sc2 = new CollisionCallback() {
                        public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                            int crossings = level.getLevelFact("crossings", 0);
                            // The second callback works the same way
                            if (crossings == 0) {
                                thisActor.remove(false);
                                level.makeGoodieAsCircle(75, 21, 2, 2, "blueball.png");

                                Obstacle oo = level.makeObstacleAsBox(90, 0, 1, 32, "purpleball.png");
                                oo.setHeroCollisionCallback(2, 0, 0, 0, 0, this);
                                level.putLevelFact("crossings", 1);
                            }
                            // same for the third callback
                            else if (crossings == 1) {
                                thisActor.remove(false);
                                level.makeGoodieAsCircle(105, 1, 2, 2, "blueball.png");

                                Obstacle oo = level.makeObstacleAsBox(120, 0, 1, 32, "purpleball.png");
                                oo.setHeroCollisionCallback(3, 0, 0, 0, 0, this);
                                level.putLevelFact("crossings", 2);
                            }
                            // The fourth callback draws the destination
                            else if (crossings == 2) {
                                thisActor.remove(false);
                                // print a message and pause the game, via
                                // PauseScene
                                level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The destination is\nnow available"), 0);
                                level.makeDestinationAsCircle(120, 20, 2, 2, "mustardball.png");
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
        else if (index == 64) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Activate and then \ntouch the obstacle"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // make a level... notice that it needs a lot more goodies
            // than are on the screen...
            Destination d = level.makeDestinationAsCircle(29, 1, 2, 2, "mustardball.png");
            d.setActivationScore(3, 0, 0, 0);
            level.setVictoryDestination(1);

            // draw an obstacle, make it a touch callback, and then draw the
            // goodie we need to get in order to activate the obstacle
            Obstacle o = level.makeObstacleAsCircle(10, 5, 3, 3, "purpleball.png");
            o.setPhysics(1, 0, 1);
            // we'll give this callback the id "39", just for fun
            o.setTouchCallback(1, 0, 0, 0, true, new LolActorEvent() {
                public void go(WorldActor actor) {
                    // note: we could draw a picture of an open chest in the
                    // obstacle's place, or even use a disappear animation whose
                    // final frame looks like an open treasure chest.
                    actor.remove(false);
                    for (int i = 0; i < 3; ++i)
                        level.makeGoodieAsCircle(9 * i, 20 - i, 2, 2, "blueball.png");
                }
            });
            o.setDisappearSound("hipitch.ogg");

            Goodie g = level.makeGoodieAsCircle(0, 30, 2, 2, "blueball.png");
            g.setDisappearSound("lowpitch.ogg");
        }

        /*
         * this level shows how to use enemy defeat callbacks. There are four
         * ways to defeat an enemy, so we enableTilt all mechanisms in this level,
         * to see if they all work to cause enemy callbacks to run the
         * onEnemyCallback code. Another important point here is that the IDs
         * don't need to be unique for *any* callbacks. We can use the same ID
         * every time...
         */
        else if (index == 65) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // give the hero strength, so that we can use him to defeat an enemy
            // as a test of enemy callbacks
            Hero h = level.makeHeroAsCircle(12, 12, 4, 4, "greenball.png");
            h.setStrength(3);
            h.setMoveByTilting();
            h.setInvincibleAnimation(level.makeAnimation(4, true).to("colorstar5.png", 100).to("colorstar6.png", 100).to("colorstar7.png", 100).to("colorstar8.png", 100));

            // a goodie, so we can do defeat by invincibility
            Goodie g1 = level.makeGoodieAsCircle(20, 29, 2, 3, "purpleball.png");
            g1.setInvincibilityDuration(15);

            // enableTilt throwing projectiles, so that we can test enemy callbacks
            // again
            h.setTouchToThrow(h, 4, 2, 30, 0);
            level.configureProjectiles(100, 1, 1, "greyball.png", 1, 0, true);

            // add an obstacle that has an enemy collision callback, so it can
            // defeat enemies
            Obstacle o = level.makeObstacleAsCircle(30, 10, 5, 5, "blueball.png");
            o.setPhysics(1000, 0, 0);
            o.setCanDrag(false);
            o.setEnemyCollisionCallback(0, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    if (collideActor.getInfoText().equals("weak")) {
                        ((Enemy) collideActor).defeat(true);
                    }
                }
            });

            // now draw our enemies... we need enough to be able to test that
            // all four defeat mechanisms work. Note that we attach defeat
            // callback code to each of them.
            LolActorEvent sc = new LolActorEvent() {
                public void go(WorldActor actor) {
                    // always reset the pausescene, in case it has something on
                    // it from before...
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#58E2A0", 16, "", "", level.DisplayFixedText("good job, here's a prize"), 0);
                    level.getPauseScene().show();
                    // use random numbers to figure out where to draw a goodie
                    // as a reward... picking in the range 0-46,0-30 ensures
                    // that with width and height of 2, the goodie stays on
                    // screen
                    level.makeGoodieAsCircle(level.getRandom(46), level.getRandom(30), 2, 2, "blueball.png");
                }
            };
            Enemy e1 = level.makeEnemyAsCircle(5, 5, 1, 1, "redball.png");
            e1.setDefeatCallback(sc);

            Enemy e2 = level.makeEnemyAsCircle(5, 5, 2, 2, "redball.png");
            e2.setDefeatCallback(sc);
            e2.setInfoText("weak");

            Enemy e3 = level.makeEnemyAsCircle(40, 3, 1, 1, "redball.png");
            e3.setDefeatCallback(sc);

            Enemy e4 = level.makeEnemyAsCircle(25, 25, 1, 1, "redball.png");
            e4.setDefeatCallback(sc);
            e4.setDisappearOnTouch();

            Enemy e5 = level.makeEnemyAsCircle(25, 29, 1, 1, "redball.png");
            e5.setDefeatCallback(sc);

            // win by defeating enemies
            level.setVictoryEnemyCount();
        }

        /*
         * This level shows that we can resize a hero on the fly, and change its
         * image. We use a collision callback to cause the effect. Furthermore,
         * we can increment scores inside of the callback code, which lets us
         * activate the destination on an obstacle collision
         */
        else if (index == 66) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("Only stars can reach\nthe destination"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            level.addDisplay(220, 280, "arial.ttf", "#3C46FF", 12, "", " Goodies", level.DisplayGoodies1, 2);

            // the destination won't work until some goodies are collected...
            Destination d = level.makeDestinationAsBox(46, 2, 2, 2, "colorstar.png");
            d.setActivationScore(4, 1, 3, 0);
            level.setVictoryDestination(1);

            // Colliding with this star will make the hero into a star... see
            // onHeroCollideCallback for details
            Obstacle o = level.makeObstacleAsBox(30, 0, 3, 3, "legstar1.png");
            o.setPhysics(1, 0, 1);
            o.setHeroCollisionCallback(0, 0, 0, 0, 1, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    // here's a simple way to increment a goodie count
                    level.incrementGoodiesCollected2();
                    // here's a way to set a goodie count
                    level.setGoodiesCollected3(3);
                    // here's a way to read and write a goodie count
                    level.setGoodiesCollected1(4 + level.getGoodiesCollected1());
                    // get rid of the star, so we know it's been used
                    thisActor.remove(true);
                    // resize the hero, and change its image
                    collideActor.resize(collideActor.getXPosition(), collideActor.getYPosition(), 5, 5);
                    collideActor.setImage("legstar1.png");
                }
            });
        }

        /*
         * This level shows how to use countdown timers to win a level, tests
         * some color features, and introduces a vector throw mechanism with
         * fixed velocity
         */
        else if (index == 67) {

            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere\nto throw a ball"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // Here's a simple pause button and pause scene
            level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Game Paused"), 0);
            level.addTapControl(0, 300, 20, 20, "red.png", level.PauseAction);

            // draw a hero, and a button for throwing projectiles in many
            // directions. Note that this is going to look like an "asteroids"
            // game, with a hero covering the bottom of the screen, so that
            // anything that falls to the bottom counts against the player
            Hero h = level.makeHeroAsBox(1, 0, 46, 1, "greenball.png");
            level.addDirectionalThrowButton(0, 0, 960, 640, "", h, 100, 0, 1);

            // set up our pool of projectiles, then set them to have a fixed
            // velocity when using the vector throw mechanism
            level.configureProjectiles(100, 1, 1, "greyball.png", 1, 0, true);
            level.setProjectileRange(50);
            level.setFixedVectorThrowVelocityForProjectiles(5);

            // we're going to win by "surviving" for 25 seconds... with no
            // enemies, that shouldn't be too hard
            level.setWinCountdown(25, "You Survived!");
            level.addDisplay(28, 250, "arial.ttf", "#C0C0C0", 16, "", "", level.DisplayWinCountdown, 2);
            // just to play it safe, let's say that we win on level...
            // this ensures that collecting goodies or defeating enemies won't
            // accidentally cause us to win. Of course, with no destination,
            // there's no way to win now, except surviving.
            level.setVictoryDestination(1);
        }

        /*
         * We can make a hero hover, and then have it stop hovering when it is
         * flicked or moved via "touchToMove". This demonstrates the effect via
         * flick. It also shows that an enemy (or obstacle/goodie/destination)
         * can fall due to gravity.
         */
        else if (index == 68) {

            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Flick the hero into the destination"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsBox(21, 23, 3, 3, "greenball.png");
            h.setHover(21, 23);
            h.setFlickable(0.7f);

            // place an enemy, let it fall
            Enemy e = level.makeEnemyAsCircle(31, 25, 3, 3, "redball.png");
            e.setCanFall();

            level.makeDestinationAsCircle(25, 25, 5, 5, "mustardball.png");
            level.setVictoryDestination(1);
        }

        /*
         * The default behavior is for a hero to be able to jump any time it
         * collides with an obstacle. This isn't, of course, the smartest way to
         * do things, since a hero in the air shouldn't jump. One way to solve
         * the problem is by altering the presolve code in level.java. Another
         * approach, which is much simpler, is to mark some walls so that the
         * hero doesn't have jump re-enabled upon a collision.
         */
        else if (index == 69) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero to\nmake it jump"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);

            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.5f, 0, 0.6f);
            h.setMoveByTilting();
            h.setTouchToJump();
            h.setJumpImpulses(0, 15);
            level.setCameraChase(h);

            // hero can jump while on this obstacle
            level.makeObstacleAsBox(10, 3, 10, 1, "red.png");

            // hero can't jump while on this obstacle
            Obstacle o = level.makeObstacleAsBox(40, 3, 10, 1, "red.png");
            o.setReJump(false);
        }

        /*
         * When something chases an entity, we might not want it to chase in
         * both the X and Y dimensions... this shows how we can chase in a
         * single direction.
         */
        else if (index == 70) {
            // set up a simple level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("You can walk through the wall"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setMoveByTilting();

            level.makeDestinationAsCircle(42, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // These obstacles chase the hero, but only in one dimension
            Obstacle e = level.makeObstacleAsCircle(0, 0, 1, 1, "red.png");
            e.setChaseSpeed(15, h, false, true);
            e.setCollisionsEnabled(true);
            Obstacle e2 = level.makeObstacleAsCircle(0, 0, 1, 1, "red.png");
            e2.setChaseSpeed(15, h, true, false);
            e2.setCollisionsEnabled(true);

            // Here's a wall, and a movable round obstacle
            Obstacle o = level.makeObstacleAsBox(40, 1, .5f, 20, "red.png");
            Obstacle o2 = level.makeObstacleAsCircle(8, 8, 2, 2, "blueball.png");
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
        else if (index == 71) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Poke the hero, then\n where you want it\nto go."), 0);

            // This hero moves via poking. the "false" means that we don't have
            // to poke hero, poke location, poke hero, poke location, ...
            // Instead, we can poke hero, poke location, poke location. the
            // first "true" means that as we drag our finger, the hero will
            // change its direction of travel. The second "true" means the hero
            // will stop immediately when we release our finger.
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "legstar1.png");
            h.setDefaultAnimation(level.makeAnimation(200, true, "legstar1.png", "legstar1.png"));
            h.setDefaultReverseAnimation(level.makeAnimation(200, true, "fliplegstar8.png", "fliplegstar8.png"));
            h.setPokePath(4, false);

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // sometimes a control needs to have a large touchable area, but a
            // small image. One way to do it is to make an invisible control,
            // then put a picture on top of it. This next line shows how to draw
            // a picture on the HUD
            level.addImage(2, 2, 2, 2, "red.png");
        }

        /*
         * It can be useful to make a Hero stick to an obstacle. As an example,
         * if the hero should stand on a platform that moves along a route, then
         * we will want the hero to "stick" to it, even as the platform moves
         * downward.
         */
        else if (index == 72) {

            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press screen borders\nto move the hero"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            // give a little friction, to help the hero stick to platforms
            h.setPhysics(2, 0, .5f);

            // create a destination
            level.makeDestinationAsCircle(20, 15, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // This obstacle is sticky on top... Jump onto it and watch what
            // happens
            Obstacle o = level.makeObstacleAsBox(10, 5, 8, .5f, "red.png");
            o.setRoute(new Route(5).to(10, 5).to(5, 15).to(10, 25).to(15, 15).to(10, 5), 5, true);
            o.setPhysics(100, 0, .1f);
            o.setSticky(true, false, false, false);

            // This obstacle is not sticky... it's not nearly as much fun
            Obstacle o2 = level.makeObstacleAsBox(30, 5, 8, .5f, "red.png");
            o2.setRoute(new Route(5).to(30, 5).to(25, 15).to(30, 25).to(45, 15).to(30, 5), 5, true);
            o2.setPhysics(100, 0, 1f);

            // draw some buttons for moving the hero
            level.addToggleButton(0, 5, 5, 22, "", level.makeXMotionAction(h, -5), level.makeXMotionAction(h, 0));
            level.addToggleButton(43, 5, 5, 22, "", level.makeXMotionAction(h, 5), level.makeXMotionAction(h, 0));
        }

        /*
         * When using "vector" projectiles, if the projectile isn't a circle we
         * might want to rotate it in the direction of travel. Also, this level
         * shows how to do walls that can be passed through in one direction.
         */
        else if (index == 73) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere\nto shoot a laserbeam"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            level.makeDestinationAsCircle(42, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions. It
            // only covers half the screen, to show how such an effect would
            // behave
            level.addDirectionalThrowButton(0, 0, 480, 640, "", h, 100, 0, 0);

            // set up a pool of projectiles with fixed velocity, and with
            // rotation
            level.configureProjectiles(100, .1f, 3, "red.png", 1, 0, false);
            level.setFixedVectorThrowVelocityForProjectiles(10);
            level.setRotateVectorThrowForProjectiles();

            // create a box that is easy to fall into, but hard to getLoseScene out of,
            // by making its sides each "one-sided"
            Obstacle bottom = level.makeObstacleAsBox(10, 10, 10, .2f, "red.png");
            bottom.setOneSided(2);
            Obstacle left = level.makeObstacleAsBox(10, 10, .2f, 10, "red.png");
            left.setOneSided(1);
            Obstacle right = level.makeObstacleAsBox(20, 10, .2f, 10, "red.png");
            right.setOneSided(3);
            Obstacle top = level.makeObstacleAsBox(10, 25, 10, .2f, "red.png");
            top.setOneSided(0);
        }

        /*
         * This level shows how to use multiple types of goodie scores
         */
        else if (index == 74) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Green, Red, and Grey\nballs are goodies"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setMoveByTilting();

            // the destination requires lots of goodies of different types
            Destination d = level.makeDestinationAsCircle(42, 31, 2, 2, "mustardball.png");
            d.setActivationScore(1, 1, 3, 0);
            level.setVictoryDestination(1);

            level.addDisplay(10, 110, "arial.ttf", "#00FFFF", 16, "", " blue", level.DisplayGoodies1, 2);
            level.addDisplay(10, 140, "arial.ttf", "#00FFFF", 16, "", " green", level.DisplayGoodies2, 2);
            level.addDisplay(10, 170, "arial.ttf", "#00FFFF", 16, "", " red", level.DisplayGoodies3, 2);

            level.setLoseCountdown(100, "");
            level.addDisplay(250, 30, "arial.ttf", "#000000", 32, "", "", level.DisplayLoseCountdown, 2);

            // draw the goodies
            for (int i = 0; i < 3; ++i) {
                Goodie b = level.makeGoodieAsCircle(10 * i, 30, 2, 2, "blueball.png");
                b.setScore(1, 0, 0, 0);
                Goodie g = level.makeGoodieAsCircle(10 * i + 2.5f, 30, 1, 1, "greenball.png");
                g.setScore(0, 1, 0, 0);
                Goodie r = level.makeGoodieAsCircle(10 * i + 6, 30, 1, 1, "redball.png");
                r.setScore(0, 0, 1, 0);
            }

            // When the hero collides with this obstacle, we'll increase the
            // time remaining. See onHeroCollideCallback()
            Obstacle o = level.makeObstacleAsBox(40, 0, 5, 200, "red.png");
            o.setHeroCollisionCallback(1, 1, 1, 0, 0, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    // add 15 seconds to the timer
                    level.updateTimerExpiration(15);
                    thisActor.remove(true);
                }
            });
        }

        /*
         * this level shows passthrough objects and chase again, to help
         * demonstrate how chase works
         */
        else if (index == 75) {
            // set up a simple level


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("You can walk through the wall"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setMoveByTilting();
            h.setPassThrough(7); // make sure obstacle has same value

            // the destination requires lots of goodies of different types
            level.makeDestinationAsCircle(42, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // the enemy chases the hero, but can't getLoseScene through the wall
            Enemy e = level.makeEnemyAsCircle(42, 1, 5, 4, "red.png");
            e.setChaseSpeed(1, h, true, true);

            Obstacle o = level.makeObstacleAsBox(40, 1, .5f, 20, "red.png");
            o.setPassThrough(7);
        }

        /*
         * We can have a control that increases the hero's speed while pressed,
         * and decreases it upon release
         */
        else if (index == 76) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere to speed up"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            level.makeDestinationAsCircle(120, 31, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            Hero h = level.makeHeroAsBox(2, 25, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            // give the hero a fixed velocity
            h.addVelocity(4, 0);
            // center the camera a little ahead of the hero
            h.setCameraOffset(15, 0);
            level.setCameraChase(h);

            // set up the background
            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // draw a turbo boost button that covers the whole screen... make
            // sure its "up" speeds match the hero velocity
            level.addToggleButton(0, 0, 960, 640, "", level.makeXYMotionAction(h, 15, 0), level.makeXYMotionAction(h, 4, 0));
        }

        /*
         * Sometimes, we want to make the hero move when we press a control, but
         * when we release we don't want an immediate stop. This shows how to
         * getLoseScene that effect.
         */
        else if (index == 77) {
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press anywhere to start moving"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            Hero h = level.makeHeroAsBox(2, 1, 3, 7, "greenball.png");
            h.setCameraOffset(15, 0);
            level.setCameraChase(h);

            level.setBackgroundColor("#17B4FF");
            level.addHorizontalBackgroundLayer(.5f, 1, "mid.png", 0, 960, 640);

            // This control has a dampening effect, so that on release, the hero
            // slowly stops
            level.addToggleButton(0, 0, 960, 640, "", level.makeXYDampenedMotionAction(h, 10, 0, 0), level.makeXYDampenedMotionAction(h, 10, 0, 1));
        }

        /*
         * One-sided obstacles can be callback obstacles. This allows, among
         * other things, games like doodle jump. This level shows how it all
         * interacts.
         */
        else if (index == 78) {

            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("One-sided + Callbacks"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();

            level.makeDestinationAsCircle(42, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // create a platform that we can jump through from above
            Obstacle platform = level.makeObstacleAsBox(10, 5, 10, .2f, "red.png");
            platform.setOneSided(2);
            // Set a callback, then re-enableTilt the platform's collision effect.
            // Be sure to check onHeroCollideCallback
            platform.setHeroCollisionCallback(0, 0, 0, 0, 0, new CollisionCallback() {
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    collideActor.setAbsoluteVelocity(collideActor.getXVelocity(), 5);
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
         * before it can be given a new level. Also, the hero will keep
         * moving after the screen is released. We will also show the Fact
         * interface.
         */
        else if (index == 79) {
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Poke the hero, then\n where you want it\nto go."), 0);

            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            h.setFingerChase(4, false, true);

            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // A callback control is a way to run arbitrary code whenever the
            // control is pressed. This is something of a catch-all for any sort
            // of behavior we might want. See onControlPressCallback().
            level.addTapControl(2, 2, 2, 2, "red.png", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("Current score " + level.getGoodiesCollected1()), 0);
                    level.getPauseScene().show();
                    level.incrementGoodiesCollected1();
                    return true;
                }
            });

            level.addDisplay(12, 2, "arial.ttf", "#000000", 12, "-", ".", level.DisplayLevelFact("level test"), 2);
            level.addDisplay(12, 4, "arial.ttf", "#000000", 12, "-", ".", level.DisplaySessionFact("session test"), 2);
            level.addDisplay(12, 6, "arial.ttf", "#000000", 12, "-", ".", level.DisplayGameFact("game test"), 2);
            level.addTapControl(2, 4.5f, 2, 2, "red.png", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.putLevelFact("level test", 1 + level.getLevelFact("level test", -1));
                    return true;
                }
            });
            level.addTapControl(2, 7, 2, 2, "red.png", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.putSessionFact("session test", 1 + level.getSessionFact("session test", -1));
                    return true;
                }
            });
            level.addTapControl(2, 9.5f, 2, 2, "red.png", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.putGameFact("game test", 1 + level.getGameFact("game test", -1));
                    return true;
                }
            });
        }

        /*
         * Sometimes we need to manually force an entity to be immune to
         * gravity.
         */
        else if (index == 80) {

            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Testing Gravity Defy?"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();

            Destination d = level.makeDestinationAsCircle(42, 14, 2, 2, "mustardball.png");
            // note: it must not be immune to physics (third parameter true), or
            // it will pass through the bounding box, but we do want it to move
            // and not fall downward
            d.setAbsoluteVelocity(-2, 0);
            d.setGravityDefy();
            level.setVictoryDestination(1);
        }

        /*
         * Test to show that we can have obstacles with a polygon shape
         */
        else if (index == 81) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Testing Polygons"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setMoveByTilting();

            level.makeDestinationAsCircle(42, 14, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // create a polygon obstacle
            Obstacle o = level.makeObstacleAsPolygon(10, 10, 2, 5, "blueball.png", -1, 2, -1, 0, 0, -3, 1, 0, 1, 1);
            o.setShrinkOverTime(1, 1, true);
        }

        /*
         * A place for playing with a side-scrolling platformer that has lots of
         * features
         */
        else if (index == 82) {
            // set up a standard side scroller with tilt:
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press the hero to\nmake it jump"), 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a simple jumping hero
            Hero h = level.makeHeroAsBox(5, 0, 2, 6, "greenball.png");
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            h.setMoveByTilting();
            level.setCameraChase(h);

            // This enemy can be defeated by jumping. Note that the hero's
            // bottom must be higher than the enemy's middle point, or the jump
            // won't defeat the enemy.
            Enemy e = level.makeEnemyAsCircle(15, 0, 5, 5, "redball.png");
            e.setDefeatByJump();
        }

        /*
         * Demonstrate the ability to set up paddles that rotate back and forth
         */
        else if (index == 83) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Avoid revolving obstacles"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero h = level.makeHeroAsCircle(5, 0, 2, 6, "greenball.png");
            h.setMoveByTilting();

            // Note: you must give density to the revolving part...
            Obstacle revolving = level.makeObstacleAsBox(20, 10, 2, 8, "red.png");
            revolving.setPhysics(1, 0, 0);
            Obstacle anchor = level.makeObstacleAsBox(20, 19, 2, 2, "blueball.png");

            revolving.setRevoluteJoint(anchor, 0, 0, 0, 6);
            revolving.setRevoluteJointLimits(1.7f, -1.7f);
            revolving.setRevoluteJointMotor(4, Float.POSITIVE_INFINITY);
            level.makeDestinationAsCircle(40, 30, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
        }

        /*
         * Demonstrate panning to view more of the level
         */
        else if (index == 84) {
            // set up a big screen
            level.setCameraBounds(4 * 48, 2 * 32);

            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The star rotates in\nthe direction of movement"), 0);
            level.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(29, 60, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            level.setCameraChase(h);

            // zoom buttons
            level.addTapControl(0, 0, 480, 640, "", level.ZoomOutAction(8));
            level.addTapControl(480, 0, 480, 640, "", level.ZoomInAction(.25f));

            // turn on panning
            level.addPanControl(0, 0, 960, 640, "");
        }

        /*
         * Demonstrate pinch-to-zoom, and also demonstrate one-time callback
         * controls
         */
        else if (index == 85) {
            // set up a big screen
            level.setCameraBounds(4 * 48, 2 * 32);

            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("The star rotates in\nthe direction of movement"), 0);
            level.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(29, 60, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "legstar1.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            level.setCameraChase(h);

            // turn on pinch zoomg
            level.addPinchZoomControl(0, 0, 960 / 20f, 640 / 20f, "", 8, .25f);

            // add a one-time callback control
            level.addTapControl(2, 2, 2, 2, "blueball.png", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    if (!mIsActive)
                        return false;
                    mIsActive = false;
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 20, "", "", level.DisplayFixedText("you can only pause once..."), 0);
                    level.getPauseScene().show();
                    this.mSource.setImage("greenball.png");
                    return true;
                }
            });
        }

        /*
         * Demonstrate some advanced controls
         */
        else if (index == 86) {
            // set up a screen

            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(29, 30, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            final Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            level.setCameraChase(h);
            h.setDamping(1);
            h.setAngularDamping(2);

            // Set up a control that rotates, and when tapped, stops rotating and gives its rotation
            // to the hero
            final TouchEventHandler rotatorSC = new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    if (this.mIsActive) {
                        h.setRotation(this.mSource.getRotation() % (2 * (float) Math.PI));
                        this.mSource.setRotationSpeed(0);
                        this.mIsActive = false;
                        return true;
                    }
                    return false;
                }
            };
            final SceneActor rotator = level.addTapControl(10.75f, 6.75f, 2.5f, 2.5f, "legstar1.png", rotatorSC);
            rotator.setRotationSpeed(2);

            // Set up a control that gets bigger and smaller, to indicate a value that changes
            // between 0 and 100
            TouchEventHandler barSC = new TouchEventHandler() {
                public boolean go(float eventPositionX, float eventPositionY) {
                    if (mSource.getInfoInt() == -200)
                        return false;
                    this.mIsActive = false;
                    // get the rotation and the magnitude
                    float rotation = h.getRotation();
                    float magnitude = mSource.getInfoInt();
                    // create a unit vector
                    Vector2 v = new Vector2(1, 0);
                    v.rotate(rotation * 180 / (float) Math.PI + 90);
                    v.scl(magnitude);
                    h.setAbsoluteVelocity(v.x, v.y);
                    mSource.setInfoInt(-200);
                    return true;
                }
            };
            final SceneActor bar = level.addTapControl(23.5f, 0, .5f, 16f, "greenball.png", barSC);
            // make the bar change size over time
            // We will use bar's attached integer to get this to work.  When the absolute value
            // is in the range 0,100, it indicates the percentage to show.  When it is negative, we
            // are shrinking.  When it is -200, it is disabled.
            bar.setInfoInt(0);
            level.setTimerCallback(.1f, .1f, new LolAction() {
                @Override
                public void go() {
                    int i = bar.getInfoInt();
                    if (i == -200)
                        return;
                    bar.setFlipAndClipRatio(0, 0, 1, Math.abs(i / 100f));
                    i = (i == 100) ? -100 : i + 1;
                    bar.setInfoInt(i);
                }
            });

            // when the hero stops, start the controls again
            h.setStopCallback(new LolActorEvent() {
                public void go(WorldActor self) {
                    rotator.setRotationSpeed(2);
                    rotatorSC.mIsActive = true;
                    bar.setInfoInt(0);
                }
            });
        }

        /*
         * Weld joints
         */
        else if (index == 87) {
            // set up a screen


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            level.makeDestinationAsCircle(29, 30, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // set up a hero and fuse an obstacle to it
            Hero h = level.makeHeroAsCircle(4, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Obstacle o = level.makeObstacleAsCircle(1, 1, 1, 1, "blueball.png");
            o.setCanFall();
            h.setWeldJoint(o, 3, 0, 0, 0, 45);
        }

        /*
         * Demonstrate that we can have callback buttons on PauseScenes
         */
        else if (index == 88) {


            level.enableTilt(10, 10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Interactive Pause Scenes\n(click the red square)"), 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // Demonstrate the ability to chase while keeping existing velocity
            // in one direction
            Obstacle o = level.makeObstacleAsCircle(15, 15, 2, 2, "purpleball.png");
            o.setAbsoluteVelocity(5, 1);
            o.setChaseFixedMagnitude(h, 3, 0, false, true);

            // Create a pause scene that has a back button on it, and a button
            // for pausing the level
            level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Game Paused"), 0);
            level.getPauseScene().addTapControl(0, 30, 2, 2, "red.png", new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    level.doBack();
                    return true;
                }
            }, 0);

            level.getPauseScene().addTapControl(1, 1, 1, 1, "red.png", new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    level.winLevel();
                    return true;
                }
            }, 0);
            level.getPauseScene().addTapControl(9.5f, 9.5f, 1, 1, "red.png", new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    level.loseLevel();
                    return true;
                }
            }, 0);
            level.getPauseScene().suppressClearClick();
            level.addTapControl(0, 15, 1, 1, "red.png", level.PauseAction);
        }

        /*
         * Use multiple heroes to combine positive and negative results
         */
        else if (index == 89) {

            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // now let's draw two heroes who can both move by tilting, and
            // who both have density and friction. Note that we lower the
            // density, so they move faster
            Hero h1 = level.makeHeroAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            h1.setJumpImpulses(0, 10);
            h1.setTouchToJump();
            h1.setMustSurvive();
            Hero h2 = level.makeHeroAsBox(0, 0, 48, .1f, "");
            h2.setMustSurvive();
            h1.setPassThrough(1);
            h2.setPassThrough(1);

            Enemy e1 = level.makeEnemyAsCircle(29, 29, 1, 1, "redball.png");
            e1.setKinematic();
            e1.setAbsoluteVelocity(0, -1);

            // notice that now we will make two destinations, each of which
            // defaults to only holding ONE hero, but we still need to getLoseScene two
            // heroes to destinations in order to complete the level
            level.makeDestinationAsCircle(29, 6, 2, 2, "mustardball.png");
        }

        /*
         * Demonstrate that we can save entities so that we can access them from
         * a callback
         */
        else if (index == 90) {

            level.resetGravity(0, -10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 5);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Keep pressing until\na hero makes it to\nthe destination"), 0);

            for (int i = 0; i < 10; ++i) {
                Hero h = level.makeHeroAsBox(4 * i + 2, 0.1f, 2, 2, "greenball.png");
                h.setPhysics(1, 1, 5);
                level.putLevelActor("" + i, h);
            }

            level.makeDestinationAsCircle(29, 16, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            // A callback control is a way to run arbitrary code whenever the
            // control is pressed. This is something of a catch-all for any sort
            // of behavior we might want. See onControlPressCallback().
            level.addTapControl(0, 0, 960, 640, "", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    for (int i = 0; i < 10; ++i) {
                        WorldActor p = level.getLevelActor("" + i);
                        if (p != null) {
                            p.setAbsoluteVelocity(5 - level.getRandom(10), 10);
                        }
                    }
                    return true;
                }
            });
        }

        /**
         * Demo a truck, using distance and revolute joints
         */
        else if (index == 91) {

            level.resetGravity(0, -10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);

            Hero truck = level.makeHeroAsBox(3, 3, 4, 1.5f, "red.png");
            truck.setPhysics(1, 0, 0);
            Obstacle head = level.makeObstacleAsCircle(4.5f, 4, 1, 1, "blueball.png");
            head.setPhysics(1, 0, 0);
            Obstacle backWheel = level.makeObstacleAsCircle(3, 2, 1.5f, 1.5f, "blueball.png");
            backWheel.setPhysics(3, 0, 1);
            Obstacle frontWheel = level.makeObstacleAsCircle(5.5f, 2, 1.5f, 1.5f, "blueball.png");
            frontWheel.setPhysics(3, 0, 1);

            backWheel.setRevoluteJoint(truck, -1.5f, -1, 0, 0);
            backWheel.setRevoluteJointMotor(-10f, 10f);
            frontWheel.setRevoluteJoint(truck, 1.5f, -1, 0, 0);
            frontWheel.setRevoluteJointMotor(-10f, 10f);

            // this is not how we want the head to look, but it makes for a nice
            // demo
            head.setDistanceJoint(truck, 0, 1, 0, 0);

            level.makeDestinationAsBox(47, 0, .1f, 32, "");
            level.setVictoryDestination(1);
        }

        /**
         * Demonstrate how we can chain pausescenes together, and also show how to use particle
         * effects
         */
        else if (index == 92) {
            // start with a basic tilt-based side-scroller
            level.setCameraBounds(3 * 48, 32);
            level.resetGravity(0, -10);
            level.enableTilt(10, 0);
            level.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            level.makeDestinationAsCircle(120, 1, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);
            level.setCameraChase(h);

            // put some flame effects on a black background
            level.setBackgroundColor("#000000");
            for (int i = 5; i < 150; i += 15) {
                Effect e = level.makeParticleSystem("flame.txt", -2, i, 5);
                e.setRepeat(true);
            }

            // here's a weak attempt at snow
            Effect e = level.makeParticleSystem("snow.txt", 2, 15, 40);
            e.setRepeat(true);
            e = level.makeParticleSystem("snow.txt", 2, 55, 40);
            e.setRepeat(true);
            e = level.makeParticleSystem("snow.txt", 2, 85, 40);
            e.setRepeat(true);
            // the trick for getting one PauseScene's dismissal to result in another PauseScene
            // drawing right away is to use the PauseScene CallbackButton facility.  When the first
            // PauseScene is touched, we dismiss it and immediately draw another PauseScene

            // set up a simple PauseScene
            level.getPauseScene().reset();
            level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("test"), 0);
            // this is the code to run when the *second* pausescene is touched.  Making it "final"
            // means that we can refer to it inside of the other callback
            // TODO: make these into TouchEventHandlers?
            final LolAction sc2 = new LolAction() {
                public void go() {
                    level.getPauseScene().dismiss();
                }
            };
            // this is the code to run when the *first* pausescene is touched
            final LolAction sc1 = new LolAction() {
                public void go() {
                    // clear the pausescene, draw another one
                    level.getPauseScene().reset();
                    level.getPauseScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("test2"), 0);
                    level.getPauseScene().addTapControl(0, 0, 960 / 20f, 640 / 20f, "", new TouchEventHandler() {
                        @Override
                        public boolean go(float eventX, float eventY) {
                            sc2.go();
                            return true;
                        }
                    }, 0);
                    level.getPauseScene().show();
                }
            };
            // set the callback for the first pausescene, and show it
            level.getPauseScene().addTapControl(0, 0, 960 / 20f, 640 / 20f, "", new TouchEventHandler() {
                @Override
                public boolean go(float eventX, float eventY) {
                    sc1.go();
                    return true;
                }
            }, 0);
            level.getPauseScene().show();

            level.addTapControl(0, 0, 480, 640, "", level.ZoomOutAction(8));
            level.addTapControl(480, 0, 480, 640, "", level.ZoomInAction(.25f));

        }

        // Show how to make an "infinite" level, and add a foreground layer
        else if (index == 93) {
            // set up a standard side scroller with tilt, but make it really really long:
            level.setCameraBounds(300000, 32);
            level.resetGravity(0, -10);
            level.getPreScene().addTextCentered(24, 16, "arial.ttf", "#FFFFFF", 32, "", "", level.DisplayFixedText("Press to make\nthe hero go up"), 0);
            level.drawBoundingBox(0, 0, 300000, 32, "red.png", 0, 0, 0);

            // make a hero
            Hero h = level.makeHeroAsCircle(2, 2, 3, 3, "greenball.png");
            level.setCameraChase(h);
            h.setAbsoluteVelocity(10, 0);
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);

            // touching the screen makes the hero go upwards
            level.addToggleButton(0, 0, 960, 640, "", level.makeYMotionAction(h, 20), level.makeYMotionAction(h, 0));

            // set up our background, with a few layers
            level.setBackgroundColor("17B4FF");
            level.addHorizontalBackgroundLayer(0, 1, "back.png", 0, 960, 640);
            level.addHorizontalForegroundLayer(.5f, 1, "mid.png", 0, 480, 320);
            level.addHorizontalBackgroundLayer(1.25f, 1, "front.png", 20, 454, 80);

            // we win by collecting 10 goodies...
            level.setVictoryGoodies(10, 0, 0, 0);
            level.addDisplay(15, 600, "arial.ttf", "#FFFFFF", 20, "", " goodies", level.DisplayGoodies1, 2);

            // now set up an obstacle and attach a callback to it
            //
            // Note that the obstacle needs to be final or we can't access it within the callback
            final Obstacle trigger = level.makeObstacleAsBox(30, 0, 1, 32, "");
            CollisionCallback lc = new CollisionCallback() {
                /**
                 * Each time the hero hits the obstacle, we'll run this code to draw a new enemy
                 * and a new obstacle on the screen.  We'll randomize their placement just a bit.
                 * Also move the obstacle forward, so we can hit it again.
                 */
                public void go(WorldActor thisActor, WorldActor collideActor, Contact contact) {
                    // make a random enemy and a random goodie.  Put them in X coordinates relative to the trigger
                    level.makeEnemyAsCircle(trigger.getXPosition() + 40 + level.getRandom(10), level.getRandom(30), 2, 2, "redball.png");
                    level.makeGoodieAsCircle(trigger.getXPosition() + 50 + level.getRandom(10), level.getRandom(30), 2, 2, "blueball.png");
                    // move the trigger so we can hit it again
                    trigger.setPosition(trigger.getXPosition() + 50, trigger.getYPosition());
                }
            };
            trigger.setHeroCollisionCallback(0, 0, 0, 0, 0, lc);
            // No transfer of momeuntum when the hero collides with the trigger
            trigger.setCollisionsEnabled(false);
        }
        // Test of tap and toggle obstacles
        // TODO: make this a better test
        else if (index == 94) {


            level.enableTilt(10, 10);
            level.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            Hero h = level.makeHeroAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();
            level.makeDestinationAsCircle(29, 26, 2, 2, "mustardball.png");
            level.setVictoryDestination(1);

            Obstacle o = level.makeObstacleAsBox(10, 10, 3, 3, "red.png");
            o.setToggleCallback(new LolAction() {
                @Override
                public void go() {
                    System.out.println("ooh, yeah, I'm downpressed");
                }
            }, new LolAction() {
                @Override
                public void go() {
                    System.out.println("not anymore");
                }
            });

            Obstacle o2 = level.makeObstacleAsBox(20, 20, 3, 3, "red.png");
            o2.setTapCallback(new TouchEventHandler() {
                @Override
                public boolean go(float eventPositionX, float eventPositionY) {
                    System.out.println("I was tapped.  yes!");
                    return true;
                }
            });
        }
    }
}
