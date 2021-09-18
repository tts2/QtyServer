package com.javacgo.remote.qty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QtyDeskServerApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(QtyDeskServerApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
