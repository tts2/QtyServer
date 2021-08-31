package com.javacgo.remote.qt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KkdeskserverApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(KkdeskserverApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
