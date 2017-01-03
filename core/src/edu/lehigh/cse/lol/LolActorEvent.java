package edu.lehigh.cse.lol;

/**
 * LolActorEvent describes code to run in response to an event, when the only information needed
 * to handle the event is the specific WorldActor who was involved in the event.
 */
public interface LolActorEvent {
    void go(WorldActor actor);
}
