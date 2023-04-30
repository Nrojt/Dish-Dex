package com.nrojt.dishdex.utils.viewmodel;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nrojt.dishdex.backend.viewmodels.HomePageFragmentViewModel;

public class HomePageFragmentViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public HomePageFragmentViewModelFactory(Application application) {
        this.application = application;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomePageFragmentViewModel.class)) {
            return (T) new HomePageFragmentViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

