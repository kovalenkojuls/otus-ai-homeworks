# Анализ качества кода и читаемости

## Проблемы читаемости и поддержки

### 1. Неполное документирование JavaDoc

**Проблема**: Большинство классов и методов не имеют JavaDoc комментариев.

**Пример из EntityPath.java**:
```java
public class EntityPath {
    protected String previousPath;
    
    public EntityPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    protected EntityPath addToPrevious(String property) {
        return new EntityPath((getPath() != null ? getPath() + "." : "") + (property != null ? property : ""));
    }
    
    // ... нет JavaDoc
}
```

**Рекомендация**: Добавить JavaDoc для всех публичных и защищенных методов:

```java
/**
 * Класс для представления пути к свойству сущности в HQL запросе.
 * Используется в domain-specific language (DSL) для безопасного создания путей.
 */
public class EntityPath {
    /**
     * Предыдущая часть пути (не включает текущее свойство).
     */
    protected String previousPath;
    
    /**
     * Создает новый путь с заданной предыдущей частью.
     * 
     * @param previousPath предыдущая часть пути или null для начала
     */
    public EntityPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    /**
     * Добавляет новое свойство к текущему пути.
     * 
     * @param property название свойства
     * @return новый EntityPath с обновленным путем
     */
    protected EntityPath addToPrevious(String property) {
        // ... реализация
    }
    
    /**
     * Получает полный путь к свойству.
     * 
     * @return полный путь или null
     */
    public String getPath() {
        return previousPath;
    }
}
```

---

### 2. Смешение логики и побочных эффектов

**Проблема**: Методы инициализации имеют побочные эффекты, что затрудняет отладку.

**Пример из CommonWhereExpression.java**:
```java
public void init(HQLBuilder builder) {
    if (arguments == null) return;
    
    if (getType().getNumberOfArguments() != getArguments().length)
        throw new IllegalArgumentException(...);
    
    this.querySubstitutes = new String[getArguments().length];
    
    for (int i = 0; i < getQuerySubstitutes().length; i++) {
        this.getQuerySubstitutes()[i] = processVariable(getArguments()[i], builder);
    }
}
```

**Проблемы**:
- Метод `init` модифицирует состояние объекта
- Сложная логика обработки переменных скрыта в `processVariable`
- Тяжело протестировать изолированно

**Рекомендация**: Разделить логику инициализации от генерации:

```java
public class CommonWhereExpression {
    private Object[] arguments;
    private String[] processedArgs;
    private boolean initialized = false;
    
    public synchronized void init(HQLBuilder builder) {
        if (initialized) return;
        
        this.processedArgs = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            processedArgs[i] = processVariable(arguments[i], builder);
        }
        initialized = true;
    }
    
    public String build() {
        if (!initialized) {
            throw new IllegalStateException("Expression not initialized");
        }
        return MessageFormat.format(getType().getTemplate(), processedArgs);
    }
}
```

---

### 3. Плохая обработка null значений

**Проблема**: Многие методы не обрабатывают null значения явно.

**Пример из Expressions.java**:
```java
public static CommonWhereExpression eq(Object first, Object second) {
    return new CommonWhereExpression(ExpressionType.EQ, first, second);
    // Что произойдет, если first или second будут null?
}
```

**Проблемные методы**:
- `eq`, `notEq`, `gt`, `lt`, `ge`, `le` - сравнение с null не определено в HQL
- `in`, `notIn` - null в коллекции может вызвать проблемы
- `like`, `between` - null значения не допустимы

**Рекомендация**: Добавить явные проверки или JavaDoc о поведении при null:

```java
/**
 * Создает выражение равенства.
 * 
 * @param first первое значение (не может быть null для HQL comparison)
 * @param second второе значение (не может быть null для HQL comparison)
 * @return выражение равенства
 * @throws IllegalArgumentException если оба аргумента null
 */
public static CommonWhereExpression eq(Object first, Object second) {
    if (first == null && second == null) {
        throw new IllegalArgumentException("Both arguments cannot be null for EQ expression");
    }
    return new CommonWhereExpression(ExpressionType.EQ, first, second);
}
```

---

### 4. Непонятные имена переменных

**Проблема**: Некоторые переменные имеют неинформативные имена.

**Пример из SetExpression.java**:
```java
public class SetExpression extends CommonWhereExpression {
    private final String path;
    private final Object value;
    private String variableName;
    // ...
}
```

**Рекомендация**: Использовать более понятные имена:

```java
public class SetExpression extends CommonWhereExpression {
    private final String propertyPath;
    private final Object newValue;
    private String variableName;
    // ...
}
```

