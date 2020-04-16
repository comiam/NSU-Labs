package comiam.factoryapp.factory.producer.threadpool;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskPool
{
    private final Queue<FactoryTask> tasks;

    public TaskPool()
    {
        tasks = new LinkedBlockingQueue<>(3000);
    }

    public synchronized void pushTask(FactoryTask task)
    {
        tasks.offer(task);

        notifyAll();
    }

    public synchronized FactoryTask getTask()
    {
        while(isEmpty())
            try
            {
                this.wait();
            }catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return null;
            }

        return tasks.peek();
    }

    public synchronized void removeFirstTask()
    {
        if(isEmpty())
            return;

        tasks.poll();
    }

    public synchronized int getCountOfTaskJobs()
    {
        int result = 0;

        for(var task : tasks)
            result += task.getCountOfCars();

        return result;
    }

    public synchronized boolean isEmpty()
    {
        return tasks.size() == 0;
    }
}
