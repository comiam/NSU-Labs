package comiam.jcalc.calculator;

import comiam.jcalc.exception.stack.EmptyStackException;
import comiam.jcalc.exception.stack.UnknownVariableException;
import comiam.jcalc.log.Log;
import comiam.jcalc.utils.ArgChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class CalculatorStack
{
    private static long uniqueVarCount;
    private static long varCount;
    private static final ArrayList<String> initVars;
    private static final Stack<String> varStack;
    private static final HashMap<String, Double> varMap;

    static {
        varStack        = new Stack<>();
        varMap          = new HashMap<>();
        initVars        = new ArrayList<>();
    }

    public static long getVarCount()
    {
        return varCount;
    }

    public static void createVar(String name, double value)
    {
        if(!initVars.contains(name) && !ArgChecker.isNumeric(name))
        {
            initVars.add(name);
            uniqueVarCount++;
        }

        varMap.put(name, value);
    }

    public static void push(String var) throws UnknownVariableException
    {
        if(initVars.contains(var))
            varStack.push(var);
        else
            throw new UnknownVariableException(var);

        varCount++;
    }

    public static void push(double value)
    {
        varStack.push("" + uniqueVarCount);
        varMap.put("" + uniqueVarCount, value);

        uniqueVarCount++;
        varCount++;
    }

    public static double pop() throws EmptyStackException
    {
        if(getVarCount() == 0)
            throw new EmptyStackException();
        else
        {
            varCount--;
            if(ArgChecker.isNumeric(varStack.peek()))
                return varMap.remove(varStack.pop());
            else
                return varMap.get(varStack.pop());
        }
    }

    public static double peek() throws EmptyStackException
    {
        if(getVarCount() == 0)
            throw new EmptyStackException();

        return varMap.get(varStack.peek());
    }

    public static void printStack()
    {
        StringBuilder message = new StringBuilder("[");
        if(varStack.isEmpty())
            message.append("empty");
        for(int i = 0; i < varStack.size(); i++)
            message.append(ArgChecker.isNumeric(varStack.get(i)) ? "var" + varStack.get(i) : varStack.get(i)).append(" = ").append(varMap.get(varStack.get(i))).append(i == varStack.size() - 1 ? "" : ", ");
        message.append("]");

        Log.log(Log.LogType.INFO, message.toString(), null);
    }

    public static void clear()
    {
        varMap.clear();
        varStack.clear();
        initVars.clear();
        varCount = 0;
        uniqueVarCount = 0;
    }
}
