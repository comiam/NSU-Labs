package comiam.jcalc.calculator;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCalculatorTest
{
    @Test
    public void execute() throws IOException
    {
        PrintStream stream = new PrintStream(new File("test"));
        stream.print("DEFINE a 4\n" +
                     "PUSH a\n" +
                     "PUSH 4\n" +
                     "DefinE b 13\n" +
                     "PUSH b\n" +
                     "PUSH 33\n" +
                     "PUSH 16 #qSQS\n" +
                     "#PUSH 16 #qSQS\n" +
                     "sqrt\n" +
                     "PUawSH 4\n" +
                     "-\n" +
                     "+\n" +
                     "push 2\n" +
                     "/\n" +
                     "push 3\n" +
                     "/\n" +
                     "*\n" +
                     "+\n" +
                     "PRINT");
        stream.close();

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/home/maxim/IdeaProjects/calculator/out/artifacts/calculator_jar/calculator.jar", "test");
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        StringBuilder message = new StringBuilder();

        while((s = in.readLine()) != null)
            message.append(s);

        p.destroy();
        Files.delete(Paths.get(new File("test").toURI()));

        Assert.assertEquals((int)Double.parseDouble(message.toString()) ,32);
    }
}