# Анализ документации

## Проблемы документации

### 1. Отсутствие JavaDoc в коде

**Проблема**: Критическое отсутствие JavaDoc комментариев в исходном коде.

**Ситуация**:
- Ни один класс не имеет JavaDoc
- Ни один метод не документирован
- Параметры и возвращаемые значения не описаны

**Пример из EntityPath.java**:
```java
// Нет JavaDoc
public class EntityPath {
    protected String previousPath;
    
    public EntityPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    protected EntityPath addToPrevious(String property) {
        return new EntityPath((getPath() != null ? getPath() + "." : "") + (property != null ? property : ""));
    }
    
    protected String step(String property) {
        return getPath() != null ? getPath() + "." + property : property;
    }
    
    public String getPath() { return previousPath; }
    
    public static EntityPath fromString(String path) {
        return new EntityPath(path);
    }
}
```

**Рекомендация**: Добавить JavaDoc для всех публичных классов и методов:

```java
/**
 * Класс для представления пути к свойству сущности в HQL запросе.
 * Используется в domain-specific language (DSL) для безопасного создания путей
 * свойств сущностей без опечаток.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * public class MyEntity {
 *     public static DSL alias(String alias) { return new DSL(alias); }
 *     
 *     public static class DSL extends EntityPath {
 *         private DSL(String previousPath) { super(previousPath); }
 *         public EntityPath fieldOne() { return addToPrevious("fieldOne"); }
 *     }
 * }
 * }</pre>
 */
public class EntityPath {
    /**
     * Предыдущая часть пути (не включает текущее свойство).
     */
    protected String previousPath;
    
    /**
     * Создает новый путь с заданной предыдущей частью.
     * 
     * @param previousPath предыдущая часть пути или null для начала цепочки
     */
    public EntityPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    /**
     * Добавляет новое свойство к текущему пути.
     * 
     * @param property название свойства для добавления
     * @return новый EntityPath с обновленным путем
     */
    protected EntityPath addToPrevious(String property) {
        // ... реализация
    }
    
    /**
     * Получает полный путь к свойству.
     * 
     * @return полный путь или null если путь пустой
     */
    public String getPath() {
        return previousPath;
    }
    
    /**
     * Создает новый путь из строки.
     * Используется для ручного задания путей без использования DSL.
     * 
     * @param path строковое представление пути (например, "alias.property")
     * @return новый EntityPath
     */
    public static EntityPath fromString(String path) {
        return new EntityPath(path);
    }
}
```

---

### 2. Отсутствие README.md в репозитории

**Проблема**: В README.md есть описание, но оно поверхностное и не содержит:
- Детальных примеров использования
-Troubleshooting секции
- Contributing guidelines
- Architecture overview

**Рекомендация**: Расширить README.md следующими разделами:

```markdown
# HQL Builder - Подробное руководство

## Быстрый старт

### Установка

### Базовое использование

### Продвинутые примеры

## Архитектура

### Основные компоненты

### Паттерны проектирования

##Troubleshooting

## Contributing

## Лицензия
```

---

### 3. Отсутствие API документации

**Проблема**: Нет JavaDoc API документации для публичных интерфейсов.

**Что нужно документировать**:
- Все классы в `org.adaptms.hqlbuilder.builder`
- Все классы в `org.adaptms.hqlbuilder.property`
- Все классы в `org.adaptms.hqlbuilder.expression`
- Интерфейсы и абстрактные классы

**Пример для Expressions.java**:
```java
/**
 * Фабрика для создания выражений WHERE.
 * 
 * <p>Этот класс предоставляет статические методы для создания всех типов
 * выражений, используемых в HQL запросах.
 * 
 * <p>Примеры использования:
 * <pre>{@code
 * // Простое сравнение
 * Expressions.eq(MyEntity.alias("me").field(), "value")
 * 
 * // Сложное выражение с логическими операторами
 * Expressions.or(
 *     Expressions.eq(MyEntity.alias("me").fieldOne(), "value1"),
 *     Expressions.and(
 *         Expressions.like(MyEntity.alias("me").fieldTwo(), Expressions.wrapLike("test")),
 *         Expressions.isNull(MyEntity.alias("me").fieldThree())
 *     )
 * )
 * 
 * // BETWEEN выражение
 * Expressions.between(MyEntity.alias("me").age(), 18, 65)
 * }</pre>
 */
public interface Expressions {
    // ... методы с JavaDoc
}
```

---

### 4. Отсутствие примеров в документации

