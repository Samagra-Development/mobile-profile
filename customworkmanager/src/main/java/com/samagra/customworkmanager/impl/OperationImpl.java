/*
 * Copyright 2018 The Android Open Source Project
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

package com.samagra.customworkmanager.impl;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.samagra.customworkmanager.Operation;
import com.samagra.customworkmanager.impl.utils.futures.SettableFuture;

/**
 * A concrete implementation of a {@link Operation}.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class OperationImpl implements Operation {

    private final MutableLiveData<State> mOperationState;
    private final SettableFuture<State.SUCCESS> mOperationFuture;

    public OperationImpl() {
        mOperationState = new MutableLiveData<>();
        mOperationFuture = SettableFuture.create();
        // Mark the operation as in progress.
        setState(Operation.IN_PROGRESS);
    }

    @Override
    public @NonNull ListenableFuture<State.SUCCESS> getResult() {
        return mOperationFuture;
    }

    @Override
    public @NonNull
    LiveData<State> getState() {
        return mOperationState;
    }

    /**
     * Updates the {@link com.samagra.customworkmanager.Operation.State} of the {@link Operation}.
     *
     * @param state The current {@link Operation.State}
     */
    public void setState(@NonNull State state) {
        mOperationState.postValue(state);

        // Only terminal state get updates to the future.
        if (state instanceof State.SUCCESS) {
            mOperationFuture.set((State.SUCCESS) state);
        } else if (state instanceof State.FAILURE) {
            State.FAILURE failed = (State.FAILURE) state;
            mOperationFuture.setException(failed.getThrowable());
        }
    }
}
