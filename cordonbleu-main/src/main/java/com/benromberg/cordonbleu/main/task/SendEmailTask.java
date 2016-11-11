package com.benromberg.cordonbleu.main.task;

import com.benromberg.cordonbleu.service.email.EmailService;

import javax.inject.Inject;

public class SendEmailTask extends AbstractTask {
    private EmailService emailService;

    @Inject
    public SendEmailTask(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void runTask() {
        emailService.sendQueuedEmails();
    }
}
