import os
import requests
import json
from typing import Optional, Type, Union, Dict, List, Any
from langchain.tools import BaseTool
from pydantic import BaseModel, Field

class APIInput(BaseModel):
    """Входные параметры для вызова API"""
    endpoint: str = Field(
        description="Путь к эндпоинту (например, /api/pets или /api/pets/123)"
    )
    method: str = Field(
        description="HTTP метод: GET, POST, PUT, DELETE"
    )
    params: Optional[Dict[str, Any]] = Field(
        default=None,
        description="Query-параметры (только для GET/DELETE)"
    )
    body: Optional[Dict[str, Any]] = Field(
        default=None,
        description="JSON-тело запроса (только для POST/PUT)"
    )

class PetShelterAPITool(BaseTool):
    """Инструмент для взаимодействия с Pet Shelter API"""

    name: str = "pet_shelter_api"
    description: str = """
    Инструмент для работы с Pet Shelter API (питомцы).
    
    КЛЮЧЕВЫЕ ПРАВИЛА:
        - "покажи всех" -> GET /api/pets с пустыми params={}
        - "покажи кошек" -> GET /api/pets с params={"species": "CAT"}
        - "покажи доступных" -> GET /api/pets с params={"status": "AVAILABLE"}
        - "покажи на лечении" -> GET /api/pets с params={"status": "TREATMENT"}
        - "покажи молодых" -> GET /api/pets с params={"maxAge": 3}
        - "отсортируй по возрасту" -> GET /api/pets с params={"sortBy": "age"}
        - Параметры params и body ВСЕГДА должны быть словарями (даже пустыми {})
        - НЕ ИСПОЛЬЗУЙ несуществующие параметры: health, healthStatus, condition, treatment
        - Если пользователь просит фильтр по здоровью (здоровые/больные/травмированные):
          1. Сделай запрос GET /api/pets без params (получи всех)
          2. В ответе отфильтруй по полю healthStatus (HEALTHY/SICK/INJURED/RECOVERING)
          3. Напиши: "Отфильтровал по состоянию здоровья из полного списка:"

    КАК ПОНИМАТЬ ЗАПРОСЫ ПОЛЬЗОВАТЕЛЯ:
        - "доступные", "свободные", "можно забрать" -> status=AVAILABLE
        - "усыновлённые", "забрали", "нашли дом" -> status=ADOPTED
        - "на передержке" -> status=FOSTERED
        - "на лечении", "лечатся" -> status=TREATMENT
        - "кошки", "коты", "котята", "кошачьи" -> species=CAT
        - "собаки", "псы", "щенки", "собачьи" -> species=DOG
        - "птицы", "попугаи", "канарейки" -> species=BIRD
        - "кролики", "крольчата" -> species=RABBIT
        - "хомяки" -> species=HAMSTER
        - "молодые", "до 2 лет", "котята", "щенки", "малыши" -> maxAge=2
        - "по возрасту", "отсортировать по возрасту" -> sortBy=age
        - "по имени", "по алфавиту", "от А до Я" -> sortBy=name&sortDirection=asc
        - "новые", "сначала новые", "последние поступления" -> sortBy=arrival_date&sortDirection=desc
        - "старые", "давно в приюте" -> sortBy=arrival_date&sortDirection=asc

    ДОСТУПНЫЕ ЭНДПОИНТЫ И ПАРАМЕТРЫ:

    1. ПОКАЗАТЬ ВСЕХ питомцев:
       endpoint: "/api/pets"
       method: "GET"
       params: {}
       body: {}

    2. ПОКАЗАТЬ питомца по ID:
       endpoint: "/api/pets/123" (где 123 - ID питомца)
       method: "GET"
       params: {}
       body: {}

    3. ФИЛЬТРАЦИЯ по виду животного (species):
       endpoint: "/api/pets"
       method: "GET"
       params: {"species": "CAT"}
       body: {}
       Доступные значения: CAT, DOG, BIRD, RABBIT, HAMSTER

    4. ФИЛЬТРАЦИЯ по статусу (status):
       endpoint: "/api/pets"
       method: "GET"
       params: {"status": "AVAILABLE"}
       body: {}
       Доступные значения: AVAILABLE, ADOPTED, FOSTERED, TREATMENT

    5. ФИЛЬТРАЦИЯ по максимальному возрасту (maxAge):
       endpoint: "/api/pets"
       method: "GET"
       params: {"maxAge": 5}
       body: {}
       Используй целое число

    6. СОРТИРОВКА (sortBy + sortDirection):
       endpoint: "/api/pets"
       method: "GET"
       params: {"sortBy": "arrival_date", "sortDirection": "asc"}
       body: {}
       sortBy: arrival_date (по умолчанию), name, age
       sortDirection: asc (по возрастанию) или desc (по убыванию, по умолчанию)

    7. ДОБАВИТЬ питомца:
       endpoint: "/api/pets"
       method: "POST"
       params: {}
       body: {"name": "Барсик", "species": "CAT", "age": 3, "breed": "Сиамский", "color": "серый", "weightKg": 4.5, "description": "Ласковый"}
       Обязательные поля: name, species
       Необязательные: age, breed, color, weightKg, description, status, healthStatus, arrivalDate

    8. ОБНОВИТЬ питомца:
       endpoint: "/api/pets/123" (ID существующего питомца)
       method: "PUT"
       params: {}
       body: {"name": "Новое имя", "age": 4} (только изменяемые поля)

    9. УДАЛИТЬ питомца:
       endpoint: "/api/pets/123"
       method: "DELETE"
       params: {}
       body: {}
    """
    args_schema: Type[BaseModel] = APIInput
    return_direct: bool = False

    def _get_api_url(self) -> str:
        """Получает URL API из переменных окружения"""
        url = os.getenv("API_URL", "http://pet-shelter:8080")
        print(f"🔧 PetShelterAPITool использует URL: {url}")
        return url

    def _make_request(
        self,
        method: str,
        url: str,
        params: Optional[Dict] = None,
        body: Optional[Dict] = None
    ) -> requests.Response:
        """Выполняет HTTP-запрос с обработкой ошибок"""
        headers = {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }

        timeout = int(os.getenv("API_TIMEOUT", "10"))

        try:
            if method == "GET":
                response = requests.get(url, params=params, headers=headers, timeout=timeout)
            elif method == "POST":
                response = requests.post(url, json=body, params=params, headers=headers, timeout=timeout)
            elif method == "PUT":
                response = requests.put(url, json=body, params=params, headers=headers, timeout=timeout)
            elif method == "DELETE":
                response = requests.delete(url, params=params, headers=headers, timeout=timeout)
            else:
                raise ValueError(f"Неподдерживаемый HTTP метод: {method}")

            response.raise_for_status()
            return response

        except requests.exceptions.Timeout:
            raise Exception(f"Таймаут при запросе к {url} (превышено {timeout} сек)")
        except requests.exceptions.ConnectionError:
            raise Exception(f"Не удалось подключиться к API по адресу {url}. Проверьте, запущен ли сервер.")
        except requests.exceptions.HTTPError as e:
            error_detail = ""
            try:
                error_detail = e.response.json()
            except:
                error_detail = e.response.text

            raise Exception(
                f"Ошибка API (HTTP {e.response.status_code}): {error_detail}"
            )

    def _run(
        self,
        endpoint: str,
        method: str,
        params: Optional[Dict[str, Any]] = None,
        body: Optional[Dict[str, Any]] = None
    ) -> str:
        """
        Выполняет запрос к Pet Shelter API

        Args:
            endpoint: путь (например, /api/pets или /api/pets/1)
            method: GET, POST, PUT, DELETE
            params: query параметры
            body: тело запроса в JSON

        Returns:
            JSON-строка с результатом
        """
        params = params or {}
        body = body or {}

        # Строим URL
        api_url = self._get_api_url().rstrip('/')
        endpoint = endpoint.strip('/')
        url = f"{api_url}/{endpoint}"

        action_descriptions = {
            "GET": f"получение данных: {endpoint}",
            "POST": f"добавление питомца",
            "PUT": f"обновление питомца: {endpoint}",
            "DELETE": f"удаление питомца: {endpoint}"
        }

        try:
            # Выполняем запрос
            response = self._make_request(method, url, params, body)

            # Парсим ответ
            data = None
            if response.content:
                data = response.json()

            return json.dumps({
                "status": "success",
                "action": action_descriptions.get(method, f"операция {method}"),
                "data": data,
                "errors": None
            }, ensure_ascii=False, indent=2)

        except Exception as e:
            return json.dumps({
                "status": "error",
                "action": action_descriptions.get(method, f"операция {method}"),
                "data": None,
                "errors": [str(e)]
            }, ensure_ascii=False, indent=2)