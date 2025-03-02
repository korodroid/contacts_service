package flutter.plugins.contactsservice.contactsservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

/** ContactsServicePlugin */
public class ContactsServicePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, ActivityPluginBinding.ActivityResultListener {
    private MethodChannel channel;
    private Context applicationContext;
    private Activity activity;
    private ContactsServiceDelegate delegate;
    private ActivityPluginBinding activityPluginBinding;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        applicationContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "github.com/clovisnicolas/flutter_contacts");
        channel.setMethodCallHandler(this);
        delegate = new ContactsServiceDelegate(applicationContext);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
        applicationContext = null;
        delegate = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addActivityResultListener(this);
        delegate.setActivity(activity);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
        if (activityPluginBinding != null) {
            activityPluginBinding.removeActivityResultListener(this);
            activityPluginBinding = null;
        }
        delegate.setActivity(null);
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addActivityResultListener(this);
        delegate.setActivity(activity);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
        if (activityPluginBinding != null) {
            activityPluginBinding.removeActivityResultListener(this);
            activityPluginBinding = null;
        }
        delegate.setActivity(null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        delegate.onMethodCall(call, result);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (delegate == null) {
            return false;
        }
        
        if (requestCode == ContactsServiceDelegate.REQUEST_OPEN_CONTACT_PICKER) {
            if (resultCode == Activity.RESULT_CANCELED) {
                delegate.finishWithResult(ContactsServiceDelegate.FORM_OPERATION_CANCELED);
                return true;
            }

            if (intent == null || intent.getData() == null) {
                delegate.finishWithResult(ContactsServiceDelegate.FORM_OPERATION_CANCELED);
                return true;
            }

            Uri contactUri = intent.getData();
            Cursor cursor = applicationContext.getContentResolver().query(contactUri, null, null, null, null);
            
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String id = contactUri.getLastPathSegment();
                    delegate.getContacts("openDeviceContactPicker", id, false, false, false, false, delegate.getResult());
                } else {
                    delegate.finishWithResult(ContactsServiceDelegate.FORM_OPERATION_CANCELED);
                }
                cursor.close();
            } else {
                delegate.finishWithResult(ContactsServiceDelegate.FORM_OPERATION_CANCELED);
            }
            return true;
        }

        return false;
    }
}
