package com.benromberg.cordonbleu.service.email;

import com.benromberg.cordonbleu.data.model.User;

import org.junit.Rule;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import java.util.List;

import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailServiceTest {
    private static final User SENDING_USER = new User("sending@user.com", "user", "some password");
    private static final String TEST_HTML_CONTENT = "<p>Hello World!</p>";
    private static final String TEST_PLAIN_CONTENT = "Hello, World!";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String EMAIL_TO = "to@test.com";

    @Rule
    public final EmailRule emailRule = new EmailRule();

    @Test
    public void sendEmail() throws Exception {
        emailRule.queueAndSendEmail(SENDING_USER, TEST_TEMPLATE, EMAIL_TO);
        List<MimeMessage> emails = emailRule.getReceivedMessages();
        assertThat(emails).hasSize(1);
        assertThat(emailRule.getRecipientsTo()).containsExactly(EMAIL_TO);
        assertThat(emailRule.getReplyTo()).isEqualTo(SENDING_USER.getEmail());
        assertThat(emailRule.getSubject()).contains(TEST_SUBJECT);
        assertThat(emailRule.getPlainBody()).contains(TEST_PLAIN_CONTENT);
        assertThat(emailRule.getHtmlBody()).contains(TEST_HTML_CONTENT);
    }

    @Test
    public void sendEmail_WithNoRecipient_DoesNothing() throws Exception {
        emailRule.queueAndSendEmail(SENDING_USER, TEST_TEMPLATE);
        assertThat(emailRule.getReceivedMessages()).isEmpty();
    }

    @Test
    public void sendEmail_WithFailingConnection_PutsMailBackIntoQueue() throws Exception {
        emailRule.shutdownEmailServer();
        emailRule.queueAndSendEmail(SENDING_USER, TEST_TEMPLATE, EMAIL_TO);
        assertThat(emailRule.getReceivedMessages()).isEmpty();
        assertThat(emailRule.getQueueLength()).isEqualTo(1);
    }

    private static final EmailTemplate TEST_TEMPLATE = new EmailTemplate("test.stg") {
        @Override
        protected ST getHtmlBodyTemplate() {
            return super.getHtmlBodyTemplate().add("value", TEST_HTML_CONTENT);
        }

        @Override
        protected ST getPlainBodyTemplate() {
            return super.getPlainBodyTemplate().add("value", TEST_PLAIN_CONTENT);
        }

        @Override
        protected ST getSubjectTemplate() {
            return super.getSubjectTemplate().add("value", TEST_SUBJECT);
        }
    };
}
