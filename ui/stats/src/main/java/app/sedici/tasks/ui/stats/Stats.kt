package app.sedici.tasks.ui.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Stats() {
    Stats(viewModel = hiltViewModel())
}

@Composable
internal fun Stats(viewModel: StatsViewModel) {
    Scaffold(
        topBar = {
            StatsAppBar()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "Hello from ${stringResource(R.string.stats_title)}!")
        }
    }
}

@Composable
private fun StatsAppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.stats_title)) }
    )
}

@Preview
@Composable
private fun StatsAppBarPreview() {
    StatsAppBar()
}
