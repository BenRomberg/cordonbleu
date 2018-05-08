package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.email.EmailTemplate;

import org.stringtemplate.v4.ST;

public class SingleAssignmentEmailTemplate extends EmailTemplate {
    private final Commit commit;
    private final User assignedBy;
    private final String commitPath;

    public SingleAssignmentEmailTemplate(Commit commit, User assignedBy, String commitPath) {
        super("single-assignment.stg");
        this.commit = commit;
        this.assignedBy = assignedBy;
        this.commitPath = commitPath;
    }

    @Override
    protected ST getSubjectTemplate() {
        return injectCommonAttributes(super.getSubjectTemplate());
    }

    @Override
    protected ST getPlainBodyTemplate() {
        return injectCommonAttributes(super.getPlainBodyTemplate());
    }

    @Override
    protected ST getHtmlBodyTemplate() {
        return injectCommonAttributes(super.getHtmlBodyTemplate());
    }

    private ST injectCommonAttributes(ST template) {
        return template.add("commit", commit).add("commitPath", commitPath).add("assignedBy", assignedBy);
    }

}
