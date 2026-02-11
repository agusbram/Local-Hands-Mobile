package com.undef.localhandsbrambillafunes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens

/**
 * Un elemento de lista de productos optimizado que usa AsyncImage para carga diferida
 * y reduce las recomposiciones innecesarias
 */
@Composable
fun ProductListItem(product : Product, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable() {
                navController.navigate(route = AppScreens.ProductDetailScreen.createRoute(product.id))
            },
        shape = RoundedCornerShape(corner = CornerSize(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            ProductImage(product = product)
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(text = product.name, style = typography.bodyLarge)
                Text(text = "Ver Detalle.", style = typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ProductImage(product: Product) {
    AsyncImage(
        model = product.images[0],
        contentDescription = product.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(8.dp)
            .size(80.dp)
            .clip(RoundedCornerShape(corner = CornerSize(16.dp)))
    )
}
