# Анализ тестов и покрытия

## Проблемы тестирования

### 1. Недостаточное покрытие тестами

**Проблема**: Тесты покрывают только базовые сценарии, около 30-40% покрытия.

**Что покрыто в HQLBuilderTest**:
- Базовый SELECT запрос (1 тест)
- SELECT с колонкой (1 тест)
- SELECT с WHERE (1 тест)
- SELECT с subquery (1 тест)
- SELECT с коллекцией (1 тест)
- AND/OR выражения (1 тест)
- JOIN (2 теста)
- ORDER BY (1 тест)
- DELETE (1 тест)
- UPDATE (1 тест)

**Что НЕ покрыто**:
- Ошибочные сценарии (исключения, валидация)
- граничные случаи (null, пустые коллекции)
- edge cases (очень длинные запросы, много вложенных subquery)
- конкурентный доступ
- производительность
- различные типы выражений (например, between, like с модификаторами)
- все типы JOIN (LEFT, RIGHT, INNER, FULL)
- генерация переменных при сложных сценариях

**Рекомендация**: Добавить минимум 50 дополнительных тестов для достижения 80% покрытия.

---

### 2. Отсутствие тестов на исключения

**Проблема**: Нет тестов на генерацию исключений при некорректных данных.

**Что нужно добавить**:
```java
@Test
void testNullPathInColumnExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new ColumnExpression(null, ColumnExpressionType.DEFAULT));
}

@Test
void testNullTypeInColumnExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new ColumnExpression("field", null));
}

@Test
void testEmptyPathInOrderByExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new OrderByExpression("", QueryOrderDirection.ASC));
}

@Test
void testNullOrderDirection() {
    assertThrows(IllegalArgumentException.class, () -> 
        new OrderByExpression("field", null));
}

@Test
void testEmptyPathInGroupByExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new GroupByExpression(""));
}

@Test
void testNullWhereExpressionInWhereExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new WhereExpression(null, new HQLBuilder()));
}

@Test
void testNullBuilderInWhereExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new WhereExpression(mock(CommonWhereExpression.class), null));
}

@Test
void testNullEntityPathInJoinExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new JoinExpression(JoinType.LEFT, null, "alias", mock(CommonWhereExpression.class)));
}

@Test
void testEmptyEntityPathInJoinExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        new JoinExpression(JoinType.LEFT, "", "alias", mock(CommonWhereExpression.class)));
}

@Test
void testNullTypeInAbstractExpression() {
    assertThrows(NullPointerException.class, () -> 
        new AbstractExpression(null) {});
}

@Test
void testWrongNumberOfArguments() {
    assertThrows(IllegalArgumentException.class, () -> 
        new CommonWhereExpression(ExpressionType.EQ, "onlyOneArg"));
}
```

---

### 3. Отсутствие интеграционных тестов

**Проблема**: Нет тестов, которые проверяют полный цикл работы с реальной ORM.

**Рекомендация**: Добавить интеграционные тесты с встроенной базой данных (H2, Derby):

```java
@SpringBootTest
class IntegrationTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void testFullQueryCycle() {
        // Создаем данные
        TestEntity entity = new TestEntity();
        entity.setFieldOne("test");
        entity.setFieldTwo(123);
        entityManager.persist(entity);
        entityManager.flush();
        
        // Создаем запрос через HQLBuilder
        HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
            .where(Expressions.eq(TestEntity.alias("te").fieldOne(), "test"));
        
        // Выполняем через Hibernate
        Query<TestEntity> query = entityManager.createQuery(builder.build());
        builder.getVariables().forEach(query::setParameter);
        
        List<TestEntity> results = query.getResultList();
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Очистка
        entityManager.remove(entity);
    }
}
```

---

### 4. Отсутствие тестов производительности

**Проблема**: Нет JMH тестов для измерения производительности.

