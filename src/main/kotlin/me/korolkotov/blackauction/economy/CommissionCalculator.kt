package me.korolkotov.blackauction.economy

class CommissionCalculator(
    private val percent: Double,
    private val fixed: Double
) {
    init {
        require(percent in 0.0..100.0) {
            "Commission percent must be between 0 and 100"
        }
        require(fixed >= 0.0) {
            "Fixed commission must be >= 0"
        }
    }

    fun calculate(finalPrice: Double): CommissionResult {
        require(finalPrice >= 0.0) {
            "Final price must be >= 0"
        }

        val percentValue = finalPrice * (percent / 100.0)
        val totalCommission = percentValue + fixed
        val sellerReceives = (finalPrice - totalCommission).coerceAtLeast(0.0)

        return CommissionResult(
            finalPrice = finalPrice,
            percentCommission = percentValue,
            fixedCommission = fixed,
            totalCommission = totalCommission,
            sellerReceives = sellerReceives
        )
    }
}
