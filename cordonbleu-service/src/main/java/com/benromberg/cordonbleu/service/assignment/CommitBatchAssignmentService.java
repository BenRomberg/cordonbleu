package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommitBatchAssignmentService {

    public List<CommitBatchAssignment> generateCommitBatchAssignments(List<Commit> commits, List<User> users) {
        UsersRandomLoopIterator usersIterator = new UsersRandomLoopIterator(users);

        return commits.stream()
                .collect(Collectors.groupingBy(commit -> commit.getAuthor().getEmail()))
                .entrySet()
                .stream()
                .map(entry -> toCommitBatchAssignment(usersIterator.getNextWithEmailNotEqualTo(entry.getKey()), entry))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<CommitBatchAssignment> toCommitBatchAssignment(Optional<User> user, Map.Entry<String, List<Commit>> entry) {
        return user.map(assignTo -> new CommitBatchAssignment(assignTo, entry.getValue().iterator().next().getAuthor(), entry.getValue()));
    }

    private class UsersRandomLoopIterator {
        List<User> shuffledUsers;
        Iterator<User> iterator;

        private UsersRandomLoopIterator(List<User> users) {
            shuffledUsers = new ArrayList<>(users);
            Collections.shuffle(shuffledUsers);
            iterator = shuffledUsers.iterator();
        }

        private Optional<User> getNextWithEmailNotEqualTo(String emailToIgnore) {
            int resetCount = 0;

            while (resetCount < 2) {
                if (!iterator.hasNext()) {
                    iterator = shuffledUsers.iterator();
                    resetCount++;
                }

                User nextUser = iterator.next();

                if (isEmailNotEqualTo(nextUser, emailToIgnore)) {
                    return Optional.of(nextUser);
                }
            }

            return Optional.empty();
        }

        private boolean isEmailNotEqualTo(User user, String email) {
            return !user.getEmail().equals(email) && !user.getEmailAliases().contains(email);
        }
    }
}
