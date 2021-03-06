package com.bdxh.mqttconnection.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bdxh.mqttconnection.BaseActivity;
import com.bdxh.mqttconnection.R;
import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载中 界面
 */
public class DownloadingFrag extends Fragment {
    private static final String TAG = "rustAppDownloadFrag";

    private TaskAdapter taskAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Map<String, Long> mProgressUpdateTimeMap = new HashMap<>();
    private BaseActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_downloading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.download_task_re);

        activity = (BaseActivity) getActivity();
        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        DownloadCenter.getInstance(activity).addListener(mDownloadCenterListener);
        taskAdapter.setOnItemClick(new OnItemClick() {
            @Override
            public void onDelClick(final ControlCallBack callBack) {
                Log.d(TAG, "onDelClick: " + callBack);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.delete();
                    }
                }).start();
            }

            @Override
            public void onStartOrPauseClick(final ControlCallBack callBack) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack.isPaused() || callBack.isError()) {
                            DownloadCenter.getInstance(activity).continueDownload(callBack);
                        } else {
                            callBack.pause();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroy() {
        DownloadCenter.getInstance(activity).removeListener(mDownloadCenterListener);
        super.onDestroy();
    }

    private DownloadCenterListener mDownloadCenterListener = new DownloadCenterListener() {
        @Override
        public void onStart(final ControlCallBack callBack) {
            super.onStart(callBack);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    taskAdapter.addTask(callBack);
                }
            });
        }

        @Override
        public void onPaused(final String url) {
            super.onPaused(url);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    taskAdapter.updatePaused(url);
                }
            });

        }

        @Override
        public void onSuccess(final String url) {
            super.onSuccess(url);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    taskAdapter.updateSuccess(url);
                }
            });
        }

        @Override
        public void onError(final String url, Throwable e) {
            super.onError(url, e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    taskAdapter.updateError(url);
                }
            });
        }

        @Override
        public void onProgress(final String url, final long bytesRead, final long contentLength, final boolean done) {
            super.onProgress(url, bytesRead, contentLength, done);
            if (bytesRead == contentLength) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        taskAdapter.updateProgress(url, bytesRead, contentLength, done);
                    }
                });
            } else {
                Long last = mProgressUpdateTimeMap.get(url); // 避免频繁更新
                if (last == null || System.currentTimeMillis() - last > 100) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            taskAdapter.updateProgress(url, bytesRead, contentLength, done);
                        }
                    });
                }
                mProgressUpdateTimeMap.put(url, System.currentTimeMillis());
            }
        }

        @Override
        public void onDeleted(final String url) {
            super.onDeleted(url);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "取消任务", Toast.LENGTH_SHORT).show();
                    taskAdapter.updateDeleted(url);
                }
            });
        }
    };

    static class VH extends RecyclerView.ViewHolder {
        View root;
        TextView urlTv;
        TextView filenameTv;
        TextView statusTv;
        ImageView delIv;
        ImageView startOrPauseIv;
        ProgressBar pb;

        public VH(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            filenameTv = itemView.findViewById(R.id.filename_tv);
            urlTv = itemView.findViewById(R.id.url_tv);
            pb = itemView.findViewById(R.id.pb);
            delIv = itemView.findViewById(R.id.del_iv);
            startOrPauseIv = itemView.findViewById(R.id.start_and_pause_iv);
            statusTv = itemView.findViewById(R.id.status_tv);
        }
    }

    class TaskAdapter extends RecyclerView.Adapter<VH> {
        private List<ControlCallBack> callBackList;
        private OnItemClick onItemClick;

        public TaskAdapter() {
            callBackList = new ArrayList<>();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_task, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            final ControlCallBack callBack = callBackList.get(position);
            holder.statusTv.setText(callBack.getState().toString());
            holder.urlTv.setText(callBack.getUrl());
            holder.filenameTv.setText(callBack.getTargetFile().getName());
            holder.pb.setVisibility((callBack.isPaused() || callBack.isDownloading()) ? View.VISIBLE : View.INVISIBLE);
            if (callBack.getProgressTotal() > 0 && callBack.getProgressCurrent() >= 0) {
                double progress = callBack.getProgressCurrent() * 1.0 / callBack.getProgressTotal();
                holder.pb.setMax(1000);
                LogUtils.d(" 下载进度 " + progress);
                holder.pb.setProgress((int) (1000 * progress));
            } else {
                holder.pb.setVisibility(View.INVISIBLE);
            }
            holder.startOrPauseIv.setImageResource(callBack.isPaused() ? R.drawable.ic_launcher_background : R.drawable.ic_launcher_foreground);
            switch (callBack.getState()) {
                case CREATED:
                    break;
                case DOWNLOADING:
                    break;
                case PAUSING:
                    holder.startOrPauseIv.setVisibility(View.VISIBLE);
                    break;
                case PAUSED:
                    holder.startOrPauseIv.setVisibility(View.VISIBLE);
                    break;
                case DONE:
                    holder.startOrPauseIv.setVisibility(View.INVISIBLE);
                    break;
                case ERROR:
                    holder.startOrPauseIv.setImageResource(R.drawable.ic_launcher_background);
                    holder.startOrPauseIv.setVisibility(View.VISIBLE);
                    break;
                case DELETING:
                    break;
            }
            holder.delIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClick != null) {
                        onItemClick.onDelClick(callBack);
                    }
                }
            });
            holder.startOrPauseIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClick != null) {
                        onItemClick.onStartOrPauseClick(callBack);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return callBackList.size();
        }

        public void setOnItemClick(OnItemClick onItemClick) {
            this.onItemClick = onItemClick;
        }

        public void addTask(ControlCallBack callBack) {
            if (!callBackList.contains(callBack)) {
                callBackList.add(callBack);
            }
            notifyDataSetChanged();
        }

        public void updateSuccess(String url) {
            ControlCallBack callBack = getTaskByUrl(url);
            notifyDataSetChanged();
        }

        public void updatePaused(String url) {
            int index = getTaskIndexByUrl(url);
            if (index >= 0) {
                notifyItemChanged(index);
            }
        }

        public void updateError(String url) {
            ControlCallBack callBack = getTaskByUrl(url);
            if (callBack != null) {
                if (callBack.isDeletingState()) {
                    callBackList.remove(callBack);
                } else {
                    Log.d(TAG, "updateError: " + url);

                }
            }
            notifyDataSetChanged();
        }

        public void updateProgress(String url, long bytesRead, long contentLength, boolean done) {
            int index = getTaskIndexByUrl(url);
            if (index >= 0) {
                ControlCallBack callBack = callBackList.get(index);
                if (callBack != null) {
                    callBack.setProgressCurrent(bytesRead + callBack.getLocalFileStartByteIndex());
                    callBack.setProgressTotal(contentLength + callBack.getLocalFileStartByteIndex());
                }
                notifyItemChanged(index);
            }
        }

        public void updateDeleted(String url) {
            ControlCallBack callBack = getTaskByUrl(url);
            callBackList.remove(callBack);
            notifyDataSetChanged();
        }

        private ControlCallBack getTaskByUrl(String url) {
            for (ControlCallBack c : callBackList) {
                if (c.getUrl().equals(url)) {
                    return c;
                }
            }
            return null;
        }

        private int getTaskIndexByUrl(String url) {
            for (int i = 0; i < callBackList.size(); i++) {
                ControlCallBack c = callBackList.get(i);
                if (c.getUrl().equals(url)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public interface OnItemClick {
        void onDelClick(ControlCallBack callBack);

        void onStartOrPauseClick(ControlCallBack callBack);
    }

}
