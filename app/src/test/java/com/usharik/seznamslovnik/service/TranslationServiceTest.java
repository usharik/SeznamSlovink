package com.usharik.seznamslovnik.service;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.adapter.TranslationResult;
import com.usharik.seznamslovnik.dao.AppDatabase;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;

import io.reactivex.Maybe;
import io.reactivex.subjects.PublishSubject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TranslationServiceTest {

    TranslationService translationService;
    Retrofit retrofit;
    Retrofit suggestionRetrofit;
    TranslationStorageDao translationStorageDao;

    @Before
    public void before() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_TRANSLATE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();

        suggestionRetrofit = new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_SUGGEST)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        AppState appState = new AppState();
        appState.isOfflineMode = false;

        translationStorageDao = mock(TranslationStorageDao.class);
        AppDatabase appDatabase = mock(AppDatabase.class);
        when(appDatabase.translationStorageDao()).thenReturn(translationStorageDao);

        DatabaseManager databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getActiveDbInstance()).thenReturn(appDatabase);

        translationService = new TranslationService(databaseManager,
                appState, retrofit, suggestionRetrofit, mock(NetworkService.class), PublishSubject.create());
    }

    @Test
    public void testOnlineTranslation1() {
        TranslationResult translationResult = translationService.getOnlineTranslation("pas", "cz", "ru")
                .blockingGet();
        assertEquals(3, translationResult.getTranslations().size());
        assertEquals("pas", translationResult.getWord());
    }

    @Test
    public void testOnlineTranslation2() {
        final String[] A_TRANSLATIONS = new String[]{"и","да", "уж", "плюс", "плюс", "да", "и да́же", "а", "но", "да",
                "a to, a sice а и́менно", "да", "и (поэ́тому)", "и потому́", "а", "и"};

        TranslationResult translationResult = translationService.getOnlineTranslation("a", "cz", "ru")
                .blockingGet();
        assertEquals(A_TRANSLATIONS.length, translationResult.getTranslations().size());
        assertEquals("a", translationResult.getWord());
        for (int i=0; i<translationResult.getTranslations().size(); i++) {
            assertEquals(A_TRANSLATIONS[i], translationResult.getTranslations().get(i));
        }
    }

    @Test
    public void testOnlineSuggestions() {
        when(translationStorageDao.getSuggestions(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(Maybe.just(new ArrayList<>()));

        List<String> strings = translationService.getOnlineSuggestions("a", "cz", "ru", 50).blockingGet();

        assertEquals(21, strings.size());
    }
}
