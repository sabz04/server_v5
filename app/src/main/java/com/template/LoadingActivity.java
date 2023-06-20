package com.template;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.net.ConnectivityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        FirebaseApp.initializeApp(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        DatabaseReference dbNodeRef = reference.child("db");
        //не работает - этот узел возвращает null
        DatabaseReference linkNodeRef = reference.child("link");

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Keys.PREFS_NAME, MODE_PRIVATE);
        //проверка на интернет-соединение
        ConnectivityManager mgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        //если нет соединения, полная очистка сохраненных данных, чтобы при другом заходе с интернетом, заново кинуть запрос на сервер(было в ТЗ)
        if (netInfo == null || netInfo != null && !netInfo.isConnected()) {
                // Очистка SharedPreferences
                ClearSharedPrefs(sharedPreferences);
                MoveBetweenActivities(LoadingActivity.this, MainActivity.class);
                return;
            }
        //проверяем, есть ли сохр. данные. ДА - открываем то, какой был последний результат и завершаем метод. НЕТ- идем дальше и кидаем запрос на сервер
        String currentSavedValue = sharedPreferences.getString(Keys.PREFS_NAME_LINK, "");
        if(!currentSavedValue.isEmpty() || !currentSavedValue.equals("")){
            if(currentSavedValue.equals(Keys.PREFS_NAME_LINK_ERROR)){
                MoveBetweenActivities(LoadingActivity.this, MainActivity.class);
            }
            else{
                MoveBetweenActivities(LoadingActivity.this, WebActivity.class);
            }
            return;
        }
        //установка слушателя для чтения данных из базы данных
        dbNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object domenName = dataSnapshot.getValue();
                    HashMap<String, String> url = (HashMap<String, String>) domenName;
                    String str = url.get("link");
                    String endUrl = GetUrl(str);

                    //okhttp
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();


                    Request request = new Request.Builder()
                            .url(endUrl)
                            .addHeader("User-Agent", System.getProperty("http.agent"))
                            .build();
                    String requestHeader = request.header("User-Agent");
                    try {
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                EditSavedValue(sharedPreferences,Keys.PREFS_NAME_LINK, Keys.PREFS_NAME_LINK_ERROR);
                                MoveBetweenActivities(LoadingActivity.this, MainActivity.class);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String response_body =  response.body().string();
                                if(response.isSuccessful()){
                                    EditSavedValue(sharedPreferences, Keys.PREFS_NAME_LINK, response_body);
                                    MoveBetweenActivities(LoadingActivity.this, WebActivity.class);
                                }
                                else{
                                    EditSavedValue(sharedPreferences,Keys.PREFS_NAME_LINK, Keys.PREFS_NAME_LINK_ERROR);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки чтения данных
            }
        });


    }
    private void ClearSharedPrefs(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    //чтобы постоянно не писать одно и то же обращение к сохр данным
    private void EditSavedValue(SharedPreferences sharedPreferences, String keyName, String newValue){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyName, newValue);
        editor.apply();
    }
    //чтобы код не повторялся
    private void MoveBetweenActivities(Context packageContext, Class tClass){
        Intent intent = new Intent(packageContext, tClass);
        startActivity(intent);
    }
    //формирование ссылки для отправки на сервер запроса
    private String GetUrl(String domenNameURL){
        return  String.format("%s/?packageid=%s&usserid=%s&getz=%s&getr=utm_source=google-play&utm_medium=organic",
                domenNameURL, getApplicationContext().getPackageName(), UUID.randomUUID(), TimeZone.getDefault().getID()
                );
    }
}