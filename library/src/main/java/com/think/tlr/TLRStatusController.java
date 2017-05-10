package com.think.tlr;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.think.tlr.TLRLinearLayout.LoadStatus;
import com.think.tlr.TLRLinearLayout.RefreshStatus;

/**
 * Created by borney on 5/5/17.
 */
class TLRStatusController {
    private TLRCalculator mCalculator;
    /**
     * 刷新阀值(高度), 加载阀值
     */
    private int mRefreshThresholdHeight, mLoadThresholdHeight;

    /**
     * 刷新状态机
     */
    private RefreshStatus mRefreshStatus = RefreshStatus.IDLE;
    /**
     * 加载状态机
     */
    private LoadStatus mLoadStatus = LoadStatus.IDLE;

    /**
     * 是否释放刷新/加载
     */
    private boolean isReleaseRefresh = true, isReleaseLoad = true;

    /**
     * 是否是自动刷新
     */
    private boolean isAutoRefreshing = false;

    private boolean isRefreshing = false, isLoading = false;

    private TLRUiHandler mTLRUiHandler;

    TLRStatusController(TLRCalculator calculator, Context context, AttributeSet attrs) {
        mCalculator = calculator;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout);
        if (array == null) {
            Log.e("initAttrs array is null");
            return;
        }
        try {
            final int N = array.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = array.getIndex(i);
                if (index == R.styleable.TLRLinearLayout_releaseRefresh) {
                    isReleaseRefresh = array.getBoolean(index, isReleaseRefresh);
                } else if (index == R.styleable.TLRLinearLayout_releaseLoad) {
                    isReleaseLoad = array.getBoolean(index, isReleaseLoad);
                }
            }
        } finally {
            array.recycle();
        }
    }

    /**
     * calculate refresh view status
     *
     * @param moveDown view is move down now
     */
    public void calculateMoveRefreshStatus(boolean moveDown) {
        int totalOffsetY = mCalculator.getTotalOffsetY();
        //y 方向移动正值,切Load为初始状态
        if (totalOffsetY > 0 && mLoadStatus == LoadStatus.IDLE) {
            if (moveDown) {//view向下运动
                if (mRefreshStatus == RefreshStatus.IDLE && !isRefreshing) {
                    notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
                }
                if (totalOffsetY >= mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.PULL_DOWN) {
                    notifyRefreshStatusChanged(RefreshStatus.RELEASE_REFRESH);
                    if (isAutoRefreshing) {
                        notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
                        notifyRefreshStatusChanged(RefreshStatus.IDLE);
                    }
                }
            } else {//view向上运动
                if (totalOffsetY < mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
                    notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
                }
                if (mRefreshStatus == RefreshStatus.PULL_DOWN) {
                    notifyRefreshStatusChanged(RefreshStatus.IDLE);
                }
            }
        }
    }

    /**
     * calculate load view status
     *
     * @param moveUp view is move up now
     */
    public void calculateMoveLoadStatus(boolean moveUp) {
        int totalOffsetY = mCalculator.getTotalOffsetY();
        if (totalOffsetY < 0 && mRefreshStatus == RefreshStatus.IDLE) {
            if (moveUp) {//view向上运动
                if (mLoadStatus == LoadStatus.IDLE && !isLoading) {
                    notifyLoadStatusChanged(LoadStatus.PULL_UP);
                }
                if (Math.abs(totalOffsetY) >= mLoadThresholdHeight && mLoadStatus == LoadStatus.PULL_UP) {
                    notifyLoadStatusChanged(LoadStatus.RELEASE_LOAD);
                }
            } else {//view向下运动
                if (Math.abs(totalOffsetY) < mLoadThresholdHeight && mLoadStatus == LoadStatus.RELEASE_LOAD) {
                    notifyLoadStatusChanged(LoadStatus.PULL_UP);
                }
                if (mLoadStatus == LoadStatus.PULL_UP) {
                    notifyLoadStatusChanged(LoadStatus.IDLE);
                }
            }
        }
    }

    /**
     * calculate refresh view status when up motionevent
     */
    public void calculatorUpRefreshStatus() {
        if (mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
            if (isReleaseRefresh) {
                notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
            }
            notifyRefreshStatusChanged(RefreshStatus.IDLE);
        } else if (mRefreshStatus != RefreshStatus.IDLE) {
            notifyRefreshStatusChanged(RefreshStatus.IDLE);
        }
    }

    /**
     * calculate load view status when up motionevent
     */
    public void calculatorUpLoadStatus() {
        if (mLoadStatus == LoadStatus.RELEASE_LOAD) {
            if (isReleaseLoad) {
                notifyLoadStatusChanged(LoadStatus.LOADING);
            }
            notifyLoadStatusChanged(LoadStatus.IDLE);

        } else if (mLoadStatus != LoadStatus.IDLE) {
            notifyLoadStatusChanged(LoadStatus.IDLE);
        }
    }

    private void notifyRefreshStatusChanged(RefreshStatus status) {
        if (mRefreshStatus == status) {
            return;
        }
        mRefreshStatus = status;
        if (mRefreshStatus == RefreshStatus.REFRESHING) {
            isRefreshing = true;
        }
        mTLRUiHandler.onRefreshStatusChanged(mRefreshStatus);
    }

    private void notifyLoadStatusChanged(LoadStatus status) {
        if (mLoadStatus == status) {
            return;
        }
        mLoadStatus = status;
        if (mLoadStatus == LoadStatus.LOADING) {
            isLoading = true;
        }
        mTLRUiHandler.onLoadStatusChanged(mLoadStatus);
    }

    public void setTLRUiHandler(TLRUiHandler uiHandler) {
        mTLRUiHandler = uiHandler;
    }

    public void setAutoRefreshing(boolean autoRefreshing) {
        Log.v("setAutoRefreshing:" + autoRefreshing);
        isAutoRefreshing = autoRefreshing;
    }

    public boolean isAutoRefreshing() {
        return isAutoRefreshing;
    }

    public void setLoadThresholdHeight(int height) {
        mLoadThresholdHeight = height;
    }

    public void setRefreshThresholdHeight(int height) {
        mRefreshThresholdHeight = height;
    }

    public LoadStatus getLoadStatus() {
        return mLoadStatus;
    }

    public RefreshStatus getRefreshStatus() {
        return mRefreshStatus;
    }

    public boolean isReleaseRefresh() {
        return isReleaseRefresh;
    }

    public void setReleaseRefresh(boolean releaseRefresh) {
        isReleaseRefresh = releaseRefresh;
    }

    public boolean isReleaseLoad() {
        return isReleaseLoad;
    }

    public void setReleaseLoad(boolean releaseLoad) {
        isReleaseLoad = releaseLoad;
    }

    public void finishRefresh() {
        isRefreshing = false;
    }

    public void finishLoad() {
        isLoading = false;
    }
}
