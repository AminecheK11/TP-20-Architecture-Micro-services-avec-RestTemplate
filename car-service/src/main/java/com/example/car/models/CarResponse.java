package com.example.car.models;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarResponse {
 private Long id;
 private String brand;
 private String model;
 private String matricule;
 private Client client;
}
