package com.example.foodmanagement;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import com.example.foodmanagement.model.FoodData;

import java.util.Date;
import java.util.List;

public class ExpirationNotifyJobService extends JobService {

    private JobParameters mParams;
    private final static ComponentName JOB_SERVICE_NAME = new ComponentName(
            "com.example.foodmanagement", "com.example.foodmanagement" +
            ".ExpirationNotifyJobService" );
    private final static int JOB_ID = 0x01;

    public static void schedule( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        JobInfo.Builder builder = new JobInfo.Builder( JOB_ID, JOB_SERVICE_NAME );

        // setBackOffCriteria(long initialBackoffMillis, int backoffPolicy)
        //     initialbackoffMillis: バックオフ時間算出の基準値
        //     backoffPolicy: BACKOFF_POLICY_LINEARかBACKOFF_POLICY_EXPONENTIALを指定
        //                    LINEARの時は current+initial * fail_count
        //                    EXPONENTIALの時は current + initial * 2 ^ (fail_count -1)
        //                    後にリトライされる。defaultは30sec, EXPONENTIAL。最長バックオフは5hr
        builder.setBackoffCriteria( 10000, JobInfo.BACKOFF_POLICY_LINEAR );

        // setExtras(PersistableBundle)
        // PersistableBundleを利用して、onStartJob時に取り出すbundleを用意できる
        PersistableBundle bundle = new PersistableBundle();
        builder.setExtras( bundle );

        // setMinimumLatency()
        //     実行可能になってからの最低遅延時間を設定する
        //     定期実行Jobには必要ないため、build()時にエラー扱いとなる
//        builder.setMinimumLatency(5000);

        // setOverrideDeadline()
        //     実行可能になってからの最大遅延時間を設定する
        //     定期実行Jobには必要ないため、build()時にエラー扱いとなる
//        builder.setOverrideDeadline(20000);

        // setPeriodic()
        //     定期実行を設定する。前のJobが終わってからの経過時間(Millis)を指定する
        //     Priodic指定した場合は、状態が変更されても継続実行される
        builder.setPeriodic( 1000 * 60 * 15 );

        // setPersisted()
        //     再起動時にJobを実行継続させるかどうか。
        //     trueを設定した場合は、BOOT_COMPLETEDが無いとエラー扱いとなる
        builder.setPersisted( true );

        // setRequiredNetworkType()
        //      Jobの実行に必要なネットワーク形態を設定する
        //      NETWORK_TYPE_NONE: 指定なし（ありでもなしでも）
        //      NETWORK_TYPE_ANY: なんらかのネットワーク
        //      NETWORK_TYPE_UNMETERD: 従量制でないネットワーク
        builder.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY );

        // Deviceがidle maintenance windowの時に実行するかどうか
        // idolかつbackoffCriteriaを設定するとexceptionを拾う
//        builder.setRequiresDeviceIdle(true);

        // Deviceが給電状態かどうかを設定する
        builder.setRequiresCharging( false );

        scheduler.schedule( builder.build() );
    }

    public static void cancelJobs( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        scheduler.cancel( JOB_ID );

        // scheduler.cancelAll();
    }

    @Override
    public boolean onStartJob( JobParameters params ) {

        mParams = params;
        new Thread( new Runnable() {

            @Override
            public void run() {
                Log.d( "-----", "check expiration task called." );
                FoodService svc = new FoodService();
                List<FoodData> foodList = svc.getFoodList();
                Date now = new Date();
                foodList.forEach( ex -> {
                    long nowMills = now.getTime();
                    long expirationMills = ex.getExpiration().getTime();

                    long diff =
                            ( expirationMills - nowMills ) / 1000 * 60 * 60 * 24;

                    // 賞味期限切れ3日前に通知
                    if (diff <= 1) {
                        // TODO 通知
                        Log.d( "通知：", ex.getFoodName() + "はあと1日で賞味期限が切れます" );
                    } else if (diff <= 3) {
                        // TODO 通知
                        Log.d( "通知：", ex.getFoodName() + "はあと3日で賞味期限が切れます" );
                    }
                } );

                if (mParams != null) {
                    jobFinished( mParams, false );
                }
            }
        } ).start();

        return true;
    }

    @Override
    public boolean onStopJob( JobParameters params ) {

        jobFinished( params, false );
        return false;
    }

    private void checkExpiration() {

    }
}
