package com.example.photonest.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.WorkerParameters
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.PendingOperationDao
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.repository.ICommentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingOperationDao: PendingOperationDao,
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val commentRepository: ICommentRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingOps = pendingOperationDao.getAllPendingNow()
        if (pendingOps.isEmpty()) return Result.success()

        var allSucceeded = true
        for (op in pendingOps) {
            val result = when (op.type) {
                "LIKE"     -> postRepository.likePost(op.targetId)
                "UNLIKE"   -> postRepository.unlikePost(op.targetId)
                "FOLLOW"   -> userRepository.followUser(op.targetId)
                "UNFOLLOW" -> userRepository.unfollowUser(op.targetId)
                "BOOKMARK" -> postRepository.bookmarkPost(op.targetId)
                "UNBOOKMARK" -> postRepository.unbookmarkPost(op.targetId)
                else -> NetworkResult.Error("Unknown operation type: ${op.type}")
            }
            when (result) {
                is NetworkResult.Success -> pendingOperationDao.deleteById(op.id)
                is NetworkResult.Error   -> {
                    pendingOperationDao.incrementRetry(op.id)
                    if (op.retryCount >= 5) {
                        // Give up after 5 retries
                        pendingOperationDao.deleteById(op.id)
                    }
                    allSucceeded = false
                }
                else -> allSucceeded = false
            }
        }
        return if (allSucceeded) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "PhotoNestSyncWorker"

        /** Schedule sync when network becomes available. */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
