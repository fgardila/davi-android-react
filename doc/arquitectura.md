# arquitectura.md — Decisiones de diseño DaviPlata

> Registro de decisiones arquitectónicas (ADR) para la prueba técnica DaviPlata.
> Cada sección documenta _qué_ se decidió, _por qué_ se eligió sobre las alternativas
> y _dónde_ vive el código relevante.

---

## 1. Arquitectura general: brownfield híbrido

**Decisión:** Android nativo (Kotlin + Compose) como "shell" con React Native embebido para Home y Movimientos.

**Motivación:** La prueba técnica exige explícitamente demostrar integración bidireccional con el Bridge clásico. Mantener la sesión, seguridad y operaciones críticas (Transferencia, Login) en nativo reduce la superficie de ataque y permite control total sobre el ciclo de vida. RN se usa solo donde aporta valor diferencial de demo (lista reactiva, saldo animado).

---

## 2. React Native 0.79.7 con `newArchEnabled=false`

**Decisión:** Fijar RN 0.79.7 y deshabilitar la Nueva Arquitectura (Fabric/TurboModules).

**Motivación:** A partir de RN 0.76 la Nueva Arquitectura es el valor por defecto, a partir de RN 0.80 el Bridge clásico quedó congelado (`legacy frozen`), y a partir de RN 0.82 fue eliminado por completo. 0.79.7 es la **última versión** de RN que todavía honra `newArchEnabled=false` y a la vez es compatible con Kotlin 2.2.x + Compose BOM 2026.x — el resto del stack moderno que se exige en esta prueba. Mantenerla garantiza que `ReactContextBaseJavaModule`, `@ReactMethod`, `Promise` y `RCTDeviceEventEmitter` estén disponibles sin migrar a TurboModules.

`gradle.properties`:
```
newArchEnabled=false
hermesEnabled=true
```

> **Trade-off:** la spec original del plan apuntaba a 0.73 por máxima estabilidad del Bridge clásico, pero subir Kotlin a 2.x + Compose BOM 2026 forzó el salto a 0.79.7. Cualquier upgrade futuro a 0.80+ requeriría migrar a New Architecture.

---

## 3. Backend mockeado en proceso (MockInterceptor)

**Decisión:** Implementar el backend como un `MockInterceptor` de OkHttp respaldado por un `MockDataStore` en memoria, en lugar de levantar un backend real (Node.js / Ktor) o usar un BaaS (Supabase / Firebase).

**Motivación:** La prueba técnica en `prueba_tecnica_mobile_android.md` lista el backend como **"libre"** y se enfoca en evaluar la app móvil: integración del bridge, manejo de sesión, cifrado, seguridad y arquitectura. Bajo ese alcance:

- Un backend real (Ktor o Node) implicaría empaquetar otro proceso, definir despliegue, persistencia y credenciales — esfuerzo que no aporta puntos a la evaluación y reparte el foco fuera del cliente Android.
- Un BaaS (Supabase, Firebase) introduciría dependencia de red, cuentas externas, latencia variable y costo. Imposibilita demostrar la app offline y obliga al evaluador a configurar credenciales para correrla.
- Un mock en proceso ofrece lo que la prueba exige (respuestas HTTP deterministas, todos los códigos de error de un backend real: 400, 401, 404, 409, 423, 500) **sin sobre-ingeniería**, sin dependencia externa y con demo reproducible.

El `MockInterceptor` introduce 1 segundo de delay (`Thread.sleep(BuildConfig.MOCK_DELAY_MS)`) para simular latencia realista. Todos los errores son deterministas: password `"wrong"` → 401, monto > saldo → 409, teléfono `"0000000000"` → 404. Esto permite **demostrar el flujo completo de errores** sin depender de provocar fallos en un servidor.

**Trade-off asumido:** No hay persistencia entre reinicios de app (los datos viven en memoria). Aceptable y declarado para una prueba de demo. El swap a un backend real es directo: solo cambiar el `OkHttpClient` en `NetworkModule` para apuntar al host real y remover el interceptor del mock.

**Código:**
- `data/remote/mock/MockInterceptor.kt` — router de rutas HTTP
- `data/remote/mock/MockDataStore.kt` — estado in-memory con `Mutex`
- `data/remote/mock/MockRules.kt` — errores deterministas

