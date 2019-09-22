package com.github.helloiampau.petsapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.helloiampau.petsapp.Commands;
import com.github.helloiampau.petsapp.R;

public class LoginForm extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View login = inflater.inflate(R.layout.login, container, false);

    EditText username = login.findViewById(R.id.usernameTextEdit);
    if(username.getText().toString().equals("")) {
      username.setText("helloiampau");
    }

    Button loginButton = login.findViewById(R.id.login);
    loginButton.setOnClickListener(b -> {
      Commands.login(username.getText().toString());
    });

    return login;
  }
}
