/*
 * Copyright 2017 The Android Open Source Project
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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.samagra.customworkmanager.Data;
import com.samagra.customworkmanager.impl.model.Dependency;
import com.samagra.customworkmanager.impl.model.DependencyDao;
import com.samagra.customworkmanager.impl.model.SystemIdInfo;
import com.samagra.customworkmanager.impl.model.SystemIdInfoDao;
import com.samagra.customworkmanager.impl.model.WorkName;
import com.samagra.customworkmanager.impl.model.WorkNameDao;
import com.samagra.customworkmanager.impl.model.WorkProgress;
import com.samagra.customworkmanager.impl.model.WorkProgressDao;
import com.samagra.customworkmanager.impl.model.WorkSpec;
import com.samagra.customworkmanager.impl.model.WorkSpecDao;
import com.samagra.customworkmanager.impl.model.WorkTag;
import com.samagra.customworkmanager.impl.model.WorkTagDao;
import com.samagra.customworkmanager.impl.model.WorkTypeConverters;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.MIGRATION_3_4;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.MIGRATION_4_5;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.MIGRATION_6_7;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.VERSION_2;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.VERSION_3;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.VERSION_5;
import static com.samagra.customworkmanager.impl.WorkDatabaseMigrations.VERSION_6;
import static com.samagra.customworkmanager.impl.model.WorkTypeConverters.StateIds.COMPLETED_STATES;

/**
 * A Room database for keeping track of work states.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Database(entities = {
        Dependency.class,
        WorkSpec.class,
        WorkTag.class,
        SystemIdInfo.class,
        WorkName.class,
        WorkProgress.class},
        version = 7)
@TypeConverters(value = {Data.class, WorkTypeConverters.class})
public abstract class WorkDatabase extends RoomDatabase {

    @VisibleForTesting
    public static final String DB_NAME = "com.samagra.customworkmanager.workdb";
    // Delete rows in the workspec table that...
    private static final String PRUNE_SQL_FORMAT_PREFIX = "DELETE FROM workspec WHERE "
            // are completed...
            + "state IN " + COMPLETED_STATES + " AND "
            // and the minimum retention time has expired...
            + "(period_start_time + minimum_retention_duration) < ";
    // and all dependents are completed.
    private static final String PRUNE_SQL_FORMAT_SUFFIX = " AND "
            + "(SELECT COUNT(*)=0 FROM dependency WHERE "
            + "    prerequisite_id=id AND "
            + "    work_spec_id NOT IN "
            + "        (SELECT id FROM workspec WHERE state IN " + COMPLETED_STATES + "))";

    private static final long PRUNE_THRESHOLD_MILLIS = TimeUnit.DAYS.toMillis(7);

    /**
     * Creates an instance of the WorkDatabase.
     *
     * @param context         A context (this method will use the application context from it)
     * @param queryExecutor   An {@link Executor} that will be used to execute all async Room
     *                        queries.
     * @param useTestDatabase {@code true} to generate an in-memory database that allows main thread
     *                        access
     * @return The created WorkDatabase
     */
    public static WorkDatabase create(
            @NonNull Context context,
            @NonNull Executor queryExecutor,
            boolean useTestDatabase) {
        RoomDatabase.Builder<WorkDatabase> builder;
        if (useTestDatabase) {
            builder = Room.inMemoryDatabaseBuilder(context, WorkDatabase.class)
                    .allowMainThreadQueries();
        } else {
            builder = Room.databaseBuilder(context, WorkDatabase.class, DB_NAME);
        }

        return builder.setQueryExecutor(queryExecutor)
                .addCallback(generateCleanupCallback())
                .addMigrations(WorkDatabaseMigrations.MIGRATION_1_2)
                .addMigrations(
                        new WorkDatabaseMigrations.WorkMigration(context, VERSION_2, VERSION_3))
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(
                        new WorkDatabaseMigrations.WorkMigration(context, VERSION_5, VERSION_6))
                .addMigrations(MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build();
    }

    static Callback generateCleanupCallback() {
        return new Callback() {
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
                db.beginTransaction();
                try {
                    // Prune everything that is completed, has an expired retention time, and has no
                    // active dependents:
                    db.execSQL(getPruneSQL());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static String getPruneSQL() {
        return PRUNE_SQL_FORMAT_PREFIX + getPruneDate() + PRUNE_SQL_FORMAT_SUFFIX;
    }

    static long getPruneDate() {
        return System.currentTimeMillis() - PRUNE_THRESHOLD_MILLIS;
    }

    /**
     * @return The Data Access Object for {@link WorkSpec}s.
     */
    public abstract WorkSpecDao workSpecDao();

    /**
     * @return The Data Access Object for {@link Dependency}s.
     */
    public abstract DependencyDao dependencyDao();

    /**
     * @return The Data Access Object for {@link WorkTag}s.
     */
    public abstract WorkTagDao workTagDao();

    /**
     * @return The Data Access Object for {@link SystemIdInfo}s.
     */
    public abstract SystemIdInfoDao systemIdInfoDao();

    /**
     * @return The Data Access Object for {@link WorkName}s.
     */
    public abstract WorkNameDao workNameDao();

    /**
     * @return The Data Access Object for {@link WorkProgress}.
     */
    @NonNull
    public abstract WorkProgressDao workProgressDao();
}
