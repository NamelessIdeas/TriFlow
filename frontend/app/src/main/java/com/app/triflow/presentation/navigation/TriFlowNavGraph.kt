package com.app.triflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.triflow.presentation.feature.auth.LoginScreen
import com.app.triflow.presentation.feature.auth.RegisterScreen
import com.app.triflow.presentation.feature.auth.SplashScreen
import com.app.triflow.presentation.feature.auth.SplashViewModel
import com.app.triflow.presentation.feature.gtd.tasks.TaskDetailScreen
import com.app.triflow.presentation.feature.quiz.QuizScreen
import com.app.triflow.presentation.feature.home.HomeScaffold
import com.app.triflow.presentation.feature.secondbrain.editor.NoteEditorScreen

@Composable
fun TriFlowNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Splash,
        modifier = modifier,
    ) {
        composable<Destination.Splash> {
            val vm: SplashViewModel = hiltViewModel()
            val target by vm.target.collectAsStateWithLifecycle()
            SplashScreen(target = target) { destination ->
                navController.navigate(destination) {
                    popUpTo(Destination.Splash) { inclusive = true }
                }
            }
        }

        authGraph(
            onAuthenticated = {
                navController.navigate(Destination.HomeGraph) {
                    popUpTo(Destination.AuthGraph) { inclusive = true }
                }
            },
            onOpenQuiz = { navController.navigate(Destination.Quiz) },
            onNavigate = navController::navigate,
            onPopBack = navController::popBackStack,
        )

        composable<Destination.Quiz> {
            QuizScreen(onBack = navController::popBackStack)
        }

        composable<Destination.HomeGraph> {
            HomeScaffold(
                onLogout = {
                    navController.navigate(Destination.AuthGraph) {
                        popUpTo(0)
                    }
                },
                onOpenQuiz = { navController.navigate(Destination.Quiz) },
                onOpenTaskDetail = { id ->
                    navController.navigate(Destination.TaskDetail(id))
                },
                onOpenNote = { id ->
                    navController.navigate(Destination.NoteEditor(id))
                },
            )
        }

        composable<Destination.TaskDetail> { entry ->
            val route = entry.toRoute<Destination.TaskDetail>()
            TaskDetailScreen(
                taskId = route.taskId,
                onBack = navController::popBackStack,
            )
        }

        composable<Destination.NoteEditor> { entry ->
            val route = entry.toRoute<Destination.NoteEditor>()
            NoteEditorScreen(
                noteId = route.noteId,
                onBack = navController::popBackStack,
                onOpenTaskDetail = { id ->
                    navController.navigate(Destination.TaskDetail(id)) {
                        popUpTo(Destination.HomeGraph)
                    }
                },
            )
        }
    }
}

private fun NavGraphBuilder.authGraph(
    onAuthenticated: () -> Unit,
    onOpenQuiz: () -> Unit,
    onNavigate: (Destination) -> Unit,
    onPopBack: () -> Unit,
) {
    navigation<Destination.AuthGraph>(startDestination = Destination.Login) {
        composable<Destination.Login> {
            LoginScreen(
                onLoggedIn = onAuthenticated,
                onGoToRegister = { onNavigate(Destination.Register) },
                onOpenQuiz = onOpenQuiz,
            )
        }
        composable<Destination.Register> {
            RegisterScreen(
                onRegistered = onAuthenticated,
                onBack = onPopBack,
            )
        }
    }
}
