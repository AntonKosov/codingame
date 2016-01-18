import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PlayerTest {
    @Test
    public void simpleTest() {
        String data = "2\n" +
                "2\n" +
                "42\n" +
                "31\n";
        System.setIn(new ByteArrayInputStream(data.getBytes()));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        System.setOut(printStream);

        Player.main(null);

        System.out.flush();
        String answer = stream.toString();

        Assert.assertEquals("", answer);
    }
}
