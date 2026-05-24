package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.remote.api.DashboardApi
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Dashboard
import com.app.triflow.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi,
    private val executor: ApiCallExecutor,
) : DashboardRepository {

    override suspend fun getDashboard(): Outcome<Dashboard> =
        executor(mapper = { it.toDomain() }) { api.get() }
}
