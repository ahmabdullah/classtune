package com.classtune.classtuneapp.freeversion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.GcmIntentService;
import com.classtune.classtuneapp.schoolapp.model.HomeworkData;
import com.classtune.classtuneapp.schoolapp.model.ModelContainer;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.AppConstant;
import com.classtune.classtuneapp.schoolapp.utils.AppUtility;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.MyTagHandler;
import com.classtune.classtuneapp.schoolapp.utils.ReminderHelper;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper.UserTypeEnum;
import com.classtune.classtuneapp.schoolapp.viewhelpers.CustomButton;
import com.classtune.classtuneapp.schoolapp.viewhelpers.ExpandableTextView;
import com.classtune.classtuneapp.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class SingleHomeworkActivity extends ChildContainerActivity {


    private UIHelper uiHelper;
	private String id;
	private TextView tvLesson;
	//private WebView webViewContent;
	
	private ExpandableTextView txtContent;
	private TextView tvSubject;
	private TextView tvDate;
	private TextView section;
	private CustomButton btnDone;
	private CustomButton btnReminder;
	private ImageView ivSubjectIcon;
	
	private LinearLayout bottmlay;
	
	private HomeworkData data;
	private Gson gson;
	
	private ImageButton btnDownload;
	private LinearLayout layoutDownloadHolder;

    private RelativeLayout layoutMessage;
    private LinearLayout layoutDataContainer;
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}



    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_homework);
		
		gson = new Gson();
		
		uiHelper = new UIHelper(SingleHomeworkActivity.this);
		
		if(getIntent().getExtras() != null)
			this.id = getIntent().getExtras().getString(AppConstant.ID_SINGLE_HOMEWORK);
		
		initView();
		initApicall();



        /*if(getIntent().getExtras()!=null)
        {
            Log.e("EXTRAS", "is: "+getIntent().getExtras().getBundle("total_unread_extras").getString("rid"));
        }*/

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
		//this.webViewContent = (WebView)this.findViewById(R.id.webViewContent);
		this.txtContent = (ExpandableTextView)this.findViewById(R.id.txtContent);
		this.tvLesson = (TextView) this.findViewById(R.id.tv_homework_content);
		this.tvSubject = (TextView) this.findViewById(R.id.tv_teacher_feed_subject_name);
		this.tvDate = (TextView) this.findViewById(R.id.tv_teacher_homewrok_feed_date);
		this.section = (TextView) this.findViewById(R.id.tv_teavher_homework_feed_section);
		this.btnDone = (CustomButton) this.findViewById(R.id.btn_done);
		this.btnReminder = (CustomButton) this.findViewById(R.id.btn_reminder);
		this.ivSubjectIcon = (ImageView) this.findViewById(R.id.imgViewCategoryMenuIcon);
		this.bottmlay = (LinearLayout)this.findViewById(R.id.bottmlay);
		this.btnDownload = (ImageButton)this.findViewById(R.id.btnDownload);
		this.layoutDownloadHolder = (LinearLayout)this.findViewById(R.id.layoutDownloadHolder);

        layoutMessage = (RelativeLayout)this.findViewById(R.id.layoutMessage);
        layoutDataContainer = (LinearLayout)this.findViewById(R.id.layoutDataContainer);


	}
	
	
	
	private void initAction()
	{
		this.tvLesson.setText(data.getName());
		this.txtContent.setText(Html.fromHtml(data.getContent(), null, new MyTagHandler()));
		
		
		if ( data.getIsDone().equalsIgnoreCase(AppConstant.ACCEPTED) || 
				data.getIsDone().equalsIgnoreCase(AppConstant.SUBMITTED)) {
			btnDone.setImage(R.drawable.done_tap);
			btnDone.setTitleColor(this.getResources().getColor(R.color.classtune_green_color));

			btnDone.setEnabled(false);
		} else {

			btnDone.setImage(R.drawable.done_normal);
			btnDone.setTitleColor(this.getResources().getColor(R.color.gray_1));

			if (userHelper.getUser().getType() == UserTypeEnum.STUDENT) {
				btnDone.setEnabled(true);
			}
			if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
				btnDone.setEnabled(false);
			}

			btnDone.setTag( data.getId());
		}
		
		
		this.tvSubject.setText(data.getSubject());
		
		String[] parts = data.getDueDate().split(" ");
		String part1 = parts[0];
		this.tvDate.setText(part1);
		
		this.section.setText("By "+data.getTeacherName());
		
		this.ivSubjectIcon.setImageResource(AppUtility.getImageResourceId(data.getSubject_icon_name(), this));
		
		if(data.getTimeOver() == 0)
		{
			bottmlay.setVisibility(View.VISIBLE);
		}
		else if(data.getTimeOver() == 1)
		{
			bottmlay.setVisibility(View.GONE);
		}
		
		
		btnDone.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				processDoneButton((CustomButton) v);
			}
		});
		
		btnReminder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final CustomButton btn = (CustomButton) v;
				setButtonState(btn, R.drawable.btn_reminder_tap, false, "Reminder");


				AppUtility.listenerDatePickerCancel = new AppUtility.IDatePickerCancel() {
					@Override
					public void onCancelCalled() {

						Log.e("CCCCC", "cancel called");
						btn.setImage(R.drawable.btn_reminder_normal);
						btn.setTitleColor(SingleHomeworkActivity.this.getResources().getColor(R.color.gray_1));
						btn.setEnabled(true);
					}
				};



				AppUtility.showDateTimePicker(AppConstant.KEY_HOMEWORK+data.getId(), data.getSubject()+ ": " + AppConstant.NOTIFICATION_HOMEWORK, data.getName(), SingleHomeworkActivity.this);
			}
		});
		
		
		
		if(!TextUtils.isEmpty(data.getAttachmentFileName()))
		{
			layoutDownloadHolder.setVisibility(View.VISIBLE);
		}
		else
		{
			layoutDownloadHolder.setVisibility(View.GONE);
		}
		
		
		
		btnDownload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//http://api.champs21.com/api/freeuser/downloadattachment?id=47
				
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://api.champs21.com/api/freeuser/downloadattachment?id=" + data.getId())));
			}
		});


        if (ReminderHelper.getInstance().reminder_map.containsKey(AppConstant.KEY_HOMEWORK+data.getId())){
            setButtonState(btnReminder, R.drawable.btn_reminder_tap, false, "Reminder");

        }else {
            setButtonState(btnReminder, R.drawable.btn_reminder_normal, true, "Reminder");
        }

		
	}

    @SuppressLint("ResourceAsColor")
    private void setButtonState(CustomButton btn, int imgResId, boolean enable , String btnText) {

        btn.setImage(imgResId);
        btn.setTitleText(btnText);
        btn.setEnabled(enable);
        if(enable) {
            setBtnTitleColor(btn, R.color.gray_1);
        } else {
            setBtnTitleColor(btn, R.color.classtune_green_color);
        }
    }
    private void setBtnTitleColor(CustomButton btn, int colorId) {
        btn.setTitleColor(this.getResources().getColor(colorId));
    }
	
	protected void processDoneButton(CustomButton button) {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();

		Log.e("User secret", UserHelper.getUserSecret());
		Log.e("Ass_ID", button.getTag().toString());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.ASSIGNMENT_ID, button.getTag().toString());

		AppRestClient.post(URLHelper.URL_HOMEWORK_DONE, params, doneBtnHandler);
	}
	
	AsyncHttpResponseHandler doneBtnHandler = new AsyncHttpResponseHandler() {
		public void onFailure(Throwable arg0, String arg1) {
			Log.e("button", "failed");
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		public void onStart() {
			Log.e("button", "onstart");
			uiHelper.showLoadingDialog("Please wait...");
		};

		public void onSuccess(int arg0, String response) {
			Log.e("response", response);
			Log.e("button", "success");
			uiHelper.dismissLoadingDialog();

			ModelContainer modelContainer = GsonParser.getInstance().parseGson(response);

			if (modelContainer.getStatus().getCode() == 200) {
				data.setIsDone(AppConstant.ACCEPTED);
				
				btnDone.setImage(R.drawable.done_tap);
				btnDone.setTitleColor(SingleHomeworkActivity.this.getResources().getColor(R.color.classtune_green_color));

				btnDone.setEnabled(false);
				
			} else {
				uiHelper.showMessage("Error in operation!");
			}



			Log.e("status code", modelContainer.getStatus().getCode() + "");
		};
	};
	
	
	
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void showWebViewContent(String text, WebView webView) {
		
		final String mimeType = "text/html";
		final String encoding = "UTF-8";

		webView.loadDataWithBaseURL("", text, mimeType, encoding, null);
		WebSettings webViewSettings = webView.getSettings();
		webViewSettings.setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient());

				
	}
	
	
	
	private List<HomeworkData> parseHomeworkData(String object) {

		List<HomeworkData> tags = new ArrayList<HomeworkData>();
		Type listType = new TypeToken<List<HomeworkData>>() {
		}.getType();
		tags = (List<HomeworkData>) new Gson().fromJson(object, listType);
		return tags;
	}
	
	
	private void initApicall()
	{
		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("id", this.id);
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
            if(getIntent().getExtras()!=null)
            {
                if(getIntent().getExtras().containsKey("total_unread_extras"))
                {
                    String rid = getIntent().getExtras().getBundle("total_unread_extras").getString("rid");
                    String rtype = getIntent().getExtras().getBundle("total_unread_extras").getString("rtype");

                    params.put(RequestKeyHelper.STUDENT_ID, getIntent().getExtras().getBundle("total_unread_extras").getString("student_id"));
                    params.put(RequestKeyHelper.BATCH_ID, getIntent().getExtras().getBundle("total_unread_extras").getString("batch_id"));


                    GcmIntentService.initApiCall(rid, rtype);
                }
                else
                {
                    params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
                    params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
                }
            }
            else
            {
                params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
                params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
            }


			//params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			//params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
			
			//Log.e("STU_ID", userHelper.getUser().getSelectedChild().getId());
			//Log.e("STU_PROFILE_ID", userHelper.getUser().getSelectedChild().getProfileId());
			//Log.e("STU_BATCH_ID", userHelper.getUser().getSelectedChild().getBatchId());
		}
		
		
		AppRestClient.post(URLHelper.URL_SINGLE_HOMEWORK, params, singleHomeWorkHandler);
	}
	
	AsyncHttpResponseHandler singleHomeWorkHandler = new AsyncHttpResponseHandler() {

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
				
				JsonObject objHomework = modelContainer.getData().get("homework").getAsJsonObject();
				data = gson.fromJson(objHomework.toString(), HomeworkData.class);
				
				Log.e("HHH", "data: " + data.getName());
				
				initAction();
				
			}

            else if(modelContainer.getStatus().getCode() == 400 && modelContainer.getStatus().getCode() != 404)
            {
                layoutDataContainer.setVisibility(View.GONE);
                layoutMessage.setVisibility(View.VISIBLE);
            }
			
			else {

			}
			
			

		};
	};
	
	/*@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		webViewContent.destroy();
	};*/


}
