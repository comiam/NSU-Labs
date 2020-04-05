package comiam.jcalc.loader;

import comiam.jcalc.exception.operations.InvalidNameException;
import comiam.jcalc.exception.operations.UnknownOperationException;
import comiam.jcalc.exception.parsing.ParsingConfigurationException;
import comiam.jcalc.utils.ArgChecker;

import java.util.HashMap;
import java.util.Scanner;

public class Loader
{
    private static final HashMap<String, Class<?>> operations;

    static {
        operations = new HashMap<>();
        try(Scanner scan = new Scanner(Loader.class.getResourceAsStream("/res/config")))
        {
            String op, classPath;
            while(scan.hasNext())
            {
                op = scan.next().toLowerCase();
                if(ArgChecker.isNumeric(op))
                    throw new InvalidNameException("Numeric operation name " + op);
                if(!scan.hasNext())
                    throw new ParsingConfigurationException("Can't get class path for operation " + op + "!!!");
                classPath = scan.next();
                operations.put(op, Class.forName(classPath));
            }
        }catch(Throwable e)
        {
            e.printStackTrace(System.err);
        }finally
        {
            if(operations.isEmpty())
                System.err.println("I haven't any declared operations in configuration file!");
        }
    }

    public static Class<?> getOperationClass(String name) throws UnknownOperationException
    {
        name = name.toLowerCase();
        if(operations.containsKey(name))
            return operations.get(name);
        else
            throw new UnknownOperationException(name);
    }
}
