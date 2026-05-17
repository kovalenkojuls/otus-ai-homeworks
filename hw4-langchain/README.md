# Домашнее задание 4: Pet Shelter AI Agent на LangChain

## 🏗️ Архитектура

Проект реализован в виде трёх контейнеризованных компонентов:

1. **База данных PostgreSQL** — хранение данных о животных.
2. **Приложение Pet Shelter (Java + Spring)** — REST API для управления приютом.
   - Добавление животного
   - Получение списка всех животных (с фильтрацией и сортировкой)
   - Получение животного по ID
   - Обновление данных животного
   - Удаление животного

3. **AI-агент на LangChain** — интерактивный агент, взаимодействующий с Pet Shelter API через естественные запросы.

📌 Подробная схема контейнеров представлена в файле [`docker-compose.yml`](./docker-compose.yml).

---

## 🚀 Запуск проекта

1. Перейдите в директорию `hw4-langchain/`.

2. Создайте файл `.env` и заполните его учётными данными.

   Пример содержимого `.env` можно посмотреть в `.env.example`:
   ```env
   POSTGRES_USER=postgres_user
   POSTGRES_PASSWORD=postgress_password
   
   API_URL=http://pet-shelter-app:8080

   OPENROUTER_API_KEY=your_open_router_api_key
   OPENROUTER_MODEL=openai/gpt-oss-120b:free
   OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
   ```

3. Выполните сборку и запуск контейнеров:
   ```bash
   docker-compose up -d
   ```

4. Убедитесь, что все контейнеры запущены корректно:
   ```bash
   docker-compose ps
   ```
   Вы должны увидеть три активных контейнера: `pet-shelter-postgres`, `pet-shelter-app `, `pet-shelter-agent`.

5. Проверьте успешное подключение агента к LLM:
   ```bash
   docker-compose logs pet-agent
   ```
   В логах должно появиться:
   ```
   pet-shelter-agent  | 🔄 Тестирую LLM...
   pet-shelter-agent  | ✅ LLM ответ: OK
   pet-shelter-agent  | 🐾 Pet Shelter AI Agent готов!
   pet-shelter-agent  | Введите запрос (или 'exit' для выхода)
   pet-shelter-agent  | --------------------------------------------------
   pet-shelter-agent  |
   ```

6. Подключитесь к работающему контейнеру AI-агента:
   ```bash
   docker attach pet-shelter-agent
   ```

7. Теперь вы можете взаимодействовать с агентом в интерактивном режиме:
   - Введите natural-language-запрос (например: *«Покажи всех кошек, отсортированных по имени»*).
   - Агент проанализирует запрос, сгенерирует вызов API и выполнит его.
   - Выведет результат — и снова ожидает ввода.
   - Для выхода введите `exit` или нажмите `Ctrl+C`.

---

## 🐾 Примеры запросов

- *«Добавь в приют кошку по имени Барсик, возраст 3 года»*
- *«Какие животные у нас есть? Отсортируй по дате добавления, по убыванию»*
- *«Удали животное с ID 456»*
- *«Измени возраст на 5 лет у животного ID 789»*

---

## 🛠️ Технологии

- **Backend API**: Java 17 + Spring Boot
- **База данных**: PostgreSQL 15
- **Orchestration**: Docker + Docker Compose
- **AI-агент**: LangChain, Python 3.11, OpenRouter
- **Intel IDEA + Continue + Ollama + qwen3-coder-next:cloud** для помощи в написание данного дз

---

## 📁 Структура проекта

```
hw4-langchain/
├── docker-compose.yml       # Конфигурация трёх контейнеров
├── .env                     # Переменные окружения
├── pet-shelter-api/         # Сервис Pet Shelter (Java/Spring)
├── pet-shelter-agent/       # Python-агент на LangChain
└── README.md
```

---

