package com.safa.cinetown;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "app.correo.remitente=test@example.com"
})
class CineTownApplicationTests {

    @Test
    void contextLoads() {
    }

}
