package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;

import java.io.File;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;

public interface CodeRepositoryFactory {
    CodeRepository createCodeRepository(CodeRepositoryMetadata metadata, File folder,
            SshPrivateKeyPasswordProvider sshPrivateKeyPasswordProvider);
}
