package org.dsqrwym.business.ui.components.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.confirm_delete_category
import maian.business.generated.resources.delete_warning_with_children
import maian.business.generated.resources.other_languages
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.ui.components.category.SharedCategoryPathRow
import org.dsqrwym.shared.ui.components.dialog.SharedConfirmDeleteDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun BusinessCategoryPath(path: List<String>, categoryName: String) {
    SharedCategoryPathRow(pathNames = path, currentName = categoryName)
}

@Composable
fun BusinessCategoryLanguages(languages: List<SharedCategoryTranslation>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "${stringResource(BusinessRes.string.other_languages)}:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (languages.isEmpty()) {
            Text(" -- ", style = MaterialTheme.typography.bodySmall)
        } else {
            languages.forEachIndexed { index, (langCode, name) ->
                Text(
                    "$langCode: $name",
                    style = MaterialTheme.typography.bodySmall
                )

                if (index != languages.lastIndex) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessConfirmDeleteCategories(
    categoryName: String,
    childrenCount: Int,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val content = """${stringResource(BusinessRes.string.confirm_delete_category, categoryName)}
        ${if (childrenCount > 0) stringResource(BusinessRes.string.delete_warning_with_children, childrenCount) else ""}
    """.trimIndent()
    SharedConfirmDeleteDialog(
        title = stringResource(SharedRes.string.delete),
        text = content,
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}
