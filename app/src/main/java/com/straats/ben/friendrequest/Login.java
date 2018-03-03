package com.straats.ben.friendrequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    }

    protected void loginClick(View v) {
        if (isSignUp) {
            Toast.makeText(getApplicationContext(),"Signing you up",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"Logging you in",Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, Landing.class);
        startActivity(intent);
    }

    protected void signUpClick(View v) {
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
}
