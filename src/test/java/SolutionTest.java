import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Scanner.class, Solution.class})
public class SolutionTest {
    private PrintStream mOriginalOutPrintStream;

    private final LinkedList<String> mOutQueue = new LinkedList<>();

    private long mAnswer;

    @Before
    public void before() {
        mOriginalOutPrintStream = System.out;
    }

    @After
    public void after() {
        System.setOut(mOriginalOutPrintStream);
    }

    @Test(expected = SuccessException.class)
    public void sample() throws Exception {
        startTest(
                "......-...-..---.-----.-..-..-..",
                new String[] {
                        "HELL",
                        "HELLO",
                        "OWORLD",
                        "WORLD",
                        "TEST"
                },
                2
        );
    }

    @Test(expected = SuccessException.class)
    public void myTest0() throws Exception {
        startTest(
                "....----",
                new String[] {
                        "E",
                        "EE",
                        "T",
                        "TT"
                },
                25
        );
    }

    @Test(expected = SuccessException.class)
    public void myTest1() throws Exception {
        startTest(
                ".........",
                new String[] {
                        "E",
                        "EEE"
                },
                19
        );
    }

    private void startTest(String morse, String[] dictionary, long answer) throws Exception {
        mOutQueue.clear();
        mOutQueue.add(morse);
        mOutQueue.add(String.valueOf(dictionary.length));
        Collections.addAll(mOutQueue, dictionary);
        mAnswer = answer;

        final Scanner scanner = PowerMockito.mock(Scanner.class);
        PowerMockito.whenNew(Scanner.class).withAnyArguments().thenReturn(scanner);
        PowerMockito.when(scanner.nextInt()).thenAnswer(invocation -> Integer.parseInt(mOutQueue.pop()));
        PowerMockito.when(scanner.next()).thenAnswer(invocation -> mOutQueue.pop());

        PrintStream printStream = Mockito.mock(PrintStream.class);
        Mockito.doAnswer(invocation -> {
            final String answer1 = invocation.getArguments()[0].toString();
            mOriginalOutPrintStream.println(answer1);
            Assert.assertEquals(mAnswer, Long.parseLong(answer1));

            throw new SuccessException();
        }).when(printStream).println(Mockito.anyLong());
        System.setOut(printStream);

        Solution.main(null);
    }

    private static class SuccessException extends Exception {

    }
}
