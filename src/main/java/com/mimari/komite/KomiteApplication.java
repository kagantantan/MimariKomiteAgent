package com.mimari.komite;

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {ChromaVectorStoreAutoConfiguration.class})
public class KomiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(KomiteApplication.class, args);
    }
}
