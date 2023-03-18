import 'dart:async';

import 'package:android_intent_plus/android_intent.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/delivery_view.dart';
import 'package:vanoma_driver/src/geolocation/background_geolocation_service.dart';
import 'package:vanoma_driver/src/home/app_drawer.dart';
import 'package:vanoma_driver/src/notification/notification_service.dart';
import 'package:vanoma_driver/src/settings/settings_controller.dart';

class HomeView extends StatefulWidget {
  const HomeView({Key? key, required this.settingsController})
      : super(key: key);

  final SettingsController settingsController;

  @override
  _HomeViewState createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> with WidgetsBindingObserver {
  @override
  void initState() {
    WidgetsBinding.instance?.addObserver(this);
    WidgetsBinding.instance?.addPostFrameCallback(
      (_) => NotificationService().initPlatformState(context),
    );
    BackgroundGeolocationService.initializePlatformState(context);
    Future.delayed(const Duration(milliseconds: 10),
        Provider.of<DeliveryController>(context, listen: false).fetchStops);
    super.initState();
  }

  Future<void> _openGoogleMapsNavigation(double lat, double lng) {
    final AndroidIntent intent = AndroidIntent(
        action: 'action_view',
        data: Uri.encodeFull('google.navigation:q=$lat,$lng&mode=d'),
        package: 'com.google.android.apps.maps');
    // _enterPIPMode();
    return intent.launch();
  }

  @override
  Widget build(BuildContext context) {
    final Size size = MediaQuery.of(context).size;

    return Scaffold(
      body: Stack(
        children: <Widget>[
          Positioned(
            top: 32,
            width: size.width,
            child: Builder(
              builder: (cxt) => DeliveryView(
                openGoogleMapsNavigation: _openGoogleMapsNavigation,
                homeContext: cxt,
              ),
            ),
          ),
        ],
      ),
      drawer: AppDrawer(
        settingsController: widget.settingsController,
      ),
    );
  }
}
