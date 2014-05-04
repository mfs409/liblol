using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Threading;

namespace LOL
{
    internal class Timer
    {

        internal class TimerTask
        {
            //private long waitTicks;
            private DateTime executeTime;
            private Action task;

            internal DateTime ExecuteTime
            {
                get { return executeTime; }
            }

            /*internal long WaitTicks
            {
                get { return waitTicks; }
            }*/

            internal Action Task
            {
                get { return task; }
            }

            internal TimerTask(Action a): this(a, 0)
            {
                
            }

            internal TimerTask(Action a, long delay)
            {
                task = a;
                executeTime = DateTime.Now.AddMilliseconds(delay);
                //waitTicks = delay;
            }
        }

        private static Timer inst = new Timer();

        private DispatcherTimer t;

        private List<TimerTask> tasks;

        //private long tickCounter;

        internal Timer()
        {
            tasks = new List<TimerTask>();
            t = new DispatcherTimer();
            t.Stop();
            t.Tick += OnTick;
            //tickCounter = 0;
            t.Interval = TimeSpan.FromMilliseconds(100);
        }

        private void OnTick(object sender, EventArgs args)
        {
            //++tickCounter;
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

        internal static Timer Instance
        {
            get { return inst; }
        }

        internal void Clear()
        {
            lock (tasks)
            {
                tasks.Clear();
            }
        }

        internal void Stop()
        {
            t.Stop();
        }

        internal void Start()
        {
            t.Start();
        }

        internal void Delay(long delayMillis)
        {
            lock (tasks)
            {
                foreach (TimerTask t in tasks)
                {
                    t.ExecuteTime.AddMilliseconds(delayMillis);
                }
            }
        }

        internal static void Schedule(Action a, float delaySeconds)
        {
            lock (Timer.Instance.tasks)
            {
                Timer.Instance.tasks.Add(new TimerTask(a, (long)(delaySeconds * 1000.0)));
            }
        }

    }
}