## 📝 Пример взаимодействия с агентом:
### Запрос 1
```
💭 Ваш запрос: покажи всех животных в приюте                                                                                                                                                      
🤔 Думаю...                                                                                                         
                 

> Entering new AgentExecutor chain...

Invoking: `pet_shelter_api` with `{'endpoint': '/api/pets', 'method': 'GET', 'params': {}}`


🔧 PetShelterAPITool использует URL: http://pet-shelter-app:8080
{
  "status": "success",
  "action": "получение данных: api/pets",
  "data": [
    {
      "id": 1,
      "name": "Барсик",
      "species": "CAT",
      "breed": "шотландский вислоухий",
      "age": 2,
      "color": "серый",
      "weightKg": 6.0,
      "arrivalDate": "2026-05-15",
      "healthStatus": "HEALTHY",
      "description": "Ласковый, любит играть с мячиком",
      "status": "AVAILABLE",
      "statusLabel": "available"
    },
    {
      "id": 2,
      "name": "Рекс",
      "species": "DOG",
      "breed": "немецкая овчарка",
      "age": 5,
      "color": "черный с подпалинами",
      "weightKg": 28.0,
      "arrivalDate": "2026-04-10",
      "healthStatus": "HEALTHY",
      "description": "Собака-поводырь, дрессирована",
      "status": "AVAILABLE",
      "statusLabel": "available"
    },
    {
      "id": 3,
      "name": "Кеша",
      "species": "BIRD",
      "breed": "акула",
      "age": 3,
      "color": "зеленый",
      "weightKg": 0.3,
      "arrivalDate": "2026-03-22",
      "healthStatus": "RECOVERING",
      "description": "Оправился от травмы крыла",
      "status": "FOSTERED",
      "color": null,
      "weightKg": null,
      "arrivalDate": "2026-01-21",
      "healthStatus": "HEALTHY",
      "description": null,
      "status": "AVAILABLE",
      "statusLabel": "available"
    }
  ],
  "errors": null
}**Status:** success  
**Action:** Получены данные о всех питомцах в приюте.  
**Data:**  
- Барсик, кот, 2 года, серый, статус AVAILABLE, здоровье HEALTHY.  
- Рекс, собака, 5 лет, черный с подпалинами, статус AVAILABLE, здоровье HEALTHY.  
- Кеша, птица, 3 года, зеленый, статус FOSTERED, здоровье RECOVERING.  
- Вася, собака, возраст и детали не указаны, статус AVAILABLE, здоровье HEALTHY.  

**Errors:** null

> Finished chain.

✅ Ответ:
**Status:** success
**Action:** Получены данные о всех питомцах в приюте.
**Data:**
- Барсик, кот, 2 года, серый, статус AVAILABLE, здоровье HEALTHY.
- Рекс, собака, 5 лет, черный с подпалинами, статус AVAILABLE, здоровье HEALTHY.
- Кеша, птица, 3 года, зеленый, статус FOSTERED, здоровье RECOVERING.
- Вася, собака, возраст и детали не указаны, статус AVAILABLE, здоровье HEALTHY.

**Errors:** null
--------------------------------------------------
```
### Запрос 2
```
💭 Ваш запрос: добавь питомца собаку васю. дата прибытия 21.01.2026. статус здоровья - здоров
🤔 Думаю...


> Entering new AgentExecutor chain...

Invoking: `pet_shelter_api` with `{'endpoint': '/api/pets', 'method': 'POST', 'params': {}, 'body': {'name': 'Вася', 'species': 'DOG', 'arrivalDate': '2026-01-21', 'healthStatus': 'HEALTHY'}}`


🔧 PetShelterAPITool использует URL: http://pet-shelter-app:8080
{
  "status": "success",
  "action": "добавление питомца",
  "data": {
    "id": 5,
    "name": "Вася",
    "species": "DOG",
    "breed": null,
    "age": null,
    "color": null,
    "weightKg": null,
    "arrivalDate": "2026-01-21",
    "healthStatus": "HEALTHY",
    "description": null,
    "status": "AVAILABLE",
    "statusLabel": "available"
  },
  "errors": null
}**Status:** success  
**Action:** Добавлен новый питомец – собака «Вася».  
**Data:**  
- ID: 5  
- Имя: Вася  
- Вид: DOG  
- Дата прибытия: 2026‑01‑21  
- Состояние здоровья: HEALTHY  
- Статус: AVAILABLE (по умолчанию)  

**Errors:** null

> Finished chain.

✅ Ответ:
**Status:** success
**Action:** Добавлен новый питомец – собака «Вася».
**Data:**
- ID: 5
- Имя: Вася
- Вид: DOG
- Дата прибытия: 2026‑01‑21
- Состояние здоровья: HEALTHY
- Статус: AVAILABLE (по умолчанию)

**Errors:** null
--------------------------------------------------
```
### Запрос 3
```
💭 Ваш запрос: покажи всех котов отсортированных по дате прибытия
🤔 Думаю...


> Entering new AgentExecutor chain...

Invoking: `pet_shelter_api` with `{'endpoint': '/api/pets', 'method': 'GET', 'params': {'species': 'CAT', 'sortBy': 'arrival_date'}, 'body': None}`


🔧 PetShelterAPITool использует URL: http://pet-shelter-app:8080
{
  "status": "success",
  "action": "получение данных: api/pets",
  "data": [
    {
      "id": 1,
      "name": "Барсик",
      "species": "CAT",
      "breed": "шотландский вислоухий",
      "age": 2,
      "color": "серый",
      "weightKg": 6.0,
      "arrivalDate": "2026-05-15",
      "healthStatus": "HEALTHY",
      "description": "Ласковый, любит играть с мячиком",
      "status": "AVAILABLE",
      "statusLabel": "available"
    }
  ],
  "errors": null
}**Status:** success  
**Action:** Получены все коты, отсортированные по дате прибытия.  
**Data:**  
- **ID 1** – Барсик, 2 года, шотландский вислоухий, серый, прибыл 15.05.2026, статус AVAILABLE, здоровье HEALTHY.  

**Errors:** null

> Finished chain.

✅ Ответ:
**Status:** success
**Action:** Получены все коты, отсортированные по дате прибытия.
**Data:**
- **ID 1** – Барсик, 2 года, шотландский вислоухий, серый, прибыл 15.05.2026, статус AVAILABLE, здоровье HEALTHY.

**Errors:** null
--------------------------------------------------
```
### Запрос 4
```
💭 Ваш запрос: удали питомца с айди 1
🤔 Думаю...


> Entering new AgentExecutor chain...

Invoking: `pet_shelter_api` with `{'endpoint': '/api/pets/1', 'method': 'DELETE', 'params': {}}`


🔧 PetShelterAPITool использует URL: http://pet-shelter-app:8080
{
  "status": "success",
  "action": "удаление питомца: api/pets/1",
  "data": null,
  "errors": null
}**Status:** success  
**Action:** Питомец с ID 1 удалён из приюта.  
**Data:** —  
**Errors:** null

> Finished chain.

✅ Ответ:
**Status:** success
**Action:** Питомец с ID 1 удалён из приюта.
**Data:** —
**Errors:** null
--------------------------------------------------

```
### Запрос 5
```
💭 Ваш запрос: измени вес у живтного с айди 3 на 6.5 
🤔 Думаю...


> Entering new AgentExecutor chain...

Invoking: `pet_shelter_api` with `{'endpoint': '/api/pets/3', 'method': 'PUT', 'params': {}, 'body': {'weightKg': 6.5}}`


🔧 PetShelterAPITool использует URL: http://pet-shelter-app:8080
{
  "status": "success",
  "action": "обновление питомца: api/pets/3",
  "data": {
    "id": 3,
    "name": "Кеша",
    "species": "BIRD",
    "breed": "акула",
    "age": 3,
    "color": "зеленый",
    "weightKg": 6.5,
    "arrivalDate": "2026-03-22",
    "healthStatus": "RECOVERING",
    "description": "Оправился от травмы крыла",
    "status": "FOSTERED",
    "statusLabel": "fostered"
  },
  "errors": null
}**Status:** success  
**Action:** Обновлен вес питомца с ID 3 до 6.5 kg.  
**Data:** Питомец «Кеша» (птица) теперь весит 6.5 kg.  

**Errors:** null

> Finished chain.

✅ Ответ:
**Status:** success
**Action:** Обновлен вес питомца с ID 3 до 6.5 kg.
**Data:** Питомец «Кеша» (птица) теперь весит 6.5 kg.

**Errors:** null
--------------------------------------------------
```

