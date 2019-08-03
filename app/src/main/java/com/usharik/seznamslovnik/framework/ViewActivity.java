package com.usharik.seznamslovnik.framework;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * Created by macbook on 17.02.2018.
 */

public abstract class ViewActivity<T extends ViewModel> extends AppCompatActivity implements HasSupportFragmentInjector {
    private T viewModel;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory appViewModelFactory;

    protected abstract Class<T> getViewModelClass();

    @Override
    protected void onResume() {
        super.onResume();
        AndroidInjection.inject(this);
        viewModel = ViewModelProviders
                .of(this, appViewModelFactory)
                .get(getViewModelClass());
    }

    public T getViewModel() {
        return viewModel;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
