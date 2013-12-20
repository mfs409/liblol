
package com.me.mylolgame;

// TODO: demonstrate the simple animation technique

// TODO: demonstrate obstacle._peer?

// TODO: Demo projectile setCollisionOk?

// TODO: Verify that mp3 and jpg files work

// TODO: demo setEnemyCollideTriggerDelay and setEnemyJump (do we need setEnemyJump?)

// TODO: Level 70 is no longer valid. We can use it as a home for a new demo

// TODO: level 28 accelerometer trick no longer applies. Altering the order of popup and accel in Level.render() would
// restore the behavior, but the behavior is actually not desirable.

// TODO: test if x and y parallax works as expected (swimming level with >32 Y?)

// TODO: woowoowoo.ogg doesn't seem to play on Desktop, but works on Phone (see level 20)

// TODO: need a demo to show that we can chase in just X or just Y

// TODO: consider making a tool for proper sprite sheet manipulation

// TODO: test Tilt in portrait mode, and test if upside-down screens work (landscape and portrait)

import edu.lehigh.cse.lol.*;

public class MyLolGame extends LOL {
    @Override
    public void nameResources() {
        // load regular (non-animated) images
        Media.registerImage("greenball.png");
        Media.registerImage("mustardball.png");
        Media.registerImage("red.png");
        Media.registerImage("redball.png");
        Media.registerImage("blueball.png");
        Media.registerImage("purpleball.png");
        Media.registerImage("msg1.png");
        Media.registerImage("msg2.png");
        Media.registerImage("fade.png");
        Media.registerImage("greyball.png");

        // load the image we show on the main screen
        Media.registerImage("splash.png");

        // parallax stuff
        Media.registerImage("mid.png");
        Media.registerImage("front.png");
        Media.registerImage("back.png");

        // animated images
        Media.registerAnimatableImage("stars.png", 8, 1);
        Media.registerAnimatableImage("flystar.png", 2, 1);
        Media.registerAnimatableImage("starburst.png", 4, 1);
        Media.registerAnimatableImage("colorstar.png", 8, 1);

        // sounds
        Media.registerSound("hipitch.ogg");
        Media.registerSound("lowpitch.ogg");
        Media.registerSound("losesound.ogg");
        Media.registerSound("slowdown.ogg");
        Media.registerSound("woowoowoo.ogg");
        Media.registerSound("fwapfwap.ogg");
        Media.registerSound("winsound.ogg");

        // Background music
        Media.registerMusic("tune.ogg", true);
    }

