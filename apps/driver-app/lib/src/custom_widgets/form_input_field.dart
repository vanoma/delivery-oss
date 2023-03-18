import 'package:flutter/material.dart';

class FormInputField extends StatelessWidget {
  final String label;
  final IconData? prefixIcon;
  final Widget? suffixIcon;
  final String? hint;
  final String? initialValue;
  final String? error;
  final int? minLines;
  final int maxLines;
  final int? maxLength;
  final void Function(String) onChanged;
  final bool obscureText;
  final bool disabled;

  const FormInputField(
      {Key? key,
      required this.label,
      this.prefixIcon,
      this.suffixIcon,
      this.hint,
      this.initialValue,
      this.error,
      this.minLines = 1,
      this.maxLines = 1,
      this.maxLength,
      this.obscureText = false,
      this.disabled = false,
      required this.onChanged})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      minLines: minLines,
      maxLines: maxLines,
      maxLength: maxLength,
      initialValue: initialValue,
      obscureText: obscureText,
      decoration: InputDecoration(
        labelText: label,
        hintText: hint,
        errorText: error,
        prefixIcon: prefixIcon != null ? Icon(prefixIcon) : null,
        suffixIcon: suffixIcon,
      ),
      onChanged: onChanged,
      enabled: !disabled,
    );
  }
}