**Datos seed:**
- Fabian Ardila · `3001234567` / `demo1234` · saldo $1.250.345
- Ana Gómez · `3009876543` / `demo1234` · saldo $480.000
- 12 movimientos pre-generados (mezcla DEBIT/CREDIT, último mes)

---

## 4. `DaviPlataReactNativeHost` dentro de `:app`

**Decisión:** El `ReactNativeHost` singleton vive directamente en el módulo `:app`, no en un módulo Gradle separado.

**Motivación:** Para esta prueba técnica un único módulo Android mantiene el build graph simple, evita configuraciones cruzadas de plugins Kotlin/Compose entre módulos y deja todas las dependencias del bridge RN colocadas junto a las Activities que las consumen. La separación en módulos solo aporta valor cuando hay reutilización entre apps o equipos distintos — ninguno de los dos casos aplica aquí.

**Archivo:** `app/src/main/java/dev/code93/daviplata/rnhost/DaviPlataReactNativeHost.kt`

---

## 5. Inyección en el Bridge vía `EntryPointAccessors` (no `@Inject constructor`)

**Decisión:** `DaviPlataBridgeModule` recibe sus dependencias por constructor "manual" — los UseCases se obtienen vía `BridgeEntryPoint` (`@EntryPoint` de Hilt) en `DaviPlataApp.onCreate()`.

**Motivación:** El Bridge clásico instancia `NativeModule` a través de `ReactPackage.createNativeModules(ReactApplicationContext)`, un método del framework RN que Hilt no controla. No se puede anotar `@AndroidEntryPoint` en un `ReactContextBaseJavaModule`. La solución canónica es usar `EntryPointAccessors.fromApplication()` para extraer el grafo de Hilt dentro del `Application` y pasar las dependencias al `Package`.

```kotlin
// DaviPlataApp.onCreate()
val ep = EntryPointAccessors.fromApplication(this, BridgeEntryPoint::class.java)
val bridgePackage = DaviPlataBridgePackage(
    ep.getBalanceUseCase(),
    ep.getMovementsUseCase(),
    ep.clearSessionUseCase(),
    ep.bridgeEventBus(),
    ep.secureStorage(),
)
```

---

## 6. `BridgeEventBus` con cola de eventos pendientes

**Decisión:** `BridgeEventBus` mantiene una lista de eventos pendientes cuando no hay `ReactContext` activo y los entrega en cuanto `attach(context)` es llamado.

**Motivación:** `DaviPlataBridgeModule.initialize()` (que llama `attach`) puede ejecutarse después de que algún evento ya fue emitido (p.ej. `SESSION_EXPIRED` desde `AuthInterceptor` antes de que RN esté montado). La cola evita perder eventos en esa ventana de tiempo.

```kotlin
// BridgeEventBus.emit()
if (ctx != null && ctx.hasActiveReactInstance()) {
    ctx.getJSModule(RCTDeviceEventEmitter::class.java).emit(eventName, params)
} else {
    pending.add(eventName to params)   // entregado en attach()
}
```

---

## 7. Sesión cifrada con `EncryptedSharedPreferences` (`security-crypto:1.1.0`)

**Decisión:** Persistir la sesión con `EncryptedSharedPreferences` + `MasterKey` AES-256-GCM respaldada por Android Keystore.

**Motivación:** `androidx.security:security-crypto` fue deprecada por Google en abril de 2025, pero la versión `1.1.0` estable sigue siendo funcional y mantiene la API más simple del ecosistema para cifrado de preferencias. Se elige por simplicidad de API y porque este es un proyecto de demo. Para producción real se migraría a `DataStore + Tink` o al fork mantenido `dev.spght:encryptedprefs-ktx`.

**El campo `expiresAtMillis` en `SecureStorage` y el campo `expiresAt` en `MockDataStore.MockSession` son independientes.** El primero es el TTL local (leído por la UI y `SessionGuard`); el segundo es el que `MockInterceptor` usa para decidir si la sesión es válida a nivel HTTP.

---

## 8. `openTransfer()` via `Class.forName()` para evitar dependencias circulares

**Decisión:** El `DaviPlataBridgeModule` usa `Class.forName("dev.code93.daviplata.presentation.transfer.TransferActivity")` en lugar de referenciar la clase directamente.

**Motivación:** Si el bridge importa `TransferActivity`, y `TransferActivity` (a través de su ViewModel y UseCases) importa clases del módulo bridge o datos, se crean dependencias circulares en el grafo de compilación. `Class.forName()` rompe esta dependencia en tiempo de compilación sin afectar el comportamiento en runtime.

