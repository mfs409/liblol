/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

/**
 * The ScreenManager interface provides a way for programmers to tell LibLOL how to configure a
 * screen.  The codes for building the Splash, Help, Store, Config, and Levels should each be a
 * ScreenManager.
 */
public interface ScreenManager {
    /**
     * Configure a Screen, so that it can be displayed and used by the game player.
     *
     * @param index The numerical index of the screen to display. Your code should use an
     *              <code>if</code> statement to decide what screen to display based on the value of
     *              <code>index</code>
     * @param level The actual level to configure.  Calling methods of <code>level</code> is
     *              the way to start putting stuff into the level.
     */
    void display(int index, Level level);
}
