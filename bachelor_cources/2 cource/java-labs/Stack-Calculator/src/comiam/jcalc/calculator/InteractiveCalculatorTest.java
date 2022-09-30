package comiam.jcalc.calculator;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class InteractiveCalculatorTest
{
    @Test
    public void execute() throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/home/maxim/IdeaProjects/calculator/out/artifacts/calculator_jar/calculator.jar");
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        PrintStream out = new PrintStream(p.getOutputStream());

        in.readLine();
        out.println("DEFINE a 4");
        out.flush();
        out.println("PUSH a");
        out.flush();
        out.println("PUSH 4");
        out.flush();
        out.println("DefinE b 13 #adawdawd");
        out.flush();
        out.println("PUSH b");
        out.flush();
        out.println("PUSH 33");
        out.flush();
        out.println("PUSH 16");
        out.flush();
        out.println("sqrt");
        out.flush();
        out.println("PUawSH 4");
        out.flush();
        out.println("#adawdawd push");
        out.println("-");
        out.flush();
        out.println("+");
        out.flush();
        out.println("push 2");
        out.flush();
        out.println("/");
        out.flush();
        out.println("push 3");
        out.flush();
        out.println("/");
        out.flush();
        out.println("*");
        out.flush();
        out.println("+");
        out.flush();

        out.println("print");
        out.flush();
        String message = in.readLine();

        out.println("exit");
        out.flush();

        p.destroy();

        Assert.assertTrue(message.contains("32"));
    }
}