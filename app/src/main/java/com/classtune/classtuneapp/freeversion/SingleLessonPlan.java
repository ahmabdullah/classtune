package com.classtune.classtuneapp.freeversion;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.classtune.classtuneapp.R;
import com.classtune.classtuneapp.schoolapp.model.LessonPlan;
import com.classtune.classtuneapp.schoolapp.model.Wrapper;
import com.classtune.classtuneapp.schoolapp.networking.AppRestClient;
import com.classtune.classtuneapp.schoolapp.utils.AppConstant;
import com.classtune.classtuneapp.schoolapp.utils.AppUtility;
import com.classtune.classtuneapp.schoolapp.utils.GsonParser;
import com.classtune.classtuneapp.schoolapp.utils.MyTagHandler;
import com.classtune.classtuneapp.schoolapp.utils.RequestKeyHelper;
import com.classtune.classtuneapp.schoolapp.utils.URLHelper;
import com.classtune.classtuneapp.schoolapp.utils.UserHelper;
import com.classtune.classtuneapp.schoolapp.viewhelpers.CustomTabButton;
import com.classtune.classtuneapp.schoolapp.viewhelpers.ExpandableTextView;
import com.classtune.classtuneapp.schoolapp.viewhelpers.PopupDialogLessonPlanConfirmation;
import com.classtune.classtuneapp.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Calendar;

/**
 * Created by BLACK HAT on 01-Apr-15.
 */
public class SingleLessonPlan extends ChildContainerActivity {

    private UIHelper uiHelper;
    private UserHelper userHelper;

    private Gson gson;
    private String id;
    private LessonPlan data;

    private TextView txtTitleLessonPlan;
    private TextView txtSubject;
    private TextView txtCategory;
    private TextView txtDate;
    private ExpandableTextView txtDescription;

    private CustomTabButton btnDelete;
    private CustomTabButton btnEdit;

    private LinearLayout layoutButtonHolder;


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

        setContentView(R.layout.activity_single_lessonplan);

        gson = new Gson();

        uiHelper = new UIHelper(this);
        userHelper = new UserHelper(this);


        if(getIntent().getExtras() != null)
            this.id = getIntent().getExtras().getString(AppConstant.ID_SINGLE_LESSON_PLAN);


        initView();
        initApiCall();

    }


    private void initView()
    {
        txtTitleLessonPlan = (TextView)this.findViewById(R.id.txtTitleLessonPlan);
        txtSubject = (TextView)this.findViewById(R.id.txtSubject);
        txtCategory = (TextView)this.findViewById(R.id.txtCategory);
        txtDate = (TextView)this.findViewById(R.id.txtDate);
        txtDescription = (ExpandableTextView)this.findViewById(R.id.txtDescription);

        btnDelete = (CustomTabButton)this.findViewById(R.id.btnDelete);
        btnEdit = (CustomTabButton)this.findViewById(R.id.btnEdit);

        btnDelete.setButtonSelected(true, R.color.black);
        btnEdit.setButtonSelected(true, R.color.black);

        layoutButtonHolder = (LinearLayout)this.findViewById(R.id.layoutButtonHolder);

        if (userHelper.getUser().getType() != UserHelper.UserTypeEnum.TEACHER)
        {
            layoutButtonHolder.setVisibility(View.GONE);
        }
        else
        {
            layoutButtonHolder.setVisibility(View.VISIBLE);
        }

    }

    private void initAction()
    {
        txtTitleLessonPlan.setText(data.getTitle());
        txtSubject.setText("Subject: "+data.getSubjects());
        txtCategory.setText("Category: "+data.getCategory());
        txtDate.setText("Published date: "+ AppUtility.getDateString(data.getPublishDate(), AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER));


        txtDescription.setText(Html.fromHtml(data.getDescription(), null, new MyTagHandler()));


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnDelete.setButtonSelected(true, R.color.black, R.drawable.btn_delete_lesson_plan);

                showCustomDialogConfirmationDelete("LESSON PLAN", "Delete", "Are you sure you want to delete selected lesson plan?", R.drawable.lessonplan_icon_red, SingleLessonPlan.this);

            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnEdit.setButtonSelected(true, R.color.black, R.drawable.btn_edit_lesson_plan);

                Intent intent = new Intent(SingleLessonPlan.this, EditLessonPlanActivity.class);
                intent.putExtra(AppConstant.ID_SINGLE_LESSON_PLAN, SingleLessonPlan.this.id);

                startActivityForResult(intent, 77);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 77)
        {
            initApiCall();
        }



    }


    private String getCurrentDate()
    {
        Calendar c = Calendar.getInstance();
        return AppUtility.getFormatedDateString(AppUtility.DATE_FORMAT_APP, c);
    }

    private void initApiCall()
    {


        RequestParams params = new RequestParams();
        params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
        params.put("id", this.id);


        AppRestClient.post(URLHelper.URL_SINGLE_LESSON_PLAN, params, singleLessonPlanHandler);


    }

    AsyncHttpResponseHandler singleLessonPlanHandler = new AsyncHttpResponseHandler() {

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

            Log.e("RES", "response string: " + responseString);


            Wrapper modelContainer = GsonParser.getInstance()
                    .parseServerResponse(responseString);

            if (modelContainer.getStatus().getCode() == 200) {

                JsonObject objLessonPlan = modelContainer.getData().get("lessonplan").getAsJsonObject();
                data = gson.fromJson(objLessonPlan.toString(), LessonPlan.class);



                initAction();

            }

            else {

            }



        }


    };



    private void initApiCallDeleteLessonPlan()
    {
        RequestParams params = new RequestParams();
        params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
        params.put("id", this.id);

        AppRestClient.post(URLHelper.URL_LESSON_DELETE, params, lessonDeleteHandler);
    }

    AsyncHttpResponseHandler lessonDeleteHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(Throwable arg0, String arg1) {
            uiHelper.showMessage(arg1);
            if (uiHelper.isDialogActive()) {
                uiHelper.dismissLoadingDialog();
            }
        }

        ;

        @Override
        public void onStart() {

            uiHelper.showLoadingDialog("Please wait...");


        }

        ;

        @Override
        public void onSuccess(int arg0, String responseString) {


            uiHelper.dismissLoadingDialog();


            Wrapper modelContainer = GsonParser.getInstance()
                    .parseServerResponse(responseString);



            if (modelContainer.getStatus().getCode() == 200) {


                Toast.makeText(SingleLessonPlan.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                finish();


            } else {

            }


        }

        ;
    };

    private void showCustomDialogConfirmationDelete(String titleText, String actionButtonText, String description,
                                                    int iconResId, Context context) {

        PopupDialogLessonPlanConfirmation picker = PopupDialogLessonPlanConfirmation.newInstance(0);


        picker.setData(titleText, actionButtonText, description, iconResId, context, new PopupDialogLessonPlanConfirmation.ActionCallback() {
            @Override
            public void onActionCalled() {

                initApiCallDeleteLessonPlan();
            }

            @Override
            public void onCancelCalled() {

            }
        });


        picker.show(getSupportFragmentManager(), null);
    }

}
