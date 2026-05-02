/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberKtorScopeClipboard(): (String) -> Unit
