package comiam.factoryapp.factory.producer.threadpool;

public class FactoryTask
{
    private int countOfCars;

    public FactoryTask(int countOfCars)
    {
        this.countOfCars = countOfCars;
    }

    public synchronized int getJob()
    {
        if(countOfCars == 0)
            return 0;

        return countOfCars--;
    }
    public synchronized boolean isDone()
    {
        return countOfCars == 0;
    }

    public int getCountOfCars()
    {
        return countOfCars;
    }
}
