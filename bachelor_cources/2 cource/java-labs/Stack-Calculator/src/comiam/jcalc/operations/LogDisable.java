package comiam.jcalc.operations;

import comiam.jcalc.log.Log;

public class LogDisable extends Operation
{
    @Override
    public void exec()
    {
        Log.disableLogging();
    }
}