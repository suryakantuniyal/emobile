package com.android.emobilepos;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class ShowProductVideoActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_video_layout);
		Bundle extras = this.getIntent().getExtras();
		VideoView myVideoView = (VideoView) findViewById(R.id.myvideoview);
		myVideoView.setVideoURI(Uri.parse(extras.getString("url")));
		myVideoView.setMediaController(new MediaController(this));
		myVideoView.requestFocus();
		myVideoView.start();
	}
}
