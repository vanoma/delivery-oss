import 'package:flutter/material.dart';

class AppTheme {
  AppTheme._();

  static const Map<int, Color> _swatch = {
    50: Color(0xFFfde4ea),
    100: Color(0xFFf9bacb),
    200: Color(0xFFf58ea9),
    300: Color(0xFFf06087),
    400: Color(0xFFeb3f6d),
    500: Color(0xFFe61f55),
    600: Color(0xFFd61b53),
    700: Color(0xFFc1164f),
    800: Color(0xFFac104c),
    900: Color(0xFF880746),
  };

  static final primarySwatch = MaterialColor(_swatch[600]!.value, _swatch);

  static final elevatedButtonTheme = ElevatedButtonThemeData(
    style: ButtonStyle(
      shape: MaterialStateProperty.resolveWith<OutlinedBorder>(
        (_) => RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(30),
        ),
      ),
      minimumSize:
          MaterialStateProperty.all<Size>(const Size(double.maxFinite, 52)),
    ),
  );

  static final inputDecorationTheme = InputDecorationTheme(
    filled: true,
    fillColor: _swatch[600]!.withOpacity(.15),
    floatingLabelBehavior: FloatingLabelBehavior.never,
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(30),
      borderSide: BorderSide(color: _swatch[100]!),
    ),
    disabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(30),
      borderSide: BorderSide(color: _swatch[100]!),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(30),
      borderSide: BorderSide(color: _swatch[200]!),
    ),
    errorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(30),
      borderSide: BorderSide(color: _swatch[200]!),
    ),
    focusedErrorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(30),
      borderSide: BorderSide(color: _swatch[200]!),
    ),
    contentPadding: const EdgeInsets.fromLTRB(24, 14, 8, 14),
  );

  static final outlinedButtonTheme = OutlinedButtonThemeData(
    style: ButtonStyle(
      shape: MaterialStateProperty.resolveWith<OutlinedBorder>(
        (_) => RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(30),
        ),
      ),
      side: MaterialStateProperty.resolveWith<BorderSide>(
        (states) => BorderSide(
          color: AppTheme.primarySwatch[100]!,
        ),
      ),
    ),
  );

  static final textButtonTheme = TextButtonThemeData(
    style: ButtonStyle(
      shape: MaterialStateProperty.resolveWith<OutlinedBorder>(
        (_) => RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
    ),
  );

  static const textTheme = TextTheme(
    headline4: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
    headline5: TextStyle(
      fontSize: 20,
      fontWeight: FontWeight.bold,
      letterSpacing: 0.18,
    ),
    headline6: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w700,
      letterSpacing: 0.15,
    ),
    subtitle1: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w600,
      letterSpacing: 0.15,
    ),
    subtitle2: TextStyle(
      fontSize: 14,
      fontWeight: FontWeight.w500,
      letterSpacing: 0.1,
    ),
    bodyText1: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w400,
      letterSpacing: 0.5,
    ),
    bodyText2: TextStyle(
      fontSize: 14,
      fontWeight: FontWeight.w500,
      letterSpacing: 0.25,
    ),
    caption: TextStyle(
      fontSize: 12,
      fontWeight: FontWeight.normal,
      letterSpacing: 0.4,
    ),
    overline: TextStyle(
      fontSize: 10,
      fontWeight: FontWeight.normal,
      letterSpacing: 1.5,
    ),
    button: TextStyle(
      fontSize: 14,
      fontWeight: FontWeight.bold,
      letterSpacing: 1.25,
    ),
  );

  static final lightTheme = ThemeData(
    primarySwatch: AppTheme.primarySwatch,
    fontFamily: 'Poppins',
    textTheme: AppTheme.textTheme,
    elevatedButtonTheme: AppTheme.elevatedButtonTheme,
    inputDecorationTheme: AppTheme.inputDecorationTheme,
    colorScheme: const ColorScheme.light().copyWith(
      primary: AppTheme.primarySwatch[600],
      primaryVariant: AppTheme.primarySwatch[900],
      secondary: AppTheme.primarySwatch[50],
      secondaryVariant: AppTheme.primarySwatch[100],
    ),
    dividerTheme: const DividerThemeData(
      thickness: 1,
      space: 1,
    ),
    iconTheme: IconThemeData(
      color: AppTheme.primarySwatch[600],
    ),
    outlinedButtonTheme: AppTheme.outlinedButtonTheme,
    textButtonTheme: textButtonTheme,
  );

  static final darkTheme = ThemeData(
    fontFamily: 'Poppins',
    textTheme: AppTheme.textTheme,
    colorScheme: const ColorScheme.dark().copyWith(
      primary: AppTheme.primarySwatch[300],
      secondary: AppTheme.primarySwatch[50],
      secondaryVariant: AppTheme.primarySwatch[100],
      error: const ColorScheme.light().error,
      onError: const ColorScheme.light().onError,
    ),
    elevatedButtonTheme: AppTheme.elevatedButtonTheme,
    inputDecorationTheme: AppTheme.inputDecorationTheme,
    outlinedButtonTheme: AppTheme.outlinedButtonTheme,
    dividerTheme: const DividerThemeData(
      thickness: 1,
      space: 1,
    ),
    textButtonTheme: textButtonTheme,
  );
}
