package my.noveldokusha.ui.composeViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import my.noveldokusha.R
import my.noveldokusha.ui.theme.ColorAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarSearch(
    focusRequester: FocusRequester,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClose: () -> Unit,
    onTextDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    placeholderText: String = stringResource(R.string.search_here),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Many hacks going on here to make it scrollBehavior compatible
    Box {
        Box(
            modifier
                .padding(8.dp)
                .background(containerColor, CircleShape)
                .matchParentSize()
        )
        TopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,

                ),
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            title = {
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    maxLines = 1,
                    colors = TextFieldDefaults.textFieldColors(
                        cursorColor = ColorAccent,
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onTertiary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (searchText.isNotBlank()) {
                            onTextDone(searchText)
                        }
                    }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = placeholderText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchText.isNotEmpty(),
                            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
                        ) {
                            IconButton(onClick = { onSearchTextChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}
