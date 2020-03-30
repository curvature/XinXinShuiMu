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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.http.response.TypedResponse;

import java.util.List;

public class MainPageLoader extends AsyncTaskLoader<TypedResponse<List<MainPageItem>>>
        implements MainPageRepository.MainPageRepositoryObserver {

    private MainPageRepository mRepository;

    public MainPageLoader(Context context, @NonNull MainPageRepository repository) {
        super(context);
        mRepository = repository;
    }

    @Override
    public TypedResponse<List<MainPageItem>> loadInBackground() {
        return mRepository.getMainPageItems();
    }

    @Override
    public void deliverResult(TypedResponse<List<MainPageItem>> data) {
        if (isReset()) {
            return;
        }

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        // Deliver any previously loaded data immediately if available.
        if (mRepository.cachedItemsAvailable()) {
            deliverResult(new TypedResponse<>(mRepository.getCachedItems()));
        }

        // Begin monitoring the underlying data source.
        mRepository.addContentObserver(this);

        if (takeContentChanged() || !mRepository.cachedItemsAvailable()) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mRepository.removeContentObserver(this);
    }

    @Override
    public void onMainPageChanged() {
        if (isStarted()) {
            forceLoad();
        }
    }
}
