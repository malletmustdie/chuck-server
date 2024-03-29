package ru.elias.server.service.impl.unit;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import ru.elias.server.client.JokeReactiveClient;
import ru.elias.server.dto.JokeDto;
import ru.elias.server.dto.JokesGeneralStatistic;
import ru.elias.server.exception.BusinessException;
import ru.elias.server.exception.ErrorType;
import ru.elias.server.filter.JokeQueryCriteria;
import ru.elias.server.filter.base.StringFilter;
import ru.elias.server.filter.common.CommonBooleanBuilder;
import ru.elias.server.mapper.JokeMapper;
import ru.elias.server.model.Category;
import ru.elias.server.model.Joke;
import ru.elias.server.repository.CategoryRepository;
import ru.elias.server.repository.JokeQueryCustomRepository;
import ru.elias.server.repository.JokeRepository;
import ru.elias.server.service.MessageSourceHelper;
import ru.elias.server.service.impl.JokeServiceImpl;

@ExtendWith(MockitoExtension.class)
class JokeServiceImplTest {

    @Mock
    private JokeRepository jokeRepository;

    @Mock
    private JokeQueryCustomRepository jokeQueryCustomRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private JokeMapper jokeMapper;

    @Mock
    private JokeReactiveClient jokeClient;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @Mock
    private CommonBooleanBuilder commonBooleanBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JokeServiceImpl jokeService;

    @Test
    void whenCreateJokeWithAutoModeThenCreateJoke() throws JsonProcessingException {
        var mockedCategory = Category.builder().name("some-cat").build();
        var jokeName = Mono.just(mockedCategory.getName());
        when(categoryRepository.findCategoryByName(ArgumentMatchers.anyString()))
               .thenReturn(Optional.of(mockedCategory));
        when(jokeClient.getRandomJokeByCategory(ArgumentMatchers.anyString()))
               .thenReturn(jokeName);
        mockObjectMapper();
        var actual = jokeService.createJoke(true, mockedCategory.getName(), null);
        assertThat(actual.getBody()).isNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(jokeRepository, Mockito.times(1)).save(ArgumentMatchers.any(Joke.class));
        verify(jokeRepository, Mockito.times(1)).exists(ArgumentMatchers.any(Predicate.class));
        verifyNoMoreInteractions();
    }

