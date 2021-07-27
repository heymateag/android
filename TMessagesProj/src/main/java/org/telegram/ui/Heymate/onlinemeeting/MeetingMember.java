package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.view.View;

import org.telegram.ui.Heymate.log.HMLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKVideoAspect;
import us.zoom.sdk.ZoomInstantSDKVideoCanvas;
import us.zoom.sdk.ZoomInstantSDKVideoView;

public class MeetingMember {

    private final ZoomInstantSDKUser mUser;
    private final String mName;
    private final String mUserId;

    private final boolean mIsServiceProvider;

    private final List<WeakReference<ZoomInstantSDKVideoView>> mViews = new ArrayList<>();

    protected MeetingMember(ZoomInstantSDKUser user, String serviceProviderId) {
        mUser = user;

        OnlineMeeting.UserInfo userInfo = new OnlineMeeting.UserInfo(user.getUserName());

        mName = userInfo.name;
        mUserId = userInfo.id;

        mIsServiceProvider = mUserId != null && mUserId.equals(serviceProviderId);
    }

    protected ZoomInstantSDKUser getZoomUser() {
        return mUser;
    }

    public String getName() {
        return mName;
    }

    public String getUserId() {
        return mUserId;
    }

    public boolean isServiceProvider() {
        return mIsServiceProvider;
    }

    public boolean isMuted() {
        return mUser.getAudioStatus().isMuted();
    }

    public boolean isVideoOn() {
        return mUser.getVideoStatus().isOn();
    }

    public View createView(Context context) {
        HMLog.d("MeetingMember", "createView. context exists=" + (context != null));
        // Create a new VideoView within your Activity
        ZoomInstantSDKVideoView videoView = new ZoomInstantSDKVideoView(context);

        // Retrieve the canvas from a specific user
        ZoomInstantSDKVideoCanvas canvas = mUser.getVideoCanvas();

        // Subscribe to a user’s video
        if (canvas != null) {
            canvas.subscribe(videoView, ZoomInstantSDKVideoAspect.ZoomInstantSDKVideoAspect_PanAndScan);
        }
        else {
            return null;
        }

        mViews.add(new WeakReference<>(videoView));

        return videoView;
    }

    public void releaseView(View view) {
        HMLog.d("MeetingMember", "releaseView. View type is " + view.getClass().getSimpleName());
        if (view instanceof ZoomInstantSDKVideoView) {
            ZoomInstantSDKVideoCanvas canvas = mUser.getVideoCanvas();

            if (canvas != null) {
                canvas.unSubscribe((ZoomInstantSDKVideoView) view);
            }
        }
    }

    protected void release() {
        ZoomInstantSDKVideoCanvas canvas = mUser.getVideoCanvas();
        HMLog.d("MeetingMember", "release. canvas exists=" + (canvas != null));

        if (canvas == null) {
            return;
        }

        for (WeakReference<ZoomInstantSDKVideoView> viewReference: mViews) {
            if (viewReference.get() != null) {
                canvas.unSubscribe(viewReference.get());
            }
        }

        mViews.clear();
    }

}
