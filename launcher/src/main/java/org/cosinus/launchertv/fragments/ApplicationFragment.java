/*
 * Simple TV Launcher
 * Copyright 2017 Alexandre Del Bigio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.launchertv.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cosinus.launchertv.AppInfo;
import org.cosinus.launchertv.WebLink;
import org.cosinus.launchertv.R;
import org.cosinus.launchertv.Setup;
import org.cosinus.launchertv.Utils;
import org.cosinus.launchertv.activities.ApplicationList;
import org.cosinus.launchertv.views.ApplicationView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;


import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.widget.ImageView;
import android.app.AlertDialog;

import android.graphics.Typeface;
import android.widget.Button;
import android.os.Build;
import java.io.DataOutputStream;
import java.io.IOException;
import android.os.Build;

import java.lang.reflect.Method;
@SuppressWarnings("PointlessBooleanExpression")
public class ApplicationFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
	public static final String TAG = "ApplicationFragment";
	private static final String PREFERENCES_NAME = "applications";
	private static final int REQUEST_CODE_APPLICATION_LIST = 0x1E;
	private static final int REQUEST_CODE_WALLPAPER = 0x1F;
	private static final int REQUEST_CODE_APPLICATION_START = 0x20;
	private static final int REQUEST_CODE_PREFERENCES = 0x21;

//	private String ipAddress = getEthernetIPAddress();
	private String ipv6Address = getIPV6Address();
	String serialNumber = getSerialNumber();

	private TextView mClock;
	private TextView mDate;
	private boolean mBatteryChangedReceiverRegistered = false;

	private final Handler mHandler = new Handler();
	private final Runnable mTimerTick = new Runnable() {
		@Override
		public void run() {
			setClock();
		}
	};

	private int mGridX = 4;
	private int mGridY = 2;
	private LinearLayout mContainer;
	private LinearLayout mRestart;
	private ApplicationView[][] mApplications = null;
	private View mSettings;
	private View mGridView;
	private TextView mhotlineTBGD;
	private TextView mhotlineCBQL;
	private Setup mSetup;
	String BodyImage = "";
	String IDPhong = "";
	String TenPhong = "";

	WebLink[] webLinks = new WebLink[]{
			new WebLink("", R.drawable.uneti, "TÀI LIỆU HƯỚNG DẪN"),
			new WebLink("", R.drawable.usb, "USB"),
			new WebLink("", R.drawable.uneti_qrcode, "QR PHÒNG HỌC"),
			new WebLink("https://support.uneti.edu.vn/ho-tro-thiet-bi-giang-duong/bao-hong/", R.drawable.uneti, "HỖ TRỢ THIẾT BỊ"),
			new WebLink("https://egov.uneti.edu.vn", R.drawable.uneti, "EGOV"),
			new WebLink("https://www.google.com", R.drawable.chrome, "GOOGLE CHROME"),
			new WebLink("https://www.youtube.com", R.drawable.youtube, "YOUTUBE")
	};

	public ApplicationFragment() {
		// Required empty public constructor
	}
	public static String getSerialNumber() {
		String serialNumber;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);

			// (?) Lenovo Tab (https://stackoverflow.com/a/34819027/1276306)
			serialNumber = (String) get.invoke(c, "gsm.sn1");

			if (serialNumber.equals(""))
				// Samsung Galaxy S5 (SM-G900F) : 6.0.1
				// Samsung Galaxy S6 (SM-G920F) : 7.0
				// Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
				// (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
				serialNumber = (String) get.invoke(c, "ril.serialnumber");

			if (serialNumber.equals(""))
				// Archos 133 Oxygen : 6.0.1
				// Google Nexus 5 : 6.0.1
				// Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
				// Honor 5C (NEM-L51) : 7.0
				// Honor 5X (KIW-L21) : 6.0.1
				// Huawei M2 (M2-801w) : 5.1.1
				// (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
				serialNumber = (String) get.invoke(c, "ro.serialno");

			if (serialNumber.equals(""))
				// (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
				serialNumber = (String) get.invoke(c, "sys.serialnumber");

			if (serialNumber.equals(""))
				// Archos 133 Oxygen : 6.0.1
				// Honor 9 Lite (LLD-L31) : 8.0
				serialNumber = Build.SERIAL;

			// If none of the methods above worked
			if (serialNumber.equals(""))
				serialNumber = null;
		} catch (Exception e) {
			e.printStackTrace();
			serialNumber = null;
		}

		return serialNumber;
	}

//	public static String getEthernetIPAddress() {
//		try {
//			// Lấy danh sách các interface mạng
//			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//			while (interfaces.hasMoreElements()) {
//				NetworkInterface networkInterface = interfaces.nextElement();
//
//				// Bỏ qua các interface không khả dụng hoặc là loopback
//				if (!networkInterface.isUp() || networkInterface.isLoopback()) {
//					continue;
//				}
//
//				// Kiểm tra tên interface là Ethernet
//				String interfaceName = networkInterface.getDisplayName().toLowerCase();
//				if (interfaceName.contains("eth") || interfaceName.contains("wlan")) {
//					// Lấy danh sách các địa chỉ IP
//					Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//					while (inetAddresses.hasMoreElements()) {
//						InetAddress inetAddress = inetAddresses.nextElement();
//
//						// Chỉ lấy địa chỉ IPv4
//						if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
//							return inetAddress.getHostAddress();
//						}
//					}
//				}
//			}
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
//
//		return "Không tìm thấy địa chỉ IP Ethernet.";
//	}

	public static String getIPV6Address() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				// Bỏ qua các giao diện không hoạt động
				if (!networkInterface.isUp()) {
					continue;
				}

				// Kiểm tra nếu là Ethernet (eth0) và lấy địa chỉ IP
				if (networkInterface.getName().equals("eth0")) {
					Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress inetAddress = addresses.nextElement();

						// Chỉ lấy địa chỉ IPv6
						if (inetAddress instanceof java.net.Inet6Address) {
							String ipAddress = inetAddress.getHostAddress();
							// Kiểm tra nếu địa chỉ IPv6 có chứa %eth0
							if (ipAddress.contains("%eth0")) {
								return ipAddress.split("%")[0];
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Không tìm thấy địa chỉ IPv6";
	}

	String apiURL = "https://api.uneti.edu.vn/api/ThietBi_GiangDuong/QLP_Phong_Load_R_Para_File_box_ip?DT_QLP_Phong_DiaChiBox=";
	String apiURL_post = "https://api.uneti.edu.vn/api/ThietBi_GiangDuong/BoxHoatDong_Add_Para";

	private void callApi(final String apiUrl) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;

				try {
					// Khởi tạo URL và mở kết nối
					URL url = new URL(apiUrl);
					connection = (HttpURLConnection) url.openConnection();

					// Thiết lập phương thức GET
					connection.setRequestMethod("GET");

					// Thiết lập timeout
					connection.setConnectTimeout(10000);
					connection.setReadTimeout(10000);

					// Kiểm tra mã phản hồi
					int responseCode = connection.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_OK) {
						// Đọc dữ liệu từ API
						reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						String responseData = response.toString();
						try {
							JSONObject jsonResponse = new JSONObject(responseData);
							if (jsonResponse.has("body")) {
								JSONArray bodyArray = jsonResponse.getJSONArray("body");

								// Lấy đối tượng đầu tiên trong mảng "body"
								JSONObject firstItem = bodyArray.getJSONObject(0);

								// Lấy giá trị của các trường từ đối tượng đầu tiên
								IDPhong = firstItem.getString("DT_QLP_Phong_ID").trim();
								TenPhong = firstItem.getString("DT_QLP_Phong_TenPhong").trim();
								BodyImage = firstItem.getString("qrPhong").trim().replace("\\/", "/") .replace("[", "").replace("]", ""); ;;

//								String rawBody = jsonResponse.getString("body").trim();
//								BodyImage = rawBody.replace("\\/", "/") .replace("[", "").replace("]", ""); ;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else {
						final int errorCode = responseCode;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//Toast.makeText(getActivity(), "Lỗi: " + errorCode, Toast.LENGTH_LONG).show();
							}
						});
					}


				} catch (final Exception e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Toast.makeText(getActivity(), "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.", Toast.LENGTH_LONG).show();
						}
					});
				} finally {
					// Đóng kết nối và luồng đọc
					if (reader != null) {
						try {
							reader.close();
						} catch (Exception ignored) {}
					}
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	private void postApi(final String apiUrl_post, final String HT_TK_LichSu_BoxHoatDong_IDPhong, final String HT_TK_LichSu_BoxHoatDong_ChucNang) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;
				DataOutputStream outputStream = null;

				try {
					// Khởi tạo URL và mở kết nối
					URL url = new URL(apiUrl_post);
					connection = (HttpURLConnection) url.openConnection();

					// Thiết lập phương thức POST
					connection.setRequestMethod("POST");
					connection.setDoOutput(true); // Bật chế độ cho phép gửi dữ liệu
					connection.setConnectTimeout(10000);
					connection.setReadTimeout(10000);

					// Cấu trúc dữ liệu gửi đi dưới dạng x-www-form-urlencoded
					String postData = "HT_TK_LichSu_BoxHoatDong_IDPhong=" + URLEncoder.encode(HT_TK_LichSu_BoxHoatDong_IDPhong, "UTF-8") +
							"&HT_TK_LichSu_BoxHoatDong_ChucNang=" + URLEncoder.encode(HT_TK_LichSu_BoxHoatDong_ChucNang, "UTF-8");

					// Gửi dữ liệu
					outputStream = new DataOutputStream(connection.getOutputStream());
					outputStream.writeBytes(postData);
					outputStream.flush();

					// Kiểm tra mã phản hồi
					int responseCode = connection.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_OK) {
						// Đọc dữ liệu từ API
						reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}

					}
					else {
						// Nếu có lỗi, hiển thị mã lỗi
						final int errorCode = responseCode;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//Toast.makeText(getActivity(), "Lỗi: " + errorCode, Toast.LENGTH_LONG).show();
							}
						});
					}
				} catch (final Exception e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Toast.makeText(getActivity(), "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.", Toast.LENGTH_LONG).show();
						}
					});
				} finally {
					// Đóng kết nối và tài nguyên
					try {
						if (reader != null) {
							reader.close();
						}
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (Exception ignored) {}
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	private void showQrCode(String base64Image, String TenPhong) {
		try {
			// Giải mã Base64 thành byte array
			String[] imageStrConvert = base64Image.replaceAll("\"", "").split("base64");
			String base64Handle = "";
			if (imageStrConvert != null && imageStrConvert.length > 0) {
				base64Handle = imageStrConvert[imageStrConvert.length - 1].replaceAll("\'", "");
			}
			byte[] decodedString = Base64.decode(base64Handle, Base64.DEFAULT);
			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

			// Phóng to ảnh lên
			int newSize = 600;
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(decodedByte, newSize, newSize, true);

			// Tạo ImageView để hiển thị ảnh QR
			ImageView qrImageView = new ImageView(getContext());
			qrImageView.setImageBitmap(scaledBitmap);
			LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(newSize, newSize);
			qrImageView.setLayoutParams(imageLayoutParams);

			// Tạo TextView để hiển thị text "Mã QR phòng học"
			TextView qrTitle = new TextView(getContext());
			qrTitle.setText("Mã QR Code - "+TenPhong);
			qrTitle.setTextSize(24);
			qrTitle.setTypeface(null, Typeface.BOLD);
			qrTitle.setGravity(Gravity.CENTER);

			TextView qrHeader = new TextView(getContext());
			qrHeader.setText("IP phòng học - "+ipv6Address);
			qrHeader.setTextSize(24);
			qrHeader.setTypeface(null, Typeface.BOLD);
			qrHeader.setGravity(Gravity.CENTER);

			// Thêm khoảng cách giữa text và hình ảnh
			LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			textLayoutParams.setMargins(0, 35, 0, 35); // Khoảng cách 16dp phía dưới text
			qrTitle.setLayoutParams(textLayoutParams);
			qrHeader.setLayoutParams(textLayoutParams);

			// Tạo LinearLayout để chứa TextView và ImageView
			LinearLayout layout = new LinearLayout(getContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setGravity(Gravity.CENTER);
			layout.setPadding(16, 16, 16, 16);
			layout.addView(qrTitle); // Thêm TextView vào
			layout.addView(qrImageView); // Thêm ImageView vào55
			layout.addView(qrHeader); // Thêm TextView vào

			// Hiển thị trong AlertDialog
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setView(layout);
			builder.setPositiveButton("Đóng", null);
			AlertDialog alertDialog = builder.create();

			// Hiển thị AlertDialog
			alertDialog.show();

			// Lấy nút "Đóng" và thay đổi kích thước chữ
			Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setTextSize(17);  // Thay đổi kích thước chữ của nút "Đóng"
			positiveButton.setTypeface(null, Typeface.BOLD);  //
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getContext(), "Không thể kết nối đến máy chủ. Không thể hiển thị QR Code ", Toast.LENGTH_LONG).show();
		}
	}

	public static ApplicationFragment newInstance() {
		return new ApplicationFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_application, container, false);

		mSetup = new Setup(getContext());
		mContainer = (LinearLayout) view.findViewById(R.id.container);
//		mSettings = view.findViewById(R.id.settings);
//		mGridView = view.findViewById(R.id.application_grid);
		mClock = (TextView) view.findViewById(R.id.clock);
		mDate = (TextView) view.findViewById(R.id.date);
		final LinearLayout batteryLayout = (LinearLayout) view.findViewById(R.id.restart_layout);
//		mBatteryLevel = (TextView) view.findViewById(R.id.battery_level);
		mRestart = (LinearLayout) view.findViewById(R.id.restart);
		mhotlineTBGD = (TextView) view.findViewById(R.id.hotlineTBGD);
		mhotlineCBQL = (TextView) view.findViewById(R.id.hotlineCBQL);


		if (mSetup.keepScreenOn())
			mContainer.setKeepScreenOn(true);

		if (mSetup.showDate() == false)
			mDate.setVisibility(View.GONE);


		mhotlineTBGD.setText("Hotline cán bộ trực TBGD: 19001008");
		mhotlineCBQL.setText("Hotline cán bộ quản lý: 19001008");

//		mSettings.setOnClickListener(this);
//		mGridView.setOnClickListener(this);
		mRestart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Xử lý sự kiện click tại đây
				closeBrowser();
			}
		});
		createApplications();
		callApi(apiURL+serialNumber);


		return view;
	}

	private void createApplications() {

		mContainer.removeAllViews();
		mGridX = mSetup.getGridX();
		mGridY = mSetup.getGridY();

		if (mGridX < 2)
			mGridX = 2;
		if (mGridY < 1)
			mGridY = 1;

		int marginX = Utils.getPixelFromDp(getContext(), mSetup.getMarginX());
		int marginY = Utils.getPixelFromDp(getContext(), mSetup.getMarginY());

		boolean showNames = mSetup.showNames();

		mApplications = new ApplicationView[mGridY][mGridX];

		int position = 0;
		for (int y = 0; y < mGridY; y++) {
			LinearLayout ll = new LinearLayout(getContext());
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setGravity(Gravity.CENTER_VERTICAL);
			ll.setFocusable(false);
			ll.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
			));

			for (int x = 0; x < mGridX; x++) {
				if (position >= webLinks.length) {
					break;
				}
				else {
					ApplicationView av = new ApplicationView(getContext());
					av.setOnClickListener(this);
					av.setOnLongClickListener(this);
					av.setOnMenuOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onLongClick(v);
						}
					});
					av.setPosition(position++);
					av.showName(showNames);
					WebLink webLink = webLinks[y * mGridX + x];
					av.setImageResource(webLink.getIcon())
							.setText(webLink.getName())
							.setPackageName(webLink.getPackageName());
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
						av.setId(0x00FFFFFF + position);
					} else {
						av.setId(View.generateViewId());
					}
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
					lp.setMargins(marginX * 2, marginY * 10, marginX * 2, marginY * 10);

					av.setLayoutParams(lp);
					ll.addView(av);
					mApplications[y][x] = av;
				}
			}
			mContainer.addView(ll);
		}


	}

	// Hàm restart ứng dụng
	private void restartApp() {
		PackageManager pm = getActivity().getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage("com.android.chrome"); // Thay bằng package của trình duyệt bạn muốn dừng
		if (intent != null) {
			// Thực thi việc mở trình duyệt (nếu muốn khởi động lại)
			startActivity(intent);
		}
		else {
			Toast.makeText(getActivity(), "Không thể khởi động lại", Toast.LENGTH_SHORT).show();
		}
	}

	public void closeBrowser() {
//		ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
//		String packageName = "com.android.chrome"; // Package name của trình duyệt, thay đổi tùy bạn dùng trình duyệt nào.
//
//		activityManager.killBackgroundProcesses(packageName); // Dừng toàn bộ Chrom
	}

	@Override
	public void onStart() {
		super.onStart();
		setClock();

		mHandler.postDelayed(mTimerTick, 1000);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mTimerTick);

	}

	private void setClock() {

		Date date = new Date(System.currentTimeMillis());

		// Đặt múi giờ cho khu vực cụ thể (ví dụ UTC+7 cho Việt Nam)
		TimeZone timeZone = TimeZone.getTimeZone("GMT+7");  // Sử dụng múi giờ UTC+7 (hoặc thay bằng múi giờ khác nếu cần)

		// Định dạng giờ với mẫu "hh:mm a" (giờ 12h + AM/PM)
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
		timeFormat.setTimeZone(timeZone);  // Đảm bảo sử dụng múi giờ UTC+7

		// Định dạng ngày với mẫu "MMMM d, yyyy"
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		dateFormat.setTimeZone(timeZone);  // Đảm bảo sử dụng múi giờ UTC+7

		// Hiển thị kết quả
		String formattedTime = timeFormat.format(date);
		String formattedDate = dateFormat.format(date);

		mClock.setText(formattedTime);
		mDate.setText(formattedDate);
		mHandler.postDelayed(mTimerTick, 1000);
	}

	@Override
	public boolean onLongClick(View v) {
		if (v instanceof ApplicationView) {
//			ApplicationView appView = (ApplicationView) v;
//			if (appView.hasPackage() && mSetup.iconsLocked()) {
//				Toast.makeText(getActivity(), R.string.home_locked, Toast.LENGTH_SHORT).show();
//			} else {
//				openApplicationList(ApplicationList.VIEW_LIST, appView.getPosition(), appView.hasPackage(), REQUEST_CODE_APPLICATION_LIST);
//			}
			openApplication((ApplicationView) v);
			return (true);
		}
		return (false);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof ApplicationView) {
			openApplication((ApplicationView) v);
			return;
		}
	}

	private void openApplication(ApplicationView v) {
		try {
			Toast.makeText(getActivity(), v.getName(), Toast.LENGTH_SHORT).show();
			postApi(apiURL_post, IDPhong, v.getName());
			if(v.getName().equals("QR PHÒNG HỌC")){

				ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
				if (activeNetwork != null && activeNetwork.isConnected()) {
					if (BodyImage == null || BodyImage.isEmpty()) {
						serialNumber = getIPV6Address();
						callApi(apiURL+serialNumber);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								showQrCode(BodyImage, TenPhong);
							}
						}, 2000);
					}
					else {
						showQrCode(BodyImage, TenPhong);
					}
				}
				else {
					// Không có kết nối mạng, hiển thị thông báo
					Toast.makeText(getContext(), "Không có kết nối mạng. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
				}
			}
			else if(v.getName().equals("USB")){

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setType("*/*");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

			}
			else if(v.getName().equals("HỖ TRỢ THIẾT BỊ")){

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(v.getPackageName()+IDPhong));
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(v.getPackageName()));
				startActivity(intent);
			}

		} catch (Exception e) {
			Toast.makeText(getActivity(), v.getName() + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void openApplication(String packageName) {
		try {
			Intent startApp = getLaunchIntentForPackage(packageName);
			Toast.makeText(getActivity(), packageName, Toast.LENGTH_SHORT).show();
			startActivity(startApp);
		} catch (Exception e) {
			Toast.makeText(getActivity(), packageName + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void openApplicationList(int viewType, int appNum, boolean showDelete, int requestCode) {
		Intent intent = new Intent(getActivity(), ApplicationList.class);
		intent.putExtra(ApplicationList.APPLICATION_NUMBER, appNum);
		intent.putExtra(ApplicationList.VIEW_TYPE, viewType);
		intent.putExtra(ApplicationList.SHOW_DELETE, showDelete);
		startActivityForResult(intent, requestCode);
	}
	
	private Intent getLaunchIntentForPackage(String packageName) {
		PackageManager pm = getActivity().getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		
		if(launchIntent == null) {
			launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName);
		}
		
		return launchIntent;			
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

			// Lấy URI của thư mục người dùng đã chọn
			Uri treeUri = data.getData();

			// Cấp quyền truy cập cho URI này
			getActivity().getContentResolver().takePersistableUriPermission(treeUri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

			// Sau khi lấy URI, bạn có thể làm việc với thư mục đã chọn
			// Ví dụ: Mở thư mục đã chọn với ứng dụng khác
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(treeUri, "*/*");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(intent);
		}
	}


}
