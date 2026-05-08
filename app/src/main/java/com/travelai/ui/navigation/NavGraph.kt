package com.travelai.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelai.ui.chat.ChatScreen
import com.travelai.ui.history.HistoryScreen
import com.travelai.ui.planner.TripPlannerScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TravelAiRoutes.PLANNER_ROUTE,
        modifier = modifier
    ) {
        composable(route = TravelAiRoutes.PLANNER_ROUTE) {
            TripPlannerScreen(
                onOpenChat = {
                    navController.navigate(TravelAiRoutes.CHAT_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onCreateItinerary = { draftPrompt ->
                    navController.navigate(TravelAiRoutes.chatRoute(draftPrompt = draftPrompt))
                }
            )
        }

        composable(route = TravelAiRoutes.CHAT_ROUTE) {
            ChatScreen(
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.CHAT_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.SESSION_ID_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TravelAiRoutes.DRAFT_PROMPT_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ChatScreen(
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.HISTORY_ROUTE) {
            HistoryScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                },
                onSessionClick = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId))
                }
            )
        }
    }
}

private object TravelAiRoutes {
    const val PLANNER_ROUTE = "planner"
    const val CHAT_ROUTE = "chat"
    const val HISTORY_ROUTE = "history"
    const val SESSION_ID_ARG = "sessionId"
    const val DRAFT_PROMPT_ARG = "draftPrompt"
    const val CHAT_ROUTE_WITH_ARGS =
        "$CHAT_ROUTE?$SESSION_ID_ARG={$SESSION_ID_ARG}&$DRAFT_PROMPT_ARG={$DRAFT_PROMPT_ARG}"

    fun chatRoute(
        sessionId: Long? = null,
        draftPrompt: String? = null
    ): String {
        val arguments = buildList {
            sessionId?.let { add("$SESSION_ID_ARG=$it") }
            draftPrompt
                ?.takeIf { it.isNotBlank() }
                ?.let { add("$DRAFT_PROMPT_ARG=${Uri.encode(it)}") }
        }
        return if (arguments.isEmpty()) {
            CHAT_ROUTE
        } else {
            "$CHAT_ROUTE?${arguments.joinToString("&")}"
        }
    }
}
