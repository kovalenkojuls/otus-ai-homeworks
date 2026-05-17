import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.agents import create_tool_calling_agent, AgentExecutor
from langchain.memory import ConversationBufferMemory
from api_tool import PetShelterAPITool

load_dotenv(override=False)

# Настройки OpenRouter
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
OPENROUTER_MODEL = os.getenv("OPENROUTER_MODEL", "minimax/minimax-m2.5:free")
OPENROUTER_BASE_URL = os.getenv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1")

# LLM через OpenRouter
llm = ChatOpenAI(
    model=OPENROUTER_MODEL,
    temperature=0,
    base_url=OPENROUTER_BASE_URL,
    openai_api_key=OPENROUTER_API_KEY
)

# Тест LLM
print("🔄 Тестирую LLM...")
try:
    test = llm.invoke("Скажи 'OK' если работаешь")
    print(f"✅ LLM ответ: {test.content}")
except Exception as e:
    print(f"❌ Ошибка LLM: {e}")

# Инициализация инструмента
api_tool = PetShelterAPITool()
tools = [api_tool]

SYSTEM_PROMPT = """Ты — API-оператор Pet Shelter.
Твоя задача — обрабатывать запросы пользователя и вызывать методы API через инструмент pet_shelter_api.

## ПРАВИЛА:
1. Всегда используй инструмент pet_shelter_api для взаимодействия с API
2. Если пользователь дал неполные данные — ЗАДАЙ УТОЧНЯЮЩИЕ ВОПРОСЫ
3. НЕ придумывай данные, которых нет в ответе API
4. Если API вернул ошибку — сообщи пользователю и объясни причину

## ФОРМАТ ОТВЕТА (ОБЯЗАТЕЛЬНЫЙ):
Status: success | error
Action: описание действия
Data: данные из API или краткая сводка
Errors: ошибки если есть или null

## ПРИМЕРЫ:
- "привет" → поздоровайся и напомни о возможностях
- "покажи всех" → вызови инструмент с GET /api/pets
- запрос не про питомцев → вежливо объясни, что работаешь только с Pet Shelter API"""

# Память
memory = ConversationBufferMemory(
    memory_key="chat_history",
    return_messages=True
)

# Промпт
prompt = ChatPromptTemplate.from_messages([
    ("system", SYSTEM_PROMPT),
    MessagesPlaceholder(variable_name="chat_history"),
    ("user", "{input}"),
    MessagesPlaceholder(variable_name="agent_scratchpad")
])

# Создание агента
agent = create_tool_calling_agent(
    llm=llm,
    tools=tools,
    prompt=prompt
)

agent_executor = AgentExecutor.from_agent_and_tools(
    agent=agent,
    tools=tools,
    verbose=True,
    memory=memory,
    max_iterations=5,
    handle_parsing_errors=True,
    early_stopping_method="generate"
)

def run_agent(query: str) -> str:
    try:
        result = agent_executor.invoke({"input": query})
        return result.get("output", "Ошибка: агент не вернул ответ.")
    except Exception as e:
        import traceback
        print("\n" + "="*50)
        print("❌ ОШИБКА:")
        traceback.print_exc()
        print("="*50)
        return f"Ошибка: {str(e)}"