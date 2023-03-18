import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class SignInForm extends StatefulWidget {
  const SignInForm({Key? key, required this.changePage, required this.goNext})
      : super(key: key);
  final VoidCallback changePage;
  final VoidCallback goNext;

  @override
  _SignInFormState createState() => _SignInFormState();
}

class _SignInFormState extends State<SignInForm> {
  bool _obscurePassword = true;
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    final controller = Provider.of<OnboardingController>(context);
    final isConnected = context.watch<bool>();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 14.5),
          child: Text(
            'Sign in',
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: 'Phone number',
          prefixIcon: Icons.phone,
          onChanged: (String value) => controller.changePhoneNumber(value),
          error: controller.phoneNumber.isValidating
              ? controller.phoneNumber.error
              : null,
        ),
        const SizedBox(height: 20),
        FormInputField(
          label: 'Password',
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
          onChanged: (String value) => controller.changeSignInPassword(value),
          error: controller.password.isValidating
              ? controller.password.error
              : null,
        ),
        const SizedBox(height: 20),
        ContainedButton(
          text: 'Sign in',
          onPressed: () async {
            if (controller.isSignInValid) {
              if (isConnected) {
                try {
                  _setLoading();
                  await controller.signIn();
                } on HttpException catch (errorMessage) {
                  CustomSnackBar.buildErrorSnackbar(
                    context,
                    errorMessage.toString(),
                  );
                }
                _setLoading();
              }
            } else {
              controller.changeValidationStatus('phoneNumber');
              controller.changeValidationStatus('password');
            }
          },
          isLoading: _isLoading,
        ),
        const SizedBox(height: 12),
        Padding(
          padding: const EdgeInsets.only(left: 16),
          child: Row(
            children: <Widget>[
              const Text(
                'Don\'t have an account? ',
              ),
              InkWell(
                onTap: () {
                  controller.resetFields();
                  widget.changePage();
                },
                child: Text('Sign up',
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.primary,
                      fontWeight: FontWeight.bold,
                    )),
              ),
            ],
          ),
        )
      ],
    );
  }

  void _setLoading() {
    setState(() {
      _isLoading = !_isLoading;
    });
  }
}
