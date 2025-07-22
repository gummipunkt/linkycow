package com.wltr.linkycow.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object AddLink : Screen("addLink?id={id}") {
        fun createRoute(id: Int?) = "addLink?id=$id"
    }
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: Int) = "link_detail/$linkId"
    }
    object FilteredLinks : Screen("filteredLinks/{filterType}/{filterId}/{filterName}") {
        fun createRoute(filterType: String, filterId: Int, filterName: String) = "filteredLinks/$filterType/$filterId/$filterName"
    }
    object Settings : Screen("settings")
    object About : Screen("about")
} 