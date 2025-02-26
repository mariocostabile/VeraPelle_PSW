//package psw.verapelle.config;
//
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KeycloakConfig {
//
//    @Bean
//    public Keycloak keycloak() {
//        return KeycloakBuilder.builder()
//                .serverUrl("http://localhost:8081") // URL del server Keycloak
//                .realm("ProgettoPSW_PelleVera") // Realm di amministrazione (di solito "master" per admin)
//                .clientId("pelle-vera-api") // Client usato per l'admin API
//                .username("admin") // Username dell'admin
//                .password("admin") // Password dell'admin
//                .build();
//    }
//}
//
