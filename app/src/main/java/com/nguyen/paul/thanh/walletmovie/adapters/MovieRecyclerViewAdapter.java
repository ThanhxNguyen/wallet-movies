package com.nguyen.paul.thanh.walletmovie.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
 * Adapter for movies list
 */

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder> {

    private Context mContext;
    private List<Movie> mMoviesList;
    private OnRecyclerViewClickListener mListener;
    private int mLayoutForPopupMenu;
    private int gridListViewLayout;
    private int listViewLayout;
    //flag to indicate list view display type
    private boolean displayListInGrid;

    public interface OnRecyclerViewClickListener {
        int ADD_TO_FAVOURITE_TRIGGERED = 1000;
        int REMOVE_MOVIE_TRIGGERED = 1001;
        void onRecyclerViewClick(Movie movie);
        void onPopupMenuClick(PopupMenu popupMenu, Movie movie, int action);
    }

    public MovieRecyclerViewAdapter(Context context, List<Movie> moviesList, OnRecyclerViewClickListener listener, int layoutForPopupMenu) {
        mContext = context;
        mMoviesList = moviesList;
        mListener = listener;
        mLayoutForPopupMenu = layoutForPopupMenu;
        //initialize list view layouts
        gridListViewLayout = R.layout.grid_movie_list_item;
        listViewLayout = R.layout.movie_list_item;
        //set list vies display in grid by default
        displayListInGrid = true;
    }

    @Override
    public MovieRecyclerViewAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(displayListInGrid) {
            //display list view in grid
            view = LayoutInflater.from(parent.getContext())
                    .inflate(gridListViewLayout, parent, false);
        } else {
            //display list view in normal list
            view = LayoutInflater.from(parent.getContext())
                    .inflate(listViewLayout, parent, false);
        }

        return new MovieViewHolder(mContext, view, mListener, mLayoutForPopupMenu);
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter.MovieViewHolder holder, int position) {
        Movie movie = mMoviesList.get(position);
        if(movie != null) {
            holder.setMovie(movie);
            //binding view holder
            holder.bind();
        }
    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    public void setGridListViewLayout() {
        displayListInGrid = true;
    }

    public void setListViewLayout() {
        displayListInGrid = false;
    }

    @SuppressWarnings("WeakerAccess")
    public class MovieViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

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
            mThumbnail = (ImageView) mView.findViewById(R.id.movie_poster);
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
            if(displayListInGrid) {
                mDescription.setText( (mMovie.getOverview().length() > 100) ? mMovie.getOverview().substring(0, 101) + "..." : mMovie.getOverview() );
            }

            //set click listener for image thumbnail, when it's clicked, navigate to movie details
            mView.setOnClickListener(mItemClickListener);

            //set click listener for 3-dots popup menu
            mThreeDotsMenu.setOnClickListener(mThreeDotsMenuClickListener);

            if(!TextUtils.isEmpty(mMovie.getPosterPath())) {
                //if display image in grid, get the image size w342 otherwise w92 (w=width)
                String sizeConfig = (displayListInGrid) ? "w342" : "w92";
                //load movie thumb from internet
                String imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl(sizeConfig) + mMovie.getPosterPath();

                Glide.with(mContext).load(imgUrl)
//                        .thumbnail(0.1f)
                        .crossFade()
//                        .fitCenter()
                        //Glide has some issues with loading vector drawable at the moment
                        .placeholder(R.drawable.ic_image_placeholder_white_24dp)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.ic_image_placeholder_white_24dp)
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
