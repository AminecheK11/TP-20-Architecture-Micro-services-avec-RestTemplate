package com.example.car;
import com.example.car.models.CarResponse;
import com.example.car.services.CarService;
import com.example.car.repositories.CarRepository;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "eureka.client.enabled=false",
 "spring.datasource.url=jdbc:mysql://localhost:3306/carservicedb_test?createDatabaseIfNotExist=true"
})
class CarApplicationTests {
 static final AtomicBoolean fail=new AtomicBoolean(false);
 static final CopyOnWriteArrayList<String> calls=new CopyOnWriteArrayList<>();
 static final HttpServer upstream=start();
 static HttpServer start() {
  try {
   var s=HttpServer.create(new InetSocketAddress("localhost",0),0);
   s.createContext("/SERVICE-CLIENT/api/client/",e -> {
    String path=e.getRequestURI().getPath(); calls.add(path);
    int id=Integer.parseInt(path.substring(path.lastIndexOf('/')+1));
    String[] names={"","Rabab SELIMANI","Amal RAMI","Samir SAFI"};
    String json=fail.get() ? "{}" : "{\"id\":"+id+",\"nom\":\""+names[id]+"\",\"age\":22}";
    byte[] body=json.getBytes(StandardCharsets.UTF_8);
    e.getResponseHeaders().set("Content-Type","application/json");
    e.sendResponseHeaders(fail.get()?503:200,body.length);
    try(var out=e.getResponseBody()) {out.write(body);}
   }); s.start(); return s;
  } catch(java.io.IOException ex) {throw new ExceptionInInitializerError(ex);}
 }
 @DynamicPropertySource static void endpoint(DynamicPropertyRegistry r) {
  r.add("client.gateway-url",()->"http://localhost:"+upstream.getAddress().getPort()+"/SERVICE-CLIENT/api/client");
 }
 @AfterAll static void stop(){upstream.stop(0);}
 @Autowired TestRestTemplate http;
 @Autowired CarService service;
 @Autowired CarRepository repository;
 @Autowired javax.sql.DataSource datasource;
 @Test void mysqlConnectionIsReal() throws Exception {
  try(var c=datasource.getConnection()) {
   assertThat(c.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
   assertThat(c.getCatalog()).isEqualTo("carservicedb_test");
  }
 }
 @Test void listEnrichesFourCarsThroughGatewayPath() {
  calls.clear();
  var r=http.getForEntity("/api/car",CarResponse[].class);
  assertThat(r.getStatusCode().value()).isEqualTo(200);
  assertThat(r.getBody()).hasSize(4);
  assertThat(r.getBody()).extracting(v->v.getClient().getNom())
   .containsExactly("Rabab SELIMANI","Amal RAMI","Samir SAFI","Amal RAMI");
  assertThat(calls).contains("/SERVICE-CLIENT/api/client/1","/SERVICE-CLIENT/api/client/2","/SERVICE-CLIENT/api/client/3");
 }
 @Test void detailContainsCarAndClient() {
  Long id=repository.findAll().get(0).getId();
  var r=http.getForEntity("/api/car/"+id,CarResponse.class);
  assertThat(r.getStatusCode().value()).isEqualTo(200);
  assertThat(r.getBody().getBrand()).isEqualTo("Ford");
  assertThat(r.getBody().getClient().getNom()).isEqualTo("Rabab SELIMANI");
 }
 @Test void missingCarReturns404() {
  assertThat(http.getForEntity("/api/car/9223372036854775807",String.class).getStatusCode().value()).isEqualTo(404);
 }
 @Test void unavailableClientDoesNotFailList() {
  fail.set(true);
  try {
   var r=http.getForEntity("/api/car",CarResponse[].class);
   assertThat(r.getStatusCode().value()).isEqualTo(200);
   assertThat(r.getBody()).hasSize(4).allSatisfy(v->assertThat(v.getClient()).isNull());
  } finally {fail.set(false);}
 }
 @Test void startupDoesNotDuplicateCars() {
  long count=repository.count();
  service.initialiserSiVide();service.initialiserSiVide();
  assertThat(repository.count()).isEqualTo(count);
 }
}
