package org.example;

import java.util.ArrayList;
import java.util.Scanner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;


public class Main_1208V2 {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("請輸入數組");
            String line = scanner.nextLine();
            String[] t = line.split(",");
            ArrayList<Integer> array = new ArrayList<>();
            for(int z= 0;z<t.length;z++){
                array.add(Integer.valueOf(t[z]));
            }

//   int[] array = {51,-4,0,24,-13,-6,0,38,-27,-48,3,17,8,0,21};

            ArrayList<Integer> zarray = new ArrayList<>();
            ArrayList<Integer> tarray = new ArrayList<>();
            ArrayList<Integer> oarray = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                if(array.get(i) ==0){
                    zarray.add(array.get(i));
                }else if(array.get(i) %2 == 0){
                    tarray.add(array.get(i));
                }else{
                    oarray.add(array.get(i));
                }
            }

//   for (int i = 0; i < array.length; i++) {
//    if(array[i] ==0){
//     zarray.add(array[i]);
//    }else if(array[i] %2 == 0){
//     tarray.add(array[i]);
//    }else{
//     oarray.add(array[i]);
//    }
//   }
            //小到大
            tarray =bubbleSort(tarray);
            //大到小
            oarray =bubbleSortDescending(oarray);

            ArrayList<Integer> mergedArray =  new ArrayList<>();
            mergedArray.addAll(zarray);
            mergedArray.addAll(tarray);
            mergedArray.addAll(oarray);

            for (int value : mergedArray) {
                System.out.print(value + " ");
            }
            System.out.println();
        }catch (Exception e){
            System.out.println(e);
        }
    }
    static ArrayList<Integer> bubbleSortDescending(ArrayList<Integer> arr) {
        int n = arr.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // 如果当前元素小于下一个元素，则交换它们（实现大到小排序）
                if (arr.get(j) < arr.get(j + 1)) {
                    // 使用 arr.set 进行元素交换
                    int temp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, temp);
                }
            }
        }
        return arr;
    }
    static ArrayList<Integer> bubbleSort(ArrayList<Integer> arr) {
        int n = arr.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // 如果当前元素大于下一个元素，则交换它们
                if (arr.get(j) > arr.get(j + 1)) {
                    int temp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, temp);
                }
            }
        }
        return arr;
    }
}