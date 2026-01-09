package com.undef.localhandsbrambillafunes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellerCreationStatus

/**
 * Composable responsable de gestionar el flujo completo
 * de conversión de un usuario a vendedor.
 *
 * Este flujo incluye:
 * - Verificación del estado actual del usuario
 * - Confirmación de intención
 * - Ingreso del nombre del emprendimiento
 * - Navegación automática a la pantalla de ventas
 *
 * @param navController Controlador de navegación.
 * @param sellViewModel ViewModel que maneja el estado de creación del vendedor.
 * @param onDismiss Callback para cerrar el flujo.
 */
@Composable
fun SellerConversionHandler(
    navController: NavController,
    sellViewModel: SellViewModel,
    onDismiss: () -> Unit
) {
    val status by sellViewModel.status.collectAsState()
    val entrepreneurshipName by sellViewModel.entrepreneurshipName.collectAsState()


    // Estados locales para el control de diálogos
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showEntrepreneurDialog by remember { mutableStateOf(false) }

    /**
     * Observa los cambios de estado del proceso de creación
     * de vendedor y reacciona en consecuencia.
     */
    LaunchedEffect(status) {
        when (status) {
            SellerCreationStatus.SUCCESS, SellerCreationStatus.ALREADY_EXISTS -> {
                navController.navigate(AppScreens.SellScreen.route)
                onDismiss()
            }
            SellerCreationStatus.IDLE -> {
                // El ViewModel ha confirmado que el usuario no es vendedor. Mostramos el diálogo.
                showConfirmationDialog = true
            }
            SellerCreationStatus.ERROR -> {
                onDismiss()
            }
            else -> {}
        }
    }

    /**
     * Verifica el estado del usuario al iniciar el flujo.
     */
    LaunchedEffect(Unit) {
        // Resetear el nombre al empezar
        sellViewModel.resetConversionState()

        // Verificación rápida inicial
        val isAlreadySeller = sellViewModel.isUserAlreadySeller()

        if (isAlreadySeller) {
            // Navegar directamente
            navController.navigate(AppScreens.SellScreen.route)
            onDismiss()
        } else {
            // Solo si NO es vendedor, iniciar el flujo de verificación
            sellViewModel.checkCurrentUserStatus()
        }
    }

    /**
     * Diálogo de confirmación para convertirse en emprendedor.
     */
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text("Convertirse en emprendedor") },
            text = { Text("Usted está a punto de convertirse en emprendedor. ¿Está seguro?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmationDialog = false
                    showEntrepreneurDialog = true
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text("No")
                }
            }
        )
    }

    /**
     * Diálogo para ingresar el nombre del emprendimiento.
     */
    if (showEntrepreneurDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text("Nombre del emprendimiento") },
            text = {
                Column {
                    Text("Ingrese el nombre de su emprendimiento:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = entrepreneurshipName,
                        onValueChange = { sellViewModel.onEntrepreneurshipNameChange(it) },
                        placeholder = { Text("Ej: Dulzuras del Valle") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    sellViewModel.convertUserToSeller()
                },
                    enabled = entrepreneurshipName.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    /**
     * Diálogo de carga mostrado durante el procesamiento.
     */
    if (status == SellerCreationStatus.LOADING) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Procesando...") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = {}
        )
    }
}
