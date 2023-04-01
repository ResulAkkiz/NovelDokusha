package my.noveldokusha.ui.screens.databaseSearch

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.noveldokusha.R
import my.noveldokusha.ui.composeViews.AnimatedTransition
import my.noveldokusha.ui.composeViews.MyButton
import my.noveldokusha.ui.composeViews.TopAppBarSearch
import my.noveldokusha.ui.screens.sourceCatalog.ToolbarMode
import my.noveldokusha.ui.theme.InternalTheme
import my.noveldokusha.ui.theme.Success500
import my.noveldokusha.ui.theme.Themes

@Composable
fun CheckBoxCategory(
    state: ToggleableState,
    text: String,
    onStateChanged: (state: ToggleableState) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkedColor by animateColorAsState(
        targetValue = when (state) {
            ToggleableState.Off -> Success500
            ToggleableState.On -> Success500
            ToggleableState.Indeterminate -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(250)
    )

    val onClick = {
        val nextState = when (state) {
            ToggleableState.Off -> ToggleableState.On
            ToggleableState.On -> ToggleableState.Indeterminate
            ToggleableState.Indeterminate -> ToggleableState.Off
        }
        onStateChanged(nextState)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() }
    ) {
        TriStateCheckbox(
            state = state,
            onClick = onClick,
            colors = CheckboxDefaults.colors(
                checkedColor = checkedColor,
                uncheckedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                disabledCheckedColor = checkedColor.copy(alpha = 0.25f),
                disabledUncheckedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                disabledIndeterminateColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                checkmarkColor = MaterialTheme.colorScheme.inverseOnSurface,
            )
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(4.dp)
                .weight(1f)
        )
    }
}

@Composable
fun SearchGenres(
    list: SnapshotStateList<GenreItem>,
    onSearchClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        MyButton(
            text = stringResource(id = R.string.search_with_filters),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            onClick = onSearchClick,
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            itemsIndexed(list) { index, it ->
                CheckBoxCategory(
                    state = it.state,
                    text = it.genre,
                    onStateChanged = { state -> list[index] = it.copy(state = state) }
                )
            }
        }
    }
}

@Composable
private fun ToolbarMain(
    title: String,
    subtitle: String,
    onSearch: () -> Unit,
    topPadding: Dp,
    height: Dp
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = topPadding, bottom = 0.dp, start = 12.dp, end = 12.dp)
            .height(height)
    ) {

        // Fake button to center text
        IconButton(onClick = { }, enabled = false) {}

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium
            )
        }

        IconButton(onClick = onSearch) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_search_24),
                contentDescription = stringResource(R.string.search_for_title)
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSearchView(
    title: String,
    subtitle: String,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    genresList: SnapshotStateList<GenreItem>,
    onTitleSearchClick: (text: String) -> Unit,
    onGenreSearchClick: () -> Unit,
) {
    val focusManager by rememberUpdatedState(newValue = LocalFocusManager.current)
    val focusRequester = remember { FocusRequester() }
    var toolbarMode by rememberSaveable { mutableStateOf(ToolbarMode.MAIN) }

    Column {
        AnimatedTransition(targetState = toolbarMode) { target ->
            when (target) {
                ToolbarMode.MAIN -> ToolbarMain(
                    title = title,
                    subtitle = subtitle,
                    onSearch = { toolbarMode = ToolbarMode.SEARCH },
                    topPadding = 8.dp,
                    height = 56.dp
                )
                ToolbarMode.SEARCH -> TopAppBarSearch(
                    focusRequester = focusRequester,
                    searchText = searchText,
                    onSearchTextChange = onSearchTextChange,
                    onClose = {
                        focusManager.clearFocus()
                        toolbarMode = ToolbarMode.MAIN
                    },
                    onTextDone = { onTitleSearchClick(searchText) },
                    placeholderText = stringResource(R.string.search_by_title),
                )
            }
        }
        SearchGenres(list = genresList, onSearchClick = onGenreSearchClick)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewView() {
    val list = remember {
        mutableStateListOf<GenreItem>(
            GenreItem("Fantasy", "fn", ToggleableState.On),
            GenreItem("Horror", "fn", ToggleableState.Off),
            GenreItem("Honesty", "fn", ToggleableState.Off),
            GenreItem("Drama", "fn", ToggleableState.Indeterminate),
            GenreItem("Historical", "fn", ToggleableState.Off),
            GenreItem("Crime", "fn", ToggleableState.Off),
            GenreItem("Seinen", "fn", ToggleableState.Off),
            GenreItem("Action", "fn", ToggleableState.Off),
            GenreItem("Fantasy", "fn", ToggleableState.Off),
            GenreItem("Horror", "fn", ToggleableState.On),
            GenreItem("Honesty", "fn", ToggleableState.On),
            GenreItem("Drama", "fn", ToggleableState.Off),
            GenreItem("Historical", "fn", ToggleableState.Off),
            GenreItem("Crime", "fn", ToggleableState.Off),
            GenreItem("Seinen", "fn", ToggleableState.Off),
            GenreItem("Action", "fn", ToggleableState.Off),
            GenreItem("Horror", "fn", ToggleableState.Off),
            GenreItem("Honesty", "fn", ToggleableState.Off),
            GenreItem("Drama", "fn", ToggleableState.Off),
            GenreItem("Historical", "fn", ToggleableState.Off),
            GenreItem("Crime", "fn", ToggleableState.Off),
            GenreItem("Seinen", "fn", ToggleableState.Off),
            GenreItem("Action", "fn", ToggleableState.Off)
        )
    }
    var searchText by remember { mutableStateOf("hero") }

    InternalTheme(Themes.DARK) {
        DatabaseSearchView(
            title = "Database",
            subtitle = "Baka-Updates",
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            genresList = list,
            onTitleSearchClick = {},
            onGenreSearchClick = {}
        )
    }
}