    @Override
    public void configureLevel(int whichLevel) {
        /**
         * @level: 1
         * @description: In this level, all we have is a hero (the green ball)
         *               who needs to make it to the destination (a mustard
         *               colored ball). The game is configured to use tilt to
         *               control the hero.
         * @whatsnew: control a hero with tilt
         * @whatsnew: win by reaching a destination
         */
        if (whichLevel == 1) {
            // set the screen to 48 meters by 32 meters
            Level.configure(48, 32);
            // there is no default force pushing everything
            Physics.configure(0, 0);

            // in this level, we'll use tilt to move some things around. The
            // maximum force that tilt can exert on anything is 10 in the X
            // dimension, and 10 in the Y dimension
            Tilt.enable(10, 10);

            // now let's create a hero, and indicate that the hero can move by
            // tilting the phone.
            Hero h = Hero.makeAsCircle(4, 17, 3, 3, "greenball.png");
            h.setMoveByTilting();

            // finally, let's draw a circular destination
            Destination.makeAsCircle(29, 26, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /**
         * @level: 2
         * @description: In this level, we add a few features to level 1 to make
         *               it a bit more playable. Note, though, that since
         *               nothing has physics properties attached to it, the
         *               behavior when the ball crashes into the bounding box
         *               doesn't really feel right.
         * @whatsnew: draw a bounding box around the level, so the hero can't
         *            fall into infinity
         * @whatsnew: custom text when the level is won
         * @whatsnew: a pop up scene shows for one second at the start of the
         *            level
         */
        else if (whichLevel == 2) {
            // start by setting everything up just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // new: add a bounding box so the hero can't fall off the screen
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // new: change the text that we display when the level is won
            PostScene.setDefaultWinText("Good job!");

            // new: add a pop-up message that shows for one second at the
            // beginning of the level
            PreScene.addText("Reach the destination\nto win this level.", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            PreScene.addImage("red.png", 50, 50, 10, 1);
        }

        /**
         * @level: 3
         * @description: In this level, we change the physics from level 2 so
         *               that things roll and bounce a little bit more nicely
         * @whatsnew: the hero and bounding box now have nonzero physics so that
         *            things are a little bit more smooth
         * @whatsnew: we don't set the win text anymore, so now when we win
         *            we'll go back to the default message
         */
        else if (whichLevel == 3) {
            // start by setting up the level just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            // new: give the hero some density and friction, so that it can roll
            // when it encounters a wall... notice that once it has density, it
            // has mass, and it moves a lot slower...
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // new: the bounding box now also has nonzero density,
            // elasticity, and friction
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            PreScene.addText("Reach the destination\nto win this level.", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            PreScene.setExpire(2);
        }

        /**
         * @level: 4
         * @description: This level shows that we the natural behavior is for
         *               each destination to only hold one hero
         * @whatsnew: we are showing a popup image instead of popup text
         * @whatsnew: there are two heroes
         * @whatsnew: it takes two heroes to reach destinations before the level
         *            is won
         */
        else if (whichLevel == 4) {

            // start by setting up the level boundaries, tilt, and bounding box
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // now let's draw two heroes who can both move by tilting, and
            // who both have density and friction
            //
            // NB: less density, so faster movement
            Hero h1 = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h1.setPhysics(.1f, 0, 0.6f);
            h1.setMoveByTilting();
            Hero h2 = Hero.makeAsCircle(14, 7, 3, 3, "greenball.png");
            h2.setPhysics(.1f, 0, 0.6f);
            h2.setMoveByTilting();

            // notice that now we will make two destinations, each of which
            // defaults to only holding ONE hero, but we
            // still need to get two heroes to destinations in order to complete
            // the level
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Destination.makeAsCircle(29, 26, 1, 1, "mustardball.png");
            Score.setVictoryDestination(2);

            // Let's show msg1.png instead of text. Note that we had to
            // register it in registerMedia()
            PreScene.addImage("msg1.png", 0, 0, 460, 320);
        }

        /**
         * @level: 5
         * @description: This level demonstrates that we can have many heroes
         *               that can reach the same destination
         * @whatsnew: the destination can hold two heroes
         * @whatsnew: when the hero reaches the destination, we play a sound
         */
        else if (whichLevel == 5) {
            // begin by configuring the level and heroes just like in level 4
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

            // now let's make a destination, but indicate that it can hold TWO
            // heroes
            Destination d = Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            d.setHeroCount(2);

            // let's also say that whenever a hero reaches the destination, a
            // sound will play
            d.setArrivalSound("hipitch.ogg");

            // Indicate that two heroes have to reach destinations in order to
            // finish the level
            Score.setVictoryDestination(2);

            // Change the pop-up message slightly
            PreScene.addText("All heroes must\nreach the destination", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
        }

        /**
         * @level: 6
         * @description: this demonstrates an alternative way of using tilt.
         *               Instead of tilt representing a force applied to
         *               entities, tilt now represents a velocity to apply to
         *               those entities. Note that this form of tilt behaves
         *               oddly when we have friction on the ball and wall.
         * @whatsnew: using tilt as velocity instead of tilt as force
         */
        else if (whichLevel == 6) {

            // configure a basic level, just like the start of level 2:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // change the behavior or tilt
            Tilt.setAsVelocity(true);

            // and print a popup to tell the user what's going on...
            PreScene.addText("A different way\nto use tilt.", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
        }

        /**
         * @level: 7
         * @description: This level adds an enemy, to demonstrate that we can
         *               make it possible to lose a level
         * @whatsnew: there is a stationary enemy
         * @whatsnew: a pop-up message that stays until it is pressed
         */
        else if (whichLevel == 7) {
            // configure a basic level, just like the start of level 2:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw an enemy:
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);

            // display a message that stays until it is pressed
            PreScene.addText("Avoid the enemy and\nreach the destination", 50, 50, 255, 255, 255,
                    "arial.ttf", 20);

            PostScene.disable();
        }

        /**
         * @level: 8
         * @description: This level explores a bit more of what we can do with
         *               enemies, by having an enemy with a fixed path.
         * @whatsnew: make a moveable enemy and attach a fixed path to it
         */
        else if (whichLevel == 8) {
            // configure a basic level, just like the start of level 2:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw an enemy that can move
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(100.0f, 0.3f, 0.6f);

            // attach a path to the enemy. It starts at (25, 25) and moves to
            // (25, 2). This means it has *2* points on its route. Notice that
            // since it loops, it is not going to gracefully move back to its
            // starting point

            e.setRoute(new Route(2).to(25, 25).to(25, 2), 10, true);

            // display a message that stays until it is pressed
            PreScene.addText("Avoid the enemy and\nreach the destination", 50, 50, 255, 255, 255,
                    "arial.ttf", 20);
        }

        /**
         * @level: 9
         * @description: This level explores a bit more of what we can do with
         *               paths.
         * @whatsnew: The path end point is the same as the start point, so that
         *            we get cleaner movement
         */
        else if (whichLevel == 9) {
            // configure a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw an enemy that can move
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);

            // attach a path to the enemy. This time, we add a third point,
            // which is the same as the starting point. This will give us a
            // nicer sort of movement. Also note the diagonal movement.
            e.setRoute(new Route(3).to(25, 25).to(12, 2).to(25, 25), 2, true);
            // note that any number of points is possible... you could have
            // extremely complex shapes!

            // display a message that stays until it is pressed
            PreScene.addText("Avoid the enemy and\nreach the destination", 50, 50, 255, 255, 255,
                    "arial.ttf", 20);
        }

        /**
         * @level: 10
         * @description: This level fleshes out a bunch of additional enemy
         *               features, as well as sound features
         * @whatsnew: any entity can be controlled by tilt, so here we add an
         *            enemy that is controlled by tilt.
         * @whatsnew: any entity can have a continuous rotation, so we do that
         *            to the Destination
         * @whatsnew: this level also demonstrates background music, and sounds
         *            to play on victory or defeat.
         * @whatsnew: this level shows a popup message that uses an image, and
         *            that does not go away until it is touched
         * @whatsnew: there is a custom message when the level is lost
         */
        else if (whichLevel == 10) {
            // configure a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(0.1f, 0, 0.6f);
            h.setMoveByTilting();

            // update: let's make the destination rotate:
            Destination d = Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            d.setRotationSpeed(1);
            Score.setVictoryDestination(1);

            // update: draw an enemy who moves via tilt
            Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(1.0f, 0.3f, 0.6f);
            e3.setMoveByTilting();

            // show a message that must be touched in order to remove it
            PreScene.addImage("msg2.png", 10, 10, 460, 320);

            // configure some sounds
            PostScene.setWinSound("winsound.ogg");
            PostScene.setLoseSound("losesound.ogg");
            Level.setMusic("tune.ogg");

            // custom text for when the level is lost
            PostScene.setDefaultLoseText("Better luck next time...");
        }

        /**
         * @level: 11
         * @description: This shows how it is possible to have a level that is
         *               larger than one screen. It also demonstrates zoom
         *               buttons, and introduces the notion of invisibile
         *               controls.
         * @whatsnew: zoom buttons on the left and right halves of the screen
         * @whatsnew: the level is larger than the screen, but the screen always
         *            shows the hero
         */
        else if (whichLevel == 11) {

            // make the level really big
            Level.configure(400, 300);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 400, 300, "red.png", 0, 0, 0);

            PreScene.addText("Press left to zoom out\nright to zoom in", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);

            // put the hero and destination far apart
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(329, 281, 10, 10, "mustardball.png");
            Score.setVictoryDestination(1);

            // add zoom buttons
            Controls.addZoomInButton(240, 0, 240, 320, "", .25f);
            Controls.addZoomOutButton(0, 0, 240, 320, "", 8);

            // chase the hero with the camera
            Level.setCameraChase(h);
        }

        /**
         * @level: 12
         * @description: this level introduces obstacles, and also shows the
         *               difference between "box" and "circle" physics
         * @whatsnew: stationary obstacles
         * @whatsnew: examples of box vs. circle physics
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
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // let's draw an obstacle whose underlying shape is a box, but whose
            // picture is a circle. This can be odd... our hero can roll around
            // an invisible corner on this obstacle
            Obstacle o1 = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(1, 0, 1);

            // now let's draw an obstacle whose shape and picture are both
            // circles. The hero rolls around this nicely.
            Obstacle o2 = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(1, 0, 1);

            // now let's draw a wall using circle physics and a stretched
            // rectangular
            // picture. This wall will do really funny things
            Obstacle o3 = Obstacle.makeAsCircle(20, 25, 6, 0.5f, "red.png");
            o3.setPhysics(1, 0, 1);

            // now let's draw a rectangular wall the right way, as a box
            Obstacle o4 = Obstacle.makeAsBox(34, 2, 0.5f, 20, "red.png");
            o4.setPhysics(1, 0, 1);

            // print a popup about this level
            PreScene.addText("An obstacle's appearance may\nnot match its physics", 50, 50, 255,
                    255, 255, "arial.ttf", 32);
        }

        /**
         * @level: 13
         * @description: this level just plays around with physics a little bit,
         *               to show how friction and elasticity can do interesting
         *               things.
         */
        else if (whichLevel == 13) {
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("These obstacles have\ndifferent physics\nparameters", 50, 50, 255,
                    255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Obstacle o1 = Obstacle.makeAsCircle(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(0, 100, 0);
            Obstacle o2 = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(10, 0, 100);
            Score.setVictoryDestination(1);
        }

        /**
         * @level: 14
         * @description: This level introduces goodies. Goodies are something
         *               that we collect. We can make the collection of goodies
         *               lead to changes in the behavior of the game, and in
         *               this example, the collection of goodies "enables" a
         *               destination.
         * @whatsnew: introduce goodies
         * @whatsnew: destination does not work until enough goodies are
         *            collected
         * @whatsnew: display the goodie count on the heads-up display
         */
        else if (whichLevel == 14) {

            // set up a basic level with a tilt-based hero
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            // now let's add some stationary goodies. Note that the default is
            // for goodies to have a circle as their shape, and to not cause a
            // change in the hero's behavior at the time when a collision
            // occurs... we can play with this later :)
            //
            // Note that goodies can have one of 4 "counts". By default, a
            // goodie counts as type 1.
            Goodie.makeAsCircle(0, 30, 1, 1, "blueball.png");
            Goodie.makeAsCircle(0, 15, 1, 1, "blueball.png");

            // here we create a destination. Note that we now set its activation
            // score to 2, so that you must
            // collect two goodies before the destination will "work"
            Destination d = Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            d.setActivationScore(2, 0, 0, 0);

            Score.setVictoryDestination(1);

            // let's put a display on the screen to see how many goodies we've
            // collected
            Controls.addGoodieCount1(2, "Goodies", 220, 280);
            PreScene.addText("You must collect\ntwo blue balls", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);

        }

        /**
         * @level: 15
         * @description: All entities can have a fixed motion path. Here, we'll
         *               attach a path to a few kinds of entities to show this
         *               ability
         * @whatsnew: show that destinations, goodies, and obstacles can have
         *            motion paths too
         * @whatsnew: also show how to change the color and size of on-screen
         *            text, and show how when the goodie counter's max field is
         *            0, the display is just the number of goodies collected
         */
        else if (whichLevel == 15) {
            // set up a basic tilt-based level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Every entity can move...", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(44, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            // make a destination that moves, and that requires one goodie to be
            // collected before it works
            Destination d = Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
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

            // draw a goodie counter in light blue with a 12-point font
            Controls.addGoodieCount1(0, "Goodies", 220, 280, "arial.ttf", 60, 70, 255, 12);
        }

        /**
         * @level: 16
         * @description: show how we can make a level that is won by collecting
         *               goodies, and show how we can set a time limit.
         * @whatsnew: victory by goodie count
         * @whatsnew: lose by running out of time
         */
        else if (whichLevel == 16) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Collect all\nblue balls\nto win", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 20, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw 5 goodies
            Goodie.makeAsCircle(.5f, .5f, 1, 1, "blueball.png");
            Goodie.makeAsCircle(5.5f, 1.5f, 1, 1, "blueball.png");
            Goodie.makeAsCircle(10.5f, 2.5f, 1, 1, "blueball.png");
            Goodie.makeAsCircle(15.5f, 3.5f, 1, 1, "blueball.png");
            Goodie.makeAsCircle(20.5f, 4.5f, 1, 1, "blueball.png");

            // indicate that we win by collecting enough goodies
            Score.setVictoryGoodies(5, 0, 0, 0);
            // put the goodie count on the screen
            Controls.addGoodieCount1(5, "Goodies", 220, 280);
            // put a countdown on the screen
            Controls.addCountdown(15, "Time Up!", 400, 50);

            PauseScene.addCenteredText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            Controls.addPauseButton(0, 300, 20, 20, "red.png");
        }

        /**
         * @level: 17
         * @description: This level shows how "obstacles" need not actually
         *               impede the hero's movement. Here, we attach
         *               "damping factors" to the hero, which let us make the
         *               hero speed up or slow down based on interaction with
         *               the obstacle. This level also adds a stopwatch.
         *               Stopwatches don't have any meaning, but they are nice
         *               to have anyway...
         * @whatsnew: dampening factors on obstacles
         * @whatsnew: added a stopwatch
         */
        else if (whichLevel == 17) {

            // set up a basic level with a tilt hero and a destination
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Obstacles as zoom\nstrips, friction pads\nand repellers", 50, 50,
                    255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // add a stopwatch... note that there are two ways to add a
            // stopwatch, the other of which allows for configuring the font
            Controls.addStopwatch(50, 50);

            PauseScene.addCenteredText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            PauseScene.addBackButton("red.png", 0, 300, 20, 20);
            Controls.addPauseButton(0, 300, 20, 20, "red.png");

            // now draw three obstacles. Note that they have different dampening
            // factors
            // one important thing to notice is that since we place these on the
            // screen *after* we
            // place the hero on the screen, the hero will go *under* these
            // things.

            // this obstacle's dampening factor means that on collision, the
            // hero's velocity is
            // multiplied by -1... he bounces off at an angle.
            Obstacle o = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setDamp(-1);

            // this obstacle accelerates the hero... it's like a turbo booster
            o = Obstacle.makeAsCircle(20, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setDamp(5);

            // this obstacle slows the hero down... it's like running on
            // sandpaper
            o = Obstacle.makeAsBox(30, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setRotationSpeed(2);
            o.setDamp(0.2f);
        }

        /**
         * @level: 18
         * @description: This level shows that it is possible to give heroes and
         *               enemies different strengths, so that a hero doesn't
         *               disappear after a single collision
         * @whatsnew: when an enemy defeats a hero, we can customize the message
         *            that prints
         * @whatsnew: setting the strength of heroes, and the damage caused by
         *            enemies. The default is for enemies to have "2" units of
         *            damage, and heroes to have "1" unit of strength, so that
         *            any collision defeats the hero without removing the enemy.
         * @whatsnew: show how much strength the hero has left. Note that if
         *            there were several heroes in this level, we'd only see the
         *            strength of the last hero
         */
        else if (whichLevel == 18) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("The hero can defeat \nup to two enemies...", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a hero and give it strength of 10
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setStrength(10);
            h.setMoveByTilting();

            // draw a strength meter
            Controls.addStrengthMeter("Strength", 220, 280, h);

            // our first enemy stands still:
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            e.setDefeatHeroText("How did you hit that?");

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
            e.setDefeatHeroText("You can't run");
        }

        /**
         * @level: 19
         * @description: This level shows that we can win a level by defeating
         *               all enemies
         * @whatsnew: ability to win by defeating enemies
         */
        else if (whichLevel == 19) {
            // set up a basic level with a tilt hero
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("You have 10 seconds\nto defeat the enemies", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // give the hero enough strength that this will work...
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setStrength(10);
            h.setMoveByTilting();

            // draw a few enemies
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            e.setDamage(4);
            e = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e.setPhysics(.1f, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(4);

            // put a countdown on the screen
            Controls.addCountdown(10, "Time Up!", 200, 25);

            // indicate that defeating enemies is the key to success
            Score.setVictoryEnemyCount();
        }

        /**
         * @level: 20
         * @description: this level shows that a goodie can increase the hero's
         *               strength, and that we can win by defeating a specific
         *               number of enemies
         * @whatsnew: goodies that give the hero more strength
         * @whatsnew: winning without defeating all the enemies
         */
        else if (whichLevel == 20) {
            // set up a basic level that can be won by defeating 1 enemy
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Collect blue balls\nto increse strength", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
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
            Goodie g = Goodie.makeAsCircle(0, 30, 1, 1, "blueball.png");
            g.setStrengthBoost(5);
            g.setDisappearSound("woowoowoo.ogg");

            // track strength, win by defeating one enemy
            Controls.addStrengthMeter("Strength", 220, 280, h);

            // win by defeating one enemy
            Score.setVictoryEnemyCount(1);
            PostScene.setDefaultWinText("Good enough...");
        }

        /**
         * @level: 21
         * @description: this level introduces the idea of invincibility.
         *               Collecting the goodie makes the hero invincible for a
         *               little while...
         * @whatsnew: Invincibility from a goodie
         * @whatsnew: Print FPS to the screen
         */
        else if (whichLevel == 21) {
            // basic setup:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("The blue ball will\nmake you invincible\nfor 15 seconds", 50, 50,
                    255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // draw a few enemies...
            for (int i = 0; i < 5; ++i) {
                Enemy e = Enemy.makeAsCircle(5 * i + 1, 25, 2, 2, "redball.png");
                e.setPhysics(1.0f, 0.3f, 0.6f);
                e.setRotationSpeed(1);
                e.setDamage(4);
            }

            // this goodie makes us invincible
            Goodie g = Goodie.makeAsCircle(30, 30, 1, 1, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);

            // we'll still say you win by reaching the destination. Defeating
            // enemies is just for fun...
            Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Controls.addGoodieCount1(0, "Goodies", 220, 280);

            Controls.addFPS(400, 15, "arial.ttf", 200, 200, 100, 12);
        }

        /**
         * @level: 22
         * @description: Some goodies can "count" for more than one point...
         *               they can even count for negative points.
         * @whatsnew: goodie values that change how many points we get for
         *            collecting a goodie
         */
        else if (whichLevel == 22) {

            // basic configuration
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Collect 'the right' \nblue balls to\nactivate destination", 50, 50,
                    255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination d = Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            d.setActivationScore(7, 0, 0, 0);
            Score.setVictoryDestination(1);

            // create some goodies with special values
            Goodie g1 = Goodie.makeAsCircle(0, 30, 1, 1, "blueball.png");
            g1.setScore(-2, 0, 0, 0);
            Goodie g2 = Goodie.makeAsCircle(0, 15, 1, 1, "blueball.png");
            g2.setScore(7, 0, 0, 0);

            // create some regular goodies
            Goodie.makeAsCircle(30, 30, 1, 1, "blueball.png");
            Goodie.makeAsCircle(35, 30, 1, 1, "blueball.png");

            // print a goodie count to show how the count goes up and down
            Controls.addGoodieCount1(0, "Progress", 220, 280);
        }

        /**
         * @level: 23
         * @description: this level demonstrates that we can drag entities (in
         *               this case, obstacles), and that we can make rotated
         *               obstacles. The latter could be useful for having angled
         *               walls in a maze
         * @whatsnew: entites that can be dragged
         * @whatsnew: oblong obstacles that can be rotated
         */
        else if (whichLevel == 23) {
            // basic tilt-based level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Rotating oblong obstacles\nand draggable obstacles", 50, 50, 255,
                    255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw an obstacle that we can drag
            Obstacle o = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setCanDrag(true);

            // draw an obstacle that is oblong (due to its width and height)
            // and that is rotated. Note that this needs
            // to be a box, or it will not have the right underlying shape.
            o = Obstacle.makeAsBox(12, 12, 3.5f, .5f, "purpleball.png");
            o.setRotation(45);
        }

        /**
         * @level: 24
         * @description: this level shows how we can use "poking" to move
         *               obstacles. In this case, pressing an obstacle selects
         *               it, and pressing the screen moves the obstacle to that
         *               location. Double-tapping an obstacle removes it.
         * @whatsnew: pokeable obstacles
         * @whatsnew: we can draw pictures on the screen that don't have an
         *            influence on the physics of the game
         */
        else if (whichLevel == 24) {
            // a basic level:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            PreScene.addText("Touch the obstacle\nto select, then" + "\ntouch a destination", 50,
                    50, 255, 255, 255, "arial.ttf", 32);

            // draw a picture on the default plane (0)
            Util.drawPicture(0, 0, 48, 32, "greenball.png", 0);

            // draw the hero and destination after the picture, so that the
            // picture is behind/below them...
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a pokeable obstacle
            Obstacle o = Obstacle.makeAsBox(0, 0, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(0, 100, 0);
            o.setPokeToPlace(250);
        }

        /**
         * @level: 25
         * @description: In this level, the enemy chases the hero
         * @whatsnew: an enemy that follows the hero, wherever the hero goes...
         * @whatsnew: use of drawPictureBehindScene to draw something behind the
         *            hero
         */
        else if (whichLevel == 25) {
            // basic setup
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("The enemy will chase you", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // create an enemy who chases the hero
            Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(.1f, 0.3f, 0.6f);
            e3.setChaseSpeed(8, h, true, true);

            // draw a picture late within this block of code, but still cause
            // the picture to be drawn behind everything else by giving it a z
            // index of -2
            Util.drawPicture(0, 0, 48, 32, "greenball.png", -2);
        }

        /**
         * @level: 26
         * @description: demonstrate that we can make obstacles play sounds
         *               either when we collide with them, or touch them
         * @whatsnew: touch an obstacle to make it play a sound
         * @whatsnew: collide with an obstacle to make it play a sound
         */
        else if (whichLevel == 26) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Touch the purple ball \nor collide with it", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up our obstacle so that collision and touch make it play
            // sounds
            Obstacle o = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setTouchSound("lowpitch.ogg");
            o.setCollideSound("hipitch.ogg", 2000);
        }

        /**
         * @level: 27
         * @description: this hero rotates so that it faces in the direction of
         *               movement
         * @whatsnew: heroes whose rotation changes with their direction
         */
        else if (whichLevel == 27) {
            // set up a big screen
            Level.configure(4 * 48, 2 * 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("The star rotates in\nthe direction of movement", 50, 50, 255, 255,
                    255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 4 * 48, 2 * 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(29, 60, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a hero who rotates in the direction of movement
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setRotationByDirection();
            h.setMoveByTilting();
            Level.setCameraChase(h);
        }

        /**
         * @level: 28
         * @description: This level shows three things. The first is that a
         *               custom motion path can allow things to violate the laws
         *               of physics and pass through other things. The second is
         *               that motion paths can go off-screen. The third is that
         *               PreScene is not benign. The only way to win this level
         *               is to hold the phone a certain way, wait three seconds,
         *               and then press the message. While the phone is held,
         *               some gravitational force can be "built up", so that the
         *               hero moves really fast when the level finally starts
         * @whatsnew: paths that go off screen
         */
        else if (whichLevel == 28) {
            // set up a regular level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Reach the destination\nto win the game.", 50, 50, 255, 255, 255,
                    "arial.ttf", 20);
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

        /**
         * @level: 29
         * @description: this level shows that we can draw on the screen to
         *               create obstacles.
         * @whatsnew: scribblemode for drawing obstacles on the screen
         */
        else if (whichLevel == 29) {
            // set up a basic level:
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Draw on the screen\nto make obstacles appear", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(21.5f, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(21.5f, 1, 2, 2, "mustardball.png");
            Score.setVictoryDestination(1);

            // turn on 'scribble mode'. Be sure to play with the last parameter
            // to see the difference between scribbles
            // that can move and that can't move
            Level.setScribbleOn("purpleball.png", 3, 1.5f, 1.5f, 0, 0, 0, true, 10);
        }

        /**
         * @level: 30
         * @description: this level shows that we can "flick" things to move
         *               them
         * @whatsnew: In this level, notice that we did not enable tilt!
         *            Instead, we specified that there is a default gravity in
         *            the Y dimension pushing everything down. This is much like
         *            gravity on earth. The only way to move things, then, is
         *            via flicking them.
         * @whatsnew: using flick to move the hero
         */
        else if (whichLevel == 30) {
            // create a level with a constant force downward in the Y dimension
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            // create a hero who we can flick
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setFlickable(1f);
            h.disableRotation();

            // set up a destination
            Destination.makeAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /**
         * @level: 31
         * @description: this level introduces a new concept: side-scrolling
         *               games. just like in level 30, we have a constant force
         *               in the negative direction. However, in this level, we
         *               say that tilt can produce forces in X but not in Y.
         *               Thus we can tilt to move the hero left/right. Note,
         *               too, that the hero will fall to the floor, since there
         *               is a constant downward force, but there is not any
         *               mechanism to apply a Y force to make it move back up.
         * @whatsnew: we changed the physics, so that now we have a tilt-based
         *            side scroller
         */
        else if (whichLevel == 31) {
            // make a long level but not a tall level, and provide a constant
            // downward force:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            // turn on tilt, but only in the X dimension
            Tilt.enable(10, 0);
            PreScene.addText("Side scroller / tilt demo", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);
        }

        /**
         * @level: 32
         * @description: In the previous level, it was hard to see that the hero
         *               was moving. We can make a background layer to remedy
         *               this situation. Notice that the background uses
         *               transparency to show the blue color for part of the
         *               screen
         * @whatsnew: background colors and background images
         * @whatsnew: demonstrate that we can have things hover on the screen
         *            despite the fact that the screen is scrolling
         */
        else if (whichLevel == 32) {
            // start by repeating the previous level:
            Level.configure(30 * 48, 32);
            Physics.configure(0, -10);
            // turn on tilt, but only in the X dimension
            Tilt.enable(10, 0);
            PreScene.addText("Side scroller / tilt demo", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 30 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(30 * 48 - 5, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);

            // now paint the background blue
            Background.setColor(0, 0, 255);
            // put in a picture that scrolls at half the speed of the hero in
            // the x direction (ignore y scrolling for now)
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // make an obstacle that hovers...
            Obstacle o = Obstacle.makeAsCircle(10, 10, 5, 5, "blueball.png");
            o.setHover(100, 100);
        }

        /**
         * @level: 33
         * @description: this level adds multiple background layers, and it also
         *               allows the hero to jump via touch
         * @whatsnew: jumping via touching heroes
         * @whatsnew: background layers
         * @whatsnew: jump sounds
         */
        else if (whichLevel == 33) {

            // set up a standard side scroller with tilt:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press the hero to\nmake it jump", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            // this says that touching makes the hero jump
            h.setTouchToJump();
            // this is the force of a jump. remember that up is positive.
            h.setJumpImpulses(0, 10);
            // the sound to play when we jump
            h.setJumpSound("fwapfwap.ogg");

            // set up our background again, but add a few more layers
            Background.setColor(0, 0, 255);
            // this layer has a scroll factor of 0... it won't move
            Background.addHorizontalLayer(0, 1, "back.png", 0);
            // this layer moves at half the speed of the hero
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);
            // this layer is faster than the hero
            Background.addHorizontalLayer(1.25f, 1, "front.png", 20);
            Level.setCameraChase(h);
        }

        /**
         * @level: 34
         * @description: tilt doesn't always work so nicely in side scrollers.
         *               An alternative is for the hero to have a fixed rate of
         *               motion. Another issue was that you had to touch the
         *               hero itself to make it jump. Now, we use an invisible
         *               button so touching any part of the screen makes the
         *               hero jump.
         * @whatsnew: camera offset on the hero, so that it stays a bit to the
         *            left of the center of the screen
         * @whatsnew: fixed velocity on the hero
         * @whatsnew: jump button on the HUD
         */
        else if (whichLevel == 34) {
            // set up a side scroller, but don't turn on tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press anywhere to jump", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // note: the bounding box does not have friction, and neither does
            // the hero
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 0, 0, 0);

            // make a hero, but don't let it rotate:
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
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // draw a jump button that covers the whole screen
            Controls.addJumpButton(0, 0, 480, 320, "", h);

            // if the hero jumps over the destination, we have a problem. To fix
            // it, let's put an
            // invisible enemy right after the destination, so that if the hero
            // misses the
            // destination, it hits the enemy and we can start over
            Enemy.makeAsBox(130, 0, .5f, 32, "");
        }

        /**
         * @level: 35
         * @description: the default is that once a hero jumps, it can't jump
         *               again until it touches an obstacle (floor or wall).
         *               Here, we enable multiple jumps. Coupled with a small
         *               jump impulse, this makes jumping feel more like
         *               swimming or controlling a helicopter.
         */
        else if (whichLevel == 35) {

            // set up a standard side scroller without tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Multi-jump is enabled", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            Hero h = Hero.makeAsBox(2, 0, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            // the hero now has multijump, with small jumps:
            h.setMultiJumpOn();
            Level.setCameraChase(h);
            h.setCameraOffset(15, 0);
            h.setJumpImpulses(0, 2);
            h.addVelocity(5, 0, false);

            // this is all the same as before, to include the invisible enemy
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);
            Controls.addJumpButton(0, 0, 480, 320, "", h);
            Destination.makeAsCircle(120, 31, 1, 1, "mustardball.png");
            Enemy.makeAsBox(130, 0, .5f, 32, "");
            Score.setVictoryDestination(1);
        }

        /**
         * @level: 36
         * @description: This level shows that we can make a hero move based on
         *               how we touch the screen
         * @whatsnew: controls on the screen for moving the hero
         * @whatsnew: the hero can face backwards when it moves backwards
         */
        else if (whichLevel == 36) {
            // set up a basic side scroller without tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            PreScene.addText("Press screen borders\nto move the hero", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 0, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);

            // this lets the hero flip its image when it moves backwards
            h.setCanFaceBackwards();

            Level.setCameraChase(h);
            Destination.makeAsCircle(120, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // let's draw an enemy, just in case anyone wants to try to go to
            // the top left corner
            Enemy.makeAsCircle(3, 27, 3, 3, "redball.png");

            // draw some buttons for moving the hero
            Controls.addLeftButton(0, 50, 50, 220, "", 15, h);
            Controls.addRightButton(430, 50, 50, 220, "", 15, h);
            Controls.addUpButton(50, 270, 380, 50, "", 15, h);
            Controls.addDownButton(50, 0, 380, 50, "", 15, h);
        }

        /**
         * @level: 37
         * @description: In the last level, we had complete control of the
         *               hero's movement. Here, we give the hero a fixed
         *               velocity, and only control its up/down movement.
         */
        else if (whichLevel == 37) {
            // set up a basic side-scroller
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            PreScene.addText("Press screen borders\nto move up and down", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            // be careful about friction!
            Destination.makeAsCircle(120, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // as with the bounding box, be careful about friction on the hero!
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(10, 0, false);

            Level.setCameraChase(h);

            // draw an enemy to avoid, and one at the end
            Enemy.makeAsCircle(53, 28, 3, 3, "redball.png");
            Enemy.makeAsBox(130, 0, .5f, 32, "");

            // draw the up/down controls
            Controls.addDownButton(50, 0, 380, 50, "", 15, h);
            Controls.addUpButton(50, 270, 380, 50, "", 15, h);
        }

        /**
         * @level: 38
         * @description: this level demonstrates crawling heroes. We can use
         *               this to simulate crawling, ducking, rolling, spinning,
         *               etc. Note, too, that we can use it to make the hero
         *               defeat certain enemies via crawl.
         * @whatsnew: buttons for making heroes crawl
         * @whatsnew:
         */
        else if (whichLevel == 38) {
            // basic configuration:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press the screen\nto crawl", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);
            Destination.makeAsCircle(120, 0, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            h.setPhysics(.1f, 0, 0);
            h.addVelocity(5, 0, false);
            Level.setCameraChase(h);
            // to enable crawling, we just draw a crawl button on the screen
            Controls.addCrawlButton(0, 0, 480, 320, "", h);

            // make an enemy who we can defeat by colliding with it while
            // crawling
            Enemy e = Enemy.makeAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();
        }

        /**
         * @level: 39
         * @description: we can make a hero start moving only when it is pressed
         * @whatsnew: touch and go
         */
        else if (whichLevel == 39) {
            // set up a basic side scroller
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press the hero\nto start moving\n", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            Destination.makeAsCircle(120, 0, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero who doesn't start moving until it is touched
            //
            // note that this hero is not a perfect circle, and the hero is
            // "norotate".
            // You will probably get strange behaviors if you choose any other
            // options
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            h.setTouchAndGo(10, 0);
            Level.setCameraChase(h);
        }

        /**
         * @level: 40
         * @description: ALE has limited support for SVG. If you draw a picture
         *               in Inkscape or another SVG tool, and it only consists
         *               of lines, then you can import it into your game as an
         *               obstacle. Drawing a picture on top of the obstacle is
         *               probably a good idea, though we don't bother in this
         *               level
         * @whatsnew: import an svg as an obstacle
         * @whatsnew: side scroller with velocity tilt override
         */
        else if (whichLevel == 40) {
            // set up a tilt-based side scroller
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Tilt.setAsVelocity(true);
            PreScene.addText("Obstacles can\nbe drawn from SVG\nfiles", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);

            // make a hero who can jump
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            Level.setCameraChase(h);

            // draw an obstacle from SVG
            SVG.importLineDrawing("shape.svg", 1, 0, 0, 2f, .5f, 25f, 15f);

            // notice that we can only get to the destination by jumping from
            // *on top of* the obstacle
            Destination.makeAsCircle(120, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // zoom buttons so that we can see the whole obstacle
            Controls.addZoomInButton(0, 0, 20, 20, "red.png", .25f);
            Controls.addZoomOutButton(460, 0, 20, 20, "red.png", 8);
        }

        /**
         * @level: 41
         * @description: this is a side-scroller with speed boosters for
         *               changing the hero's velocity
         * @whatsnew: speed booster obstacles
         */
        else if (whichLevel == 41) {
            // set up a basic level with a fixed velocity hero and a destination
            Level.configure(10 * 48, 32);
            Physics.configure(0, 0);
            PreScene.addText("Speed boosters and reducers", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 10 * 480, 320, "", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 0, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            Destination.makeAsCircle(450, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            h.addVelocity(10, 0, false);
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // place a speed-up obstacle that lasts for 2 seconds
            Obstacle o1 = Obstacle.makeAsCircle(40, 1, 4, 4, "purpleball.png");
            o1.setSpeedBoost(20, 0, 2);

            // place a slow-down obstacle that lasts for 3 seconds
            Obstacle o2 = Obstacle.makeAsCircle(120, 1, 4, 6, "purpleball.png");
            o2.setSpeedBoost(-9, 0, 3);

            // place a permanent +3 speedup obstacle
            Obstacle o3 = Obstacle.makeAsCircle(240, 1, 4, 4, "purpleball.png");
            o3.setSpeedBoost(20, 0, -1);

            Level.setCameraChase(h);
        }

        /**
         * @level: 42
         * @description: this is a very gross level, which exists just to show
         *               that backgrounds can scroll vertically.
         * @whatsnew: vertical background colors and images
         */
        else if (whichLevel == 42) {
            // set up a level where tilt only makes the hero move up and down
            Level.configure(48, 4 * 32);
            Physics.configure(0, 0);
            Tilt.enable(0, 10);
            PreScene.addText("Vertical scroller demo", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 4 * 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 120, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsBox(0, 2, 48, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Level.setCameraChase(h);

            // set up a vertical scrolling background
            Background.setColor(255, 0, 255);
            Background.addVerticalLayer(1, 0, "back.png", 0);
            Background.addVerticalLayer(1, .5f, "mid.png", 0);
            Background.addVerticalLayer(1, 1, "front.png", 0);
        }

        /**
         * @level: 43
         * @description: the next few levels demonstrate support for throwing
         *               projectiles. In this level, we throw projectiles by
         *               touching the hero
         * @whatsnew: throw a projectile by touching the hero
         */
        else if (whichLevel == 43) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Press the hero\nto make it throw\nprojectiles", 50, 50, 255, 255,
                    255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a hero, and indicate that touching it makes it throw
            // projectiles
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setTouchToThrow(h);
            h.setMoveByTilting();

            // configure a pool of projectiles. be sure to hover the mouse over
            // 'configure' to see what all the parameters do. In particular,
            // note how the projectiles fly out of the hero, and how many can be
            // on screen at any time
            Projectile.configure(3, 1, 1, "greyball.png", 0, 10, 1, .5f, 1, 0, true);
        }

        /**
         * @level: 44
         * @description: this is another demo of how throwing projectiles works.
         *               Like the previous demo, it doesn't actually use
         *               projectiles for anything, it is just to show how to get
         *               some different behaviors in terms of how the
         *               projectiles move.
         * @whatsnew: limiting the range of projectiles
         * @whatsnew: throw projectiles by touching the screen
         */
        else if (whichLevel == 44) {
            // set up a basic tilt level
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press anywhere\nto throw a gray\nball", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 30, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 0, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up a pool of projectiles, but now once the projectiles move
            // more than 250x20 away
            // from the hero, they disappear
            Projectile.configure(100, 1, 1, "greyball.png", 30, 0, 4, 0, 1, 0, true);
            Projectile.setRange(25);

            // add a button for throwing projectiles
            Controls.addThrowButton(0, 0, 480, 320, "", h, 100);
            Level.setCameraChase(h);
        }

        /**
         * @level 45
         * @description: this level demonstrates that we can defeat enemies by
         *               throwing projectiles at them
         * @whatsnew: show that we can defeat enemies by throwing projectiles at
         *            them
         * @whatsnew: holding the button doesn't make multiple shots fire
         */
        else if (whichLevel == 45) {
            // set up a simple, small level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            PreScene.addText("Defeat all enemies\nto win", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // set up our projectiles... note that the last parameter means that
            // projectiles each do
            // 2 units of damage
            Projectile.configure(3, .4f, .1f, "greyball.png", 0, 10, .2f, -.5f, 2, 0, true);

            // draw a few enemies... note that they have different amounts of
            // damage
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5) {
                Enemy ee = Enemy.makeAsCircle(i, i + 5, 2, 2, "redball.png");
                ee.setPhysics(1.0f, 0.3f, 0.6f);
                ee.setDamage(i);
            }
            Score.setVictoryEnemyCount();

            // this button only throws one projectile per press...
            Controls.addSingleThrowButton(0, 0, 480, 320, "", h);
        }

        /**
         * @level: 46
         * @description: this level shows how to throw projectiles in a variety
         *               of directions
         * @whatsnew: the "vector throw" mechanism
         */
        else if (whichLevel == 46) {
            // set up a simple level
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press anywhere\nto throw a ball", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 0, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions
            Controls.addVectorThrowButton(0, 0, 480, 320, "", h, 100);

            // set up our pool of projectiles. The main challenge here is that
            // the farther from the hero we press, the faster the projectile
            // goes, so we multiply the velocity by .8 to slow it down a bit
            Projectile.configure(100, 1, 1, "greyball.png", 30, 0, 0, 0, 1, 0, true);
            Projectile.setProjectileVectorDampeningFactor(.8f);
            Projectile.setRange(30);

            Level.setCameraChase(h);
        }

        /**
         * @level: 47
         * @description: this level shows that with the "vector" projectiles, we
         *               can still have gravity affect the projectiles. This is
         *               very good for basketball-style games.
         * @whatsnew: holding the screen no longer throws multiple projectiles
         *            in that direction
         * @whatsnew: projectiles can be prevented from disappearing when they
         *            collide with certain types of obstacles
         */
        else if (whichLevel == 47) {
            // set up a basic level
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press anywhere\nto throw a ball", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation(); // Remember: disableRotation doesn't work well
                                 // if we have mass...
            h.setPhysics(0, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(120, 0, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // the only differences here are that we turn gravity back on for
            // the projectiles, and
            // we use a "single throw" button
            Controls.addVectorSingleThrowButton(0, 0, 480, 320, "", h);
            Projectile.configure(100, 1, 1, "greyball.png", 30, 0, 4, 0, 1, 0, true);
            Projectile.setProjectileVectorDampeningFactor(.8f);
            Projectile.setRange(40);
            Projectile.setProjectileGravityOn();
            Obstacle o = Obstacle.makeAsBox(10, 20, 2, 2, "red.png");
            // This is a rather complex trick: we can specify that when a
            // projectile collides with things, it is allowed
            // to bounce off of them, and that when it collides with /this/
            // obstacle, we want custom code to run. The
            // custom code appears below, as onProjectileCollideTrigger, and
            // when it is called, our id (the first "0")
            // will be passed along to the code, along with the specific
            // obstacle and projectile that were involved in
            // the collision. The default is that when a projectile hits one of
            // these special obstacles, it doesn't
            // automatically disappear, because in that code you could say
            // "p.remove(true)" if that was the behavior you
            // wanted. So, by default, it doesn't disappear, and instead we get
            // the projectile bouncing off of the
            // obstacle, but not off of anything else.
            Projectile.enableCollisionsForProjectiles();
            o.setProjectileCollisionTrigger(0, 0, 0, 0, 0);
            Level.setCameraChase(h);
        }

        /**
         * @level: 48
         * @description: this level shows how enemies can reproduce. This can
         *               simulate cancer cells, or fire on a building. We do
         *               this by using a timer connected to an enemy. Whenever
         *               the timer goes off, we will have access to the enemy so
         *               that we can copy it.
         * @whatsnew: enemy timer triggers
         * @whatsnew: sound when the projectile is thrown
         */
        else if (whichLevel == 48) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 0);
            PreScene.addText("Throw balls at \nthe enemies before\nthey reproduce", 50, 50, 255,
                    255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setTouchToThrow(h);
            h.setMoveByTilting();

            // configure a pool of projectiles
            Projectile.configure(100, .5f, .5f, "greyball.png", 0, 10, 2, -.5f, 1, 0, true);
            Projectile.setThrowSound("fwapfwap.ogg");
            Projectile.setProjectileDisappearSound("slowdown.ogg");

            // draw an enemy that reproduces via a timer trigger
            Enemy e = Enemy.makeAsCircle(23, 20, 1, 1, "redball.png");
            e.setDisappearSound("lowpitch.ogg");
            Level.setEnemyTimerTrigger(2, 2, e);
            Score.setVictoryEnemyCount();
            Controls.addDefeatedCount(0, "Enemies Defeated", 20, 20);
        }

        /**
         * @level: 49
         * @description: this level shows what happens when enemies reproduce
         *               when they are moveable
         */
        else if (whichLevel == 49) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("These enemies are\nreally tricky", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 29, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make our initial enemy
            Enemy e = Enemy.makeAsCircle(23, 2, 1, 1, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setMoveByTilting();
            // warning: "6" is going to lead to 127 enemies eventually...
            Level.setEnemyTimerTrigger(6, 2, e);
        }

        /**
         * @level: 50
         * @description: this level shows simple animation. Every entity can
         *               have a default animation.
         * @whatsnew: the hero has an animation in this level, which makes it
         *            look like a star with streamers underneath it
         */
        else if (whichLevel == 50) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Make a wish!", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // this hero will be animated:
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            // this says that we scroll through the 0, 1, 2, and 3 cells of the
            // image, and we show each for 200 milliseconds
            h.setDefaultAnimation(new Animation("stars.png", 4, true).to(0, 200).to(1, 200)
                    .to(2, 200).to(3, 200));
        }

        /**
         * @level: 51
         * @description: this level introduces jumping animations and
         *               disappearance animations
         * @whatsnew: jump animation
         * @whatsnew: disappearance animation
         */
        else if (whichLevel == 51) {
            // make a tilt sidescroller
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press the hero to\nmake it jump", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero, and give it two animations: one for when it is in
            // the air, and another
            // for the rest of the time. Note that both sets of images must be
            // cells from the same
            // .png file
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setJumpImpulses(0, 20);
            h.setTouchToJump();
            h.setMoveByTilting();
            h.setDefaultAnimation(new Animation("stars.png", 4, true).to(0, 200).to(1, 200)
                    .to(2, 200).to(3, 200));
            h.setJumpAnimation(new Animation("stars.png", 4, true).to(4, 200).to(5, 200).to(6, 200)
                    .to(7, 200));

            // create a goodie that has a disappearance animation. Note that
            // this can be a totally
            // different image than its regular appearance... that's OK, because
            // once we touch the
            // entity, it disappears and the animation just plays once in the
            // background. Note, too,
            // that the final cell is blank, so that we don't leave a residue on
            // the screen.

            Goodie g = Goodie.makeAsCircle(15, 9, 5, 5, "stars.png");
            g.setDisappearAnimation(new Animation("starburst.png", 4, false).to(2, 200).to(1, 200)
                    .to(0, 200).to(3, 200), 1, 0, 5, 5);
            Level.setCameraChase(h);
        }

        /**
         * @level: 52
         * @description: this level shows that projectiles can be animated
         * @whatsnew: projectile animations
         * @whatsnew: animations when the hero throws a projectile
         */
        else if (whichLevel == 52) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Press the hero\nto make it\nthrow a ball", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // set up our hero
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "colorstar.png");
            h.setPhysics(1, 0, 0.6f);
            h.setTouchToThrow(h);
            h.setMoveByTilting();

            // set up an animation when the hero throws:
            h.setThrowAnimation(new Animation("colorstar.png", 2, false).to(3, 100).to(4, 500));

            // make a projectile pool and give an animation pattern for the
            // projectiles
            Projectile.configure(100, 1, 1, "flystar.png", 0, 10, 0, -.5f, 1, 0, true);
            Projectile.setAnimation(new Animation("flystar.png", 2, true).to(0, 100).to(1, 100));
        }

        /**
         * @level: 53
         * @description: this level explores invincibility animation. While
         *               we're at it, we make some enemies that aren't affected
         *               by invincibility, and some that can even damage the
         *               hero while they are invincible.
         * @whatsnew: invincibility animation
         * @whatsnew: enemies that resist invincibility
         * @whatsnew: enemies that do damage even when the hero is invincible
         * @whatsnew: display a picture when the level is won
         */
        else if (whichLevel == 53) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("The blue ball will\nmake you invincible\nfor 15 seconds", 50, 50,
                    255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make an animated hero, and also give it an invincibility
            // animation
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setDefaultAnimation(new Animation("colorstar.png", 4, true).to(0, 300).to(1, 300)
                    .to(2, 300).to(3, 300));
            h.setInvincibleAnimation(new Animation("colorstar.png", 4, true).to(4, 100).to(5, 100)
                    .to(6, 100).to(7, 100));

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
                // won't harm an
                // invincible hero
                if (i == 1)
                    e.setResistInvincibility();
            }
            // neat trick: this enemy does zero damage, but slows the hero down.
            Enemy e = Enemy.makeAsCircle(30, 20, 2, 2, "redball.png");
            e.setPhysics(10, 0.3f, 0.6f);
            e.setMoveByTilting();
            e.setDamage(0);

            // add a goodie that makes the hero invincible
            Goodie g = Goodie.makeAsCircle(30, 30, 1, 1, "blueball.png");
            g.setInvincibilityDuration(15);
            g.setRoute(new Route(3).to(30, 30).to(10, 10).to(30, 30), 5, true);
            g.setRotationSpeed(0.25f);
            Controls.addGoodieCount1(0, "Goodies", 220, 280);

            // draw a picture when the level is won, and don't print text...
            // this particular picture
            // isn't very useful
            PostScene.addWinImage("fade.png", 0, 0, 480, 320);
        }

        /**
         * @level: 54
         * @description: demonstrate crawl animation, and also show that on
         *               multitouch phones, we can "crawl" in the air while
         *               jumping.
         * @whatsnew: crawl animation
         * @whatsnew: show a picture when the level is lost
         */
        else if (whichLevel == 54) {
            // make a simple level:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press the left side of\nthe screen to crawl\n"
                    + "or the right side\nto jump.", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 0);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero with fixed velocity, and give it crawl and jump
            // animations
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "stars.png");
            h.setPhysics(1, 0, 0);
            h.addVelocity(15, 0, false);
            h.setCrawlAnimation(new Animation("stars.png", 4, true).to(0, 100).to(1, 300)
                    .to(2, 300).to(3, 100));
            h.setJumpAnimation(new Animation("stars.png", 4, true).to(4, 200).to(5, 200).to(6, 200)
                    .to(7, 200));

            // enable hero jumping and crawling
            h.setJumpImpulses(0, 15);
            Controls.addJumpButton(0, 0, 240, 320, "", h);
            Controls.addCrawlButton(241, 0, 480, 320, "", h);

            // add an enemy we can defeat via crawling, just for fun. It should
            // be defeated even by a "jump crawl"
            Enemy e = Enemy.makeAsCircle(110, 1, 5, 5, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setDefeatByCrawl();

            // include a picture on the "try again" screen
            PostScene.addLoseImage("fade.png", 0, 0, 480, 320);
            PostScene.setDefaultLoseText("Oh well...");
            Level.setCameraChase(h);
        }

        /**
         * @level: 55
         * @description: This isn't quite the same as animation, but it's nice.
         *               We can indicate that a hero's image changes via goodie
         *               count. This can, for example, allow a hero to change
         *               (e.g., get healthier) by swapping through images as
         *               goodies are collected
         */
        else if (whichLevel == 55) {
            // set up a basic level with a bunch of goodies
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            for (int i = 0; i < 8; ++i)
                Goodie.makeAsCircle(5 + 2 * i, 5 + 2 * i, 2, 2, "blueball.png");
            Destination d = Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            d.setActivationScore(8, 0, 0, 0);
            Score.setVictoryDestination(1);

            // Note: colorstar.png has 8 cells...
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "colorstar.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            // set up the animation by matching the cell in the image to a
            // specific goodie count.
            // That is, when the count is 1, we show picture 2. When the count
            // is 2, we show picture
            // 1, etc.
            //
            // note: no change for goodie count 3, and remember that 0 is the
            // default picture so it's showing already
            //
            // Note: this is ugly, because we are matching the frames, but
            // duration is actually the goodie count
            h.setAnimateByGoodieCount(new Animation("colorstar.png", 7, false).to(2, 1).to(1, 2)
                    .to(4, 3).to(5, 4).to(6, 5).to(7, 6).to(3, 8));
        }

        /**
         * @level: 56
         * @description: demonstrate that obstacles can defeat enemies, and that
         *               we can use this feature to have obstacles that only
         *               defeat certain "marked" enemies
         * @whatsnew: gravity multiplier, to make the forces happen more quickly
         * @whatsnew: use of enemyCollisionTrigger to make some obstacles able
         *            to defeat some enemies, and enable some of these obstacles
         *            to disappear after defeating an enemy
         * @whatsnew: moveable obstacles
         */
        else if (whichLevel == 56) {
            // make a basic level, but increase the speed of gravity updates
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Tilt.setGravityMultiplier(3);
            PreScene.addText("You can defeat\ntwo enemies with\nthe blue ball", 50, 50, 255, 255,
                    255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // put an enemy defeated count on the screen, in red with a small
            // font
            Controls.addDefeatedCount(2, "Enemies Defeated", 20, 20, "arial.ttf", 255, 0, 0, 10);

            // make a moveable obstacle that can defeat enemies
            Obstacle o = Obstacle.makeAsCircle(10, 2, 4, 4, "blueball.png");
            o.setPhysics(.1f, 0, 0.6f);
            o.setMoveByTilting();
            o.setEnemyCollisionTrigger(0, 0, 0, 0, 0, 0);

            // make a small obstacle that can also defeat enemies, but doesn't
            // disappear
            Obstacle o2 = Obstacle.makeAsCircle(.5f, .5f, 2, 2, "blueball.png");
            o2.setPhysics(1, 0, 0.6f);
            o2.setMoveByTilting();
            o2.setEnemyCollisionTrigger(1, 0, 0, 0, 0, 0);

            // this enemy has a triggerID of 1... no obstacle will defeat it
            Enemy e = Enemy.makeAsCircle(40, 2, 4, 4, "redball.png");
            e.setPhysics(1, 0, 0.6f);
            e.setMoveByTilting();

            // This enemy also has a triggerID of 1... no obstacle will defeat
            // it
            Enemy e1 = Enemy.makeAsCircle(40, 2, 4, 4, "redball.png");
            e1.setPhysics(1, 0, 0.6f);

            // these enemies have class 7... our obstacles will defeat them
            Enemy e2 = Enemy.makeAsCircle(40, 22, 4, 4, "redball.png");
            e2.setPhysics(1, 0, 0.6f);
            e2.setMoveByTilting();
            e2.setInfoText("weak");

            Enemy e3 = Enemy.makeAsCircle(40, 12, 4, 4, "redball.png");
            e3.setPhysics(1, 0, 0.6f);
            e3.setMoveByTilting();
            e3.setInfoText("weak");

            // win by defeating enemies
            Score.setVictoryEnemyCount(2);
        }

        /**
         * @level: 57
         * @description: this level shows an odd way of moving the hero. There's
         *               friction on the floor, so it can only move by tilting
         *               while the hero is in the air
         */
        else if (whichLevel == 57) {
            // set up a side scroller level, but give the bounding box some
            // friction
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press the hero to\nmake it jump", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a box hero with friction... it won't roll on the floor, so
            // it's stuck!
            Hero h = Hero.makeAsBox(2, 2, 3, 3, "stars.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 5);
            h.setTouchToJump();
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);

            Level.setCameraChase(h);

            // draw a background
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);
        }

        /**
         * @level: 58
         * @description: this level shows that we can put an obstacle on the
         *               screen and use it to make the hero throw projectiles.
         *               It also shows that we can make entities that shrink
         *               over time... growth is possible too, with a negative
         *               value.
         * @whatsnew: limit the total number of projectiles that can be thrown
         * @whatsnew: make an entity shrink over time
         * @whatsnew: make projectiles that have a randomly selected image
         * @whatsnew: show how many shots are left
         */
        else if (whichLevel == 58) {
            // make a simple level with left/right tilt and Y gravity
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 5, 3, 3, "greenball.png");
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();

            // make an obstacle that causes the hero to throw Projectiles
            Obstacle o = Obstacle.makeAsCircle(43, 27, 5, 5, "purpleball.png");
            o.setCollisionEffect(false);
            o.setTouchToThrow(h);

            // set up our projectiles
            Projectile.configure(3, 1, 1, "colorstar.png", 0, 15, 0, 0, 2, 0, true);
            Projectile.setNumberOfProjectiles(20); // there are only 20... throw
                                                   // them carefully
            // Allow the projectile image to be chosen randomly from a sprite
            // sheet
            Projectile.setImageSource("colorstar.png");
            // show how many shots are left
            Controls.addProjectileCount("projectiles left", 5, 300, "arial.ttf", 255, 255, 255, 12);

            // draw a bunch of enemies to defeat
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);
            e.setRotationSpeed(1);
            for (int i = 1; i < 20; i += 5) {
                Enemy e1 = Enemy.makeAsCircle(i, i + 8, 2, 2, "redball.png");
                e1.setPhysics(1.0f, 0.3f, 0.6f);
            }

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

        /**
         * @level: 59
         * @description: this level shows that we can make a hero in the air
         *               rotate. Rotation doesn't do anything, but it looks
         *               nice...
         * @whatsnew: rotation buttons
         * @whatsnew: this level relies on being able to jump after touching a
         *            side wall
         */
        else if (whichLevel == 59) {
            // make a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Destination.makeAsCircle(46, 10, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);

            // make the hero jumpable, so that we can see it spin in the air
            Hero h = Hero.makeAsCircle(4, 27, 3, 3, "stars.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 10);
            h.setTouchToJump();

            // add rotation buttons
            Controls.addRotateButton(0, 240, 80, 80, "", -.5f, h);
            Controls.addRotateButton(380, 240, 80, 80, "", .5f, h);
        }

        /**
         * @level 60
         * @description: we can attach movement buttons to any moveable entity,
         *               so in this case, we attach it to an obstacle to get an
         *               arkanoid-like effect.
         * @whatsnew: attaching left/right buttons to an obstacle instead of
         *            controlling the last hero created
         */
        else if (whichLevel == 60) {
            // make a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            Destination.makeAsCircle(30, 10, 2.5f, 2.5f, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero who is always moving... note there is no friction,
            // anywhere, and the hero
            // is elastic... it won't ever stop...
            Hero h = Hero.makeAsCircle(4, 4, 3, 3, "greenball.png");
            h.setPhysics(0, 1, 0);
            h.addVelocity(0, 10, false);

            // make an obstacle and then connect it to some controls
            Obstacle o = Obstacle.makeAsBox(2, 30.9f, 4, 1, "red.png");
            o.setPhysics(100, 1, 0);
            Controls.addLeftButton(0, 0, 240, 320, "", 5, o);
            Controls.addRightButton(240, 0, 240, 320, "", 5, o);
        }

        /**
         * @level: 61
         * @description: this level demonstrates that things can appear and
         * @whatsnew: use of disappearDelay and appearanceDelay
         */
        else if (whichLevel == 61) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Things will appear \nand disappear...", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
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

        /**
         * @level: 62
         * @description: this level demonstrates the use of timer triggers. We
         *               can use timers to make more of the level appear over
         *               time. In this case, we'll chain the timer triggers
         *               together, so that we can get more and more things to
         *               develop. Be sure to look at the onTimeTrigger code to
         *               see how the rest of this level works.
         * @whatsnew: destinations and goodies with fixed velocities
         * @whatsnew: enemy who disappears when it is touched
         * @whatsnew: enemy who can be dragged around
         * @whatsnew: timer triggers
         */
        else if (whichLevel == 62) {
            // create a level that has a hero, but nothing else
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            PreScene.addText("There's nothing to\ndo... yet", 50, 50, 255, 255, 255, "arial.ttf",
                    20);

            // note: there's no destination yet, but we still say it's how to
            // win... we'll get a
            // destination in this level after a few timers run...
            Score.setVictoryDestination(1);

            // now set a timer trigger. after three seconds, the
            // onTimerTrigger() method will run,
            // with level=62 and id=0
            Level.setTimerTrigger(0, 3);
        }

        /**
         * @level: 63
         * @description: this level shows triggers that run on a collision. In
         *               this case, it lets us draw out the next part of the
         *               level later, instead of drawing the whole thing right
         *               now. In a real level, we'd draw a few screens at a
         *               time, and not put the trigger obstacle at the end of a
         *               screen, so that we'd never see the drawing of stuff
         *               taking place, but for this demo, that's actually a nice
         *               effect. Be sure to look at onCollideTrigger for more
         *               details.
         * @whatsnew: obstacles that are collision triggers
         * @whatsnew: obstacles with collision sounds
         * @whatsnew: collision triggers that depend on collecting enough
         *            goodies before they work
         */
        else if (whichLevel == 63) {
            // make a tilt level with just a hero
            Level.configure(3 * 48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Keep going right!", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Controls.addGoodieCount1(0, "Goodies", 220, 280);
            Score.setVictoryDestination(1);

            // this obstacle is a collision trigger... when the hero hits it,
            // the next part of the
            // level appears. Note, too, that it disappears when the hero hits
            // it, so we can play a
            // sound if we want...
            Obstacle o = Obstacle.makeAsBox(30, 0, 1, 32, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setHeroCollisionTrigger(0, 0, 0, 0, 0);
            o.setDisappearSound("hipitch.ogg");

            Level.setCameraChase(h);
        }

        /**
         * @level: 64
         * @description: this level demonstrates triggers that happen when we
         *               touch an obstacle. Be sure to look at the
         *               onTouchTrigger() method for more details
         * @whatsnew: touchtrigger obstacles
         */
        else if (whichLevel == 64) {
            // set up a basic level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Activate and then \ntouch the obstacle", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();

            // make a destination... notice that it needs a lot more goodies
            // than are on the
            // screen...
            Destination d = Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            d.setActivationScore(3, 0, 0, 0);
            Score.setVictoryDestination(1);

            // draw an obstacle, make it a touch trigger, and then draw the
            // goodie we need to get in
            // order to activate the obstacle
            Obstacle o = Obstacle.makeAsCircle(10, 5, 3, 3, "purpleball.png");
            o.setPhysics(1, 0, 1);
            o.setTouchTrigger(39, 1, 0, 0, 0, true); // I picked '39'
                                                     // arbitrarily...
            o.setDisappearSound("hipitch.ogg");
            Goodie g = Goodie.makeAsCircle(0, 30, 1, 1, "blueball.png");
            g.setDisappearSound("lowpitch.ogg");
        }

        /**
         * @level: 65
         * @description: this level shows how to use enemy defeat triggers.
         *               There are four ways to defeat an enemy, so we enable
         *               all mechanisms in this level, to see if they all work
         *               to cause enemy triggers to run the onEnemyTrigger code.
         *               Another important point here is that the IDs don't need
         *               to be unique for *any* triggers. We can use the same ID
         *               every time...
         * @whatsnew: use enemy defeat triggers
         * @whatsnew: the trigger code uses random number generation to place a
         *            reward goodie whenever an enemy is defeated
         */
        else if (whichLevel == 65) {
            // draw a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // give the hero strength, so that we can use him to defeat an enemy
            // as a test of enemy triggers
            Hero h = Hero.makeAsCircle(12, 12, 4, 4, "greenball.png");
            h.setStrength(3);
            h.setMoveByTilting();

            // enable throwing projectiles, so that we can test enemy triggers
            // again
            h.setTouchToThrow(h);
            Projectile.configure(100, 1, 1, "greyball.png", 30, 0, 4, 0, 1, 0, true);

            // add an obstacle that has a collision trigger
            Obstacle o = Obstacle.makeAsCircle(30, 10, 5, 5, "blueball.png");
            o.setPhysics(1000, 0, 0);
            o.setCanDrag(false);
            o.setEnemyCollisionTrigger(0, 0, 0, 0, 0, 0);

            // now draw our enemies... we need enough to be able to test that
            // all five defeat mechanisms work.
            Enemy e1 = Enemy.makeAsCircle(5, 5, 1, 1, "redball.png");
            e1.setDefeatTrigger(0);

            Enemy e2 = Enemy.makeAsCircle(5, 5, 2, 2, "redball.png");
            e2.setDefeatTrigger(0);
            e2.setInfoText("weak");

            Enemy e3 = Enemy.makeAsCircle(40, 3, 1, 1, "redball.png");
            e3.setDefeatTrigger(0);

            Enemy e4 = Enemy.makeAsCircle(25, 25, 1, 1, "redball.png");
            e4.setDefeatTrigger(0);
            e4.setDisappearOnTouch();

            Enemy e5 = Enemy.makeAsCircle(25, 29, 1, 1, "redball.png");
            e5.setDefeatTrigger(0);

            // a goodie, so we can do defeat by invincibility
            Goodie g1 = Goodie.makeAsCircle(20, 29, 2, 3, "purpleball.png");
            g1.setInvincibilityDuration(15);

            // win by defeating enemies
            Score.setVictoryEnemyCount();
        }

        /**
         * @level: 66
         * @description: This level shows that we can resize a hero on the fly,
         *               and change its image. We use a collision trigger to
         *               cause the effect. Furthermore, we can increment scores
         *               inside of the trigger code, which lets us activate the
         *               destination on an obstacle collision
         * @whatsnew: resizing a hero
         * @whatsnew: changing a hero's image on-the-fly
         * @whatsnew: manually modifying the scores
         */
        else if (whichLevel == 66) {
            // make a tilt level with just a hero
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addCenteredText("Only stars can reach\nthe destination", 255, 255, 255,
                    "arial.ttf", 20);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 29, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Controls.addGoodieCount1(0, "Goodies", 220, 280);

            // the destination won't work until some goodies are collected...
            Destination d = Destination.makeAsBox(46, 2, 2, 2, "colorstar.png");
            d.setActivationScore(4, 1, 3, 0);
            Score.setVictoryDestination(1);

            // Colliding with this star will make the hero into a star
            Obstacle o = Obstacle.makeAsBox(30, 0, 3, 3, "stars.png");
            o.setPhysics(1, 0, 1);
            o.setHeroCollisionTrigger(0, 0, 0, 0, 0);
        }

        /**
         * @level: 67
         * @description: This level shows how to use countdown timers to win a
         *               level, tests some color features, and introduces a
         *               vector throw mechanism with fixed velocity
         */
        else if (whichLevel == 67) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press anywhere\nto throw a ball", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsBox(1, 0, 46, 1, "greenball.png");
            Controls.addWinCountdown(25, 28, 250, "arial.ttf", 192, 192, 192, 16);
            Score.setVictoryDestination(1);

            PauseScene.addCenteredText("Game Paused", 255, 255, 255, "arial.ttf", 32);
            Controls.addPauseButton(0, 300, 20, 20, "red.png");

            // draw a button for throwing projectiles in many directions
            Controls.addVectorThrowButton(0, 0, 480, 320, "", h, 100);

            // set up our pool of projectiles. The main challenge here is that
            // the farther from the
            // hero we press, the faster the projectile goes, so we multiply the
            // velocity by .3 to
            // slow it down a bit
            Projectile.configure(100, 1, 1, "greyball.png", 30, 0, 0, 1, 1, 0, true);
            Projectile.setRange(50);
            Projectile.setFixedVectorThrowVelocity(5);

        }

        /**
         * @level: 68
         * @description Test hovering heroes that stop hovering after a press
         * @whatsnew: setCanFall to allow an entity to be subject to gravity
         *            without having a pre-set motion
         */
        else if (whichLevel == 68) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press anywhere\nto throw a ball", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsBox(21, 23, 3, 3, "greenball.png");
            // note that if we did hover, then flickable, this would not work
            // correctly, because setFlickable will
            // re-enable gravity on the hero after setHover temporarily disables
            // it.
            h.setFlickable(0.7f);
            h.setHover(21, 23);
            // place an enemy, let it fall
            Enemy e = Enemy.makeAsCircle(31, 25, 3, 3, "redball.png");
            e.setCanFall();

            Destination.makeAsCircle(25, 25, 5, 5, "mustardball.png");
            Score.setVictoryDestination(1);
        }

        /**
         * @level: 69
         * @description: this level shows that not all obstacles cause the hero
         *               to be able to jump again
         * @whatsnew: obstacles that don't re-enable jumping
         */
        else if (whichLevel == 69) {
            // set up a standard side scroller with tilt:
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Press the hero to\nmake it jump", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 1);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // make a hero
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.5f, 0, 0.6f);
            h.setMoveByTilting();
            // this says that touching makes the hero jump
            h.setTouchToJump();
            // this is the force of a jump. remember that up is negative, not
            // positive.
            h.setJumpImpulses(0, 15);
            Level.setCameraChase(h);

            // draw a few walls
            // hero can jump while on this obstacle
            Obstacle.makeAsBox(10, 3, 10, 1, "red.png");
            // hero can't jump while on this obstacle
            Obstacle o = Obstacle.makeAsBox(40, 3, 10, 1, "red.png");
            o.setReJump(false);
        }

        /**
         * @level: 70
         * @description: A test of the PokePath feature
         * @whatsnew: setPokePath to move an entity along a path / else if
         *            (whichLevel == 70) { // start by setting everything up
         *            just like in level 1 Level.configure(48, 32);
         *            Physics.configure(0, 0); Tilt.enable(10, 10); Hero h =
         *            Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
         *            h.setMoveByTilting(); h.setPokePath(4);
         *            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
         *            Score.setVictoryDestination(1); Util.drawBoundingBox(0, 0,
         *            48, 32, "red.png", 0, 0, 0);
         *            PreScene.addText("Poke the hero, then\n where you want it\nto go."
         *            , "arial.ttf", 1); } /**
         * @level: 71
         * @description: A test of the PokeVelocity feature
         * @whatsnew: setPokeVelocity to move an entity along a path
         * @whatsnew: draw an image directly onto the heads-up display
         */
        else if (whichLevel == 71) {
            // start by setting everything up just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "stars.png");
            h.setCanFaceBackwards();
            h.setPokePath(4, false, true, true);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Controls.addImage(40, 40, 40, 40, "red.png");
            Score.setVictoryDestination(1);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            PreScene.addText("Poke the hero, then\n where you want it\nto go.", 50, 50, 255, 255,
                    255, "arial.ttf", 32);
        }

        /**
         * @level: 72
         * @description: This level tests sticky obstacles
         * @whatsnew: sticky obstacles. Note that the obstacle must have more
         *            density than the hero for these to work correctly.
         */
        else if (whichLevel == 72) {
            // set up a basic side scroller without tilt
            Level.configure(48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press screen borders\nto move the hero", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, 0, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            // the hero can jump
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            h.setPhysics(2, 0, .1f);

            // create a destination
            Destination.makeAsCircle(20, 15, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            Obstacle o = Obstacle.makeAsBox(10, 5, 8, .5f, "red.png");
            o.setRoute(new Route(5).to(10, 5).to(5, 15).to(10, 25).to(15, 15).to(10, 5), 5, true);
            o.setPhysics(100, 0, .1f);
            // Note: this is only sticky on the top!
            o.setSticky(true, false, false, false);

            Obstacle o2 = Obstacle.makeAsBox(30, 5, 8, .5f, "red.png");
            o2.setRoute(new Route(5).to(30, 5).to(25, 15).to(30, 25).to(45, 15).to(30, 5), 5, true);
            o2.setPhysics(100, 0, 1f);

            // draw some buttons for moving the hero
            Controls.addLeftButton(0, 50, 50, 220, "", 5, h);
            Controls.addRightButton(430, 50, 50, 220, "", 5, h);
        }

        /**
         * @level: 73
         * @description: this level shows how to throw projectiles that rotate
         *               correctly, and how to add one-sided entities
         * @whatsnew: setRotateVectorThrow
         * @whatsnew: setOneSided
         */
        else if (whichLevel == 73) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Press anywhere\nto throw a ball", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(42, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // draw a button for throwing projectiles in many directions
            Controls.addVectorThrowButton(0, 0, 480, 320, "", h, 100);

            // set up our pool of projectiles. The main challenge here is that
            // the farther from the
            // hero we press, the faster the projectile goes, so we multiply the
            // velocity by .3 to
            // slow it down a bit
            Projectile.configure(100, .1f, 3, "red.png", 30, 0, 0, 0, 1, 0, false);
            Projectile.setFixedVectorThrowVelocity(10);
            Projectile.setRange(50);
            Projectile.setRotateVectorThrow();

            // create a box that is easy to fall into, but hard to get out of
            Obstacle bottom = Obstacle.makeAsBox(10, 10, 10, .2f, "red.png");
            bottom.setOneSided(2);
            Obstacle left = Obstacle.makeAsBox(10, 10, .2f, 10, "red.png");
            left.setOneSided(1);
            Obstacle right = Obstacle.makeAsBox(20, 10, .2f, 10, "red.png");
            right.setOneSided(3);
            Obstacle top = Obstacle.makeAsBox(10, 25, 10, .2f, "red.png");
            top.setOneSided(0);
        }

        /**
         * @level: 74
         * @description: this level shows how to use multiple types of goodies
         * @whatsnew: Different activationscores for destinations
         * @whatsnew: different goodiecounts from controls
         * @whatsnew: Goodies with different score types that increment
         * @whatsnew: triggers with multiple types of goodies in the activation
         * @whatsnew: adding to the countdown timer via updateTimerExpiration
         */
        else if (whichLevel == 74) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("Green, Red, and Grey\nballs are goodies", 50, 50, 255, 255, 255,
                    "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setMoveByTilting();

            // the destination requires lots of goodies of different types
            Destination d = Destination.makeAsCircle(42, 31, 1, 1, "mustardball.png");
            d.setActivationScore(1, 1, 3, 0);
            Score.setVictoryDestination(1);

            Controls.addGoodieCount1(0, "blue", 10, 110, "arial.ttf", 0, 255, 255, 16);
            Controls.addGoodieCount2(0, "green", 10, 140, "arial.ttf", 0, 255, 255, 16);
            Controls.addGoodieCount3(0, "red", 10, 170, "arial.ttf", 0, 255, 255, 16);

            Controls.addCountdown(100, "", 250, 30);

            for (int i = 0; i < 3; ++i) {
                Goodie b = Goodie.makeAsCircle(10 * i, 30, 1, 1, "blueball.png");
                b.setScore(1, 0, 0, 0);
                Goodie g = Goodie.makeAsCircle(10 * i + 2.5f, 30, 1, 1, "greenball.png");
                g.setScore(0, 1, 0, 0);
                Goodie r = Goodie.makeAsCircle(10 * i + 6, 30, 1, 1, "redball.png");
                r.setScore(0, 0, 1, 0);
            }

            Obstacle o = Obstacle.makeAsBox(40, 0, 5, 200, "red.png");
            o.setHeroCollisionTrigger(0, 1, 1, 1, 0);
        }

        /**
         * @level: 75
         * @description: this level shows passthrough objects
         * @whatsnew: setting passthrough
         */
        else if (whichLevel == 75) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PreScene.addText("You can walk through the wall", 50, 50, 255, 255, 255, "arial.ttf",
                    32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "stars.png");
            h.setMoveByTilting();
            h.setPassThrough(7); // make sure obstacle has same value

            // the destination requires lots of goodies of different types
            Destination.makeAsCircle(42, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            Enemy e = Enemy.makeAsCircle(42, 1, 5, 4, "red.png");
            e.setChaseSpeed(1, h, true, true);

            Obstacle o = Obstacle.makeAsBox(40, 1, .5f, 20, "red.png");
            o.setPassThrough(7);
        }

        /**
         * @level: 76
         * @description: Demonstrate the use of a turbo boost button
         * @whatsnew: Controls.addTurboButton
         */
        else if (whichLevel == 76) {
            // set up a side scroller, but don't turn on tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, 10);
            PreScene.addText("Press anywhere to speed up", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Destination.makeAsCircle(120, 31, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // note: the bounding box does not have friction, and neither does
            // the hero
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            // make a hero, but don't let it rotate:
            Hero h = Hero.makeAsBox(2, 25, 3, 7, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0);
            // give the hero a fixed velocity
            h.addVelocity(4, 0, false);
            // center the camera a little ahead of the hero, so he is not
            // centered
            h.setCameraOffset(15, 0);
            Level.setCameraChase(h);

            // set up the background
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // draw a turbo boost button that covers the whole screen... make
            // sure its "up" speeds match the hero
            // velocity
            Controls.addTurboButton(0, 0, 480, 320, "", 15, 0, 4, 0, h);
        }

        /**
         * @level: 77
         * @description: Demonstrate a control that doesn't stop the hero upon
         *               release
         * @whatsnew: Controls.addDampenedMotionButton
         */
        else if (whichLevel == 77) {
            // set up a side scroller, but don't turn on tilt
            Level.configure(3 * 48, 32);
            Physics.configure(0, -10);
            PreScene.addText("Press anywhere to speed up", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Destination.makeAsCircle(120, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // note: the bounding box has friction, so the hero will glide
            Util.drawBoundingBox(0, 0, 3 * 48, 32, "red.png", 1, 0, 0);

            // make a hero, but don't let it rotate:
            Hero h = Hero.makeAsBox(2, 1, 3, 7, "greenball.png");
            // center the camera a little ahead of the hero, so he is not
            // centered
            h.setCameraOffset(15, 0);
            Level.setCameraChase(h);

            // set up the background
            Background.setColor(0, 0, 255);
            Background.addHorizontalLayer(.5f, 1, "mid.png", 0);

            // draw a turbo boost button that covers the whole screen... make
            // sure its "up" speeds match the hero
            // velocity
            Controls.addDampenedMotionButton(0, 0, 480, 320, "", 10, 0, 4, h);
        }

        /**
         * @level: 78
         * @description: Demonstrate how onesided and triggers interact
         */
        else if (whichLevel == 78) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Does autojump work?", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(.1f, 0, 0);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            Destination.makeAsCircle(42, 1, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);

            // create a box that is easy to fall into, but hard to get out of
            Obstacle bottom = Obstacle.makeAsBox(10, 5, 10, .2f, "red.png");
            bottom.setOneSided(2);
            bottom.setHeroCollisionTrigger(0, 0, 0, 0, 0);
            bottom.setCollisionEffect(true);
            // make the z index of the bottom -1, so that the hero (index 0)
            // will be drawn on top of the box, not under
            // it
            bottom.setZIndex(-1);
        }

        /**
         * @level: 79
         * @description: A test of extended PokePath features
         * @whatsnew: setKeepPokeEntity to avoid re-touching the hero every time
         *            we want to register a new movement
         * @whatsnew: setPokePathFixedVelocity to always have the same velocity,
         *            regardless of distance
         * @whatsnew: setPokeChaseMode to track movement of poke presses, not
         *            just down presses
         * @whatsnew: Controls.addTriggerControl for an on-screen button that
         *            runs custom code
         */
        else if (whichLevel == 79) {
            // start by setting everything up just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            h.setPokePath(4, true, false, false);
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Score.setVictoryDestination(1);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);
            PreScene.addText("Poke the hero, then\n where you want it\nto go.", 50, 50, 255, 255,
                    255, "arial.ttf", 32);
            Controls.addTriggerControl(40, 40, 40, 40, "red.png", 747);
        }

        /**
         * @level: 80
         * @description: A test of gravity-defying objects
         * @whatsnew: setGravityDefy
         */
        else if (whichLevel == 80) {
            // set up a simple level
            Level.configure(48, 32);
            Physics.configure(0, -10);
            Tilt.enable(10, 0);
            PreScene.addText("Testing Gravity Defy?", 50, 50, 255, 255, 255, "arial.ttf", 32);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.disableRotation();
            h.setPhysics(1, 0, 0.6f);
            h.setMoveByTilting();
            h.setJumpImpulses(0, 15);
            h.setTouchToJump();
            Destination d = Destination.makeAsCircle(42, 14, 1, 1, "mustardball.png");
            // note: it must not be immune to physics, or it will pass through
            // the bounding box
            d.setAbsoluteVelocity(-2, 0, false);
            d.setGravityDefy();
            Score.setVictoryDestination(1);
        }
    }

    /**
     * Describe how each help scene ought to be drawn. Every game must implement
     * this method to describe how each help scene should appear. Note that you
     * *must* specify the maximum number of help scenes for your game in the
     * res/values/gameconfig.xml file. If you specify "0", then you can leave
     * this code blank. NB: A real game would need to provide better help. This
     * is just a demo.
     * 
     * @param whichScene The help scene being drawn. The game engine will set
     *            this value to indicate which scene needs to be drawn.
     */
    @Override
    public void configureHelpScene(int whichScene) {

        // Our first scene describes the color coding that we use for the
        // different entities in the game
        if (whichScene == 1) {
            HelpLevel.configure(0, 0, 0);
            HelpLevel.drawText(100, 5, "The levels of this game demonstrate\nthe features of ALE");

            HelpLevel.drawPicture(50, 60, 30, 30, "greenball.png");
            HelpLevel.drawText(100, 60, "This is a hero");

            HelpLevel.drawPicture(50, 100, 30, 30, "blueball.png");
            HelpLevel.drawText(100, 100, "This is an object you can collect");

            HelpLevel.drawPicture(50, 140, 30, 30, "redball.png");
            HelpLevel.drawText(100, 140, "This is an enemy.  Beware!");

            HelpLevel.drawPicture(50, 180, 30, 30, "mustardball.png");
            HelpLevel.drawText(100, 180, "This is a destination that the\nhero(s) must reach");

            HelpLevel.drawPicture(50, 220, 30, 30, "purpleball.png");
            HelpLevel.drawText(100, 220, "This is an obstacle you can \ncollide with");

            HelpLevel.drawPicture(50, 260, 30, 30, "greyball.png");
            HelpLevel.drawText(100, 260, "This is a projectile you can throw");
        }
        // Our second help scene is just here to show that it is possible to
        // have more than one help scene.
        else if (whichScene == 2) {
            HelpLevel.configure(255, 255, 0);
            HelpLevel.drawText(100, 5, "Be sure to read the ALEDemoGame.java code\n"
                    + "while you play, so you can see\n" + "how the game works", 55, 110, 165,
                    "arial.ttf", 14);
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
     * is hit, this code will run. If the TriggerObstacle has a unique ID (for
     * example, its 'x' coordinate), then we can use that id to know where on
     * the screen we are, and we can draw the next part of the level correctly.
     * 
     * @param id The ID of the obstacle that was hit by the hero
     * @param whichLevel The current level
     */
    @Override
    public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h) {
        // obstacle trigger code for level 63
        if (whichLevel == 63) { // the first trigger just causes us to make a
                                // new trigger a little farther out
            if (id == 0) {
                Obstacle oo = Obstacle.makeAsBox(60, 0, 1, 32, "purpleball.png");
                oo.setPhysics(1, 0, 1);
                oo.setHeroCollisionTrigger(1, 1, 0, 0, 0);
                Goodie.makeAsCircle(45, 1, 1, 1, "blueball.png");
                o.remove(false);
            }
            // same for the second trigger... note that we also need to make a
            // goodie, because we've chosen to require more goodies to activate
            // these collisiontrigger obstacles
            else if (id == 1) {
                Obstacle oo = Obstacle.makeAsBox(90, 0, 1, 32, "purpleball.png");
                oo.setPhysics(1, 0, 1);
                oo.setHeroCollisionTrigger(2, 2, 0, 0, 0);
                Goodie.makeAsCircle(75, 21, 1, 1, "blueball.png");
                o.remove(false);
            }
            // same thing
            else if (id == 2) {
                Obstacle oo = Obstacle.makeAsBox(120, 0, 1, 32, "purpleball.png");
                oo.setPhysics(1, 0, 1);
                oo.setHeroCollisionTrigger(3, 3, 0, 0, 0);
                Goodie.makeAsCircle(105, 1, 1, 1, "blueball.png");
                o.remove(false);
            }
            // well done... now print a message and make the destination
            else if (id == 3) {
                PreScene.addText("The destination is\nnow available", 50, 50, 255, 255, 255,
                        "arial.ttf", 32);
                PreScene.setExpire(1);
                Destination.makeAsCircle(120, 20, 2, 2, "mustardball.png");
                o.remove(false);
            }
        } else if (whichLevel == 66) {
            // here's a simple way to increment a goodie count
            Score.incrementGoodiesCollected2();
            // here's a way to set a goodie count
            Score.setGoodiesCollected3(3);
            // here's a way to read and write a goodie count
            Score.setGoodiesCollected1(4 + Score.getGoodiesCollected1());
            // get rid of the star, so we know it's been used
            o.remove(true);
            h.resize(h.getXPosition(), h.getYPosition(), 5, 5);
            h.setImage("stars.png");
        } else if (whichLevel == 74) {
            // add 15 seconds to the timer
            Controls.updateTimerExpiration(15);
            o.remove(true);
        } else if (whichLevel == 78) {
            h.setAbsoluteVelocity(h.getXVelocity(), 5, false);
            return;
        }
    }

    /**
     * If a game uses Entities that are touch triggers, it must provide this to
     * specify what to do when such an obstacle is touched by the user The idea
     * behind this mechanism is that it allows the creation of more interactive
     * games, since there can be items to unlock, treasure chests to open, and
     * other such behaviors.
     * 
     * @param id The ID of the obstacle that was hit by the hero
     * @param whichLevel The current level
     */
    @Override
    public void onTouchTrigger(int id, int whichLevel, PhysicsSprite o) {
        // In level 64, we draw a bunch of goodies when the obstacle is touched.
        // This is supposed to be like having a treasure chest open up.
        if (whichLevel == 64) {
            if (id == 39) {
                o.remove(false);
                for (int i = 0; i < 3; ++i)
                    Goodie.makeAsCircle(9 * i, 20 - i, 2, 2, "blueball.png");
            }
        }
    }

    /**
     * If a game uses timer triggers, it must provide this to specify what to do
     * when a timer expires.
     * 
     * @param id The ID of the obstacle that was hit by the hero
     * @param whichLevel The current level
     */
    @Override
    public void onTimeTrigger(int id, int whichLevel) {
        // here's the code for level 62
        if (whichLevel == 62) { // after first trigger, print a message, draw an
                                // enemy, register a new timer
            if (id == 0) {
                PreScene.addText("Ooh... a draggable enemy", 50, 50, 255, 255, 0, "arial.ttf", 12);
                PreScene.setExpire(1);

                // make a draggable enemy
                Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
                e3.setPhysics(1.0f, 0.3f, 0.6f);

                e3.setCanDrag(true);

                Level.setTimerTrigger(1, 3);
            }
            // after second trigger, draw an enemy who disappears on touch,
            // register a new timer
            else if (id == 1) {
                PreScene.addText("Touch the enemy and it will go away", 50, 50, 255, 0, 255,
                        "arial.ttf", 12);
                PreScene.setExpire(1);
                Enemy e4 = Enemy.makeAsCircle(35, 5, 2, 2, "redball.png");
                e4.setPhysics(1.0f, 0.3f, 0.6f);
                e4.setDisappearOnTouch();
                Level.setTimerTrigger(2, 3);
            }
            // after third trigger, draw an enemy, a goodie, and a destination,
            // all with fixed velocity
            else if (id == 2) {
                PreScene.addText("Now you can see the rest of the level", 50, 50, 255, 255, 0,
                        "arial.ttf", 12);
                PreScene.setExpire(1);
                Destination d = Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
                d.addVelocity(-.5f, -1, false);

                Enemy e5 = Enemy.makeAsCircle(35, 15, 2, 2, "redball.png");
                e5.setPhysics(1.0f, 0.3f, 0.6f);
                e5.addVelocity(4, 4, false);
                Goodie gg = Goodie.makeAsCircle(10, 10, 2, 2, "blueball.png");
                gg.addVelocity(5, 5, false);
            }
        }
    }

    /**
     * If you want to have enemy timertriggers, then you must override this to
     * define what happens when the timer expires
     * 
     * @param id The id that was assigned to the timer that exired
     * @param whichLevel The current level
     * @param e The enemy to modify
     */
    @Override
    public void onEnemyTimeTrigger(int id, int whichLevel, Enemy e) {
        if (whichLevel == 48) {
            // the ID represents the number of remaining reproductions for the
            // current enemy (e), so that we don't reproduce forever (note that
            // we could, if we wanted to...)
            Enemy left = Enemy.makeAsCircle(e.getXPosition() - 2 * id, e.getYPosition() + 2 * id,
                    e.getWidth(), e.getHeight(), "redball.png");
            left.setDisappearSound("lowpitch.ogg");
            Enemy right = Enemy.makeAsCircle(e.getXPosition() + 2 * id, e.getYPosition() + 2 * id,
                    e.getWidth(), e.getHeight(), "redball.png");
            right.setDisappearSound("lowpitch.ogg");
            if (id > 0) {
                Level.setEnemyTimerTrigger(id - 1, 2, left);
                Level.setEnemyTimerTrigger(id - 1, 2, e);
                Level.setEnemyTimerTrigger(id - 1, 2, right);
            }
        } else if (whichLevel == 49) {
            // in this case, every enemy will produce one offspring on each
            // timer
            Enemy e2 = Enemy.makeAsCircle(e.getXPosition(), e.getYPosition(), e.getWidth(),
                    e.getHeight(), "redball.png");
            e2.setPhysics(1.0f, 0.3f, 0.6f);
            e2.setMoveByTilting();
            if (id > 0) {
                Level.setEnemyTimerTrigger(id - 1, 2, e);
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
     */
    @Override
    public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e) {
        if (whichLevel == 65) {
            PreScene.addText("good job, here's a prize", 50, 50, 88, 226, 160, "arial.ttf", 16);
            PreScene.setExpire(1);
            // use random numbers to figure out where to draw a goodie as a
            // reward
            Goodie.makeAsCircle(Util.getRandom(46), Util.getRandom(30), 2, 2, "blueball.png");
        }
    }

    /**
     * If you want to have obstacletriggers, then you must override this to
     * define what happens when an enemy hits the obstacle
     * 
     * @param whichLevel The id that was assigned to the enemy who was defeated
     * @param o The obstacle involved in the collision
     * @param e The enemy involved in the collision
     */
    @Override
    public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e) {
        if (whichLevel == 56) {
            if (e.getInfoText() == "weak")
                return;
            if (id == 0) {
                e.defeat(true);
                o.remove(true);
            }
            if (id == 1) {
                e.defeat(true);
            }
        }
        if (whichLevel == 65) {
            if (e.getInfoText() == "weak") {
                e.defeat(true);
            }
        }
    }

    /**
     * If you want to have obstacletriggers, then you must override this to
     * define what happens when a projectile hits the obstacle
     * 
     * @param whichLevel The id that was assigned to the enemy who was defeated
     * @param o The obstacle involved in the collision
     * @param p The projectile involved in the collision
     */
    @Override
    public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p) {
        /*
         * if (whichLevel == 47) { // do nothing... as long as there is a
         * collision registered, the projectile won't disappear... }
         */
    }

    /**
     * If you want to do something when the level ends (like record a high
     * score), you will need to override this method
     * 
     * @param win true if the level was won, false otherwise
     */
    @Override
    public void levelCompleteTrigger(boolean win) {
    }

    /**
     * If you use TriggerControls, you must override this to define what happens
     * when the control is pressed
     * 
     * @param id The id that was assigned to the Control
     * @param whichLevel The _current level
     */
    @Override
    public void onControlPressTrigger(int id, int whichLevel) {
        if (whichLevel == 79) {
            if (id == 747)
                PreScene.addText("Hello", 50, 50, 2550, 2550, 255, "arial.ttf", 20);
            PreScene.setExpire(1);
        }
    }

    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public LOLConfiguration config() {
        return new Config();
    }

    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public SplashConfiguration splashConfig() {
        return new SplashConfig();
    }
}
