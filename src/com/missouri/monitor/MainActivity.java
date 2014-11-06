package com.missouri.monitor;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.missouri.monitor.logger.Logger;
import com.missouri.monitor.utils.ProcessInfo;
import com.missouri.monitor.utils.Programs;
import com.missouri.monitor.utils.Utils;

public class MainActivity extends Activity {
	private final static Logger log = Logger.getLogger(MainActivity.class);

	private List<Programs> processList;
	private ProcessInfo processInfo;
	private Intent monitorService;
	private ListView lstViProgram;
	private Button btnTest;
	private int pid, uid;
	private boolean isServiceStop = false;
	private UpdateReceiver receiver;

	private TextView nbTitle;
	private Long mExitTime = (long) 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log.i("onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initTitleLayout();
		processInfo = new ProcessInfo();
		btnTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				monitorService = new Intent();
				monitorService.setClass(MainActivity.this, MonitorService.class);
				if (getString(R.string.start_test).equals(btnTest.getText().toString())) {
					ListAdapter adapter = (ListAdapter) lstViProgram.getAdapter();
					if (adapter.checkedProg != null) {
						String packageName = adapter.checkedProg.getPackageName();
						String processName = adapter.checkedProg.getProcessName();
						Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
						String startActivity = "";
						log.d(packageName);
						// clear logcat
						try {
							Runtime.getRuntime().exec("logcat -c");
						} catch (IOException e) {
							log.d(e.getMessage());
						}
						try {
							startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
							startActivity(intent);
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
							return;
						}
						waitForAppStart(packageName);
						monitorService.putExtra("processName", processName);
						monitorService.putExtra("pid", pid);
						monitorService.putExtra("uid", uid);
						monitorService.putExtra("packageName", packageName);
						monitorService.putExtra("startActivity", startActivity);
						startService(monitorService);
						isServiceStop = false;
						btnTest.setText(getString(R.string.stop_test));
					} else {
						Toast.makeText(MainActivity.this, getString(R.string.choose_app_toast), Toast.LENGTH_LONG).show();
					}
				} else {
					btnTest.setText(getString(R.string.start_test));
					Toast.makeText(MainActivity.this, getString(R.string.test_result_file_toast) + MonitorService.resultFilePath,
							Toast.LENGTH_LONG).show();
					stopService(monitorService);
				}
			}
		});
		lstViProgram.setAdapter(new ListAdapter());
		lstViProgram.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				RadioButton rdBtn = (RadioButton) ((LinearLayout) view).getChildAt(0);
				rdBtn.setChecked(true);
			}
		});

		nbTitle.setText(getString(R.string.app_name));
		receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MonitorService.SERVICE_ACTION);
		registerReceiver(receiver, filter);
	}

	private void initTitleLayout() {
		nbTitle = (TextView) findViewById(R.id.nb_title);
		lstViProgram = (ListView) findViewById(R.id.processList);
		btnTest = (Button) findViewById(R.id.test);
	}

	/**
	 * customized BroadcastReceiver
	 * 
	 * @author Ricky
	 */
	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceStop = intent.getExtras().getBoolean("isServiceStop");
			if (isServiceStop) {
				btnTest.setText(getString(R.string.start_test));
			}
		}
	}

	@Override
	protected void onStart() {
		log.d("onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		log.d("onResume");
		if (isServiceStop) {
			btnTest.setText(getString(R.string.start_test));
		}
	}

	/**
	 * wait for test application started.
	 *
	 * @param packageName
	 *            package name of test application
	 */
	private void waitForAppStart(String packageName) {
		log.d("wait for app start");
		boolean isProcessStarted = false;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + Utils.TIMEOUT) {
			processList = processInfo.getRunningProcess(getBaseContext());
			for (Programs Programs : processList) {
				if ((Programs.getPackageName() != null) && (Programs.getPackageName().equals(packageName))) {
					pid = Programs.getPid();
					log.d("pid:" + pid);
					uid = Programs.getUid();
					if (pid != 0) {
						isProcessStarted = true;
						break;
					}
				}
			}
			if (isProcessStarted) {
				break;
			}
		}
	}

	/**
	 * show a dialog when click return key.
	 *
	 * @return Return true to prevent this event from being propagated further,
	 *         or false to indicate that you have not handled this event and it
	 *         should continue to be propagated.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				if (monitorService != null) {
					log.d("stop service");
					stopService(monitorService);
				}
				log.d("exit Emmagee");
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * customizing adapter.
	 *
	 * @author andrewleo
	 */
	private class ListAdapter extends BaseAdapter {
		List<Programs> Programs;
		Programs checkedProg;
		int lastCheckedPosition = -1;

		public ListAdapter() {
			Programs = processInfo.getRunningProcess(getBaseContext());
		}

		@Override
		public int getCount() {
			return Programs.size();
		}

		@Override
		public Object getItem(int position) {
			return Programs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Programs pr = Programs.get(position);
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
			}
			Viewholder holder = (Viewholder) convertView.getTag();
			if (holder == null) {
				holder = new Viewholder();
				convertView.setTag(holder);
				holder.imgViAppIcon = (ImageView) convertView.findViewById(R.id.image);
				holder.txtAppName = (TextView) convertView.findViewById(R.id.text);
				holder.rdoBtnApp = (RadioButton) convertView.findViewById(R.id.rb);
				holder.rdoBtnApp.setFocusable(false);
				holder.rdoBtnApp.setOnCheckedChangeListener(checkedChangeListener);
			}
			holder.imgViAppIcon.setImageDrawable(pr.getIcon());
			holder.txtAppName.setText(pr.getProcessName());
			holder.rdoBtnApp.setId(position);
			holder.rdoBtnApp.setChecked(checkedProg != null && getItem(position) == checkedProg);
			return convertView;
		}

		OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					final int checkedPosition = buttonView.getId();
					if (lastCheckedPosition != -1) {
						RadioButton tempButton = (RadioButton) findViewById(lastCheckedPosition);
						if ((tempButton != null) && (lastCheckedPosition != checkedPosition)) {
							tempButton.setChecked(false);
						}
					}
					checkedProg = Programs.get(checkedPosition);
					lastCheckedPosition = checkedPosition;
				}
			}
		};
	}

	/**
	 * save status of all installed processes
	 *
	 * @author andrewleo
	 */
	static class Viewholder {
		TextView txtAppName;
		ImageView imgViAppIcon;
		RadioButton rdoBtnApp;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

}
