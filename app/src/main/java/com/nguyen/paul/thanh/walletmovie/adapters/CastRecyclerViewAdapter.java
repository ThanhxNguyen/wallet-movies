package com.nguyen.paul.thanh.walletmovie.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Recycler view adapter for movie casts list
 */

public class CastRecyclerViewAdapter extends RecyclerView.Adapter<CastRecyclerViewAdapter.CastViewHolder> {

    private Context mContext;
    private List<Cast> mCastList;
    private OnMovieCastItemClick mListner;

    public interface OnMovieCastItemClick {
        void onMovieCastItemClick(Cast cast);
    }

    public CastRecyclerViewAdapter(Context context, List<Cast> casts, OnMovieCastItemClick listener) {
        mContext = context;
        mCastList = casts;
        mListner = listener;
    }

    @Override
    public CastRecyclerViewAdapter.CastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate the layout
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cast_list_item, parent, false);

        return new CastViewHolder(mContext, view, mListner);
    }

    @Override
    public void onBindViewHolder(CastRecyclerViewAdapter.CastViewHolder holder, int position) {
        Cast cast = mCastList.get(position);

        holder.setCast(cast);
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return mCastList.size();
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "CastViewHolder";

        private OnMovieCastItemClick mListener;
        private Context mContext;
        private Cast mCast;
        public View mView;
        public CircleImageView mCastThumbnail;
        public TextView mCastName;
        public TextView mCastCharacter;

        public CastViewHolder(Context context, View itemView, OnMovieCastItemClick listener) {
            super(itemView);
            mContext = context;
            mListener = listener;
            mView = itemView;
            mCastThumbnail = (CircleImageView) mView.findViewById(R.id.movie_cast_thumbnail);
            mCastName = (TextView) mView.findViewById(R.id.movie_cast_name);
            mCastCharacter = (TextView) mView.findViewById(R.id.movie_cast_character);

            //set click listener
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onMovieCastItemClick(mCast);
                }
            });
        }

        public void setCast(Cast cast) {
            mCast = cast;
        }

        public void bind() {
            String character = mCast.getCharacter();
            mCastName.setText(mCast.getName());
            if(TextUtils.isEmpty(character)) {
                mCastCharacter.setText("");
            } else {
                mCastCharacter.setText(character.contains("/") ? character.split("/")[0] : character);
            }

            String imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl("w500") + mCast.getProfilePath();
            Glide.with(mContext).load(imgUrl)
                    .dontAnimate()
                    .placeholder(R.drawable.ic_account_circle_white_24dp)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.ic_account_circle_white_24dp)
                    .into(mCastThumbnail);
        }
    }
}
