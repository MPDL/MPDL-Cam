package de.mpg.mpdl.labcam.code.injection.component;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.injection.module.ApplicationModule;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(BaseActivity baseActivity);

    Context context();
}
