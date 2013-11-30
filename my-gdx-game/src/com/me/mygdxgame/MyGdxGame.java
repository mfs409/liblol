package com.me.mygdxgame;

// TODO: are all boxes and circles being drawn to the correct proportion? Our
// Obstacles are... not sure about others... and the ctors aren't consistent!

// STATUS: ready to start working on level 14

// NB: the 'getx' and 'gety' methods of physicssprite return center coords of
// body, not coords of the bottom left of the sprite

import edu.lehigh.cse.ale.*;

public class MyGdxGame extends ALE
{
    @Override
    public void nameResources()
    {
        Media.registerImage("greenball.png");
        Media.registerImage("mustardball.png");
        Media.registerImage("red.png");
        Media.registerImage("redball.png");
        Media.registerImage("blueball.png");
        Media.registerImage("purpleball.png");
        Media.registerImage("msg1.png");
        Media.registerImage("msg2.png");
        Media.registerImage("splash.png");

        Media.registerSound("hipitch.ogg");
        Media.registerSound("losesound.ogg");

        Media.registerMusic("tune.ogg", true);
    }

    @Override
    public void configureLevel(int whichLevel)
    {
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
            Level.setVictoryDestination(1);
        }
        else if (whichLevel == 2) {
            // start by setting everything up just like in level 1
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Level.setVictoryDestination(1);

            // new: add a bounding box so the hero can't fall off the screen
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 0, 0, 0);

            // new: change the text that we display when the level is won
            Level.setWinText("Good job!");