---

## 9. Navegación interna RN sin `react-navigation`

**Decisión:** El switch entre Home y Movimientos se maneja con un `useState('HOME' | 'MOVEMENTS')` en `App.tsx`. No se usa `@react-navigation/native`.

**Motivación:** El bundle debe ser ligero. `react-navigation` añade ~200 KB al bundle JS y requiere configuración de native stacks. Para dos pantallas, un state switch es suficiente y elimina la dependencia. La navegación nativa (Android back-stack) sigue siendo responsabilidad de `HomeReactActivity`.

---

## 10. TTL de sesión configurable vía `BuildConfig`

| Build type | `SESSION_TTL_MINUTES` | `MOCK_DELAY_MS` | `DEBUG_TOOLS` |
|---|---|---|---|
| debug | 2 | 1000 | true |
| release | 15 | 1000 | false |

`DEBUG_TOOLS=true` habilita el botón "⚠ Forzar sesión expirada" en HomeScreen (visible solo con `__DEV__` en RN) y el método `forceSessionExpired()` en el Bridge. Este método establece `SecureStorage.expiresAt = 0L` para que la siguiente verificación local del TTL falle. La expiración completa del flujo HTTP requiere además que el TTL de `MockDataStore.sessions` haya vencido (2 min).

---

## 11. Errores deterministas en MockInterceptor

| Condición de entrada | Código HTTP | Código de error |
|---|---|---|
| `password == "wrong"` | 401 | `INVALID_CREDENTIALS` |
| 3 intentos fallidos seguidos | 423 | `ACCOUNT_LOCKED` (+ `retryAfterSeconds`) |
| Teléfono ya registrado | 409 | `PHONE_TAKEN` |
| `toPhone == "0000000000"` | 404 | `RECIPIENT_NOT_FOUND` |
| `amount > balance` | 409 | `INSUFFICIENT_FUNDS` |
| `amount <= 0` | 400 | `INVALID_AMOUNT` |
| Sesión inválida / expirada | 401 | `SESSION_EXPIRED` |
| Header `X-Mock-Force: 500` | 500 | `INTERNAL_ERROR` |

---

## 12. Tests unitarios

**Estado actual:** Kotlin **127 tests** en 15 suites, RN **36 tests** en 6 suites — ambas suites verdes.

**Cómo se ejecutan:**

```bash
# Kotlin
./gradlew :app:testDebugUnitTest

# React Native
cd rn-bundle && npx jest
```

**Qué se cubre (vistazo general):**

- **Validación de dominio:** `ValidatorsTest` valida teléfono, password, email, documento, monto.
- **Mapping de errores:** `ErrorMapperTest` cubre la tabla completa HTTP → `AppError` (401/404/409/423/500 + `SESSION_EXPIRED`).
- **UseCases:** `LoginUseCase`, `RegisterUseCase`, `CreateTransferUseCase`, `FindRecipientUseCase` — validaciones de entrada + delegación a repositorios.
- **Repositorios:** `SessionRepositoryImpl` (incluye `sessionFlow` reactivo) y `TransferRepositoryImpl` (404 → null special case).
- **Seguridad:** `SessionGuardTest`, `PasswordHasherTest` (BCrypt cost-10).
- **Backend mock:** `MockInterceptorTest` corre Retrofit + OkHttp + `MockInterceptor` sin mocks (integración real, 1s/test). El orden de los interceptores importa: `AuthInterceptor` se registra **antes** que `MockInterceptor` para que la cabecera `Authorization` esté presente cuando `requireValidSession()` la lee.
- **ViewModels:** `LoginViewModel`, `RegisterViewModel`, `TransferViewModel` — reducer completo + casos críticos (concurrent guard, self-transfer, password strength).
- **JS bridge:** `nativeApi.test.ts` (wrapper sobre `NativeModules`), `events.test.ts` (`DeviceEventEmitter`), `useBalanceQuery` y `useMovementsQuery` (React Query v5), `MovementItem` (status badges + strikethrough), `formatCurrency`.

**Frameworks:** JUnit 4 + MockK + Turbine + `kotlinx-coroutines-test` para Kotlin; Jest + `@testing-library/react-native` para JS.

**Lo que NO se testea unitariamente (y por qué):** Activities y Composables requieren instrumentación Android (`androidTest/`); `SecureStorage` necesita Robolectric por dependencia de Keystore; `DaviPlataBridgeModule` necesita un `ReactApplicationContext` real — la lógica útil ya está delegada a UseCases testeados.

