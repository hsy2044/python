package org.example;

import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
//        String a = "Hello";
//        String b ="World!";
//
//        if(a.equals("Hello") && b.equals("World!")){
//            System.out.println(a+" "+b);
//        }
//        else {
//            System.out.println("err");
//        }

//        for(int i=1;i<=9;i++){
//            for(int j=1;j<=9;j++){
//                System.out.print(i+"*"+j+"="+i*j+" ");
//            }
//            System.out.println("\n");

        Scanner scanner = new Scanner(System.in);

        //for (int a=1;a<=3;a++) {
            System.out.print("請輸入：");
            String EJ = scanner.nextLine();
            int m,n;
            System.out.print("m：");
            m = scanner.nextInt();
            System.out.print("n：");
            n = scanner.nextInt();

            System.out.print("輸出結果:");
            //String pigLatinWord = convertToPigLatin(EJ);
            //String convert = convert(EJ);
            //String verb = verb(EJ);
            String print = print(EJ,m,n);


        //}
    }

    //第一題
    private static String convertToPigLatin(String word) {
        if (word.length() == 0) {
            return word;  // 如果字串為空，不進行變換
        }

        char firstChar = word.charAt(0);
        String restOfWord = word.substring(1);

        // 將頭字母搬到尾字母之後，同時尾巴補上 "ay"
        String pigLatinWord = restOfWord + firstChar + "ay";

        System.out.println("Output:" + pigLatinWord);
        return pigLatinWord;
    }

    //第二題
    private static String convert(String word) {

        char convertArray[]=word.toCharArray();

        int start = 0;
        int end = convertArray.length - 1;

        while(start < end){
            char temp = convertArray[start];
            convertArray[start] = convertArray[end];
            convertArray[end] = temp;

            // 移動指針
            start++;
            end--;
        }
        System.out.println(convertArray);
        return new String(convertArray);
    }


    //第三題
    private static String verb(String word) {
        char convertArray[]=word.toCharArray();

        int start = 0;
        int end = convertArray.length - 1;
        String one="s,z,x";
        String two="ch,sh";
        String AA = String.valueOf(convertArray[end-1]);


        if (one.indexOf(String.valueOf(convertArray[end])) >= 0){
            String a = word.substring(0,word.length());
            System.out.println(a+"es");
        }else if(two.indexOf(String.valueOf(convertArray[end-1])+String.valueOf(convertArray[end])) >= 0){
            String a = word.substring(0,word.length());
            System.out.println(a+"es");
        } else if (String.valueOf(convertArray[end]).equals("y") && (AA.equals("a") || AA.equals("e") || AA.equals("i") || AA.equals("o") || AA.equals("u"))){
            String a = word.substring(0,word.length()-1);
            System.out.println(a+"ies");
        }else
            System.out.println(word+"s");


        return new String(convertArray);
    }

    ///第四題
    private static String print(String word,int m, int n) {
        char  Arry[]= word.toCharArray();
        int legth= Arry.length;


        if(m > n || n > legth){
            System.out.println("err");
        }else {
            for (m=m;m<n;m++){
                System.out.print(Arry[m]);
            }
        }


        return word;
    }
}


