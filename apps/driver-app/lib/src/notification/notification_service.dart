import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';

class NotificationService {
  Future<void> initPlatformState(BuildContext context) async {
    OneSignal.shared.setLogLevel(OSLogLevel.verbose, OSLogLevel.none);

    var settings = {
      OSiOSSettings.autoPrompt: false,
      OSiOSSettings.promptBeforeOpeningPushUrl: true
    };

    OneSignal.shared.setNotificationWillShowInForegroundHandler(
        (OSNotificationReceivedEvent event) async {
      print("Notification received...");
      event.complete(event.notification);
      String? notificationType =
          event.notification.additionalData?['notificationType'];

      if (isNewAssignment(notificationType)) {
        await fetchNewAssignment(context);
      }
    });

    OneSignal.shared.setNotificationOpenedHandler(
        (OSNotificationOpenedResult result) async {
      print(
          "Opened notification: \n${result.notification.jsonRepresentation().replaceAll("\\n", "\n")}");

      String? notificationType =
          result.notification.additionalData?['notificationType'];

      if (isNewAssignment(notificationType)) {
        await fetchNewAssignment(context);
      }
    });

    OneSignal.shared
        .setInAppMessageClickedHandler((OSInAppMessageAction action) {
      print(
          "In App Message Clicked: \n${action.jsonRepresentation().replaceAll("\\n", "\n")}");
    });

    OneSignal.shared
        .setSubscriptionObserver((OSSubscriptionStateChanges changes) {
      print("SUBSCRIPTION STATE CHANGED: ${changes.jsonRepresentation()}");
    });

    OneSignal.shared.setPermissionObserver((OSPermissionStateChanges changes) {
      print("PERMISSION STATE CHANGED: ${changes.jsonRepresentation()}");
    });

    OneSignal.shared.setEmailSubscriptionObserver(
        (OSEmailSubscriptionStateChanges changes) {
      print("EMAIL SUBSCRIPTION STATE CHANGED ${changes.jsonRepresentation()}");
    });

    const ONE_SIGNAL_APP_ID = String.fromEnvironment('ONE_SIGNAL_APP_ID',
        defaultValue: '45b0b302-0ee9-432f-9c0c-bc74b81382b4');
    await OneSignal.shared.setAppId(ONE_SIGNAL_APP_ID);
  }

  bool isNewAssignment(String? notificationType) =>
      notificationType != null &&
      (notificationType == "NEW_ASSIGNMENT" ||
          notificationType == "CANCELLED_ASSIGNMENT");

  fetchNewAssignment(BuildContext context) async {
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context, listen: false);
    String driverId = deliveryController.driverId ?? "";

    if (driverId != "") {
      await Navigator.pushNamed(context, '/assignments');
      deliveryController.fetchStops();
    } else {
      CustomSnackBar.buildErrorSnackbar(
        context,
        "Cannot get new assignment. DriverId is null.",
      );
    }
  }
}
