package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import com.benromberg.cordonbleu.data.model.Entity;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.benromberg.cordonbleu.data.validation.Validation;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheDao<I, E extends Entity<I>> extends MongoDao<I, E> {
    private final Cache<I, Optional<E>> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public CacheDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName,
            Validation<? super E> validation) {
        super(database, idClass, elementClass, collectionName, new CustomModule(), validation);
    }

    public CacheDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName,
            CustomModule customModule) {
        super(database, idClass, elementClass, collectionName, customModule);
    }

    public CacheDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName,
            CustomModule customModule, Validation<? super E> validation) {
        super(database, idClass, elementClass, collectionName, customModule, validation);
    }

    @Override
    public Optional<E> findById(I id) {
        return convertException(() -> cache.get(id, () -> super.findById(id)));
    }

    @Override
    public void insert(E element) {
        super.insert(element);
        cache.invalidate(element.getId());
    }

    @Override
    protected Optional<E> update(Query find, DBUpdate.Builder update) {
        Optional<E> changed = super.update(find, update);
        if (changed.isPresent()) {
            cache.put(changed.get().getId(), changed);
        }
        return changed;
    }

    @Override
    protected Optional<E> update(I id, DBUpdate.Builder update) {
        Optional<E> changed = super.update(id, update);
        cache.put(id, changed);
        return changed;
    }

    @Override
    public boolean remove(I id) {
        boolean hasRemoved = super.remove(id);
        cache.invalidate(id);
        return hasRemoved;
    }

    public void invalidate(I id) {
        cache.invalidate(id);
    }
}
