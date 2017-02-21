package com.nguyen.paul.thanh.walletmovie.pages.castdetails;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;
import com.nguyen.paul.thanh.walletmovie.model.Cast;

/**
 * Created by THANH on 21/02/2017.
 */

public interface CastDetailsContract {

    interface View extends BaseView {
        void populateCastDetails(Cast cast);
    }

    interface Presenter extends BasePresenter {
        void getCastDetails(String castDetailsUrl);
    }

}
