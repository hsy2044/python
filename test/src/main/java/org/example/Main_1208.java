package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class Main_1208 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

//        int array[] = new int[20];
        ArrayList<Integer> array = new ArrayList<Integer>();
        int even = 0, odd = 0;


        for (int a = 1; a <= 15; a++) {
            System.out.print("Integer" + a + ":");
            int num = scanner.nextInt();
            array.add(num);
        }

        int evenarray[] = new int[array.size()];
        int oddarray[] = new int[array.size()];
        int num0 = 0;
        int num1 = 0;
        int num2 = 0;

        for (int b = 0; b < array.size(); b++) {
//            System.out.print(array[b] + " ");

            if (array.get(b) == 0) {
                evenarray[num0] = array.get(b);
                num1 += 1;
                even++;
            } else if (array.get(b) % 2 == 0) {

                evenarray[num1] = array.get(b);
                num1 += 1;
                even++;
            } else if (array.get(b) % 2 != 0) {

                oddarray[num2] = array.get(b);
                num2 += 1;
                odd++;
            }
        }
        System.out.println(array);



        bubbleSort(evenarray);
        bubbleSort(oddarray);
        for (int c = 0; c < even; c++) {
            System.out.print(evenarray[c] + " ");
        }
        for (int d = 0; d < odd; d++) {
            System.out.print(oddarray[d] + " ");
        }

    }

    private static void bubbleSort(int[] arr) {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                // 如果相鄰的元素順序不正確，則交換它們
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
//                    swap(arr, j, j + 1);
                }
            }
        }
    }

    // 交換陣列中的兩個元素
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}