import 'package:flutter/material.dart';
import 'package:flutter_background_geolocation/flutter_background_geolocation.dart'
    as bg;
import 'package:geolocator/geolocator.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';
import 'package:vanoma_driver/src/services/http_service.dart';

const Map<int, String> locationPermissions = {
  3: 'ALLOWED_ALWAYS',
  4: 'ALLOWED_WHEN_IN_USE',
  2: 'DENIED',
};

class BackgroundGeolocationService {
  static void initializePlatformState(BuildContext context) async {
    bg.BackgroundGeolocation.onHeartbeat((_) {
      bg.BackgroundGeolocation.getCurrentPosition(maximumAge: 5000);
    });
    bg.BackgroundGeolocation.onLocation((location) {
      checkDepartureConfirmation(context, location.coords);
    });
    bg.BackgroundGeolocation.onProviderChange((event) async {
      bg.BackgroundGeolocation.setConfig(bg.Config(extras: {
        'isLocationServiceEnabled': event.enabled,
        'locationAccessStatus': locationPermissions[event.status],
        'isGpsEnabled': event.gps,
      }));
    });
    // bg.BackgroundGeolocation.onSchedule((event) async {
    //   if (!event.enabled) {
    //     setSchedule();
    //   }
    // });
    final String driverId =
        Provider.of<OnboardingController>(context, listen: false).driverId!;
    final String userId =
        Provider.of<OnboardingController>(context, listen: false).userId!;
    final String? _cookies = await HttpService.getCookies();
    final String? _accessToken = await HttpService().accessToken;
    final Map<String, dynamic> _headers = HttpService().headersNoAuthHeader;

    String? refreshToken;
    try {
      refreshToken = _cookies!
          .split(';')
          .firstWhere((element) => element.contains('EnvRefreshToken'))
          .split('=')[1];
    } catch (error) {
      print('ERROR: Couldn\'t find refresh token in a cookie. $error');
    }

    _headers['cookie'] = _cookies;

    bg.ProviderChangeEvent _providerChange =
        await bg.BackgroundGeolocation.providerState;

    bg.BackgroundGeolocation.ready(
      bg.Config(
        desiredAccuracy: bg.Config.DESIRED_ACCURACY_HIGH,
        distanceFilter: 10,
        stopOnTerminate: false,
        startOnBoot: true,
        enableHeadless: true,
        maxDaysToPersist: 1,
        maxRecordsToPersist: 1,
        logLevel: bg.Config.LOG_LEVEL_VERBOSE,
        heartbeatInterval: 60,
        httpRootProperty: '.',
        url: '${HttpService.baseUrl}/drivers2/$driverId/locations',
        locationTemplate:
            '{"latitude":"<%= latitude %>","longitude":"<%= longitude %>","batteryLevel":"<%= battery.level %>","isMockLocation":"<%= mock %>"}',
        headers: _headers,
        authorization: bg.Authorization(
          accessToken: _accessToken,
          refreshToken: refreshToken,
          refreshUrl: '${HttpService.baseUrl}/refresh-token',
          refreshPayload: {
            "refreshToken": '{refreshToken}',
          },
        ),
        stopTimeout: 1,
        extras: {
          'isLocationServiceEnabled': _providerChange.enabled,
          'locationAccessStatus': locationPermissions[_providerChange.status],
          'isGpsEnabled': _providerChange.gps,
        },
      ),
    ).then((bg.State state) {
      if (!state.enabled) {
        bg.BackgroundGeolocation.start();
      }
    }).catchError((_) {});
  }

  static Future<void> checkDepartureConfirmation(
      BuildContext context, bg.Coords coords) async {
    final GeolocatorPlatform geolocatorPlatform = GeolocatorPlatform.instance;
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context, listen: false);
    if (deliveryController.stops.isEmpty ||
        deliveryController.currentStopIndex == 0 ||
        deliveryController
            .stops[deliveryController.currentStopIndex].hasDeparted) {
      return;
    }
    Stop previousStop =
        deliveryController.stops[deliveryController.currentStopIndex - 1];
    double distance = geolocatorPlatform.distanceBetween(
      coords.latitude,
      coords.longitude,
      previousStop.address.latitude,
      previousStop.address.longitude,
    );

    if (distance > 100) {
      try {
        await deliveryController.confirmDeparture(
          deliveryController.stops[deliveryController.currentStopIndex].stopId,
        );
      } catch (e) {
        // ignore: avoid_print
        print(e);
      }
    }
  }

  // static Future<void> setSchedule() async {
  //   DateTime now = DateTime.now();
  //   bg.State state = await bg.BackgroundGeolocation.state;
  //   if (state.enabled) {
  //     bg.BackgroundGeolocation.stop();
  //   }
  //   bg.BackgroundGeolocation.setConfig(
  //     bg.Config(
  //       schedule: [
  //         '1-7 ${now.hour}:${now.minute + 1}-${now.hour}:${now.minute + 2}',
  //       ],
  //     ),
  //   );
  //   bg.BackgroundGeolocation.startSchedule();
  // }
}
