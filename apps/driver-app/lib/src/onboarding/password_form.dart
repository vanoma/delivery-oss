import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class PasswordForm extends StatefulWidget {
  final VoidCallback goNext;
  const PasswordForm({Key? key, required this.goNext}) : super(key: key);

  @override
  _PasswordFormState createState() => _PasswordFormState();
}

class _PasswordFormState extends State<PasswordForm> {
  bool _obscurePassword = true;

  @override
  Widget build(BuildContext context) {
    final delegate = AppLocalizations.of(context)!;
    final controller = Provider.of<OnboardingController>(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 17),
          child: Text(
            delegate.createAPassword,
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: delegate.password,
          prefixIcon: Icons.lock,
          suffixIcon: IconButton(
            icon: Icon(
              _obscurePassword ? Icons.visibility : Icons.visibility_off,
              size: 20,
            ),
            onPressed: () =>
                setState(() => _obscurePassword = !_obscurePassword),
          ),
          obscureText: _obscurePassword,
          onChanged: (String value) => controller.changePassword(value),
          error: controller.password.isValidating
              ? controller.password.error
              : null,
        ),
        const SizedBox(height: 20),
        FormInputField(
          label: delegate.confirmPassword,
          prefixIcon: Icons.lock,
          suffixIcon: IconButton(
            icon: Icon(
              _obscurePassword ? Icons.visibility : Icons.visibility_off,
              size: 20,
            ),
            onPressed: () =>
                setState(() => _obscurePassword = !_obscurePassword),
          ),
          obscureText: _obscurePassword,
          onChanged: (String value) => controller.changeConfirmPassword(value),
          error: controller.confirmPassword.isValidating
              ? controller.confirmPassword.error
              : null,
        ),
        const SizedBox(height: 20),
        ContainedButton(
          onPressed: () {
            if (controller.isPasswordValid) {
              widget.goNext();
            } else {
              controller.changeValidationStatus('password');
              controller.changeValidationStatus('confirmPassword');
            }
          },
          text: 'Create password',
        )
      ],
    );
  }
}
