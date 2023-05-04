package com.nrojt.dishdex.backend.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nrojt.dishdex.utils.database.MyDatabaseHelper;

public class AddCategoryFragmentViewModel extends ViewModel {
    MutableLiveData<String> categoryName;
    public AddCategoryFragmentViewModel() {
        categoryName = new MutableLiveData<>();
    }

    public void setCategoryName(String categoryName) {
        if (categoryName == null){
            categoryName = "";
        }
        this.categoryName.setValue(categoryName);
    }

    public MutableLiveData<String> getCategoryName(){
       return categoryName;
    }

    public boolean addCategory(Context context){
        if (categoryName.getValue() == null || categoryName.getValue().isBlank()){
            return false;
        }

        MyDatabaseHelper db = MyDatabaseHelper.getInstance(context);
        return db.addCategory(categoryName.getValue());
    }

}
