import 'package:flutter/material.dart';

class TopButton extends StatelessWidget {
  final Widget child;
  final Color? color;
  final double? width;
  final VoidCallback? onTap;

  const TopButton({
    Key? key,
    required this.child,
    this.color,
    this.width,
    this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 40,
      width: width,
      decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(10),
          boxShadow: [
            BoxShadow(
              blurRadius: 8,
              spreadRadius: 4,
              color: Theme.of(context).colorScheme.onSurface.withOpacity(.15),
            )
          ]),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          overlayColor: MaterialStateProperty.all<Color>(
            Theme.of(context).colorScheme.onSurface.withOpacity(0.4),
          ),
          borderRadius: BorderRadius.circular(10),
          onTap: onTap,
          child: child,
        ),
      ),
    );
  }
}
