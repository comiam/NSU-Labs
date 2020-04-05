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

    public ProducerSection(Factory factory)
    {
        workers = new ArrayList<>();
        pool = new TaskPool();

        for(int i = 0; i < factory.getProducerDelay(); i++)
            workers.add(new Thread(new Producer(factory, pool)));

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
            try
            {
                thread.join();
            }catch(InterruptedException ignored){}

        workers.clear();
        pool = null;
        workers = null;

        init = false;
    }
}
