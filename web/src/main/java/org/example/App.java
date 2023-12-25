package org.example;

import org.example.base.LogHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class App extends SpringBootServletInitializer {
    private static final String CLASS_NAME = App.class.getSimpleName();
    private static LogHelper logger = new LogHelper();

    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        } catch (Exception e) {
            logger.exceptionMsg(e, CLASS_NAME, " start failed!!!");
        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }
}
