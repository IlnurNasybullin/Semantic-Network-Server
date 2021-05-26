package app.server.validators;

public enum ErrorCode {
    PK_NULL,
    ENTITY_NULL,
    PK_IS_NOT_NULL,
    FK_INSERT_UPDATE_CONSTRAINT,
    FK_DELETE_CONSTRAINT,
    FK_NULL,
    UNKNOWN_ERROR,
    COLLECTION_BIG_SIZE
}
