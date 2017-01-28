package com.nguyen.paul.thanh.walletmovie.chains;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of RequestChain
 */

public class SearchMoviesByCast implements RequestChain {

    private static final String TAG = "SearchMoviesByCast";

    private RequestChain mNextChain;
    private OnChainComplete mListener;
    private NetworkRequest mNetworkRequest;
    private String requestTag;
    private Activity mActivity;

    public SearchMoviesByCast(Activity activity, OnChainComplete listener, NetworkRequest networkRequest, String requestTag) {
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
                            /*
                            * If movie search by cast name, it will return cast basic information including cast ID.
                            * get the cast ID and send one more request to get movies related to that cast id.
                            * The reason because TMDB multi search only return maximum 3 movies related to a cast. Therefore,
                            * to provide more search results, another http request needs to be made.
                            */
                            JSONArray results = response.getJSONArray("results");
                            JSONObject castProfile = results.getJSONObject(0);
                            int castId = castProfile.getInt("id");
                            getMoviesRelatedToCast(castId);

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

    public void getMoviesRelatedToCast(int castId) {
        final String url = MovieQueryBuilder.getInstance().discover().moviesRelatedTo(castId).build();
        final List<Movie> movieList = new ArrayList<>();
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
                                //since this is the last chain, return whatever the result is
                                mListener.onChainComplete(movieList);
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
}
