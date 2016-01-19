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
