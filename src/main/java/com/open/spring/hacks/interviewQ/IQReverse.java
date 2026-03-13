package com.open.spring.hacks.interviewQ;

/**
 * Reverse a string
 * College Board will typically ask you to reverse a string, number or something similar
 * This is a common interview question that tests your understanding of simple alorithms
 * Coding different methods explores the different coding styles within Java
 */
public class IQReverse {
    /** Method 1: While loop using string concatenation (Classic Method)
     * CharAt() allows you to access the character at a specific index
     * length() allows you to find the length of the string
     * --- How it works ---
     * Initial call: reverseString1("Hello, World!")
     * Iteration 1: reversed = "" + "!" = "!"
     * Iteration 2: reversed = "!" + "d" = "!d"
     * Iteration 3: reversed = "!d" + "l" = "!dl"
     * ...
     * Last Iteration: reversed = "!dlroW ,olle" + "H" = "!dlroW ,olleH"
     * Return reversed: "!dlroW ,olleH"
     */
    public static String reverseString1(String original) {
        String reversed = "";
        int i = original.length();
        while (i > 0) {
            reversed = reversed + original.charAt(i - 1);
            i--;
        }
        return reversed;
    }

    /** Method 2: For each loop using string concatenation (College Board Method)
     * The foreach loop is used to iterate through the string
     * The .toCharArray() method is used to convert the string to a character array
     * The concatenation operator is used to add characters to the reversed string;
     * --- How it works ---
     * Initial call: reverseString2("Hello, World!")
     * Iteration 1: reversed = "H" + "" = "H"
     * Iteration 2: reversed = "e" + "H" = "eH"
     * Iteration 3: reversed = "l" + "eH" = "leH"
     * ...
     * Last Iteration: reversed = "!" + "dlroW ,olleH" = "!dlroW ,olleH"
     * Return reversed: "!dlroW ,olleH"
     */
    public static String reverseString2(String original) {
        String reversed = "";
        for (char c : original.toCharArray()) { // "Hello, World!" to ['H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!'] 
            reversed = c + reversed; // Add character in front of current string, thus reversing the string
        }
        return reversed;
    }

    /** Method 3: Recursion Method (Recursion Method)
     * The base case is when the string is empty, the reversed string is empty
     * The recursive case is when the string is not empty, the first character is added to the end of the reversed string
     * The .isEmpty() method is used to check if the string is empty
     * The substring() method is used to remove the first character from the string
     * --- How it works ---
     * Initial call: reverseString3("Hello, World!")
     * Recursive call: reverseString3("ello, World!") + "H"
     * Recursive call: reverseString3("llo, World!") + "e" + "H"
     * ...
     * Last Pass: reverseString3("") + "d" + "l" + "r" + "o" + "W" + "," + "o" + "l" + "l" + "e" + "H"
     * Recursion Unwinding: "" + "d" + "l" + "r" + "o" + "W" + "," + "o" + "l" + "l" + "e" + "H"
     * Return result: "dlroW,olleH"
     */
    public static String reverseString3(String original) {
        if (original.isEmpty()) {
            return original;
        } else {
            // Trims 0 index on each recursion and creates a stack of concatenations at unwinding
            return reverseString3(original.substring(1)) + original.charAt(0);
        }
    }

    /** Method 4: For loop using StringBuilder (Modern Method)
     * StringBuilder is a mutable sequence of characters; this is considered more efficient than using string concatenation
     * the .append() method is used to add characters to the mutable sequence
     * the .toString() method is used to convert the sequence back to a string
     * Conventional for loop is used to iterate through the string;
     * .length() allows you to find the length of the string
     * --- How it works ---
     * Initial call: reverseString4("Hello, World!")
     * reversed = ""
     * reversed = "" + "!" = "!"
     * reversed = "!" + "d" = "!d"
     * reversed = "!d" + "l" = "!dl"
     * ...
     * Last Iteration: reversed = "!dlroW,olle" + "H" = "!dlroW,olleH"
     * reversed.toString() = "!dlroW,olleH"
     * Return reversed: "!dlroW,olleH"
     */
    public static String reverseString4(String original) {
        StringBuilder reversed = new StringBuilder();
        for (int i = original.length() - 1; i >= 0; i--) {
            reversed.append(original.charAt(i));
        }
        return reversed.toString();
    }

    /** Method 5: Functional Programming using Streams (Modern Method)
     * The .chars() method converts the string to an IntStream, which is a stream of ASCII values of the characters     
     * The .forEach() method is used to iterate through the stream
     * The .insert() method is used to insert characters at the beginning of the StringBuilder
     * The (char) cast is used to convert the integer ASCII value to a character
     * The .toString() method is used to return the StringBuilder as a string
     * --- How it works ---
     * Initial call: reverseString5("Hello, World!")
     * reversed = ""
     * reversed = "H" -> "H"
     * reversed = "e" -> "eH"
     * reversed = "l" -> "leH"
     * ...
     * Last Iteration: reversed = "!" -> "!dlroW,olleH"
     * reversed.toString() = "!dlroW,olleH"
     * Return reversed: "!dlroW,olleH"
     */
    public static String reverseString5(String original) {
        StringBuilder reversed = new StringBuilder();
        original.chars().forEach(c -> reversed.insert(0, (char) c));
        return reversed.toString();
    }

