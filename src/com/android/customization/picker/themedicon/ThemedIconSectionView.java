/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.customization.picker.themedicon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Switch;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.provider.Settings;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.android.wallpaper.R;
import com.android.wallpaper.picker.SectionView;

/**
 * The {@link SectionView} for themed icon section view
 */
public class ThemedIconSectionView extends SectionView {

    private Switch mSwitchView;
    private boolean mIsTIActivated;

    public ThemedIconSectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTitle(context.getString(R.string.themed_icon_title));
        mIsTIActivated = Settings.System.getInt(getContext().getContentResolver(), Settings.System.L3_THEMED_ICONS, 0) == 1;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwitchView = findViewById(R.id.themed_icon_toggle);
        mSwitchView.setChecked(mIsTIActivated);
        mSwitchView.setOnCheckedChangeListener((buttonView, isChecked) ->
                mSwitchView.setChecked(mIsTIActivated)
        );
        setOnClickListener(v -> modeToggleClicked());

    }

    /** Gets the switch view. */
    public Switch getSwitch() {
        return mSwitchView;
    }

    private void modeToggleClicked() {
        mIsTIActivated = !mIsTIActivated;
        viewActivated(mIsTIActivated);
    }

    @Override
    public void setEnabled(boolean enabled) {
        final int numOfChildViews = getChildCount();
        for (int i = 0; i < numOfChildViews; i++) {
            getChildAt(i).setEnabled(enabled);
        }
    }

    private void viewActivated(boolean isChecked) {
        Settings.System.putInt(getContext().getContentResolver(), Settings.System.L3_THEMED_ICONS, isChecked ? 1 : 0);
        if (mSectionViewListener != null) {
            mSectionViewListener.onViewActivated(getContext(), isChecked);
        }
        mSwitchView.setChecked(isChecked);
        new killLauncher3Task(getContext()).execute();
    }

    private static class killLauncher3Task extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public killLauncher3Task(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
              try {
        	  Thread.sleep(3000); //3s
              } catch (InterruptedException ie) {}

            try {
                ActivityManager am =
                        (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                IActivityManager ams = ActivityManager.getService();
                for (ActivityManager.RunningAppProcessInfo app: am.getRunningAppProcesses()) {
                    if ("com.android.launcher3".equals(app.processName)) {
                        ams.killApplicationProcess(app.processName, app.uid);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
