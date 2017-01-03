package edu.lehigh.cse.lol;

/**
 * LolActorEvent describes code to run in response to an event, when the only information needed
 * to handle the event is the specific WorldActor who was involved in the event.
 */
public interface LolActorEvent {
    /**
     * The go() method encapsulates the code that should be run
     *
     * @param actor The actor involved in this event
     */
    void go(WorldActor actor);
}
