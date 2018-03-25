package com.straats.ben.friendrequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private Button signUpButton;
    private Button loginButton;
    private EditText nameTextBox;
    private EditText usernameTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordTextBox;

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

        confirmPasswordTextBox.setVisibility(View.INVISIBLE);
        nameTextBox.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    signMeUp(usernameTextBox.getText().toString(), nameTextBox.getText().toString(), passwordTextBox.getText().toString(), confirmPasswordTextBox.getText().toString());
                } else {
                    logMeIn(usernameTextBox.getText().toString(), passwordTextBox.getText().toString());
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    nameTextBox.setVisibility(View.INVISIBLE);
                    confirmPasswordTextBox.setVisibility(View.INVISIBLE);
                    loginButton.setText("Login");
                    signUpButton.setText("SignUp");
                    isSignUp = false;
                } else {
                    nameTextBox.setVisibility(View.VISIBLE);
                    confirmPasswordTextBox.setVisibility(View.VISIBLE);
                    loginButton.setText("Confirm");
                    signUpButton.setText("Back to login");
                    isSignUp = true;
                }
            }
        });
    }

    private void logMeIn(final String username, final String password) {

        HashMap<String, String> loginBody = new HashMap<>();
        loginBody.put("strategy", "local");
        loginBody.put("email", username);
        loginBody.put("password", password);

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Utils.accessToken = response.getString("accessToken");
                    Utils.userName = username;
                    Intent intent = new Intent(getApplicationContext(), Landing.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Bad credentials",Toast.LENGTH_LONG).show();
            }
        };

        Utils.volleyRequest(getApplication(), Utils.authenticationURL, Utils.loginTAG,
                Request.Method.POST, loginBody, callback);
    }

    private void signMeUp(final String username, String name, final String password, String repeatUsername) {
        if (username.length() < 3) {
            Toast.makeText(getApplicationContext(),"Username too short",Toast.LENGTH_LONG).show();
        }
        else if (name.length() < 3) {
            Toast.makeText(getApplicationContext(),"Name too short",Toast.LENGTH_LONG).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(),"Password too short",Toast.LENGTH_LONG).show();
        }
        else if (!password.equals(repeatUsername)) {
            Toast.makeText(getApplicationContext(),"Passwords don\'t match",Toast.LENGTH_LONG).show();
        }
        else {

            HashMap<String, String> loginBody = new HashMap<>();
            loginBody.put("email", username);
            loginBody.put("name", name);
            loginBody.put("password", password);

            Utils.VolleyCallback callback = new Utils.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    logMeIn(username, password);
                }

                @Override
                public void onFailure(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Can\'t create the user",Toast.LENGTH_LONG).show();
                }
            };

            Utils.volleyRequest(getApplication(), Utils.usersURL, Utils.signUpTAG,
                    Request.Method.POST, loginBody, callback);

        }
    }
}
