package com.example.android.medicinereminder.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static final int THREAD_COUNT = 3;

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    private static AppExecutors ourInstance = null;

    public static AppExecutors getInstance() {
        if (ourInstance == null) {
            ourInstance = new AppExecutors(
                    Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(THREAD_COUNT),
                    new MainThreadExecutor()
            );
        }
        return ourInstance;
    }

    public Executor diskIO() {
        return ourInstance.diskIO;
    }

    public Executor networkIO() {
        return ourInstance.networkIO;
    }

    public Executor mainThread() {
        return ourInstance.mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
