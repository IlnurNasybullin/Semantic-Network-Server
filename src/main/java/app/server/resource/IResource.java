package app.server.resource;

import app.server.domain.IEntity;

import java.io.Serializable;

public interface IResource<T extends IEntity> extends Serializable {
    T toEntity();
}
