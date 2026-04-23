# HQL Builder - Core Components

## Основные компоненты библиотеки

### 1. DSL (Domain-Specific Language)

DSL позволяет создавать типобезопасные цепочки свойств сущностей.

**Структура DSL для сущности:**

```java
@Entity
public class MyEntity {
    // Точка входа для запросов без алиасов (update и delete)
    public static DSL root() { 
        return new DSL(null); 
    }
    
    // Точка входа для запросов с алиасами (select)
    public static DSL alias(String alias) { 
        return new DSL(alias); 
    }
    
    // Класс для построения цепочки свойств
    public static class DSL extends EntityPath {
        private DSL(String previousPath) { 
            super(previousPath); 
        }
        
        // Простой property descriptor
        public EntityPath fieldOne() { 
            return addToPrevious("fieldOne"); 
        }
        
        // Reference descriptor для связанных сущностей
        public AnotherEntity.DSL referencedEntity() { 
            return AnotherEntity.alias(step("referencedEntity")); 
        }
    }
}
```

**Требования к сущностям:**
- Обязательная аннотация `@Entity`
- Поля сущности переводятся Hibernate в колонки таблицы

### 2. HQLBuilder

Класс для построения запросов с помощью паттерна Builder.

**Типы запросов:**

```java
// SELECT запрос
HQLBuilder selectBuilder = HQLBuilder.select(MyEntity.class, "me");

// UPDATE запрос (без алиасов, использует root())
HQLBuilder updateBuilder = HQLBuilder.update(MyEntity.class);

// DELETE запрос (без алиасов, использует root())
HQLBuilder deleteBuilder = HQLBuilder.delete(MyEntity.class);
```

**Методы HQLBuilder:**

| Метод | Описание | Пример |
|-------|----------|--------|
| `column()` | Добавляет колонку в SELECT | `.column(entity.alias("e").field())` |
| `countColumn()` | Добавляет COUNT колонку | `.countColumn(entity.root(), COUNT)` |
| `where()` | Добавляет условие WHERE | `.where(Expressions.eq(...))` |
| `join()` | Добавляет JOIN | `.join(JoinType.LEFT, ...)` |
| `orderBy()` | Добавляет ORDER BY | `.orderBy(entity.root().field(), ASC)` |
| `groupBy()` | Добавляет GROUP BY | `.groupBy(entity.root().field())` |
| `set()` | Добавляет SET для UPDATE | `.set(entity.root().field(), value)` |
| `build()` | Генерирует итоговый HQL | `.build()` |
| `getVariables()` | Возвращает карту параметров | `.getVariables()` |

### 3. Expressions (Выражения)

Класс для создания условий в WHERE-выражениях.

**Доступные выражения:**

| Выражение | Количество аргументов | Описание |
|-----------|----------------------|----------|
| `eq` | 2 | Равенство |
| `notEq` | 2 | Неравенство |
| `gt` | 2 | Больше |
| `lt` | 2 | Меньше |
| `ge` | 2 | Больше или равно |
| `le` | 2 | Меньше или равно |
| `in` | 2 | Вхождение в список |
| `notIn` | 2 | Невхождение в список |
| `like` | 2 | LIKE-запрос |
| `notLike` | 2 | NOT LIKE-запрос |
| `between` | 3 | BETWEEN |
| `isNull` | 1 | NULL |
| `isNotNull` | 1 | NOT NULL |
| `exists` | 1 | EXISTS |
| `notExists` | 1 | NOT EXISTS |
| `and` | 2+ | Логическое И |
| `or` | 2+ | Логическое ИЛИ |

**Хелперы для LIKE:**

```java
// Оборачивает строку в % для LIKE
Expressions.wrapLike("test")        // "%test%"
Expressions.wrapLike("test", true, true)     // "%test%"
Expressions.wrapLike("test", true, false)    // "%test"
Expressions.wrapLike("test", false, true)    // "test%"
```

### 4. Join Types

Типы JOIN'ов:

- `LEFT` - LEFT JOIN
- `RIGHT` - RIGHT JOIN
- `INNER` - INNER JOIN
- `FULL` - FULL JOIN

### 5. Query Order Direction

Направление сортировки:

- `ASC` - по возрастанию (по умолчанию)
- `DESC` - по убыванию

### 6. Column Expression Types

Типы колонок:

- `DEFAULT` - обычная колонка
- `COUNT` - COUNT колонка
- `DISTINCT` - DISTINCT колонка
- `COUNT_DISTINCT` - COUNT DISTINCT колонка

## Использование

**Пример SELECT запроса с JOIN:**

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .column(MyEntity.alias("me").fieldOne())
    .join(JoinType.LEFT, AnotherEntity.class, "ae", 
          Expressions.eq(AnotherEntity.alias("ae").someField(), 
                        MyEntity.alias("me").fieldOne()))
    .where(Expressions.like(AnotherEntity.alias("ae").someField(), 
                           Expressions.wrapLike("test", false, true)))
    .orderBy(MyEntity.alias("me").fieldTwo())
    .orderBy(MyEntity.alias("me").fieldOne(), QueryOrderDirection.DESC);
```

**Пример UPDATE запроса:**

```java
HQLBuilder builder = HQLBuilder.update(MyEntity.class)
    .set(MyEntity.root().fieldOne(), "new value")
    .where(Expressions.eq(MyEntity.root().fieldOne(), "old value"));
```
