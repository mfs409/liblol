package edu.lehigh.cse.lol;

import com.badlogic.gdx.physics.box2d.Contact;

/**
 * When an WorldActor collides with another WorldActor, and that collision is intended to
 * cause some custom code to run, we use this interface
 */
public interface CollisionCallback {
    /**
     * Respond to a collision with a actor. Note that one of the collision
     * actors is not named; it should be clear from the context in which this
     * was constructed.
     *
     * @param contact A description of the contact, in case it is useful
     */
    void go(WorldActor thisActor, WorldActor collideActor, Contact contact);
}
