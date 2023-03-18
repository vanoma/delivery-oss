import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_svg/svg.dart';
import 'package:flutter_background_geolocation/flutter_background_geolocation.dart'
    as bg;
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/account/account_service.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/delivery_service.dart';
import 'package:vanoma_driver/src/geolocation/background_geolocation_service.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_service.dart';
import 'package:vanoma_driver/src/utils/connection_util.dart';

import 'src/app.dart';
import 'src/settings/settings_controller.dart';
import 'src/settings/settings_service.dart';

void backgroundGeolocationHeadlessTask(bg.HeadlessEvent headlessEvent) async {
  if (headlessEvent.name == bg.Event.HEARTBEAT) {
    bg.BackgroundGeolocation.getCurrentPosition(maximumAge: 5000);
  }
  if (headlessEvent.name == bg.Event.PROVIDERCHANGE) {
    bg.BackgroundGeolocation.setConfig(bg.Config(extras: {
      'isLocationServiceEnabled': headlessEvent.event.enabled,
      'locationAccessStatus': locationPermissions[headlessEvent.event.status],
      'isGpsEnabled': headlessEvent.event.gps,
    }));
  }
  // if (headlessEvent.name == bg.Event.SCHEDULE) {
  //   if (!headlessEvent.event.enabled) {
  //     BackgroundGeolocationService.setSchedule();
  //   }
  // }
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  Future.wait([
    ...[
      'assets/images/logo.svg',
      'assets/images/vanoma.svg',
      'assets/images/city_illustration.svg',
    ]
        .map((asset) => precachePicture(
              ExactAssetPicture(SvgPicture.svgStringDecoderBuilder, asset),
              null,
            ))
        .toList(),
  ]);
  // I HAVE TO FIX THIS
  // precachePicture(Image.asset('assets/images/map_background.png'), null);

  ConnectionUtil connectionStatus = ConnectionUtil.getInstance();
  connectionStatus.initialize();

  final settingsController = SettingsController(SettingsService());
  final accountController = AccountController(AccountService());
  final OnboardingController onboardingController =
      OnboardingController(OnboardingService(), accountController);
  final deliveryController =
      DeliveryController(DeliveryService(), accountController);
  await settingsController.loadSettings();
  await onboardingController.tryToLoginAutomatically();
  if (onboardingController.driverId != null) {
    try {
      await accountController.getProfile(onboardingController.driverId!);
    } catch (e) {
      await onboardingController.signOut();
    }
  }

  runApp(MyApp(
    settingsController: settingsController,
    onboardingController: onboardingController,
    accountController: accountController,
    connectionStatus: connectionStatus,
    deliveryController: deliveryController,
  ));

  bg.BackgroundGeolocation.registerHeadlessTask(
    backgroundGeolocationHeadlessTask,
  );
}
