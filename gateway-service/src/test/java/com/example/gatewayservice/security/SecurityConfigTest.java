package com.example.gatewayservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies the security rules applied by {@link SecurityConfig}:
 * <ul>
 *   <li>{@code /actuator/**} is publicly accessible (no token required).</li>
 *   <li>All other routes require a valid HS256 JWT — unauthenticated requests receive 401.</li>
 *   <li>Requests with a valid token are not rejected by the security layer (the gateway may
 *       still return 502/503 if the downstream service is not running, which is acceptable
 *       in this context).</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityConfigTest {

  // Default PoC secret — matches the value in gateway-service/src/main/resources/application.yml
  private static final String JWT_SECRET_B64 =
      "bXNhLXBvYy1zZWNyZXQta2V5LWF0LWxlYXN0LTI1Ni1iaXRz";

  @Autowired private WebTestClient webTestClient;

  @Test
  void actuatorHealthIsAccessibleWithoutToken() {
    webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
  }

  @Test
  void protectedRouteReturns401WithoutToken() {
    webTestClient.get().uri("/v1/accounts").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void protectedCustomersRouteReturns401WithoutToken() {
    webTestClient.get().uri("/v1/customers").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void protectedRouteWithValidTokenPassesSecurityLayer() throws Exception {
    webTestClient
        .get()
        .uri("/v1/accounts")
        .header("Authorization", "Bearer " + validToken())
        .exchange()
        .expectStatus()
        .value(status -> assertThat(status).isNotIn(401, 403));
  }

  private String validToken() throws Exception {
    byte[] keyBytes = Base64.getDecoder().decode(JWT_SECRET_B64);
    JWSObject jws =
        new JWSObject(
            new JWSHeader(JWSAlgorithm.HS256),
            new Payload(
                new JSONObject(
                    Map.of(
                        "sub", "test-user",
                        "exp",
                            Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond()))));
    jws.sign(new MACSigner(keyBytes));
    return jws.serialize();
  }
}