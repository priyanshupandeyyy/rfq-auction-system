package com.rfq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RfqAuctionApplication {
    public static void main(String[] args) {
        SpringApplication.run(RfqAuctionApplication.class, args);
    }
}
