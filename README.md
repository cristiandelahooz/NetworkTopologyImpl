---

# NetworkTopologyImpl

Implementación de una topología de red en Java.

## Requisitos

- Java 21 o superior

## Instalación

Clona el repositorio:

```bash
git clone https://github.com/cristiandelahooz/NetworkTopologyImpl.git
cd NetworkTopologyImpl
```

## Compilar el proyecto

```bash
mvn clean install
```

Esto descargará las dependencias y generará el archivo JAR en la carpeta `target/`.

## Ejecutar el proyecto

Puedes ejecutar la aplicación principal (ajusta el nombre de la clase principal si es necesario):

```bash
mvn exec:java -Dexec.mainClass="org.sakidoa.Main"
```

O ejecuta el JAR directamente si tu proyecto genera uno ejecutable:

```bash
java -jar target/NetworkTopologyImpl-1.0-SNAPSHOT.jar
```

## Estructura del proyecto

```
src/
 └── main/
     └── java/
         └── ... (código fuente)
 └── test/
     └── java/
         └── ... (tests)
pom.xml
```
---
