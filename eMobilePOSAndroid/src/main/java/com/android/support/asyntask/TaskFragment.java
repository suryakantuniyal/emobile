package com.android.support.asyntask;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by guarionex on 6/6/17.
 */

public class TaskFragment extends Fragment {
    public interface TaskCallBacks {
        void onPreExecute();

        void onProgressUpdate(int percent);

        void onCancelled();

        void onPostExecute();
    }

    public interface RetainedFragmentTask {
        void setTaskCallBacks(TaskCallBacks callBacks);
    }

    private TaskCallBacks taskCallBacks;
    AsyncTask task;

    public static TaskFragment getInstance(RetainedFragmentTask task) {
        TaskFragment fragment = new TaskFragment();
        fragment.task = (AsyncTask) task;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        taskCallBacks = (TaskCallBacks) activity;
        ((RetainedFragmentTask)task).setTaskCallBacks(taskCallBacks);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        taskCallBacks = null;
    }
}
