package com.undef.localhandsbrambillafunes.data.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens

/**
 * Un elemento de lista para mostrar una única categoría.
 * Al hacer clic, navega a la pantalla de productos de esa categoría.
 */
@Composable
fun CategoryListItem(categoryName: String, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                // CORREGIDO: Ahora sí navega a la pantalla de productos por categoría
                navController.navigate(route = AppScreens.ProductsByCategoryScreen.createRoute(categoryName))
            },
        shape = RoundedCornerShape(corner = CornerSize(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp) // Aumentamos el padding para más espacio
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = "Category Icon",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = categoryName,
                style = typography.titleMedium, // Usamos un estilo de texto más grande
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}