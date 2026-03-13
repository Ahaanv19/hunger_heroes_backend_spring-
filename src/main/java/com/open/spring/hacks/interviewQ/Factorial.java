package com.open.spring.hacks.interviewQ;

/**
 * Find factorial, the product of all integers up to a given number.
 * College Board will typically ask you to perform a recursive calculation 
 */
public class Factorial {
    /** Method 1: Recursive Method (Classic Method)
     * The base case is when n = 0, the factorial is 1
     * The recursive case is when n > 0, the factorial is n * factorial(n-1)
     */
    public static int factorial1(int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * factorial1(n - 1);
        }
    }

    /** Method 2: Iterative Method
     * The result is initialized to 1
     * The for loop iterates from 1 to n
     * The result is multiplied by the current value of i
     */
    public static int factorial2(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Method 3: Stream Method
     * The .rangeClosed() method is used to create a stream of integers from 1 to n
     * The .reduce() method is used to multiply all the integers in the stream
     * The .orElse() method is used to return 1 if the stream is empty, ie n = 0
     */
    public static int factorial3(int n) {
        return java.util.stream.IntStream
            .rangeClosed(1, n)
            .reduce((a, b) -> a * b)
            .orElse(1);
    }

    /**
     * Main method to test the factorial methods
     */
    public static void main(String[] args) {
        System.out.println("Recursive factorial: " + factorial1(5));
        System.out.println("Iterative factorial: " + factorial2(5));
        System.out.println("Stream factorial: " + factorial3(5));
    }
    
}
