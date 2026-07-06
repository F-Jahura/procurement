# Procurement Monitor

Система управления закупками для ВЭД-компаний.

## 🚀 Возможности

- Управление поставщиками
- Управление товарами и остатками
- Создание и отслеживание заказов
- Импорт/экспорт данных в Excel
- Dashboard с ключевыми метриками
- Аутентификация и авторизация пользователей

## 🛠️ Технологии

- Java 21
- Spring Boot 3
- Spring Security
- Thymeleaf
- JPA / Hibernate
- MySQL
- Liquibase
- Apache POI

## 📦 Запуск локально

### Требования
- Java 21
- MySQL 8.0
- Gradle

### Шаги

```bash
# Клонировать репозиторий
git clone https://github.com/ваш-username/procurement.git

# Перейти в папку проекта
cd procurement

# Создать базу данных
mysql -u root -p
CREATE DATABASE procurement_db;

# Запустить приложение
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun

# Доступ
Логин: admin
Пароль: qwerty

📱 Деплой
Приложение задеплоено на Render:
https://procurement.onrender.com

📸 Скриншоты
https://screenshots/dashboard.png
https://screenshots/suppliers.png