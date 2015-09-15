package mo.edu.ipm.stud.envsensing;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Implement AbstractAccountAuthenticator and stub out all of its methods.
 * <p/>
 * Reference:
 * https://developer.android.com/training/sync-adapters/creating-authenticator.html
 * #CreateAuthenticator
 */
public class Authenticator extends AbstractAccountAuthenticator {

    public Authenticator(Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse r, String s, String s1,
                             String[] strings, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse r, Account account,
                                    String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

}
