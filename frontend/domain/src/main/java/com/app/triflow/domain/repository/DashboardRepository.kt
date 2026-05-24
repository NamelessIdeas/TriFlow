package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Dashboard

interface DashboardRepository {
    suspend fun getDashboard(): Outcome<Dashboard>
}
