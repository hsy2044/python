package org.example;

import org.example.base.LogHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class App extends SpringBootServletInitializer {
    private static final String CLASS_NAME = App.class.getSimpleName();
    private static LogHelper logger = new LogHelper();

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://172.16.40.30:1433;databaseName=DB2023;encrypt=true;trustServerCertificate=true";
        String user = "db2023user";
        String password = "db2023";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("連接成功");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("連接失敗");
        }
//        try {
//            SpringApplication.run(App.class, args);
//        } catch (Exception e) {
//            logger.exceptionMsg(e, CLASS_NAME, " start failed!!!");
//        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }
}
