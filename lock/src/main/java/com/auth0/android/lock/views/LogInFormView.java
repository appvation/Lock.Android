/*
 * FormView.java
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
import android.view.View;

import com.auth0.android.lock.R;
import com.auth0.android.lock.events.DatabaseLoginEvent;
import com.auth0.android.lock.views.interfaces.LockWidgetForm;

public class LogInFormView extends FormView {

    private static final String TAG = LogInFormView.class.getSimpleName();
    private ValidatedUsernameInputView usernameEmailInput;
    private ValidatedInputView passwordInput;

    public LogInFormView(Context context) {
        super(context);
    }

    public LogInFormView(LockWidgetForm lockWidget) {
        super(lockWidget.getContext());
        init(lockWidget);
    }

    private void init(final LockWidgetForm lockWidget) {
        inflate(getContext(), R.layout.com_auth0_lock_login_form_view, this);
        usernameEmailInput = (ValidatedUsernameInputView) findViewById(R.id.com_auth0_lock_input_username_email);
        usernameEmailInput.chooseDataType(lockWidget.getConfiguration());
        passwordInput = (ValidatedInputView) findViewById(R.id.com_auth0_lock_input_password);
        passwordInput.setDataType(ValidatedInputView.DataType.PASSWORD);
        View changePasswordBtn = findViewById(R.id.com_auth0_lock_change_password_btn);
        changePasswordBtn.setVisibility(lockWidget.getConfiguration().isChangePasswordEnabled() ? View.VISIBLE : View.GONE);
        changePasswordBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lockWidget.showChangePasswordForm();
            }
        });
    }

    @Override
    public Object getActionEvent() {
        return new DatabaseLoginEvent(getUsernameOrEmail(), getPassword());
    }

    private String getUsernameOrEmail() {
        return usernameEmailInput.getText();
    }

    private String getPassword() {
        return passwordInput.getText();
    }

    @Override
    public boolean validateForm() {
        boolean valid = usernameEmailInput.validate(true);
        valid = passwordInput.validate(true) && valid;
        return valid;
    }

    @Nullable
    @Override
    public Object submitForm() {
        return validateForm() ? getActionEvent() : null;
    }

}
