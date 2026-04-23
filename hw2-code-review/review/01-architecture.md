# Анализ архитектуры и дизайна кода

## Архитектурные проблемы и замечания

### 1. Отсутствие инкапсуляции данных в классах выражений

**Проблема**: В классах выражений (например, `SetExpression`, `ColumnExpression`, `OrderByExpression`) поля объявлены как `private`, но для доступа в дочерних классах используются `protected` геттеры, что нарушает принцип инкапсуляции.

**Пример из SetExpression.java**:
```java
public class SetExpression extends CommonWhereExpression {
    private final String path;  // private
    private final Object value; // private
    private String variableName; // private

    // но для доступа используются protected методы:
    protected Object getValue() { return value; }
    protected String getVariableName() { return variableName; }
}
```

**Рекомендация**: Если классы являются частью наследования, объявляйте поля как `protected`, либо используйте `private` с `protected` геттерами только в том случае, если геттеры добавляют логику.

---

### 2. Повторяющаяся логика в классах выражений

**Проблема**: Многие классы выражений имеют одинаковую структуру и повторяющийся код инициализации.

**Примеры**:
- `OrderByExpression.java` и `GroupByExpression.java` имеют схожую логику валидации
- `AndExpression.java` и `OrExpression.java` имеют идентичную структуру инициализации

**Рекомендация**: Рассмотреть создание абстрактного базового класса для выражений с двумя и более аргументами:

```java
public abstract class MultiArgumentExpression extends AbstractExpression {
    protected final CommonWhereExpression[] expressions;
    
    protected MultiArgumentExpression(ExpressionType type, CommonWhereExpression... expressions) {
        super(type);
        if (expressions == null || expressions.length < type.getNumberOfArguments()) {
            throw new IllegalArgumentException(...);
        }
        this.expressions = expressions;
    }
    
    @Override
    public void init(HQLBuilder builder) {
        for (CommonWhereExpression expression : this.expressions) {
            expression.init(builder);
        }
    }
}
```

---

### 3. Недостаточное использование полиморфизма

**Проблема**: В `Expressions.java` создание выражений происходит через статические методы, что не позволяет легко расширять функциональность без изменения исходного кода.

**Пример**:
```java
public static CommonWhereExpression eq(Object first, Object second) {
    return new CommonWhereExpression(ExpressionType.EQ, first, second);
}
```

**Рекомендация**: Рассмотреть паттерн "Фабричный метод" или "Строитель" для более гибкого создания выражений:

```java
public class ExpressionFactory {
    public CommonWhereExpression createEqual(Object first, Object second) {
        return new CommonWhereExpression(ExpressionType.EQ, first, second);
    }
    
    // Можно расширить для создания сложных выражений
    public CommonWhereExpression createComplexCondition(...) {
        // логика создания комплексного выражения
    }
}
```

---

### 4. Смешение ответственности в CommonWhereExpression

**Проблема**: Класс `CommonWhereExpression` выполняет слишком много функций:
- Управление аргументами выражения
- Обработка переменных и подстановка
- Связь с HQLBuilder
- Манипуляция с переменными

**Пример из processVariable**:
```java
protected String processVariable(Object input, HQLBuilder builder) {
    if (input instanceof EntityPath) {
        return ((EntityPath) input).getPath();
    } else if (input instanceof IBuildable) {
        if (input instanceof HQLBuilder) {
            HQLBuilder incomingBuilder = (HQLBuilder) input;
            builder.getVariables().putAll(incomingBuilder.getVariables()); // Прямая мутация!
        }
        return "(" + ((IBuildable) input).build() + ")";
    } else if (input.getClass().isArray() || Collection.class.isAssignableFrom(input.getClass())) {
        return "(:var" + builder.addVariable(input) + ")";
    } else {
        return ":var" + builder.addVariable(input);
    }
}
```

**Рекомендация**: Выделить ответственность за обработку переменных в отдельный класс `VariableHandler`:

```java
public class VariableHandler {
    public String process(Object input, HQLBuilder builder) {
        if (input instanceof EntityPath) {
            return ((EntityPath) input).getPath();
        } else if (input instanceof IBuildable) {
            return handleBuildable((IBuildable) input, builder);
        } else {
            return handlePrimitive(input, builder);
        }
    }
}
```

---

### 5. Отсутствие разделения между состоянием и поведением в HQLBuilder

**Проблема**: Класс `HQLBuilder` хранит состояние (списки колонок, условий, join'ов) и одновременно отвечает за их обработку и генерацию запроса.

**Рекомендация**: Рассмотреть разделение на:
- `HQLQueryState` - объект для хранения состояния запроса
- `HQLQueryBuilder` - класс для построения
- `HQLQueryGenerator` - класс для генерации финального HQL

---

### 6. Использование массивов Object[] вместо типобезопасных структур

**Проблема**: В `CommonWhereExpression` используется `Object[] arguments`, что не обеспечивает типобезопасность.

**Рекомендация**: Для выражений с конкретным числом аргументов использовать типизированные поля:

```java
public class BinaryExpression extends CommonWhereExpression {
    private final Object first;
    private final Object second;
    
    public BinaryExpression(ExpressionType type, Object first, Object second) {
        super(type, first, second);
        this.first = first;
        this.second = second;
    }
    
    @Override
    public void init(HQLBuilder builder) {
        super.init(builder); // для проверки количества аргументов
        // теперь first и second типизированы
    }
}
```

---

## Архитектурные сильные стороны

1. **Четкое разделение на пакеты**: Хорошо структурированная архитектура с логическим разделением на builder, property, expression.

2. **Использование интерфейсов**: `IBuildable` интерфейс обеспечивает единообразие для всех строящихся объектов.

3. **Функциональность DSL**: Отличная реализация domain-specific language для создания путей свойств.

4. **Гибкость**: Поддержка как DSL, так и ручного задания путей через `EntityPath.fromString()`.

5. **Понятные названия**: Методы и классы имеют интуитивно понятные названия.
