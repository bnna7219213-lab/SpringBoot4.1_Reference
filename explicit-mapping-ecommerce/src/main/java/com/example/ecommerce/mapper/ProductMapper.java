package com.example.ecommerce.mapper;

import com.example.ecommerce.converter.EcommerceConverters;
import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.ProductEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;

/**
 * MapStruct mapper for ProductEntity -> ProductProductDTO.
 *
 * Explicit mappings required for 5 out of 7 fields (id, sku auto-mapped):
 *
 *   source.field -> target.field         mapping mechanism
 *   ──────────────────────────────────────────────────────────
 *   name           -> name               auto (same name, type)
 *   sku            -> sku                auto (same name, type)
 *   unitPrice      -> formattedPrice     @Mapping + @Named("priceToFormatted")
 *   stock          -> inStock            @Mapping + @Named("stockToInStock")
 *   weight         -> weightDisplay      @Mapping + @Named("weightToDisplay")
 *   categoryCode   -> categoryName       @Mapping + @Named("categoryCodeToName")
 */
@Mapper(componentModel = "spring",
        uses = EcommerceConverters.class,
        imports = BigDecimal.class)
public interface ProductMapper {

    @Mapping(source = "unitPrice", target = "formattedPrice", qualifiedByName = "priceToFormatted")
    @Mapping(source = "stock", target = "inStock", qualifiedByName = "stockToInStock")
    @Mapping(source = "weight", target = "weightDisplay", qualifiedByName = "weightToDisplay")
    @Mapping(source = "categoryCode", target = "categoryName", qualifiedByName = "categoryCodeToName")
    ProductDTO toProductDTO(ProductEntity entity);
}
