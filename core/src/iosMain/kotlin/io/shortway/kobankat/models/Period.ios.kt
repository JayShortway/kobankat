package io.shortway.kobankat.models

import cocoapods.RevenueCat.RCSubscriptionPeriod
import cocoapods.RevenueCat.RCSubscriptionPeriodUnit
import cocoapods.RevenueCat.RCSubscriptionPeriodUnitDay
import cocoapods.RevenueCat.RCSubscriptionPeriodUnitMonth
import cocoapods.RevenueCat.RCSubscriptionPeriodUnitWeek
import cocoapods.RevenueCat.RCSubscriptionPeriodUnitYear

public actual typealias Period = RCSubscriptionPeriod
public actual enum class PeriodUnit {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    UNKNOWN,
}

public actual val Period.value: Int
    get() = value().toInt()

public actual val Period.unit: PeriodUnit
    get() = unit().toUnit()

internal fun RCSubscriptionPeriodUnit.toUnit(): PeriodUnit =
    when (this) {
        RCSubscriptionPeriodUnitDay -> PeriodUnit.DAY
        RCSubscriptionPeriodUnitWeek -> PeriodUnit.WEEK
        RCSubscriptionPeriodUnitMonth -> PeriodUnit.MONTH
        RCSubscriptionPeriodUnitYear -> PeriodUnit.YEAR
        else -> PeriodUnit.UNKNOWN
    }