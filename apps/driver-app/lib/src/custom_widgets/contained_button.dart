import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

class ContainedButton extends StatelessWidget {
  const ContainedButton({
    Key? key,
    this.isLoading = false,
    required this.onPressed,
    required this.text,
  }) : super(key: key);
  final bool isLoading;
  final void Function() onPressed;
  final String text;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: !isLoading
          ? () {
              FocusScope.of(context).requestFocus(FocusNode());
              onPressed();
            }
          : null,
      child: !isLoading
          ? Text(text, style: const TextStyle(fontSize: 16))
          : SpinKitThreeBounce(
              size: 32,
              color: Theme.of(context).colorScheme.primary,
            ),
    );
  }
}
