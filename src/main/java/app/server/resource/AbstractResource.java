package app.server.resource;

import app.server.domain.IEntity;

public abstract class AbstractResource<T extends IEntity> implements IResource<T> {

    public AbstractResource() {}

    public AbstractResource(T entity, boolean expand) {}

    @Override
    public abstract T toEntity();
}
