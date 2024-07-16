# Student Voice

### Java Spring + Thymeleaf backend

---

# Оглавление
1. [Требования](#требования)
2. [Конфигурация](#конфигурация)
3. [Запуск](#запуск)
4. [Описание API](#api)
5. [Работа через шаблоны](#работа-через-шаблоны)


# Требования

Для запуска приложения требуется:

- `JDK 17` (`java -version` должен возвращать 17 версию)
- `PostgreSQL` (тестировалось на 14 версии)

# Конфигурация

Перед запуском нужно изменить конфигурацию под машину, на которой будет запускаться приложение.  
>`application.yml` лежит в `src/main/resources/application.yml` если будете запускать через Intellij Idea.  
>Если запускаете как jar, нужно положить конфигурацию рядом и запускать по инструкции из блока *Запуск*.

В `application.yml` нужно указать следующие параметры:
1. `application.host` поменять на публичный адрес машины (`localhost:8080 для отладки`)
2. `spring.datasource.url` на адрес базы данных
3. `spring.datasource.username` и `spring.datasource.password` на соответствущие реквизиты базы данных

# Запуск
## Через Intellij Idea
Открываем проект, скачиваем зависимости через Maven, билдим и запускаем.

## Через JAR
Рядом с .jar кладем готовую конфигурацию и через консоль в папке с приложением вызываем:
```
java -jar {jar_name}.jar --spring.config.location=file:application.yml
```

# API
## Создание первого администратора
Создает первого админа. Доступно только если нет ни одного админа. Работает без авторизации.  
  
Запрос:  
`POST /api/admin/create-first`

Параметры:
- `username` - логин админа
- `password` - пароль админа

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

## Авторизация
Авторизирует пользователя. После успешной авторизации в куки будет лежать токен, благодаря которому будет доступ к остальному функционалу

Запрос:  
`POST /login`

Параметры:
- `username` - логин
- `password` - пароль

Ответ:
В ответ приходит html шаблон, для api он не нужен - можно игнорировать

## Институты. Доступно только админам
### Создание
Создает институт. Нужно делать заранее, если нужно отображение имени института.
Лучше сразу создайте институт Радиотехнический факультет | РТФ | Мира, 32

Запрос:  
`POST /api/institutes/create`

Параметры:
- `instituteFullName` - полное название института
- `instituteShortName` - аббревиатура института
- `instituteAddress` - адрес института формата "улица, дом". Именно так, потому что так записано в модеусе

Ответ:
```json
{
  "institutesList": [
    {
      "instituteId": int,
      "fullName": string,
      "shortName": string,
      "address": string
    }
  ]
}
```

## Пользователи. Доступно только админам
### Создание из файла
Создает пользователей (преподователей) на основе переданных в файле данных. Возвращает список кредов созданных пользователей

Запрос:  
`POST /api/users/create-from-file`

Параметры:
- `file` - multipart текстовый файл. Полное ФИО преподавателей, списком, без знаков препинания, каждый с новой строки

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "createdUsersFile": string, файл в кодировке base64
}
```

### Список пользователей
Возвращает список всех пользователей

Запрос:  
`GET /api/users/list`

Ответ:
```json
{
  "users": [
    {
      "username": string,
      "role": string
    }
  ]
}
```

### Обновление
Обновление данных пользователя

Запрос:  
`POST /api/users/update`

Параметры:
- `username` - имя пользователя
- *`password` - пароль. Опционально
- *`professorName` - ФИО преподавателя. Опционально

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

### Удаление
Удаление пользователя

Запрос:  
`POST /api/users/delete`

Параметры:
- `username` - имя пользователя

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

### Создание
Создает пользователя в базе

Запрос:  
`POST /api/users/create`

Параметры:
- `username` - логин пользователя
- `password` - пароль пользователя
- `role` - роль(`ADMIN`, `PROFESSOR`)
- `professorName` - ФИО преподавателя. Обязательно, если `role`=`PROFESSOR`

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

## Преподаватель
### Данные о текущем преподавателе
Возвращает данные о текущем(авторизованном) преподавателе

Запрос:  
`GET /api/professors/current`

Параметры:
- `from` - дата "ОТ" для поиска пар. Формат `yyyy-MM-dd`
- `to` - дата "ДО" для поиска пар. Формат `yyyy-MM-dd`

Ответ:
```json
{
  "professorName": string,
  "coursesList": [
    {
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      }
    }
  ],
  "classSessionsList": [
    {
      "sessionId": string,
      "status": string,
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      },
      "roomName": string,
      "sessionName": string,
      "professorName": string,
      "startDateTime": string,
      "endDateTime": string,
      "disableAfterTimestamp": string
    }
  ]
}
```

### Обновление пар из Модеуса
Выгружает в базу пары из модеуса текущего(авторизованном) преподавателе

Запрос:  
`GET /api/professors/update-sessions`

Параметры:
- `from` - дата "ОТ" для поиска пар. Формат `yyyy-MM-dd`
- `to` - дата "ДО" для поиска пар. Формат `yyyy-MM-dd`

Ответ:  
Если операция успешна, то:
```json
{
  "professorName": string,
  "classSessionsList": [
    {
      "sessionId": string,
      "status": string,
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      },
      "roomName": string,
      "sessionName": string,
      "professorName": string,
      "startDateTime": string,
      "endDateTime": string,
      "disableAfterTimestamp": string
    }
  ]
}
```
Если при выгрузке произошла ошибка, то в ответе будет:
```json
{
  "result": {
    "success": false,
    "message": "Во время получения пар из модеуса произошла ошибка"
  }
}
```

## Предметы
### Создание
Создает в базе новый предмет. Нужно, если предмета нет в модеусе

Запрос:  
`POST /api/courses/create`

Параметры:
- `instituteId` - id института
- `courseName` - название предмета
- `professorsNames` - ФИО преподавателей предмета, через запятую

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

### Получение данных о предмете
Возвращает данные о предмете

Запрос:  
`GET /api/courses/find`

Параметры:
- `courseId` - id института

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "courseDetails": {
    "courseName": string,
    "instituteName": string,
    "instituteAddress": string,
    "professorsNames": string
  },
  "classSessionsList": [
    {
      "sessionId": string,
      "status": string,
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      },
      "roomName": string,
      "sessionName": string,
      "professorName": string,
      "startDateTime": string,
      "endDateTime": string,
      "disableAfterTimestamp": string
    }
  ]
}
```

## Пары
### Создание
Создание пары вручную. Нужно если пары нет в модеусе.

Запрос:  
`POST /api/sessions/create`

Параметры:
- `courseId` - id предмета
- `professorName` - фио ведущего пары
- `startSession` - дата-время начала пары в формате `yyyy-MM-ddTHH:mm:ss`
- `endSession` - дата-время окончания пары в формате `yyyy-MM-ddTHH:mm:ss`
- `roomName` - номер кабинета
- `sessionName` - название пары

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

### Получение данных о паре
Возвращает данные о паре

Запрос:  
`GET /api/sessions/find`

Параметры:
- `sessionId` - id пары

Ответ:
```json
{
  "timerInfo": {
    "message": string,
    "isActive": boolean
  },
  "reviewUrl": string,
  "sessionQR": string, jpg изображение в base64
  "classSessionDate": string,
  "classSession": {
    "sessionId": string,
    "status": string,
    "courseId": string,
    "courseDetails": {
      "courseName": string,
      "instituteName": string,
      "instituteAddress": string,
      "professorsNames": string
    },
    "roomName": string,
    "sessionName": string,
    "professorName": string,
    "startDateTime": string,
    "endDateTime": string,
    "disableAfterTimestamp": string
  }
}
```

### Запуск таймера на доступ к ссылке отзыва
Запускает таймер для пары. Пока таймер работает, на пару можно отправить отзыв.
После окончания таймера, отзыв оставить нельзя, только если снова не включить таймер.

Запрос:  
`POST /api/sessions/start-timer`

Параметры:
- `sessionId` - id пары
- `time` - время на которое будет включен таймер в формате `HH:mm:ss`

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

### Список отзывов пары
Возвращает список отзывов, оставленных на пару

Запрос:  
`GET /api/sessions/reviews-list`

Параметры:
- `sessionId` - id пары

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "reviewsList": [
    {
      "reviewId": string,
      "sessionId": string,
      "studentFullName": string,
      "value": int,
      "comment": string,
      "timestamp": string
    }
  ]
}
```

### Изменение ведущего пары
Изменяет ФИО преподавателя, который будет вести пару

Запрос:  
`POST /api/sessions/change-professor`

Параметры:
- `sessionId` - id пары
- `newProfessor` - ФИО нового ведущего

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```


