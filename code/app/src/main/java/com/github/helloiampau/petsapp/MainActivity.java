package com.github.helloiampau.petsapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.helloiampau.petsapp.fragments.LoadingFragment;
import com.github.helloiampau.petsapp.fragments.LoginForm;
import com.github.helloiampau.petsapp.fragments.RoomFragment;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Status.registerContext(this);
    Commands.registerContex(this);

    Status.get().route().observe(this, (route) -> {
      if(route == null) {
        return;
      }

      FragmentManager fragmentManager = getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

      Fragment fragment = null;
      if(route.equals("loading")) {
        this.getSupportActionBar().hide();
        fragment = new LoadingFragment();
      } else if(route.equals("login")) {
        this.getSupportActionBar().show();
        fragment = new LoginForm();
      } else if(route.equals("room")) {
        fragment = new RoomFragment();
      }

      fragmentTransaction.replace(R.id.main, fragment);
      fragmentTransaction.commit();
    });

    Commands.route("loading");
  }
}
