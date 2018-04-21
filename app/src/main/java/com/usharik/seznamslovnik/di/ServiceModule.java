package com.usharik.seznamslovnik.di;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Vibrator;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.service.NetworkService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by macbook on 09.02.18.
 */

@Module(includes = {AppModule.class})
class ServiceModule {

    @Provides
    @Singleton
    AppState provideAppState() {
        return new AppState();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://slovnik.seznam.cz/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    DatabaseManager provideDatabaseManager(Application application) {
        return new DatabaseManager(application);
    }

    @Provides
    @Singleton
    TranslationService provideTranslationService(DatabaseManager databaseManager,
                                             AppState appState,
                                             Retrofit retrofit,
                                             PublishSubject<Action> executeActionSubject) {
        return new TranslationService(databaseManager, appState, retrofit, executeActionSubject);
    }

    @Provides
    @Singleton
    NetworkService provideNetworkService(Application application) {
        return new NetworkService(application);
    }

    @Provides
    @Singleton
    PublishSubject<Action> provideExecuteActionSubject() {
        return PublishSubject.create();
    }

    @Provides
    @Singleton
    ClipboardManager provideClipboardManager(Application application) {
        return (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    @Singleton
    Vibrator provideVibrator(Application application) {
        return (Vibrator) application.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    @Singleton
    Resources provideResources(Application application) {
        return application.getResources();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPrefences(Application application) {
        return application.getSharedPreferences("seznam_slovnik.prefences", Context.MODE_PRIVATE);
    }
}
