# Анализ багов и потенциальных проблем

## Критические проблемы

### 1. Отсутствие проверки валидности HQLBuilder после построения

**Проблема**: Нет проверки на корректность комбинации методов построения запроса.

**Пример проблемы**:
```java
// Нет проверки, что update/delete запрос не использует alias
HQLBuilder builder = HQLBuilder.update(TestEntity.class)
    .column(TestEntity.root().fieldOne()); // UPDATE не может иметь колонку!
```

**Рекомендация**: Добавить валидацию в методе `build()`:

```java
public String build() {
    validate();
    // ... генерация запроса
}

private void validate() {
    if (builderMode == BuilderMode.UPDATE || builderMode == BuilderMode.DELETE) {
        if (columns != null && !columns.isEmpty()) {
            throw new IllegalStateException("UPDATE and DELETE queries cannot have columns");
        }
        if (alias != null) {
            throw new IllegalStateException("UPDATE and DELETE queries cannot have alias");
        }
    }
}
```

---

### 2. Уязвимость в processVariable при обработке IBuildable

**Проблема**: Прямая мутация состояния builder при обработке subquery.

**Пример из CommonWhereExpression.java**:
```java
if (input instanceof HQLBuilder) {
    HQLBuilder incomingBuilder = (HQLBuilder) input;
    builder.getVariables().putAll(incomingBuilder.getVariables()); // Проблема!
}
```

**Потенциальная проблема**:
- Если один и тот же HQLBuilder используется в нескольких местах, переменные могут конфликтовать
- Нет проверки на дубликаты имен переменных
- Порядок добавления переменных может повлиять на итоговый запрос

**Рекомендация**: 
1. Добавить проверку дубликатов
2. Использовать префиксы для переменных из subquery
3. Сделать обработку более изолированной

```java
protected String processVariable(Object input, HQLBuilder builder) {
    if (input instanceof HQLBuilder) {
        HQLBuilder incomingBuilder = (HQLBuilder) input;
        // Генерируем уникальный префикс для subquery
        String subqueryPrefix = "sub_" + System.nanoTime() + "_";
        // Копируем переменные с префиксом
        incomingBuilder.getVariables().forEach((name, value) -> 
            builder.addVariable(value, subqueryPrefix + name));
        return "(" + incomingBuilder.build() + ")";
    }
    // ... остальная логика
}
```

---

### 3. Неполная поддержка типа INSERT

**Проблема**: В `BuilderMode.java` закомментирована константа `INSERT`, но интерфейс и документация не упоминают ограничение.

**Пример из BuilderMode.java**:
```java
public enum BuilderMode {
    SELECT("from", true),
    // INSERT("into", false), // ЗАКОММЕНТИРОВАНО
    UPDATE(null, false),
    DELETE(null, false);
}
```

**Рекомендация**: 
1. Или реализовать INSERT (хотя HQL ограничивает)
2. Или добавить явное исключение при попытке создания INSERT запроса
3. Обновить документацию с упоминанием этого ограничения

---

### 4. Нет проверки на дубликаты колонок в SELECT

**Проблема**: Можно добавить одну и ту же колонку несколько раз.

**Пример**:
```java
HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
    .column(TestEntity.alias("te").fieldOne())
    .column(TestEntity.alias("te").fieldOne()); // Дубликат!
```

**Рекомендация**: Добавить проверку уникальности колонок при добавлении.

---

## Ошибки валидации

### 5. Неправильная валидация в ColumnExpression

**Проблема**: Проверка `null == type` вместо `null == columnExpressionType`.

**Пример из ColumnExpression.java**:
```java
public ColumnExpression(String path, ColumnExpressionType type) {
    super(ExpressionType.COLUMN);
    
    if (null == path || path.isEmpty()) throw new IllegalArgumentException("Path may not be empty.");
    if (null == type) throw new IllegalArgumentException("ColumnExpressionType may not be empty."); // Валидация OK
    // ...
}
```

**Проблема**: Валидация вроде правильная, но не хватает проверки на `null` для `path`.

**Рекомендация**: Добавить валидацию на `null` для всех обязательных параметров.

---

### 6. Отсутствие проверки на null при создании выражений

**Проблема**: Многие методы в `Expressions.java` не проверяют входные параметры на null.

**Пример**:
```java
public static CommonWhereExpression eq(Object first, Object second) {
    return new CommonWhereExpression(ExpressionType.EQ, first, second);
    // Нет проверки, что first и second не null
}
```

**Рекомендация**: Добавить валидацию для обязательных параметров:

```java
public static CommonWhereExpression eq(Object first, Object second) {
    if (first == null && second == null) {
        throw new IllegalArgumentException("At least one argument must be non-null for EQ expression");
    }
    return new CommonWhereExpression(ExpressionType.EQ, first, second);
}
```

---

### 7. Нет проверки на пустой alias в JOIN

**Проблема**: В `JoinExpression.java` проверяется только `joinEntityClass`, но не `joinEntityAlias`.

**Пример из JoinExpression.java**:
```java
if (null == joinEntityClass || joinEntityClass.isEmpty()) 
    throw new IllegalArgumentException("Join entity class may not be empty.");
// А alias может быть null или пустым!
```

**Рекомендация**: Добавить проверку на alias:

```java
if (null == joinEntityAlias || joinEntityAlias.isEmpty()) 
    throw new IllegalArgumentException("Join entity alias may not be empty.");
```

---

### 8. Проблемы с именами переменных при повторном использовании

**Проблема**: Переменные генерируются как `:var_mte0`, `:var_mte1`, но если builder переиспользуется, могут возникнуть конфликты.

**Рекомендация**: Сбросить счетчик переменных при создании нового builder или использовать уникальные идентификаторы для каждого builder.

---

### 9. Отсутствие проверки на циклические зависимости в subquery

**Проблема**: Можно создать бесконечный subquery, который приведет к StackOverflowError.

**Пример**:
```java
HQLBuilder builder = HQLBuilder.select(TestEntity.class, "te")
    .where(Expressions.eq(TestEntity.alias("te").fieldOne(), 
        HQLBuilder.select(TestEntity.class, "te2")
            .where(Expressions.eq(TestEntity.alias("te2").fieldOne(), builder)) // Цикл!
    ));
```

**Рекомендация**: Добавить проверку глубины вложенности subquery.

---

## Документированные ограничения

### 10. Ограничения HQL

**Проблема**: Некоторые возможности HQL не реализованы, но не документированы.

**Нет поддержки**:
- WITH queries (CTE)
- Window functions
- Subquery in SELECT clause (только в WHERE)
- Многие специфические функции HQL

**Рекомендация**: Добавить документацию в README.md и JavaDoc о поддерживаемых и неподдерживаемых функциях HQL.
