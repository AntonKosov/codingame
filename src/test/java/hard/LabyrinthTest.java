package hard;

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
@PrepareForTest({Scanner.class, Labyrinth.class})
public class LabyrinthTest {

    private final LinkedList<String> mOutQueue = new LinkedList<String>();

    private int mKX;
    private int mKY;
    private int mTurnsLeft;
    private int mTurnsBackLeft;

    private State mState;

    private PrintStream mOriginalOutPrintStream;

    @Before
    public void before() {
        mOriginalOutPrintStream = System.out;
    }

    @After
    public void after() {
        System.setOut(mOriginalOutPrintStream);
    }

    @Test(expected = SuccessException.class)
    public void easy1Direction() throws Exception {
        startTest(7,
                new String[]{
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "???############???????????????",
                        "???############???????????????",
                        "???##T......C##???????????????",
                        "???############???????????????",
                        "???############???????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????",
                        "??????????????????????????????"
                });
    }

    @Test(expected = SuccessException.class)
    public void easyEmpty() throws Exception {
        startTest(40,
                new String[]{
                        "##############################",
                        "#T...........................#",
                        "##...........................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#............................#",
                        "#...........................C#",
                        "##############################",
                });
    }

    @Test(expected = SuccessException.class)
    public void difficultLonger() throws Exception {
        startTest(71,
                new String[] {
                        "##############################",
                        "#............................#",
                        "#..####################......#",
                        "#.....................#..C...#",
                        "#............###..##..#..#...#",
                        "##.###########################",
                        "#...#.....##......##.........#",
                        "#...#..#..##..##..##..####...#",
                        "###....#......##......##.....#",
                        "#..#.#######################.#",
                        "#..#.#................#......#",
                        "#..#.#................T......#",
                        "#..#.#######################.#",
                        "#............................#",
                        "##############################"
                });
    }

    private void startTest(int alarm, final String[] maze) throws Exception {
        final int height = maze.length;
        final int width = maze[0].length();
        mOutQueue.clear();
        for (int y = 0; y < maze.length; y++) {
            String row = maze[y];
            final int indexOf = row.indexOf("T");
            if (indexOf >= 0) {
                mKX = indexOf;
                mKY = y;
                break;
            }
        }
        mTurnsLeft = 1200;
        mTurnsBackLeft = alarm;
        mState = State.goToControl;

        final boolean[][] visibility = new boolean[width][height];
        setVisibility(height, width, visibility);

        mOutQueue.add(String.valueOf(height));
        mOutQueue.add(String.valueOf(width));
        mOutQueue.add(String.valueOf(alarm));

        mOutQueue.add(String.valueOf(mKY));
        mOutQueue.add(String.valueOf(mKX));
        putMaze(height, width, maze, visibility);

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
                final String answer = invocation.getArguments()[0].toString();
                mOriginalOutPrintStream.println(answer);

                if (answer.equals("UP")) {
                    mKY--;
                } else if (answer.equals("RIGHT")) {
                    mKX++;
                } else if (answer.equals("DOWN")) {
                    mKY++;
                } else if (answer.equals("LEFT")) {
                    mKX--;
                }

                String currentCell = maze[mKY].substring(mKX, mKX + 1);

                Assert.assertNotEquals("#", currentCell);

                setVisibility(height, width, visibility);
                mOutQueue.add(String.valueOf(mKY));
                mOutQueue.add(String.valueOf(mKX));
                putMaze(height, width, maze, visibility);
                mTurnsLeft--;

                if (mState == State.goToBack) {
                    mTurnsBackLeft--;
                }

                if (mState == State.goToControl && currentCell.equals("C")) {
                    mState = State.goToBack;
                }

                if (mState == State.goToBack && currentCell.equals("T")) {
                    //finish
                    throw new SuccessException();
                }

                Assert.assertTrue(mTurnsLeft > 0);
                Assert.assertTrue(mTurnsBackLeft > 0);

                return null;
            }
        }).when(printStream).println(Mockito.anyString());
        System.setOut(printStream);

        Labyrinth.main(null);
    }

    private void putMaze(int height, int width, String[] maze, boolean[][] visibility) {
        StringBuilder row = new StringBuilder(width);
        for (int y = 0; y < height; y++) {
            row.setLength(0);
            for (int x = 0; x < width; x++) {
                boolean isVisible = visibility[x][y];
                String symbol = isVisible ? maze[y].substring(x, x + 1) : "?";
                if (isVisible && symbol.equals("?")) {
                    Assert.fail("The maze is wrong");
                }
                row.append(symbol);
            }
            mOutQueue.add(row.toString());
        }
    }

    private void setVisibility(int height, int width, boolean[][] visibility) {
        for (int y = mKY - 2; y <= mKY + 2; y++) {
            if (y >= 0 && y < height) {
                for (int x = mKX - 2; x <= mKX + 2; x++) {
                    if (x >= 0 && x < width) {
                        visibility[x][y] = true;
                    }
                }
            }
        }
    }

    private enum State {
        goToControl, goToBack
    }

    private static class SuccessException extends Exception {

    }
}
