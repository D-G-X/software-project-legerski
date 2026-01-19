package de.hft.licensing.application.services;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void resetStaticState() throws Exception {
        setStaticField(EmailService.class, "staticMailSender", null);
        setStaticField(EmailService.class, "staticSenderEmail", null);
    }

    @Test
    void sendEmail_throwsIllegalStateException_whenNotInitialized() {
        EmailService svc = new EmailService(mailSender, "sender@x.de");
        setStaticSilently("staticMailSender", null);

        assertThrows(IllegalStateException.class,
                () -> svc.sendEmail("to@x.de", null, "sub", "<b>body</b>"));
    }

    @Test
    void sendEmail_returnsFalse_whenToNull() {
        new EmailService(mailSender, "sender@x.de");
        assertFalse(new EmailService(mailSender, "sender@x.de").sendEmail(null, null, "s", "b"));
        verifyNoInteractions(mailSender);
    }

    @Test
    void sendEmail_returnsFalse_whenToBlank() {
        new EmailService(mailSender, "sender@x.de");
        assertFalse(new EmailService(mailSender, "sender@x.de").sendEmail("   ", null, "s", "b"));
        verifyNoInteractions(mailSender);
    }

    @Test
    void sendEmail_returnsTrue_whenCcNull() throws Exception {
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(msg);

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertTrue(svc.sendEmail("to@x.de", null, "sub", "<b>body</b>"));
        verify(mailSender).send(msg);
    }

    @Test
    void sendEmail_returnsTrue_whenCcBlank() throws Exception {
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(msg);

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertTrue(svc.sendEmail("to@x.de", "   ", "sub", "<b>body</b>"));
        verify(mailSender).send(msg);
    }

    @Test
    void sendEmail_returnsTrue_whenCcProvided() throws Exception {
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(msg);

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertTrue(svc.sendEmail("to@x.de", "cc@x.de", "sub", "<b>body</b>"));
        verify(mailSender).send(msg);
    }

    @Test
    void sendEmail_returnsFalse_whenCreateMimeMessageThrowsMailException() {
        when(mailSender.createMimeMessage()).thenThrow(new MailSendException("create failed"));

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertThrows(MailSendException.class, () -> svc.sendEmail("to@x.de", null, "s", "b"));
    }

    @Test
    void sendEmail_returnsFalse_whenSendThrowsMailException() throws Exception {
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(msg);
        doThrow(new MailSendException("send failed")).when(mailSender).send(msg);

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertFalse(svc.sendEmail("to@x.de", null, "sub", "<b>body</b>"));
    }

    @Test
    void sendEmail_returnsFalse_whenHelperThrowsMessagingException() throws Exception {
        MimeMessage msg = spy(new MimeMessage(Session.getInstance(new Properties())));
        when(mailSender.createMimeMessage()).thenReturn(msg);

        doThrow(new MessagingException("boom"))
                .when(msg).setRecipient(eq(jakarta.mail.Message.RecipientType.TO), any(jakarta.mail.Address.class));

        EmailService svc = new EmailService(mailSender, "sender@x.de");

        assertFalse(svc.sendEmail("to@x.de", null, "sub", "<b>body</b>"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    private static void setStaticField(Class<?> clazz, String name, Object value) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static void setStaticSilently(String field, Object value) {
        try {
            setStaticField(EmailService.class, field, value);
        } catch (Exception ignored) {
        }
    }
}