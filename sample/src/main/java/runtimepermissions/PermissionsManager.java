
package runtimepermissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PermissionsManager {
    private static final String TAG = PermissionsManager.class.getSimpleName();

    private static PermissionsManager mInstance = null;

    private final Set<String> mPendingRequests = new HashSet<String>(1);

    private final Set<String> mPermissions = new HashSet<String>(1);

    private final List<WeakReference<PermissionsResultAction>> mPendingActions = new ArrayList<WeakReference<PermissionsResultAction>>(
            1);

    public static PermissionsManager getInstance() {
        if (mInstance == null) {
            mInstance = new PermissionsManager();
        }
        return mInstance;
    }

    private PermissionsManager() {
        initializePermissionsMap();
    }

    @SuppressWarnings("unused")
    public synchronized void requestAllManifestPermissionsIfNecessary(final @Nullable
    Activity activity, final @Nullable
    PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        String[] perms = getManifestPermissions(activity);
        requestPermissionsIfNecessaryForResult(activity, perms, action);
    }

    @NonNull
    private synchronized String[] getManifestPermissions(@NonNull
    final Activity activity) {
        PackageInfo packageInfo = null;
        List<String> list = new ArrayList<String>(1);
        try {
            Log.d(TAG, activity.getPackageName());
            packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "A problem occurred when retrieving permissions", e);
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null) {
                for (String perm : permissions) {
                    Log.d(TAG, "Manifest contained permission: " + perm);
                    list.add(perm);
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    @SuppressWarnings("unused")
    public synchronized void requestPermissionsIfNecessaryForResult(@Nullable
    Activity activity, @NonNull
    String[] permissions, @Nullable
    PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                // if there is no permission to request, there is no reason to
                // keep the action int the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                mPendingRequests.addAll(permList);
                ActivityCompat.requestPermissions(activity, permsToRequest, 1);
            }
        }
    }

    private synchronized void addPendingAction(@NonNull
    String[] permissions, @Nullable
    PermissionsResultAction action) {
        if (action == null) {
            return;
        }
        action.registerPermissions(permissions);
        mPendingActions.add(new WeakReference<PermissionsResultAction>(action));
    }

    private synchronized void removePendingAction(@Nullable
    PermissionsResultAction action) {
        for (Iterator<WeakReference<PermissionsResultAction>> iterator = mPendingActions.iterator(); iterator
                .hasNext();) {
            WeakReference<PermissionsResultAction> weakRef = iterator.next();
            if (weakRef.get() == action || weakRef.get() == null) {
                iterator.remove();
            }
        }
    }

    @NonNull
    private List<String> getPermissionsListToRequest(@NonNull
    Activity activity, @NonNull
    String[] permissions, @Nullable
    PermissionsResultAction action) {
        List<String> permList = new ArrayList<String>(permissions.length);
        for (String perm : permissions) {
            if (!mPermissions.contains(perm)) {
                if (action != null) {
                    action.onResult(perm, Permissions.NOT_FOUND);
                }
            } else if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                if (!mPendingRequests.contains(perm)) {
                    permList.add(perm);
                }
            } else {
                if (action != null) {
                    action.onResult(perm, Permissions.GRANTED);
                }
            }
        }
        return permList;
    }

    private void doPermissionWorkBeforeAndroidM(@NonNull
    Activity activity, @NonNull
    String[] permissions, @Nullable
    PermissionsResultAction action) {
        for (String perm : permissions) {
            if (action != null) {
                if (!mPermissions.contains(perm)) {
                    action.onResult(perm, Permissions.NOT_FOUND);
                } else if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                    action.onResult(perm, Permissions.DENIED);
                } else {
                    action.onResult(perm, Permissions.GRANTED);
                }
            }
        }
    }

    private synchronized void initializePermissionsMap() {
        Field[] fields = Manifest.permission.class.getFields();
        for (Field field : fields) {
            String name = null;
            try {
                name = (String)field.get("");
            } catch (IllegalAccessException e) {
                Log.d(TAG, "Could not access field", e);
            }
            mPermissions.add(name);
        }
    }

    public synchronized void notifyPermissionsChange(@NonNull
    String[] permissions, @NonNull
    int[] results) {
        int size = permissions.length;
        if (results.length < size) {
            size = results.length;
        }
        Iterator<WeakReference<PermissionsResultAction>> iterator = mPendingActions.iterator();
        while (iterator.hasNext()) {
            PermissionsResultAction action = iterator.next().get();
            for (int n = 0; n < size; n++) {
                if (action == null || action.onResult(permissions[n], results[n])) {
                    iterator.remove();
                    break;
                }
            }
        }
        for (int n = 0; n < size; n++) {
            mPendingRequests.remove(permissions[n]);
        }
    }

}
