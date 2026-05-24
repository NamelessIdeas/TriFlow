package com.app.triflow.domain.usecase.dashboard

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Dashboard
import com.app.triflow.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): Outcome<Dashboard> = repository.getDashboard()
}
