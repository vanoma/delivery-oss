import 'package:flutter/material.dart';

class CustomSnackBar {
  CustomSnackBar._();

  static buildErrorSnackbar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          message,
          style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.onError),
          textAlign: TextAlign.center,
        ),
        backgroundColor: Theme.of(context).colorScheme.error,
        duration: const Duration(seconds: 10),
      ),
    );
  }

  static buildSuccessSnackBar(BuildContext context, String message,
      {Duration duration = const Duration(seconds: 5)}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          message,
          style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.onPrimary),
          textAlign: TextAlign.center,
        ),
        backgroundColor: Colors.green,
        duration: duration,
      ),
    );
  }
}
