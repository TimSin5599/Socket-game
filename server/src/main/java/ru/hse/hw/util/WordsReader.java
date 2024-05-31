package ru.hse.hw.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class WordsReader {
    private static final String RESOURCE_NAME = "/russian_nouns.txt";
    private static final Random RANDOM = new Random();

    private WordsReader() {}

    public static String[] readWords() {
        ArrayList<String> words = new ArrayList<>();
        InputStream inputStream = WordsReader.class.getResourceAsStream(RESOURCE_NAME);

        if (inputStream != null) {
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    words.add(scanner.nextLine().trim());
                }
            }
        }
        return words.toArray(new String[0]);
    }
}
