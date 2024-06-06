package io.shortway.kobankat.ui.revenuecatui

import cocoapods.PurchasesHybridCommonUI.RCPaywallViewController
import cocoapods.PurchasesHybridCommonUI.RCPaywallViewControllerDelegateProtocol
import io.shortway.kobankat.CustomerInfo
import io.shortway.kobankat.Package
import io.shortway.kobankat.models.StoreTransaction
import io.shortway.kobankat.toPurchasesErrorOrThrow
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import objcnames.classes.RCCustomerInfo
import objcnames.classes.RCPackage
import objcnames.classes.RCStoreTransaction
import platform.CoreGraphics.CGSize
import platform.Foundation.NSError
import platform.darwin.NSObject

internal fun PaywallListener.toIosPaywallDelegate(
    onHeightChange: (Int) -> Unit
): RCPaywallViewControllerDelegateProtocol =
    PaywallListenerWrapper(this, onHeightChange)

private class PaywallListenerWrapper(
    private val listener: PaywallListener,
    private val onHeightChange: (Int) -> Unit
) : RCPaywallViewControllerDelegateProtocol,
    NSObject() {

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun paywallViewController(
        controller: RCPaywallViewController,
        didStartPurchaseWithPackage: RCPackage
    ) = listener.onPurchaseStarted(didStartPurchaseWithPackage as Package)

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun paywallViewController(
        controller: RCPaywallViewController,
        didFinishPurchasingWithCustomerInfo: RCCustomerInfo,
        transaction: RCStoreTransaction?
    ) = listener.onPurchaseCompleted(
        didFinishPurchasingWithCustomerInfo as CustomerInfo,
        transaction as StoreTransaction
    )

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun paywallViewController(
        controller: RCPaywallViewController,
        didFailPurchasingWithError: NSError
    ) = listener.onPurchaseError(didFailPurchasingWithError.toPurchasesErrorOrThrow())

    override fun paywallViewControllerDidCancelPurchase(controller: RCPaywallViewController) =
        listener.onPurchaseCancelled()

    override fun paywallViewControllerDidStartRestore(controller: RCPaywallViewController) =
        listener.onRestoreStarted()

    @Suppress("CAST_NEVER_SUCCEEDS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun paywallViewController(
        controller: RCPaywallViewController,
        didFinishRestoringWithCustomerInfo: RCCustomerInfo
    ) = listener.onRestoreCompleted(didFinishRestoringWithCustomerInfo as CustomerInfo)

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun paywallViewController(
        controller: RCPaywallViewController,
        didFailRestoringWithError: NSError
    ) = listener.onRestoreError(didFailRestoringWithError.toPurchasesErrorOrThrow())


    override fun paywallViewController(
        controller: RCPaywallViewController,
        didChangeSizeTo: CValue<CGSize>
    ) {
        var height: Int? = null
        memScoped { height = didChangeSizeTo.ptr.pointed.height.toInt() }
        onHeightChange(height!!)
    }
}
