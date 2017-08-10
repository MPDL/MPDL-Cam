package de.mpg.mpdl.labcam.code.data.net.api;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface CollectionMessageApi {

    @GET("collections?size=10000")
    Observable<CollectionMessage> getCollections();

    @GET("collections?size=10000")
    Observable<CollectionMessage> getGrantedCollectionMessage(@Query("q") String q);
}
