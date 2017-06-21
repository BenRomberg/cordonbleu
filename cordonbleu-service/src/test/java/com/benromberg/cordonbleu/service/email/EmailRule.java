package com.benromberg.cordonbleu.service.email;

import com.google.common.net.MediaType;

import com.benromberg.cordonbleu.data.model.User;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class EmailRule implements TestRule {
    public static final String EMAIL_ROOT_PATH = "http://benromberg.com/cordonbleu";
    private static final ServerSetup EMAIL_SERVER = ServerSetupTest.SMTP;

    private final GreenMailRule greenMail = new GreenMailRule(EMAIL_SERVER);
    private final EmailService emailService = new EmailService(
            new EmailConfiguration(EMAIL_SERVER.getBindAddress(), EMAIL_SERVER.getPort(), "", "", "cordonbleu@example.com", EMAIL_ROOT_PATH,
                    ""));

    @Override
    public Statement apply(Statement base, Description description) {
        return greenMail.apply(base, description);
    }

    public void queueAndSendEmail(User replyTo, EmailTemplate template, String... to) {
        emailService.queueEmail(asList(to), replyTo, template);
        sendQueuedEmails();
    }

    public void shutdownEmailServer() {
        greenMail.stop();
    }

    public List<MimeMessage> getReceivedMessages() {
        return asList(greenMail.getReceivedMessages());
    }

    public int getQueueLength() {
        return emailService.getQueueLength();
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public List<String> getRecipientsTo() {
        Address[] recipients = convertException(() -> getReceivedMessages().get(0).getRecipients(RecipientType.TO));
        return Stream.of(recipients).map(address -> address.toString()).collect(toList());
    }

    public String getReplyTo() {
        Address[] replyTos = convertException(() -> getReceivedMessages().get(0).getReplyTo());
        return replyTos[0].toString();
    }

    public String getSubject() {
        return convertException(() -> getReceivedMessages().get(0).getSubject());
    }

    public void sendQueuedEmails() {
        emailService.sendQueuedEmails();
    }

    public String getPlainBody() {
        return getPart(getReceivedMessages().get(0), MediaType.PLAIN_TEXT_UTF_8);
    }

    public String getHtmlBody() {
        return getPart(getReceivedMessages().get(0), MediaType.HTML_UTF_8);
    }

    private String getPart(MimeMessage email, MediaType part) {
        Object msgContent = convertException(() -> email.getContent());
        if (msgContent instanceof Multipart) {
            return convertException(() -> getPlainBodyFromMultipart((Multipart) msgContent, part));
        }
        return msgContent.toString();
    }

    private String getPlainBodyFromMultipart(Multipart multipart, MediaType part) throws IOException,
            MessagingException {
        for (int j = 0; j < multipart.getCount(); j++) {
            BodyPart bodyPart = multipart.getBodyPart(j);
            if (bodyPart.getContent() instanceof Multipart) {
                return getPlainBodyFromMultipart((Multipart) bodyPart.getContent(), part);
            }
            if (part.equals(MediaType.parse(bodyPart.getContentType()))) {
                return bodyPart.getContent().toString();
            }
        }
        return multipart.toString();
    }

}
