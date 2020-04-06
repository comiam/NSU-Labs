package comiam.factoryapp.gui.uithread;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.fxml.MainWindowController;

public class ThreadUpdater implements Runnable
{
    private Factory factory;
    private MainWindowController controller;

    public ThreadUpdater(MainWindowController controller, Factory factory)
    {
        this.controller = controller;
        this.factory = factory;
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            //TODO Чото делаем, обновляем текстовые поля в приложухе из завода самогона
        }
    }
}
