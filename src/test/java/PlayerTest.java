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
                "2 0 2 2 2\n" +
                "0 2 1 2 1\n" +
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
                "0 0 0 3 1\n" +
                "3 0 5 0 1\n" +
                "1 1 4 1 2\n" +
                "1 1 1 2 1\n" +
                "4 1 6 1 2\n" +
                "4 1 4 3 1\n" +
                "6 1 6 4 1\n" +
                "1 2 3 2 1\n" +
                "0 3 4 3 1\n" +
                "1 4 6 4 1\n");
    }

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
