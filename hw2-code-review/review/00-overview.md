# Ревью проекта HQL Builder

## Краткое описание проекта и выбранной области анализа

**Проект**: HQL Builder - Java библиотека для упрощения создания HQL (Hibernate Query Language) запросов.

**Основная область анализа**: Архитектура кода, качество реализации, безопасность, тестирование и документация.

**Текущая версия**: 1.0.0-r2 (по pom.xml)

**Цель библиотеки**: Предоставить удобный Builder API для создания HQL запросов с поддержкой domain-specific language (DSL) для типобезопасного построения путей свойств сущностей.

**Структура проекта**:
```
src/main/java/org/adaptms/hqlbuilder/
├── builder/          # Классы Builder API (HQLBuilder, BuilderMode)
├── property/         # Классы для работы с путями свойств (EntityPath)
├── expression/       # Классы выражений (where, join, order, group, column, set, Expressions)
└── IBuildable.java   # Интерфейс для построения строк
```

**Ключевые возможности**:
- SELECT, UPDATE, DELETE запросы
- JOIN различных типов (LEFT, RIGHT, INNER, FULL)
- WHERE выражения (eq, like, between, in и др.)
- ORDER BY, GROUP BY
- Поддержка параметризированных запросов
- Subquery поддержка
- DSL для типобезопасного создания путей
