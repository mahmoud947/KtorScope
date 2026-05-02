/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

@Composable
internal actual fun rememberKtorScopeClipboard(): (String) -> Unit {
    return remember {
        { text ->
            UIPasteboard.generalPasteboard.string = text
        }
    }
}
