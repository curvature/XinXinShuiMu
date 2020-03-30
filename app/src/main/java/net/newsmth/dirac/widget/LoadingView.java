package net.newsmth.dirac.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.newsmth.dirac.R;

/**
 * Created by wang on 16/11/2017.
 */


public class LoadingView extends FrameLayout {

    public final View progressBar;
    public final TextView emptyView;

    {
        LayoutInflater.from(getContext()).inflate(R.layout.loading_view, this);
        progressBar = findViewById(R.id.progress);
        emptyView = findViewById(R.id.empty);
    }

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void showLoading() {
        setVisibility(VISIBLE);
        progressBar.setVisibility(VISIBLE);
        emptyView.setVisibility(GONE);
    }

    public void showEmpty(String emptyHint) {
        progressBar.setVisibility(GONE);
        emptyView.setText(emptyHint);
        emptyView.setVisibility(VISIBLE);
    }

    public void showEmpty(@StringRes int res) {
        showEmpty(getResources().getString(res));
    }

    public void hide() {
        setVisibility(GONE);
    }

}
