package com.android.print.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class IpAddressEdit extends Activity {
	// private MyDialog dialog;
	private LinearLayout layout;
	private int[] mIDs = {R.id.ip_edit_1, R.id.ip_edit_2, R.id.ip_edit_3, R.id.ip_edit_4};
	private EditText ipEdit1, ipEdit2, ipEdit3, ipEdit4;
	private EditText[] ipEdits = { ipEdit1, ipEdit2, ipEdit3, ipEdit4};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ip_address_edit);

		MyTextWatcher[] mTextWatcher = new MyTextWatcher[4];
		for(int i = 0; i < 4; i++)
		{
			ipEdits[i] = (EditText)findViewById(mIDs[i]);
			mTextWatcher[i] = new MyTextWatcher(ipEdits[i]);
			ipEdits[i].addTextChangedListener(mTextWatcher[i]);
		}

		// dialog=new MyDialog(this);
		layout = (LinearLayout) findViewById(R.id.exit_layout);
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(getApplicationContext(), "click other outside window close", Toast.LENGTH_SHORT).show();
			}
		});
	}

	class MyTextWatcher implements TextWatcher
	{
		public EditText mEditText;

		public MyTextWatcher(EditText mEditText) {
			super();
			this.mEditText = mEditText;
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			if(s.length() == 3)
			{
				if (Integer.parseInt(mEditText.getEditableText().toString()) > 255) {
					mEditText.setText("255");
				}
				if(this.mEditText == ipEdits[0])
				{
					ipEdits[1].requestFocus();
				}
				else if(this.mEditText == ipEdits[1])
				{
					ipEdits[2].requestFocus();
				}
				else if(this.mEditText == ipEdits[2])
				{
					ipEdits[3].requestFocus();
				}
			}
			else if(s.length() == 0)
			{
				if(this.mEditText == ipEdits[3])
				{
					ipEdits[2].requestFocus();
				}
				else if(this.mEditText == ipEdits[2])
				{
					ipEdits[1].requestFocus();
				}
				else if(this.mEditText == ipEdits[1])
				{
					ipEdits[0].requestFocus();
				}
			}

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//click other outside window close
		//finish();
		return true;
	}

	public void okBtnClick(View v) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < 4; i++)
		{
			sb.append(ipEdits[i].getEditableText());
			if (i != 3) {
				sb.append(".");
			}
		}
		Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();

		Intent intent = new Intent();
        intent.putExtra("ip_address", sb.toString());
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	public void cancelBtnClick(View v) {
		this.finish();
	}

}
