import 'package:flutter/material.dart';

class CustomIconButton extends StatelessWidget {
  final Icon icon;
  final VoidCallback? onTap;
  final Color color;

  const CustomIconButton({
    Key? key,
    required this.icon,
    this.onTap,
    required this.color,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Material(
      color: color.withOpacity(.15),
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.all(8),
          child: icon,
        ),
      ),
    );
  }
}
