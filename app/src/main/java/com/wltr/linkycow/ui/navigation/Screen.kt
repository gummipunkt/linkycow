package com.wltr.linkycow.ui.navigation

/**
 * Sealed class defining all navigation destinations in the app.
 * Provides type-safe route definitions and helper methods for navigation.
 */
sealed class Screen(val route: String) {
    
    /**
     * Login/authentication screen
     */
    object Login : Screen("login")
    
    /**
     * Main dashboard screen with link list
     */
    object Main : Screen("main")
    
    /**
     * Add/edit link screen with optional link ID parameter
     */
    object AddLink : Screen("addLink?id={id}") {
        fun createRoute(id: Int?) = "addLink?id=$id"
    }
    
    /**
     * Link detail view screen
     */
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: Int) = "link_detail/$linkId"
    }
    
    /**
     * Filtered links screen (by collection or tag)
     */
    object FilteredLinks : Screen("filteredLinks/{filterType}/{filterId}/{filterName}") {
        fun createRoute(filterType: String, filterId: Int, filterName: String) = 
            "filteredLinks/$filterType/$filterId/$filterName"
    }
    
    /**
     * App settings screen
     */
    object Settings : Screen("settings")
    
    /**
     * About/info screen
     */
    object About : Screen("about")
} 