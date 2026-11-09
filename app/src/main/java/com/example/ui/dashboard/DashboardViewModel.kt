package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.CafeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalCustomers: Int = 0,
    val todayIncome: Double = 0.0,
    val monthIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val totalCompletedApps: Int = 0,
    val totalPendingApps: Int = 0,
    val recentApplications: List<ApplicationRecordEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val netProfit: Double = 0.0
)

class DashboardViewModel(private val repository: CafeRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getCustomerCount(),
        repository.getTodayIncome(),
        repository.getMonthIncome(),
        repository.getTodayExpense(),
        repository.getCompletedApplicationsCount(),
        repository.getPendingApplicationsCount(),
        repository.getRecentApplications(5),
        repository.getRecentTransactions(6)
    ) { params: Array<Any> ->
        val customerCount = params[0] as Int
        val todayInc = params[1] as Double
        val monthInc = params[2] as Double
        val todayExp = params[3] as Double
        val completedApps = params[4] as Int
        val pendingApps = params[5] as Int
        @Suppress("UNCHECKED_CAST")
        val recentApps = params[6] as List<ApplicationRecordEntity>
        @Suppress("UNCHECKED_CAST")
        val recentTrans = params[7] as List<TransactionEntity>

        DashboardUiState(
            totalCustomers = customerCount,
            todayIncome = todayInc,
            monthIncome = monthInc,
            todayExpense = todayExp,
            totalCompletedApps = completedApps,
            totalPendingApps = pendingApps,
            recentApplications = recentApps,
            recentTransactions = recentTrans,
            netProfit = todayInc - todayExp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun updateApplicationStatus(app: ApplicationRecordEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateApplication(app.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun quickAddExpense(category: String, amount: Double, desc: String, method: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    category = category,
                    amount = amount,
                    description = desc,
                    paymentMethod = method,
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
