package com.example.den.vkconect;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 0;//READ_CONTACTS permissio
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    private ProgressDialog progressDialog = null;
    private UserLoginTask mAuthTask = null;
    private TextView link;
    private CheckBox checkBox;
    private String scope[] = new String[]{VKScope.WALL, VKScope.PHOTOS, VKScope.STATUS, VKScope.STATS};
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private Intent main;
    private Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Восстановление сохранённой сессии
        account.restore(this);

        mEmailLayout = findViewById(R.id.email_layout);
        mPasswordLayout = findViewById(R.id.password_layout);
        checkBox = findViewById(R.id.idChekBox);
        link = findViewById(R.id.infoTxtCredits);
        final Button autorizetFromVK = findViewById(R.id.idVKbutton);
        final Button signInButton = findViewById(R.id.email_sign_in_button);

        mEmailLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mEmailLayout.setError("");
            }
        });

        link.setMovementMethod(LinkMovementMethod.getInstance());//делаем ссылку кликабельной
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.isChecked()) {
                    signInButton.setEnabled(true);
                    autorizetFromVK.setEnabled(true);
                } else {
                    signInButton.setEnabled(false);
                    autorizetFromVK.setEnabled(true);
                }
            }
        });

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (areFieldsValid()) {
                    if (mAuthTask != null) {//проверяем запускался ли AsyncTask
                        return;
                    }
                    showProgress("Ожидайте, подключаемся...");
                    mAuthTask = new UserLoginTask(mEmailLayout.getEditText().getText().toString(), mPasswordLayout.getEditText().getText().toString());
                    mAuthTask.execute((Void) null);//запускаем поток
                }
            }
        });

        autorizetFromVK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.login(LoginActivity.this, scope);
            }
        });


//        // Set up the login form.
//        mEmailView = findViewById(R.id.name);
//
//        mPasswordView = findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
//

//
//        mLoginFormView = findViewById(R.id.login_form);
//        mProgressView = findViewById(R.id.login_progress);
//
//        link = findViewById(R.id.infoTxtCredits);
//        link.setMovementMethod(LinkMovementMethod.getInstance());
//
//        checkBox = findViewById(R.id.idChekBox);
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (checkBox.isChecked()) {
//                    mEmailSignInButton.setEnabled(true);
//                } else mEmailSignInButton.setEnabled(false);
//            }
//        });
    }//onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                account.access_token = res.accessToken;
                account.user_id = Long.parseLong(res.userId);
                account.save(LoginActivity.this);

                // Пользователь успешно авторизовался
                AuthorizationUtils.setAuthorized(LoginActivity.this);
                onLoginCompleted();
            }

            @SuppressLint("ShowToast")
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Snackbar.make(findViewById(R.id.idLinearLayout), "Введите данные или авторизуйтесь через VK.com!", Snackbar.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }//onActivityResult

    //	запуск маинАктивити
    private void onLoginCompleted() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }//onLoginCompleted

    //	It checks the email field
    private boolean areFieldsValid() {
        boolean mail = mEmailLayout != null && mEmailLayout.getEditText() != null //проверяем поле имейл на нул
                && !TextUtils.isEmpty(mEmailLayout.getEditText().getText()); //проверяем поле имейл на пустоту
//                && Patterns.EMAIL_ADDRESS.matcher(mEmailLayout.getEditText().getText()).matches();//проверяем на формат имейл адреса;
        boolean pass = mPasswordLayout != null && mPasswordLayout.getEditText() != null //проверяем поле пароль на нул
                && !TextUtils.isEmpty(mPasswordLayout.getEditText().getText()) //проверяем поле имейл на пустоту
                && mPasswordLayout.getEditText().getText().length() > 4;//проверяем длину пароля;
        if (!mail) {
            mEmailLayout.setError(getString(R.string.error_invalid_email));
            mPasswordLayout.setErrorEnabled(false);
            return false;
        } else if (!pass){
            mPasswordLayout.setError(getString(R.string.error_invalid_password));
            mEmailLayout.setErrorEnabled(false);
            return false;
        }else{
            mEmailLayout.setErrorEnabled(false);
            mPasswordLayout.setErrorEnabled(false);
            return true;
        }
    }
//===============================================
//    private void populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return;
//        }
//
//    }
//
//    @SuppressLint("ObsoleteSdkInt")
//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
//    }


    //    /**
//     * Attempts to sign in or register the account specified by the login form.
//     * If there are form errors (invalid email, missing fields, etc.), the
//     * errors are presented and no actual login attempt is made.
//     */
//    private void attemptLogin() {
////        if (mAuthTask != null) {//проверяем запускался ли AsyncTask
////            return;
////        }
//
////        //сброс ошибок
////        mEmailView.setError(null);
////        mPasswordView.setError(null);
//
//        String email = mEmailView.getText().toString();
//        String password = mPasswordView.getText().toString();
//
//        boolean cancel = false;
////        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            //окошко с ошибкой
//            focusView.requestFocus();
//        } else {
//
//            showProgress(true);//показываем прогресс
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);//запускаем поток
//        }
//    }
////=================================================================================================
//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }
//
//    private boolean isPasswordValid(String password) {
//        //TODO: Replace this with your own logic
//        return password.length() > 4;
//    }
////=================================================================================================
//    //Показывает прогресс выполнения и скрывает форму входа.
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
//    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(LoginActivity.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mEmailView.setAdapter(adapter);
//    }

    //
//    private interface ProfileQuery {
//        String[] PROJECTION = {
//                ContactsContract.CommonDataKinds.Email.ADDRESS,
//                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
//        };
//
//        int ADDRESS = 0;
//        int IS_PRIMARY = 1;
//    }
//=================================================================================================
//    /**
//     * Represents an asynchronous login/registration task used to authenticate
//     * the user.
//     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {//если емаил есть в первой части
                    //Учетная запись существует, возвращает true, если пароль совпадает.
                    return pieces[1].equals(mPassword);//если пароль есть во второй чаcти, то true
                }
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            hideProgress();
            if (success) {
                AuthorizationUtils.setAuthorized(LoginActivity.this);//ставим метку что авторизировались
                onLoginCompleted();//запуск маинАктивити
            } else {
                Snackbar.make(findViewById(R.id.idLinearLayout), "Аккаунт не найден!", Snackbar.LENGTH_LONG).show();
                mEmailLayout.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            hideProgress();
        }
    }//UserLoginTask

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showProgress(String text) {
        if (progressDialog == null) {
            try {
                progressDialog = ProgressDialog.show(this, "", text);
                progressDialog.setCancelable(false);
            } catch (Exception e) {

            }
        }
    }
}

