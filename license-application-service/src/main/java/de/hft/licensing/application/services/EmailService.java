package de.hft.licensing.application.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;

@Service
public class EmailService {

    private static JavaMailSender staticMailSender;

    private static String staticSenderEmail;

    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String senderEmail) {

        EmailService.staticMailSender = mailSender;
        EmailService.staticSenderEmail = senderEmail;
    }

    /**
     * Sends an email with the specified parameters.
     *
     * @param to      Recipient email address.
     * @param cc      CC email address (can be null).
     * @param subject Subject of the email.
     * @param body    Body content of the email (HTML format).
     * @return true if the email was sent successfully, false otherwise.
     */
    public static boolean sendEmail(String to, String cc, String subject, String body) {
        if (staticMailSender == null) {
            throw new IllegalStateException("EmailService not initialized.");
        }
        if (to == null || to.trim().isEmpty()) {
            System.err.println("Cannot send email: 'to' address is required.");
            return false;
        }

        MimeMessage message = staticMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(staticSenderEmail);

            helper.setTo(to);

            if (cc != null && !cc.trim().isEmpty()) {
                helper.setCc(cc);
            }

            helper.setSubject(subject);
            helper.setText(body, true);

            staticMailSender.send(message);
            System.out.println("Email successfully sent to: " + to);
            return true;

        } catch (MessagingException e) {
            System.err.println("Email formatting error for " + to + ": " + e.getMessage());
            return false;

        } catch (MailException e) {
            System.err.println("Email connection/sending failure for " + to + ": " + e.getMessage());
            return false;
        }
    }
}

