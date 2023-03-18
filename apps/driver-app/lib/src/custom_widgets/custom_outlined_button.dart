import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

class CustomOutlinedButton extends StatelessWidget {
  const CustomOutlinedButton({
    Key? key,
    this.isLoading = false,
    required this.onPressed,
    required this.text,
  }) : super(key: key);
  final bool isLoading;
  final void Function()? onPressed;
  final String text;

  @override
  Widget build(BuildContext context) {
    return OutlinedButton(
      onPressed: !isLoading ? onPressed : null,
      child: !isLoading
          ? Text(text, style: const TextStyle(fontSize: 16))
          : SpinKitThreeBounce(
              size: 16,
              color: Theme.of(context).colorScheme.primary,
            ),
    );
  }
}
