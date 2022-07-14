package io.elias.server.filter;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.elias.server.filter.base.StringFilter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Объект фильтрации, содержащий общие поля для запроса на получение шутки")
public class JokeQueryCriteria {

    @Parameter(description = "Наименование шутки")
    private StringFilter jokeName;

    @Parameter(description = "Наименование категории")
    private StringFilter categoryName;

}