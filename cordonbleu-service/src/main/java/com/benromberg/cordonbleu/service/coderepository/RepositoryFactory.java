package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;

import java.io.File;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.benromberg.cordonbleu.service.coderepository.svncredential.SvnCredentialProvider;

public class RepositoryFactory implements CodeRepositoryFactory {
    @Override
    public CodeRepository createCodeRepository(CodeRepositoryMetadata metadata, File folder,
            SshPrivateKeyPasswordProvider sshPrivateKeyPasswordProvider, SvnCredentialProvider svnCredentialProvider) {
        if(CodeRepositoryMetadata.TYPE_SVN.equals(metadata.getType())) {
            return new SvnRepository(metadata, folder, svnCredentialProvider);
        } else {
            return new GitRepository(metadata, folder, sshPrivateKeyPasswordProvider);
        }
    }
}
