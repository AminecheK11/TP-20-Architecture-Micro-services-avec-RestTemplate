package com.example.car.models;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Client {
 private Long id;
 private String nom;
 private Float age;
}
