/*
 * Copyright © 2018 Syberia Project
 * Date: 22.08.2018
 * Time: 21:21
 * Author: @alexxxdev <alexxxdev@ya.ru>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syberia.settings.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.syberia.SyberiaUtils;

import com.syberia.settings.preference.CustomSeekBarPreference;
import com.syberia.settings.preference.SecureSettingSwitchPreference;

public class GeneralTweaks extends SettingsPreferenceFragment implements OnPreferenceChangeListener{

    private ListPreference mRecentsComponentType;
    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private SecureSettingSwitchPreference mRoundedFwvals;
    private static final String RECENTS_COMPONENT_TYPE = "recents_component";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";

    
    private ListPreference mScreenOffAnimation;
    private ListPreference mVelocityFriction;
    private ListPreference mPositionFriction;
    private ListPreference mVelocityAmplitude;
    private static final String SCREEN_OFF_ANIMATION = "screen_off_animation";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.general_tweaks);
		ContentResolver resolver = getActivity().getContentResolver();
        mScreenOffAnimation = (ListPreference) findPreference(SCREEN_OFF_ANIMATION);
        int screenOffStyle = Settings.System.getInt(resolver, Settings.System.SCREEN_OFF_ANIMATION, 0);
        mScreenOffAnimation.setValue(String.valueOf(screenOffStyle));
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
        mScreenOffAnimation.setOnPreferenceChangeListener(this);

        // recents component type
        mRecentsComponentType = (ListPreference) findPreference(RECENTS_COMPONENT_TYPE);
        int type = Settings.System.getInt(resolver,
                Settings.System.RECENTS_COMPONENT, 0);
        mRecentsComponentType.setValue(String.valueOf(type));
        mRecentsComponentType.setSummary(mRecentsComponentType.getEntry());
        mRecentsComponentType.setOnPreferenceChangeListener(this);
	
	Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        mCornerRadius.setOnPreferenceChangeListener(this);
        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int cornerRadius = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                (int) (res.getDimension(resourceIdRadius) / density));
        mCornerRadius.setValue(cornerRadius / 1);

        // Rounded Content Padding
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        mContentPadding.setOnPreferenceChangeListener(this);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int contentPadding = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                (int) (res.getDimension(resourceIdPadding) / density));
        mContentPadding.setValue(contentPadding / 1);

        // Rounded use Framework Values
        mRoundedFwvals = (SecureSettingSwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);

        float velFriction = Settings.System.getFloatForUser(resolver,
                    Settings.System.STABILIZATION_VELOCITY_FRICTION,
                    0.1f,
                    UserHandle.USER_CURRENT);
        mVelocityFriction = (ListPreference) findPreference("stabilization_velocity_friction");
	mVelocityFriction.setValue(Float.toString(velFriction));
	mVelocityFriction.setSummary(mVelocityFriction.getEntry());
	mVelocityFriction.setOnPreferenceChangeListener(this);

	float posFriction = Settings.System.getFloatForUser(resolver,
                    Settings.System.STABILIZATION_POSITION_FRICTION,
                    0.1f,
                    UserHandle.USER_CURRENT);
            mPositionFriction = (ListPreference) findPreference("stabilization_position_friction");
	mPositionFriction.setValue(Float.toString(posFriction));
	mPositionFriction.setSummary(mPositionFriction.getEntry());
	mPositionFriction.setOnPreferenceChangeListener(this);

	int velAmplitude = Settings.System.getIntForUser(resolver,
                    Settings.System.STABILIZATION_VELOCITY_AMPLITUDE,
                    8000,
                    UserHandle.USER_CURRENT);
            mVelocityAmplitude = (ListPreference) findPreference("stabilization_velocity_amplitude");
	mVelocityAmplitude.setValue(Integer.toString(velAmplitude));
	mVelocityAmplitude.setSummary(mVelocityAmplitude.getEntry());
	mVelocityAmplitude.setOnPreferenceChangeListener(this);

    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = getContext().getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        mCornerRadius.setValue((int) (res.getDimension(resourceIdRadius) / density));
        mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mScreenOffAnimation) {
		String value = (String) newValue;
		Settings.System.putInt(resolver,
		Settings.System.SCREEN_OFF_ANIMATION, Integer.valueOf(value));
		int valueIndex = mScreenOffAnimation.findIndexOfValue(value);
		mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[valueIndex]);
	return true;
        } else if (preference == mRecentsComponentType) {
            int type = Integer.valueOf((String) newValue);
            int index = mRecentsComponentType.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_COMPONENT, type);
            mRecentsComponentType.setSummary(mRecentsComponentType.getEntries()[index]);
            if (type == 1) { // Disable swipe up gesture, if oreo type selected
               Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
            }
            SyberiaUtils.showSystemUiRestartDialog(getContext());
            return true;
	} else if (preference == mCornerRadius) {
            Settings.Secure.putInt(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    ((int) newValue) * 1);
        } else if (preference == mContentPadding) {
            Settings.Secure.putInt(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                    ((int) newValue) * 1);
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
        } else if (preference == mVelocityFriction) {
	    String value = (String) newValue;
	    Settings.System.putFloatForUser(resolver,
	             Settings.System.STABILIZATION_VELOCITY_FRICTION, Float.valueOf(value), UserHandle.USER_CURRENT);
	    int valueIndex = mVelocityFriction.findIndexOfValue(value);
	    mVelocityFriction.setSummary(mVelocityFriction.getEntries()[valueIndex]);
        } else if (preference == mPositionFriction) {
	    String value = (String) newValue;
	    Settings.System.putFloatForUser(resolver,
	             Settings.System.STABILIZATION_POSITION_FRICTION, Float.valueOf(value), UserHandle.USER_CURRENT);
	    int valueIndex = mPositionFriction.findIndexOfValue(value);
	    mPositionFriction.setSummary(mPositionFriction.getEntries()[valueIndex]);
        } else if (preference == mVelocityAmplitude) {
	    String value = (String) newValue;
	    Settings.System.putFloatForUser(resolver,
	             Settings.System.STABILIZATION_VELOCITY_AMPLITUDE, Float.valueOf(value), UserHandle.USER_CURRENT);
	    int valueIndex = mVelocityAmplitude.findIndexOfValue(value);
	    mVelocityAmplitude.setSummary(mVelocityAmplitude.getEntries()[valueIndex]);
       }
    	return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYBERIA;
    }
}