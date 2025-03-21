package com.classtune.classtuneapp.freeversion;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.classtune.classtuneapp.schoolapp.GcmIntentService;
import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.model.MeetingStatus;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.AppConstant;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper.UserTypeEnum;
import com.classtune.classtuneapp.schoolapp.viewhelpers.PopupDialogMeetingStatus;
import com.classtune.classtuneapp.schoolapp.viewhelpers.PopupDialogMeetingStatus.PopupOkButtonClickListener;
import com.classtune.classtuneapp.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class SingleMeetingRequestActivity extends ChildContainerActivity {
	
	
	private UIHelper uiHelper;
	private UserHelper userHelper;
	private MeetingStatus data;
	private String id;
	
	private Gson gson;
	
	
	private TextView txtName;
	private TextView txtBatch;
	private TextView txtDate;
	private TextView txtDescription;
	
	private LinearLayout layoutStatus;
	private TextView txtStatus;
	
	private LinearLayout layoutBatch;

    private RelativeLayout layoutMessage;
    private ScrollView layoutDataContainer;
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_meeting_request);
		
		gson = new Gson();
		
		uiHelper = new UIHelper(this);
		userHelper = new UserHelper(this);
		
		
		if(getIntent().getExtras() != null)
			this.id = getIntent().getExtras().getString(AppConstant.ID_SINGLE_MEETING_REQUEST);
		
		
		initView();
		initApiCall();

        if(getIntent().getExtras()!=null)
        {
            if(getIntent().getExtras().containsKey("total_unread_extras"))
            {
                String rid = getIntent().getExtras().getBundle("total_unread_extras").getString("rid");
                String rtype = getIntent().getExtras().getBundle("total_unread_extras").getString("rtype");


                GcmIntentService.initApiCall(rid, rtype);
            }
        }

		
	}


	private void initView() 
	{
		txtName = (TextView)this.findViewById(R.id.txtName);
		txtBatch = (TextView)this.findViewById(R.id.txtBatch);
		txtDate = (TextView)this.findViewById(R.id.txtDate);
		txtDescription = (TextView)this.findViewById(R.id.txtDescription);
		
		layoutStatus = (LinearLayout)this.findViewById(R.id.layoutStatus);
		txtStatus = (TextView)this.findViewById(R.id.txtStatus);
		
		layoutBatch = (LinearLayout)this.findViewById(R.id.layoutBatch);
		
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			layoutBatch.setVisibility(View.GONE);
		}
		else
		{
			layoutBatch.setVisibility(View.VISIBLE);
		}

        layoutMessage = (RelativeLayout)this.findViewById(R.id.layoutMessage);
        layoutDataContainer = (ScrollView)this.findViewById(R.id.layoutDataContainer);
	}

	private void initApiCall()
	{

		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("id", this.id);
		
		
		AppRestClient.post(URLHelper.URL_SINGLE_MEETING_REQUEST, params, singleMeetingRequestHandler);
	
	}
	
	private void initAction() 
	{
		
		if(data.getTimeOver() == 1)
		{
			txtStatus.setText("Time Exceed");
			txtStatus.setTextColor(Color.BLACK);
			layoutStatus.setBackgroundColor(Color.parseColor("#fff200"));
		}
		else
		{
			if(data.getStatus().equalsIgnoreCase("0"))
			{
				txtStatus.setText("Pending");
				txtStatus.setTextColor(Color.BLACK);
				layoutStatus.setBackgroundColor(Color.parseColor("#e7ecee"));
			}
			else if(data.getStatus().equalsIgnoreCase("1"))
			{
				//holder.txtStatus.setText("Done");
				txtStatus.setText("Accepted");
				txtStatus.setTextColor(Color.WHITE);
				layoutStatus.setBackgroundColor(Color.parseColor("#4f9611"));
			}
			else if(data.getStatus().equalsIgnoreCase("2"))
			{
				txtStatus.setText("Declined");
				txtStatus.setTextColor(Color.WHITE);
				layoutStatus.setBackgroundColor(Color.parseColor("#d71921"));
			}
			else
			{
				txtStatus.setText("-----");
				
			}
			
			if(data.getTimeOver() == 1)
			{
				txtStatus.setText("Time Exceed");
				txtStatus.setTextColor(Color.BLACK);
				layoutStatus.setBackgroundColor(Color.parseColor("#fff200"));
			}
			
			

			layoutStatus.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(data.getTimeOver() == 0 && data.getType() == 1)
						showCustomDialogStatus(data.getId(), "MEETING STATUS", R.drawable.icon_meeting_request_status, "You have pending meeting request");
				}
			});
			
			
		}
		
		
		txtName.setText(data.getName());
		txtBatch.setText(data.getBatch());
		//txtDate.setText(data.getDate());

        String time = data.getDate();

        if (time.length() > 0 ) {
            time = time.substring(0, time.length()-3);
        }
        txtDate.setText(time);


		txtDescription.setText(data.getDescription());
		
		
	}
	
	
	AsyncHttpResponseHandler singleMeetingRequestHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
		};

		@Override
		public void onStart() {
			
				uiHelper.showLoadingDialog("Please wait...");
			

		};

		@Override
		public void onSuccess(int arg0, String responseString) {
			

			uiHelper.dismissLoadingDialog();


			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			if (modelContainer.getStatus().getCode() == 200) {

                layoutDataContainer.setVisibility(View.VISIBLE);
                layoutMessage.setVisibility(View.GONE);
				
				JsonObject objNotice = modelContainer.getData().get("meetings").getAsJsonObject();
				data = gson.fromJson(objNotice.toString(), MeetingStatus.class);
				
				
				
				initAction();
				
			}

            else if(modelContainer.getStatus().getCode() != 200 || modelContainer.getStatus().getCode() != 404)
            {
                layoutDataContainer.setVisibility(View.GONE);
                layoutMessage.setVisibility(View.VISIBLE);
            }
			
			else {

			}
			
			

		}

		
	};
	
	
	private void showCustomDialogStatus(final String meetingId, String headerText, int imgResId, String descriptionText)
	{

		PopupDialogMeetingStatus picker = PopupDialogMeetingStatus.newInstance(0);
		picker.setData(headerText, descriptionText, imgResId,this);
			  
			  
		picker.setCallBack(new PopupOkButtonClickListener() {
			   
			   @Override
			   public void onOkButtonClicked(String status)
			   {
				    // TODO Auto-generated method stub
				    Log.e("STATUS", "is: " + status);
				    
				    if(status.equalsIgnoreCase("accepted"))
				    {
				    	initApiCallStatus(meetingId, "1");
				    	
				    	txtStatus.setText("Accepted");
						txtStatus.setTextColor(Color.WHITE);
						layoutStatus.setBackgroundColor(Color.parseColor("#4f9611"));
				    }
				    
				    else if(status.equalsIgnoreCase("declined"))
				    {
				    	initApiCallStatus(meetingId, "2");
				    	
				    	txtStatus.setText("Declined");
						txtStatus.setTextColor(Color.WHITE);
						layoutStatus.setBackgroundColor(Color.parseColor("#d71921"));
				    }
				    
			    
			   }
			   
		  });
	  
			  
		picker.show(getSupportFragmentManager(), null);
			 
	}
	
	private void initApiCallStatus(String meetingId, String status) {

		RequestParams params = new RequestParams();

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("meeting_id", meetingId);
		params.put("status", status);
		
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
            if(getIntent().getExtras()!=null)
            {
                if(getIntent().getExtras().containsKey("total_unread_extras"))
                {
                    String rid = getIntent().getExtras().getBundle("total_unread_extras").getString("rid");
                    String rtype = getIntent().getExtras().getBundle("total_unread_extras").getString("rtype");

                    params.put(RequestKeyHelper.STUDENT_ID, getIntent().getExtras().getBundle("total_unread_extras").getString("student_id"));

                    GcmIntentService.initApiCall(rid, rtype);
                }
                else
                {
                    params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());

                }
            }
            else
            {
                params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());

            }


			//params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
		}
		
		
		
		Log.e("STAUSSSS", "meeting_id: " + meetingId);
		Log.e("STAUSSSS", "status: " + status);
		
		AppRestClient.post(URLHelper.URL_MEETING_STATUS, params,
				meetingStatusHandler);
	}
	
	private AsyncHttpResponseHandler meetingStatusHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			// uiHelper.showMessage(arg1);
			// uiHelper.dismissLoadingDialog();

		};

		@Override
		public void onStart() {
			// uiHelper.showLoadingDialog("Please wait...");

			
		};

		@Override
		public void onSuccess(String responseString) {
			
			

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			Log.e("RES", "is: " + responseString);
			

			if (modelContainer.getStatus().getCode() == 200) {
				
			
				
				
			}

			else {

			}
		};
	};
	

}
