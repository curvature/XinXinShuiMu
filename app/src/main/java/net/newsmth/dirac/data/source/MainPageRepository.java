/*
 * Copyright 2016, The Android Open Source Project
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

package net.newsmth.dirac.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.http.response.TypedResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p/>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class MainPageRepository {

    private static MainPageRepository INSTANCE = null;

    private final MainPageDataSource[] mRemoteDataSourceQueue;

    private final MainPageDataSource mLocalDataSource;
    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    List<MainPageItem> mCachedMainPageItems;
    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests. Dirty means the data in the local
     * repository are considered stale.
     */
    boolean mCacheIsDirty = true;
    private List<MainPageRepositoryObserver> mObservers = new ArrayList<MainPageRepositoryObserver>();

    // Prevent direct instantiation.
    private MainPageRepository(@NonNull MainPageDataSource localDataSource,
                               @NonNull MainPageDataSource remoteDataSource,
                               @NonNull MainPageDataSource remoteDataSource2) {
        mLocalDataSource = localDataSource;
        mRemoteDataSourceQueue = new MainPageDataSource[2];
        mRemoteDataSourceQueue[0] = remoteDataSource;
        mRemoteDataSourceQueue[1] = remoteDataSource2;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link MainPageRepository} instance
     */
    public static MainPageRepository getInstance(MainPageDataSource tasksLocalDataSource,
                                                 MainPageDataSource tasksRemoteDataSource,
                                                 MainPageDataSource tasksRemoteDataSource2) {
        if (INSTANCE == null) {
            INSTANCE = new MainPageRepository(tasksLocalDataSource, tasksRemoteDataSource,
                    tasksRemoteDataSource2);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public void addContentObserver(MainPageRepositoryObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void removeContentObserver(MainPageRepositoryObserver observer) {
        mObservers.remove(observer);
    }

    private void notifyContentObserver() {
        for (MainPageRepositoryObserver observer : mObservers) {
            observer.onMainPageChanged();
        }
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first. This is done synchronously because it's used by the {@link MainPageLoader},
     * which implements the async mechanism.
     */
    @Nullable
    public TypedResponse<List<MainPageItem>> getMainPageItems() {

        TypedResponse<List<MainPageItem>> res = new TypedResponse<>();

        List<MainPageItem> tasks = null;

        if (!mCacheIsDirty) {
            // Respond immediately with cache if available and not dirty
            if (mCachedMainPageItems != null) {
                res.data = mCachedMainPageItems;
                res.from = TypedResponse.FROM_MEMORY;
                return res;
            } else {
                // Query the local storage if available.
                tasks = mLocalDataSource.getMainPageItems();
            }
        }

        // To simplify, we'll consider the local data source fresh when it has data.
        if (tasks == null || tasks.isEmpty()) {
            // Grab remote data if cache is dirty or local data not available.
            tasks = mRemoteDataSourceQueue[0].getMainPageItems();

            if (tasks == null) {
                tasks = mRemoteDataSourceQueue[1].getMainPageItems();
                if (tasks != null) {
                    MainPageDataSource temp = mRemoteDataSourceQueue[0];
                    mRemoteDataSourceQueue[0] = mRemoteDataSourceQueue[1];
                    mRemoteDataSourceQueue[1] = temp;
                }
            }

            if (tasks == null) {
                res.error = 1;
                if (mCacheIsDirty) {
                    if (mCachedMainPageItems == null) {
                        res.data = mLocalDataSource.getMainPageItems();
                        res.from = TypedResponse.FROM_LOCAL_DATABSE;
                    } else {
                        res.data = getCachedItems();
                        res.from = TypedResponse.FROM_MEMORY;
                    }
                    return res;
                }
            } else {
                res.data = tasks;
                res.from = TypedResponse.FROM_SERVER;
                saveTasksInLocalDataSource(tasks);
            }
        }

        processLoadedTasks(tasks);

        return res;

    }

    public boolean cachedItemsAvailable() {
        return mCachedMainPageItems != null && !mCacheIsDirty;
    }

    public List<MainPageItem> getCachedItems() {
        return mCachedMainPageItems;
    }

    private void saveTasksInLocalDataSource(List<MainPageItem> tasks) {
        if (tasks != null) {
            mLocalDataSource.saveMainPageItems(tasks);
        }
    }

    private void processLoadedTasks(List<MainPageItem> tasks) {
        mCachedMainPageItems = tasks;
        mCacheIsDirty = false;
    }

    public void refreshMainPageItems() {
        mCacheIsDirty = true;
        notifyContentObserver();
    }


    public interface MainPageRepositoryObserver {
        void onMainPageChanged();
    }
}
