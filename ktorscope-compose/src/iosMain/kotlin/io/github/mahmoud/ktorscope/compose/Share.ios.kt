/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberKtorScopeShare(): (String) -> Unit {
    return remember {
        { text ->
            val controller = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )

            val presentingController = UIApplication.sharedApplication.keyWindow
                ?.rootViewController
                ?.topPresentedViewController()

            presentingController?.view?.let { view ->
                controller.popoverPresentationController()?.sourceView = view
                controller.popoverPresentationController()?.sourceRect = view.bounds
            }

            presentingController?.presentViewController(
                controller,
                animated = true,
                completion = null,
            )
        }
    }
}

private fun UIViewController.topPresentedViewController(): UIViewController {
    var controller = this
    while (controller.presentedViewController != null) {
        controller = controller.presentedViewController!!
    }
    return controller
}
