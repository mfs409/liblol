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

package com.me.mylolgame;

import edu.lehigh.cse.lol.ScreenManager;

/**
 * In games that have a store, there should be a button on the main screen (or
 * somewhere!) for going to the first screen of the store.
 *
 * The store is built the same way as any other part of a game, but you'll
 * probably want to use the Facts interface to keep track of coins, inventories,
 * etc.
 *
 * Our demo doesn't use a store, so we leave our code blank.
 */
public class Store implements ScreenManager {
    /**
     * This function draws the different screens of our store
     *
     * @param which The store screen to draw.
     */
    public void display(int which) {
        // If the store has multiple screens, use Lol.doStore(int) as the
        // callback on buttons for navigating among store screens
    }
}
