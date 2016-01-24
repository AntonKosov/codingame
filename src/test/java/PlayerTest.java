import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PlayerTest {

    @Test
    public void simplest() {
        startTest(3, 3,

                "1.2\n" +
                "...\n" +
                "..1\n",

                "0 0 2 0 1\n" +
                "2 0 2 2 1\n");
    }

    @Test
    public void simple() {
        startTest(3, 3,

                "1.3\n" +
                "...\n" +
                "123\n",

                "0 0 2 0 1\n" +
                "0 2 1 2 1\n" +
                "2 0 2 2 2\n" +
                "1 2 2 2 1\n");
    }

    @Test
    public void basic() {
        startTest(4, 3,

                "14.3\n" +
                "....\n" +
                ".4.4\n",

                "0 0 1 0 1\n" +
                "1 0 3 0 1\n" +
                "1 0 1 2 2\n" +
                "3 0 3 2 2\n" +
                "1 2 3 2 2\n");
    }

    @Test
    public void intermediate2() {
        startTest(7, 5,

                "2..2.1.\n" +
                ".3..5.3\n" +
                ".2.1...\n" +
                "2...2..\n" +
                ".1....2\n",

                "0 0 3 0 1\n" +
                "3 0 5 0 1\n" +
                "1 1 4 1 2\n" +
                "4 1 6 1 2\n" +
                "1 1 1 2 1\n" +
                "1 2 3 2 1\n" +
                "0 0 0 3 1\n" +
                "4 1 4 3 1\n" +
                "0 3 4 3 1\n" +
                "6 1 6 4 1\n" +
                "1 4 6 4 1\n");
    }

    @Test
    public void advanced() {
        startTest(8, 8,

                "3.4.6.2.\n" +
                ".1......\n" +
                "..2.5..2\n" +
                "1.......\n" +
                "..1.....\n" +
                ".3..52.3\n" +
                ".2.17..4\n" +
                ".4..51.2\n",

                "0 0 2 0 2\n" +
                "0 0 0 3 1\n" +
                "2 0 4 0 2\n" +
                "4 0 6 0 2\n" +
                "4 0 4 2 2\n" +
                "1 1 1 5 1\n" +
                "2 2 4 2 1\n" +
                "2 2 2 4 1\n" +
                "4 2 7 2 1\n" +
                "4 2 4 5 1\n" +
                "7 2 7 5 1\n" +
                "1 5 4 5 2\n" +
                "4 5 4 6 2\n" +
                "5 5 7 5 2\n" +
                "1 6 1 7 2\n" +
                "3 6 4 6 1\n" +
                "4 6 7 6 2\n" +
                "4 6 4 7 2\n" +
                "7 6 7 7 2\n" +
                "1 7 4 7 2\n" +
                "4 7 5 7 1\n");
    }

    @Test
    public void cg() {
        startTest(5, 14,

                "22221\n" +
                "2....\n" +
                "2....\n" +
                "2....\n" +
                "2....\n" +
                "22321\n" +
                ".....\n" +
                ".....\n" +
                "22321\n" +
                "2....\n" +
                "2....\n" +
                "2.131\n" +
                "2..2.\n" +
                "2222.\n",

                "0 0 1 0 1\n" +
                "0 0 0 1 1\n" +
                "1 0 2 0 1\n" +
                "2 0 3 0 1\n" +
                "3 0 4 0 1\n" +
                "0 1 0 2 1\n" +
                "0 2 0 3 1\n" +
                "0 3 0 4 1\n" +
                "0 4 0 5 1\n" +
                "0 5 1 5 1\n" +
                "1 5 2 5 1\n" +
                "2 5 3 5 1\n" +
                "2 5 2 8 1\n" +
                "3 5 4 5 1\n" +
                "0 8 1 8 1\n" +
                "0 8 0 9 1\n" +
                "1 8 2 8 1\n" +
                "2 8 3 8 1\n" +
                "3 8 4 8 1\n" +
                "0 9 0 10 1\n" +
                "0 10 0 11 1\n" +
                "0 11 0 12 1\n" +
                "2 11 3 11 1\n" +
                "3 11 4 11 1\n" +
                "3 11 3 12 1\n" +
                "0 12 0 13 1\n" +
                "3 12 3 13 1\n" +
                "0 13 1 13 1\n" +
                "1 13 2 13 1\n" +
                "2 13 3 13 1\n");
    }

//    @Test
//    public void expert() {
//        startTest(23, 23,
//
//                "3..2.2..1....3........4\n" +
//                ".2..1....2.6.........2.\n" +
//                "..3..6....3............\n" +
//                ".......2........1..3.3.\n" +
//                "..1.............3..3...\n" +
//                ".......3..3............\n" +
//                ".3...8.....8.........3.\n" +
//                "6.5.1...........1..3...\n" +
//                "............2..6.31..2.\n" +
//                "..4..4.................\n" +
//                "5..........7...7...3.3.\n" +
//                ".2..3..3..3............\n" +
//                "......2..2...1.6...3...\n" +
//                "....2..................\n" +
//                ".4....5...3............\n" +
//                ".................2.3...\n" +
//                ".......3.3..2.44....1..\n" +
//                "3...1.3.2.3............\n" +
//                ".2.....3...6.........5.\n" +
//                "................1......\n" +
//                ".1.......3.6.2...2...4.\n" +
//                "5...............3.....3\n" +
//                "4...................4.2\n",
//
//                "0 0 1 0 1\n" +
//                "2 13 3 13 1\n");
//    }

    private void startTest(int width, int height, String in, String answer) {
        String data = width + "\n" + height + "\n" + in;
        System.setIn(new ByteArrayInputStream(data.getBytes()));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        System.setOut(printStream);

        Player.main(null);

        System.out.flush();
        String out = stream.toString();

        Assert.assertEquals(answer, out);
    }
}