---

## 13. Single-Activity Architecture con `DaviPlataActivity`

**Decisión:** Fusionar `SplashActivity`, `LoginActivity` y `RegisterActivity` en un único `DaviPlataActivity` que hospeda un `NavHost` Compose con type-safe routes.

**Motivación:** El pre-Home (Splash + Auth) comparte ciclo de vida, theme y back stack. Tener 3 Activities introducía 3 task roots, multiplicaba boilerplate y dejaba la decisión de navegación dentro de las pantallas. Con un único Activity:

- Una sola raíz de back-stack (UX coherente).
- Pantallas Compose stateless con `*Route` (stateful + `hiltViewModel()`) + `*Screen` (UI pura) — patrón Now-in-Android.
- `AppCoordinator` orquesta navegación: las pantallas no conocen `NavController`.
- ViewModels scopeados al `NavBackStackEntry`: al `popBackStack()`, el VM correspondiente se limpia automáticamente. Un `AuthViewModel` global retendría estado de pantallas ya cerradas — anti-patrón.

```kotlin
sealed interface AppRoute {
    @Serializable data object Splash : AppRoute
    @Serializable data object Login : AppRoute
    @Serializable data object Register : AppRoute
}
```

`HomeReactActivity`, `TransferActivity` y `SessionExpiredActivity` siguen siendo Activities independientes: `HomeReactActivity` porque `ReactActivity` tiene lifecycle propio del bridge RN; las otras dos por contención de scope (intent-driven, no comparten ciclo con auth).

**Código:**
- `presentation/DaviPlataActivity.kt` — host único
- `presentation/navigation/AppRoute.kt` — sealed con `@Serializable`
- `presentation/navigation/AppCoordinator.kt` — orquestador (single source of truth para `NavController`)
- `presentation/navigation/AppNavGraph.kt` — extension `NavGraphBuilder.appGraph(coordinator)`
- `presentation/splash/SplashRoute.kt`, `auth/login/LoginRoute.kt`, `auth/register/RegisterRoute.kt`

---

## 14. Navigation Compose 2.8.5 con type-safe routes (`@Serializable`)

**Decisión:** Usar `composable<AppRoute.Login> { }` (sintaxis tipada de Navigation Compose 2.8+) en vez de rutas string-based.

**Motivación:** Las rutas string son frágiles: errores tipográficos solo aparecen en runtime, los argumentos viajan como `Bundle<String, String?>` y se pierden los tipos. Con Navigation Compose 2.8 + `kotlinx-serialization`, los destinos son `data object` o `data class` anotados con `@Serializable`, el compilador valida nombres, y los argumentos viajan tipados. Es la forma idiomática en Android moderno.

Requiere el plugin `org.jetbrains.kotlin.plugin.serialization` y `kotlinx-serialization-json` en el classpath — ver `gradle/libs.versions.toml`.

---

## 15. `SplashScreen` API moderno (`androidx.core:core-splashscreen:1.2.0`)

**Decisión:** Usar `installSplashScreen()` en `DaviPlataActivity.onCreate()` controlado por un `MutableStateFlow<Boolean>` a nivel Activity, en lugar de una `SplashActivity` con animación Compose propia.

