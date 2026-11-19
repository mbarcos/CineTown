package com.safa.cinetown.servicios;

import com.safa.cinetown.config.CorreoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@EnableConfigurationProperties(CorreoProperties.class)
public class CorreoCompraService {
    private static final System.Logger LOGGER = System.getLogger(CorreoCompraService.class.getName());
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm", new Locale("es", "ES"));

    private final JavaMailSender mailSender;
    private final CorreoProperties properties;

    public CorreoCompraService(JavaMailSender mailSender, CorreoProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarConfirmacion(CompraConfirmadaEvent compra) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(properties.remitente());
        mensaje.setTo(compra.email());
        mensaje.setSubject("CineTown - Confirmación de compra " + compra.codigo());
        mensaje.setText("Tu compra de entradas está confirmada.\n\n"
                + "Código de compra: " + compra.codigo() + "\n"
                + "Película: " + compra.pelicula() + "\n"
                + "Sesión: " + FECHA.format(compra.fechaSesion()) + "\n"
                + "Sala: " + compra.sala() + "\n"
                + "Entradas: " + compra.entradas() + "\n"
                + "Total: " + compra.total() + " EUR\n\n"
                + "Conserva este correo y el código de compra.");

        try {
            mailSender.send(mensaje);
        } catch (MailException ex) {
            // La compra ya está confirmada: un fallo SMTP no debe convertirla en un error HTTP
            // que pueda llevar al cliente a repetir la compra.
            LOGGER.log(System.Logger.Level.ERROR,
                    "No se pudo enviar el correo de confirmación de la compra " + compra.codigo(), ex);
        }
    }
}
