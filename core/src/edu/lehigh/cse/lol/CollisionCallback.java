package edu.lehigh.cse.lol;

import com.badlogic.gdx.physics.box2d.Contact;

/**
 * A callback to run when a WorldActor collides with another WorldActor
 */
public interface CollisionCallback {
    /**
     * Provide some code to run in response to a collision between actors.
     *
     * @param thisActor    The actor to which this callback was attached
     * @param collideActor The actor who collided with <code>thisActor</code>
     * @param contact      A low-level description of the collision event
     */
    void go(WorldActor thisActor, WorldActor collideActor, Contact contact);
}
