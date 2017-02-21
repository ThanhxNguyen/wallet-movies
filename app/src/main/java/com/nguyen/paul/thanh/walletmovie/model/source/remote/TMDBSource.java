package com.nguyen.paul.thanh.walletmovie.model.source.remote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.chains.MovieSearchChain;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.MoviesMultiSearch;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest.NETWORK_REQUEST_TAG;

/**
 * Created by THANH on 17/02/2017.
 */

public class TMDBSource implements MovieSearchChain.MoviesSearchChainListener {

    private NetworkRequest mNetworkRequest;
    private MovieRequestListener mMovieRequestListener;
    private MoviesMultiSearch mMovieSearch;
    private TrailersRequestListener mTrailersRequestListener;
    private MovieCastsRequestListener mMovieCastsRequestListener;

    public interface MovieRequestListener {
        void onMovieRequestComplete(List<Movie> movies);
    }

    public interface TrailersRequestListener {
        void onTrailersRequestComplete(List<String> trailerList);
    }

    public interface MovieCastsRequestListener {
        void onMovieCastsRequestComplete(List<Cast> castList);
    }

    public TMDBSource() {
        List<Genre> genreList = ( (App) App.getAppContext()).getGenreListFromApi();
        if(genreList.size() == 0) {
            //if there is no genre values in cache, send a request to get genre values
            String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
            sendRequestToGetGenreList(genreListUrl);
        }
        mMovieSearch = new MoviesMultiSearch(this, NETWORK_REQUEST_TAG);
    }

    public void setTrailersRequestListener(TrailersRequestListener listener) {
        mTrailersRequestListener = listener;
    }

    public void setMovieRequestListener(MovieRequestListener listener) {
        mMovieRequestListener = listener;
    }

    public void setMovieCastsRequestListener(MovieCastsRequestListener listener) {
        mMovieCastsRequestListener = listener;
    }

    public void getMovies(String url) {
        mMovieSearch.search(url);
    }

    public void cancelRequests() {
        NetworkRequest.getInstance(App.getAppContext())
                .getRequestQueue()
                .cancelAll(NETWORK_REQUEST_TAG);
    }

    @Override
    public void onMoviesSearchComplete(List<Movie> movieList) {
        mMovieRequestListener.onMovieRequestComplete(movieList);
    }

    private void sendRequestToGetGenreList(String url) {
        JsonObjectRequest genreJsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //successfully get data
                        try {
                            JSONArray genres = response.getJSONArray("genres");
                            parseGenreList(genres);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "onErrorResponse: Error getting genre list " + error.toString());
                    }
                });

        genreJsonRequest.setTag(NETWORK_REQUEST_TAG);
        NetworkRequest.getInstance(App.getAppContext()).getRequestQueue().add(genreJsonRequest);
    }

    private void parseGenreList(JSONArray genres) {
        List<Genre> genreList = new ArrayList<>();
        for(int i=0; i<genres.length(); i++) {
            try {
                JSONObject genreJsonObj = genres.getJSONObject(i);
                int genreId = genreJsonObj.getInt("id");
                String genreName = genreJsonObj.getString("name");

                Genre genre = new Genre(genreId, genreName);
                genreList.add(genre);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //cache genres list value
        ( (App) App.getAppContext()).setGenreListFromApi(genreList);
    }

    public void getTrailers(String movieTrailerUrl) {
        JsonObjectRequest movieTrailersJsonRequest = new JsonObjectRequest(Request.Method.GET, movieTrailerUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<String> trailerList = new ArrayList<>();
                            JSONArray results = response.getJSONArray("results");
                            if(results.length() > 0) {
                                for(int i=0; i<results.length(); i++) {
                                    JSONObject trailerObj = results.getJSONObject(i);
                                    trailerList.add(trailerObj.getString("key"));
                                }

                            }
                            mTrailersRequestListener.onTrailersRequestComplete(trailerList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTrailersRequestListener.onTrailersRequestComplete(null);
                    }
                });

        movieTrailersJsonRequest.setTag(NETWORK_REQUEST_TAG);
        NetworkRequest.getInstance(App.getAppContext()).getRequestQueue().add(movieTrailersJsonRequest);
    }

    public void getMovieCasts(String movieCastsUrl) {
        final int castLimit = 6;
        JsonObjectRequest castJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, movieCastsUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Cast> castList = new ArrayList<>();
                            JSONArray casts = response.getJSONArray("cast");
                            //get the first 6 casts for now
                            for(int i=0; i<castLimit; i++) {
                                JSONObject castJsonObj = casts.getJSONObject(i);
                                Cast cast = new Cast();
                                cast.setId(castJsonObj.getInt("id"));
                                cast.setName(castJsonObj.getString("name"));
                                cast.setCharacter(castJsonObj.getString("character"));
                                cast.setProfilePath(castJsonObj.getString("profile_path"));

                                //add new cast to the list
                                castList.add(cast);
                            }

                            //return results back
                            mMovieCastsRequestListener.onMovieCastsRequestComplete(castList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mMovieCastsRequestListener.onMovieCastsRequestComplete(null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mMovieCastsRequestListener.onMovieCastsRequestComplete(null);
                    }
                });

        //set tag for this request
        castJsonObjectRequest.setTag(NETWORK_REQUEST_TAG);
        //add to request queue
        NetworkRequest.getInstance(App.getAppContext()).getRequestQueue().add(castJsonObjectRequest);
    }
}
