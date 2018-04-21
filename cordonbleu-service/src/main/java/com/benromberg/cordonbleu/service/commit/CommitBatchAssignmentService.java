package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommitBatchAssignmentService {

    public List<CommitBatchAssignment> generateCommitBatchAssignments(List<Commit> commits, List<User> users) {
        checkNotEmpty(commits, users);
        UsersRandomLoopIterator usersIterator = new UsersRandomLoopIterator(users);

        return commits.stream()
                .collect(Collectors.groupingBy(commit -> commit.getAuthor().getEmail()))
                .entrySet()
                .stream()
                .map(entry -> toCommitBatchAssignment(usersIterator.getNext(), entry))
                .collect(Collectors.toList());
    }

    private CommitBatchAssignment toCommitBatchAssignment(User user, Map.Entry<String, List<Commit>> entry) {
        return new CommitBatchAssignment(user, entry.getValue().iterator().next().getAuthor(), entry.getValue());
    }

    private void checkNotEmpty(List<Commit> commits, List<User> users) {
        if (commits.isEmpty()) {
            throw new IllegalArgumentException("Commits list shall not be null");
        }
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Users list shall not be null");
        }
    }

    private class UsersRandomLoopIterator {
        List<User> shuffledUsers;
        Iterator<User> iterator;

        private UsersRandomLoopIterator(List<User> users) {
            shuffledUsers = new ArrayList<>(users);
            Collections.shuffle(shuffledUsers);
            iterator = shuffledUsers.iterator();
        }

        private User getNext() {
            if (!iterator.hasNext()) {
                iterator = shuffledUsers.iterator();
            }
            return iterator.next();
        }
    }
}
