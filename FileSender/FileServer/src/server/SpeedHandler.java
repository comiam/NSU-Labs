package server;

import java.text.DecimalFormat;

import static server.DataBundle.*;

public class SpeedHandler implements Runnable
{
    private final long id;
    private boolean running;

    public SpeedHandler(int id)
    {
        this.id = id;
        this.running = false;
    }

    public void finish()
    {
        if(running)
        {
            running = false;
            synchronized (this)
            {
                this.notifyAll();
            }
        }
    }

    @Override
    public void run()
    {
        running = true;
        long transferStartTime = System.currentTimeMillis();
        long currentTime;
        long previousTime = transferStartTime;

        try
        {
            long previousBytesReceivedValue = 0;
            while (!Thread.currentThread().isInterrupted() && running)
            {
                synchronized (this)
                {
                    this.wait(3000);
                }

                currentTime = System.currentTimeMillis();

                double timeElapsed = ((double) currentTime - transferStartTime) / 1000.0;
                double totalDataReceived = (double) getTotalBytesReceived(id) / (1024 * 1024);
                DecimalFormat decimalFormat = new DecimalFormat("####.##");

                double secondSinceLastSpeedCheck = (double) (currentTime - previousTime) / 1000;
                double recentDataReceived = (double) (getTotalBytesReceived(id) - previousBytesReceivedValue) / (1024 * 1024);

                System.out.println(id + " client : total speed " + decimalFormat.format(totalDataReceived / timeElapsed) + " mb/sec");
                System.out.println(id + " client : current speed " + decimalFormat.format(recentDataReceived / secondSinceLastSpeedCheck) + " mb/sec");

                previousBytesReceivedValue = getTotalBytesReceived(id);
                previousTime = currentTime;
            }
        } catch (InterruptedException ex)
        {
            System.out.println("Error during receiving file!");
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}