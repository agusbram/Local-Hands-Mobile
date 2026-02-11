package com.undef.localhandsbrambillafunes.ui.navigation

/**
 * Clase sellada que define las diferentes pantallas de la aplicación.
 * Cada pantalla se representa como un objeto que hereda de esta clase.
 *
 * @param route String que define la ruta de navegación para la pantalla.
 */
sealed class AppScreens(val route: String) {
    // Objetos que representan las pantallas de la aplicación

    // Pantalla de inicio/splash que se muestra al arrancar la aplicación.
    object SplashScreen: AppScreens("splash_screen")

    // Pantalla de inicio de sesión.
    object LoginScreen: AppScreens("login_screen")

    // Pantalla para la recuperación de contraseña olvidada.
    object ForgotPasswordScreen: AppScreens("forgot_password_screen")

    // Pantalla de registro de nuevos usuarios.
    object RegisterScreen: AppScreens("register_screen")

    // Pantalla principal de la aplicación.
    object HomeScreen: AppScreens("home_screen")

    // Pantalla de configuración de la aplicación.
    object SettingsScreen: AppScreens("settings_screen")

    // Pantalla de perfil del usuario.
    object ProfileScreen: AppScreens("profile_screen")

    // Pantalla de categorías de productos.
    object CategoryScreen: AppScreens("category_screen")

    // Pantalla de productos favoritos del usuario.
    object FavoritesScreen: AppScreens("favorite_screens")

    // Pantalla de búsqueda de productos.
    object SearchBarScreen: AppScreens("search_bar_screen")

    // Pantalla para emprender productos.
    object SellScreen: AppScreens("sell_screen")

    // Pantalla para editar productos
    object EditProductScreen: AppScreens("edit_product_screen/{productId}"){
        fun createRoute(productId: Int) = "edit_product_screen/$productId"
    }
    
    object ProductDetailScreen : AppScreens("product_detail_screen/{productId}") {
        fun createRoute(productId: Int) = "product_detail_screen/$productId"
    }
    
    object ProductOwnerDetailScreen : AppScreens("product_owner_detail_screen/{productId}") {
        fun createRoute(productId: Int) = "product_owner_detail_screen/$productId"
    }

    /**
     * Nueva pantalla para mostrar productos filtrados por categoría.
     * La ruta incluye un placeholder para el nombre de la categoría.
     */
    object ProductsByCategoryScreen : AppScreens("products_by_category/{categoryName}") {
        /**
         * Crea la ruta completa para navegar a esta pantalla, reemplazando el placeholder
         * con el nombre de la categoría real.
         * @param categoryName El nombre de la categoría a mostrar.
         * @return La ruta de navegación completa (ej. "products_by_category/Artesanías").
         */
        fun createRoute(categoryName: String) = "products_by_category/$categoryName"
    }
}