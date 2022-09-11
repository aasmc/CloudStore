package ru.aasmc.cloudstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
public class CloudStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudStoreApplication.class, args);
    }

}
