# 📦 Procurement Monitor

> Система управления закупками для ВЭД-компаний. Управляйте поставщиками, товарами, заказами и анализируйте ключевые метрики в реальном времени.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-46E3B7)](https://render.com)

---

## ✨ Возможности

### 📊 Dashboard
- Количество поставщиков, товаров и заказов
- Сумма всех заказов в валюте
- Быстрые действия для добавления новых записей

### 🏢 Управление поставщиками
- CRUD операции
- Фильтрация по стране и статусу
- Поиск по названию

### 📦 Управление товарами
- Учет остатков и продаж
- Визуальные индикаторы низкого остатка (🔴 < 10, 🟡 < 30)
- Фильтр по минимальному остатку

### 📄 Импорт/Экспорт Excel
- Загрузка поставщиков и товаров из Excel
- Выгрузка данных в Excel
- Отчет о результатах импорта

### 🔐 Безопасность
- Аутентификация через Spring Security
- JWT токены для API
- Роли пользователей (USER, ADMIN)

---

## 🛠️ Технологический стек

| Компонент | Технология |
|-----------|------------|
| **Язык** | Java 21 |
| **Фреймворк** | Spring Boot 3.4.0 |
| **Безопасность** | Spring Security + JWT |
| **База данных** | MySQL 8.0 |
| **Миграции** | Liquibase |
| **Шаблонизатор** | Thymeleaf |
| **Excel** | Apache POI |
| **Сборка** | Gradle (Kotlin DSL) |
| **Контейнеризация** | Docker |
| **Деплой** | Render |

---

## 🚀 Быстрый старт

### 📋 Требования
- Java 21
- MySQL 8.0
- Gradle 8.5+

### 🔧 Установка и запуск

```bash
# 1. Клонировать репозиторий
git clone https://github.com/ваш-username/procurement.git
cd procurement

# 2. Создать базу данных
mysql -u root -p
CREATE DATABASE procurement_db;
exit;

# 3. Запустить тесты (опционально)
SPRING_PROFILES_ACTIVE=mysql ./gradlew clean test

# 4. Запустить приложение
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun

# 5. Открыть в браузере
http://localhost:8080/login
🔑 Доступ к приложению
Роль	Логин	Пароль
Администратор	admin	qwerty
🐳 Запуск через Docker
bash
# 1. Собрать JAR файл
./gradlew clean build -x test

# 2. Собрать Docker образ
docker build -t procurement-app .

# 3. Запустить контейнеры
docker-compose up -d

# 4. Проверить логи
docker-compose logs -f
📱 Деплой
🔗 Живая демонстрация
Приложение доступно по адресу:
👉 https://procurement.onrender.com

📦 Деплой на Render
Форкните репозиторий на GitHub

Создайте аккаунт на Render

Подключите репозиторий и настройте переменные окружения

Нажмите Deploy

📸 Скриншоты
Dashboard	Поставщики	Товары
https://screenshots/dashboard.png	https://screenshots/suppliers.png	https://screenshots/products.png
📁 Структура проекта
text
procurement/
├── src/
│   ├── main/
│   │   ├── java/com/fatema/procurement/
│   │   │   ├── config/          # Конфигурации (Security, Liquibase)
│   │   │   ├── controller/      # REST контроллеры
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA сущности
│   │   │   ├── repository/      # Репозитории Spring Data
│   │   │   ├── service/         # Бизнес-логика
│   │   │   └── utils/           # Утилиты (JWT, Excel)
│   │   └── resources/
│   │       ├── db/changelog/    # Liquibase миграции
│   │       ├── templates/       # Thymeleaf шаблоны
│   │       └── application.yml  # Конфигурация приложения
│   └── test/                    # Тесты
├── Dockerfile                    # Инструкция для Docker
├── docker-compose.yml            # Оркестрация контейнеров
└── build.gradle.kts              # Сборка проекта
🤝 Вклад в проект
Форкните репозиторий

Создайте ветку (git checkout -b feature/amazing-feature)

Внесите изменения и сделайте коммит (git commit -m 'Add amazing feature')

Запушьте ветку (git push origin feature/amazing-feature)

Откройте Pull Request

📄 Лицензия
Этот проект распространяется под лицензией MIT. Подробнее см. в файле LICENSE.

📧 Контакты
Автор: Fatema
Email: [ваш-email@example.com]
GitHub: ваш-username

⭐️ Если этот проект был полезен, поставьте звезду на GitHub!

text

---

## 📸 Добавьте скриншоты

Создайте папку `screenshots` в корне проекта и добавьте:

```bash
mkdir screenshots
Добавьте скриншоты:

dashboard.png

suppliers.png

products.png

🎨 Дополнительные бейджи (shield.io)
markdown
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-46E3B7)](https://render.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