**Рекомендация**: Добавить JMH тесты:

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PerformanceTest {
    
    @Benchmark
    public void testSimpleSelect() {
        HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
            .column(TestEntity.alias("te").fieldOne())
            .column(TestEntity.alias("te").fieldTwo());
        String query = builder.build();
    }
    
    @Benchmark
    public void testComplexSelect() {
        HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
            .column(TestEntity.alias("te").fieldOne())
            .where(Expressions.eq(TestEntity.alias("te").fieldOne(), "test"))
            .where(Expressions.gt(TestEntity.alias("te").fieldTwo(), 100))
            .join(JoinType.LEFT, AnotherEntity.class, "ae", 
                  Expressions.eq(AnotherEntity.alias("ae").id(), TestEntity.alias("te").reference().id()))
            .orderBy(TestEntity.alias("te").fieldOne())
            .orderBy(TestEntity.alias("te").fieldTwo(), QueryOrderDirection.DESC);
        String query = builder.build();
    }
    
    @Benchmark
    public void testDeeplyNestedSubquery() {
        HQLBuilder subquery = HQLBuilder.select(TestEntity.class, "t2")
            .where(Expressions.eq(TestEntity.alias("t2").fieldOne(), "inner"));
        HQLBuilder builder = HQLBuilder.select(TestEntity.class, "t1")
            .where(Expressions.in(TestEntity.alias("t1").fieldOne(), subquery));
        String query = builder.build();
    }
}
```

---

### 5. Отсутствие тестов на конкурентность

**Проблема**: Классы не протестированы на многопоточность.

**Рекомендация**: Добавить тесты для проверки потокобезопасности:

```java
@Test
void testConcurrentBuilderUsage() throws Exception {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te");
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(100);
    
    for (int i = 0; i < 100; i++) {
        final int index = i;
        executor.submit(() -> {
            try {
                builder.where(Expressions.eq(TestEntity.alias("te").fieldOne(), "value" + index));
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    
    // Проверяем, что запрос сгенерировался без ошибок
    String query = builder.build();
    assertNotNull(query);
}
```

---

### 6. Отсутствие тестов на граничные случаи

**Что нужно добавить**:
```java
@Test
void testNullValue() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.isNull(TestEntity.alias("te").fieldOne()));
    String query = builder.build();
    assertTrue(query.contains("is null"));
}

@Test
void testNotNullValue() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.isNotNull(TestEntity.alias("te").fieldOne()));
    String query = builder.build();
    assertTrue(query.contains("is not null"));
}

@Test
void testLikeExpression() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.like(TestEntity.alias("te").fieldOne(), Expressions.wrapLike("test")));
    String query = builder.build();
    assertTrue(query.contains("like"));
}

@Test
void testBetweenExpression() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.between(TestEntity.alias("te").fieldTwo(), 10, 20));
    String query = builder.build();
    assertTrue(query.contains("between"));
}

@Test
void testInExpressionWithList() {
    List<String> values = Arrays.asList("a", "b", "c");
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.in(TestEntity.alias("te").fieldOne(), values));
    String query = builder.build();
    assertTrue(query.contains("in"));
}

@Test
void testExistsExpression() {
    HQLBuilder subquery = HQLBuilder.select(TestEntity.class, "t2")
        .where(Expressions.eq(TestEntity.alias("t2").fieldOne(), "test"));
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "t1")
        .where(Expressions.exists(subquery));
    String query = builder.build();
    assertTrue(query.contains("exists"));
}

@Test
void testNotExistsExpression() {
    HQLBuilder subquery = HQLBuilder.select(TestEntity.class, "t2")
        .where(Expressions.eq(TestEntity.alias("t2").fieldOne(), "test"));
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "t1")
        .where(Expressions.notExists(subquery));
    String query = builder.build();
    assertTrue(query.contains("not exists"));
}

@Test
void testNotInExpression() {
    List<String> values = Arrays.asList("a", "b", "c");
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.notIn(TestEntity.alias("te").fieldOne(), values));
    String query = builder.build();
    assertTrue(query.contains("not in"));
}

@Test
void testNotLikeExpression() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.notLike(TestEntity.alias("te").fieldOne(), Expressions.wrapLike("test")));
    String query = builder.build();
    assertTrue(query.contains("not like"));
}

