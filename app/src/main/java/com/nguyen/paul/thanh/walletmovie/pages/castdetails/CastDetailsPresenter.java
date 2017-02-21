package com.nguyen.paul.thanh.walletmovie.pages.castdetails;

import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.TMDBSource;

/**
 * Created by THANH on 21/02/2017.
 */

public class CastDetailsPresenter implements CastDetailsContract.Presenter {

    private CastDetailsContract.View mView;
    private TMDBSource mTMDBSource;

    public CastDetailsPresenter(CastDetailsContract.View view) {
        mView = view;
        mTMDBSource = new TMDBSource();
        mTMDBSource.setCastDetailsRequestListener(new TMDBSource.CastDetailsRequestListener() {
            @Override
            public void onCastDetailsRequestComplete(Cast cast) {
                mView.populateCastDetails(cast);
            }
        });
    }

    @Override
    public void getCastDetails(String castDetailsUrl) {
        mTMDBSource.getCastDetails(castDetailsUrl);
    }
}
