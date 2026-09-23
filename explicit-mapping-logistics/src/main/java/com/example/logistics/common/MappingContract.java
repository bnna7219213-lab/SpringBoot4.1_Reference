package com.example.logistics.common;

import java.lang.annotation.*;

/**
 * Marker annotation documenting which fields require explicit mapping.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface MappingContract {
    ContractType value();

    enum ContractType {
        AUTO, RENAME, TYPE_CONVERT, COMPUTED, FIELD_MERGE, LIST_MAP
    }
}
