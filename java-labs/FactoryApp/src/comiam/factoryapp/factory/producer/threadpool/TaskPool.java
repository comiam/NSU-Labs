package comiam.factoryapp.factory.producer.threadpool;

import java.util.ArrayList;

public class TaskPool
{
    private ArrayList<FactoryTask> taskArray;

    public TaskPool()
    {
        taskArray = new ArrayList<>();
    }

    public synchronized void pushTask(FactoryTask task)
    {
        taskArray.add(task);

        notifyAll();
    }

    public synchronized FactoryTask getTask()
    {
        if(isEmpty())
            return null;
        else
            return taskArray.get(0);
    }

    public synchronized void removeFirstTask()
    {
        if(isEmpty())
            return;

        taskArray.remove(0);
    }

    public int getCountOfTaskJobs()
    {
        int result = 0;
        synchronized(this)
        {
            for(var task : taskArray)
                result += task.getCountOfCars();
        }
        return result;
    }

    public synchronized boolean isEmpty()
    {
        return taskArray.size() == 0;
    }
}
