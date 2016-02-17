package veryhard;

import java.util.*;

class TheResistance {

    private static final boolean SHOW_LOGS = false;

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
        final int sizeOfDictionary = in.nextInt();
        final ArrayList<String> dictionary = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sizeOfDictionary; i++) {
            final String word = in.next();
            sb.setLength(0);
            for (int j = 0; j < word.length(); j++) {
                final String letter = word.substring(j, j + 1);
                sb.append(LETTERS.get(letter));
            }
            dictionary.add(sb.toString());
        }
        final long[] possible = new long[message.length() + 1];
        possible[possible.length - 1] = 1;
        for (int i = possible.length - 2; i >= 0; i--) {
            final long countOfVariants = possible[i + 1];
            if (countOfVariants == 0) {
                continue;
            }
            final String sentence = message.substring(0, i + 1);
            for (String word : dictionary) {
                if (sentence.endsWith(word)) {
                    final int index = i - word.length() + 1;
                    possible[index] += countOfVariants;
                    log(index + "=" + possible[index]);
                }
            }
        }

        System.out.println(possible[0]);
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }
}