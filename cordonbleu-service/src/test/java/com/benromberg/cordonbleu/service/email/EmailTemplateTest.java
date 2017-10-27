package com.benromberg.cordonbleu.service.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import com.benromberg.cordonbleu.service.email.EmailTemplate;

public class EmailTemplateTest {
    private static final String SEPARATOR_BETWEEN_HEADER_AND_FOOTER = "separator between header and footer";

    @Test
    public void plainHeaderAndFooter_HaveTwoNewlinesPadding() throws Exception {
        EmailTemplate template = new EmailTemplate("test.stg") {
            @Override
            protected ST getPlainBodyTemplate() {
                return super.getPlainBodyTemplate().add("value", SEPARATOR_BETWEEN_HEADER_AND_FOOTER);
            }
        };

        String CRLF = System.getProperty("line.separator");
        assertThat(template.getPlainBody()).contains(CRLF+CRLF + SEPARATOR_BETWEEN_HEADER_AND_FOOTER + CRLF+CRLF);
        assertThat(template.getPlainBody()).doesNotContain(CRLF+CRLF+CRLF);
    }

    @Test
    public void subjectPrefix_EndsWithSpace() throws Exception {
        EmailTemplate template = new EmailTemplate("test.stg") {
        };
        assertThat(template.getSubject()).endsWith(" ");
    }
}
