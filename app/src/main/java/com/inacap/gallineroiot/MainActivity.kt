package com.inacap.gallineroiot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.inacap.gallineroiot.ui.theme.GallineroIoTTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GallineroIoTTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFFF0F4F8), Color(0xFFE2E8F0))))
                ) {
                    Scaffold(containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainApp(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(modifier: Modifier = Modifier) {
    val authManager = remember { AuthManager() }
    val gallineroManager = remember { GallineroManager() }

    var usuarioLogueado by remember { mutableStateOf(authManager.obtenerUsuarioActual() != null) }
    var gallineroActual by remember { mutableStateOf<String?>(null) }

    if (!usuarioLogueado) {
        PantallaLogin(authManager) { usuarioLogueado = true }
    } else if (gallineroActual == null) {
        PantallaConfiguracion(gallineroManager) { codigo -> gallineroActual = codigo }
    } else {
        PantallaGallinero(
            codigoGallinero = gallineroActual!!,
            authManager = authManager,
            gallineroManager = gallineroManager,
            onCerrarSesion = {
                authManager.cerrarSesion()
                usuarioLogueado = false
                gallineroActual = null
            }
        )
    }
}

@Composable
fun PantallaLogin(authManager: AuthManager, onLoginExitoso: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Home, contentDescription = "Gallinero", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = if (isLoginMode) "Bienvenido de Nuevo" else "Crea tu Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    if (email.isBlank() || password.isBlank()) return@Button Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                    if (isLoginMode) {
                        authManager.iniciarSesion(email, password) { exito, error -> if (exito) onLoginExitoso() else Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show() }
                    } else {
                        authManager.registrarUsuario(email, password) { exito, error ->
                            if (exito) {
                                Toast.makeText(context, "Registro exitoso. Revisa tu correo.", Toast.LENGTH_LONG).show()
                                isLoginMode = true
                            } else Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(if (isLoginMode) "Entrar" else "Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(if (isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

@Composable
fun PantallaConfiguracion(gallineroManager: GallineroManager, onConfigurado: (String) -> Unit) {
    val context = LocalContext.current
    var nombreGallinero by remember { mutableStateOf("") }
    var cantidadGallinas by remember { mutableStateOf("") }
    var codigoParaUnirse by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Configuración Inicial", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Crear un nuevo gallinero", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nombreGallinero, onValueChange = { nombreGallinero = it }, label = { Text("Nombre (Ej: Corral 1)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = cantidadGallinas, onValueChange = { cantidadGallinas = it }, label = { Text("Cantidad de gallinas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    val cantidad = cantidadGallinas.toIntOrNull()
                    if (nombreGallinero.isNotBlank() && cantidad != null && cantidad > 0) {
                        gallineroManager.crearGallinero(nombreGallinero, cantidad) { exito, codigo -> if (exito) onConfigurado(codigo!!) else Toast.makeText(context, "Error: $codigo", Toast.LENGTH_LONG).show() }
                    } else Toast.makeText(context, "Llena los datos correctamente", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Crear Gallinero") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("O únete a uno existente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = codigoParaUnirse, onValueChange = { codigoParaUnirse = it }, label = { Text("Código (Ej: GALLINA-1234)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    if (codigoParaUnirse.isNotBlank()) {
                        gallineroManager.unirseAGallinero(codigoParaUnirse) { exito, error -> if (exito) onConfigurado(codigoParaUnirse) else Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show() }
                    }
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Unirse al Gallinero") }
            }
        }
    }
}

@Composable
fun PantallaGallinero(codigoGallinero: String, authManager: AuthManager, gallineroManager: GallineroManager, onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val notificacionManager = remember { NotificacionManager(context) }

    var estadoPuerta by remember { mutableStateOf("Cargando...") }
    var gallinasEsperadas by remember { mutableStateOf(0) }
    var gallinasActuales by remember { mutableStateOf(0) }
    var alertaLanzada by remember { mutableStateOf(false) }
    var horaActual by remember { mutableStateOf("") }
    var clima by remember { mutableStateOf("Soleado") }

    // Variables para el contador
    var tiempoRestante by remember { mutableStateOf(10) }
    var mostrandoContador by remember { mutableStateOf(false) }
    var mostrarHistorial by remember { mutableStateOf(false) }

    // Reloj y clima
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("America/Santiago")
            horaActual = sdf.format(Date())
            clima = if (Random.nextBoolean()) "Soleado ☀️" else "Nublado ☁️"
            delay(1000)
        }
    }

    // LÓGICA DEL CONTADOR
    LaunchedEffect(estadoPuerta, gallinasActuales, gallinasEsperadas, mostrandoContador) {
        if (estadoPuerta == "ABIERTA" && mostrandoContador) {
            if (tiempoRestante > 0) {
                delay(1000)
                tiempoRestante -= 1
            } else {
                // Al llegar a 0, suma la gallina, oculta el contador y resetea
                gallineroManager.sumarGallina(codigoGallinero, gallinasActuales)
                mostrandoContador = false
                tiempoRestante = 10
            }
        } else if (estadoPuerta == "CERRADA") {
            // Si la puerta se cierra, cancelamos el contador
            mostrandoContador = false
            tiempoRestante = 10
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(codigoGallinero) {
        val listener = gallineroManager.escucharGallinero(codigoGallinero) { estado, esperadas, actuales ->
            estadoPuerta = estado
            gallinasEsperadas = esperadas
            gallinasActuales = actuales

            // Si la puerta se acaba de abrir Y faltan gallinas, activamos el contador
            if (estado == "ABIERTA" && actuales < esperadas && !mostrandoContador && tiempoRestante == 10) {
                mostrandoContador = true
            }

            // Alerta de notificación
            if (actuales < esperadas) {
                val faltantes = esperadas - actuales
                if (!alertaLanzada) {
                    notificacionManager.lanzarAlarma(faltantes)
                    notificacionManager.registrarHistorial(codigoGallinero, faltantes)
                    alertaLanzada = true
                }
            } else {
                alertaLanzada = false
            }
        }
        onDispose { listener.remove() }
    }

    if (mostrarHistorial) {
        PantallaHistorial(codigoGallinero, gallineroManager) { mostrarHistorial = false }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // RELOJ Y CLIMA
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Text("⏰ $horaActual", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Text("$clima", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA DE CÓDIGO
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Código para compartir", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(codigoGallinero, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TARJETA DE LA PUERTA Y CONTADOR
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = if (estadoPuerta == "ABIERTA") Color(0xFF4CAF50) else Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado de la Puerta", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text(text = estadoPuerta, color = if (estadoPuerta == "ABIERTA") Color(0xFF4CAF50) else Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Black)

                    // MOSTRAR CONTADOR SOLO SI CORRESPONDE
                    if (estadoPuerta == "ABIERTA" && mostrandoContador) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = tiempoRestante / 10f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2196F3),
                            trackColor = Color(0xFFE3F2FD)
                        )
                        Text("Entrada de gallina en: $tiempoRestante seg", color = Color(0xFF2196F3), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA DE GALLINAS
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gallinas Registradas", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$gallinasActuales / $gallinasEsperadas", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    if (gallinasActuales < gallinasEsperadas) {
                        Text("¡ALERTA! Faltan ${gallinasEsperadas - gallinasActuales} gallinas", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTONES
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { gallineroManager.cambiarEstadoPuerta(codigoGallinero, "ABIERTA") }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) {
                    Text("Abrir", fontWeight = FontWeight.Bold)
                }
                Button(onClick = { gallineroManager.cambiarEstadoPuerta(codigoGallinero, "CERRADA") }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { gallineroManager.actualizarGallinas(codigoGallinero, gallinasActuales - 1) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), shape = RoundedCornerShape(12.dp)) {
                Text("Escanear (Simular 1 menos)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = { mostrarHistorial = true }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Ver Historial de Alertas", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar Sesión", color = Color.Gray)
            }
        }
    }
}

@Composable
fun PantallaHistorial(codigoGallinero: String, gallineroManager: GallineroManager, onVolver: () -> Unit) {
    var historial by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    DisposableEffect(codigoGallinero) {
        val listener = gallineroManager.escucharHistorial(codigoGallinero) { lista -> historial = lista }
        onDispose { listener.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Historial de Alertas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))

        if (historial.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay alertas registradas aún.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historial) { registro ->
                    val mensaje = registro["mensaje"] as? String ?: "Alerta"
                    val fechaLong = registro["fecha"] as? Long ?: 0L
                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    formatter.timeZone = TimeZone.getTimeZone("America/Santiago")
                    val fechaTexto = formatter.format(Date(fechaLong))

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = mensaje, fontWeight = FontWeight.Bold, color = Color.Red)
                                Text(text = "Fecha: $fechaTexto", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Volver al Panel", fontWeight = FontWeight.Bold)
        }
    }
}