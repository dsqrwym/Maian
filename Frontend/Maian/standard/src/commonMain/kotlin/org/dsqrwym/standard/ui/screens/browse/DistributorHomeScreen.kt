package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.categories
import maian.shared.generated.resources.products
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailDistributor
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.standard.ui.viewmodels.browse.DistributorHomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DistributorHomeScreen(
    distributor: RetailDistributor,
    onNavigateBack: (() -> Unit)? = null,
    onProductClick: (String) -> Unit,
    onCategoryClick: (RetailCategory, List<RetailCategory>, String) -> Unit = { _, _, _ -> },
    viewModel: DistributorHomeViewModel = koinViewModel(),
) {
    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Text(distributor.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
    ) { padding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            DistributorHeader(distributor)
            PrimaryTabRow(selectedTabIndex = viewModel.selectedTab) {
                Tab(
                    selected = viewModel.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text(stringResource(SharedRes.string.products)) },
                    icon = { Icon(Icons.Outlined.Inventory2, contentDescription = null) },
                )
                Tab(
                    selected = viewModel.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text(stringResource(SharedRes.string.categories)) },
                    icon = { Icon(Icons.Outlined.Category, contentDescription = null) },
                )
            }
            when (viewModel.selectedTab) {
                0 -> ProductBrowseScreen(
                    scope = BrowseScope.DISTRIBUTOR,
                    distributorId = distributor.id,
                    onProductClick = onProductClick,
                )

                else -> CategoryBrowseScreen(
                    scope = BrowseScope.DISTRIBUTOR,
                    distributorId = distributor.id,
                    onProductClick = onProductClick,
                    onCategoryClick = onCategoryClick,
                )
            }
        }
    }
}

@Composable
private fun DistributorHeader(distributor: RetailDistributor) {
    Surface(tonalElevation = 2.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = distributor.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            distributor.secondaryText.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
