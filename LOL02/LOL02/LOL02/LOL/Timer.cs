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

using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Threading;

namespace LOL
{
    /**
     * Represents a Timer that can execute asynchronous tasks and manage a small
     * task schedule.
     */
    internal class Timer
    {
        /**
         * Represents a task executed by the timer and a time delay
         */
        internal class TimerTask
        {
            /**
             * Time to execute the task
             */
            private DateTime executeTime;

            /**
             * Action to execute
             */
            private Action task;

            /**
             * Returns the execution time
             */
            internal DateTime ExecuteTime
            {
                get { return executeTime; }
                set { executeTime = value; }
            }

            /**
             * Returns the task delegate
             */
            internal Action Task
            {
                get { return task; }
            }

            /**
             * Starts the task with no delay.
             */
            internal TimerTask(Action a): this(a, 0)
            {
                
            }

            /**
             * Starts a task with a delay in milliseconds
             */
            internal TimerTask(Action a, long delay)
            {
                task = a;
                executeTime = DateTime.Now.AddMilliseconds(delay);
            }
        }

        /**
         * Singleton
         */
        private static Timer inst = new Timer();

        /**
         * Timer for reading time pulses
         */
        private DispatcherTimer t;

        /**
         * List of timer tasks in the schedule
         */
        private List<TimerTask> tasks;

        /**
         * Creates a timer that waits 100 ms to poll the task list.
         */
        internal Timer()
        {
            tasks = new List<TimerTask>();
            t = new DispatcherTimer();
            t.Stop();
            t.Tick += OnTick;
            t.Interval = TimeSpan.FromMilliseconds(100);
        }

        /**
         * Activated when the DispatchTimer pulses.
         * 
         * @param sender the calling object
         * @param args any arguments from the DispatchTimer
         */
        private void OnTick(object sender, EventArgs args)
        {
            lock (tasks)
            {
                for (int j = tasks.Count - 1; j >= 0; --j)
                {
                    if (tasks[j].ExecuteTime <= DateTime.Now)
                    {
                        TimerTask tmp = tasks[j];
                        tasks.RemoveAt(j);

                        Deployment.Current.Dispatcher.BeginInvoke(tmp.Task);
                    }
                }
            }
        }

        /**
         * Returns the Singleton
         */
        internal static Timer Instance
        {
            get { return inst; }
        }

        /**
         * Clears all scheduled timer tasks.
         */
        internal void Clear()
        {
            lock (tasks)
            {
                tasks.Clear();
            }
        }

        /**
         * Stops the timer
         */
        internal void Stop()
        {
            t.Stop();
        }

        /**
         * Starts the timer
         */
        internal void Start()
        {
            t.Start();
        }

        /**
         * Delays all tasks by a certain amount of time.
         * 
         * @param delayMillis the delay in milliseconds
         */
        internal void Delay(long delayMillis)
        {
            lock (tasks)
            {
                foreach (TimerTask t in tasks)
                {
                    t.ExecuteTime = t.ExecuteTime.AddMilliseconds(delayMillis);
                }
            }
        }

        /**
         * Schedules a task for the future.
         * 
         * @param a the action to invoke
         * @param delaySeconds the number of seconds to wait
         */
        internal static void Schedule(Action a, float delaySeconds)
        {
            lock (Timer.Instance.tasks)
            {
                Timer.Instance.tasks.Add(new TimerTask(a, (long)(delaySeconds * 1000.0)));
            }
        }

    }
}
