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

import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * The Fact interface provides a way for the programmer to store per-level,
 * per-session, and per-game (persistent) information about the game. As an
 * example, in a game of golf, one could use per-level to track the current
 * number of strokes, use per-session to track the score across all 18 holes,
 * and per-game to track the player's high score.
 */
public class Facts {

    /**
     * Store string/integer pairs that get reset at the end of every level
     */
    private static final Hashtable<String, Integer> mLevelFacts = new Hashtable<String, Integer>();

    /**
     * Store string/integer pairs that get reset whenever we restart the program
     */
    private static final Hashtable<String, Integer> mSessionFacts = new Hashtable<String, Integer>();

    /**
     * Reset all per-level facts
     */
    static void resetLevelFacts() {
        mLevelFacts.clear();
    }

    /**
     * Reset all per-session facts
     */
    static void resetSessionFacts() {
        mSessionFacts.clear();
    }

    /**
     * Look up a fact that was stored for the current level. If no such fact
     * exists, -1 will be returned.
     * 
     * @param factName
     *            The name used to store the fact
     * @return The integer value corresponding to the last value stored
     */
    public static int getLevelFact(String factName) {
        Integer i = mLevelFacts.get(factName);
        if (i == null) {
            Util.message("ERROR", "Error retreiving level fact '" + factName
                    + "'");
            return -1;
        }
        return i;
    }

    /**
     * Save a fact about the current level. If the factName has already been
     * used for this level, the new value will overwrite the old.
     * 
     * @param factName
     *            The name for the fact being saved
     * @param factValue
     *            The integer value that is the fact being saved
     */
    public static void putLevelFact(String factName, int factValue) {
        mLevelFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, -1 will be returned.
     * 
     * @param factName
     *            The name used to store the fact
     * @return The integer value corresponding to the last value stored
     */
    public static int getSessionFact(String factName) {
        Integer i = mSessionFacts.get(factName);
        if (i == null) {
            Util.message("ERROR", "Error retreiving level fact '" + factName
                    + "'");
            return -1;
        }
        return i;
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     * 
     * @param factName
     *            The name for the fact being saved
     * @param factValue
     *            The integer value that is the fact being saved
     */
    public static void putSessionFact(String factName, int factValue) {
        mSessionFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, -1 will be returned.
     * 
     * @param factName
     *            The name used to store the fact
     * @return The integer value corresponding to the last value stored
     */
    public static int getGameFact(String factName) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mConfig
                .getStorageKey());
        return prefs.getInteger(factName, -1);
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     * 
     * @param factName
     *            The name for the fact being saved
     * @param factValue
     *            The integer value that is the fact being saved
     */
    public static void putGameFact(String factName, int factValue) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mConfig
                .getStorageKey());
        prefs.putInteger(factName, factValue);
        prefs.flush();
    }
}