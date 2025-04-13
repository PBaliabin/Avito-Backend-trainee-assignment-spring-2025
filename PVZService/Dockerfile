# Используем официальный образ OpenJDK 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл JAR вашего Spring Boot приложения в контейнер
COPY target/PVZService-1.0-SNAPSHOT.jar /app/myapp.jar

# Указываем команду для запуска приложения
CMD ["java", "-jar", "/app/myapp.jar"]

# Открываем порт 8080 для доступа к приложению
EXPOSE 8080
