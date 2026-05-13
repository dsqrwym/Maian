package org.dsqrwym.shared.ui.components.product.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.add_to_cart
import maian.shared.generated.resources.quantity
import org.dsqrwym.shared.domain.product.SharedProductDetailVariant
import org.dsqrwym.shared.ui.components.containers.StateContent
import org.dsqrwym.shared.ui.components.containers.UiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedProductBottomCartBar(
    modifier: Modifier = Modifier,
    selectedVariant: SharedProductDetailVariant?,
    quantityText: String,
    canAddToCart: Boolean,
    addToCartUiState: UiState = UiState.Idle,
    onQuantityChange: (String) -> Unit,
    onQuantityStep: (Int) -> Unit,
    onAddToCartClick: () -> Unit = {},
) {
    val canEditQuantity = selectedVariant != null && addToCartUiState != UiState.Loading

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 260.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onQuantityStep(-1) },
                enabled = canEditQuantity,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.outline,
                )
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = null)
            }

            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 72.dp, max = 150.dp),
                value = quantityText,
                onValueChange = onQuantityChange,
                enabled = canEditQuantity,
                singleLine = true,
                label = { Text(stringResource(SharedRes.string.quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            IconButton(
                onClick = { onQuantityStep(1) },
                enabled = canEditQuantity,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.outline,
                )
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }

        ElevatedButton(
            modifier = Modifier
                .animateContentSize()
                .then(
                    if (addToCartUiState == UiState.Idle) Modifier.widthIn(min = 150.dp)
                    else Modifier
                ),
            enabled = canAddToCart && addToCartUiState == UiState.Idle,
            onClick = onAddToCartClick,
        ) {
            StateContent(state = addToCartUiState) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AddShoppingCart,
                        contentDescription = stringResource(SharedRes.string.add_to_cart),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(SharedRes.string.add_to_cart))
                }
            }
        }
    }
}
