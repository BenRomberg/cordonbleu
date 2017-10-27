package com.benromberg.cordonbleu.service.coderepository;

import static java.util.Arrays.asList;

import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;

import com.benromberg.cordonbleu.data.model.Commit;

public class CodeRepositoryServiceMock extends CodeRepositoryService {
    private final String pathBefore;
    private final String pathAfter;

    public CodeRepositoryServiceMock(String pathBefore, String pathAfter) {
        super(null, () -> null, null, null, null, null, null, null);
        this.pathBefore = pathBefore;
        this.pathAfter = pathAfter;
    }

    @Override
    public CommitDetail getCommitDetail(Commit commit) {
        return new CommitDetail(commit, asList(CommitFile.changed(pathBefore, pathAfter,
                CommitFileContent.ofSource("1st\n2nd\n3rd"), CommitFileContent.ofSource("1st\n2nd\n3rd"))));
    }

}