    @Test
    void whenCreateJokeWithAutoModeThenNotCreateJokeAndThrowBusinessExeption() {
        var mockedCategory = Category.builder().name("some-cat").build();
        when(categoryRepository.findCategoryByName(ArgumentMatchers.anyString()))
               .thenReturn(Optional.empty());
        when(messageSourceHelper.getMessage(ArgumentMatchers.any(ErrorType.class), ArgumentMatchers.any()))
               .thenReturn(ErrorType.CATEGORY_NOT_FOUND_BY_NAME.getMessage());
        assertThatThrownBy(() -> jokeService.createJoke(true, mockedCategory.getName(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorType.CATEGORY_NOT_FOUND_BY_NAME.getMessage());
        verify(jokeRepository, Mockito.never()).save(ArgumentMatchers.any(Joke.class));
        verify(jokeRepository, Mockito.never()).exists(ArgumentMatchers.any(Predicate.class));
        verifyNoMoreInteractions();
    }

    @Test
    void whenCreateJokeWithManualMode() {
        var mockedCategory = Category.builder().name("some-cat").build();
        var mockedJoke = Joke.builder().name("some-joke").category(mockedCategory).build();
        when(jokeMapper.map(ArgumentMatchers.any(JokeDto.class)))
               .thenReturn(mockedJoke);
        var dto = JokeDto.builder().joke(mockedJoke.getName()).category(mockedCategory.getName()).build();
        var actual = jokeService.createJoke(false, null, dto);
        assertThat(actual.getBody()).isNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(jokeMapper, Mockito.times(1)).map(ArgumentMatchers.any(JokeDto.class));
        verify(jokeRepository, Mockito.times(1)).save(ArgumentMatchers.any(Joke.class));
        verifyNoMoreInteractions();
    }

    @Test
    void whenGetRandomJokeThenReturnJokeDto() {
        var mockedCategory = Category.builder().name("some-cat").build();
        var mockedJoke = Joke.builder().name("some-joke").category(mockedCategory).build();
        var mockedJokeDto = JokeDto.builder().joke("some-joke").category("some-cat").build();
        when(jokeMapper.map(ArgumentMatchers.any(Joke.class))).thenReturn(mockedJokeDto);
        when(jokeRepository.findRandomJoke()).thenReturn(mockedJoke);
        var actual = jokeService.getRandomJoke();
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().getJoke()).isEqualTo(mockedJokeDto.getJoke());
        assertThat(actual.getBody().getCategory()).isEqualTo(mockedJokeDto.getCategory());
        verify(jokeRepository, Mockito.times(1)).findRandomJoke();
        verify(jokeMapper, Mockito.times(1)).map(ArgumentMatchers.any(Joke.class));
        verifyNoMoreInteractions();
    }

    @Test
    void whenGetRandomJokeByCategoryThenReturnJokeDto() {
        var mockedCategory = Category.builder().id(1L).name("some-cat").build();
        var mockedJoke = Joke.builder().name("some-joke").category(mockedCategory).build();
        var mockedJokeDto = JokeDto.builder().joke("some-joke").category("some-cat").build();
        when(categoryRepository.findCategoryByName(ArgumentMatchers.anyString()))
               .thenReturn(Optional.of(mockedCategory));
        when(jokeRepository.findRandomJokeByCategoryId(ArgumentMatchers.anyLong()))
                .thenReturn(mockedJoke);
        when(jokeMapper.map(ArgumentMatchers.any(Joke.class)))
                .thenReturn(mockedJokeDto);
        var actual = jokeService.getRandomJokeByCategory("some-cat");
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().getJoke()).isEqualTo(mockedJokeDto.getJoke());
        assertThat(actual.getBody().getCategory()).isEqualTo(mockedJokeDto.getCategory());
        verify(categoryRepository, Mockito.times(1)).findCategoryByName(ArgumentMatchers.anyString());
        verify(jokeRepository, Mockito.times(1)).findRandomJokeByCategoryId(ArgumentMatchers.anyLong());
        verify(jokeMapper, Mockito.times(1)).map(ArgumentMatchers.any(Joke.class));
        verifyNoMoreInteractions();
    }

