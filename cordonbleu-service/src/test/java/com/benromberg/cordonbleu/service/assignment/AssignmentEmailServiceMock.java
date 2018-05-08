package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;

public class AssignmentEmailServiceMock extends AssignmentEmailService {

    private Commit calledWithCommit;

    private User calledWithUser;

    private User calledWithAssignedBy;

    public AssignmentEmailServiceMock() {
        super(null);
    }

    @Override
    public void sendSingleAssignmentEmail(Commit commit, User assignedTo, User assignedBy) {
        calledWithCommit = commit;
        calledWithUser = assignedTo;
        calledWithAssignedBy = assignedBy;
    }

    public Commit getCalledWithCommit() {
        return calledWithCommit;
    }

    public User getCalledWithUser() {
        return calledWithUser;
    }

    public User getCalledWithAssignedBy() {
        return calledWithAssignedBy;
    }
}
