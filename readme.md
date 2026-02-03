# PrimeFinder con Sincronización de Hilos

Christian Alfonso Romero Martinez

Implementación de búsqueda de números primos con sincronización de hilos usando el patrón wait/notify de Java. El programa permite pausar y reanudar los hilos trabajadores de manera controlada sin usar espera activa (busy-waiting).

## Descripción del Proyecto

Este proyecto implementa un buscador de números primos multi-hilo que puede pausarse automáticamente cada T milisegundos para mostrar el progreso y esperar confirmación del usuario antes de continuar. La sincronización entre hilos se logra usando los mecanismos nativos de Java: `synchronized`, `wait()` y `notifyAll()`.

## Comenzando

Estas instrucciones te permitirán obtener una copia del proyecto en funcionamiento en tu máquina local para propósitos de desarrollo y pruebas.

### Prerequisitos

Necesitas tener instalado:

- Java Development Kit (JDK) 8 o superior
- Apache Maven 3.6 o superior
- Git (para clonar el repositorio)

Verifica las instalaciones con:

```bash
java -version
mvn -version
git --version
```

### Instalación

Pasos para configurar el entorno de desarrollo:

1. Clona el repositorio

```bash
git clone <url-del-repositorio>
cd wait-notify-excercise
```

2. Compila el proyecto

```bash
mvn clean compile
```

3. Ejecuta la aplicación

```bash
mvn exec:java
```

## Ejecutando las Pruebas

Para ejecutar las pruebas automáticas del sistema:

```bash
mvn test
```

### Pruebas de Funcionalidad

Las pruebas verifican:

- La correcta identificación de números primos
- La sincronización entre hilos
- El conteo acumulado de primos encontrados
- La pausa y reanudación de hilos

Ejemplo de salida esperada:

```
2
3
5
...
Pausado. Conteo de primos: 1250
Presiona ENTER
...
Pausado. Conteo de primos: 2845
Presiona ENTER
```

### Pruebas de Estilo de Código

El proyecto sigue las convenciones de código Java estándar y usa:

- Nomenclatura camelCase para variables y métodos
- PascalCase para nombres de clases
- Comentarios descriptivos en español

## Diseño de Sincronización

### Monitor y Lock

El diseño usa un único objeto monitor (`pausedLock`) compartido por todos los hilos trabajadores y el hilo de control. Este monitor coordina la pausa y reanudación de forma centralizada.

```java
private final Object pausedLock = new Object();
```

### Condición de Espera

La variable `paused` (marcada como `volatile`) indica el estado del sistema:

```java
private volatile boolean paused = false;
```

Se usa `volatile` para garantizar visibilidad inmediata del cambio entre todos los hilos sin necesidad de sincronización adicional para lecturas.

### Mecanismo de Pausa

Cada hilo trabajador verifica periódicamente si debe pausarse usando el método `checkPaused()`:

```java
private void checkPaused() {
    synchronized (pausedLock) {
        while (control.isPaused()) {
            try {
                pausedLock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

Este método:
- Adquiere el lock sobre `pausedLock`
- Usa un bucle `while` para verificar la condición (evita spurious wakeups)
- Llama `wait()` liberando temporalmente el lock
- Se despierta cuando otro hilo llama `notifyAll()`

### Mecanismo de Reanudación

El hilo de control reanuda todos los trabajadores con:

```java
private void resumeWorkers() {
    synchronized (pausedLock) {
        paused = false;
        pausedLock.notifyAll();
    }
}
```

Esto:
- Cambia el estado a no pausado
- Despierta a TODOS los hilos en espera (usando `notifyAll()`)

### Evitando Lost Wakeups

El diseño previene lost wakeups mediante:

1. **Mismo monitor**: Todos los `wait()` y `notifyAll()` usan `pausedLock`
2. **Verificación en bucle**: El `while` en `checkPaused()` re-verifica la condición después de despertar
3. **Sincronización consistente**: Toda modificación de `paused` se hace dentro de `synchronized`

### Conteo Thread-Safe

El contador de primos usa sincronización a nivel de método:

```java
public synchronized void incrementPrimerCount() {
    primeCount++;
}

public synchronized int getPrimeCount() {
    return primeCount;
}
```

Esto garantiza que el incremento y lectura sean operaciones atómicas.

### Pool de Hilos con ExecutorService

Se usa `ExecutorService` para gestionar eficientemente los hilos trabajadores:

```java
pool = Executors.newFixedThreadPool(NTHREADS);
for(int i = 0; i < NTHREADS; i++) {
    pool.submit(pft[i]);
}
pool.shutdown();
```

Ventajas:
- Reutilización de hilos (menor overhead)
- Gestión automática del ciclo de vida
- Mejor escalabilidad

## Construido Con

- [Java 8](https://www.oracle.com/java/) - Lenguaje de programación
- [Maven](https://maven.apache.org/) - Gestión de dependencias y construcción
- [ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) - Framework de concurrencia de Java

## Configuración

Parámetros configurables en `Control.java`:

```java
private final static int NTHREADS = 3;           // Número de hilos trabajadores
private final static int MAXVALUE = 30000000;     // Valor máximo a evaluar
private final static int TMILISECONDS = 5000;     // Intervalo de pausa (ms)
```

## Licencia

Este proyecto fue desarrollado como ejercicio académico para el curso de Arquitecturas de Software (ARSW) - Escuela Colombiana de Ingeniería Julio Garavito.

## Notas Adicionales

### Observaciones del Diseño

1. **Sin Busy-Waiting**: El uso de `wait()` garantiza que los hilos no consumen CPU mientras están pausados.

2. **Fairness**: Aunque `notifyAll()` despierta a todos los hilos, el orden de ejecución depende del scheduler del sistema operativo.

3. **Interrupciones**: El manejo de `InterruptedException` preserva el estado de interrupción del hilo para permitir una terminación limpia.

4. **Granularidad**: La verificación `checkPaused()` se hace en cada iteración del bucle de búsqueda, permitiendo pausas rápidas (latencia < 1 iteración).

5. **Visibilidad**: El uso de `volatile` en `paused` garantiza que los cambios sean visibles inmediatamente sin necesidad de sincronización en cada lectura.

### Mejoras Potenciales

- Implementar métricas de rendimiento (tiempo total, primos/segundo)
- Agregar capacidad de cancelación anticipada
- Persistir resultados en archivo
- Interfaz gráfica para mejor visualización
