package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Share
{
	public Share(Context context, String uri)
	{
		//Toast.makeText(context, uri, Toast.LENGTH_SHORT).show();
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
		context.startActivity(Intent.createChooser(share, "分享"));
	}
}