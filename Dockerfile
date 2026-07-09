# Etapa de construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo pom.xml y descargar las dependencias primero (aprovecha la cache de Docker)
COPY pom.xml .
# Opcional: descargar dependencias offline para acelerar builds subsiguientes (ignorar si falla)
RUN mvn dependency:go-offline -B || true

# Copiar el codigo fuente
COPY src ./src

# Compilar empaquetando el .jar saltando los tests para un despliegue mas rapido
RUN mvn clean package -DskipTests

# Etapa de ejecucion (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar solo el .jar generado de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# El puerto se puede exponer documentando (el real lo define el yml)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
