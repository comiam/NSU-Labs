package comiam.jcalc.factory;

import comiam.jcalc.exception.operations.UnknownOperationException;
import comiam.jcalc.loader.Loader;
import comiam.jcalc.operations.Operation;

import java.lang.reflect.InvocationTargetException;

public abstract class OperationCreator
{
    public static Operation getOperation(String name) throws UnknownOperationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return (Operation) Loader.getOperationClass(name).getConstructor().newInstance();
    }
}
