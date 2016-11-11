package com.benromberg.cordonbleu.main.config;

import static java.util.Arrays.asList;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.DatabaseWithMigration;
import com.benromberg.cordonbleu.data.testutil.DatabaseRule;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryFactory;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryFactoryMock;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.highlight.SyntaxHighlighter;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class TestModule extends AbstractModule {
    @Provides
    public DatabaseWithMigration getDatabase() {
        return new DatabaseWithMigration(DatabaseRule.getDB(), new DaoRule.NullMongoMigration());
    }

    @Provides
    public SyntaxHighlighter getSyntaxHighlighter() {
        return new SyntaxHighlighter(null, null) {
            @Override
            public List<String> highlight(CommitFileState state) {
                return asList(state.getContent().split("\n", -1));
            }
        };
    }

    @Override
    protected void configure() {
        bind(CodeRepositoryFactory.class).to(CodeRepositoryFactoryMock.class);
    }

}
