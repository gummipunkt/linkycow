sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object AddLink : Screen("add_link")
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: Int) = "link_detail/$linkId"
    }
} 