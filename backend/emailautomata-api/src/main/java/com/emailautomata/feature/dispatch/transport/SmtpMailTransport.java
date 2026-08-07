package com.emailautomata.feature.dispatch.transport;

import com.emailautomata.core.config.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Real SMTP transport, active only when {@code emailautomata.mail.transport}
 * is set to 'smtp'.
 *
 * <p>Relies on Spring Boot's auto-configured {@link JavaMailSender}, so
 * {@code spring.mail.host} and credentials must also be supplied when this
 * transport is selected.</p>
 */
@Component
@ConditionalOnProperty(name = "emailautomata.mail.transport", havingValue = "smtp")
public class SmtpMailTransport implements MailTransport {

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public SmtpMailTransport(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void deliver(OutboundMessage message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(properties.fromAddress());
            mail.setTo(message.toEmail());
            mail.setSubject(message.subject());
            mail.setText(message.body());
            mailSender.send(mail);
        } catch (MailException ex) {
            throw new MailTransportException(
                    ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    @Override
    public String name() {
        return "smtp";
    }
}