# HQL Builder - Quick Start

## Быстрый старт для разработчиков

### 1. Базовая настройка

Добавьте библиотеку в проект:

```xml
<dependency>
    <groupId>org.adaptms</groupId>
    <artifactId>hql-builder</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Подготовка сущности

```java
@Entity
public class MyEntity {
    public static DSL root() { return new DSL(null); }
    public static DSL alias(String alias) { return new DSL(alias); }
    
    public static class DSL extends EntityPath {
        private DSL(String previousPath) { super(previousPath); }
        public EntityPath fieldOne() { return addToPrevious("fieldOne"); }
        public EntityPath fieldTwo() { return addToPrevious("fieldTwo"); }
    }
    
    // поля и геттеры/сеттеры
}
```

### 3. Создание запроса

```java
// SELECT запрос
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .column(MyEntity.alias("me").fieldOne())
    .where(Expressions.eq(MyEntity.alias("me").fieldOne(), "value"));

// Получение HQL и параметров
String hql = builder.build();
Map<String, Object> variables = builder.getVariables();
```

### 4. Распространенные сценарии

#### SELECT с JOIN

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .join(JoinType.LEFT, AnotherEntity.class, "ae", 
          Expressions.eq(AnotherEntity.alias("ae").id(), 
                        MyEntity.alias("me").referencedEntity().id()))
    .where(Expressions.eq(AnotherEntity.alias("ae").status(), "ACTIVE"));
```

#### UPDATE

```java
HQLBuilder builder = HQLBuilder.update(MyEntity.class)
    .set(MyEntity.root().fieldOne(), "new value")
    .where(Expressions.eq(MyEntity.root().id(), 123));
```

#### DELETE

```java
HQLBuilder builder = HQLBuilder.delete(MyEntity.class)
    .where(Expressions.isNull(MyEntity.root().fieldOne()));
```

### 5. Дополнительная документация

- [overview.md](overview.md) - Общее описание проекта
- [core-components.md](core-components.md) - Детальное описание компонентов
- [api-examples.md](api-examples.md) - Подробные примеры использования
- [important-notes.md](important-notes.md) - Важные замечания и ограничения

---

**Благодарим за использование HQL Builder!**
