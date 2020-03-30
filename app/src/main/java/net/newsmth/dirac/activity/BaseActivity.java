package net.newsmth.dirac.activity;

import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTaskDescription(new ActivityManager.TaskDescription(null, null, Color.WHITE));
    }
}