package org.dsqrwym.shared.ui.components.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_detail_coming_soon
import maian.shared.generated.resources.order_number_value
import maian.shared.generated.resources.orders
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailPlaceholderScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
) {
    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Text(
                text = stringResource(SharedRes.string.orders),
                style = MaterialTheme.typography.titleMedium,
            )
        },
    ) { padding, _ ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(SharedRes.string.order_number_value, orderId),
                style = MaterialTheme.typography.labelMedium,
            )
            SharedPlainNotFoundPlaceholder(stringResource(SharedRes.string.order_detail_coming_soon))
        }
    }
}
