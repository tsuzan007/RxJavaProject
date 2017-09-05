package com.app.pets.rxjavaproject;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    //id references to view

    @BindView(R.id.searchBox)
    EditText searchEditText;

    @BindView(R.id.searchBtn)
    Button searchButton;

    @BindView(R.id.listView)
    ListView mlistView;

    //observable variable that holds two observable
    private Observable<String> observablecombine;

    //variable to dispose observer
    private Disposable disposable;

    //arraylist to hold list of countries
    protected static ArrayList<String> countries=new ArrayList<>();

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



    }


    /*
    Observable that wrap around searchButton events.
    Once search button is clicked, text from the searchbox is
    taken and passed it to the observer.
     */
    private Observable<String> searchObservable(){
        return  Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> e) throws Exception {
                searchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        e.onNext(searchEditText.getText().toString());
                    }
                });
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        searchButton.setOnClickListener(null);
                    }
                });

            }
        });
    }

    private Observable<String> searchTextChangeObservable(){
        return  Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> e) throws Exception {

                TextWatcher textWatcher=new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        e.onNext(s.toString());

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };
                searchEditText.addTextChangedListener(textWatcher);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=new Intent(this,MyintentService.class);
        startService(intent);
        observablecombine=Observable.merge(searchObservable(),searchTextChangeObservable());
       disposable= observablecombine
                .observeOn(Schedulers.io())
                .map(new Function<String, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> apply(@NonNull String s) throws Exception {
                        return searchinList(s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(@NonNull ArrayList<String> strings) throws Exception {
                        arrayAdapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,strings);
                        mlistView.setAdapter(arrayAdapter);
                    }
                });

    }

    public ArrayList<String> searchinList(String s){
        ArrayList<String> searchresult=new ArrayList<>();
        for(String a:countries){
            if(a.startsWith(s)){
                searchresult.add(a);
            }
        }
        return searchresult;
    }

    public static class MyintentService extends IntentService {


         public MyintentService(){
            super("test");
        }
        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Log.e(".....","intentService");
            for (String countryCode : Locale.getISOCountries()) {

                Locale obj = new Locale("", countryCode);

                countries.add(obj.getDisplayCountry());

            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(disposable!=null){
            disposable.dispose();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countries=null;
    }


}
