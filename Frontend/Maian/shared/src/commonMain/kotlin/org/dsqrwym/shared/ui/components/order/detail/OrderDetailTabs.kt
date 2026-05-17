package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailTabs(
    selectedTab: OrderDetailTab,
    onTabSelected: (OrderDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { OrderDetailTab.entries }
    PrimaryScrollableTabRow(
        modifier = modifier.fillMaxWidth(),
        selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
        edgePadding = 12.dp,
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(tab.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