**Проблема**: В README.md примеры есть, но они не исчерпывающие.

**Рекомендация**: Добавить примеры для всех основных сценариев:

```markdown
## Примеры использования

### 1. Простой SELECT запрос

```java
HQLBuilder builder = HQLBuilder.select(User.class, "u")
    .column(User.alias("u").name())
    .column(User.alias("u").email())
    .where(Expressions.eq(User.alias("u").status(), "ACTIVE"));
```

### 2. SELECT с JOIN

```java
HQLBuilder builder = HQLBuilder.select(Order.class, "o")
    .join(JoinType.LEFT, Customer.class, "c", 
          Expressions.eq(Customer.alias("c").id(), Order.alias("o").customerId()))
    .join(JoinType.INNER, Product.class, "p", 
          Expressions.eq(Product.alias("p").id(), Order.alias("o").productId()))
    .where(Expressions.like(Customer.alias("c").name(), Expressions.wrapLike("John")));
```

### 3. UPDATE запрос

```java
HQLBuilder builder = HQLBuilder.update(User.class)
    .set(User.root().status(), "INACTIVE")
    .where(Expressions.lt(User.root().lastLogin(), Calendar.getInstance().getTime()));
```

### 4. DELETE запрос

```java
HQLBuilder builder = HQLBuilder.delete(User.class)
    .where(Expressions.isNull(User.root().email()));
```

### 5. Сложные выражения

```java
HQLBuilder builder = HQLBuilder.select(Product.class, "p")
    .where(Expressions.or(
        Expressions.and(
            Expressions.ge(Product.alias("p").price(), 100),
            Expressions.le(Product.alias("p").price(), 500)
        ),
        Expressions.in(Product.alias("p").category(), Arrays.asList("electronics", "books"))
    ));
```
```

---

### 5. Отсутствие диаграмм и схем

**Проблема**: Нет визуальных диаграмм архитектуры и связей между компонентами.

**Рекомендация**: Добавить:
- UML диаграмму классов
- Диаграмму последовательности для типичных сценариев
- Диаграмму потоков данных

---

### 6. Отсутствие документации по расширению

**Проблема**: Нет руководства по расширению библиотеки.

**Рекомендация**: Добавить раздел "Extending HQL Builder":

```markdown
## Расширение HQL Builder

### Создание пользовательских выражений

Для создания собственного выражения:

1. Наследуйтесь от `AbstractExpression`
2. Реализуйте метод `build()`
3. Добавьте фабричный метод в `Expressions`

Пример:
```java
public class CustomExpression extends AbstractExpression {
    private final String customCondition;
    
    public CustomExpression(String condition) {
        super(ExpressionType.CUSTOM);
        this.customCondition = condition;
    }
    
    @Override
    public String build() {
        return customCondition;
    }
}

// В Expressions.java
public static CommonWhereExpression custom(String condition) {
    return new CustomExpression(condition);
}
```

### Добавление новых типов запросов

Для добавления нового типа запроса:

1. Добавьте константу в `BuilderMode`
2. Создайте методы в `HQLBuilder`
3. Добавьте обработку в `build()` методе
```

---

### 7. Отсутствие документации по миграции

**Проблема**: Нет руководства по миграции с предыдущих версий.

**Рекомендация**: Добавить CHANGELOG.md и миграционное руководство.

---

### 8. Плохая структура документации в README.md

**Проблема**: README.md длинный и трудный для навигации.

**Рекомендация**: Разбить на секции с оглавлением:

```markdown
# HQL Builder

- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [Examples](#examples)
- [API Reference](#api-reference)
- [Advanced Topics](#advanced-topics)
- [Contributing](#contributing)
```

---

## Рекомендации по улучшению документации

### 1. Добавить Javadoc плагин в Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <outputDirectory>target/site/apidocs</outputDirectory>
        <doclint>all,-missing</doclint>
    </configuration>
</plugin>
```

### 2. Сгенерировать JavaDoc и разместить на GitHub Pages

### 3. Добавить JavaDoc примеры кода

```java
/**
 * @example
 * <pre>{@code
 * HQLBuilder builder = HQLBuilder.select(User.class, "u")
 *     .column(User.alias("u").name())
 *     .where(Expressions.eq(User.alias("u").status(), "ACTIVE"));
 * }</pre>
 */
```

### 4. Создать документацию в Markdown формате

Использовать MkDocs или Docusaurus для генерации сайта документации.

### 5. Добавить видео-туториалы

Создать короткие видео-уроки по основным сценариям использования.
