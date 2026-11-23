package com.example.car;
import com.example.car.services.CarService;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class CarApplication {
 public static void main(String[] args) { SpringApplication.run(CarApplication.class,args); }
 @Bean public RestTemplate restTemplate() {
  var factory=new SimpleClientHttpRequestFactory();
  factory.setConnectTimeout(5000);
  factory.setReadTimeout(5000);
  return new RestTemplate(factory);
 }
 @Bean CommandLineRunner demo(CarService service) { return args -> service.initialiserSiVide(); }
}
