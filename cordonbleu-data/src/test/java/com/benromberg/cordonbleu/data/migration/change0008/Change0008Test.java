package com.benromberg.cordonbleu.data.migration.change0008;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0008.Change0008;

public class Change0008Test {
    private static final String CACHE_ID_1 = "repo-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String HIGHLIGHT_CACHE_COLLECTION = "highlightCache";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0008.class);

    @Test
    public void dropHighlightCacheCollection() throws Exception {
        TestCollection<HighlightCacheBefore> collectionBefore = changeRule.getCollection(HIGHLIGHT_CACHE_COLLECTION,
                HighlightCacheBefore.class);
        collectionBefore.insert(new HighlightCacheBefore(CACHE_ID_1, OTHER_FIELD_1));

        changeRule.runChanges();

        assertThat(changeRule.collectionExists(HIGHLIGHT_CACHE_COLLECTION)).isFalse();
    }
}
