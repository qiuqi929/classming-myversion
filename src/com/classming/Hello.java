package com.classming;

public class Hello {
    public static void main(String[] args) {
        System.out.println("hello");
        int sum = 0;
        for (int i = 0; i < 100; i ++) {
            sum += i;
        }
        if (sum % 2 != 0) {
            System.out.println("nonono");
        }
        print("hello1");
        test();
        System.out.println(sum);
    }

    public static void test() {
        int sum = 0;
        for (int i = 0; i < 100; i ++) {
            sum += i;
        }
    }

    public static void print(String target) {
        System.out.println(target);
    }
}
