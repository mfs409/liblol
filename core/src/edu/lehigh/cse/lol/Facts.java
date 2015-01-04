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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * The Fact interface provides a way for the programmer to store per-level,
 * per-session, and per-game (persistent) information about the game. As an
 * example, in a game of golf, one could use per-level to track the current
 * number of strokes, use per-session to track the score across all 18 holes,
 * and per-game to track the player's high score.
 *
 * One might argue that it's easier to create fields inside of some of the game
 * classes to handle this role. However, doing so is (a) confusing for novices,
 * and (b) risky if the programmer isn't completely certain about the lifecycle
 * of the object. Using Facts takes a bit more code, but in the end it's easier.
 */
public class Facts {
    /**
     * Look up a fact that was stored for the current level. If no such fact
     * exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public static int getLevelFact(String factName, int defaultVal) {
        Integer i = Lol.sGame.mCurrentLevel.mLevelFacts.get(factName);
        if (i == null) {
            Util.message("ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current level. If the factName has already been
     * used for this level, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public static void putLevelFact(String factName, int factValue) {
        Lol.sGame.mCurrentLevel.mLevelFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, -1 will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public static int getSessionFact(String factName, int defaultVal) {
        Integer i = Lol.sGame.mSessionFacts.get(factName);
        if (i == null) {
            Util.message("ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public static void putSessionFact(String factName, int factValue) {
        Lol.sGame.mSessionFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
    public static int getGameFact(String factName, int defaultVal) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mStorageKey);
        return prefs.getInteger(factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public static void putGameFact(String factName, int factValue) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mStorageKey);
        prefs.putInteger(factName, factValue);
        prefs.flush();
    }

    /**
     * Look up an Actor that was stored for the current level. If no such Actor
     * exists, null will be returned.
     *
     * @param actorName The name used to store the Actor
     * @return The last Actor stored with this name
     */
    public static Actor getLevelActor(String actorName) {
        Actor actor = Lol.sGame.mCurrentLevel.mLevelActors.get(actorName);
        if (actor == null) {
            Util.message("ERROR", "Error retreiving level fact '" + actorName + "'");
            return null;
        }
        return actor;
    }

    /**
     * Save a Actor from the current level. If the actorName has already been
     * used for this level, the new value will overwrite the old.
     *
     * @param actorName The name for the Actor being saved
     * @param actor     The Actor that is the fact being saved
     */
    public static void putLevelActor(String actorName, Actor actor) {
        Lol.sGame.mCurrentLevel.mLevelActors.put(actorName, actor);
    }
}
