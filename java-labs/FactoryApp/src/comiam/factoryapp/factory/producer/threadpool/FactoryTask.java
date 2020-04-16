package comiam.factoryapp.factory.producer.threadpool;

public class FactoryTask
{
    private int countOfCars;

    public FactoryTask(int countOfCars)
    {
        this.countOfCars = countOfCars;
    }

    public synchronized void getCarJob()
    {
        if(countOfCars <= 0)
            return;

        countOfCars--;
    }
    public synchronized boolean isDone()
    {
        return countOfCars <= 0;
    }

    public synchronized int getCountOfCars()
    {
        return countOfCars;
    }
}
