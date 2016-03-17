/*
 * PanelHolder.java
 *
 * Copyright (c) 2016 Auth0 (http://auth0.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.auth0.android.lock.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.auth0.android.lock.Configuration;
import com.auth0.android.lock.R;
import com.auth0.android.lock.events.SocialConnectionEvent;
import com.auth0.android.lock.views.interfaces.LockWidget;
import com.auth0.android.lock.views.interfaces.LockWidgetForm;
import com.squareup.otto.Bus;

public class ClassicPanelHolder extends RelativeLayout implements ModeSelectionView.ModeSelectedListener, LockWidgetForm, View.OnClickListener, LockWidget {

    private final Bus bus;
    private final Configuration configuration;
    private FormLayout formLayout;
    private ModeSelectionView modeSelectionView;
    private ChangePasswordFormView changePwdForm;
    private ActionButton actionButton;
    private LayoutParams termsParams;

    public ClassicPanelHolder(Context context) {
        super(context);
        bus = null;
        configuration = null;
    }

    public ClassicPanelHolder(Context context, Bus lockBus, Configuration configuration) {
        super(context);
        this.bus = lockBus;
        this.configuration = configuration;
        init();
    }

    private void init() {
        if (configuration.getDefaultDatabaseConnection() != null && configuration.isSignUpEnabled()) {
            RelativeLayout.LayoutParams switcherParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            switcherParams.addRule(ALIGN_PARENT_TOP, TRUE);
            modeSelectionView = new ModeSelectionView(getContext(), this);
            modeSelectionView.setId(R.id.com_auth0_lock_form_selector);
            addView(modeSelectionView, switcherParams);
        }

        formLayout = new FormLayout(this);
        formLayout.setId(R.id.com_auth0_lock_form_layout);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int verticalMargin = (int) getResources().getDimension(R.dimen.com_auth0_lock_widget_vertical_margin_medium);
        params.setMargins(0, verticalMargin, 0, verticalMargin);
        params.addRule(BELOW, R.id.com_auth0_lock_form_selector);
        params.addRule(ABOVE, R.id.com_auth0_lock_terms_layout);
        params.addRule(CENTER_IN_PARENT, TRUE);
        addView(formLayout, params);

        RelativeLayout.LayoutParams actionParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        actionButton = new ActionButton(getContext());
        actionButton.setId(R.id.com_auth0_lock_action_button);
        actionButton.setOnClickListener(this);
        addView(actionButton, actionParams);

        termsParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        termsParams.addRule(ABOVE, R.id.com_auth0_lock_action_button);
        View termsLayout = inflate(getContext(), R.layout.com_auth0_lock_terms_layout, null);
        termsLayout.setId(R.id.com_auth0_lock_terms_layout);
        addView(termsLayout, termsParams);
        onModeSelected(FormLayout.DatabaseForm.LOG_IN);
    }

    private void showChangePasswordForm(boolean show) {
        if (formLayout != null) {
            formLayout.setVisibility(show ? GONE : VISIBLE);
        }
        if (modeSelectionView != null) {
            modeSelectionView.setVisibility(show ? GONE : VISIBLE);
        }

        if (changePwdForm == null && show) {
            changePwdForm = new ChangePasswordFormView(this);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(CENTER_IN_PARENT, TRUE);
            addView(changePwdForm, params);
        } else if (changePwdForm != null && !show) {
            removeView(changePwdForm);
            changePwdForm = null;
        }
    }

    public boolean onBackPressed() {
        if (changePwdForm != null && changePwdForm.getVisibility() == VISIBLE) {
            showChangePasswordForm(false);
            return true;
        }
        boolean handled = formLayout != null && formLayout.onBackPressed();
        if (handled) {
            modeSelectionView.setVisibility(View.VISIBLE);
        }
        return handled;
    }

    /**
     * Displays a progress bar on top of the action button. This will also
     * enable or disable the action button.
     *
     * @param show whether to show or hide the action bar.
     */
    public void showProgress(boolean show) {
        if (actionButton != null) {
            actionButton.showProgress(show);
        }
        if (modeSelectionView != null) {
            modeSelectionView.setEnabled(!show);
        }
    }

    @Override
    public void onModeSelected(FormLayout.DatabaseForm mode) {
        formLayout.changeFormMode(mode);
        int height = (int) getResources().getDimension(R.dimen.com_auth0_lock_terms_height);
        termsParams.height = mode == FormLayout.DatabaseForm.SIGN_UP ? height : 0;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void onClick(View v) {
        Object event;
        if (changePwdForm != null) {
            event = changePwdForm.submitForm();
        } else {
            event = formLayout.onActionPressed();
        }
        if (event != null) {
            bus.post(event);
        }
    }

    @Override
    public void showChangePasswordForm() {
        showChangePasswordForm(true);
    }

    @Override
    public void onSocialLogin(SocialConnectionEvent event) {
        bus.post(event);
    }
}
