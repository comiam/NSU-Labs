package comiam.jcalc.operations;

import comiam.jcalc.log.Log;

public class LogEnable extends Operation
{
    @Override
    public void exec()
    {
        Log.init();
        Log.enableLogging();
    }
}
