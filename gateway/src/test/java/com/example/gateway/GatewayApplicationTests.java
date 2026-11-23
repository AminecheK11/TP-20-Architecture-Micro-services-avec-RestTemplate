package com.example.gateway;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "eureka.client.enabled=false")
class GatewayApplicationTests {
 static final AtomicReference<String> received=new AtomicReference<>();
 static final HttpServer upstream=start();
 static HttpServer start() {
  try {
   var s=HttpServer.create(new InetSocketAddress("localhost",0),0);
   s.createContext("/",e -> {
    received.set(e.getRequestURI().toString());
    byte[] body=("backend:"+e.getRequestURI()).getBytes(StandardCharsets.UTF_8);
    e.getResponseHeaders().set("Content-Type","text/plain");
    e.sendResponseHeaders(e.getRequestURI().getPath().endsWith("/999") ? 404 : 200,body.length);
    try(var out=e.getResponseBody()) {out.write(body);}
   });
   s.start(); return s;
  } catch(java.io.IOException ex) {throw new ExceptionInInitializerError(ex);}
 }
 @DynamicPropertySource static void discovery(DynamicPropertyRegistry r) {
  r.add("spring.cloud.discovery.client.simple.instances.SERVICE-CLIENT[0].uri",
   () -> "http://localhost:"+upstream.getAddress().getPort());
 }
 @AfterAll static void stop() {upstream.stop(0);}
 @Autowired WebTestClient http;
 @Test void routesCollectionAndStripsServicePrefix() {
  http.get().uri("/SERVICE-CLIENT/api/client").exchange().expectStatus().isOk()
   .expectBody(String.class).isEqualTo("backend:/api/client");
 }
 @Test void routesIndividualClientAndQuery() {
  http.get().uri("/SERVICE-CLIENT/api/client/1?detail=true").exchange().expectStatus().isOk()
   .expectBody(String.class).isEqualTo("backend:/api/client/1?detail=true");
 }
 @Test void preservesBackend404() {
  http.get().uri("/SERVICE-CLIENT/api/client/999").exchange().expectStatus().isNotFound();
 }
 @Test void serviceNameIsCaseSensitive() {
  received.set(null);
  http.get().uri("/service-client/api/client").exchange().expectStatus().isNotFound();
  org.assertj.core.api.Assertions.assertThat(received.get()).isNull();
 }
 @Test void actuatorShowsDiscoveredLoadBalancedRoute() {
  http.get().uri("/actuator/gateway/routes").exchange().expectStatus().isOk()
   .expectBody().jsonPath("$[0].uri").isEqualTo("lb://SERVICE-CLIENT");
 }
 @Test void healthIsUp() {
  http.get().uri("/actuator/health").exchange().expectStatus().isOk()
   .expectBody().jsonPath("$.status").isEqualTo("UP");
 }
}
