package com.nguyen.paul.thanh.walletmovie.chains;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of RequestChain
 */

public class SearchMoviesByName implements RequestChain {

    private static final String TAG = "SearchMoviesByName";

    private RequestChain mNextChain;
    private OnChainComplete mListener;
    private NetworkRequest mNetworkRequest;
    private String requestTag;
    private Activity mActivity;

    public SearchMoviesByName(Activity activity, OnChainComplete listener, NetworkRequest networkRequest, String requestTag) {
        mListener = listener;
        mNetworkRequest = networkRequest;
        this.requestTag = requestTag;
        mActivity = activity;
    }

    @Override
    public void setNextChain(RequestChain nextChain) {
        mNextChain = nextChain;
    }

    @Override
    public void search(final String url) {
        final List<Movie> movieList = new ArrayList<>();
        //create JsonObjectRequest and pass it to Volley
        JsonObjectRequest moviesListJsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //successfully
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for(int i=0; i<results.length(); i++) {
                                JSONObject tempMovieJsonObj = results.getJSONObject(i);
                                Movie movie = parseMovieJsonObject(tempMovieJsonObj);
                                if(movie != null) {
                                    movieList.add(movie);
                                }
                            }

                            //if movie is NOT empty, invoke callback to display movies list
                            //or if movie list is empty, no results when search movies by name
                            //try next chain and search movie by actors/actresses
                            if(movieList.size() > 0) {
                                mListener.onChainComplete(movieList);
                            } else {
                                //pass request to next chain to handle
                                mNextChain.search(url);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //handle errors
                    }
                });
        //making network request to get json object from themoviedb.org
        //having trouble when add to request queue using singleton class methods
//      //mNetworkRequest.addToRequestQueue(moviesListJsonObject, NETWORK_REQUEST_TAG);
        moviesListJsonObject.setTag(requestTag);
        mNetworkRequest.getRequestQueue().add(moviesListJsonObject);
    }

    private Movie parseMovieJsonObject(JSONObject obj) {
        Movie movie = new Movie();
        try {

            movie.setId(obj.getInt("id"));
            movie.setTitle(obj.getString("title"));
            movie.setOverview(obj.getString("overview"));
            movie.setReleaseDate(obj.getString("release_date"));
            movie.setRuntime(0);//put ternary condition here maybe
            movie.setCountry("Unknown");
            movie.setStatus("Unknown");
            movie.setVoteAverage(obj.getDouble("vote_average"));
            movie.setPosterPath( (obj.isNull("poster_path"))
                    ? ""
                    : obj.getString("poster_path"));
            //get genre id from movie json object and use it to get genre name from genre list
            JSONArray genreIds = obj.getJSONArray("genre_ids");
            //get genre values from cache
            List<Genre> genreListFromApi = ((App) mActivity.getApplication()).getGenreListFromApi();
            if(genreListFromApi.size() > 0) {
                List<Genre> movieGenreList = new ArrayList<>();
                for(int i=0; i<genreIds.length(); i++) {
                    for(Genre g : genreListFromApi) {
                        if(genreIds.getInt(i) == g.getId()) {
                            //found matching id
                            movieGenreList.add(g);
                            break;
                        }
                    }
                }

                movie.setGenres(movieGenreList);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return movie;
    }
}