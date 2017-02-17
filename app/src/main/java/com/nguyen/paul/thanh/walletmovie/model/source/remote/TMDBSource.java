package com.nguyen.paul.thanh.walletmovie.model.source.remote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.chains.MovieSearchChain;
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

import static com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest.GENRE_REQUEST_TAG;
import static com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest.MOVIE_REQUEST_TAG;

/**
 * Created by THANH on 17/02/2017.
 */

public class TMDBSource implements MovieSearchChain.MoviesSearchChainListener {

    private NetworkRequest mNetworkRequest;
    private MovieRequestListener mListener;
    private MoviesMultiSearch mMovieSearch;

    public interface MovieRequestListener {
        void onMovieRequestComplete(List<Movie> movies);
    }

    public TMDBSource(MovieRequestListener listener) {
        List<Genre> genreList = ( (App) App.getAppContext()).getGenreListFromApi();
        if(genreList.size() == 0) {
            //if there is no genre values in cache, send a request to get genre values
            String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
            sendRequestToGetGenreList(genreListUrl);
        }
        mListener = listener;
        mMovieSearch = new MoviesMultiSearch(this, MOVIE_REQUEST_TAG);
    }

    public void getMovies(String url) {
        mMovieSearch.search(url);
    }

    public void cancelRequests() {
        NetworkRequest.getInstance(App.getAppContext())
                .getRequestQueue()
                .cancelAll(MOVIE_REQUEST_TAG);
    }

    @Override
    public void onMoviesSearchComplete(List<Movie> movieList) {
        mListener.onMovieRequestComplete(movieList);
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

        NetworkRequest.getInstance(App.getAppContext()).addToRequestQueue(genreJsonRequest, GENRE_REQUEST_TAG);
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
}
