package com.siliconlabs.bluetoothmesh.App.Activities.Logs

import dagger.Module
import dagger.Provides

@Module
class LogsActivityModule {
    @Provides
    fun provideView(logsActivity: LogsActivity): LogsActivityView {
        return logsActivity
    }

    @Provides
    fun providePresenter(logsActivityView: LogsActivityView): LogsActivityPresenter {
        return LogsActivityPresenter(logsActivityView)
    }
}