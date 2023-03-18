package com.example.vanoma_driver

import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
   private val CHANNEL = "pip_channel"

   @RequiresApi(Build.VERSION_CODES.O)
   override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
       super.configureFlutterEngine(flutterEngine)
       MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
           call, result ->
           result.success(call.method);
           if (call.method == "enterPIP") {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   activity?.enterPictureInPictureMode(
                       PictureInPictureParams.Builder()
                .setAspectRatio(Rational(2, 3))
                .build())
               }
           } else {
                   result.notImplemented()
               }
       }
   }
   override fun onFlutterUiDisplayed() {
        if (Build.VERSION.SDK_INT >= 100) {
            reportFullyDrawn();
        }
    }
}
