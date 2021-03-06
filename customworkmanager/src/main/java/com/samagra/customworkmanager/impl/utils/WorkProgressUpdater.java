/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samagra.customworkmanager.impl.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.common.util.concurrent.ListenableFuture;
import com.samagra.customworkmanager.Data;
import com.samagra.customworkmanager.Logger;
import com.samagra.customworkmanager.ProgressUpdater;
import com.samagra.customworkmanager.WorkInfo.State;
import com.samagra.customworkmanager.impl.WorkDatabase;
import com.samagra.customworkmanager.impl.model.WorkProgress;
import com.samagra.customworkmanager.impl.utils.futures.SettableFuture;
import com.samagra.customworkmanager.impl.utils.taskexecutor.TaskExecutor;

import java.util.UUID;

/**
 * Persists {@link com.samagra.customworkmanager.ListenableWorker} progress in a {@link WorkDatabase}.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class WorkProgressUpdater implements ProgressUpdater {
    // Synthetic access
    static final String TAG = Logger.tagWithPrefix("WorkProgressUpdater");

    // Synthetic access
    final WorkDatabase mWorkDatabase;
    // Synthetic access
    final TaskExecutor mTaskExecutor;

    public WorkProgressUpdater(
            @NonNull WorkDatabase workDatabase,
            @NonNull TaskExecutor taskExecutor) {
        mWorkDatabase = workDatabase;
        mTaskExecutor = taskExecutor;
    }

    @NonNull
    @Override
    public ListenableFuture<Void> updateProgress(
            @NonNull final Context context,
            @NonNull final UUID id,
            @NonNull final Data data) {
        final SettableFuture<Void> future = SettableFuture.create();
        mTaskExecutor.executeOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                String workSpecId = id.toString();
                Logger.get().info(TAG, String.format("Updating progress for %s (%s)", id, data));
                mWorkDatabase.beginTransaction();
                try {
                    State state = mWorkDatabase.workSpecDao().getState(workSpecId);
                    if (state == null) {
                        Logger.get().warning(TAG,
                                String.format(
                                        "Ignoring setProgressAsync(...). WorkSpec (%s) does not "
                                                + "exist.",
                                        workSpecId));
                    } else if (state.isFinished()) {
                        Logger.get().warning(TAG,
                                String.format(
                                        "Ignoring setProgressAsync(...). WorkSpec (%s) has "
                                                + "finished execution.",
                                        workSpecId));
                    } else {
                        WorkProgress progress = new WorkProgress(workSpecId, data);
                        mWorkDatabase.workProgressDao().insert(progress);
                    }
                    future.set(null);
                    mWorkDatabase.setTransactionSuccessful();
                } catch (Throwable throwable) {
                    Logger.get().error(TAG, "Error updating Worker progress", throwable);
                    future.setException(throwable);
                } finally {
                    mWorkDatabase.endTransaction();
                }
            }
        });
        return future;
    }
}