    /** Number Reversei 1: Using While Loop (Classic and College Board Method)
     * The % operator is used to get the last digit of the number
     * The / operator is used to remove the last digit of the number
     * The * operator is used to shift the digits to the left
     * The + operator is used to add the last digit to the reversed number
     * --- How it works ---
     * Initial call: reverseNumber(12345)
     * reversed = 0 * 10 + 5 = 5
     * reversed = 5 * 10 + 4 = 54
     * reversed = 54 * 10 + 3 = 543
     * reversed = 543 * 10 + 2 = 5432
     * reversed = 5432 * 10 + 1 = 54321
     * Return reversed: 54321
     */
    public static int reverseNumber1(int number) {
        int reversed = 0;
        while (number != 0) {
            reversed = reversed * 10 + number % 10;
            number = number / 10;
        }
        return reversed;
    }

    /** Number Reverse 2: Using Recursion (Recursion Method)
     * The base case is when the number is 0, the reversed number is 0
     * The recursive case is when the number is not 0, the last digit is added to the reversed number
     * The recursive case is when the number is not 0, the last digit is added to the reversed number
     * The recursive case is when the number is not 0, the last digit is added to the reversed number
     * --- How it works ---
     *  
     * Initial call: reverseNumber2(12345, 0)
     * Recursive call: reverseNumber2(1234, 5)
     * Recursive call: reverseNumber2(123, 54)
     * Recursive call: reverseNumber2(12, 543)
     * Recursive call: reverseNumber2(1, 5432)
     * Recursive call: reverseNumber2(0, 54321)
     * Return result: 54321
     */
    public static int reverseNumber2(int number, int reversed) {
        if (number == 0) {
            return reversed;
        } else {
            return reverseNumber2(number / 10, reversed * 10 + number % 10);
        }
    }

    /** Number Reverse 3: Converting to String (Using Helper Method)
     * Use the reverseString methods to reverse the string representation of the number
     * Convert the reversed string back to an integer
     * --- How it works ---
     * Initial call: reverseNumber3(12345)
     * reversed = reverseString1("12345") = "54321"
     * Return Integer.parseInt("54321") = 54321
     * Return reversed: 54321
     */
    public static int reverseNumber3(int number) {
        return Integer.parseInt(reverseString1(Integer.toString(number)));
    }

    /** Number Reverse 4: Using Functional Programming (Modern Method)
     * Use modulo math inside of a stream to reverse the number
     * --- How it works ---
     * Initial state: a = 0
     * First .reduce: a = 0, b = 5 -> 0 * 10 + 5 = 5
     * Second .reduce: a = 5, b = 4 -> 5 * 10 + 4 = 54
     * Third .reduce: a = 54, b = 3 -> 54 * 10 + 3 = 543
     * Fourth .reduce: a = 543, b = 2 -> 543 * 10 + 2 = 5432
     * Last .reduce: a = 5432, b = 1 -> 5432 * 10 + 1 = 54321
     * Return .reduce: 54321
     */
    public static int reverseNumber4(int number) {
        // observe "Method Chaining" for interate, map, and reduce until n = 0
        return java.util.stream.IntStream
                .iterate(number, n -> n != 0, n -> n / 10) // Iterate and divide by 10 until n = 0
                .map(n -> n % 10) // Maps each iteration to the last digit, one's place
                .reduce(0, (a, b) -> a * 10 + b); // Reduce the stream to a single integer
                // a: accumulated value, starts with 0 increasing by each a * 10 + b calculation
                // b: current digit in the stream from n % 10
                // returns: the accumulated value "a" as the reversed number at end of stream
    }

    // Main method to test the reverseString methods
    public static void main(String[] args) {
        String original = "Hello, World!";
        System.out.println("Original: \t\t\t\t" + original);

        System.out.println("Reversed using While: \t\t\t" + reverseString1(original));
        System.out.println("Reversed using For each: \t\t" + reverseString2(original));
        System.out.println("Reversed using Recursion: \t\t" + reverseString3(original));
        System.out.println("Reversed with StringBuilder: \t\t" + reverseString4(original));
        System.out.println("Reversed with Functional Programming: \t" + reverseString5(original));

        System.out.println();

        int number = 12345;
        System.out.println("Original Number: \t\t\t\t" + number);
        System.out.println("Reversed Number using While: \t\t\t" + reverseNumber1(number));
        System.out.println("Reversed Number using Recursion: \t\t" + reverseNumber2(number, 0));
        System.out.println("Reversed Number converting to String: \t\t" + reverseNumber3(number));
        System.out.println("Reversed Number with Functional Programming: \t" + reverseNumber4(number));
    }
}