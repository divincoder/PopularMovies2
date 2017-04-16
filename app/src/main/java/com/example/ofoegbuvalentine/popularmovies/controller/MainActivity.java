package com.example.ofoegbuvalentine.popularmovies.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ofoegbuvalentine.popularmovies.MovieAdapter;
import com.example.ofoegbuvalentine.popularmovies.NetworkChecker;
import com.example.ofoegbuvalentine.popularmovies.R;
import com.example.ofoegbuvalentine.popularmovies.api.Client;
import com.example.ofoegbuvalentine.popularmovies.api.Service;
import com.example.ofoegbuvalentine.popularmovies.model.Movie;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.ofoegbuvalentine.popularmovies.MovieAdapter.MOVIE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SORT_STATE = "sort_state";
    private final static String SORT_TOP = "Top Rated";
    private final static String SORT_POPULAR = "Popular";
    private static final Service API_INTERFACE = Client.getClient().create(Service.class);
    private static final Type TYPE = new TypeToken<List<Movie>>() {
    }.getType();
    private boolean isTopRated = false;
    private RecyclerView mMoviesRecyclerView;
    private ProgressBar mLoadingIndicator;
    private ArrayList<Movie> mMoviesList;
    private MovieAdapter mMoviesAdapter;
    private CollapsingToolbarLayout toolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.progressBar);toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE) || !savedInstanceState.containsKey(SORT_STATE)) {
            if (NetworkChecker.isNetworkConnected(this)) {
                getMoviesBySortOrder(isTopRated);
            } else {
                NetworkChecker.showDialog(this, android.R.drawable.ic_dialog_alert, R.string.internet)
                        .setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        })
                        .show();
            }
        } else if (savedInstanceState.getParcelableArrayList(MOVIE) == null) {
            if (NetworkChecker.isNetworkConnected(this)) {
                getMoviesBySortOrder(isTopRated);
            } else {
                NetworkChecker.showDialog(this, android.R.drawable.ic_dialog_alert, R.string.internet)
                        .setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        })
                        .show();
            }
        } else {
            mMoviesList = savedInstanceState.getParcelableArrayList(MOVIE);
            isTopRated = savedInstanceState.getBoolean(SORT_STATE);
            if (isTopRated) {
                toolbarLayout.setTitle(getString(R.string.title, SORT_TOP));
            } else {
                toolbarLayout.setTitle(getString(R.string.title, SORT_POPULAR));
            }
            loadData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE, mMoviesList);
        outState.putBoolean(SORT_STATE, isTopRated);
    }

    /**
     * Method to load data in views
     */
    private void loadData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mMoviesAdapter = new MovieAdapter(MainActivity.this, mMoviesList);
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);
    }

    /**
     * helper method to get popular movies
     */
    private void getPopularMovies() {
        Call<JsonObject> call = API_INTERFACE.getPopularMovies(getString(R.string.api_key), 1);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                mMoviesList = new Gson().fromJson(response.body().getAsJsonArray("results"), TYPE);
                loadData();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                t.printStackTrace();
                NetworkChecker.showToast(MainActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG);


            }
        });
    }

    /**
     * method to get top rated movies
     */
    private void getTopRatedMovies() {
        Call<JsonObject> call = API_INTERFACE.getTopRatedMovies(getString(R.string.api_key), 1);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                mMoviesList = new Gson().fromJson(response.body().getAsJsonArray("results"), TYPE);
                loadData();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                t.printStackTrace();
                NetworkChecker.showToast(MainActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getTitle().equals(SORT_TOP)) {
            getMoviesBySortOrder(true);
            isTopRated = true;
            item.setTitle(SORT_POPULAR);
        } else if (item.getTitle().equals(SORT_POPULAR)) {
            getMoviesBySortOrder(false);
            isTopRated = false;
            item.setTitle(SORT_TOP);
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.sort_order);
        if (isTopRated) {
            menuItem.setTitle(SORT_POPULAR);
        } else {
            menuItem.setTitle(SORT_TOP);
        }
        return true;
    }

    /**
     * method to re-query movie db API and update views based on user sort selection
     *
     * @param sortChoice selected sort choice
     */
    private void getMoviesBySortOrder(boolean sortChoice) {
        if (sortChoice) {
            toolbarLayout.setTitle(getString(R.string.title, SORT_TOP));
            getTopRatedMovies();
        } else {
            toolbarLayout.setTitle(getString(R.string.title, SORT_POPULAR));
            getPopularMovies();
        }
    }
}
