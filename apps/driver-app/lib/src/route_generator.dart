import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_view.dart';
import 'package:vanoma_driver/src/assignments/assignments.dart';
import 'package:vanoma_driver/src/custom_widgets/connectivity_widget.dart';
import 'package:vanoma_driver/src/custom_widgets/loading_screen.dart';
import 'package:vanoma_driver/src/home/home_view.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_view.dart';
import 'package:vanoma_driver/src/route_names.dart';
import 'package:vanoma_driver/src/settings/settings_controller.dart';
import 'package:vanoma_driver/src/utils/connection_util.dart';
import 'package:tuple/tuple.dart';

class RouteGenerator {
  static Route<dynamic> generateRoute(
    RouteSettings routeSettings,
    SettingsController settingsController,
    ConnectionUtil connectionStatus,
  ) {
    return MaterialPageRoute<void>(
      settings: routeSettings,
      builder: (BuildContext context) {
        switch (routeSettings.name) {
          case account:
            return ConnectivityWidget(
              connectionStatus: connectionStatus,
              builder: (context) => const AccountView(),
            );
          case '/assignments':
            return ConnectivityWidget(
              connectionStatus: connectionStatus,
              builder: (context) => const Assignments(),
            );
          case initialRoute:
          default:
            return ConnectivityWidget(
              builder: (context) =>
                  Selector<OnboardingController, Tuple2<bool, String?>>(
                selector: (_, onboardingController) => Tuple2(
                    onboardingController.isAuthenticated,
                    onboardingController.userId),
                builder: (context, onboardingController, _) =>
                    onboardingController.item1
                        ? HomeView(settingsController: settingsController)
                        : FutureBuilder(
                            future: Provider.of<OnboardingController>(context,
                                    listen: false)
                                .tryToLoginAutomatically(),
                            builder: (context, authSnapshot) =>
                                authSnapshot.connectionState ==
                                        ConnectionState.waiting
                                    ? LoadingScreen()
                                    : const OnboardingView(),
                          ),
              ),
              connectionStatus: connectionStatus,
            );
        }
      },
    );
  }
}
