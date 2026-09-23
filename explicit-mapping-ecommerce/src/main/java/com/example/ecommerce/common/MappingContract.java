package com.example.ecommerce.common;

import java.lang.annotation.*;

/**
 * Marker annotation documenting which fields require explicit mapping.
 * Not processed at runtime — serves as inline documentation.
 *
 * BeanUtils can handle:
 *   - Same name + same type: direct copy
 * BeanUtils CANNOT handle (fails silently):
 *   - field renamed
 *   - type different
 *   - computed value
 *   - multi-field merged
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface MappingContract {
    ContractType value();

    enum ContractType {
        AUTO,           // same name+type, BeanUtils OK
        RENAME,         // @Mapping(source, target)
        TYPE_CONVERT,   // @Mapping(qualifiedByName)
        COMPUTED,       // @AfterMapping
        FIELD_MERGE,    // @AfterMapping with multiple sources
        LIST_MAP        // Nested type conversion
    }
}
