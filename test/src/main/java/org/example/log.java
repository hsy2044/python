package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class log {
    public static void main(String[] args) throws Exception {

        try {
            // 指定txt檔案的路徑
            String txtFilePath = "C:\\Users\\User\\Desktop\\LOG\\fep-server-atm-ATMService-2023-02-23-0.log";

            // 使用BufferedReader來讀取檔案
            BufferedReader bufferedReader = new BufferedReader(new FileReader(txtFilePath));
            Scanner scanner = new Scanner(System.in);
            System.out.println("FSCODE:");
            String num = scanner.nextLine();
            // 讀取每一行的內容
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                String[] k = line.split("\\|");

                if(k[4].trim().contains(num))
                {
                    if(k[9].trim().equals("AAIn")||k[9].trim().equals("AdapterIn")||k[9].trim().equals("AdapterOut")||k[9].trim().equals("AAOut"))
                    {
                        System.out.println(k[0]+k[2]+" "+k[4]+k[6]+k[9]+k[18]);
                    }
                }
            }

            // 關閉資源
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
