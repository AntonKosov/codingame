import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Scanner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Scanner.class, Player.class})
public class PlayerTest {

    private final LinkedList<String> mOutQueue = new LinkedList<String>();
    private PrintStream mOriginalOutPrintStream;
    private int mTurns;
    private int mPrevX;
    private int mPrevY;
    private int mBombX;
    private int mBombY;

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
        startTest(10, 10, 7, 4, 6, 2, 6);
    }

    private void startTest(int width, int height, int bombX, int bombY, final int turns, int startX, int startY) throws Exception {
        mOutQueue.clear();
        mOutQueue.add(String.valueOf(width));
        mOutQueue.add(String.valueOf(height));
        mOutQueue.add(String.valueOf(turns));
        mOutQueue.add(String.valueOf(startX));
        mOutQueue.add(String.valueOf(startY));
        mOutQueue.add("UNKNOWN");

        mTurns = turns;
        mPrevX = startX;
        mPrevY = startY;
        mBombX = bombX;
        mBombY = bombY;

        final Scanner scanner = PowerMockito.mock(Scanner.class);
        PowerMockito.whenNew(Scanner.class).withAnyArguments().thenReturn(scanner);
        PowerMockito.when(scanner.nextInt()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return Integer.parseInt(mOutQueue.pop());
            }
        });
        PowerMockito.when(scanner.next()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return mOutQueue.pop();
            }
        });

        PrintStream printStream = Mockito.mock(PrintStream.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mTurns--;
                Assert.assertTrue(mTurns >= 0);

                final String answer = invocation.getArguments()[0].toString();
                mOriginalOutPrintStream.println(answer);

                final String[] coord = answer.split(" ");
                final int x = Integer.parseInt(coord[0]);
                final int y = Integer.parseInt(coord[1]);

                if (x == mBombX && y == mBombY) {
                    throw new SuccessException();
                }

                int prevDistance2 = (mBombX - mPrevX) * (mBombX - mPrevX) + (mBombY - mPrevY) * (mBombY - mPrevY);
                int currentDistance2 = (mBombX - x) * (mBombX - x) + (mBombY - y) * (mBombY - y);

                if (prevDistance2 == currentDistance2) {
                    mOutQueue.add("SAME");
                } else if (prevDistance2 < currentDistance2) {
                    mOutQueue.add("COLDER");
                } else {
                    mOutQueue.add("WARMER");
                }

                mPrevX = x;
                mPrevY = y;

                return null;
            }
        }).when(printStream).println(Mockito.anyString());
        System.setOut(printStream);

        Player.main(null);
    }

    private static class SuccessException extends Exception {

    }
}
