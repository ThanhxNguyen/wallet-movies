package com.nguyen.paul.thanh.walletmovie.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
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
    private int mLayoutForPopupMenu;

    public interface OnRecyclerViewClickListener {
        public static final int ADD_TO_FAVOURITE_TRIGGERED = 1000;
        public static final int REMOVE_MOVIE_TRIGGERED = 1001;
        public void onRecyclerViewClick(Movie movie);
        public void onPopupMenuClick(PopupMenu popupMenu, Movie movie, int action);
    }

    public MovieRecyclerViewAdapter(Context context, List<Movie> moviesList, OnRecyclerViewClickListener listener, int layoutForPopupMenu) {
        mContext = context;
        mMoviesList = moviesList;
        mListener = listener;
        mLayoutForPopupMenu = layoutForPopupMenu;
    }

    @Override
    public MovieRecyclerViewAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.movie_list_item, parent, false);

        MovieViewHolder holder = new MovieViewHolder(mContext, view, mListener, mLayoutForPopupMenu);

        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter.MovieViewHolder holder, int position) {
        Movie movie = mMoviesList.get(position);
        holder.setMovie(movie);
        //binding view holder
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        private Context mContext;
        private View mView;
        private PopupMenu mPopupMenu;
        private OnRecyclerViewClickListener mListener;
        private View.OnClickListener mItemClickListener;
        private View.OnClickListener mThreeDotsMenuClickListener;
        private int mLayoutForPopupmenu;
        private Movie mMovie;
        public TextView mTitle;
        public ImageView mThumbnail;
        public ImageView mThreeDotsMenu;
        public TextView mReleaseDateValue;
        public TextView mVoteValue;
        public TextView mGenres;
        public TextView mDescription;

        public MovieViewHolder(Context context, View view, OnRecyclerViewClickListener listener, int layoutForPopupMenu) {
            super(view);
            mContext = context;
            mView = view;
            mListener = listener;
            mLayoutForPopupmenu = layoutForPopupMenu;

            mTitle = (TextView) mView.findViewById(R.id.movie_title);
            mThumbnail = (ImageView) mView.findViewById(R.id.movie_thumbnail);
            mThreeDotsMenu = (ImageView) mView.findViewById(R.id.three_dots_menu);
            mReleaseDateValue = (TextView) mView.findViewById(R.id.movie_release_date_value);
            mVoteValue = (TextView) mView.findViewById(R.id.movie_vote_value);
            mGenres = (TextView) mView.findViewById(R.id.movie_genres);
            mDescription = (TextView) mView.findViewById(R.id.movie_description);

            //initialize click listeners
            mItemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onRecyclerViewClick(mMovie);
                }
            };
            mThreeDotsMenuClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayPopupMenu();
                }
            };
        }

        private void displayPopupMenu() {
            mPopupMenu = new PopupMenu(mContext, mThreeDotsMenu);
            MenuInflater inflater = mPopupMenu.getMenuInflater();
            //inflate popup based on different pages. For example, home page popup menu doesn't have "remove movie" option etc...
            inflater.inflate(mLayoutForPopupmenu, mPopupMenu.getMenu());
            //set listener for popup menu
            mPopupMenu.setOnMenuItemClickListener(this);
            //display popup menu
            mPopupMenu.show();
        }

        //populate UI and set listener appropriately
        public void bind() {
            mTitle.setText(mMovie.getTitle());
            mReleaseDateValue.setText(mMovie.getReleaseDate());
            mVoteValue.setText(String.valueOf(mMovie.getVoteAverage()));

            List<Genre> genres = mMovie.getGenres();
            StringBuilder genreValues = new StringBuilder();
            String prefix = " | ";
            for(int i=0; i<genres.size(); i++) {
                genreValues.append(genres.get(i).getName());
                genreValues.append(prefix);

            }
            mGenres.setText( (genreValues.toString().length()>0) ? genreValues.delete(genreValues.length()-2, genreValues.length()-1) : "Unknown");
            mDescription.setText( (mMovie.getOverview().length() > 100) ? mMovie.getOverview().substring(0, 101) + "..." : mMovie.getOverview() );

            //set click listener for image thumbnail, when it's clicked, navigate to movie details
            mView.setOnClickListener(mItemClickListener);

            //set click listener for 3-dots popup menu
            mThreeDotsMenu.setOnClickListener(mThreeDotsMenuClickListener);

            if(mMovie.getPosterPath().equals("")) {
                Log.d("something", "bind: poster path is null for " + mMovie.getTitle());
                Glide.with(mContext)
                        .load("")
                        .placeholder(R.drawable.ic_favorite)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .into(mThumbnail);
            } else {
                //load movie thumb from internet
                String imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl("w500") + mMovie.getPosterPath();

                Glide.with(mContext).load(imgUrl)
//                        .thumbnail(0.1f)
                        .crossFade()
                        .fitCenter()
                        .placeholder(R.drawable.ic_image_placeholder_white)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.ic_image_placeholder_white)
                        .into(mThumbnail);
            }
        }

        public void setMovie(Movie movie) {
            this.mMovie = movie;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            //handle popup menu click events here
            switch (item.getItemId()) {
                case R.id.popup_add_favourite:
                    mListener.onPopupMenuClick(mPopupMenu, mMovie, OnRecyclerViewClickListener.ADD_TO_FAVOURITE_TRIGGERED);
                    return true;
                case R.id.popup_remove_movie:
                    mListener.onPopupMenuClick(mPopupMenu, mMovie, OnRecyclerViewClickListener.REMOVE_MOVIE_TRIGGERED);
                default:
                    return false;
            }

        }
    }
}
