package de.mpg.mpdl.labcam.code.mvp.view;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.base.BaseView;

/**
 * Created by yingli on 3/20/17.
 */

public interface ItemsView extends BaseView{
    void getItemsSuc(ItemMessage model);
    void getItemsFail(Throwable e);
}
