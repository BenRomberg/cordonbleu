package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;

import java.io.File;
import java.util.Optional;

import com.benromberg.cordonbleu.service.coderepository.CodeRepository;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryFactory;
import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.benromberg.cordonbleu.service.coderepository.svncredential.SvnCredentialProvider;

public class CodeRepositoryFactoryMock implements CodeRepositoryFactory {
    private File lastFolder;
    private final Optional<CodeRepositoryMock> defaultRepository;
    private final Optional<CodeRepositoryMetadata> defaultRepositoryMetadata;

    public CodeRepositoryFactoryMock() {
        defaultRepository = Optional.empty();
        defaultRepositoryMetadata = Optional.empty();
    }

    public CodeRepositoryFactoryMock(CodeRepositoryMetadata defaultRepositoryMetadata) {
        this.defaultRepositoryMetadata = Optional.of(defaultRepositoryMetadata);
        defaultRepository = Optional.of(new CodeRepositoryMock(defaultRepositoryMetadata));
    }

    @Override
    public CodeRepository createCodeRepository(CodeRepositoryMetadata repository, File folder,
                                               SshPrivateKeyPasswordProvider sshPrivateKeyPasswordProvider, SvnCredentialProvider svnCredentialProvider) {
        lastFolder = folder;
        if (defaultRepositoryMetadata.isPresent() && repository.equals(defaultRepositoryMetadata.get())) {
            return defaultRepository.get();
        }
        return new CodeRepositoryMock(repository);
    }

    public File getLastFolder() {
        return lastFolder;
    }

    public CodeRepositoryMock getDefaultRepository() {
        return defaultRepository.get();
    }
}