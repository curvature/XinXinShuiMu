package net.newsmth.dirac.data.source.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.data.source.MainPageDataSource;
import net.newsmth.dirac.http.parser.MainPageParser;
import net.newsmth.dirac.http.response.TypedResponse;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.RetrofitUtils;

import java.util.List;

public class MainPageRemoteDataSource implements MainPageDataSource {

    private static MainPageRemoteDataSource INSTANCE;

    private MainPageRemoteDataSource(@NonNull Context context) {
    }

    public static MainPageRemoteDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MainPageRemoteDataSource(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public List<MainPageItem> getMainPageItems() {
        try {
            TypedResponse<List<MainPageItem>> res = new MainPageParser()
                    .parseResponse(RetrofitUtils.create(ApiService.class).getMainPage().blockingSingle());
            return res.data;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveMainPageItems(List<MainPageItem> mainPageItems) {
        throw new UnsupportedOperationException();
    }
}
