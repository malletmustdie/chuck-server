package ru.elias.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.elias.server.config.SpringMapperConfig;
import ru.elias.server.model.Category;

import ru.elias.server.dto.CategoryDto;

@Mapper(config = SpringMapperConfig.class)
public interface CategoryMapper {

   @Mapping(source = "request.name", target = "name")
   Category map(CategoryDto request);

   @Mapping(source = "entity.name", target = "name")
   CategoryDto map(Category entity);

}
