# Frontend — Accommodation Booking Service

Сучасний клієнтський SPA-застосунок для сервісу бронювання житла, побудований на **React 19 + TypeScript + Vite + Tailwind CSS v4**.

---

## 🚀 Основний стек технологій

- **Фреймворк**: [React 19](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/)
- **Збирач та Dev-сервер**: [Vite 6+](https://vitejs.dev/) (швидкий HMR, оптимізований бандлінг)
- **Стилізація**: [Tailwind CSS v4](https://tailwindcss.com/) (`@tailwindcss/vite`)
- **Маршрутизація**: [React Router DOM v7](https://reactrouter.com/) (декларативна навігація, Protected Routes)
- **HTTP Клієнт**: [Axios](https://axios-http.com/) (інтерцептори для JWT-токенів, обробка 401/403)
- **Іконки**: [Lucide React](https://lucide.dev/)

---

## 📂 Структура проекту

```
frontend/
├── src/
│   ├── api/                     # Модулі для взаємодії з REST API бекенду
│   │   ├── client.ts            # Налаштований Axios інстанс з інтерцепторами
│   │   ├── auth.ts              # Логін, реєстрація
│   │   ├── accommodations.ts    # Отримання, створення, оновлення, видалення житла
│   │   └── users.ts             # Отримання профілю, зміна даних та ролей
│   ├── components/              # Перевикористовувані UI компоненти
│   │   ├── Navbar.tsx           # Верхнє навігаційне меню з відображенням ролей
│   │   ├── ProtectedRoute.tsx   # Захист приватних та менеджерських маршрутів
│   │   └── AccommodationCard.tsx# Картка об'єкта нерухомості з динамічними фото
│   ├── context/                 # Глобальний контекст стану
│   │   └── AuthContext.tsx      # Сесія користувача, збереження токена, перевірка ролей
│   ├── pages/                   # Сторінки застосунку
│   │   ├── AccommodationsPage.tsx         # Каталог житла з пошуком та фільтрацією
│   │   ├── AccommodationDetailsPage.tsx   # Детальна сторінка об'єкта з калькулятором ціни
│   │   ├── LoginPage.tsx                  # Вхід (з кнопкою швидкого заповнення менеджера)
│   │   ├── RegisterPage.tsx               # Реєстрація нового користувача
│   │   ├── ProfilePage.tsx                # Особистий кабінет користувача
│   │   └── ManagerAccommodationsPage.tsx  # Панель менеджера (CRUD над житлом)
│   ├── types/                   # TypeScript інтерфейси моделей
│   │   └── index.ts             # Accommodation, User, Auth, DTO
│   ├── App.tsx                  # Декларація маршрутів застосунку
│   ├── main.tsx                 # Точка монтування React DOM
│   └── index.css                # Глобальні стилі Tailwind
├── Dockerfile                   # Багатоетапна збірка (Node build + Nginx alpine)
├── nginx.conf                   # Конфігурація веб-сервера з SPA fallback та API proxy
└── vite.config.ts               # Конфігурація Vite з плагінами та проксі на :8080
```

---

## 🛠️ Запуск локально для розробки

1. **Встановіть залежності**:
   ```bash
   cd frontend
   npm install
   ```

2. **Запустіть dev-сервер**:
   ```bash
   npm run dev
   ```
   Застосунок відкриється за адресою: `http://localhost:5173`

> Запити до `/api/*` автоматично проксіюються через Vite на працюючий Spring Boot бекенд (`http://localhost:8080`).

---

## 📦 Збірка проекту для production

```bash
npm run build
```
Зібраний статичний бандл згенеровано в директорію `frontend/dist/`.

---

## 🐳 Запуск через Docker

У корені репозиторію виконайте:
```bash
docker compose up --build
```
Фронтенд буде доступний на порту `http://localhost:3000`.
