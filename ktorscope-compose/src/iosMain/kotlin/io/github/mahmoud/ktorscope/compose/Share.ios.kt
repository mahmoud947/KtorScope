/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
internal actual fun rememberKtorScopeShare(): (String) -> Unit {
    return remember {
        { text ->
            val controller = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                controller,
                animated = true,
                completion = null,
            )
        }
    }
}
