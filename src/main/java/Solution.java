import java.util.*;

class Solution {

    private static final HashMap<String, String> LETTERS = new HashMap<>();

    static {
        LETTERS.put("A", ".-");
        LETTERS.put("B", "-...");
        LETTERS.put("C", "-.-.");
        LETTERS.put("D", "-..");
        LETTERS.put("E", ".");
        LETTERS.put("F", "..-.");
        LETTERS.put("G", "--.");
        LETTERS.put("H", "....");
        LETTERS.put("I", "..");
        LETTERS.put("J", ".---");
        LETTERS.put("K", "-.-");
        LETTERS.put("L", ".-..");
        LETTERS.put("M", "--");
        LETTERS.put("N", "-.");
        LETTERS.put("O", "---");
        LETTERS.put("P", ".--.");
        LETTERS.put("Q", "--.-");
        LETTERS.put("R", ".-.");
        LETTERS.put("S", "...");
        LETTERS.put("T", "-");
        LETTERS.put("U", "..-");
        LETTERS.put("V", "...-");
        LETTERS.put("W", ".--");
        LETTERS.put("X", "-..-");
        LETTERS.put("Y", "-.--");
        LETTERS.put("Z", "--..");
    }

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final String message = in.next();
        log(message);
        final int sizeOfDictionary = in.nextInt();
        final String[] dictionary = new String[sizeOfDictionary];
        for (int i = 0; i < sizeOfDictionary; i++) {
            final String word = in.next();
            log(word);
            final String morse = wordToMorse(word);
            dictionary[i] = morse;
        }
        final int[] possible = new int[message.length() + 1];
        possible[possible.length - 1] = 1;
        for (int i = possible.length - 2; i > 0; i--) {
            if (possible[i + 1] == 0) {
                continue;
            }
            final String sentences = message.substring(0, i + 1);
            for (String word : dictionary) {
                if (sentences.endsWith(word)) {
                    possible[i - word.length() + 1]++;
                }
            }
        }

        System.out.println(possible[0]);
    }

    private static String wordToMorse(String word) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            final String letter = word.substring(i, i + 1);
            sb.append(LETTERS.get(letter));
        }

        return sb.toString();
    }

    private static void log(String message) {
        System.err.println(message);
    }
}