package mo.edu.ipm.stud.envsensing.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import mo.edu.ipm.stud.envsensing.Authenticator;

/**
 * A bound Service that instantiates the authenticator when started.
 * <p/>
 * Reference:
 * https://developer.android.com/training/sync-adapters/creating-authenticator.html
 * #CreateAuthenticatorService
 */
public class AuthenticatorService extends Service {
    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
