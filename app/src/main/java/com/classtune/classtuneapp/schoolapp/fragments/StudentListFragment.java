package com.classtune.classtuneapp.schoolapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.classtune.classtuneapp.freeversion.PaidVersionHomeFragment;
import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.adapters.StudentListAdapter;
import com.classtune.classtuneapp.schoolapp.model.BaseType;
import com.classtune.classtuneapp.schoolapp.model.Batch;
import com.classtune.classtuneapp.schoolapp.model.Picker;
import com.classtune.classtuneapp.schoolapp.model.Picker.PickerItemSelectedListener;
import com.classtune.classtuneapp.schoolapp.model.PickerType;
import com.classtune.classtuneapp.schoolapp.model.StudentAttendance;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.AppConstant;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.classtune.classtuneapp.schoolapp.viewhelpers.UIHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Locale;

public class StudentListFragment extends UserVisibleHintFragment {
	private View rootView;
	private ListView studentListView;
	private LinearLayout pbLayout;
	private EditText editsearch;
	private ArrayList<StudentAttendance> arraylist = new ArrayList<StudentAttendance>();
	private StudentListAdapter adapter;
	private UIHelper uiHelper;
	private boolean isStudentListloaded = false;
	private Context mContext;

	private TextView txtMessage;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//fetchData();
		mContext = getActivity();
		uiHelper = new UIHelper(getActivity());
	}

	private void fetchData() {
		
			RequestParams params = new RequestParams();
			params.put(RequestKeyHelper.BATCH_ID,selectedBatch.getId());
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			AppRestClient.post(URLHelper.URL_GET_STUDENTS_ATTENDANCE, params,
					getStudentHandler);
			
	}

	AsyncHttpResponseHandler getStudentHandler = new AsyncHttpResponseHandler() {
		public void onFailure(Throwable arg0, String arg1) {
			pbLayout.setVisibility(View.GONE);
			Log.e("error", arg1);
		};

		public void onStart() {
			pbLayout.setVisibility(View.VISIBLE);
			arraylist.clear();
			if(adapter!=null)
			adapter.notifyDataSetChanged();
		};

		public void onSuccess(int arg0, String responseString) {
			arraylist.clear();

			pbLayout.setVisibility(View.GONE);
			Log.e("Menu", responseString);
			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			arraylist.addAll(GsonParser.getInstance().parseStudentList(
					(wrapper.getData().get("batch_attendence")).toString()));
			adapter = new StudentListAdapter(mContext, arraylist);
			studentListView.setAdapter(adapter);
			isStudentListloaded = true;


			if(arraylist.size() <= 0)
			{
				txtMessage.setVisibility(View.VISIBLE);
			}
			else
			{
				txtMessage.setVisibility(View.GONE);
			}


		};
	};
	private ImageButton batchSelectLayout;
	private TextView batchTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.student_list_fragment_layout, container,
				false);
		editsearch = (EditText) rootView.findViewById(R.id.search);
		studentListView = (ListView) rootView.findViewById(R.id.listview);
		pbLayout = (LinearLayout) rootView.findViewById(R.id.pb);
		batchTextView = (TextView)rootView.findViewById(R.id.date_text);
		if(PaidVersionHomeFragment.selectedBatch!=null){
			batchTextView.setText(PaidVersionHomeFragment.selectedBatch.getName());
		}
		// Capture Text in EditText
		batchSelectLayout = (ImageButton)rootView.findViewById(R.id.lay_student_list_header_tap);
		batchSelectLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showPicker(PickerType.TEACHER_BATCH);
			}
		});
		editsearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String text = editsearch.getText().toString()
						.toLowerCase(Locale.getDefault());
				if (selectedBatch != null)
					adapter.filter(text);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
				// TODO Auto-generated method stub
			}
		});

		txtMessage = (TextView)rootView.findViewById(R.id.txtMessage);

		if(arraylist.size() <= 0)
		{
			txtMessage.setVisibility(View.VISIBLE);
		}
		else
		{
			txtMessage.setVisibility(View.GONE);
		}


		return rootView;
	}
	
	public void showPicker(PickerType type) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, PaidVersionHomeFragment.batches, PickerCallback , "Select Batch");
		picker.show(getChildFragmentManager(), null);
	}
	private Batch selectedBatch;
	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case TEACHER_BATCH:
				
				selectedBatch=(Batch)item;
				PaidVersionHomeFragment.selectedBatch = selectedBatch;
				if(PaidVersionHomeFragment.selectedBatch!=null){
					batchTextView.setText(PaidVersionHomeFragment.selectedBatch.getName());
				}
				fetchData();
				break;
			
			default:
				break;
			}

		}
	};
	@Override
	protected void onVisible() {
		//showPicker(PickerType.TEACHER_BATCH);
		if(!PaidVersionHomeFragment.isBatchLoaded){
			RequestParams params=new RequestParams();
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			AppRestClient.post(URLHelper.URL_GET_TEACHER_BATCH, params, getBatchEventsHandler);
		}else {
			if(PaidVersionHomeFragment.selectedBatch!=null){
				selectedBatch=PaidVersionHomeFragment.selectedBatch;
				if(!isStudentListloaded) fetchData();
				
			}else {
				showPicker(PickerType.TEACHER_BATCH);
			}
		}
	}
	
	AsyncHttpResponseHandler getBatchEventsHandler=new AsyncHttpResponseHandler()
	{

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			//uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		}

		@Override
		public void onStart() {
			super.onStart();
			
			if(!uiHelper.isDialogActive())
				uiHelper.showLoadingDialog(getString(R.string.loading_text));
			else
				uiHelper.updateLoadingDialog(getString(R.string.loading_text));
			
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			uiHelper.dismissLoadingDialog();
			Log.e("Response", responseString);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				PaidVersionHomeFragment.isBatchLoaded=true;
				PaidVersionHomeFragment.batches.clear();
				String data=wrapper.getData().get("batches").toString();
				PaidVersionHomeFragment.batches.addAll(GsonParser.getInstance().parseBatchList(data));
				//showPicker(PickerType.TEACHER_BATCH);
				showPicker(PickerType.TEACHER_BATCH);
			}
		}
		
	};
	@Override
	protected void onInvisible() {
		
	}

}