## ✅ Использованные промты:
### Промт 1
```
Напиши приложение на спринге в папке hw4-langchain/

API методы (5 эндпоинтов):
1. POST /api/pets - Добавить нового питомца в приют
   json
   {"name": "Барсик", "type": "cat", "breed": "шотландский вислоухий", "age": 2, "color": "серый", "weight_kg": 4.5, "arrival_date": "2026-05-15", "health_status": "healthy", "description": "Ласковый, любит играть с мячиком"}

2. GET /api/pets - Получить список питомцев с фильтрацией
   Query params: ?species=cat&status=available&maxAge=3&sortBy=arrival_date

3. GET /api/pets/{id} - Получить информацию о конкретном питомце

4. PUT /api/pets/{id} - Обновить данные питомца
   Изменить статус (available/adopted/fostered/treatment)
   Обновить вес/здоровье/описание

5. DELETE /api/pets/{id} - Питомца забрали навсегда (полное удаление из приюта)

Статусы питомцев (enum): available - доступен для адопции. adopted - нашел новый дом. fostered - на временной передержке. treatment - на лечении.
Типы животных (enum): cat, dog, bird, rabbit, hamster.
Статусы по здоровью (enum): сам придумай.
Все енамы должны храниться в соответствующих классах приложения, а в бд писаться просто строкой.

Напиши docker-compose.yml: один контейнер с постгресом куда всё будет писаться. второй контейнер - с самим приложением на спринге.

Так же в спринг приложении напиши миграцию на liquibase которая создаст необходимую структуру в бд. Пока я думаю достаточно одной таблицы pets.

Всё должно запускаться одной командой docker-compose up и быть доступно по localhost:8080
```
### Промт 2
```
У меня в папке hw4-langchain лежит приложение не спринге. это минимальный рест хттп апи.

API методы (5 эндпоинтов):
1. POST /api/pets - Добавить нового питомца в приют
   json
   {"name": "Барсик", "type": "cat", "breed": "шотландский вислоухий", "age": 2, "color": "серый", "weight_kg": 4.5, "arrival_date": "2026-05-15", "health_status": "healthy", "description": "Ласковый, любит играть с мячиком"}

2. GET /api/pets - Получить список питомцев с фильтрацией
   Query params: ?species=cat&status=available&maxAge=3&sortBy=arrival_date

3. GET /api/pets/{id} - Получить информацию о конкретном питомце

4. PUT /api/pets/{id} - Обновить данные питомца
   Изменить статус (available/adopted/fostered/treatment)
   Обновить вес/здоровье/описание

5. DELETE /api/pets/{id} - Питомца забрали навсегда (полное удаление из приюта).

Это всё доступно по http://localhost:8080. Само приложение и база данных подняты в докер контейнерах.

Теперь мне необходимо сделать следующее:

Реализуйте tool для вызова API

Tool должен быть оформлен как LangChain tool.
Внутри tool — явный HTTP-вызов или функция-обёртка.
Tool возвращает структурированный результат (JSON / dict → string).
Собери агента.
Настройте системный prompt:
роль агента (API-оператор);
ограничения (что он может и не может делать);
правила вызова tool.
Подключите API-tool.
Реализуйте запуск из CLI, например:
python main.py ""создай пользователя с именем Alex""
Определите контракт ответа Ответ агента должен быть в фиксированном формате, например:

Status: success | error
Action: <описание действия>
Data: <результат API>
Errors: <если есть>

Всё это необходимо поднять в третьем контейнере. создай необходимые папки и файлы для реализации этого задания. всё должно так же подниматься просто docker-compose up -d
все креды вынеси в какой нибудь лобальный конфиг типа .env или как переменные докер контейнера.
```
### Промт 3
```
Проект готов. Оформи в корне папке hw4-langchain/ файл README.md. Там красифо сформулируй следующее:
Для данного дз было разработано приложение на джава+спринг Приют домашних животных. В отдельном контейнере поднят постгрес. В отдельном - само прилдожение. Оно предоставляет апи для добавления животного в приют, получение списка животных, получение конкретного по айди, получение списка с учётом сортировок и фильтров, удаления по айди и обновления по айди. В третьем контейнере поднят агент на langchain для общения с этим апи. Со структурой контейнеров можно ознакомиться в hw4-langchain/docker-compose.yml

описание запуска: 1. hw4-langchain/.env тут заполнить креды. 2. из hw4-langchain/ выполнить команду docker-compose up -d. 3. после сборки и запуска контейнеров убедиться что все три контейнера поднялись без ошибок docker-compose ps. 4. при старте контейнера с langchain агентом выполняется тест подключения к llm. посмотрите логи контейнера и убедитесь в успехе подключения docker-compose logs pet-agent. вы должны увидеть следующее:

pet-shelter-agent  | 🔄 Тестирую LLM...
pet-shelter-agent  | ✅ LLM ответ: OK
pet-shelter-agent  | 🐾 Pet Shelter AI Agent готов!
pet-shelter-agent  | Введите запрос (или 'exit' для выхода)
pet-shelter-agent  | --------------------------------------------------
pet-shelter-agent  |

5. далее для общения с агентом необходимо подключиться к контейнеру docker attach pet-shelter-agent
6. готово. внутри цикл на питоне ждет ввода с консоли. вводите запрос - агент его обрабатывает - выполняет - выводит результат - ожидает нового запроса. для выхода введите exit или cntl+c. 
```
PS: в реальности же промтов было в разы больше, так как бесплатный qwen3-coder-next:cloud от ollama не фонтан, а на claude мне жалко денег:)