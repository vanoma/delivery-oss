import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/app_theme.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/delivery_service.dart';
import 'package:vanoma_driver/src/localization/rw_intl.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';
import 'package:vanoma_driver/src/route_generator.dart';
import 'package:vanoma_driver/src/utils/connection_util.dart';

import 'settings/settings_controller.dart';

/// The Widget that configures your application.
class MyApp extends StatefulWidget {
  const MyApp({
    Key? key,
    required this.settingsController,
    required this.onboardingController,
    required this.accountController,
    required this.connectionStatus,
    required this.deliveryController,
  }) : super(key: key);

  final SettingsController settingsController;
  final OnboardingController onboardingController;
  final AccountController accountController;
  final ConnectionUtil connectionStatus;
  final DeliveryController deliveryController;

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (context) => widget.onboardingController,
        ),
        ChangeNotifierProvider.value(
          value: widget.deliveryController,
        ),
        ChangeNotifierProvider(
          create: (context) => widget.accountController,
        ),
      ],
      child: AnimatedBuilder(
        animation: widget.settingsController,
        builder: (BuildContext context, Widget? child) {
          return GestureDetector(
            onTap: () {
              FocusScope.of(context).requestFocus(FocusNode());
            },
            child: MaterialApp(
              restorationScopeId: 'app',
              debugShowCheckedModeBanner: false,
              localizationsDelegates: const [
                RwMaterialLocalizations.delegate,
                ...AppLocalizations.localizationsDelegates,
              ],
              supportedLocales: AppLocalizations.supportedLocales,
              locale: widget.settingsController.language,
              onGenerateTitle: (BuildContext context) =>
                  AppLocalizations.of(context)!.appTitle,
              theme: AppTheme.lightTheme,
              darkTheme: AppTheme.darkTheme,
              themeMode: widget.settingsController.themeMode,
              onGenerateRoute: (routeSettings) => RouteGenerator.generateRoute(
                routeSettings,
                widget.settingsController,
                widget.connectionStatus,
              ),
            ),
          );
        },
      ),
    );
  }
}
