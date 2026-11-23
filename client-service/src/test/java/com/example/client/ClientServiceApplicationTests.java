package com.example.client;
import com.example.client.entities.Client;
import com.example.client.repositories.ClientRepository;
import com.example.client.services.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
 "eureka.client.enabled=false",
 "spring.datasource.url=jdbc:mysql://localhost:3306/clientservicedb_test?createDatabaseIfNotExist=true"
})
class ClientServiceApplicationTests {
 @Autowired TestRestTemplate http;
 @Autowired ClientRepository repository;
 @Autowired ClientService service;
 @Autowired javax.sql.DataSource datasource;

 @Test void usesMysqlTestDatabase() throws Exception {
   try(var c=datasource.getConnection()) {
     assertThat(c.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
     assertThat(c.getCatalog()).isEqualTo("clientservicedb_test");
   }
 }
 @Test void listContainsDemoData() {
   var r=http.getForEntity("/api/client",Client[].class);
   assertThat(r.getStatusCode().value()).isEqualTo(200);
   assertThat(r.getBody()).extracting(Client::getNom).contains("Rabab SELIMANI","Amal RAMI","Samir SAFI");
 }
 @Test void findExistingClient() {
   var first=service.findAll().get(0);
   var r=http.getForEntity("/api/client/"+first.getId(),Client.class);
   assertThat(r.getStatusCode().value()).isEqualTo(200);
   assertThat(r.getBody().getNom()).isEqualTo(first.getNom());
 }
 @Test void missingReturns404() {
   assertThat(http.getForEntity("/api/client/9223372036854775807",String.class).getStatusCode().value()).isEqualTo(404);
 }
 @Test void postCreatesAndPersists() {
   var input=new Client("TEST-CREATION",30f);
   input.setId(service.findAll().get(0).getId());
   var r=http.postForEntity("/api/client",input,Client.class);
   assertThat(r.getStatusCode().value()).isEqualTo(201);
   Long id=r.getBody().getId();
   try {
     assertThat(id).isNotEqualTo(input.getId());
     assertThat(repository.findById(id).orElseThrow().getNom()).isEqualTo("TEST-CREATION");
   } finally { repository.deleteById(id); }
 }
 @Test void invalidReturns400() {
   assertThat(http.postForEntity("/api/client",new Client("",-1f),String.class).getStatusCode().value()).isEqualTo(400);
 }
 @Test void initializationIsIdempotent() {
   long before=repository.count();
   service.initialiserSiVide(); service.initialiserSiVide();
   assertThat(repository.count()).isEqualTo(before);
 }
}
