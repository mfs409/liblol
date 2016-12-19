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

package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Destinations are actors that the hero should try to reach. When a hero
 * reaches a destination, the hero disappears, and the score updates.
 */
public class Destination extends Actor {
    /**
     * number of heroes who can fit at /this/ destination
     */
    int mCapacity;

    /**
     * number of heroes already in /this/ destination
     */
    int mHolding;

    /**
     * number of type each type of goodies that must be collected before this
     * destination accepts any heroes
     */
    int[] mActivation = new int[4];

    /**
     * Sound to play when a hero arrives at this destination
     */
    Sound mArrivalSound;

    /**
     * Create a destination. This is an internal method, the game designer
     * should not use this.
     *
     * @param width   Width of this destination
     * @param height  Height of this destination
     * @param imgName Name of the image to display
     */
    protected Destination(Level level, float width, float height, String imgName) {
        super(level, imgName, width, height);
        mCapacity = 1;
        mHolding = 0;
    }

    /**
     * Destinations are the last collision detection actor, so their collision
     * detection code does nothing.
     *
     * @param other   Other object involved in this collision
     * @param contact A description of the collision between this destination and
     *                the other actor
     */
    @Override
    void onCollide(Actor other, Contact contact) {
    }

    /**
     * Change the number of goodies that must be collected before the
     * destination accepts any heroes (the default is 0,0,0,0)
     *
     * @param score1 The number of type-1 goodies that must be collected.
     * @param score2 The number of type-2 goodies that must be collected.
     * @param score3 The number of type-3 goodies that must be collected.
     * @param score4 The number of type-4 goodies that must be collected.
     */
    public void setActivationScore(int score1, int score2, int score3, int score4) {
        mActivation[0] = score1;
        mActivation[1] = score2;
        mActivation[2] = score3;
        mActivation[3] = score4;
    }

    /**
     * Change the number of heroes that can be accepted by this destination (the
     * default is 1)
     *
     * @param heroes The number of heroes that can be accepted
     */
    public void setHeroCount(int heroes) {
        mCapacity = heroes;
    }

    /**
     * Specify the sound to play when a hero arrives at this destination
     *
     * @param soundName The name of the sound file that should play
     */
    public void setArrivalSound(String soundName) {
        mArrivalSound = mLevel.mMedia.getSound(soundName);
    }
}