**Motivación:** Desde Android 12, el sistema **siempre** muestra un splash al cold start de cualquier app. Si la app no implementa la API moderna, sale un splash blanco con el icono por defecto del launcher antes de poder mostrar contenido propio. Con `core-splashscreen` 1.2.0 se aprovecha ese splash del sistema para mostrar el icono del app (`windowSplashScreenAnimatedIcon = ic_launcher_foreground`) y se controla cuándo soltarlo:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { systemSplashVisible.value }
    super.onCreate(savedInstanceState)
    // ...
}
```

`SplashViewModel` ejecuta los chequeos de seguridad + sesión y, cuando termina, `SplashRoute` llama `onSplashContentReady()` que pone `systemSplashVisible.value = false`. El sistema entonces transiciona del splash al primer destino del `NavHost` (`Login` o `HomeReactActivity`) sin flash blanco.

`postSplashScreenTheme` aplica el theme normal del app al desmontar el splash — evita el "salto" visual del status bar.

---

## 16. `SessionRepository` reactivo (`sessionFlow: Flow<Session?>`)

**Decisión:** `SessionRepository` expone `sessionFlow: Flow<Session?>` además de `current()` / `save()` / `clear()`. Internamente, un `MutableStateFlow<Session?>` inicializado desde `EncryptedSharedPreferences`.

**Motivación:** Consumidores como `SplashViewModel` necesitan reaccionar a cambios de sesión sin re-leer disco. Sin un Flow, cada consumidor tendría que pollearlo o suscribirse a cambios de `SharedPreferences` — ambas opciones frágiles y costosas. Con el `MutableStateFlow`:

- `save()` actualiza disco + emite valor nuevo en una sola operación atómica.
- `clear()` emite `null`.
- `current()` lee del flow (`stateFlow.value`) sin tocar disco.

Habilita patrones como "logout en otro lado del app → cualquier observer reacciona" sin event bus adicional.

**Código:** `data/repository/SessionRepositoryImpl.kt` (testeado en `SessionRepositoryImplTest`).

---

## 17. `Validators` centralizados en `domain/validation/Validators.kt`

**Decisión:** Extraer todas las reglas de validación (longitud teléfono, longitud y composición password, formato email, document, amount) a un único módulo `Validators.kt` de funciones puras que devuelven `String?` (null = válido, string = mensaje listo para mostrar).

**Motivación:** Antes había 6+ sitios duplicando literalmente `phone.length == 10`, `password.length >= 8`, etc., entre UseCases (`LoginUseCase`, `RegisterUseCase`, `CreateTransferUseCase`, `FindRecipientUseCase`), Screens (`LoginScreen`, `RegisterScreen`, `TransferScreen`) y ViewModels (`RegisterViewModel`). Cualquier cambio de regla (p.ej. soportar prefijo internacional) requería buscar y editar todos esos sitios y abría una ventana para inconsistencias.

```kotlin
object Validators {
    const val PHONE_LENGTH = 10
    const val PASSWORD_MIN_LENGTH = 8

