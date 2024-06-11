package ru.hse.hw.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The WordsReader class which is used to read words from the file "russian_nouns.txt"
 */
public class WordsReader {
    /**
     * File name
     */
    private static final String RESOURCE_NAME = "/russian_nouns.txt";

    /**
     * private builder
     */
    private WordsReader() {}

    /**
     * The function for reading words from a file
     * @return the words in the array
     */
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
