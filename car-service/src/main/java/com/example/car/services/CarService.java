package com.example.car.services;
import com.example.car.entities.Car;
import com.example.car.models.*;
import com.example.car.repositories.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.web.client.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.*;
import java.util.List;

@Service
public class CarService {
 private static final Logger LOGGER=LoggerFactory.getLogger(CarService.class);
 private final CarRepository repository;
 private final RestTemplate restTemplate;
 private final String clientUrl;
 public CarService(CarRepository repository, RestTemplate restTemplate,
     @Value("${client.gateway-url}") String clientUrl) {
  this.repository=repository; this.restTemplate=restTemplate; this.clientUrl=clientUrl;
 }
 public List<CarResponse> findAll() {
  return repository.findAll(Sort.by("id")).stream().map(this::mapToCarResponse).toList();
 }
 public CarResponse findById(Long id) {
  return mapToCarResponse(repository.findById(id).orElseThrow(() ->
      new ResponseStatusException(HttpStatus.NOT_FOUND,"Voiture introuvable : "+id)));
 }
 public CarResponse mapToCarResponse(Car car) {
  Client client=null;
  try {
   client=restTemplate.getForObject(clientUrl+"/{id}",Client.class,car.getClient_id());
  } catch(RestClientException ex) {
   LOGGER.warn("Client {} indisponible via Gateway pour voiture {} : {}",car.getClient_id(),car.getId(),ex.getMessage());
  }
  return CarResponse.builder().id(car.getId()).brand(car.getBrand()).model(car.getModel())
      .matricule(car.getMatricule()).client(client).build();
 }
 @Transactional public void initialiserSiVide() {
  if(repository.count()==0) repository.saveAll(List.of(
      new Car(null,"Ford","2022","12 A 2345",1L),
      new Car(null,"Renault","2000","14 R 5245",2L),
      new Car(null,"Toyota","1990","34 T 6755",3L),
      new Car(null,"Ford","2021","44 R 6756",2L)));
 }
}