## Отзывы
### Сохранение отзыва
Сохранение отзыва со стороны студента. Доступно только если включен таймер на паре  

Запрос:  
`POST /api/sessions/change-professor`

Параметры:
- `sessionId` - id пары
- `newProfessor` - ФИО нового ведущего

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  }
}
```

## Рейтинг. Доступно только админам
### Рейтинг институтов
Возвращает рейтинг институтов.

Запрос:  
`GET /api/rating/institutes`

Ответ:
```json
{
  "institutesList": [
    {
      "instituteId": int,
      "fullName": string,
      "shortName": string,
      "address": string,
      "avgRating": float
    }
  ]
}
```

### Рейтинг предметов в институте
Возвращает рейтинг предметов в институте

Запрос:  
`GET /api/rating/institutes/{instituteId}`

Параметры:
- `instituteId` - id института

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "coursesList": [
    {
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      },
      "avgRating": float
    }
  ]
}
```

### Рейтинг пар предмета
Возвращает рейтинг пар предмета

Запрос:  
`GET /api/rating/course/{courseId}`

Параметры:
- `courseId` - id предмета

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "classSessionsList": [
    {
      "sessionId": string,
      "status": string,
      "courseId": string,
      "courseDetails": {
        "courseName": string,
        "instituteName": string,
        "instituteAddress": string,
        "professorsNames": string
      },
      "roomName": string,
      "sessionName": string,
      "professorName": string,
      "startDateTime": string,
      "endDateTime": string,
      "disableAfterTimestamp": string,
      "avgRating": float
    }
  ]
}
```

### Рейтинг пары
Возвращает список отзывов на пару. По сути, то же что и `GET /api/sessions/reviews-list`

Запрос:  
`GET /api/rating/session/{sessionId}`

Параметры:
- `sessionId` - id пары

Ответ:
```json
{
  "result": {
    "success": boolean,
    "message": string
  },
  "reviewsList": [
    {
      "reviewId": string,
      "sessionId": string,
      "studentFullName": string,
      "value": int,
      "comment": string,
      "timestamp": string
    }
  ]
}
```
## Работа через шаблоны

### Создание первого администратора
Для создания первого администратора нужно в браузере перейти по пути `{host}/admin/create-first`
Указать его реквизиты и нажать `create`.  
После этого надо проверить что при логине `{host}/login` с указанными реквезитами вы успешно заходите и попадаете на страницу администратора `{host}/admin-home`.
> Если нужно поменять пароль, пока что нужно делать в ручную через бд.  
> Для этого нужно дернуть `GET {host}/encode?password={new_password}`, полученную строку нужно вписать нужному пользователю в базу

### Панель администратора
Для доступа к панели администратора нужно перейти по пути `{host}/admin-home` или `{host}/admin`  
(или нажать `Домашнаяя страница` на любой странице будучи админом).  
Здесь есть следующие функции:
- **Создание пользователя.** Можно создавать как других админов, так и преподавателей.
- **Создание института.** Аббревиатура института уникальная и является вторичным ключем.
- **Просмотр рейтинга.** По нажатию открывается таблица рейтинга институтов, из которой можно добраться до любого рейтинга, вплоть до отзывов отдельной пары.

### Панель преподавателя
Для доступа к панели преподавателя нужно перейти по пути `{host}/professor-home`.  
Здесь есть 2 блока:
1. **Список предметов** с возможностью добавления новых и просмотра существующих. По нажатию на предмет открывается список запланированных пар предмета, с которого можно перейти на страницу пары или создать новую.
2. **Список запланированных пар** с возможностью добавления новых и получения qr-кодов для отзыва.

### Форма отзыва
Для того чтобы оставить отзыв нужно:
1. Создать институт
2. Создать предмет
3. Создать пару с этим предметом
4. На странице пары получить qr-код или перейти по ссылке под ним
5. На открывшейся форме заполнить ФИО, поставить оценку и нажать кнопку *Отправить*

 # Деплой на сервер (Ubuntu 22.04 Jammy)

1. Первоначально клонируем проект на сервер:

  ```bash
  git clone <имя вашего репозитория>
  ```

2. Необходимо установить JAVA 17.

  ```bash
  sudo apt update
  sudo apt install openjdk-17-jdk
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  source ~/.bashrc
  ```

3. Необходимо установить Chromedriver.

  ```bash
  wget https://storage.googleapis.com/chrome-for-testing-public/126.0.6478.126/linux64/chrome-headless-shell-linux64.zip
  unzip chromedriver_linux64.zip
  sudo mv chromedriver /usr/local/bin/chromedriver
  sudo chown root:root /usr/local/bin/chromedriver
  sudo chmod +x /usr/local/bin/chromedriver
  export WEB_DRIVER_PATH=/usr/local/bin/chromedriver
  ```

4. Установим PostgreSQL и создадим Базу данных, а также пользователя.

  ```bash
  sudo apt update
  sudo apt install postgresql
  sudo service postgresql start
  sudo systemctl enable postgresql
  sudo su - postgres
  psql -c "CREATE USER myuser WITH PASSWORD 'userPassword'"
  psql -c "CREATE DATABASE mydatabase"
  psql -c "GRANT ALL PRIVILEGES ON DATABASE mydatabase TO myuser"
  exit
  ```

  !Если не получается подключиться к базе данных откройте файл:

  ```bash
  sudo nano /etc/postgresql/<версия БД>/main/postgresql.conf
  ```

  и расскомментируйте строку:

  ```bash
  #listen_addresses = 'localhost'   
  ```

  Также можно добавить свой адрес:

  ```bash
  listen_addresses = '192.168.1.100, 127.0.0.1'
  ```

  И перезапустим нашу базу данных:

  ```bash
  sudo service postgresql restart
  ```

5. Установим maven для сборки проекта.

  ```bash
  sudo apt update
  sudo apt install maven
  mvn --version
  ```

6. Далее сформируем .jar файл. Для этого в директории проекта пропишите:

  ```bash
  mvn package spring-boot:repackage
  cd target
  ```

7. Скопируем настройки проекта `application.yml` из `src/main/resources/application.yml` в текущую директорию.

  При необходимости возможно изменение настроек в `application.yml` в зависимости от реквизитов БД.

8. Запустим проект:

  ```bash
  java -jar student-voice-0.2.jar --spring.config.location=file:application.yml --urfu.username=<пользователь> --urfu.password=<пароль>
  ```
