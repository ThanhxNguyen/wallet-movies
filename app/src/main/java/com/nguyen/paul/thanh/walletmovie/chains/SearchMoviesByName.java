package com.nguyen.paul.thanh.walletmovie.chains;

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
 * Concrete implementation of MovieSearchChain
 */

public class SearchMoviesByName implements MovieSearchChain {

    private MovieSearchChain mNextChain;
    private MoviesSearchChainListener mListener;
    private NetworkRequest mNetworkRequest;
    private String requestTag;

    public SearchMoviesByName(MoviesSearchChainListener listener, String requestTag) {
        mListener = listener;
        mNetworkRequest = NetworkRequest.getInstance(App.getAppContext());
        this.requestTag = requestTag;
    }

    @Override
    public void setNextChain(MovieSearchChain nextChain) {
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
                                mListener.onMoviesSearchComplete(movieList);
                            } else {
                                //pass request to next chain to handle
                                mNextChain.search(url);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //there is error, can't handle the search, pass to next search chain to handle
//                            mListener.onMoviesSearchComplete(null);
                            mNextChain.search(url);
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
        Movie movie = null;

        try {
            if(!obj.isNull("poster_path")) {
                movie = new Movie();
                movie.setId(obj.getInt("id"));
                movie.setTitle(obj.getString("title"));
                movie.setOverview(obj.getString("overview"));
                movie.setReleaseDate(obj.getString("release_date"));
                movie.setRuntime(0);//put ternary condition here maybe
                movie.setCountry("Unknown");
                movie.setStatus("Unknown");
                movie.setVoteAverage(obj.getDouble("vote_average"));
                movie.setPosterPath(obj.getString("poster_path"));
                //get genre id from movie json object and use it to get genre name from genre list
                JSONArray genreIds = obj.getJSONArray("genre_ids");
                //get genre values from cache
                List<Genre> genreListFromApi = ((App) App.getAppContext()).getGenreListFromApi();
                if (genreListFromApi.size() > 0) {
                    List<Genre> movieGenreList = new ArrayList<>();
                    for (int i = 0; i < genreIds.length(); i++) {
                        for (Genre g : genreListFromApi) {
                            if (genreIds.getInt(i) == g.getId()) {
                                //found matching id
                                movieGenreList.add(g);
                                break;
                            }
                        }
                    }

                    movie.setGenres(movieGenreList);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return movie;
    }
}
