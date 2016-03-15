/*
 * PasswordlessFormLayout.java
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
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.auth0.android.lock.R;
import com.auth0.android.lock.enums.PasswordlessMode;
import com.auth0.android.lock.views.interfaces.LockWidget;
import com.auth0.android.lock.views.interfaces.LockWidgetPasswordless;
import com.auth0.android.lock.views.interfaces.LockWidgetSocial;

public class PasswordlessFormLayout extends LinearLayout implements PasswordlessFormView.OnPasswordlessRetryListener {
    private final LockWidget lockWidget;
    private SocialView socialLayout;
    private TextView orSeparatorMessage;
    private PasswordlessFormView passwordlessLayout;

    public PasswordlessFormLayout(Context context) {
        super(context);
        lockWidget = null;
    }

    public PasswordlessFormLayout(LockWidget lockWidget) {
        super(lockWidget.getContext());
        this.lockWidget = lockWidget;
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        boolean showSocial = !lockWidget.getConfiguration().getSocialStrategies().isEmpty();
        boolean showPasswordless = lockWidget.getConfiguration().getDefaultPasswordlessStrategy() != null;

        if (showSocial) {
            addSocialLayout(showPasswordless);
        }
        if (showPasswordless) {
            if (showSocial) {
                addSeparator();
            }
            addPasswordlessLayout();
        }
    }

    private void addSocialLayout(boolean smallButtons) {
        SocialView socialLayout = new SocialView((LockWidgetSocial) lockWidget, smallButtons);
        addView(socialLayout);
    }

    private void addSeparator() {
        orSeparatorMessage = new TextView(getContext());
        PasswordlessMode passwordlessMode = lockWidget.getConfiguration().getPasswordlessMode();
        int stringRes = R.string.com_auth0_lock_passwordless_email_forms_separator;
        if (passwordlessMode != null && (passwordlessMode == PasswordlessMode.SMS_LINK || passwordlessMode == PasswordlessMode.SMS_CODE)) {
            stringRes = R.string.com_auth0_lock_passwordless_sms_forms_separator;
        }
        orSeparatorMessage.setText(stringRes);
        orSeparatorMessage.setGravity(Gravity.CENTER);
        int verticalPadding = (int) getResources().getDimension(R.dimen.com_auth0_lock_input_field_vertical_margin_small);
        int maxWidth = (int) getResources().getDimension(R.dimen.com_auth0_lock_max_widget_width);
        orSeparatorMessage.setPadding(0, verticalPadding, 0, verticalPadding);
        addView(orSeparatorMessage, maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void addPasswordlessLayout() {
        passwordlessLayout = new PasswordlessFormView((LockWidgetPasswordless) lockWidget, this);
        addView(passwordlessLayout);
    }

    /**
     * Should be called to update the form layout.
     *
     * @return true if it was consumed, false otherwise.
     */
    public boolean onBackPressed() {
        if (passwordlessLayout != null && passwordlessLayout.onBackPressed()) {
            if (socialLayout != null) {
                socialLayout.setVisibility(VISIBLE);
            }
            if (orSeparatorMessage != null) {
                orSeparatorMessage.setVisibility(VISIBLE);
            }
            return true;
        }
        return false;
    }

    public void codeSent() {
        if (passwordlessLayout != null) {
            if (socialLayout != null) {
                socialLayout.setVisibility(GONE);
            }
            if (orSeparatorMessage != null) {
                orSeparatorMessage.setVisibility(GONE);
            }
            passwordlessLayout.codeSent();
        }
    }

    /**
     * ActionButton has been clicked, and validation should be run on the current
     * visible form. If this validation passes, an action event will be returned.
     *
     * @return the action event of the current visible form or null if validation failed
     */
    @Nullable
    public Object onActionPressed() {
        View existingForm = getChildAt(getChildCount() == 1 ? 0 : 2);
        if (existingForm != null) {
            FormView form = (FormView) existingForm;
            return form.submitForm();
        }
        return null;
    }

    @Override
    public void onPasswordlessRetry() {
        if (socialLayout != null) {
            socialLayout.setVisibility(VISIBLE);
        }
        if (orSeparatorMessage != null) {
            orSeparatorMessage.setVisibility(VISIBLE);
        }
    }

    public void onCountryCodeSelected(String country, String dialCode) {
        passwordlessLayout.onCountryCodeSelected(country, dialCode);
    }
}