            // new: add a pop-up message that shows for one second at the
            // beginning of the level
            PopUpScene.showTextTimed("Reach the destination\nto win this level.", 1);
        }

        /**
         * @level: 3
         * 
         * @description: In this level, we change the physics from level 2 so
         *               that things roll and bounce a little bit more nicely
         * 
         * @whatsnew: the hero and bounding box now have nonzero physics so that
         *            things are a little bit more smooth
         * 
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
            Level.setVictoryDestination(1);

            // new: the bounding box now also has nonzero density,
            // elasticity, and friction
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);

            PopUpScene.showTextTimed("Reach the destination\nto win this level.", 1);
        }

        /**
         * @level: 4
         * 
         * @description: This level shows that we the natural behavior is for
         *               each destination to only hold one hero
         * 
         * @whatsnew: we are showing a popup image instead of popup text
         * 
         * @whatsnew: there are two heroes
         * 
         * @whatsnew: it takes two heroes to reach destinations before the level
         *            is won
         */
        else if (whichLevel == 4) {

            // start by setting up the level boundaries, tilt, and bounding box
            Level.configure(46, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 46, 32, "red.png", 1, .3f, 1);

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
            Level.setVictoryDestination(2);

            // Let's show msg1.png instead of text. Note that we had to
            // register it in registerMedia()
            PopUpScene.showImageTimed("msg1.png", 3, 0, 0, 460, 320);
        }

        /**
         * @level: 5
         * 
         * @description: This level demonstrates that we can have many heroes
         *               that can reach the same destination
         * 
         * @whatsnew: the destination can hold two heroes
         * 
         * @whatsnew: when the hero reaches the destination, we play a sound
         */
        else if (whichLevel == 5) {
            // begin by configuring the level and heroes just like in level 4
            Level.configure(46, 32);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 46, 32, "red.png", 1, .3f, 1);
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
            Level.setVictoryDestination(2);

            // Change the pop-up message slightly
            PopUpScene.showTextTimed("All heroes must\nreach the destination", 3);
        }

        /**
         * @level: 6
         * 
         * @description: this demonstrates an alternative way of using tilt.
         *               Instead of tilt representing a force applied to
         *               entities, tilt now represents a velocity to apply to
         *               those entities. Note that this form of tilt behaves
         *               oddly when we have friction on the ball and wall.
         * 
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
            Level.setVictoryDestination(1);

            // change the behavior or tilt
            Tilt.setAsVelocity(true);

            // and print a popup to tell the user what's going on...
            PopUpScene.showTextTimed("A different way\nto use tilt.", 1);
        }

        /**
         * @level: 7
         * 
         * @description: This level adds an enemy, to demonstrate that we can
         *               make it possible to lose a level
         * 
         * @whatsnew: there is a stationary enemy
         * 
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
            Level.setVictoryDestination(1);

            // draw an enemy:
            Enemy e = Enemy.makeAsCircle(25, 25, 2, 2, "redball.png");
            e.setPhysics(1.0f, 0.3f, 0.6f);

            // display a message that stays until it is pressed
            PopUpScene.showTextAndWait("Avoid the enemy and\nreach the destination");
        }

        /**
         * @level: 8
         * 
         * @description: This level explores a bit more of what we can do with
         *               enemies, by having an enemy with a fixed path.
         * 
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
            Level.setVictoryDestination(1);

            // draw an enemy that can move
            Enemy e = Enemy.makeAsCircle(25, 5, 2, 2, "redball.png");
            e.setPhysics(100.0f, 0.3f, 0.6f);

            // attach a path to the enemy. It starts at (25, 25) and moves to
            // (25, 2). This means it has *2* points on its route. Notice that
            // it isn't going to move quite as we'd like
            
            e.setRoute(new Route(2).to(25, 25).to(25, 2), 2, true);

            // display a message that stays until it is pressed
            PopUpScene.showTextAndWait("Avoid the enemy and\nreach the destination");
        }

        /**
         * @level: 9
         * 
         * @description: This level explores a bit more of what we can do with
         *               paths.
         * 
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
            Level.setVictoryDestination(1);

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
            PopUpScene.showTextAndWait("Avoid the enemy and\nreach the destination");
        }

        /**
         * @level: 10
         * 
         * @description: This level fleshes out a bunch of additional enemy
         *               features, as well as sound features
         * 
         * @whatsnew: any entity can be controlled by tilt, so here we add an
         *            enemy that is controlled by tilt.
         * 
         * @whatsnew: any entity can have a continuous rotation, so we do that
         *            to the Destination
         * 
         * @whatsnew: this level also demonstrates background music, and sounds
         *            to play on victory or defeat.
         * 
         * @whatsnew: this level shows a popup message that uses an image, and
         *            that does not go away until it is touched
         * 
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
            Level.setVictoryDestination(1);

            // update: draw an enemy who moves via tilt
            Enemy e3 = Enemy.makeAsCircle(35, 25, 2, 2, "redball.png");
            e3.setPhysics(1.0f, 0.3f, 0.6f);
            e3.setMoveByTilting();

            // show a message that must be touched in order to remove it
            PopUpScene.showImageAndWait("msg2.png", 10, 10, 460, 320);

            // configure some sounds Level.setWinSound("winsound.ogg");
            Level.setLoseSound("losesound.ogg");
            Level.setMusic("tune.ogg");

            // custom text for when the level is lost
            Level.setLoseText("Better luck next time...");
        }

        /**
         * @level: 11
         * 
         * @description: This shows how it is possible to have a level that is
         *               larger than one screen. It also demonstrates zoom
         *               buttons, and introduces the notion of invisibile
         *               controls.
         * 
         * @whatsnew: zoom buttons on the left and right halves of the screen
         * 
         * @whatsnew: the level is larger than the screen, but the screen always
         *            shows the hero
         */
        else if (whichLevel == 11) {

            // make the level really big
            Level.configure(400, 300);
            Physics.configure(0, 0);
            Tilt.enable(10, 10);
            Util.drawBoundingBox(0, 0, 400, 300, "red.png", 0, 0, 0);

            PopUpScene.showTextTimed("Press left to zoom out\nright to zoom in", 1);

            // put the hero and destination far apart
            Hero h = Hero.makeAsCircle(2, 2, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(329, 281, 10, 10, "mustardball.png");
            Level.setVictoryDestination(1);

            // add zoom buttons
            
            // TODO: chase shapes + zoom can lead to funny behaviors (e.g., try
            // zooming in when we're in bottom left corner; a solution is to
            // incorporate zoom into the computations in the chase shape code
            Controls.addZoomInButton(240, 0, 240, 320, "", .25f);
            Controls.addZoomOutButton(0, 0, 240, 320, "", 8);
            
            // chase the hero with the camera
            Level.setCameraChase(h);
        }

        /**
         * @level: 12
         * 
         * @description: this level introduces obstacles, and also shows the
         *               difference between "box" and "circle" physics
         * 
         * @whatsnew: stationary obstacles
         * 
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
            Level.setVictoryDestination(1);

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
            PopUpScene.showTextTimed("An obstacle's appearance may\nnot match its physics", 1);
        }

        /**
         * @level: 13
         * 
         * @description: this level just plays around with physics a little bit,
         *               to show how friction and elasticity can do interesting
         *               things.
         */
        else if (whichLevel == 13) {            
            Level.configure(48, 32);Physics.configure(0, 0);
            Tilt.enable(10, 10);
            PopUpScene.showTextTimed("These obstacles have\ndifferent physics\nparameters", 1);
            Util.drawBoundingBox(0, 0, 48, 32, "red.png", 1, .3f, 1);
            Hero h = Hero.makeAsCircle(4, 7, 3, 3, "greenball.png");
            h.setPhysics(.1f, 0, 0.6f);
            h.setMoveByTilting();
            Destination.makeAsCircle(29, 6, 1, 1, "mustardball.png");
            Obstacle o1 = Obstacle.makeAsCircle(0, 0, 3.5f, 3.5f, "purpleball.png");
            o1.setPhysics(0, 100, 0);
            Obstacle o2 = Obstacle.makeAsCircle(10, 10, 3.5f, 3.5f, "purpleball.png");
            o2.setPhysics(10, 0, 100);
            Level.setVictoryDestination(1);
        }

        /**
         * @level: 14
         * 
         * @description: This level introduces goodies. Goodies are something
         *               that we collect. We can make the collection of goodies
         *               lead to changes in the behavior of the game, and in
         *               this example, the collection of goodies "enables" a
         *               destination.
         * 
         * @whatsnew: introduce goodies
         * 
         * @whatsnew: destination does not work until enough goodies are
         *            collected
         * 
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
            Goodie.makeAsCircle(0, 30, 1, 1, "blueball.png");
            Goodie.makeAsCircle(0, 15, 1, 1, "blueball.png");

            // here we create a destination. Note that we now set its activation
            // score to 2, so that you must
            // collect two goodies before the destination will "work"
            Destination d = Destination.makeAsCircle(29, 1, 1, 1, "mustardball.png");
            d.setActivationScore(2);

            Level.setVictoryDestination(1);

            // let's put a display on the screen to see how many goodies we've
            // collected
            Controls.addGoodieCount(2, "Goodies", 220, 280);
            PopUpScene.showTextTimed("You must collect\ntwo blue balls", 1);
             
        }

        /**
         * @level: 15
         * 
         * @description: All entities can have a fixed motion path. Here, we'll
         *               attach a path to a few kinds of entities to show this
         *               ability
         * 
         * @whatsnew: show that destinations, goodies, and obstacles can have
         *            motion paths too
         * 
         * @whatsnew: also show how to change the color and size of on-screen
         *            text, and show how when the goodie counter's max field is
         *            0, the display is just the number of goodies collected
         */
        else if (whichLevel == 15) {
            /*
             * // set up a basic tilt-based level Level.configure(460, 320, 0,
             * 0); Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Every entity can move...", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "invis.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting(); // make a
             * destination that moves, and that requires one goodie to be
             * collected before it works Destination d =
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * d.setActivationScore(1); d.setRoute(new Route(3).to(290,
             * 60).to(290, 260).to(290, 60), 4, true);
             * Level.setVictoryDestination(1);
             * 
             * // make an obstacle that moves Obstacle o = Obstacle.makeAsBox(0,
             * 0, 35, 35, "purpleball.png"); o.setPhysics(0, 100, 0);
             * o.setRoute(new Route(3).to(0, 0).to(100, 100).to(0, 0), 2, true);
             * 
             * // make a goodie that moves Goodie g = Goodie.makeAsCircle(50,
             * 50, 20, 20, "blueball.png"); g.setRoute(new Route(5).to(50,
             * 50).to(50, 250).to(250, 250).to(90, 90).to(50, 50), 10, true);
             * 
             * // draw a goodie counter in light blue with a 12-point font
             * Controls.addGoodieCount(0, "Goodies", 220, 280, 60, 70, 255, 12);
             */
        }

        /**
         * @level: 16
         * 
         * @description: show how we can make a level that is won by collecting
         *               goodies, and show how we can set a time limit.
         * 
         * @whatsnew: victory by goodie count
         * 
         * @whatsnew: lose by running out of time
         */
        else if (whichLevel == 16) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Collect all\nblue balls\nto win", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // draw 5 goodies Goodie.makeAsCircle(5, 5, 10, 10,
             * "blueball.png"); Goodie.makeAsCircle(55, 15, 10, 10,
             * "blueball.png"); Goodie.makeAsCircle(105, 25, 10, 10,
             * "blueball.png"); Goodie.makeAsCircle(155, 35, 10, 10,
             * "blueball.png"); Goodie.makeAsCircle(205, 45, 10, 10,
             * "blueball.png");
             * 
             * // indicate that we win by collecting enough goodies
             * Level.setVictoryGoodies(5); // put the goodie count on the screen
             * Controls.addGoodieCount(5, "Goodies", 220, 280); // put a
             * countdown on the screen Controls.addCountdown(15, "Time Up!",
             * 200, 5);
             */
        }

        /**
         * @level: 17
         * 
         * @description: This level shows how "obstacles" need not actually
         *               impede the hero's movement. Here, we attach
         *               "damping factors" to the hero, which let us make the
         *               hero speed up or slow down based on interaction with
         *               the obstacle. This level also adds a stopwatch.
         *               Stopwatches don't have any meaning, but they are nice
         *               to have anyway...
         * 
         * @whatsnew: dampening factors on obstacles
         * 
         * @whatsnew: added a stopwatch
         */
        else if (whichLevel == 17) {
            /*
             * // set up a basic level with a tilt hero and a destination
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10);
             * PopUpScene.showTextTimed
             * ("Obstacles as zoom\nstrips, friction pads\nand repellers", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // add a stopwatch... note that there are two ways to add a
             * stopwatch, the other of // which allows for configuring the font
             * Controls.addStopwatch(50, 50);
             * 
             * // now draw three obstacles. Note that they have different
             * dampening factors // // one important thing to notice is that
             * since we place these on the screen *after* we // place the hero
             * on the screen, the hero will go *under* these things.
             * 
             * // this obstacle's dampening factor means that on collision, the
             * hero's velocity is // multiplied by -1... he bounces off at an
             * angle. Obstacle o = Obstacle.makeAsCircle(100, 100, 35, 35,
             * "purpleball.png"); o.setPhysics(1, 0, 1); o.setDamp(-1);
             * 
             * // this obstacle accelerates the hero... it's like a turbo
             * booster o = Obstacle.makeAsCircle(200, 100, 35, 35,
             * "purpleball.png"); o.setPhysics(1, 0, 1); o.setDamp(5);
             * 
             * // this obstacle slows the hero down... it's like running on
             * sandpaper o = Obstacle.makeAsBox(300, 100, 35, 35,
             * "purpleball.png"); o.setPhysics(1, 0, 1); o.setRotationSpeed(2);
             * o.setDamp(0.2f);
             */
        }

        /**
         * @level: 18
         * 
         * @description: This level shows that it is possible to give heroes and
         *               enemies different strengths, so that a hero doesn't
         *               disappear after a single collision
         * 
         * @whatsnew: when an enemy defeats a hero, we can customize the message
         *            that prints
         * 
         * @whatsnew: setting the strength of heroes, and the damage caused by
         *            enemies. The default is for enemies to have "2" units of
         *            damage, and heroes to have "1" unit of strength, so that
         *            any collision defeats the hero without removing the enemy.
         * 
         * @whatsnew: show how much strength the hero has left. Note that if
         *            there were several heroes in this level, we'd only see the
         *            strength of the last hero
         */
        else if (whichLevel == 18) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("The hero can defeat \nup to two enemies..."
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // draw a strength meter Controls.addStrengthMeter("Strength",
             * 220, 280);
             * 
             * // draw a hero and give it strength of 10 Hero h =
             * Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setStrength(10);
             * h.setMoveByTilting();
             * 
             * // our first enemy stands still: Enemy e =
             * Enemy.makeAsCircle(250, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRotationSpeed(1);
             * e.setDamage(4); e.setDefeatHeroText("How did you hit that?");
             * 
             * // our second enemy moves along a path e =
             * Enemy.makeAsCircle(350, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRoute(new Route(3).to(350,
             * 250).to(150, 250).to(350, 250), 3, true); e.setDamage(4);
             * e.setDefeatHeroText("Stay out of my way");
             * 
             * // our third enemy moves with tilt, which makes it hardest to
             * avoid e = Enemy.makeAsCircle(350, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setMoveByTilting();
             * e.setDamage(4); e.setDefeatHeroText("You can't run");
             */
        }

        /**
         * @level: 19
         * 
         * @description: This level shows that we can win a level by defeating
         *               all enemies
         * 
         * @whatsnew: ability to win by defeating enemies
         */
        else if (whichLevel == 19) {
            /*
             * // set up a basic level with a tilt hero Level.configure(460,
             * 320, 0, 0); Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("You have 10 seconds\nto defeat the enemies"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * 
             * // give the hero enough strength that this will work... Hero h =
             * Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setStrength(10);
             * h.setMoveByTilting();
             * 
             * // draw a few enemies Enemy e = Enemy.makeAsCircle(250, 250, 20,
             * 20, "redball.png"); e.setPhysics(1.0f, 0.3f, 0.6f);
             * e.setRotationSpeed(1); e.setDamage(4); e =
             * Enemy.makeAsCircle(350, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setMoveByTilting();
             * e.setDamage(4);
             * 
             * // put a countdown on the screen Controls.addCountdown(10,
             * "Time Up!", 200, 5);
             * 
             * // indicate that defeating enemies is the key to success
             * Level.setVictoryEnemyCount();
             */
        }

        /**
         * @level: 20
         * 
         * @description: this level shows that a goodie can increase the hero's
         *               strength, and that we can win by defeating a specific
         *               number of enemies
         * 
         * @whatsnew: goodies that give the hero more strength
         * 
         * @whatsnew: winning without defeating all the enemies
         */
        else if (whichLevel == 20) {
            /*
             * // set up a basic level that can be won by defeating 1 enemy
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10);
             * PopUpScene.showTextTimed
             * ("Collect blue balls\nto increse strength", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * 
             * // our default hero only has "1" strength Hero h =
             * Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // our default enemy has "2" damage Enemy e =
             * Enemy.makeAsCircle(250, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRotationSpeed(1);
             * e.setDisappearSound("slowdown.ogg");
             * 
             * // a second enemy e = Enemy.makeAsCircle(350, 150, 20, 20,
             * "redball.png"); e.setPhysics(1.0f, 0.3f, 0.6f);
             * 
             * // this goodie gives an extra "5" strength: Goodie g =
             * Goodie.makeAsCircle(0, 300, 10, 10, "blueball.png");
             * g.setStrengthBoost(5); g.setDisappearSound("woowoowoo.ogg");
             * 
             * // track strength, win by defeating one enemy
             * Controls.addStrengthMeter("Strength", 220, 280);
             * 
             * // win by defeating one enemy Level.setVictoryEnemyCount(1);
             * Level.setWinText("Good enough...");
             */
        }

        /**
         * @level: 21
         * 
         * @description: this level introduces the idea of invincibility.
         *               Collecting the goodie makes the hero invincible for a
         *               little while...
         * 
         * @whatsnew: Invincibility from a goodie
         */
        else if (whichLevel == 21) {
            /*
             * // basic setup: Level.configure(460, 320, 0, 0); Tilt.enable(10,
             * 10); PopUpScene.showTextTimed(
             * "The blue ball will\nmake you invincible\nfor 15 seconds", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // draw a few enemies... for (int i = 0; i < 5; ++i) { Enemy e =
             * Enemy.makeAsCircle(50 * i + 10, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRotationSpeed(1);
             * e.setDamage(4); }
             * 
             * // this goodie makes us invincible Goodie g =
             * Goodie.makeAsCircle(300, 300, 10, 10, "blueball.png");
             * g.setInvincibilityDuration(15); g.setRoute(new Route(3).to(300,
             * 300).to(100, 100).to(300, 300), 5, true);
             * g.setRotationSpeed(0.25f);
             * 
             * // we'll still say you win by reaching the destination. Defeating
             * enemies is just for // fun... Destination.makeAsCircle(290, 10,
             * 10, 10, "mustardball.png"); Level.setVictoryDestination(1);
             * Controls.addGoodieCount(0, "Goodies", 220, 280);
             */
        }

        /**
         * @level: 22
         * 
         * @description: Some goodies can "count" for more than one point...
         *               they can even count for negative points.
         * 
         * @whatsnew: goodie values that change how many points we get for
         *            collecting a goodie
         */
        else if (whichLevel == 22) {
            /*
             * // basic configuration Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "Collect 'the right' \nblue balls to\nactivate destination", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting(); Destination d =
             * Destination.makeAsCircle(290, 10, 10, 10, "mustardball.png");
             * d.setActivationScore(7); Level.setVictoryDestination(1);
             * 
             * // create some goodies with special values Goodie g1 =
             * Goodie.makeAsCircle(0, 300, 10, 10, "blueball.png");
             * g1.setScore(-2); Goodie g2 = Goodie.makeAsCircle(0, 150, 10, 10,
             * "blueball.png"); g2.setScore(7);
             * 
             * // create some regular goodies Goodie.makeAsCircle(300, 300, 10,
             * 10, "blueball.png"); Goodie.makeAsCircle(350, 300, 10, 10,
             * "blueball.png");
             * 
             * // print a goodie count to show how the count goes up and down
             * Controls.addGoodieCount(0, "Progress", 220, 280);
             */
        }

        /**
         * @level: 23
         * 
         * @description: this level demonstrates that we can drag entities (in
         *               this case, obstacles), and that we can make rotated
         *               obstacles. The latter could be useful for having angled
         *               walls in a maze
         * 
         * @whatsnew: entites that can be dragged
         * 
         * @whatsnew: oblong obstacles that can be rotated
         */
        else if (whichLevel == 23) {
            /*
             * // basic tilt-based level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "Rotating oblong obstacles\nand draggable obstacles", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // draw an obstacle that we can drag Obstacle o =
             * Obstacle.makeAsBox(0, 0, 35, 35, "purpleball.png");
             * o.setPhysics(0, 100, 0); // watch the strange behaviors that can
             * occur if we use 'true' instead of 'false' in the next line
             * o.setCanDrag(false);
             * 
             * // draw an obstacle that is oblong (due to its width and height)
             * and that is rotated. Note that this needs // to be a box, or it
             * will not have the right underlying shape. o =
             * Obstacle.makeAsBox(120, 120, 35, 5, "purpleball.png");
             * o.setRotation(45);
             */
        }

        /**
         * @level: 24
         * 
         * @description: this level shows how we can use "poking" to move
         *               obstacles. In this case, pressing an obstacle selects
         *               it, and pressing the screen moves the obstacle to that
         *               location. Double-tapping an obstacle removes it.
         * 
         * @whatsnew: pokeable obstacles
         * 
         * @whatsnew: we can draw pictures on the screen that don't have an
         *            influence on the physics of the game
         */
        else if (whichLevel == 24) {
            /*
             * // a basic level: Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); Util.drawBoundingBox(0, 0, 460, 320,
             * "red.png", 1, .3f, 1);
             * PopUpScene.showTextTimed("Touch the obstacle\nto select, then" +
             * "\ntouch a destination", 1);
             * 
             * // draw a picture Util.drawPicture(0, 0, 460, 320,
             * "greenball.png");
             * 
             * // draw the hero and destination after the picture, so that the
             * picture is behind/below // them... Hero h = Hero.makeAsCircle(40,
             * 70, 30, 30, "greenball.png"); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); Destination.makeAsCircle(290, 60, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // make a pokeable obstacle Obstacle o = Obstacle.makeAsBox(0, 0,
             * 35, 35, "purpleball.png"); o.setPhysics(0, 100, 0);
             * o.setPokeable();
             */
        }

        /**
         * @level: 25
         * 
         * @description: In this level, the enemy chases the hero
         * 
         * @whatsnew: an enemy that follows the hero, wherever the hero goes...
         * 
         * @whatsnew: use of drawPictureBehindScene to draw something behind the
         *            hero
         */
        else if (whichLevel == 25) {
            /*
             * // basic setup Level.configure(460, 320, 0, 0); Tilt.enable(10,
             * 10); PopUpScene.showTextTimed("The enemy will chase you", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // create an enemy who chases the hero Enemy e3 =
             * Enemy.makeAsCircle(350, 250, 20, 20, "redball.png");
             * e3.setPhysics(1.0f, 0.3f, 0.6f); e3.setChaseSpeed(2);
             * 
             * // draw a picture late within this block of code, but still cause
             * // the picture to be drawn behind everything else
             * Util.drawPictureBehindScene(0, 0, 460, 320, "greenball.png");
             */
        }

        /**
         * @level: 26
         * 
         * @description: demonstrate that we can make obstacles play sounds
         *               either when we collide with them, or touch them
         * 
         * @whatsnew: touch an obstacle to make it play a sound
         * 
         * @whatsnew: collide with an obstacle to make it play a sound
         * 
         */
        else if (whichLevel == 26) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Touch the purple ball \nor collide with it"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // set up our obstacle so that collision and touch make it play
             * sounds Obstacle o = Obstacle.makeAsCircle(100, 100, 35, 35,
             * "purpleball.png"); o.setPhysics(1, 0, 1);
             * o.setTouchSound("lowpitch.ogg"); o.setCollideSound("hipitch.ogg",
             * 2);
             */
        }

        /**
         * @level: 27
         * 
         * @description: this hero rotates so that it faces in the direction of
         *               movement
         * 
         * @whatsnew: heroes whose rotation changes with their direction
         */
        else if (whichLevel == 27) {
            /*
             * // set up a big screen Level.configure(4 * 460, 2 * 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "The star rotates in\nthe direction of movement", 1);
             * Util.drawBoundingBox(0, 0, 4 * 460, 2 * 320, "red.png", 1, 0, 1);
             * Destination.makeAsCircle(290, 600, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // set up a hero who rotates in the direction of movement Hero h
             * = Hero.makeAsCircle(20, 20, 30, 30, "stars.png"); h.setPhysics(1,
             * 0, 0.6f); h.setRotationByDirection(); h.setMoveByTilting();
             */
        }

        /**
         * @level: 28
         * 
         * @description: This level shows three things. The first is that a
         *               custom motion path can allow things to violate the laws
         *               of physics and pass through other things. The second is
         *               that motion paths can go off-screen. The third is that
         *               PopUpScene is not benign. The only way to win this
         *               level is to hold the phone a certain way, wait three
         *               seconds, and then press the message. While the phone is
         *               held, some gravitational force can be "built up", so
         *               that the hero moves really fast when the level finally
         *               starts
         * 
         * @whatsnew: paths that go off screen
         */
        else if (whichLevel == 28) {
            /*
             * // set up a regular level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextAndWait(
             * "Reach the destination\nto win the game.\n\n(tap to start)");
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(215, 290, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(215, 10, 20, 20, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // this enemy starts from off-screen Enemy e =
             * Enemy.makeAsCircle(10, -200, 440, 440, "redball.png");
             * e.setDefeatHeroText("Ha Ha Ha"); e.setRoute(new Route(3).to(10,
             * -900).to(10, 260).to(10, -200), 3, true);
             */
        }

        /**
         * @level: 29
         * 
         * @description: this level shows that we can draw on the screen to
         *               create obstacles.
         * 
         * @whatsnew: scribblemode for drawing obstacles on the screen
         */
        else if (whichLevel == 29) {
            /*
             * // set up a basic level: Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "Draw on the screen\nto make obstacles appear", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(215, 290, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(215, 10, 20, 20, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // turn on 'scribble mode'. Be sure to play with the last
             * parameter to see the difference between scribbles // that can
             * move and that can't move Obstacle.setScribbleOn("purpleball.png",
             * 3, 15, 15, 0, 0, 0, false);
             */
        }

        /**
         * @level: 30
         * 
         * @description: this level shows that we can "flick" things to move
         *               them
         * 
         * @whatsnew: In this level, notice that we did not enable tilt!
         *            Instead, we specified that there is a default gravity in
         *            the Y dimension pushing everything down. This is much like
         *            gravity on earth. The only way to move things, then, is
         *            via flicking them.
         * 
         * @whatsnew: using flick to move the hero
         */
        else if (whichLevel == 30) {
            /*
             * // create a level with a constant force downward in the Y
             * dimension Level.configure(460, 320, 0, 10);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * 
             * // create a hero who we can flick Hero h = Hero.makeAsCircle(40,
             * 270, 30, 30, "stars.png"); h.setPhysics(1, 0, 0.6f);
             * h.setFlickable(.1f);
             * 
             * // set up a destination Destination.makeAsCircle(300, 100, 25,
             * 25, "mustardball.png"); Level.setVictoryDestination(1);
             */
        }

        /**
         * @level: 31
         * 
         * @description: this level introduces a new concept: side-scrolling
         *               games. just like in level 30, we have a constant force
         *               in the negative direction. However, in this level, we
         *               say that tilt can produce forces in X but not in Y.
         *               Thus we can tilt to move the hero left/right. Note,
         *               too, that the hero will fall to the floor, since there
         *               is a constant downward force, but there is not any
         *               mechanism to apply a Y force to make it move back up.
         * 
         * @whatsnew: we changed the physics, so that now we have a tilt-based
         *            side scroller
         */
        else if (whichLevel == 31) {
            /*
             * // make a long level but not a tall level, and provide a constant
             * downward force: Level.configure(3 * 460, 320, 0, 10); // turn on
             * tilt, but only in the X dimension Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Side scroller / tilt demo", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             */
        }

        /**
         * @level: 32
         * 
         * @description: In the previous level, it was hard to see that the hero
         *               was moving. We can make a background layer to remedy
         *               this situation. Notice that the background uses
         *               transparency to show the blue color for part of the
         *               screen
         * 
         * @whatsnew: background colors and background images
         * 
         * @whatsnew: demonstrate that we can have things hover on the screen
         *            despite the fact that the screen is scrolling
         */
        else if (whichLevel == 32) {
            /*
             * // start by repeating the previous level: Level.configure(3 *
             * 460, 320, 0, 10); // turn on tilt, but only in the X dimension
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Side scroller / tilt demo", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1); // now paint the background blue
             * Background.setColor(0, 0, 255); // draw a picture as a background
             * layer, and give it a "-20" scroll rate
             * Background.addLayer("mid.png", -20, 0, 116); // now indicate that
             * the hero scroll rate is "20" Background.setScrollFactor(20);
             * 
             * // make an obstacle that hovers... Obstacle o =
             * Obstacle.makeAsCircle(100, 100, 50, 50, "blueball.png");
             * o.setHover(100, 100);
             */
        }

        /**
         * @level: 33
         * 
         * @description: this level adds multiple background layers, and it also
         *               allows the hero to jump via touch
         * 
         * @whatsnew: jumping via touching heroes
         * 
         * @whatsnew: background layers
         * 
         * @whatsnew: jump sounds
         */
        else if (whichLevel == 33) {
            /*
             * // set up a standard side scroller with tilt: Level.configure(3 *
             * 460, 320, 0, 10); Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press the hero to\nmake it jump", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make a hero Hero h = Hero.makeAsCircle(20, 20, 30, 30,
             * "greenball.png"); h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * // this says that touching makes the hero jump
             * h.setTouchToJump(); // this is the force of a jump. remember that
             * up is negative, not positive. h.setJumpImpulses(0, -10); // the
             * sound to play when we jump h.setJumpSound("fwapfwap.ogg");
             * 
             * // set up our background again, but add a few more layers
             * Background.setColor(0, 0, 255); // this layer has a scroll factor
             * of 0... it won't move Background.addLayer("back.png", 0, 0, 0);
             * Background.addLayer("mid.png", -20, 0, 116); // this layer has a
             * scroll factor of -40... it moves fast
             * Background.addLayer("front.png", -40, 0, 0);
             * Background.setScrollFactor(20);
             */
        }

        /**
         * @level: 34
         * 
         * @description: tilt doesn't always work so nicely in side scrollers.
         *               An alternative is for the hero to have a fixed rate of
         *               motion. Another issue was that you had to touch the
         *               hero itself to make it jump. Now, we use an invisible
         *               button so touching any part of the screen makes the
         *               hero jump.
         * 
         * @whatsnew: camera offset on the hero, so that it stays a bit to the
         *            left of the center of the screen
         * 
         * @whatsnew: fixed velocity on the hero
         * 
         * @whatsnew: jump button on the HUD
         */
        else if (whichLevel == 34) {
            /*
             * // set up a side scroller, but don't turn on tilt
             * Level.configure(3 * 460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press anywhere to jump", 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // note: the bounding box does not have friction, and neither
             * does the hero Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png",
             * 1, 0, 0);
             * 
             * // make a hero, but don't let it rotate: Hero h =
             * Hero.makeAsBox(20, 250, 30, 70, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0); // give the hero a
             * fixed velocity h.addVelocity(10, 0); // center the camera a
             * little ahead of the hero, so he is not centered
             * h.setCameraOffset(150, 0); // enable jumping h.setJumpImpulses(0,
             * -10);
             * 
             * // set up the background Background.setColor(0, 0, 255);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20);
             * 
             * // draw a jump button that covers the whole screen
             * Controls.addJumpButton(0, 0, 460, 320, "invis.png");
             * 
             * // if the hero jumps over the destination, we have a problem. To
             * fix it, let's put an // invisible enemy right after the
             * destination, so that if the hero misses the // destination, it
             * hits the enemy and we can start over Enemy.makeAsBox(1300, 0, 5,
             * 320, "invis.png");
             */
        }

        /**
         * @level: 35
         * 
         * @description: the default is that once a hero jumps, it can't jump
         *               again until it touches an obstacle (floor or wall).
         *               Here, we enable multiple jumps. Coupled with a small
         *               jump impulse, this makes jumping feel more like
         *               swimming or controlling a helicopter.
         */
        else if (whichLevel == 35) {
            /*
             * // set up a standard side scroller without tilt Level.configure(3
             * * 460, 320, 0, 10);
             * PopUpScene.showTextTimed("Multi-jump is enabled", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 0);
             * Hero h = Hero.makeAsBox(20, 250, 30, 70, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0); // the hero now has
             * multijump, with small jumps: h.setMultiJumpOn();
             * h.setCameraOffset(150, 0); h.setJumpImpulses(0, -2);
             * h.addVelocity(5, 0);
             * 
             * // this is all the same as before, to include the invisible enemy
             * Background.setColor(0, 0, 255); Background.addLayer("mid.png",
             * -20, 0, 116); Background.setScrollFactor(20);
             * Controls.addJumpButton(0, 0, 460, 320, "invis.png");
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Enemy.makeAsBox(1300, 0, 5, 320, "invis.png");
             * Level.setVictoryDestination(1);
             */
        }

        /**
         * @level: 36
         * 
         * @description: This level shows that we can make a hero move based on
         *               how we touch the screen
         * @whatsnew: controls on the screen for moving the hero
         * 
         * @whatsnew: the hero can face backwards when it moves backwards
         */
        else if (whichLevel == 36) {
            /*
             * // set up a basic side scroller without tilt Level.configure(3 *
             * 460, 320, 0, 0);
             * PopUpScene.showTextTimed("Press screen borders\nto move the hero"
             * , 1); Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0,
             * 1); Hero h = Hero.makeAsCircle(20, 290, 30, 30, "stars.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setCanFaceBackwards(); // this lets the hero flip its image
             * when it moves backwards Destination.makeAsCircle(1200, 310, 10,
             * 10, "mustardball.png"); Level.setVictoryDestination(1);
             * Background.setColor(0, 0, 255); Background.addLayer("mid.png",
             * -20, 0, 116); Background.setScrollFactor(20);
             * 
             * // let's draw an enemy, just in case anyone wants to try to go to
             * the top left corner Enemy e = Enemy.makeAsCircle(30, 30, 30, 30,
             * "redball.png"); e.setPhysics(1, 1, 1);
             * 
             * // draw some buttons for moving the hero
             * Controls.addLeftButton(0, 50, 50, 220, "invis.png", 5);
             * Controls.addRightButton(410, 50, 50, 220, "invis.png", 5);
             * Controls.addUpButton(50, 0, 360, 50, "invis.png", 5);
             * Controls.addDownButton(50, 270, 360, 50, "invis.png", 5);
             */
        }

        /**
         * @level: 37
         * 
         * @description: In the last level, we had complete control of the
         *               hero's movement. Here, we give the hero a fixed
         *               velocity, and only control its up/down movement.
         */
        else if (whichLevel == 37) {
            /*
             * // set up a basic side-scroller Level.configure(3 * 460, 320, 0,
             * 0);
             * PopUpScene.showTextTimed("Press screen borders\nto move up and down"
             * , 1); Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0,
             * 0); // be careful about // friction!
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1); Background.setColor(0, 0, 255);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20);
             * 
             * // as with the bounding box, be careful about friction on the
             * hero! Hero h = Hero.makeAsCircle(20, 290, 30, 30,
             * "greenball.png"); h.disableRotation(); h.setPhysics(1, 0, 0);
             * h.addVelocity(10, 0);
             * 
             * // draw an enemy to avoid, and one at the end Enemy e1 =
             * Enemy.makeAsCircle(530, 280, 30, 30, "redball.png");
             * e1.setPhysics(1, 1, 1); Enemy e2 = Enemy.makeAsBox(1300, 0, 50,
             * 320, "invis.png"); e2.setPhysics(1, 0, 0);
             * 
             * // draw the up/down controls Controls.addUpButton(50, 0, 360, 50,
             * "invis.png", 5); Controls.addDownButton(50, 270, 360, 50,
             * "invis.png", 5);
             */
        }

        /**
         * @level: 38
         * 
         * @description: this level demonstrates crawling heroes. We can use
         *               this to simulate crawling, ducking, rolling, spinning,
         *               etc. Note, too, that we can use it to make the hero
         *               defeat certain enemies via crawl.
         * 
         * @whatsnew: buttons for making heroes crawl
         * 
         * @whatsnew:
         */
        else if (whichLevel == 38) {
            /*
             * // basic configuration: Level.configure(3 * 460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press the screen\nto crawl", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f, 0);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1); Hero h = Hero.makeAsBox(20, 250,
             * 30, 70, "greenball.png"); h.setPhysics(1, 0, 0); h.addVelocity(5,
             * 0);
             * 
             * // to enable crawling, we just draw a crawl button on the screen
             * Controls.addCrawlButton(0, 0, 460, 320, "invis.png");
             * 
             * // make an enemy who we can defeat by colliding with it while
             * crawling Enemy e = Enemy.makeAsCircle(1100, 270, 50, 50,
             * "redball.png"); e.setPhysics(1.0f, 0.3f, 0.6f);
             * e.setDefeatByCrawl();
             */
        }

        /**
         * @level: 39
         * 
         * @description: we can make a hero start moving only when it is pressed
         * 
         * @whatsnew: touch and go
         */
        else if (whichLevel == 39) {
            /*
             * // set up a basic side scroller Level.configure(3 * 460, 320, 0,
             * 10);
             * PopUpScene.showTextTimed("Press the hero\nto start moving\n", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 0);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20); Destination.makeAsCircle(1200,
             * 310, 10, 10, "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // make a hero who doesn't start moving until it is touched // //
             * note that this hero is not a perfect circle, and the hero is
             * "norotate". You will // probably get strange behaviors if you
             * choose any other options Hero h = Hero.makeAsBox(20, 250, 30, 70,
             * "greenball.png"); h.disableRotation(); h.setPhysics(1, 0, 0);
             * h.setTouchAndGo(10, 0);
             */
        }

        /**
         * @level: 40
         * 
         * @description: ALE has limited support for SVG. If you draw a picture
         *               in Inkscape or another SVG tool, and it only consists
         *               of lines, then you can import it into your game as an
         *               obstacle. Drawing a picture on top of the obstacle is
         *               probably a good idea, though we don't bother in this
         *               level
         * 
         * @whatsnew: import an svg as an obstacle
         * 
         * @whatsnew: side scroller with velocity tilt override
         */
        else if (whichLevel == 40) {
            /*
             * // set up a tilt-based side scroller Level.configure(3 * 460,
             * 320, 0, 10); Tilt.enable(10, 0); Tilt.setAsVelocity(true);
             * PopUpScene
             * .showTextTimed("Obstacles can\nbe drawn from SVG\nfiles", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f, 1);
             * 
             * // make a hero who can jump Hero h = Hero.makeAsCircle(20, 20,
             * 30, 30, "greenball.png"); h.setPhysics(1, 0, 0.6f);
             * h.setJumpImpulses(0, -10); h.setTouchToJump();
             * h.setMoveByTilting();
             * 
             * // draw an obstacle from SVG
             * Util.importSVGLineDrawing("shape.svg", 255, 0, 0, 1, 0, 0, 2f,
             * .5f, 250f, 150f);
             * 
             * // notice that we can only get to the destination by jumping from
             * *on top of* the // obstacle Destination.makeAsCircle(1200, 10,
             * 10, 10, "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // zoom buttons so that we can see the whole obstacle
             * Controls.addZoomInButton(0, 0, 20, 20, "red.png", 4);
             * Controls.addZoomOutButton(440, 0, 20, 20, "red.png", 0.25f);
             */
        }

        /**
         * @level: 41
         * 
         * @description: this is a side-scroller with speed boosters for
         *               changing the hero's velocity
         * 
         * @whatsnew: speed booster obstacles
         */
        else if (whichLevel == 41) {
            /*
             * // set up a basic level with a fixed velocity hero and a
             * destination Level.configure(10 * 460, 320, 0, 0);
             * PopUpScene.showTextTimed("Speed boosters and reducers", 1);
             * Util.drawBoundingBox(0, 0, 10 * 460, 320, "invis.png", 1, 0, 1);
             * Hero h = Hero.makeAsCircle(20, 290, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * Destination.makeAsCircle(4500, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1); h.addVelocity(10, 0);
             * Background.setColor(0, 0, 255); Background.addLayer("mid.png",
             * -20, 0, 116); Background.setScrollFactor(20);
             * 
             * // place a speed-up obstacle that lasts for 1 second Obstacle o1
             * = Obstacle.makeAsCircle(400, 300, 40, 40, "purpleball.png");
             * o1.setSpeedBoost(10, 0, 1);
             * 
             * // place a slow-down obstacle that lasts for 3 seconds Obstacle
             * o2 = Obstacle.makeAsCircle(1200, 300, 40, 60, "purpleball.png");
             * o2.setSpeedBoost(-5, 0, 3);
             * 
             * // place a permanent +3 speedup obstacle Obstacle o3 =
             * Obstacle.makeAsCircle(2400, 300, 40, 40, "purpleball.png");
             * o3.setSpeedBoost(3, 0, -1);
             */
        }

        /**
         * @level: 42
         * 
         * @description: this is a very gross level, which exists just to show
         *               that backgrounds can scroll vertically.
         * 
         * @whatsnew: vertical background colors and images
         */
        else if (whichLevel == 42) {
            /*
             * // set up a level where tilt only makes the hero move up and down
             * Level.configure(460, 4 * 320, 0, 0); Tilt.enable(0, 10);
             * PopUpScene.showTextTimed("Vertical scroller demo", 1);
             * Util.drawBoundingBox(0, 0, 460, 4 * 320, "red.png", 1, 0, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(20, 1200, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // set up a vertical scrolling background
             * Background.setColorVertical(255, 0, 255);
             * Background.addLayerVertical("back.png", 0, 0, 0);
             * Background.addLayerVertical("mid.png", -20, 0, 116);
             * Background.addLayerVertical("front.png", -40, 0, 0);
             * Background.setScrollFactor(-20);
             */
        }

        /**
         * @level: 43
         * 
         * @description: the next few levels demonstrate support for throwing
         *               projectiles. In this level, we throw projectiles by
         *               touching the hero
         * 
         * @whatsnew: throw a projectile by touching the hero
         */
        else if (whichLevel == 43) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "Press the hero\nto make it throw\nprojectiles", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // create a hero, and indicate that touching it makes it throw
             * projectiles Hero h = Hero.makeAsCircle(40, 70, 30, 30,
             * "greenball.png"); h.setPhysics(1, 0, 0.6f); h.setTouchToThrow();
             * h.setMoveByTilting();
             * 
             * // configure a pool of projectiles. be sure to hover the mouse
             * over 'configure' to see what all // the parameters do. In
             * particular, note how the projectiles fly out of the hero, and //
             * how many can be on screen at any time Projectile.configure(3, 10,
             * 10, "greyball.png", 0, -10, 20, -5, 1);
             */
        }

        /**
         * @level: 44
         * 
         * @description: this is another demo of how throwing projectiles works.
         *               Like the previous demo, it doesn't actually use
         *               projectiles for anything, it is just to show how to get
         *               some different behaviors in terms of how the
         *               projectiles move.
         * 
         * @whatsnew: limiting the range of projectiles
         * 
         * @whatsnew: throw projectiles by touching the screen
         */
        else if (whichLevel == 44) {
            /*
             * // set up a basic tilt level Level.configure(3 * 460, 320, 0,
             * 10); Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a gray\nball",
             * 1); Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f,
             * 1); Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); Destination.makeAsCircle(1200, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // set up a pool of projectiles, but now once the projectiles
             * move more than 250x20 away // from the hero, they disappear
             * Projectile.configure(100, 10, 10, "greyball.png", 30, 0, 40, 0,
             * 1); Projectile.setRange(250, 20);
             * 
             * // add a button for throwing projectiles
             * Controls.addThrowButton(0, 0, 460, 320, "invis.png");
             */
        }

        /**
         * @level 45
         * 
         * @description: this level demonstrates that we can defeat enemies by
         *               throwing projectiles at them
         * 
         * @whatsnew: show that we can defeat enemies by throwing projectiles at
         *            them
         * 
         * @whatsnew: holding the button doesn't make multiple shots fire
         */
        else if (whichLevel == 45) {
            /*
             * // set up a simple, small level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); Util.drawBoundingBox(0, 0, 460, 320,
             * "red.png", 1, .3f, 1);
             * PopUpScene.showTextTimed("Defeat all enemies\nto win", 1); Hero h
             * = Hero.makeAsCircle(40, 270, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // set up our projectiles... note that the last parameter means
             * that projectiles each do // 2 units of damage
             * Projectile.configure(3, 4, 10, "greyball.png", 0, -10, 20, -5,
             * 2);
             * 
             * // draw a few enemies... note that they have different amounts of
             * damage Enemy e = Enemy.makeAsCircle(250, 250, 20, 20,
             * "redball.png"); e.setPhysics(1.0f, 0.3f, 0.6f);
             * e.setRotationSpeed(1); for (int i = 10; i < 200; i += 50) { Enemy
             * ee = Enemy.makeAsCircle(i, i, 20, 20, "redball.png");
             * ee.setPhysics(1.0f, 0.3f, 0.6f); ee.setDamage(i / 10); }
             * Level.setVictoryEnemyCount();
             * 
             * // this button only throws one projectile per press...
             * Controls.addSingleThrowButton(0, 0, 460, 320, "invis.png");
             */
        }

        /**
         * @level: 46
         * 
         * @description: this level shows how to throw projectiles in a variety
         *               of directions
         * 
         * @whatsnew: the "vector throw" mechanism
         */
        else if (whichLevel == 46) {
            /*
             * // set up a simple level Level.configure(3 * 460, 320, 0, 10);
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a ball", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); Destination.makeAsCircle(1200, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // draw a button for throwing projectiles in many directions
             * Controls.addVectorThrowButton(0, 0, 460, 320, "invis.png");
             * 
             * // set up our pool of projectiles. The main challenge here is
             * that the farther from the // hero we press, the faster the
             * projectile goes, so we multiply the velocity by .3 to // slow it
             * down a bit Projectile.configure(100, 10, 10, "greyball.png", 30,
             * 0, 40, 0, 1); Projectile.setProjectileVectorDampeningFactor(.3f);
             * Projectile.setRange(460, 320);
             */
        }

        /**
         * @level: 47
         * 
         * @description: this level shows that with the "vector" projectiles, we
         *               can still have gravity affect the projectiles. This is
         *               very good for basketball-style games.
         * 
         * @whatsnew: holding the screen no longer throws multiple projectiles
         *            in that direction
         * 
         * @whatsnew: projectiles can be prevented from disappearign when they
         *            collide with certain types of obstacles
         */
        else if (whichLevel == 47) {
            /*
             * // set up a basic level Level.configure(3 * 460, 320, 0, 10);
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a ball", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); Destination.makeAsCircle(1200, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // the only differences here are that we turn gravity back on for
             * the projectiles, and // we use a "single throw" button
             * Controls.addVectorSingleThrowButton(0, 0, 460, 320, "invis.png");
             * Projectile.configure(100, 10, 10, "greyball.png", 30, 0, 40, 0,
             * 1); Projectile.setProjectileVectorDampeningFactor(.1f);
             * Projectile.setRange(460, 320);
             * Projectile.setProjectileGravityOn(); Obstacle o =
             * Obstacle.makeAsBox(100, 10, 20, 20, "red.png"); // This is a
             * rather complex trick: we can specify that when a projectile
             * collides with things, it is allowed // to bounce off of them, and
             * that when it collides with /this/ obstacle, we want custom code
             * to run. The // custom code appears below, as
             * onProjectileCollideTrigger, and when it is called, our id (the
             * first "0") // will be passed along to the code, along with the
             * specific obstacle and projectile that were involved in // the
             * collision. The default is that when a projectile hits one of
             * these special obstacles, it doesn't // automatically disappear,
             * because in that code you could say "p.remove(true)" if that was
             * the behavior you // wanted. So, by default, it doesn't disappear,
             * and instead we get the projectile bouncing off of the //
             * obstacle, but not off of anything else.
             * Projectile.enableCollisionsForProjectiles();
             * o.setProjectileCollisionTrigger(0, 0);
             */
        }

        /**
         * @level: 48
         * 
         * @description: this level shows how enemies can reproduce. This can
         *               simulate cancer cells, or fire on a building. We do
         *               this by using a timer connected to an enemy. Whenever
         *               the timer goes off, we will have access to the enemy so
         *               that we can copy it.
         * 
         * @whatsnew: enemy timer triggers
         * 
         * @whatsnew: sound when the projectile is thrown
         */
        else if (whichLevel == 48) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 0); PopUpScene.showTextTimed(
             * "Throw balls at \nthe enemies before\nthey reproduce", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 290, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setTouchToThrow();
             * h.setMoveByTilting();
             * 
             * // configure a pool of projectiles Projectile.configure(100, 10,
             * 10, "greyball.png", 0, -10, 20, -5, 1);
             * Projectile.setThrowSound("fwapfwap.ogg");
             * Projectile.setProjectileDisappearSound("slowdown.ogg");
             * 
             * // draw an enemy that reproduces. be sure to read the parameters,
             * as this will produce // two children every two seconds, until two
             * reproductions occur... the children // reproduce too, but they
             * don't start at 2... Enemy e = Enemy.makeAsCircle(230, 20, 10, 10,
             * "redball.png"); e.setDisappearSound("lowpitch.ogg");
             * Level.setEnemyTimerTrigger(2, 2, e);
             * Level.setVictoryEnemyCount(); Controls.addDefeatedCount(0,
             * "Enemies Defeated", 20, 20);
             */
        }

        /**
         * @level: 49
         * 
         * @description: this level shows what happens when enemies reproduce
         *               when they are moveable
         */
        else if (whichLevel == 49) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("These enemies are\nreally tricky", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 290, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 290, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make our initial enemy Enemy e = Enemy.makeAsCircle(230, 20,
             * 10, 10, "redball.png"); e.setPhysics(1.0f, 0.3f, 0.6f);
             * e.setMoveByTilting(); // warning: "6" is going to lead to 127
             * enemies eventually... Level.setEnemyTimerTrigger(6, 2, e);
             */
        }

        /**
         * @level: 50
         * 
         * @description: this level shows simple animation. Every entity can
         *               have a default animation.
         * 
         * @whatsnew: the hero has an animation in this level, which makes it
         *            look like a star with streamers underneath it
         */
        else if (whichLevel == 50) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed("Make a wish!", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // this hero will be animated: Hero h = Hero.makeAsCircle(40, 70,
             * 30, 30, "stars.png"); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); // this says that we scroll through the 0,
             * 1, 2, and 3 cells of the image, and we show // each for 200
             * milliseconds int cells[] = { 0, 1, 2, 3 }; long plan[] = { 200,
             * 200, 200, 200 }; h.setDefaultAnimation(cells, plan);
             */
        }

        /**
         * @level: 51
         * 
         * @description: this level introduces jumping animations and
         *               disappearance animations
         * 
         * @whatsnew: jump animation
         * 
         * @whatsnew: disappearance animation
         */
        else if (whichLevel == 51) {
            /*
             * // make a tilt sidescroller Level.configure(3 * 460, 320, 0, 10);
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press the hero to\nmake it jump", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Background.setColor(0, 0, 255); Background.addLayer("mid.png",
             * -20, 0, 116); Background.setScrollFactor(20);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make a hero, and give it two animations: one for when it is in
             * the air, and another // for the rest of the time. Note that both
             * sets of images must be cells from the same // .png file Hero h =
             * Hero.makeAsCircle(20, 20, 30, 30, "stars.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setJumpImpulses(0, -10); h.setTouchToJump();
             * h.setMoveByTilting();
             * 
             * int cells[] = { 0, 1, 2, 3 }; long plan[] = { 200, 200, 200, 200
             * }; h.setDefaultAnimation(cells, plan); int jcells[] = { 4, 5, 6,
             * 7 }; long jplan[] = { 200, 200, 200, 200 };
             * h.setJumpAnimation(jcells, jplan);
             * 
             * // create a goodie that has a disappearance animation. Note that
             * this can be a totally // different image than its regular
             * appearance... that's OK, because once we touch the // entity, it
             * disappears and the animation just plays once in the background.
             * Note, too, // that the final cell is blank, so that we don't
             * leave a residue on the screen. Goodie g =
             * Goodie.makeAsCircle(150, 230, 50, 50, "stars.png"); int
             * gonecells[] = { 2, 1, 0, 3 }; g.setDisappearAnimation(gonecells,
             * plan, "starburst.png", 10, 0, 50, 50);
             */
        }

        /**
         * @level: 52
         * 
         * @description: this level shows that projectiles can be animated
         * 
         * @whatsnew: projectile animations
         * 
         * @whatsnew: animations when the hero throws a projectile
         */
        else if (whichLevel == 52) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Press the hero\nto make it\nthrow a ball"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // set up our hero Hero h = Hero.makeAsCircle(40, 70, 30, 30,
             * "colorstar.png"); h.setPhysics(1, 0, 0.6f); h.setTouchToThrow();
             * h.setMoveByTilting();
             * 
             * // set up an animation when the hero throws: int tCells[] = { 3,
             * 4 }; long tDurations[] = { 100, 500 };
             * h.setThrowAnimation(tCells, tDurations);
             * 
             * // make a projectile pool and give an animation pattern for the
             * projectiles Projectile.configure(100, 10, 10, "flystar.png", 0,
             * -10, 20, -5, 1); int pCells[] = { 0, 1 }; long pDurations[] = {
             * 100, 100 }; Projectile.setAnimation(pCells, pDurations);
             */
        }

        /**
         * @level: 53
         * 
         * @description: this level explores invincibility animation. While
         *               we're at it, we make some enemies that aren't affected
         *               by invincibility, and some that can even damage the
         *               hero while they are invincible.
         * 
         * @whatsnew: invincibility animation
         * 
         * @whatsnew: enemies that resist invincibility
         * 
         * @whatsnew: enemies that do damage even when the hero is invincible
         * 
         * @whatsnew: display a picture when the level is won
         */
        else if (whichLevel == 53) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); PopUpScene.showTextTimed(
             * "The blue ball will\nmake you invincible\nfor 15 seconds", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Destination.makeAsCircle(290, 10, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make an animated hero, and also give it an invincibility
             * animation Hero h = Hero.makeAsCircle(20, 20, 30, 30,
             * "colorstar.png"); h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * int[] cells = { 0, 1, 2, 3 }; long[] durations = { 300, 300, 300,
             * 300 }; h.setDefaultAnimation(cells, durations); int[] icells = {
             * 4, 5, 6, 7 }; long[] idurations = { 100, 100, 100, 100 };
             * h.setInvincibleAnimation(icells, idurations);
             * 
             * // make some enemies for (int i = 0; i < 5; ++i) { Enemy e =
             * Enemy.makeAsCircle(50 * i + 10, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRotationSpeed(1);
             * e.setDamage(4); e.setDisappearSound("hipitch.ogg");
             * 
             * // The first enemy we create will harm the hero even if the hero
             * is invincible if (i == 0) e.setImmuneToInvincibility(); // the
             * second enemy will not be harmed by invincibility, but won't harm
             * an // invincible hero if (i == 1) e.setResistInvincibility(); }
             * // neat trick: this enemy does zero damage, but slows the hero
             * down. Enemy e = Enemy.makeAsCircle(300, 200, 20, 20,
             * "redball.png"); e.setPhysics(10, 0.3f, 0.6f);
             * e.setMoveByTilting(); e.setDamage(0);
             * 
             * // add a goodie that makes the hero invincible Goodie g =
             * Goodie.makeAsCircle(300, 300, 10, 10, "blueball.png");
             * g.setInvincibilityDuration(15); g.setRoute(new Route(3).to(300,
             * 300).to(100, 100).to(300, 300), 5, true);
             * g.setRotationSpeed(0.25f); Controls.addGoodieCount(0, "Goodies",
             * 220, 280);
             * 
             * // draw a picture when the level is won, and don't print text...
             * this particular picture // isn't very useful
             * Level.setBackgroundWinImage("splash.png"); Level.setWinText("");
             */
        }

        /**
         * @level: 54
         * 
         * @description: demonstrate crawl animation, and also show that on
         *               multitouch phones, we can "crawl" in the air while
         *               jumping.
         * 
         * @whatsnew: crawl animation
         * 
         * @whatsnew: show a picture when the level is lost
         */
        else if (whichLevel == 54) {
            /*
             * // make a simple level: Level.configure(3 * 460, 320, 0, 10);
             * PopUpScene
             * .showTextTimed("Press the left side of\nthe screen to crawl\n" +
             * "or the right side\nto jump.", 1); Util.drawBoundingBox(0, 0, 3 *
             * 460, 320, "red.png", 1, .3f, 0); Destination.makeAsCircle(1200,
             * 310, 10, 10, "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // make a hero with fixed velocity, and give it crawl and jump
             * animations Hero h = Hero.makeAsBox(20, 250, 30, 70, "stars.png");
             * h.setPhysics(1, 0, 0); h.addVelocity(5, 0);
             * 
             * int hcells[] = { 0, 1, 2, 3 }; long hplan[] = { 100, 300, 300,
             * 100 }; h.setCrawlAnimation(hcells, hplan); int jcells[] = { 4, 5,
             * 6, 7 }; long jplan[] = { 200, 200, 200, 200 };
             * h.setJumpAnimation(jcells, jplan);
             * 
             * // enable hero jumping and crawling h.setJumpImpulses(0, -10);
             * Controls.addJumpButton(0, 0, 230, 320, "invis.png");
             * Controls.addCrawlButton(231, 0, 460, 320, "invis.png");
             * 
             * // add an enemy we can defeat via crawling, just for fun. It
             * should be defeated even by // a "jump crawl" Enemy e =
             * Enemy.makeAsCircle(1100, 270, 50, 50, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setDefeatByCrawl();
             * 
             * // include a picture on the "try again" screen
             * Level.setBackgroundLoseImage("splash.png");
             * Level.setLoseText("Oh well...");
             */
        }

        /**
         * @level: 55
         * 
         * @description: This isn't quite the same as animation, but it's nice.
         *               We can indicate that a hero's image changes via goodie
         *               count. This can, for example, allow a hero to change
         *               (e.g., get healthier) by swapping through images as
         *               goodies are collected
         */
        else if (whichLevel == 55) {
            /*
             * // set up a basic level with a bunch of goodies
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); for
             * (int i = 0; i < 8; ++i) Goodie.makeAsCircle(50 + 20 * i, 50 + 20
             * * i, 20, 20, "blueball.png"); Destination d =
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * d.setActivationScore(8); Level.setVictoryDestination(1);
             * 
             * // Note: colorstar.png has 8 cells... Hero h =
             * Hero.makeAsCircle(40, 270, 30, 30, "colorstar.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting(); // set up the
             * animation by matching the cell in the image to a specific goodie
             * count. // That is, when the count is 1, we show picture 2. When
             * the count is 2, we show picture // 1, etc. // // note: no change
             * for goodie count 3, and remember that 0 is the // default picture
             * so it's showing already int counts[] = { 1, 2, 4, 5, 6, 7, 8 };
             * int cells[] = { 2, 1, 3, 4, 5, 6, 7 };
             * h.setAnimateByGoodieCount(counts, cells);
             */
        }

        /**
         * @level: 56
         * 
         * @description: demonstrate that obstacles can defeat enemies, and that
         *               we can use this feature to have obstacles that only
         *               defeat certain "marked" enemies
         * 
         * @whatsnew: gravity multiplier, to make the forces happen more quickly
         * 
         * @whatsnew: use of enemyCollisionTrigger to make some obstacles able
         *            to defeat some enemies, and enable some of these obstacles
         *            to disappear after defeating an enemy
         * 
         * @whatsnew: moveable obstacles
         */
        else if (whichLevel == 56) {
            /*
             * // make a basic level, but increase the speed of gravity updates
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10);
             * Tilt.setGravityMultiplier(3); PopUpScene.showTextTimed(
             * "You can defeat\ntwo enemies with\nthe blue ball", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, 0, 1); Hero h
             * = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // put an enemy defeated count on the screen, in red with a small
             * font Controls.addDefeatedCount(2, "Enemies Defeated", 20, 20,
             * 255, 0, 0, 10);
             * 
             * // make a moveable obstacle that can defeat enemies Obstacle o =
             * Obstacle.makeAsCircle(100, 20, 40, 40, "blueball.png");
             * o.setPhysics(1, 0, 0.6f); o.setMoveByTilting();
             * o.setEnemyCollisionTrigger(0, 0);
             * 
             * // make a small obstacle that can also defeat enemies, but
             * doesn't disappear Obstacle o2 = Obstacle.makeAsCircle(5, 5, 20,
             * 20, "blueball.png"); o2.setPhysics(1, 0, 0.6f);
             * o2.setMoveByTilting(); o2.setEnemyCollisionTrigger(0, 1);
             * 
             * // this enemy has a triggerID of 1... no obstacle will defeat it
             * Enemy e = Enemy.makeAsCircle(400, 20, 40, 40, "redball.png");
             * e.setPhysics(1, 0, 0.6f); e.setMoveByTilting();
             * 
             * // This enemy also has a triggerID of 1... no obstacle will
             * defeat it Enemy e1 = Enemy.makeAsCircle(400, 20, 40, 40,
             * "redball.png"); e1.setPhysics(1, 0, 0.6f);
             * 
             * // these enemies have class 7... our obstacles will defeat them
             * Enemy e2 = Enemy.makeAsCircle(400, 220, 40, 40, "redball.png");
             * e2.setPhysics(1, 0, 0.6f); e2.setMoveByTilting();
             * e2.setInfoText("weak");
             * 
             * Enemy e3 = Enemy.makeAsCircle(400, 120, 40, 40, "redball.png");
             * e3.setPhysics(1, 0, 0.6f); e3.setMoveByTilting();
             * e3.setInfoText("weak");
             * 
             * // win by defeating enemies Level.setVictoryEnemyCount(2);
             */
        }

        /**
         * @level: 57
         * 
         * @description: this level shows an odd way of moving the hero. There's
         *               friction on the floor, so it can only move by tilting
         *               while the hero is in the air
         */
        else if (whichLevel == 57) {
            /*
             * // set up a side scroller level, but give the bounding box some
             * friction Level.configure(3 * 460, 320, 0, 10); Tilt.enable(10,
             * 0); PopUpScene.showTextTimed("Press the hero to\nmake it jump",
             * 1); Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make a box hero with friction... it won't roll on the floor,
             * so it's stuck! Hero h = Hero.makeAsBox(20, 20, 30, 30,
             * "stars.png"); h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setTouchToJump(); h.setMoveByTilting(); h.setJumpImpulses(0,
             * -10);
             * 
             * // draw a background Background.setColor(0, 0, 255);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20);
             */
        }

        /**
         * @level: 58
         * 
         * @description: this level shows that we can put an obstacle on the
         *               screen and use it to make the hero throw projectiles.
         *               It also shows that we can make entities that shrink
         *               over time... growth is possible too, with a negative
         *               value.
         * 
         * @whatsnew: limit the total number of projectiles that can be thrown
         * 
         * @whatsnew: make an entity shrink over time
         * 
         * @whatsnew: make projectiles that have a randomly selected image
         * 
         * @whatsnew: show how many shots are left
         */
        else if (whichLevel == 58) {
            /*
             * // make a simple level with left/right tilt and Y gravity
             * Level.configure(460, 320, 0, 10); Tilt.enable(10, 0);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(40, 270, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // make an obstacle that causes the hero to throw obstacles
             * Obstacle o = Obstacle.makeAsCircle(350, 5, 50, 50,
             * "purpleball.png"); o.setCollisionEffect(false);
             * o.setTouchToThrow();
             * 
             * // set up our projectiles Projectile.configure(3, 4, 10,
             * "colorstar.png", 0, -10, 20, -5, 2);
             * Projectile.setNumberOfProjectiles(20); // there are only 20...
             * throw them carefully Projectile.setImageRange(8); // pick a
             * random picture between cell 0 and cell 7 of // colorstar.png //
             * show how many shots are left
             * Controls.addProjectileCount("projectiles left", 5, 5, 255, 255,
             * 255, 12);
             * 
             * // draw a bunch of enemies to defeat Enemy e =
             * Enemy.makeAsCircle(250, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setRotationSpeed(1); for (int i
             * = 10; i < 200; i += 50) { Enemy e1 = Enemy.makeAsCircle(i, i, 20,
             * 20, "redball.png"); e1.setPhysics(1.0f, 0.3f, 0.6f); }
             * 
             * // draw an obstacle that shrinks over time Obstacle o2 =
             * Obstacle.makeAsBox(20, 300, 420, 18, "red.png");
             * o2.setShrinkOverTime(50, 1); Level.setVictoryEnemyCount(5);
             */
        }

        /**
         * @level: 59
         * 
         * @description: this level shows that we can make a hero in the air
         *               rotate. Rotation doesn't do anything, but it looks
         *               nice...
         * 
         * @whatsnew: rotation buttons
         * 
         * @whatsnew: this level relies on being able to jump after touching a
         *            side wall
         */
        else if (whichLevel == 59) {
            /*
             * // make a simple level Level.configure(460, 320, 0, 10);
             * Tilt.enable(10, 0); Util.drawBoundingBox(0, 0, 460, 320,
             * "red.png", 1, .3f, 1); Destination.makeAsCircle(300, 100, 25, 25,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // make the hero jumpable, so that we can see it spin in the air
             * Hero h = Hero.makeAsCircle(40, 270, 30, 30, "stars.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * h.setJumpImpulses(0, -10); h.setTouchToJump();
             * 
             * // add rotation buttons Controls.addRotateButton(0, 0, 80, 80,
             * "invis.png", -.5f); Controls.addRotateButton(380, 0, 80, 80,
             * "invis.png", .5f);
             */
        }

        /**
         * @level 60
         * 
         * @description: we can attach movement buttons to any moveable entity,
         *               so in this case, we attach it to an obstacle to get an
         *               arkanoid-like effect.
         * 
         * @whatsnew: attaching left/right buttons to an obstacle instead of
         *            controlling the last hero created
         */
        else if (whichLevel == 60) {
            /*
             * // make a simple level Level.configure(460, 320, 0, 0);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 0, 0, 0);
             * Destination.makeAsCircle(300, 100, 25, 25, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make a hero who is always moving... note there is no friction,
             * anywhere, and the hero // is elastic... it won't ever stop...
             * Hero h = Hero.makeAsCircle(40, 40, 30, 30, "greenball.png");
             * h.setPhysics(0, 1, 0); h.addVelocity(0, 10);
             * 
             * // make an obstacle and then connect it to some controls Obstacle
             * o = Obstacle.makeAsBox(20, 309, 40, 10, "red.png");
             * Controls.addLeftButton(0, 0, 230, 320, "invis.png", 5, o);
             * Controls.addRightButton(230, 0, 230, 320, "invis.png", 5, o);
             */
        }

        /**
         * @level: 61
         * 
         * @description: this level demonstrates that things can appear and
         *               disappear after timers expire
         * 
         * @whatsnew: use of disappearDelay and appearanceDelay
         */
        else if (whichLevel == 61) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Things will appear \nand disappear...",
             * 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // create an enemy that will quietly disappear after 2 seconds
             * Enemy e1 = Enemy.makeAsCircle(250, 250, 20, 20, "redball.png");
             * e1.setPhysics(1.0f, 0.3f, 0.6f); e1.setRotationSpeed(1);
             * e1.setDisappearDelay(2, true);
             * 
             * // create an enemy that will appear after 3 seconds Enemy e2 =
             * Enemy.makeAsCircle(350, 250, 20, 20, "redball.png");
             * e2.setPhysics(1.0f, 0.3f, 0.6f); e2.setRoute(new Route(3).to(350,
             * 250).to(150, 250).to(350, 250), 3, true); e2.setAppearDelay(3);
             */
        }

        /**
         * @level: 62
         * 
         * @description: this level demonstrates the use of timer triggers. We
         *               can use timers to make more of the level appear over
         *               time. In this case, we'll chain the timer triggers
         *               together, so that we can get more and more things to
         *               develop. Be sure to look at the onTimeTrigger code to
         *               see how the rest of this level works.
         * 
         * @whatsnew: destinations and goodies with fixed velocities
         * 
         * @whatsnew: enemy who disappears when it is touched
         * 
         * @whatsnew: enemy who can be dragged around
         * 
         * @whatsnew: timer triggers
         */
        else if (whichLevel == 62) {
            /*
             * // create a level that has a hero, but nothing else
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * PopUpScene.showTextAndWait("There's nothing to\ndo... yet");
             * 
             * // note: there's no destination yet, but we still say it's how to
             * win... we'll get a // destination in this level after a few
             * timers run... Level.setVictoryDestination(1);
             * 
             * // now set a timer trigger. after three seconds, the
             * onTimerTrigger() method will run, // with level=62 and id=0
             * Level.setTimerTrigger(0, 3);
             */
        }

        /**
         * @level: 63
         * 
         * @description: this level shows triggers that run on a collision. In
         *               this case, it lets us draw out the next part of the
         *               level later, instead of drawing the whole thing right
         *               now. In a real level, we'd draw a few screens at a
         *               time, and not put the trigger obstacle at the end of a
         *               screen, so that we'd never see the drawing of stuff
         *               taking place, but for this demo, that's actually a nice
         *               effect. Be sure to look at onCollideTrigger for more
         *               details.
         * 
         * @whatsnew: obstacles that are collision triggers
         * 
         * @whatsnew: obstacles with collision sounds
         * 
         * @whatsnew: collision triggers that depend on collecting enough
         *            goodies before they work
         */
        else if (whichLevel == 63) {
            /*
             * // make a tilt level with just a hero Level.configure(3 * 460,
             * 320, 0, 0); Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Keep going right!", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(20, 290, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * Controls.addGoodieCount(0, "Goodies", 220, 280);
             * Level.setVictoryDestination(1);
             * 
             * // this obstacle is a collision trigger... when the hero hits it,
             * the next part of the // level appears. Note, too, that it
             * disappears when the hero hits it, so we can play a // sound if we
             * want... Obstacle o = Obstacle.makeAsBox(300, 0, 10, 320,
             * "purpleball.png"); o.setPhysics(1, 0, 1);
             * o.setHeroCollisionTrigger(0, 0);
             * o.setDisappearSound("hipitch.ogg");
             */
        }

        /**
         * @level: 64
         * 
         * @description: this level demonstrates triggers that happen when we
         *               touch an obstacle. Be sure to look at the
         *               onTouchTrigger() method for more details
         * 
         * @whatsnew: touchtrigger obstacles
         */
        else if (whichLevel == 64) {
            /*
             * // set up a basic level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Activate and then \ntouch the obstacle"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * 
             * // make a destination... notice that it needs a lot more goodies
             * than are on the // screen... Destination d =
             * Destination.makeAsCircle(290, 10, 10, 10, "mustardball.png");
             * d.setActivationScore(3); Level.setVictoryDestination(1);
             * 
             * // draw an obstacle, make it a touch trigger, and then draw the
             * goodie we need to get in // order to activate the obstacle
             * Obstacle o = Obstacle.makeAsCircle(100, 5, 30, 30,
             * "purpleball.png"); o.setPhysics(1, 0, 1); o.setTouchTrigger(1,
             * 39); // I picked '39' arbitrarily...
             * o.setDisappearSound("hipitch.ogg"); Goodie g =
             * Goodie.makeAsCircle(0, 300, 10, 10, "blueball.png");
             * g.setDisappearSound("lowpitch.ogg");
             */
        }

        /**
         * @level: 65
         * 
         * @description: this level shows how to use enemy defeat triggers.
         *               There are four ways to defeat an enemy, so we enable
         *               all mechanisms in this level, to see if they all work
         *               to cause enemy triggers to run the onEnemyTrigger code.
         *               Another important point here is that the IDs don't need
         *               to be unique for *any* triggers. We can use the same ID
         *               every time...
         * 
         * @whatsnew: use enemy defeat triggers
         * 
         * @whatsnew: the trigger code uses random number generation to place a
         *            reward goodie whenever an enemy is defeated
         */
        else if (whichLevel == 65) {
            /*
             * // draw a simple level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10); Util.drawBoundingBox(0, 0, 460, 320,
             * "red.png", 0, 0, 0);
             * 
             * // give the hero strength, so that we can use him to defeat an
             * enemy // as a test of enemy triggers Hero h =
             * Hero.makeAsCircle(120, 120, 40, 40, "greenball.png");
             * h.setStrength(3); h.setMoveByTilting();
             * 
             * // enable throwing projectiles, so that we can test enemy
             * triggers // again h.setTouchToThrow(); Projectile.configure(100,
             * 10, 10, "greyball.png", 30, 0, 40, 0, 1);
             * 
             * // add an obstacle that has a collision trigger Obstacle o =
             * Obstacle.makeAsCircle(300, 100, 50, 50, "blueball.png");
             * o.setPhysics(1000, 0, 0); o.setCanDrag(true);
             * o.setEnemyCollisionTrigger(0, 0);
             * 
             * // now draw our enemies... we need enough to be able to test that
             * // all five defeat mechanisms work. Enemy e1 =
             * Enemy.makeAsCircle(50, 50, 10, 10, "redball.png");
             * e1.setDefeatTrigger(0);
             * 
             * Enemy e2 = Enemy.makeAsCircle(5, 5, 20, 20, "redball.png");
             * e2.setDefeatTrigger(0); e2.setInfoText("weak");
             * 
             * Enemy e3 = Enemy.makeAsCircle(400, 30, 10, 10, "redball.png");
             * e3.setDefeatTrigger(0);
             * 
             * Enemy e4 = Enemy.makeAsCircle(250, 250, 10, 10, "redball.png");
             * e4.setDefeatTrigger(0); e4.setDisappearOnTouch();
             * 
             * Enemy e5 = Enemy.makeAsCircle(250, 290, 10, 10, "redball.png");
             * e5.setDefeatTrigger(0);
             * 
             * // a goodie, so we can do defeat by invincibility Goodie g1 =
             * Goodie.makeAsCircle(200, 290, 10, 10, "purpleball.png");
             * g1.setInvincibilityDuration(15);
             * 
             * // win by defeating enemies Level.setVictoryEnemyCount();
             */
        }

        /**
         * @level: 66
         * 
         * @description: This level shows how we can use live-edit mode to allow
         *               the programmer to adjust entities' configurations
         *               without having to re-build the program.
         * 
         *               Note: this is only useful during development, as a
         *               time-saving technique. Be sure to turn off live edit in
         *               a level once you're sure the initial positions and
         *               sizes are satisfactory.
         * 
         * @whatsnew: live edit mode
         */
        else if (whichLevel == 66) {
            /*
             * // configure a basic level like level 7, but with the hero in
             * liveedit mode: Level.configure(460, 320, 0, 0); Tilt.enable(10,
             * 10); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setMoveByTilting(); h.setLiveEdit();
             * 
             * // make the destination liveedit Destination d =
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * d.setLiveEdit(); Level.setVictoryDestination(1);
             * 
             * // draw an enemy who moves, and who is liveedit Enemy e =
             * Enemy.makeAsCircle(250, 250, 20, 20, "redball.png");
             * e.setPhysics(1.0f, 0.3f, 0.6f); e.setMoveByTilting();
             * e.setLiveEdit();
             * 
             * // add a goodie and an obstacle Obstacle o =
             * Obstacle.makeAsBox(90, 90, 80, 10, "purpleball.png");
             * o.setLiveEdit(); Goodie g = Goodie.makeAsCircle(300, 200, 5, 5,
             * "blueball.png"); g.setLiveEdit();
             * 
             * // display a message that stays until it is pressed
             * PopUpScene.showTextAndWait
             * ("Press entities to change\ntheir configuration");
             */
        }

        /**
         * @level: 67
         * 
         * @description: This level shows how to use countdown timers to win a
         *               level, tests some color features, and introduces a
         *               vector throw mechanism with fixed velocity
         */
        else if (whichLevel == 67) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a ball", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero.makeAsBox(1, 300, 450, 18, "greenball.png");
             * Controls.addWinCountdown(25, 5, 5, 192, 192, 192, 16);
             * Level.setVictoryDestination(1);
             * 
             * // draw a button for throwing projectiles in many directions
             * Controls.addVectorThrowButton(0, 0, 460, 320, "invis.png");
             * 
             * // set up our pool of projectiles. The main challenge here is
             * that the farther from the // hero we press, the faster the
             * projectile goes, so we multiply the velocity by .3 to // slow it
             * down a bit Projectile.configure(100, 10, 10, "greyball.png", 30,
             * 0, 229, -1, 1); Projectile.setRange(460, 320);
             * Projectile.setFixedVectorThrowVelocity(5);
             */
        }

        /**
         * @level: 68
         * 
         * @description Test hovering heroes that stop hovering after a press
         * 
         * @whatsnew: setCanFall to allow an entity to be subject to gravity
         *            without having a pre-set motion
         */
        else if (whichLevel == 68) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a ball", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsBox(215, 235, 30, 30, "greenball.png"); // note
             * that if we did hover, then flickable, this would not work
             * correctly, because setFlickable will // re-enable gravity on the
             * hero after setHover temporarily disables it.
             * h.setFlickable(0.7f); h.setHover(215, 250); // place an enemy,
             * let it fall Enemy e = Enemy.makeAsCircle(215, 5, 30, 30,
             * "redball.png"); e.setCanFall();
             * 
             * Destination.makeAsCircle(50, 50, 50, 50, "mustardball.png");
             * Level.setVictoryDestination(1);
             */
        }

        /**
         * @level: 69
         * 
         * @description: this level shows that not all obstacles cause the hero
         *               to be able to jump again
         * 
         * @whatsnew: obstacles that don't re-enable jumping
         */
        else if (whichLevel == 69) {
            /*
             * // set up a standard side scroller with tilt: Level.configure(3 *
             * 460, 320, 0, 10); Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Press the hero to\nmake it jump", 1);
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // make a hero Hero h = Hero.makeAsCircle(20, 20, 30, 30,
             * "greenball.png"); h.setPhysics(1, 0, 0.6f); h.setMoveByTilting();
             * // this says that touching makes the hero jump
             * h.setTouchToJump(); // this is the force of a jump. remember that
             * up is negative, not positive. h.setJumpImpulses(0, -10);
             * 
             * // draw a few walls Obstacle.makeAsBox(100, 300, 100, 10,
             * "red.png"); // hero can jump while on this obstacle Obstacle o =
             * Obstacle.makeAsBox(400, 300, 100, 10, "red.png");
             * o.setReJump(false); // hero can't jump while on this obstacle
             */
        }

        /**
         * @level: 70
         * 
         * @description: A test of the PokePath feature
         * 
         * @whatsnew: setPokePath to move an entity along a path
         */
        else if (whichLevel == 70) {
            /*
             * // start by setting everything up just like in level 1
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10); Hero h =
             * Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setMoveByTilting(); h.setPokePath(4);
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1); Util.drawBoundingBox(0, 0, 460,
             * 320, "red.png", 0, 0, 0); PopUpScene.showTextTimed(
             * "Poke the hero, then\n where you want it\nto go.", 1);
             */
        }

        /**
         * @level: 71
         * 
         * @description: A test of the PokeVelocity feature
         * 
         * @whatsnew: setPokeVelocity to move an entity along a path
         * 
         * @whatsnew: draw an image directly onto the heads-up display
         */
        else if (whichLevel == 71) {
            /*
             * // start by setting everything up just like in level 1
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10); Hero h =
             * Hero.makeAsCircle(40, 70, 30, 30, "stars.png");
             * h.setCanFaceBackwards(); h.setPokeVelocity(4);
             * 
             * Destination.makeAsCircle(290, 60, 10, 10, "mustardball.png");
             * Controls.addImage(40, 40, 40, 40, "red.png");
             * Level.setVictoryDestination(1); Util.drawBoundingBox(0, 0, 460,
             * 320, "red.png", 0, 0, 0); PopUpScene.showTextTimed(
             * "Poke the hero, then\n where you want it\nto go.", 1);
             */
        }

        /**
         * @level: 72
         * 
         * @description: This level tests sticky obstacles
         * 
         * @whatsnew: sticky obstacles. Note that the obstacle must have more
         *            density than the hero for these to work correctly.
         */
        else if (whichLevel == 72) {
            /*
             * // set up a basic side scroller without tilt Level.configure(460,
             * 320, 0, 10);
             * PopUpScene.showTextTimed("Press screen borders\nto move the hero"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, 0, 1);
             * Hero h = Hero.makeAsCircle(20, 290, 30, 30, "greenball.png");
             * h.disableRotation(); // the hero can jump h.setJumpImpulses(0,
             * -5); h.setTouchToJump(); h.setPhysics(1, 0, 0);
             * 
             * // create a destination Destination.makeAsCircle(200, 150, 10,
             * 10, "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * Obstacle o = Obstacle.makeAsBox(100, 300, 80, 5, "red.png");
             * o.setRoute(new Route(5).to(100, 300).to(50, 250).to(100,
             * 200).to(150, 250).to(100, 300), 10, true); o.setPhysics(100, 0,
             * 1f); o.setSticky();
             * 
             * // draw some buttons for moving the hero
             * Controls.addLeftButton(0, 50, 50, 220, "invis.png", 5);
             * Controls.addRightButton(410, 50, 50, 220, "invis.png", 5);
             */
        }

        /**
         * @level: 73
         * 
         * @description: this level shows how to throw projectiles that rotate
         *               correctly, and how to add one-sided entities
         * 
         * @whatsnew: setRotateVectorThrow
         * 
         * @whatsnew: setOneSided
         */
        else if (whichLevel == 73) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Press anywhere\nto throw a ball", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); Destination.makeAsCircle(420, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // draw a button for throwing projectiles in many directions
             * Controls.addVectorThrowButton(0, 0, 460, 320, "invis.png");
             * 
             * // set up our pool of projectiles. The main challenge here is
             * that the farther from the // hero we press, the faster the
             * projectile goes, so we multiply the velocity by .3 to // slow it
             * down a bit Projectile.configure(100, 10, 30, "red.png", 30, 0,
             * 40, 0, 1); Projectile.setFixedVectorThrowVelocity(10);
             * Projectile.setRange(460, 320); Projectile.setRotateVectorThrow();
             * 
             * // create a box that is easy to fall into, but hard to get out of
             * Obstacle top = Obstacle.makeAsBox(100, 100, 100, 2, "red.png");
             * top.setOneSided(2); Obstacle left = Obstacle.makeAsBox(100, 100,
             * 2, 100, "red.png"); left.setOneSided(1); Obstacle right =
             * Obstacle.makeAsBox(200, 100, 2, 100, "red.png");
             * right.setOneSided(3); Obstacle bottom = Obstacle.makeAsBox(100,
             * 250, 100, 2, "red.png"); bottom.setOneSided(0);
             */
        }

        /**
         * @level: 74
         * 
         * @description: this level shows how to use multiple types of goodies
         * 
         * @whatsnew: Different activationscores for destinations
         * 
         * @whatsnew: different goodiecounts from controls
         * 
         * @whatsnew: Goodies with different score types that increment
         * 
         * @whatsnew: triggers with multiple types of goodies in the activation
         * 
         * @whatsnew: adding to the countdown timer via updateTimerExpiration
         */
        else if (whichLevel == 74) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("Green, Red, and Grey\nballs are goodies"
             * , 1); Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1);
             * Hero h = Hero.makeAsCircle(20, 20, 30, 30, "stars.png");
             * h.setMoveByTilting();
             * 
             * // the destination requires lots of goodies of different types
             * Destination d = Destination.makeAsCircle(420, 310, 10, 10,
             * "mustardball.png"); d.setActivationScore1(1);
             * d.setActivationScore2(1); d.setActivationScore3(3);
             * Level.setVictoryDestination(1);
             * 
             * Controls.addGoodieCount1(0, "blue", 10, 10, 0, 255, 255, 16);
             * Controls.addGoodieCount2(0, "green", 10, 40, 0, 255, 255, 16);
             * Controls.addGoodieCount3(0, "red", 10, 70, 0, 255, 255, 16);
             * 
             * Controls.addCountdown(100, "", 250, 10);
             * 
             * for (int i = 0; i < 3; ++i) { Goodie b = Goodie.makeAsCircle(100
             * * i, 300, 10, 10, "blueball.png"); b.setScore1(1); Goodie g =
             * Goodie.makeAsCircle(100 * i + 25, 300, 10, 10, "greenball.png");
             * g.setScore2(1); g.setScore1(0); // or else it counts as blue,
             * too... Goodie r = Goodie.makeAsCircle(100 * i + 60, 300, 10, 10,
             * "redball.png"); r.setScore3(1); r.setScore1(0); // or else it
             * counts as blue, too...
             * 
             * }
             * 
             * Obstacle o = Obstacle.makeAsBox(400, 0, 5, 200, "red.png");
             * o.setHeroCollisionTrigger(0, 1, 1, 1, 0);
             */
        }

        /**
         * @level: 75
         * 
         * @description: this level shows passthrough objects
         * 
         * @whatsnew: setting passthrough
         */
        else if (whichLevel == 75) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 0);
             * Tilt.enable(10, 10);
             * PopUpScene.showTextTimed("You can walk through the wall", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "stars.png");
             * h.setMoveByTilting(); h.setPassThrough(7); // make sure obstacle
             * has same value
             * 
             * // the destination requires lots of goodies of different types
             * Destination d = Destination.makeAsCircle(420, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * Enemy e = Enemy.makeAsCircle(420, 10, 50, 40, "red.png");
             * e.setChaseSpeed(1);
             * 
             * Obstacle o = Obstacle.makeAsBox(400, 0, 5, 200, "red.png");
             * o.setPassThrough(7);
             */
        }

        /**
         * @level: 76
         * 
         * @description: Demonstrate the use of a turbo boost button
         * 
         * @whatsnew: Controls.addTurboButton
         */
        else if (whichLevel == 76) {
            /*
             * // set up a side scroller, but don't turn on tilt
             * Level.configure(3 * 460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press anywhere to speed up", 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // note: the bounding box does not have friction, and neither
             * does the hero Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png",
             * 1, 0, 0);
             * 
             * // make a hero, but don't let it rotate: Hero h =
             * Hero.makeAsBox(20, 250, 30, 70, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0); // give the hero a
             * fixed velocity h.addVelocity(4, 0); // center the camera a little
             * ahead of the hero, so he is not centered h.setCameraOffset(150,
             * 0);
             * 
             * // set up the background Background.setColor(0, 0, 255);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20);
             * 
             * // draw a turbo boost button that covers the whole screen... make
             * sure its "up" speeds match the hero // velocity
             * Controls.addTurboButton(0, 0, 460, 320, "invis.png", 15, 0, 4, 0,
             * h);
             */
        }

        /**
         * @level: 76
         * 
         * @description: Demonstrate a control that doesn't stop the hero upon
         *               release
         * 
         * @whatsnew: Controls.addDampenedMotionButton
         */
        else if (whichLevel == 77) {
            /*
             * // set up a side scroller, but don't turn on tilt
             * Level.configure(3 * 460, 320, 0, 10);
             * PopUpScene.showTextTimed("Press anywhere to speed up", 1);
             * Destination.makeAsCircle(1200, 310, 10, 10, "mustardball.png");
             * Level.setVictoryDestination(1);
             * 
             * // note: the bounding box has friction, so the hero will glide
             * Util.drawBoundingBox(0, 0, 3 * 460, 320, "red.png", 1, 0, 0);
             * 
             * // make a hero, but don't let it rotate: Hero h =
             * Hero.makeAsBox(20, 250, 30, 70, "greenball.png"); // center the
             * camera a little ahead of the hero, so he is not centered
             * h.setCameraOffset(150, 0);
             * 
             * // set up the background Background.setColor(0, 0, 255);
             * Background.addLayer("mid.png", -20, 0, 116);
             * Background.setScrollFactor(20);
             * 
             * // draw a turbo boost button that covers the whole screen... make
             * sure its "up" speeds match the hero // velocity
             * Controls.addDampenedMotionButton(0, 0, 460, 320, "invis.png", 10,
             * 0, 4, h);
             */
        }

        /**
         * @level: 78
         * 
         * @description: Demonstrate how onesided and triggers interact
         */
        else if (whichLevel == 78) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 10);
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Does autojump work?", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); h.setJumpImpulses(0, -10);
             * h.setTouchToJump(); Destination.makeAsCircle(420, 310, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * 
             * // create a box that is easy to fall into, but hard to get out of
             * Obstacle bottom = Obstacle.makeAsBox(100, 250, 100, 2,
             * "red.png"); bottom.setOneSided(0);
             * bottom.setHeroCollisionTrigger(0, 0);
             */
        }

        /**
         * @level: 79
         * 
         * @description: A test of extended PokePath features
         * 
         * @whatsnew: setKeepPokeEntity to avoid re-touching the hero every time
         *            we want to register a new movement
         * 
         * @whatsnew: setPokePathFixedVelocity to always have the same velocity,
         *            regardless of distance
         * 
         * @whatsnew: setPokeChaseMode to track movement of poke presses, not
         *            just down presses
         * 
         * @whatsnew: Controls.addTriggerControl for an on-screen button that
         *            runs custom code
         */
        else if (whichLevel == 79) {
            /*
             * // start by setting everything up just like in level 1
             * Level.configure(460, 320, 0, 0); Tilt.enable(10, 10); Hero h =
             * Hero.makeAsCircle(40, 70, 30, 30, "greenball.png");
             * h.setMoveByTilting(); h.setPokePath(4);
             * h.setPokePathFixedVelocity(4); h.setKeepPokeEntity();
             * h.setPokeChaseMode(); Destination.makeAsCircle(290, 60, 10, 10,
             * "mustardball.png"); Level.setVictoryDestination(1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 0, 0, 0);
             * PopUpScene
             * .showTextTimed("Poke the hero, then\n where you want it\nto go.",
             * 1); Controls.addTriggerControl(40, 40, 40, 40, "red.png", 747);
             */
        }

        /**
         * @level: 80
         * 
         * @description: A test of gravity-defying objects
         * 
         * @whatsnew: setGravityDefy
         */
        else if (whichLevel == 80) {
            /*
             * // set up a simple level Level.configure(460, 320, 0, 10);
             * Tilt.enable(10, 0);
             * PopUpScene.showTextTimed("Testing Gravity Defy?", 1);
             * Util.drawBoundingBox(0, 0, 460, 320, "red.png", 1, .3f, 1); Hero
             * h = Hero.makeAsCircle(20, 20, 30, 30, "greenball.png");
             * h.disableRotation(); h.setPhysics(1, 0, 0.6f);
             * h.setMoveByTilting(); h.setJumpImpulses(0, -10);
             * h.setTouchToJump(); Destination d = Destination.makeAsCircle(420,
             * 140, 10, 10, "mustardball.png"); d.setAbsoluteVelocity(-2, 0);
             * d.setGravityDefy(0, -10f); Level.setVictoryDestination(1);
             */
        }

    }

    /**
     * Describe how each help scene ought to be drawn.
     * 
     * Every game must implement this method to describe how each help scene
     * should appear. Note that you *must* specify the maximum number of help
     * scenes for your game in the res/values/gameconfig.xml file. If you
     * specify "0", then you can leave this code blank.
     * 
     * NB: A real game would need to provide better help. This is just a demo.
     * 
     * @param whichScene
     *            The help scene being drawn. The game engine will set this
     *            value to indicate which scene needs to be drawn.
     */
    @Override
    public void configureHelpScene(int whichScene)
    {
        /*
         * // Our first scene describes the color coding that we use for the //
         * different entities in the game if (whichScene == 1) {
         * HelpScene.configure(0, 0, 0); HelpScene.drawText(100, 5,
         * "The levels of this game demonstrate\nthe features of ALE");
         * 
         * HelpScene.drawPicture(50, 60, 30, 30, "greenball.png");
         * HelpScene.drawText(100, 60, "This is a hero");
         * 
         * HelpScene.drawPicture(50, 100, 30, 30, "blueball.png");
         * HelpScene.drawText(100, 100, "This is an object you can collect");
         * 
         * HelpScene.drawPicture(50, 140, 30, 30, "redball.png");
         * HelpScene.drawText(100, 140, "This is an enemy.  Beware!");
         * 
         * HelpScene.drawPicture(50, 180, 30, 30, "mustardball.png");
         * HelpScene.drawText(100, 180,
         * "This is a destination that the\nhero(s) must reach");
         * 
         * HelpScene.drawPicture(50, 220, 30, 30, "purpleball.png");
         * HelpScene.drawText(100, 220,
         * "This is an obstacle you can \ncollide with");
         * 
         * HelpScene.drawPicture(50, 260, 30, 30, "greyball.png");
         * HelpScene.drawText(100, 260, "This is a projectile you can throw"); }
         * // Our second help scene is just here to show that it is possible to
         * // have more than one help scene. else if (whichScene == 2) {
         * HelpScene.configure(255, 255, 0); HelpScene.drawText(100, 5,
         * "Be sure to read the ALEDemoGame.java code\n" +
         * "while you play, so you can see\n" + "how the game works", 55, 110,
         * 165, 14); }
         */
    }

    /**
     * If a game uses Obstacles that are triggers, it must provide this to
     * specify what to do when such an obstacle is hit by a hero.
     * 
     * The idea behind this mechanism is that it allows the creation of more
     * things in the game, but only after the game has reached a particular
     * state. The most obvious example is 'infinite' levels. There, it is
     * impossible to draw the entire scene, so instead one can place an
     * invisible, full-length TriggerObstacle at some point in the scene, and
     * then when that obstacle is hit, this code will run. If the
     * TriggerObstacle has a unique ID (for example, its 'x' coordinate), then
     * we can use that id to know where on the screen we are, and we can draw
     * the next part of the level correctly.
     * 
     * @param id
     *            The ID of the obstacle that was hit by the hero
     * @param whichLevel
     *            The current level
     */
    @Override
    public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h)
    {
        /*
         * if (whichLevel == 78) { h.setAbsoluteVelocity(h.getXVelocity(), -5);
         * return; } // obstacle trigger code for level 63 if (whichLevel == 63)
         * { // the first trigger just causes us to make a new trigger a little
         * farther out if (id == 0) { Obstacle oo = Obstacle.makeAsBox(600, 0,
         * 10, 320, "purpleball.png"); oo.setPhysics(1, 0, 1);
         * oo.setHeroCollisionTrigger(1, 1); Goodie.makeAsCircle(450, 10, 10,
         * 10, "blueball.png"); o.remove(false); } // same for the second
         * trigger... note that we also need to make a goodie, because we've //
         * chosen to require more goodies to activate these collisiontrigger
         * obstacles else if (id == 1) { Obstacle oo = Obstacle.makeAsBox(900,
         * 0, 10, 320, "purpleball.png"); oo.setPhysics(1, 0, 1);
         * oo.setHeroCollisionTrigger(2, 2); Goodie.makeAsCircle(750, 210, 10,
         * 10, "blueball.png"); o.remove(false); } // same thing else if (id ==
         * 2) { Obstacle oo = Obstacle.makeAsBox(1200, 0, 10, 320,
         * "purpleball.png"); oo.setPhysics(1, 0, 1);
         * oo.setHeroCollisionTrigger(3, 3); Goodie.makeAsCircle(1050, 10, 10,
         * 10, "blueball.png"); o.remove(false); } // well done... now print a
         * message and make the destination else if (id == 3) {
         * PopUpScene.showTextTimed("The destination is\nnow available", 1);
         * Destination.makeAsCircle(1200, 200, 20, 20, "mustardball.png");
         * o.remove(false); } } else if (whichLevel == 74) { // add 15 seconds
         * to the timer Controls.updateTimerExpiration(15); }
         */
    }

    /**
     * If a game uses Obstacles that are touch triggers, it must provide this to
     * specify what to do when such an obstacle is touched by the user
     * 
     * The idea behind this mechanism is that it allows the creation of more
     * interactive games, since there can be items to unlock, treasure chests to
     * open, and other such behaviors.
     * 
     * @param id
     *            The ID of the obstacle that was hit by the hero
     * @param whichLevel
     *            The current level
     */
    @Override
    public void onTouchTrigger(int id, int whichLevel, Obstacle o)
    {
        /*
         * // in level 64, we draw a bunch of goodies when the obstacle is
         * touched. This is supposed to // be like having a treasure chest open
         * up. if (whichLevel == 64) { if (id == 39) { o.remove(false); for (int
         * i = 0; i < 3; ++i) Goodie.makeAsCircle(90 * i, 200 - i, 20, 20,
         * "blueball.png"); } }
         */
    }

    /**
     * If a game uses timer triggers, it must provide this to specify what to do
     * when a timer expires.
     * 
     * @param id
     *            The ID of the obstacle that was hit by the hero
     * @param whichLevel
     *            The current level
     */
    @Override
    public void onTimeTrigger(int id, int whichLevel)
    {
        /*
         * // here's the code for level 62 if (whichLevel == 62) { // after
         * first trigger, print a message, draw an enemy, register a new timer
         * if (id == 0) { PopUpScene.showTextTimed("Ooh... a draggable enemy",
         * 1, 255, 255, 0, 12);
         * 
         * // make a draggable enemy Enemy e3 = Enemy.makeAsCircle(350, 250, 20,
         * 20, "redball.png"); e3.setPhysics(1.0f, 0.3f, 0.6f);
         * e3.setCanDrag(true);
         * 
         * Level.setTimerTrigger(1, 3); } // after second trigger, draw an enemy
         * who disappears on touch, register a new timer else if (id == 1) {
         * PopUpScene.showTextTimed("Touch the enemy and it will go away", 1,
         * 255, 0, 255, 12); Enemy e4 = Enemy.makeAsCircle(350, 50, 20, 20,
         * "redball.png"); e4.setPhysics(1.0f, 0.3f, 0.6f);
         * e4.setDisappearOnTouch(); Level.setTimerTrigger(2, 3); } // after
         * third trigger, draw an enemy, a goodie, and a destination, all with
         * fixed // velocity else if (id == 2) {
         * PopUpScene.showTextTimed("Now you can see the rest of the level", 1,
         * 255, 255, 0, 12);
         * 
         * Destination d = Destination.makeAsCircle(290, 60, 10, 10,
         * "mustardball.png"); d.addVelocity(-5, -10);
         * 
         * Enemy e5 = Enemy.makeAsCircle(350, 150, 20, 20, "redball.png");
         * e5.setPhysics(1.0f, 0.3f, 0.6f); e5.addVelocity(4, 4); Goodie gg =
         * Goodie.makeAsCircle(100, 100, 20, 20, "blueball.png");
         * gg.addVelocity(5, 5); } }
         */
    }

    /**
     * If you want to have enemy timertriggers, then you must override this to
     * define what happens when the timer expires
     * 
     * @param id
     *            The id that was assigned to the timer that exired
     * @param whichLevel
     *            The current level
     * @param e
     *            The enemy to modify
     */
    @Override
    public void onEnemyTimeTrigger(int id, int whichLevel, Enemy e)
    {
        /*
         * if (whichLevel == 48) { // the ID represents the number of remaining
         * reproductions for the current enemy (e), so that we don't //
         * reproduce forever (note that we could, if we wanted to...) Enemy left
         * = Enemy.makeAsCircle(e.getXPosition() - 20 * id, e.getYPosition() +
         * 20 * id, e.getWidth(), e.getHeight(), "redball.png");
         * left.setDisappearSound("lowpitch.ogg"); Enemy right =
         * Enemy.makeAsCircle(e.getXPosition() + 20 * id, e.getYPosition() + 20
         * * id, e.getWidth(), e.getHeight(), "redball.png");
         * right.setDisappearSound("lowpitch.ogg"); if (id > 0) {
         * Level.setEnemyTimerTrigger(id - 1, 2, left);
         * Level.setEnemyTimerTrigger(id - 1, 2, e);
         * Level.setEnemyTimerTrigger(id - 1, 2, right); } } else if (whichLevel
         * == 49) { // in this case, every enemy will produce one offspring on
         * each timer Enemy e2 = Enemy.makeAsCircle(e.getXPosition(),
         * e.getYPosition(), e.getWidth(), e.getHeight(), "redball.png");
         * e2.setPhysics(1.0f, 0.3f, 0.6f); e2.setMoveByTilting(); if (id > 0) {
         * Level.setEnemyTimerTrigger(id - 1, 2, e);
         * Level.setEnemyTimerTrigger(id - 1, 2, e2); } }
         */
    }

    /**
     * If a game has Enemies that have 'defeatTrigger' set, then when any of
     * those enemies are defeated, this code will run
     * 
     * @param id
     *            The ID of the enemy that was defeated by the hero
     * @param whichLevel
     *            The current level
     */
    @Override
    public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e)
    {
        /*
         * if (whichLevel == 65) {
         * PopUpScene.showTextTimed("good job, here's a prize", .6f, 88, 226,
         * 160, 16); // use random numbers to figure out where to draw a goodie
         * as a reward Goodie.makeAsCircle(Util.getRandom(439),
         * Util.getRandom(299), 20, 20, "blueball.png"); }
         */
    }

    /**
     * If you want to have obstacletriggers, then you must override this to
     * define what happens when an enemy hits the obstacle
     * 
     * @param whichLevel
     *            The id that was assigned to the enemy who was defeated
     * @param o
     *            The obstacle involved in the collision
     * @param e
     *            The enemy involved in the collision
     */
    @Override
    public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e)
    {
        /*
         * if (whichLevel == 56) { if (e.getInfoText() == "weak") return; if (id
         * == 0) { e.defeat(true); o.remove(true); } if (id == 1) {
         * e.defeat(true); } }
         * 
         * if (whichLevel == 65) { if (e.getInfoText() == "weak") {
         * e.defeat(true); } }
         */
    }

    /**
     * If you want to have obstacletriggers, then you must override this to
     * define what happens when a projectile hits the obstacle
     * 
     * @param whichLevel
     *            The id that was assigned to the enemy who was defeated
     * @param o
     *            The obstacle involved in the collision
     * @param p
     *            The projectile involved in the collision
     */
    @Override
    public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p)
    {
        /*
         * if (whichLevel == 47) { // do nothing... as long as there is a
         * collision registered, the projectile won't disappear... }
         */
    }

    /**
     * If you want to do something when the level ends (like record a high
     * score), you will need to override this method
     * 
     * @param win
     *            true if the level was won, false otherwise
     * 
     */
    @Override
    public void levelCompleteTrigger(boolean win)
    {
    }

    /**
     * If you use TriggerControls, you must override this to define what happens
     * when the control is pressed
     * 
     * @param id
     *            The id that was assigned to the Control
     * @param whichLevel
     *            The _current level
     */
    @Override
    public void onControlPressTrigger(int id, int whichLevel)
    {
        /*
         * if (id == 747) PopUpScene.showTextTimed("Hello", 1);
         */
    }

    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public ALEConfiguration config()
    {
        return new Config();
    }

    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public SplashConfiguration splashConfig()
    {
        return new SplashConfig();
    }

}
