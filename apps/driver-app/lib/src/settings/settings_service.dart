import 'dart:io';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

const String themeKey = 'theme';
const String languageKey = 'language';

/// A service that stores and retrieves user settings.

class SettingsService {
  Future<ThemeMode> themeMode() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final String? themeValue = prefs.getString(themeKey);

    if (themeValue == null) return ThemeMode.system;
    ThemeMode theme = ThemeMode.values.firstWhere(
      (e) => e.toString() == themeValue,
      orElse: () => ThemeMode.system,
    );
    return theme;
  }

  Future<Locale> language() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final String? languageValue = prefs.getString(languageKey);

    if (languageValue == null) return defaultLocale();
    Locale language = AppLocalizations.supportedLocales.firstWhere(
      (e) => e.toString() == languageValue,
      orElse: defaultLocale,
    );
    return language;
  }

  Locale defaultLocale() {
    final String systemLocale = Platform.localeName;
    Locale defaultLocale = AppLocalizations.supportedLocales.firstWhere(
      (e) => e.toString() == systemLocale.split('_')[0],
      orElse: () => const Locale('en'),
    );
    return defaultLocale;
  }

  Future<void> updateThemeMode(ThemeMode theme) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString(themeKey, theme.toString());
  }

  Future<void> updateLanguage(Locale language) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString(languageKey, language.toString());
  }
}