    fun phone(value: String): String? = when {
        value.isBlank() -> "Ingresa tu teléfono"
        value.length != PHONE_LENGTH -> "Debe tener $PHONE_LENGTH dígitos"
        !value.all(Char::isDigit) -> "Solo dígitos"
        else -> null
    }
    // password, strongPassword, passwordConfirmation, name,
    // document, email, username, amount, firstError(vararg)...
}
```

Convención `String?` (en vez de `Result<Unit>` o `sealed ValidationError`) elegida porque el consumidor casi siempre necesita el mensaje listo para renderizar en la UI. Para combinar varias reglas hay `firstError(vararg)` que devuelve el primer no-null.

Cubierto por `ValidatorsTest` con 17 casos. Consumido por 4 UseCases + 3 Screens + 2 ViewModels.

---

## 18. `AuthInterceptor` — pre + post 401 con whitelist de endpoints auth

**Decisión:** `AuthInterceptor` valida la sesión **antes** de `chain.proceed()` con `SessionGuard.isExpired()`, y si la sesión está vencida (y el endpoint no está en `AUTH_ENDPOINTS`), retorna una `Response` 401 sintética sin pegarle al backend. El handler post-401 sigue activo como red de seguridad.

**Motivación:** Dos problemas resueltos:

1. **Defensa en profundidad.** Si el TTL local ya venció, no tiene sentido gastar una request y esperar el 401 del backend para descubrirlo. La validación previa es instantánea y ahorra latencia + posibles fugas de datos (un backend confundido podría devolver datos antes de chequear sesión).

2. **Bug crítico de UX.** El handler post-401 enviaba al usuario a `SessionExpiredActivity` ante **cualquier** 401, incluyendo el del flujo de login con password incorrecta. Resultado: "credenciales inválidas" → "tu sesión expiró, vuelve a entrar" → loop infinito. Con la whitelist `AUTH_ENDPOINTS = listOf("/api/auth/login", "/api/auth/register")`, los 401 de esos endpoints pasan limpios a `LoginViewModel` que renderiza el error inline en el formulario.

```kotlin
val isAuthEndpoint = AUTH_ENDPOINTS.any { req.url.encodedPath.contains(it) }
if (sessionGuard.isExpired() && !isAuthEndpoint) {
    return synthetic401(SESSION_EXPIRED_BODY)   // mismo shape que el mock
}
val response = chain.proceed(req)
if (response.code == 401 && !isAuthEndpoint) {
    sessionRepository.clear()
    launchSessionExpired()
}
return response
```

El cuerpo JSON sintético usa el mismo shape que `MockInterceptor` para `SESSION_EXPIRED`, así que `ErrorMapper` lo mapea correctamente a `AppError.SessionExpired` sin ramas especiales.

---

## 19. Self-transfer guard en `TransferViewModel`

**Decisión:** `TransferViewModel` inyecta `GetCurrentSessionUseCase`, cachea `currentUserPhone` en su `init`, y al hacer `lookupRecipient(phone)` compara contra ese teléfono. Si coincide, emite `RecipientState.SelfTransfer` y la UI bloquea el botón "Continuar".

**Motivación:** La spec lo exige explícitamente ("no se permite transferencia al mismo usuario"). Implementarlo solo a nivel de validación del backend tendría dos problemas:

- UX pobre: el usuario solo descubre el error tras pulsar "Confirmar" y esperar la latencia de red.
- Defensa única: si el backend tiene un bug que no rechaza la transferencia, se ejecuta.

La solución es client-side primero (UX inmediata, feedback en cuanto el teléfono se completa) + chequeo server-side como red de seguridad. Validación en dos capas, como el resto de errores críticos.

`RecipientState` es un sealed `data object` con ramas `Idle | Searching | Found(...) | NotFound | SelfTransfer | Error`. Cubierto por `TransferViewModelTest`.

---

## 20. React Query v5 lado RN con invalidación por bridge events

**Decisión:** El bundle JS usa `@tanstack/react-query` 5.x. Los hooks `useBalanceQuery` y `useMovementsQuery` se invalidan **reactivamente** cuando se recibe `TRANSFER_COMPLETED` o `BALANCE_UPDATED` desde nativo vía `RCTDeviceEventEmitter`, y se limpian con `queryClient.clear()` al recibir `SESSION_EXPIRED`.

**Motivación:** Sin caché reactiva, el usuario que regresa a Home tras una transferencia exitosa vería el saldo viejo hasta hacer pull-to-refresh manual. Con React Query + bridge events:

- `TransferViewModel.submit` exitoso → `BridgeEventBus.emit("TRANSFER_COMPLETED")` → en JS, listener llama `queryClient.invalidateQueries(['balance', 'movements'])` → React Query refetchea automáticamente → UI se actualiza sin intervención.
- `SESSION_EXPIRED` → `queryClient.clear()` evita que el siguiente usuario vea datos del anterior.

React Query 5.x rompió con la API de 4.x (`loading` → `isPending`, etc.).

**Código:**
- `rn-bundle/src/lib/queryClient.ts` — setup del cliente
- `rn-bundle/src/hooks/useBalanceQuery.ts`, `useMovementsQuery.ts`
- `rn-bundle/src/bridge/events.ts` — listeners de eventos nativos

---

## 21. `Movement.status` (`COMPLETED | PENDING | FAILED`)

**Decisión:** Agregar el enum `MovementStatus` al modelo de dominio `Movement` y propagarlo hasta el bundle RN, con badge visual + strikethrough en `MovementItem.tsx`.

**Motivación:** La spec exige una columna "estado" para movimientos. Antes el modelo solo distinguía DEBIT/CREDIT (tipo), no estado. Con el enum:

- Dominio: `Movement.status: MovementStatus`.
- Data: DTO usa `runCatching { MovementStatus.valueOf(raw) }.getOrDefault(COMPLETED)` para tolerar valores futuros del backend sin crash.
- Bridge: el módulo serializa el status como `String` y JS lo recibe tipado.
- RN: `MovementItem.tsx` muestra un badge de color por estado y aplica `textDecorationLine: 'line-through'` cuando es `FAILED` — feedback visual inmediato.

Cubierto por `MovementItem.test.tsx` (8 casos).

---

*Documento generado: 2026-05-14. Última actualización: 2026-05-17 — alineado con versiones reales (RN 0.79.7, security-crypto 1.1.0), eliminadas secciones de implementación interna (plugin Kotlin, getLaunchOptions, ReactApplication override, ReactNativeHost modular, Reduce Motion, ResultOverlay) por no aportar valor al ADR, condensada la sección de tests, y agregadas decisiones sobre Single-Activity, Navigation Compose 2.8.5, SplashScreen API, SessionRepository reactivo, Validators, AuthInterceptor con whitelist, self-transfer guard, React Query v5 y `Movement.status`.*
