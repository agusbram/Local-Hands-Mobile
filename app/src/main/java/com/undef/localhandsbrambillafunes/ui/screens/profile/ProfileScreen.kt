package com.undef.localhandsbrambillafunes.ui.screens.profile

import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.undef.localhandsbrambillafunes.R
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.ui.components.SellerConversionHandler
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.profile.ProfileViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel<ProfileViewModel>()
) {
    // Estado para manejar el diálogo de convertirse en vendedor
    var showSellDialog by remember { mutableStateOf(false) }

    // Se observa el estado de los campos de edición en tiempo real
    val editState by profileViewModel.editState.collectAsState()

    // Se observa el role del usuario en tiempo real
    val userRole by profileViewModel.userRole.collectAsState()
    
    // Se observa el estado general de la UI (nombre, foto, etc.)
    val profileState by profileViewModel.uiState.collectAsState()

    // Para crear los Toast
    val context = LocalContext.current

    //Para validar los datos del usuario
    val isNameValid = editState.name.length >= 3
    val isLastNameValid = editState.lastName.length >= 3
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(editState.email).matches()
    val isPhoneValid = Patterns.PHONE.matcher(editState.phone).matches()
    val isAddressValid = editState.address.length >= 5
    val isEntrepreneurshipValid = if (userRole == UserRole.SELLER) editState.entrepreneurship.length >= 4 else true

    //Para validar que el formulario esté completo para guardar los cambios
    val isFormValid = isNameValid && isLastNameValid && isEmailValid && isPhoneValid && isAddressValid && isEntrepreneurshipValid

    //Variables para dialog de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    //Variable para dialog de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Variable para dialog de eliminación de cuenta de usuario/vendedor
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Variable para dialog de eliminación de foto
    var showDeletePhotoDialog by remember { mutableStateOf(false) }

    // Launcher que abre la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profileViewModel.changeProfilePicture(it) }
    }

    // Refrescar la foto al entrar a la pantalla por única vez
    LaunchedEffect(Unit) {
        profileViewModel.refreshPhotoUrl()
    }

    /**
     * --- ESCUCHA DE EVENTOS DE LA UI ---
     * LaunchedEffect se suscribe al flujo de eventos del ViewModel (ProfileViewModel para en este caso).
     * 'key1 = true' significa que se ejecutará una sola vez y se mantendrá escuchando.
     */
    LaunchedEffect(key1 = true) {
        profileViewModel.uiEventFlow.collect { event ->
            when (event) {
                is ProfileViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    if (event.message.contains("foto", ignoreCase = true) || event.message.contains("photo", ignoreCase = true)) {
                        profileViewModel.refreshPhotoUrl()
                    }
                }
                is ProfileViewModel.UiEvent.NavigateAndClearStack -> {
                    navController.navigate(event.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Volver Atras")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(route = AppScreens.SettingsScreen.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Seccion de Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF242424),
                contentColor = Color.White
            ) {
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(route = AppScreens.HomeScreen.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(AppScreens.FavoritesScreen.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { showSellDialog = true }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorias") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(AppScreens.CategoryScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        if (profileState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val currentPhotoUrl = profileState.photoUrl
                    AsyncImage(
                        model = if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty() && File(currentPhotoUrl).exists()) {
                            File(currentPhotoUrl)
                        } else {
                            R.drawable.ic_profile_placeholder
                        },
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                                    showDeletePhotoDialog = true
                                } else {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_profile_placeholder)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campos del formulario
                OutlinedTextField(value = editState.name, onValueChange = { profileViewModel.onFieldChange(editState.copy(name = it)) }, label = { Text("Nombre") }, isError = !isNameValid, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editState.lastName, onValueChange = { profileViewModel.onFieldChange(editState.copy(lastName = it)) }, label = { Text("Apellido") }, isError = !isLastNameValid, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editState.email, onValueChange = { profileViewModel.onFieldChange(editState.copy(email = it)) }, label = { Text("Email") }, isError = !isEmailValid, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editState.phone, onValueChange = { profileViewModel.onFieldChange(editState.copy(phone = it)) }, label = { Text("Teléfono") }, isError = !isPhoneValid, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editState.address, onValueChange = { profileViewModel.onFieldChange(editState.copy(address = it)) }, label = { Text("Dirección") }, isError = !isAddressValid, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                if (userRole == UserRole.SELLER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editState.entrepreneurship, onValueChange = { profileViewModel.onFieldChange(editState.copy(entrepreneurship = it)) }, label = { Text("Nombre del Emprendimiento") }, isError = !isEntrepreneurshipValid, singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { profileViewModel.saveChanges() }, enabled = isFormValid, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar Cambios")
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showPasswordDialog = true }) {
                    Text("Cambiar Contraseña")
                }

                if (userRole == UserRole.CLIENT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showSellDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                        Text("Quiero Vender")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = { showLogoutDialog = true }) {
                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.tertiary)
                }
                TextButton(onClick = { showDeleteConfirmDialog = true }) {
                    Text("Eliminar Cuenta", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // --- Diálogos ---

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, "") }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        label = { Text("Repetir Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPassword.length >= 6 && newPassword == repeatPassword) {
                        profileViewModel.changeUserPassword(newPassword)
                        showPasswordDialog = false
                    } else {
                        Toast.makeText(context, "Las contraseñas no coinciden o son muy cortas.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    profileViewModel.logout()
                    showLogoutDialog = false
                }) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Cuenta") },
            text = { Text("Esta acción es irreversible. ¿Estás seguro de que quieres eliminar tu cuenta?") },
            confirmButton = {
                Button(onClick = {
                    profileViewModel.deleteAccount()
                    showDeleteConfirmDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Sí, eliminar cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text("Gestionar Foto de Perfil") },
            text = { Text("¿Qué te gustaría hacer?") },
            confirmButton = {
                TextButton(onClick = {
                    imagePickerLauncher.launch("image/*")
                    showDeletePhotoDialog = false
                }) {
                    Text("Cambiar Foto")
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = {
                        profileViewModel.deleteProfilePicture()
                        showDeletePhotoDialog = false
                    }) {
                        Text("Eliminar Foto", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showDeletePhotoDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    if (showSellDialog) {
        SellerConversionHandler(
            navController = navController,
            sellViewModel = hiltViewModel<SellViewModel>(),
            onDismiss = { showSellDialog = false }
        )
    }
}