    @Test
    void whenGetJokesCountStatisticsThenReturnDtoWithStatistic() {
        Long count = 255L;
        var expected = List.of(
                JokesGeneralStatistic.builder().name("some-cat-1").jokesCount(count).build(),
                JokesGeneralStatistic.builder().name("some-cat-2").jokesCount(count).build(),
                JokesGeneralStatistic.builder().name("some-cat-3").jokesCount(count).build()
        );
        when(jokeQueryCustomRepository.countByCategories()).thenReturn(expected);
        var actual = jokeService.getJokesCountStatistics();
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody()).hasSize(expected.size());
        assertThat(actual.getBody())
                .filteredOn(stat -> stat.getJokesCount().equals(count))
                .hasSize(3);
        assertThat(actual.getBody())
                .map(JokesGeneralStatistic::getName)
                .contains(expected.get(0).getName());
        assertThat(actual.getBody())
                .map(JokesGeneralStatistic::getName)
                .contains(expected.get(1).getName());
        assertThat(actual.getBody())
                .map(JokesGeneralStatistic::getName)
                .contains(expected.get(2).getName());
        verify(jokeQueryCustomRepository, Mockito.times(1)).countByCategories();
        verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Получение шуток из замоканной бд по критерии. "
            + "Ожидаемый результат - Лист Dto'шек с шуткой и категорией")
    void whenGetRandomJokeByCriteriaThenReturnDtoList() {
        var cat = Category.builder().name("some-cat").build();
        var mockedList = List.of(
                Joke.builder().name("some-joke").category(cat).build(),
                Joke.builder().name("some-joke").category(cat).build(),
                Joke.builder().name("some-joke").category(cat).build()
        );
        var mockedJokeDto = JokeDto.builder().joke("some-joke").category("some-cat").build();
        when(jokeQueryCustomRepository.findJokesByPredicate(ArgumentMatchers.any(Predicate.class)))
               .thenReturn(mockedList);
        when(jokeMapper.map(ArgumentMatchers.any(Joke.class)))
               .thenReturn(mockedJokeDto);
        var actual = jokeService.getRandomJokeByCriteria(getCriteria());
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody()).hasSize(3);
        assertThat(actual.getBody())
                .filteredOn(joke -> joke.getCategory().equals(mockedJokeDto.getCategory()))
                .hasSize(3);
        assertThat(actual.getBody())
                .flatExtracting(JokeDto::getJoke)
                .containsExactlyInAnyOrder(mockedList.stream()
                                                     .map(Joke::getName)
                                                     .toArray(String[]::new));
        verify(jokeQueryCustomRepository, Mockito.times(1))
               .findJokesByPredicate(ArgumentMatchers.any(Predicate.class));
        verify(jokeMapper, Mockito.times(3)).map(ArgumentMatchers.any(Joke.class));
        verify(commonBooleanBuilder, Mockito.times(2))
               .andMatchStringFilter(ArgumentMatchers.any(),
                                     ArgumentMatchers.any(),
                                     ArgumentMatchers.any());
        verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Получение шутки из замоканной бд по идентификатору. "
            + "Ожидаемый результат - Dto с шуткой и категорией")
    void whenGetJokeByIdThenReturnJokeDto() {
        var mockedCategory = Category.builder().id(1L).name("some-cat").build();
        var mockedJoke = Joke.builder().name("some-joke").category(mockedCategory).build();
        var mockedJokeDto = JokeDto.builder().joke("some-joke").category("some-cat").build();
        when(jokeRepository.findOne(ArgumentMatchers.any(Predicate.class)))
                .thenReturn(Optional.of(mockedJoke));
        when(jokeMapper.map(ArgumentMatchers.any(Joke.class)))
               .thenReturn(mockedJokeDto);
        var actual = jokeService.getJokeById(1L);
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().getJoke()).isEqualTo(mockedJokeDto.getJoke());
        assertThat(actual.getBody().getCategory()).isEqualTo(mockedJokeDto.getCategory());
        verify(jokeRepository, Mockito.times(1)).findOne(ArgumentMatchers.any(Predicate.class));
        verify(jokeMapper, Mockito.times(1)).map(ArgumentMatchers.any(Joke.class));
        verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Получение шутки из замоканной бд по идентификатору. "
            + "Ожидаемый результат - Выбрасывание BusinessException из-за не найденной шутки")
    void whenGetJokeByIdThenThrowBusinessException() {
        when(jokeRepository.findOne(ArgumentMatchers.any(Predicate.class)))
                .thenReturn(Optional.empty());
        when(messageSourceHelper.getMessage(ArgumentMatchers.any(ErrorType.class), ArgumentMatchers.any()))
               .thenReturn(ErrorType.JOKE_NOT_FOUND_BY_ID.getMessage());
        assertThatThrownBy(() -> jokeService.getJokeById(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorType.JOKE_NOT_FOUND_BY_ID.getMessage());
        verify(jokeRepository, Mockito.times(1)).findOne(ArgumentMatchers.any(Predicate.class));
        verify(jokeMapper, Mockito.never()).map(ArgumentMatchers.any(Joke.class));
        verifyNoMoreInteractions();
    }

    private void mockObjectMapper() throws JsonProcessingException {
        JsonNode mockNode = Mockito.mock(JsonNode.class);
        JsonNode innerMockNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readValue(ArgumentMatchers.anyString(), ArgumentMatchers.any(Class.class)))
               .thenReturn(mockNode);
        when(mockNode.get("value")).thenReturn(innerMockNode);
        when(innerMockNode.asText()).thenReturn("some-joke");
    }

    private void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                jokeRepository,
                jokeQueryCustomRepository,
                categoryRepository,
                jokeMapper,
                jokeClient,
                messageSourceHelper,
                commonBooleanBuilder
        );
    }

    private JokeQueryCriteria getCriteria() {
        StringFilter filter = new StringFilter();
        filter.setEndWith("some-value");
        filter.setDoesntContains("some-value");
        filter.setNonEmpty(true);
        var criteria = new JokeQueryCriteria();
        criteria.setJokeName(filter);
        criteria.setCategoryName(filter);
        return criteria;
    }

}