package com.safa.cinetown;

import com.safa.cinetown.config.CorreoProperties;
import com.safa.cinetown.servicios.CompraConfirmadaEvent;
import com.safa.cinetown.servicios.CorreoCompraService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorreoCompraServiceTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final CorreoCompraService service = new CorreoCompraService(
            mailSender, new CorreoProperties("entradas@cinetown.example"));
    private final CompraConfirmadaEvent compra = new CompraConfirmadaEvent(
            "ABC12345", "cliente@example.com", "Película de prueba",
            LocalDateTime.of(2026, 11, 25, 19, 0), 4, 2, new BigDecimal("14.00"));

    @Test
    void enviaConfirmacionConLosDatosDeLaCompra() {
        service.enviarConfirmacion(compra);

        org.mockito.ArgumentCaptor<SimpleMailMessage> captor =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mensaje = captor.getValue();
        assertEquals("entradas@cinetown.example", mensaje.getFrom());
        assertArrayEquals(new String[]{"cliente@example.com"}, mensaje.getTo());
        assertTrue(mensaje.getSubject().contains("ABC12345"));
        assertTrue(mensaje.getText().contains("Película: Película de prueba"));
        assertTrue(mensaje.getText().contains("25/11/2026 a las 19:00"));
        assertTrue(mensaje.getText().contains("Sala: 4"));
        assertTrue(mensaje.getText().contains("Entradas: 2"));
        assertTrue(mensaje.getText().contains("14.00 EUR"));
    }

    @Test
    void falloSmtpNoGeneraUnErrorQueAnimeARepetirLaCompra() {
        doThrow(new MailSendException("SMTP no disponible"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> service.enviarConfirmacion(compra));
    }
}
