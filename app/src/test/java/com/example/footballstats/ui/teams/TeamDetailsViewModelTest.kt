package com.example.footballstats.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.model.TeamDetails
import com.example.footballstats.domain.model.TeamPlayer
import com.example.footballstats.domain.model.TeamSeason
import com.example.footballstats.domain.repository.*
import com.example.footballstats.domain.usecase.*
import com.example.footballstats.ui.common.TeamSummaryCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TeamDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val models = mutableListOf<TeamDetailsViewModel>()
    private val summary = Team(42, "Arsenal", "England", "logo")
    private val favorites = FakeFavoritesRepository()
    private val cache = TeamSummaryCache()
    private var load: suspend (Boolean) -> TeamDetailsResult = { detailsResult() }

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun model(id: Int? = 42): TeamDetailsViewModel {
        val repository = object : TeamDetailsRepository {
            override suspend fun getDetails(teamId: Int, refresh: Boolean): TeamDetailsResult {
                assertEquals(42, teamId)
                return load(refresh)
            }
        }
        return TeamDetailsViewModel(
            SavedStateHandle(if (id == null) emptyMap() else mapOf("teamId" to id)),
            GetTeamDetailsUseCase(repository), GetFavoriteTeamsUseCase(favorites),
            SetFavoriteTeamUseCase(favorites), cache
        ).also { models += it }
    }

    @Test fun loadsDetailsByIdWithoutListOrCache() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        assertEquals("Arsenal", model.uiState.value.content?.team?.name)
        assertFalse(model.uiState.value.isLoading)
        assertTrue(model.uiState.value.canChangeFavorite)
    }

    @Test fun cachedSummaryIsVisibleWhileRequestIsPending() = runTest(dispatcher) {
        cache.putAll(listOf(summary))
        load = { awaitCancellation() }
        val model = model()
        runCurrent()
        assertEquals(summary, model.uiState.value.content?.team)
        assertTrue(model.uiState.value.isLoading)
    }

    @Test fun offlineFavoriteRemainsVisibleAfterRemoval() = runTest(dispatcher) {
        favorites.teams.value = listOf(summary)
        load = { throw IOException() }
        val model = model()
        advanceUntilIdle()
        assertTrue(model.uiState.value.isFavorite)
        assertNotNull(model.uiState.value.errorMessage)
        model.toggleFavorite()
        advanceUntilIdle()
        assertFalse(model.uiState.value.isFavorite)
        assertEquals(summary, model.uiState.value.content?.team)
        assertTrue(favorites.teams.value.isEmpty())
        assertFalse(model.uiState.value.isSavingFavorite)
    }

    @Test fun refreshRecoversFromNetworkFailure() = runTest(dispatcher) {
        load = { throw IOException() }
        val model = model()
        advanceUntilIdle()
        assertNotNull(model.uiState.value.errorMessage)
        load = { refresh -> assertTrue(refresh); detailsResult() }
        model.refresh()
        advanceUntilIdle()
        assertNull(model.uiState.value.errorMessage)
        assertNotNull(model.uiState.value.content)
    }

    @Test fun replacingRequestDoesNotDisplayCancellationAsError() = runTest(dispatcher) {
        load = { awaitCancellation() }
        val model = model()
        runCurrent()
        load = { detailsResult() }
        model.refresh()
        advanceUntilIdle()
        assertNull(model.uiState.value.errorMessage)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test fun failedFavoriteWriteCanBeRetried() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        favorites.failWrite = true
        model.toggleFavorite()
        advanceUntilIdle()
        assertNotNull(model.uiState.value.favoriteError)
        assertFalse(model.uiState.value.isSavingFavorite)
        assertFalse(model.uiState.value.isFavorite)
        favorites.failWrite = false
        model.toggleFavorite()
        advanceUntilIdle()
        assertTrue(model.uiState.value.isFavorite)
        assertNull(model.uiState.value.favoriteError)
    }

    @Test fun invalidIdDoesNotStartRequest() = runTest(dispatcher) {
        load = { error("Must not request details") }
        val model = model(null)
        advanceUntilIdle()
        assertFalse(model.uiState.value.isLoading)
        assertFalse(model.uiState.value.canChangeFavorite)
        assertEquals("Не указан ID команды.", model.uiState.value.errorMessage)
    }

    @Test fun presentationContainsShareTextAndGroupedSeasons() {
        val ui = teamDetailsUiModel(summary, detailsResult())
        assertTrue(ui.shareText.contains("Страна: England"))
        assertTrue(ui.shareText.contains("Год основания: 1886"))
        assertFalse(ui.shareText.contains("null"))
        assertEquals(listOf("• Player"), ui.players)
        assertEquals("Годы сезонов: 2025, 2024", ui.tournaments.single().value)
        assertTrue(ui.sourceLabel!!.startsWith("Сохранённая копия"))
        assertNotNull(ui.notice)
    }

    private fun detailsResult() = TeamDetailsResult(
        details = TeamDetails(
            42, "Arsenal", "England", "ARS", 1886,
            "Emirates", "London", null, 60000, null,
            listOf(TeamPlayer(1, "Player")),
            listOf(TeamSeason(1, "League", 2024), TeamSeason(1, "League", 2025), TeamSeason(1, "League", 2024))
        ),
        updatedAt = 0, isFromCache = true, issue = TeamDetailsIssue.REFRESH_FAILED
    )

    private class FakeFavoritesRepository : FavoritesRepository {
        val teams = MutableStateFlow<List<Team>>(emptyList())
        var failWrite = false
        override fun observeTeams(): Flow<List<Team>> = teams
        override suspend fun setFavorite(team: Team, isFavorite: Boolean) {
            if (failWrite) throw IOException()
            teams.value = teams.value.filterNot { it.id == team.id } + if (isFavorite) listOf(team) else emptyList()
        }
    }
}
