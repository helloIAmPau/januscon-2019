package com.github.helloiampau.customapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.helloiampau.janus.JanusFactory;
import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Platform;
import com.github.helloiampau.janus.rtc.MediaImpl;
import com.github.helloiampau.janus.rtc.MediaView;

public class MainActivity extends AppCompatActivity implements CustomWrapperDelegate {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    HttpClient.init(this);

    CustomWrapper wrapper = new CustomWrapper(this, this);

    EditText host = this.findViewById(R.id.url);
    Button button = this.findViewById(R.id.start);

    button.setOnClickListener(v -> {
      String text = button.getText().toString();

      if(text.equals("Start")) {
        wrapper.start(host.getText().toString());

        button.setText("Stop");
      } else {
        wrapper.stop();

        button.setText("Start");
      }
    });
  }

  @Override
  public void onMedia(Media media) {
    if(media.remoteVideoTrack() != null) {
      MediaView mediaView = this.findViewById(R.id.view);

      ((MediaImpl) media).addRemoteTrackSink(mediaView);
    }
  }

  @Override
  public void onHangup() {}
}
