package com.benromberg.cordonbleu.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public abstract class EmailTemplate {
    private static final STGroup COMMON_TEMPLATE = new STGroupFile("email/_common.stg", '$', '$');
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplate.class);

    private final STGroup group;

    public EmailTemplate(String filename) {
        group = new STGroupFile("email/" + filename, '$', '$');
    }

    protected void registerRenderer(Class<?> type, AttributeRenderer renderer) {
        group.registerRenderer(type, renderer);
    }

    protected ST getSubjectTemplate() {
        return group.getInstanceOf("subject");
    }

    protected ST getPlainBodyTemplate() {
        return group.getInstanceOf("plainBody");
    }

    protected ST getHtmlBodyTemplate() {
        return group.getInstanceOf("htmlBody");
    }

    public String getSubject() {
        String subjectPrefix = COMMON_TEMPLATE.getInstanceOf("subjectPrefix").render();
        String subject = subjectPrefix + getSubjectTemplate().render();
        if (subject.contains("\n")) {
            LOGGER.warn("Subject for email from template {} contains newlines, which is probably not intended.",
                    group.getFileName());
        }
        return subject;
    }

    public String getPlainBody() {
        String header = COMMON_TEMPLATE.getInstanceOf("plainHeader").render();
        String footer = COMMON_TEMPLATE.getInstanceOf("plainFooter").render();
        return header + getPlainBodyTemplate().render() + footer;
    }

    public String getHtmlBody(String sharedCss) {
        String header = COMMON_TEMPLATE.getInstanceOf("htmlHeader").add("sharedCss", sharedCss).render();
        String footer = COMMON_TEMPLATE.getInstanceOf("htmlFooter").render();
        return header + getHtmlBodyTemplate().render() + footer;
    }
}