# HQL Builder - API Examples

## Примеры использования библиотеки

### 1. Простой SELECT запрос

```java
// Создание запроса без алиасов (использует root())
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .column(MyEntity.alias("me").fieldOne())
    .column(MyEntity.alias("me").fieldTwo());

// Результат: 
// select me.fieldOne, me.fieldTwo from MyEntity me
```

### 2. SELECT с фильтрацией

```java
String filterValue = "test";

HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.eq(MyEntity.alias("me").fieldOne(), filterValue));

// Результат:
// select me from MyEntity me where me.fieldOne = :var_me0
```

### 3. SELECT с JOIN

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .join(JoinType.LEFT, AnotherEntity.class, "ae", 
          Expressions.eq(AnotherEntity.alias("ae").someField(), 
                        MyEntity.alias("me").fieldOne()))
    .join(JoinType.INNER, YetAnotherEntity.class, "yae", 
          Expressions.eq(YetAnotherEntity.alias("yae").one(), 
                        MyEntity.alias("me").fieldOne()))
    .where(Expressions.like(AnotherEntity.alias("ae").someField(), 
                           Expressions.wrapLike("test", false, true)));

// Результат:
// select me from MyEntity me 
// left join AnotherEntity ae on ae.someField = me.fieldOne 
// inner join YetAnotherEntity yae on yae.one = me.fieldOne 
// where ae.someField like 'test%'
```

### 4. COUNT запрос

```java
HQLBuilder countBuilder = HQLBuilder.select(MyEntity.class, "me")
    .countColumn(MyEntity.alias("me"), ColumnExpressionType.COUNT)
    .where(Expressions.gt(MyEntity.alias("me").id(), 123));

// Результат:
// select count(me) from MyEntity me where me.id > :var_me0
```

### 5. Сложная фильтрация с логическими операторами

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.or(
        Expressions.eq(MyEntity.alias("me").fieldOne(), "one"),
        Expressions.and(
            Expressions.like(MyEntity.alias("me").fieldTwo(), Expressions.wrapLike("test wrap")),
            Expressions.eq(MyEntity.alias("me").referencedEntity().someField(), "three")
        )
    ));

// Результат:
// select me from MyEntity me 
// where me.fieldOne = 'one' or 
// (me.fieldTwo like '%test wrap%' and me.referencedEntity.someField = 'three')
```

### 6. SELECT с подзапросом

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.in(MyEntity.alias("me").fieldOne(), 
                        HQLBuilder.select(AnotherEntity.class, "ae")
                            .column(AnotherEntity.alias("ae").someField())
                            .where(Expressions.gt(AnotherEntity.alias("ae").id(), 123))));

// Результат:
// select me from MyEntity me 
// where me.fieldOne in (select ae.someField from AnotherEntity ae where ae.id > :var_ae0)
```

### 7. UPDATE запрос

```java
HQLBuilder builder = HQLBuilder.update(MyEntity.class)
    .set(MyEntity.root().fieldOne(), "new value")
    .where(Expressions.eq(MyEntity.root().fieldOne(), "old value"));

// Результат:
// update MyEntity set fieldOne = :var_fieldOne0 
// where fieldOne = :var_fieldOne1
```

### 8. DELETE запрос

```java
HQLBuilder builder = HQLBuilder.delete(MyEntity.class)
    .where(Expressions.isNull(MyEntity.root().reference()));

// Результат:
// delete MyEntity where reference is null
```

### 9. ORDER BY и GROUP BY

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.like(MyEntity.alias("me").fieldTwo(), Expressions.wrapLike("test")))
    .orderBy(MyEntity.alias("me").fieldTwo())  // ASC по умолчанию
    .orderBy(MyEntity.alias("me").fieldOne(), QueryOrderDirection.DESC)
    .groupBy(MyEntity.alias("me").fieldOne());

// Результат:
// select me from MyEntity me 
// where me.fieldTwo like '%test%' 
// group by me.fieldOne 
// order by me.fieldTwo asc, me.fieldOne desc
```

### 10. Ручное создание путей (без DSL)

```java
// Если нельзя модифицировать сущности, можно использовать EntityPath.fromString()
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.eq(EntityPath.fromString("me.fieldOne"), "value"));

// Результат:
// select me from MyEntity me where me.fieldOne = :var_me0
```

### 11. Интеграция с Hibernate Session

```java
// В DAO классе
protected <T> Query<T> createQuery(HQLBuilder builder) {
    Query<T> query = sessionFactory.getCurrentSession().createQuery(builder.build());
    if (builder.getVariables() != null) {
        for (String name : builder.getVariables().keySet()) {
            query.setParameter(name, builder.getVariables().get(name));
        }
    }
    return query;
}

// Использование
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.eq(MyEntity.alias("me").fieldOne(), "value"));
    
Query<MyEntity> query = createQuery(builder);
List<MyEntity> results = query.getResultList();
```

### 12. Комбинированные выражения

```java
HQLBuilder builder = HQLBuilder.select(MyEntity.class, "me")
    .where(Expressions.and(
        Expressions.ge(MyEntity.alias("me").age(), 18),
        Expressions.le(MyEntity.alias("me").age(), 65),
        Expressions.or(
            Expressions.eq(MyEntity.alias("me").status(), "ACTIVE"),
            Expressions.eq(MyEntity.alias("me").status(), "PENDING")
        )
    ));

// Результат:
// select me from MyEntity me 
// where me.age >= :var_age0 
// and me.age <= :var_age1 
// and (me.status = 'ACTIVE' or me.status = 'PENDING')
```
