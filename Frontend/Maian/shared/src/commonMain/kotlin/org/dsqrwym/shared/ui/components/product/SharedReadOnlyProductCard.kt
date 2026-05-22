package org.dsqrwym.shared.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.out_of_stock
import maian.shared.generated.resources.product_image_with_name
import maian.shared.generated.resources.product_moq_value
import maian.shared.generated.resources.product_price_with_vat_value
import maian.shared.generated.resources.product_price_without_vat_value
import maian.shared.generated.resources.product_stock_count
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.components.containers.SharedOverlayContentBox
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedReadOnlyProductCard(
    name: String,
    title: String?,
    code: String,
    imageUrl: String?,
    minPrice: String?,
    minPriceIva: String?,
    totalStock: Int,
    minOrderQty: Int,
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    categoryTranslation: String? = null,
    nameTranslation: String? = null,
    titleTranslation: String? = null,
    statusText: String? = null,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null,
    showActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val platform = remember { getPlatform() }
    val hasStock = totalStock > 0
    val positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
    ) {
        Column {
            SharedOverlayContentBox(
                isLoading = isLoading,
                loadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .placeholderWithShimmer(isLoading),
                    )
                },
                overlaySurfaceColor = if (hasStock) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                topEndOverlay = {
                    if (!isLoading) {
                        SelectionContainer {
                            Text(
                                text = if (hasStock) {
                                    stringResource(SharedRes.string.product_stock_count, totalStock)
                                } else {
                                    stringResource(SharedRes.string.out_of_stock)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = if (hasStock) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                },
                            )
                        }
                    }
                },
            ) {
                val model: Any = imageUrl ?: SharedIcons.MaianLogo
                SharedAsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .then(if (imageUrl != null && onImageClick != null) Modifier.clickable(onClick = onImageClick) else Modifier),
                    model = model,
                    contentDescription = stringResource(SharedRes.string.product_image_with_name, name),
                    placeholder = rememberVectorPainter(SharedIcons.MaianLogo),
                    zoomable = false,
                    enableContextMenu = false,
                    contentScale = if (platform.type == PlatformType.Android) ContentScale.FillWidth else ContentScale.Fit,
                )
            }

            Column(Modifier.padding(SharedColumnLayout.padding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                var priceY by remember { mutableStateOf<Float?>(null) }
                var statusY by remember { mutableStateOf<Float?>(null) }

                val statusIsSecondLine by remember {
                    derivedStateOf {
                        val p = priceY
                        val s = statusY
                        p != null && s != null && s > p + 1f
                    }
                }

                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    SelectionContainer {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .placeholderWithShimmer(isLoading)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    categoryName?.takeIf { it.isNotBlank() }?.let { category ->
                        Spacer(Modifier.width(6.dp))
                        val categoryContent: @Composable () -> Unit = {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (categoryTranslation.isNullOrBlank()) {
                            categoryContent()
                        } else {
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = positionProvider,
                                tooltip = { PlainTooltip { Text(categoryTranslation) } },
                            ) { categoryContent() }
                        }
                    }
                }

                val nameContent: @Composable () -> Unit = {
                    SelectionContainer {
                        Text(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (nameTranslation.isNullOrBlank()) {
                    nameContent()
                } else {
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = positionProvider,
                        tooltip = { PlainTooltip { Text(nameTranslation) } },
                    ) {
                        nameContent()
                    }
                }

                title?.takeIf { it.isNotBlank() && it != name }?.let {
                    val titleContent: @Composable () -> Unit = {
                        SelectionContainer {
                            Text(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (titleTranslation.isNullOrBlank()) {
                        titleContent()
                    } else {
                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = positionProvider,
                            tooltip = { PlainTooltip { Text(titleTranslation) } },
                        ) {
                            titleContent()
                        }
                    }
                }

                HorizontalDivider()

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Column(modifier = Modifier.onGloballyPositioned {
                        priceY = it.positionInParent().y
                    }) {

                        SelectionContainer {
                            minPriceIva?.let {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = stringResource(SharedRes.string.product_price_with_vat_value, it),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        minPrice?.let {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = stringResource(SharedRes.string.product_price_without_vat_value, it),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .onGloballyPositioned {
                                statusY = it.positionInParent().y
                            },
                        horizontalAlignment = if (statusIsSecondLine) {
                            Alignment.Start
                        } else {
                            Alignment.End
                        }
                    ) {
                        SelectionContainer {
                            Text(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                text = stringResource(SharedRes.string.product_moq_value, minOrderQty),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        statusText?.takeIf { it.isNotBlank() }?.let {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }
                }
            }
            if (!isLoading && showActions) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(SharedColumnLayout.padding),
                    horizontalArrangement = Arrangement.End,
                    content = actions,
                )
            }
        }
    }
}
