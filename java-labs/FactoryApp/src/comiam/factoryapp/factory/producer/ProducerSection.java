package comiam.factoryapp.factory.producer;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.factory.producer.threadpool.Producer;
import comiam.factoryapp.factory.producer.threadpool.TaskPool;

import java.util.ArrayList;

public class ProducerSection
{
    private TaskPool pool;
    private ArrayList<Thread> workers;
    private boolean init;

    public ProducerSection(Factory factory, int priority)
    {
        workers = new ArrayList<>();
        pool = new TaskPool();

        for(int i = 0; i < factory.getProducerCount(); i++)
        {
            Thread thread = new Thread(new Producer(pool, factory), "Producer");
            thread.setPriority(priority);
            workers.add(thread);
        }

        init = true;
    }


    public void start()
    {
        if(!init)
            return;

        for(var thread : workers)
            thread.start();
    }

    public synchronized TaskPool getPool()
    {
        return pool;
    }

    public void destroy()
    {
        if(!init)
            return;

        for(var thread : workers)
            thread.interrupt();

        workers.clear();
        pool = null;
        workers = null;

        init = false;
    }
}
