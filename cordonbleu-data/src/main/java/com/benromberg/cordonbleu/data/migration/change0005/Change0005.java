package com.benromberg.cordonbleu.data.migration.change0005;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0005 extends Change {
    private static final String CHANGELOG = "0005";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void uniqueEmailAlias() {
        ChangeCollection collection = getCollection("user");
        collection.updateAll(user -> {
            @SuppressWarnings("unchecked")
            List<Object> uniqueArray = aliasesToUniqueArray((List<Object>) user.get("emailAliases"));
            return $setOne("emailAliases", uniqueArray);
        });
    }

    private List<Object> aliasesToUniqueArray(List<Object> aliases) {
        return aliases.stream()
                .map(alias -> object("value", alias.toString()).append("unique", alias.toString().toLowerCase()))
                .collect(toList());
    }

}