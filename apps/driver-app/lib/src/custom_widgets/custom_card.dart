import 'package:flutter/material.dart';

class CustomCard extends StatelessWidget {
  final EdgeInsetsGeometry? margin;
  final EdgeInsetsGeometry? padding;
  final Widget? child;
  final bool showShadow;

  const CustomCard({
    Key? key,
    this.margin,
    this.padding,
    this.child,
    this.showShadow = true,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: margin,
      padding: padding,
      clipBehavior: Clip.hardEdge,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: Theme.of(context).colorScheme.secondaryVariant,
        ),
        boxShadow: showShadow
            ? [
                BoxShadow(
                  blurRadius: 8,
                  spreadRadius: 4,
                  color:
                      Theme.of(context).colorScheme.onSurface.withOpacity(.15),
                )
              ]
            : null,
      ),
      child: child,
    );
  }
}
