using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Threading;
using System.ComponentModel;
using System.Windows;
using Microsoft.Xna.Framework;


namespace LOL
{
    public class Timer
    {
        public class TimerTask
        {
            public long waitTicks;
            public TimerDelegate task;

            public TimerTask(TimerDelegate t)
            {
                waitTicks = 0;
                task = t;
            }

            public TimerTask(TimerDelegate t, long delay)
            {
                waitTicks = delay;
                task = t;
            }
        }

        // Singleton instance
        private static Timer inst;
        // Dispatcher Timer object
        private DispatcherTimer t;
        // Task list
        private List<TimerTask> tasks;
        // Global tick counter
        private long tickCounter;

        // Constructor
        Timer()
        {
            tasks = new List<TimerTask>();
            t = new DispatcherTimer();
            t.Stop();
            t.Tick += OnTick;
            tickCounter = 0;
            // Wait only for 100ms
            t.Interval = TimeSpan.FromMilliseconds(100);
        }

        // Timer pulsating/ticking event
        private void OnTick(Object sender, EventArgs args)
        {
            // Concerns about thread safety or even multithreading capabilities (tls)
            tickCounter++;
            for (int j = tasks.Count - 1; j >= 0; j--)
            {
                if (tasks[j].waitTicks <= tickCounter)
                {
                    TimerTask tmp = tasks[j];
                    tasks.RemoveAt(j);

                    // Run the task in a background thread -- may cause issues (tls)
                    Deployment.Current.Dispatcher.BeginInvoke(tmp.task, null);
                }
            }
        }

        // Singletons (yay)
        public static Timer instance()
        {
            if (inst == null)
            {
                inst = new Timer();
            }
            return inst;
        }

	    /** Cancels all tasks. */
	    public void clear () {
            tasks.Clear();
	    }

        /** Stops the timer, tasks will not be executed and time that passes will not be applied to the task delays. */
        public void stop() {
            t.Stop();
	    }

        /** Starts the timer if it was stopped. */
        public void start() {
            t.Start();
	    }

	    /** Adds the specified delay to all tasks. */
	    public void delay (long delayMillis) {
            // Because intervals are 100ms each
            tickCounter -= (delayMillis / 100);
	    }

        // Delegate for Timer tasks
        public delegate void TimerDelegate();

        /** Schedules a task on {@link #instance}.
	     * @see #scheduleTask(Task, float) */
        public static void schedule(TimerDelegate t, float delaySec)
        {
            // Concerns about synchronization / thread-safety with use of tickCounter and active timer (tls)
            long actualDelay = (long)delaySec + Timer.instance().tickCounter;
            Timer.instance().tasks.Add(new TimerTask(t, actualDelay));
        }
    }
}
