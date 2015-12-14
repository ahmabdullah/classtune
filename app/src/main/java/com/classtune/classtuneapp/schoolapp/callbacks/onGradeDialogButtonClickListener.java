package com.classtune.classtuneapp.schoolapp.callbacks;

import com.classtune.classtuneapp.schoolapp.viewhelpers.GradeDialog;

import java.util.List;

public interface onGradeDialogButtonClickListener {

	public void onDoneBtnClick(GradeDialog gradeDialog, String gradeStr, List<Integer> grades);
	
}
