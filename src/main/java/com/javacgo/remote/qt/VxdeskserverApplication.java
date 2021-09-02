package com.javacgo.remote.qt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VxdeskserverApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(VxdeskserverApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
