package org.dsqrwym.shared.util.clipboard

import androidx.compose.runtime.Composable

/**
 * Returns a function that copies data to the system clipboard.
 * Using a remembered lambda allows calling it from non-composable callbacks safely.
 */
@Composable
expect fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean
