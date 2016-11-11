package com.benromberg.cordonbleu.data.util.jackson;

import java.util.Optional;

import com.benromberg.cordonbleu.data.model.Entity;

public interface ReferenceResolver<I, T extends Entity<I>> {
    Optional<T> findById(I id);
}
