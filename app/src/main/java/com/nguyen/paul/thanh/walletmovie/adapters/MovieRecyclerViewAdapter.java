package com.nguyen.paul.thanh.walletmovie.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;

import java.util.List;

/**
 * Created by THANH on 6/01/2017.
 */

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder> {

    private Context mContext;
    private List<Movie> mMoviesList;
    private OnRecyclerViewClickListener mListener;

    public interface OnRecyclerViewClickListener {
        public void onRecyclerViewClick(Movie movie);
    }

    public MovieRecyclerViewAdapter(Context mContext, List<Movie> moviesList, OnRecyclerViewClickListener listener) {
        this.mContext = mContext;
        this.mMoviesList = moviesList;
        this.mListener = listener;
    }

    @Override
    public MovieRecyclerViewAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.movie_list_item, parent, false);

        MovieViewHolder holder = new MovieViewHolder(view, mListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter.MovieViewHolder holder, int position) {
        Movie movie = mMoviesList.get(position);
        //binding view holder
        holder.bind(mContext, movie);
    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private OnRecyclerViewClickListener mListener;
        public TextView title;
        public ImageView thumbnail;

        public MovieViewHolder(View view, OnRecyclerViewClickListener listener) {
            super(view);
            this.mView = view;
            this.mListener = listener;
            title = (TextView) mView.findViewById(R.id.movie_title);
            thumbnail = (ImageView) mView.findViewById(R.id.movie_thumbnail);
        }

        //populate UI and set listener appropriately
        public void bind(Context context, final Movie movie) {
            title.setText(movie.getTitle());

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onRecyclerViewClick(movie);
                }
            });

            //load movie thumb from internet
            String imgUrl = MovieQueryBuilder.getImageBaseUrl("w185") + movie.getPosterPath();

            Glide.with(context).load(imgUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(thumbnail);
        }

    }
}
