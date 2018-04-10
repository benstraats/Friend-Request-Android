package com.straats.ben.friendrequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private Button signUpButton;
    private Button loginButton;
    private EditText nameTextBox;
    private EditText usernameTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordTextBox;
    private ProgressBar loginLoading;

    private boolean isSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isSignUp = false;

        signUpButton = findViewById(R.id.SignUpButton);
        loginButton = findViewById(R.id.LoginButton);
        nameTextBox = findViewById(R.id.NameTextBox);
        usernameTextBox = findViewById(R.id.UsernameTextBox);
        passwordTextBox = findViewById(R.id.PasswordTextBox);
        confirmPasswordTextBox = findViewById(R.id.ConfirmPasswordTextBox);
        loginLoading = findViewById(R.id.loginLoading);

        confirmPasswordTextBox.setVisibility(View.INVISIBLE);
        nameTextBox.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    signMeUp(usernameTextBox.getText().toString(), nameTextBox.getText().toString(),
                            passwordTextBox.getText().toString(), confirmPasswordTextBox.getText()
                                    .toString());
                } else {
                    logMeIn(usernameTextBox.getText().toString(), passwordTextBox.getText()
                            .toString());
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    nameTextBox.setVisibility(View.INVISIBLE);
                    confirmPasswordTextBox.setVisibility(View.INVISIBLE);
                    loginButton.setText(R.string.login_activity_login);
                    signUpButton.setText(R.string.login_activity_signup);
                    isSignUp = false;
                } else {
                    nameTextBox.setVisibility(View.VISIBLE);
                    confirmPasswordTextBox.setVisibility(View.VISIBLE);
                    loginButton.setText(R.string.login_activity_confirm);
                    signUpButton.setText(R.string.login_activity_back_to_login);
                    isSignUp = true;
                }
            }
        });
    }

    private void logMeIn(final String username, final String password) {

        if (username.length() < 3) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_username_too_short,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        else if (username.length() > 30) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_username_too_long,
                    Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_password_too_short,
                    Toast.LENGTH_SHORT).show();
        }
        else if (password.length() > 50) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_password_too_long,
                    Toast.LENGTH_SHORT).show();
        }

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Utils.accessToken = response.getString("accessToken");
                    Utils.userEmail = username;
                    getCurrentUserInfo(username);
                    Intent intent = new Intent(getApplicationContext(), Landing.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.bad_response,
                            Toast.LENGTH_SHORT).show();
                    hideLoading();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                hideLoading();
                Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                        Toast.LENGTH_SHORT).show();
            }
        };

        JSONObject body = new JSONObject();
        try {
            body.put("strategy", "local");
            body.put("email", username);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_failed_to_login,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        vw.request(getApplication(), vw.authenticationURL, vw.loginTAG,
                Request.Method.POST, body, callback);
    }

    private void signMeUp(final String username, String name, final String password,
                          String repeatUsername) {
        if (username.length() < 3) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_username_too_short,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        else if (username.length() > 30) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_username_too_long,
                    Toast.LENGTH_SHORT).show();
        }
        else if (name.length() < 3) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_name_too_short,
                    Toast.LENGTH_SHORT).show();
        }
        else if (name.length() > 100) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_name_too_long,
                    Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_password_too_short,
                    Toast.LENGTH_SHORT).show();
        }
        else if (password.length() > 50) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_password_too_long,
                    Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(repeatUsername)) {
            Toast.makeText(getApplicationContext(), R.string.login_activity_password_dont_match,
                    Toast.LENGTH_SHORT).show();
        }
        else {
            showLoading();

            final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

            JSONObject body = new JSONObject();
            try {
                body.put("email", username);
                body.put("name", name);
                body.put("password", password);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), R.string.login_activity_failed_to_signup,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    logMeIn(username, password);
                }

                @Override
                public void onFailure(VolleyError error) {
                    Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                            Toast.LENGTH_SHORT).show();
                    hideLoading();
                }
            };

            showLoading();
            vw.request(getApplication(), vw.usersURL, vw.signUpTAG,
                    Request.Method.POST, body, callback);

        }
    }

    private void getCurrentUserInfo(final String userEmail) {

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideLoading();
                try {
                    Utils.userID = response.getJSONArray("data").getJSONObject(0)
                            .getString("_id");
                    Utils.userName = response.getJSONArray("data").getJSONObject(0)
                            .getString("name");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.bad_response,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                        Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        };

        String url = vw.usersURL + "?email=" + userEmail;

        vw.request(getApplication(), url, vw.searchUsersTAG,
                Request.Method.GET, null, callback);
    }

    private void showLoading() {
        loginLoading.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
        signUpButton.setVisibility(View.INVISIBLE);
    }

    private void hideLoading() {
        loginLoading.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        signUpButton.setVisibility(View.VISIBLE);
    }
}
