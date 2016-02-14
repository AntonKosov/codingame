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

    @Test(expected = SuccessException.class)
    public void aLotOfJumps() throws Exception {
        startTest(5, 16, 4, 10, 80, 1, 5);
    }

    @Test(expected = SuccessException.class)
    public void lessJumps() throws Exception {
        startTest(18, 32, 2, 1, 45, 17, 31);
    }

    @Test(expected = SuccessException.class)
    public void tower() throws Exception {
        startTest(1, 100, 0, 86, 12, 0, 98);
    }

    @Test(expected = SuccessException.class)
    public void losserJumps() throws Exception {
        startTest(15, 15, 0, 1, 12, 3, 6);
    }

    @Test(expected = SuccessException.class)
    public void aLotOfWindows() throws Exception {
        startTest(1000, 1000, 719, 491, 27, 501, 501);
    }

    @Test(expected = SuccessException.class)
    public void aLotOfWindows2() throws Exception {
        startTest(1000, 1000, 675, 545, 27, 501, 501);
    }

    @Test(expected = SuccessException.class)
    public void soManyWindows() throws Exception {
        startTest(8000, 8000, 0, 1, 31, 3200, 2100);
    }

//    @Test(expected = SuccessException.class)
//    public void soManyWindows2() throws Exception {
//        startTest(8000, 8000, 28, 42, 31 - 3, 3201, 2100); // -3 turns
//    }

    @Test(expected = SuccessException.class)
    public void soManyWindows3() throws Exception {
        startTest(8000, 8000, 22, 39, 31 - 3, 3201, 2100); // -3 turns
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
