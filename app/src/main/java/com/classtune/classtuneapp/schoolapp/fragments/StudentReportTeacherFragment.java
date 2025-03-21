package com.classtune.classtuneapp.schoolapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.classtune.classtuneapp.freeversion.PaidVersionHomeFragment;
import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.adapters.StudentReportListAdapter;
import com.classtune.classtuneapp.schoolapp.model.StudentAttendance;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Locale;

public class StudentReportTeacherFragment extends Fragment {
	private View rootView;
	private ListView studentListView;
	private LinearLayout pbLayout;
	private EditText editsearch;
	private ArrayList<StudentAttendance> arraylist = new ArrayList<StudentAttendance>();
	private StudentReportListAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fetchData();

	}

	private void fetchData() {
		if(PaidVersionHomeFragment.selectedBatch==null)return;
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.BATCH_ID,
				PaidVersionHomeFragment.selectedBatch.getId());
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

		};

		public void onSuccess(int arg0, String responseString) {
			arraylist.clear();

			pbLayout.setVisibility(View.GONE);
			Log.e("Menu", responseString);
			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			arraylist.addAll(GsonParser.getInstance().parseStudentList(
					(wrapper.getData().get("batch_attendence")).toString()));
			adapter = new StudentReportListAdapter(getActivity(), arraylist);
			studentListView.setAdapter(adapter);
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.student_report_layout, container,
				false);
		editsearch = (EditText) rootView.findViewById(R.id.search);
		studentListView = (ListView) rootView.findViewById(R.id.listview);
		pbLayout = (LinearLayout) rootView.findViewById(R.id.pb);
		// Capture Text in EditText
		
		editsearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String text = editsearch.getText().toString()
						.toLowerCase(Locale.getDefault()) ;
				if(adapter!=null)
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
		return rootView;
	}

}