---

### 5. Смешение ответственности в классах

**Проблема**: Классы выполняют несколько задач, что нарушает принцип единственной ответственности.

**Пример из CommonWhereExpression**:
- Хранение аргументов выражения
- Инициализация переменных
- Обработка IBuildable объектов
- Манипуляция с HQLBuilder

**Рекомендация**: Разделить на более мелкие классы:

```java
public class CommonWhereExpression extends AbstractExpression {
    private final Object[] arguments;
    
    // Ответственность за генерацию выражения
    @Override
    public String build() {
        // ...
    }
}

public class ExpressionValidator {
    // Ответственность за валидацию выражений
    public void validate(CommonWhereExpression expression) {
        // ...
    }
}

public class ExpressionProcessor {
    // Ответственность за обработку переменных
    public void processVariables(CommonWhereExpression expression, HQLBuilder builder) {
        // ...
    }
}
```

---

### 6. Неполные тесты

**Проблема**: Тесты покрывают только базовые сценарии, нет тестов на граничные случаи.

**Что покрыто**:
- Базовый SELECT запрос
- WHERE с простыми выражениями
- JOIN
- ORDER BY
- UPDATE и DELETE

**Что не покрыто**:
- Обработка null значений
- Пустые коллекции
- Глубокая вложенность subquery
- Множественные выражения одного типа
- Граничные случаи валидации
- Потокобезопасность

**Рекомендация**: Добавить тесты для граничных случаев:

```java
@Test
void testNullValueInEqExpression() {
    assertThrows(IllegalArgumentException.class, () -> 
        Expressions.eq(null, null));
}

@Test
void testEmptyCollectionInInExpression() {
    List<String> emptyList = new ArrayList<>();
    assertThrows(IllegalArgumentException.class, () -> 
        Expressions.in(path, emptyList));
}

@Test
void testDeeplyNestedSubquery() {
    // Тест на глубину вложенности
}

@Test
void testDuplicateVariables() {
    // Тест на дубликаты переменных
}
```

---

### 7. Отсутствие тестов на производительность

**Проблема**: Нет тестов на производительность при большом количестве выражений.

**Рекомендация**: Добавить JMH тесты для измерения производительности:

```java
@Benchmark
public void testLargeQuery() {
    HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te");
    for (int i = 0; i < 100; i++) {
        builder.where(Expressions.eq(TestEntity.alias("te").fieldOne(), i));
    }
    String query = builder.build();
}
```

---

## Улучшения для читаемости

### 8. Слишком длинные строки в JavaDoc

**Проблема**: Некоторые JavaDoc строки слишком длинные и трудны для чтения.

**Рекомендация**: Разбивать длинные строки на несколько:

```java
/**
 * Создает выражение WHERE с оператором LIKE.
 * Используется для поиска по шаблону в базе данных.
 * 
 * @param path путь к свойству для сравнения
 * @param pattern шаблон поиска (можно использовать Expressions.wrapLike())
 * @return выражение LIKE
 */
public static CommonWhereExpression like(Object path, Object pattern) {
    return new CommonWhereExpression(ExpressionType.LIKE, path, pattern);
}
```

---

### 9. Отсутствие примеров использования в JavaDoc

**Рекомендация**: Добавить примеры в JavaDoc:

```java
/**
 * Создает выражение BETWEEN.
 * 
 * @param path путь к свойству
 * @param start начало диапазона
 * @param end конец диапазона
 * @return выражение BETWEEN
 * 
 * @example
 * <pre>
 * Expressions.between(
 *     TestEntity.alias("te").age(), 
 *     18, 
 *     65
 * )
 * // Результат: te.age between :var0 and :var1
 * </pre>
 */
public static CommonWhereExpression between(Object path, Object start, Object end) {
    return new CommonWhereExpression(ExpressionType.BETWEEN, path, start, end);
}
```

---

### 10. Непоследовательный стиль кодирования

**Проблема**: Разные подходы к обработке ошибок и валидации.

**Примеры**:
- В одном месте: `if (null == path || path.isEmpty()) throw ...`
- В другом: `if (path == null) throw ...`
- В третьем: валидация отсутствует

**Рекомендация**: Стандартизировать валидацию:

```java
protected void requireNonNull(Object value, String name) {
    if (value == null) {
        throw new IllegalArgumentException(name + " may not be null");
    }
}

protected void requireNotBlank(String value, String name) {
    if (value == null || value.isEmpty()) {
        throw new IllegalArgumentException(name + " may not be empty");
    }
}
```
