package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;

public class AssignmentEmailServiceMock extends AssignmentEmailService {

    private Commit singleAssignmentCalledWithCommit;

    private User singleAssignmentCalledWithUser;

    private User singleAssignmentCalledWithAssignedBy;

    private CommitBatchAssignment batchAssignmentCalledWithBatch;

    private User batchAssignmentCalledWithAssignedBy;

    public AssignmentEmailServiceMock() {
        super(null);
    }

    @Override
    public void sendSingleAssignmentEmail(Commit commit, User assignedTo, User assignedBy) {
        singleAssignmentCalledWithCommit = commit;
        singleAssignmentCalledWithUser = assignedTo;
        singleAssignmentCalledWithAssignedBy = assignedBy;
    }

    @Override
    public void sendBatchAssignmentEmail(CommitBatchAssignment batchAssignment, User assignedBy) {
        batchAssignmentCalledWithBatch = batchAssignment;
        batchAssignmentCalledWithAssignedBy = assignedBy;
    }

    public Commit getSingleAssignmentCalledWithCommit() {
        return singleAssignmentCalledWithCommit;
    }

    public User getSingleAssignmentCalledWithUser() {
        return singleAssignmentCalledWithUser;
    }

    public User getSingleAssignmentCalledWithAssignedBy() {
        return singleAssignmentCalledWithAssignedBy;
    }

    public CommitBatchAssignment getBatchAssignmentCalledWithBatch() {
        return batchAssignmentCalledWithBatch;
    }

    public User getBatchAssignmentCalledWithAssignedBy() {
        return batchAssignmentCalledWithAssignedBy;
    }
}
