import sys
from agent import run_agent

if __name__ == "__main__":
    print("🐾 Pet Shelter AI Agent готов!")
    print("Введите запрос (или 'exit' для выхода)")
    print("-" * 50)

    while True:
        try:
            query = input("\n💭 Ваш запрос: ").strip()

            if query.lower() in ['exit', 'quit']:
                print("👋 Пока!")
                break

            if not query:
                continue

            print("🤔 Думаю...")
            response = run_agent(query)
            print(f"\n✅ Ответ:\n{response}")
            print("-" * 50)

        except KeyboardInterrupt:
            print("\n👋 Пока!")
            break
        except Exception as e:
            print(f"❌ Ошибка: {e}")