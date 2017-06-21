package com.benromberg.cordonbleu.service.email;

import com.benromberg.cordonbleu.data.model.User;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message.RecipientType;

import static java.util.stream.Collectors.toList;

@Singleton
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final Mailer mailer;
    private final EmailConfiguration configuration;
    private final Queue<Email> emailQueue = new ConcurrentLinkedQueue<>();

    @Inject
    public EmailService(EmailConfiguration configuration) {
        mailer = new Mailer(configuration.getHost(), configuration.getPort(), configuration.getUsername(),
                configuration.getPassword(), TransportStrategy.SMTP_TLS);
        this.configuration = configuration;
    }

    public void queueEmail(Collection<String> to, User replyTo, EmailTemplate template) {
        if (to.isEmpty()) {
            return;
        }
        Email email = new Email();
        email.setFromAddress("Cordon Bleu", configuration.getFromAddress());
        email.setSubject(template.getSubject());
        email.setReplyToAddress(null, replyTo.getEmail());
        to.stream().forEach(recipient -> email.addRecipient(null, recipient, RecipientType.TO));
        email.setText(template.getPlainBody());
        email.setTextHTML(template.getHtmlBody(configuration.getSharedCss()));
        emailQueue.add(email);
    }

    public void sendQueuedEmails() {
        Email email;
        List<Email> failedEmails = new ArrayList<>();
        while ((email = emailQueue.poll()) != null) {
            trySendingEmail(email).ifPresent(failedEmail -> failedEmails.add(failedEmail));
        }
        emailQueue.addAll(failedEmails);
    }

    private Optional<Email> trySendingEmail(Email email) {
        try {
            mailer.sendMail(email);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.warn("Could not send email to '{}', requeueing.",
                    email.getRecipients().stream().map(recipient -> recipient.getAddress()).collect(toList()), e);
            return Optional.of(email);
        }
    }

    public int getQueueLength() {
        return emailQueue.size();
    }

    public String getRootPath() {
        return configuration.getRootPath();
    }
}