@Test
void testNotEqExpression() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.notEq(TestEntity.alias("te").fieldOne(), "test"));
    String query = builder.build();
    assertTrue(query.contains("!=") || query.contains("<>"));
}
```

---

### 7. Отсутствие тестов для всех типов JOIN

**Проблема**: Покрыты только LEFT и INNER JOIN, нет RIGHT и FULL.

**Рекомендация**: Добавить тесты:

```java
@Test
void testRightJoin() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .join(JoinType.RIGHT, AnotherEntity.class, "ae", 
              Expressions.eq(AnotherEntity.alias("ae").id(), TestEntity.alias("te").reference().id()));
    String query = builder.build();
    assertTrue(query.contains("right join"));
}

@Test
void testFullJoin() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .join(JoinType.FULL, AnotherEntity.class, "ae", 
              Expressions.eq(AnotherEntity.alias("ae").id(), TestEntity.alias("te").reference().id()));
    String query = builder.build();
    assertTrue(query.contains("full join"));
}

@Test
void testMultipleJoinsOfDifferentTypes() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .join(JoinType.LEFT, AnotherEntity.class, "ae1", 
              Expressions.eq(AnotherEntity.alias("ae1").id(), TestEntity.alias("te").reference().id()))
        .join(JoinType.RIGHT, YetAnotherEntity.class, "yae", 
              Expressions.eq(YetAnotherEntity.alias("yae").id(), TestEntity.alias("te").reference().id()))
        .join(JoinType.INNER, FinalEntity.class, "fe", 
              Expressions.eq(FinalEntity.alias("fe").id(), TestEntity.alias("te").reference().id()));
    String query = builder.build();
    assertTrue(query.contains("left join"));
    assertTrue(query.contains("right join"));
    assertTrue(query.contains("inner join"));
}
```

---

### 8. Отсутствие тестов на обработку переменных

**Проблема**: Нет тестов на корректность генерации имен переменных.

**Рекомендация**: Добавить тесты:

```java
@Test
void testVariableNaming() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.eq(TestEntity.alias("te").fieldOne(), "value1"))
        .where(Expressions.eq(TestEntity.alias("te").fieldTwo(), 123));
    
    Map<String, Object> variables = builder.getVariables();
    assertNotNull(variables);
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("var_te0"));
    assertTrue(variables.containsKey("var_te1"));
}

@Test
void testVariableNamingWithSubquery() {
    HQLBuilder subquery = HQLBuilder.select(TestEntity.class, "t2")
        .where(Expressions.eq(TestEntity.alias("t2").fieldOne(), "value"));
    
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .where(Expressions.in(TestEntity.alias("te").fieldOne(), subquery));
    
    Map<String, Object> variables = builder.getVariables();
    assertNotNull(variables);
    // Проверяем наличие переменных из subquery
    assertTrue(variables.containsKey("var_t20"));
}
```

---

## Рекомендации по улучшению тестирования

### 9. Добавить тестовую инфраструктуру

**Рекомендация**: Создать базовый класс для тестов:

```java
abstract class AbstractHQLBuilderTest {
    
    protected void assertQueryEquals(HQLBuilder builder, String expectedQuery) {
        assertEquals(expectedQuery, builder.build());
    }
    
    protected void assertQueryContains(HQLBuilder builder, String expectedSubstring) {
        assertTrue(builder.build().contains(expectedSubstring));
    }
    
    protected void assertVariablesCount(HQLBuilder builder, int expectedCount) {
        assertEquals(expectedCount, builder.getVariables().size());
    }
    
    @BeforeEach
    void setUp() {
        // Общая настройка для всех тестов
    }
}
```

---

### 10. Добавить тесты документации

**Рекомендация**: Добавить примеры из README.md как тесты:

```java
@Test
void testExampleFromDocumentation() {
    // Пример из README.md
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
        .column(TestEntity.alias("te").fieldOne())
        .where(Expressions.eq(TestEntity.alias("te").fieldOne(), "value"));
    
    String expected = "select te.fieldOne from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te where te.fieldOne = :var_te0";
    assertEquals(expected, builder.build());
}
```
