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
## Cristian de la Hoz: 
Me disculpo sinceramente por enviar este trabajo con retraso. Debo aclarar que la responsabilidad de la entrega tardía es completamente mía, mi compañero @ChristianDGF cumplió con todos los plazos establecidos y entregó su parte a tiempo. Yo fui quien no logró coordinar adecuadamente mis tiempos y causó este retraso en la entrega final del proyecto.

Durante el desarrollo de este simulador de topologías de red, pude comprender las diferencias fundamentales entre cada una de las implementaciones. **Ring Network** forma un círculo donde cada nodo se conecta únicamente con sus dos vecinos inmediatos (anterior y siguiente), creando un camino circular que requiere mínimo 3 nodos para funcionar correctamente. **Bus Network**, por el contrario, simula un bus compartido donde todos los nodos están conectados entre sí, permitiendo comunicación directa sin restricciones de vecindad. **HyperCube Network** presenta la estructura más compleja, requiriendo que el número de nodos sea potencia de 2 y conectando cada nodo solo con aquellos que difieren en exactamente un bit en su representación binaria, creando una topología n-dimensional eficiente. Finalmente, **Tree Network** implementa una jerarquía tipo árbol binario donde cada nodo (excepto la raíz) tiene un padre y puede tener hasta dos hijos, estableciendo conexiones bidireccionales solo entre padres e hijos directos.

Cada topología ofrece ventajas específicas: Ring para simplicidad, Bus para comunicación directa, HyperCube para eficiencia en redes grandes, y Tree para organización jerárquica. Durante el desarrollo, utilicé inteligencia artificial para comprender conceptos complejos como la implementación del algoritmo de conectividad del HyperCube usando operaciones XOR, y para generar el sistema de colores en la consola que mejora la experiencia visual del menú y los mensajes de estado del simulador.

-- Lo que más me encantó fue HyperCube, dado que cualquier nodo en un cluster de 2^n nodos es alcanzable en n o menos pasos, fue increible ver un aplicativo tan práctico de calculos binarios com XOR.
