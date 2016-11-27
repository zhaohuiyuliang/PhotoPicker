
package runtimepermissions;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class PermissionsResultAction {
    private static final String TAG = PermissionsResultAction.class.getSimpleName();

    private final Set<String> mPermissions = new HashSet<>(1);

    private Looper mLooper = Looper.getMainLooper();

    @SuppressWarnings("WeakerAccess")
    @CallSuper
    protected synchronized final boolean onResult(final @NonNull
    String permission, int result) {
        if (result == PackageManager.PERMISSION_GRANTED) {
            return onResult(permission, Permissions.GRANTED);
        } else {
            return onResult(permission, Permissions.DENIED);
        }

    }

    public abstract void onGranted();

    public abstract void onDenied(String permission);

    @SuppressWarnings("WeakerAccess")
    @CallSuper
    protected synchronized final boolean onResult(final @NonNull
    String permission, Permissions result) {
        mPermissions.remove(permission);
        if (result == Permissions.GRANTED) {
            if (mPermissions.isEmpty()) {
                new Handler(mLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        onGranted();
                    }
                });
                return true;
            }
        } else if (result == Permissions.DENIED) {
            new Handler(mLooper).post(new Runnable() {
                @Override
                public void run() {
                    onDenied(permission);
                }
            });
            return true;
        } else if (result == Permissions.NOT_FOUND) {
            if (shouldIgnorePermissionNotFound(permission)) {
                if (mPermissions.isEmpty()) {
                    new Handler(mLooper).post(new Runnable() {
                        @Override
                        public void run() {
                            onGranted();
                        }
                    });
                    return true;
                }
            } else {
                new Handler(mLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        onDenied(permission);
                    }
                });
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({
            "WeakerAccess", "SameReturnValue"
    })
    public synchronized boolean shouldIgnorePermissionNotFound(String permission) {
        Log.d(TAG, "Permission not found: " + permission);
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    @CallSuper
    protected synchronized final void registerPermissions(@NonNull
    String[] perms) {
        Collections.addAll(mPermissions, perms);
    }
}
