package com.svtech.mandiri.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.svtech.mandiri.R;
import com.svtech.mandiri.callback.PermissionCallback;

import java.util.ArrayList;
import java.util.List;




public class PermissionUtil {

    private String TAG = getClass().getSimpleName();
    protected Context context;

    public PermissionUtil(Context context) {
        this.context = context;
    }

    public void checkPhoneStatePermission(final PermissionCallback callback) {
        Dexter.withActivity((Activity)context)
                .withPermission(Manifest.permission.READ_PHONE_STATE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showMessageOpenSetting(context.getString(R.string.reason_permission_read_phone_state));
                        }
                        callback.onFailed();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).check();
    }

    public void checkLocationPermission(final PermissionCallback callback) {
        Dexter.withActivity((Activity)context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showMessageOpenSetting(context.getString(R.string.reason_permission_location));
                        }
                        callback.onFailed();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).check();
    }

    public void checkCameraPermission(final PermissionCallback callback) {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Dexter.withActivity((Activity) context)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        boolean areAllPermissionsGranted = report.areAllPermissionsGranted();
                        boolean isAllPermissionPermanentlyDenied = isAllPermissionPermanentlyDenied(report.getDeniedPermissionResponses());

                        if (areAllPermissionsGranted) {
                            callback.onSuccess();
                        } else if (isAllPermissionPermanentlyDenied) {
                            showMessageOpenSetting(context.getString(R.string.reason_permission_camera));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        callback.onFailed();
                    }
                })
                .check();
    }

//    public void checkBluetoothPermission(final PermissionCallback callback) {
//        Dexter.withActivity((Activity)context)
//                .withPermissions(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        boolean areAllPermissionsGranted = report.areAllPermissionsGranted();
//                        boolean isAllPermissionPermanentlyDenied = isAllPermissionPermanentlyDenied(report.getDeniedPermissionResponses());
//
//                        if (areAllPermissionsGranted) {
//                            callback.onSuccess();
//                        } else if (isAllPermissionPermanentlyDenied) {
//                            showMessageOpenBluetooth(context.getString(R.string.reason_permission_bluetooth));
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).check();
//    }

    public void checkWriteExternalPermission(final PermissionCallback callback) {
        Dexter.withActivity((Activity)context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showMessageOpenSetting(context.getString(R.string.reason_permission_write_external));
                        }
                        callback.onFailed();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).check();
    }

    private boolean isAllPermissionPermanentlyDenied(List<PermissionDeniedResponse> deniedPermissionResponses) {
        boolean hasPermanentlyDeniedAnyPermission = false;
        for (PermissionDeniedResponse deniedResponse : deniedPermissionResponses) {
            hasPermanentlyDeniedAnyPermission = deniedResponse.isPermanentlyDenied();
        }
        return hasPermanentlyDeniedAnyPermission;
    }

    private void showMessageOpenSetting(String msg){
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("Activate Manual", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

//    private void showMessageOpenBluetooth(String msg){
//        new AlertDialog.Builder(context)
//                .setMessage(msg)
//                .setPositiveButton("Activate Manual", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        openBluetooth();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .show();
//    }

    private void openAppSettings() {
        Uri packageUri = Uri.fromParts("package", context.getPackageName(), null);
        Intent applicationDetailsSettingsIntent = new Intent();
        applicationDetailsSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        applicationDetailsSettingsIntent.setData(packageUri);
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(applicationDetailsSettingsIntent);
    }

//    private void openBluetooth() {
//        BluetoothUtil.openBluetooth(context);
//    }

}
