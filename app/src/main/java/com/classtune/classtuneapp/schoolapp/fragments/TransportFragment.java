package com.classtune.classtuneapp.schoolapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.model.Transport;
import com.classtune.classtuneapp.schoolapp.model.UserAuthListener;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.AppConstant;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper.UserTypeEnum;
import com.classtune.classtuneapp.schoolapp.viewhelpers.UIHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

public class TransportFragment extends UserVisibleHintFragment implements
		UserAuthListener {

	private UserHelper userHelper;
	private UIHelper uiHelper;

	private String pickupLocation = "";
	private String dropLocation = "";
	private String lastUpdate = "";

	private List<Transport> listSchedule = new ArrayList<Transport>();

	private TextView txtPickupLocation;
	private TextView txtDropLocation;
	private TextView txtLastUpdate1;
	private TextView txtLastUpdate2;

	private LinearLayout layoutListHolder;
	private LinearLayout pbs;
    private Context mContext;
	private RelativeLayout nodata;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        mContext = getActivity();
		userHelper = new UserHelper(this, getActivity());

	}

	public void fetchTransportData() {
		listSchedule.clear();
		RequestParams params = new RequestParams();

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());

		if (userHelper.getUser().getType() == UserTypeEnum.STUDENT
				|| userHelper.getUser().getType() == UserTypeEnum.TEACHER) {
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser()
					.getPaidInfo().getSchoolId());
		}

		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser()
					.getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser()
					.getSelectedChild().getBatchId());
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser()
					.getSelectedChild().getSchoolId());
		}

		AppRestClient.post(URLHelper.URL_TRANSPORT, params, transportHandler);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		uiHelper = new UIHelper(this.getActivity());
		View view = inflater.inflate(R.layout.fragment_transport, container,
				false);

		initViewObjects(view);
		return view;
	}

	private void initViewObjects(View view) {
		this.txtPickupLocation = (TextView) view
				.findViewById(R.id.txtPickupLocation);
		this.txtDropLocation = (TextView) view
				.findViewById(R.id.txtDropLocation);
		this.txtLastUpdate1 = (TextView) view.findViewById(R.id.txtLastUpdate1);
		this.txtLastUpdate2 = (TextView) view.findViewById(R.id.txtLastUpdate2);
		this.layoutListHolder = (LinearLayout) view
				.findViewById(R.id.layoutListHolder);
		this.pbs = (LinearLayout) view.findViewById(R.id.pb);
		this.nodata = (RelativeLayout) view.findViewById(R.id.layoutMessage);
	}

	private void initViewActions() {
		this.txtPickupLocation.setText(pickupLocation);
		this.txtDropLocation.setText(dropLocation);
		this.txtLastUpdate1.setText(AppConstant.LAST_UPDATE + lastUpdate);
		this.txtLastUpdate2.setText(AppConstant.LAST_UPDATE + lastUpdate);
	}

	private AsyncHttpResponseHandler transportHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			// uiHelper.dismissLoadingDialog();
			pbs.setVisibility(View.GONE);
		};

		@Override
		public void onStart() {
			// uiHelper.showLoadingDialog("Please wait...");
			pbs.setVisibility(View.VISIBLE);
		};

		@Override
		public void onSuccess(String responseString) {
			// uiHelper.dismissLoadingDialog();
			pbs.setVisibility(View.GONE);
			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);
			/*Toast.makeText(getActivity(),
					"RESPONSE TRANSPORT:" + responseString, Toast.LENGTH_LONG)
					.show();*/
			if (modelContainer.getStatus().getCode() == 200) {
				Log.e("TRANSPORT", "data: "
						+ modelContainer.getData().getAsJsonObject().toString());

				Log.e("TRANSPORT",
						"data_loc1: "
								+ modelContainer.getData().getAsJsonObject()
								.get("transport").getAsJsonObject()
								.get("location").getAsJsonObject()
								.get("pickup"));
				Log.e("TRANSPORT",
						"data_loc2: "
								+ modelContainer.getData().getAsJsonObject()
								.get("transport").getAsJsonObject()
								.get("location").getAsJsonObject()
								.get("drop"));

				pickupLocation = modelContainer.getData().getAsJsonObject()
						.get("transport").getAsJsonObject().get("location")
						.getAsJsonObject().get("pickup").getAsString();

				dropLocation = modelContainer.getData().getAsJsonObject()
						.get("transport").getAsJsonObject().get("location")
						.getAsJsonObject().get("drop").getAsString();

				lastUpdate = modelContainer.getData().getAsJsonObject()
						.get("transport").getAsJsonObject().get("location")
						.getAsJsonObject().get("last_updated").getAsString();

				listSchedule = GsonParser.getInstance().parseTransportSchedule(
						modelContainer.getData().getAsJsonObject()
								.get("transport").getAsJsonObject()
								.get("schedule").getAsJsonArray().toString());
				if(listSchedule.size()>0) {
					nodata.setVisibility(View.GONE);
				}
				layoutListHolder.removeAllViews();
				for (int i = 0; i < listSchedule.size(); i++) {

					View row = LayoutInflater.from(mContext).inflate(
							R.layout.fragment_transport_singledata, null);
                    if(i%2!=0)
                    ((LinearLayout)row.findViewById(R.id.row_bg_transport)).setBackgroundColor(getResources().getColor(R.color.bg_row_odd));
					TextView txtDayName = (TextView) row
							.findViewById(R.id.txtDayName);
					TextView txtHomePickUp = (TextView) row
							.findViewById(R.id.txtHomePickUp);
					TextView txtSchoolPickUp = (TextView) row
							.findViewById(R.id.txtSchoolPickUp);

					String myString = listSchedule.get(i).getWeekDayName();
					String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
					txtDayName.setText(upperString);

					txtHomePickUp
							.setText(listSchedule.get(i).getHomePickTime());
					txtSchoolPickUp.setText(listSchedule.get(i)
							.getSchoolPickTime());

					layoutListHolder.addView(row);

				}

				initViewActions();

			}

			else {

			}

		};

	};

	@Override
	public void onAuthenticationStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthenticationSuccessful() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthenticationFailed(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onVisible() {

		fetchTransportData();
	}

	@Override
	protected void onInvisible() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaswordChanged() {
		// TODO Auto-generated method stub
		
	}

}
