package com.example.client;
import com.example.client.services.ClientService;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ClientServiceApplication.class, args); }
    // Le starter Eureka active automatiquement DiscoveryClient.
    @Bean CommandLineRunner demo(ClientService service) { return args -> service.initialiserSiVide(); }
}
