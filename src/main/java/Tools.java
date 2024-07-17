public class Tools {

    /**
     * Counts the number of times a keyword is repeated in a string
     *
     * @param userInput The string to be analyzed
     * @param keyword   The keyword to be counted
     * @return The number of times the keyword is repeated in the string
     */
    public static int numberOfTimesKeywordIsRepeated(String userInput, String keyword) {
        String[] separatedString = userInput.split("\\W+");
        int timesKeywordIsRepeated = 0;
        for (int i = 0; i < separatedString.length; i++) {
            if (keyword.equalsIgnoreCase(separatedString[i])) {
                timesKeywordIsRepeated++;
            }
        }
        return timesKeywordIsRepeated;
    }
}