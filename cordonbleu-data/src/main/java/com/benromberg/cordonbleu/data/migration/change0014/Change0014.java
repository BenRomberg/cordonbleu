package com.benromberg.cordonbleu.data.migration.change0014;

import com.benromberg.cordonbleu.data.migration.Change;
import com.benromberg.cordonbleu.data.util.KeyPair;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0014 extends Change {
    private static final String TEAM_COLLECTION = "team";
    private static final String CHANGELOG = "0014";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void addKeyPairs() {
        ChangeCollection collection = getCollection(TEAM_COLLECTION);
        collection.updateAll(team -> {
            KeyPair keyPair = getKeyPairGenerator().generate();
            return $setOne("keyPair",
                    object("privateKey", keyPair.getPrivateKey()).append("publicKey", keyPair.getPublicKey()));
        });
    }
}
