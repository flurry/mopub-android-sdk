package com.mopub.nativeads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.common.VisibleForTesting;

import java.util.WeakHashMap;

public class FlurryNativeAdRenderer implements
        MoPubAdRenderer<FlurryCustomEventNative.FlurryVideoEnabledNativeAd> {
    @NonNull private final FlurryViewBinder mViewBinder;

    // This is used instead of View.setTag, which causes a memory leak in 2.3
    // and earlier: https://code.google.com/p/android/issues/detail?id=18273
    @VisibleForTesting
    @NonNull final WeakHashMap<View, FlurryNativeViewHolder> mViewHolderMap;

    public FlurryNativeAdRenderer(@NonNull final FlurryViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<>();
    }

    @NonNull
    @Override
    public View createAdView(@NonNull final Activity activity, @Nullable final ViewGroup parent) {
        return LayoutInflater.from(activity).inflate(
                mViewBinder.staticViewBinder.layoutId, parent, false);
    }

    @Override
    public void renderAdView(@NonNull View view,
                             @NonNull FlurryCustomEventNative.FlurryVideoEnabledNativeAd ad) {
        FlurryNativeViewHolder flurryNativeViewHolder = mViewHolderMap.get(view);
        if (flurryNativeViewHolder == null) {
            flurryNativeViewHolder = FlurryNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, flurryNativeViewHolder);
        }

        update(flurryNativeViewHolder, ad);
        NativeRendererHelper.updateExtras(flurryNativeViewHolder.staticNativeViewHolder.mainView,
                mViewBinder.staticViewBinder.extras, ad.getExtras());
        setViewVisibility(flurryNativeViewHolder, View.VISIBLE);
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        return nativeAd instanceof FlurryCustomEventNative.FlurryVideoEnabledNativeAd;
    }

    private void update(final FlurryNativeViewHolder viewHolder,
                        final FlurryCustomEventNative.FlurryVideoEnabledNativeAd ad) {
        NativeRendererHelper.addTextView(viewHolder.staticNativeViewHolder.titleView,
                ad.getTitle());
        NativeRendererHelper.addTextView(viewHolder.staticNativeViewHolder.textView, ad.getText());
        NativeRendererHelper.addTextView(viewHolder.staticNativeViewHolder.callToActionView,
                ad.getCallToAction());
        NativeImageHelper.loadImageView(ad.getIconImageUrl(),
                viewHolder.staticNativeViewHolder.iconImageView);

        if (ad.isVideoAd()) {
            ad.loadVideoIntoView(viewHolder.videoView);
        } else {
            NativeImageHelper.loadImageView(ad.getMainImageUrl(),
                    viewHolder.staticNativeViewHolder.mainImageView);
        }
    }

    private void setViewVisibility(@NonNull final FlurryNativeViewHolder viewHolder,
                                   final int visibility) {
        if (viewHolder.staticNativeViewHolder.mainView != null) {
            viewHolder.staticNativeViewHolder.mainView.setVisibility(visibility);
        }
    }

    static class FlurryNativeViewHolder {
        private final StaticNativeViewHolder staticNativeViewHolder;
        private final ViewGroup videoView;

        private FlurryNativeViewHolder(final StaticNativeViewHolder staticNativeViewHolder,
                                       final ViewGroup videoView) {
            this.staticNativeViewHolder = staticNativeViewHolder;
            this.videoView = videoView;
        }

        static FlurryNativeViewHolder fromViewBinder(
                final View view,
                final FlurryViewBinder viewBinder) {
            StaticNativeViewHolder staticNativeViewHolder = StaticNativeViewHolder
                    .fromViewBinder(view, viewBinder.staticViewBinder);

            ViewGroup videoView = (ViewGroup) view.findViewById(viewBinder.videoViewId);

            return new FlurryNativeViewHolder(staticNativeViewHolder, videoView);
        }
    }
}