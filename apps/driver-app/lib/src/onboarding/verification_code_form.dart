import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class VerificationCodeForm extends StatefulWidget {
  final VoidCallback goNext;
  const VerificationCodeForm({Key? key, required this.goNext})
      : super(key: key);

  @override
  State<VerificationCodeForm> createState() => _VerificationCodeFormState();
}

class _VerificationCodeFormState extends State<VerificationCodeForm> {
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    final delegate = AppLocalizations.of(context)!;
    final controller = Provider.of<OnboardingController>(context);
    final isConnected = context.watch<bool>();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Text(
            delegate.verifyYourPhoneNumberByEnteringThePinSentViaSMS,
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: delegate.enterThePin,
          prefixIcon: Icons.lock,
          onChanged: (String value) => controller.changeVerificationCode(value),
          error: controller.verificationCode.isValidating
              ? controller.verificationCode.error
              : null,
        ),
        const SizedBox(height: 20),
        ContainedButton(
          onPressed: () async {
            if (controller.isVerificationCodeValid) {
              if (isConnected) {
                try {
                  _setLoading();
                  await controller.verifyVerificationCode();
                  widget.goNext();
                } on HttpException catch (errorMessage) {
                  CustomSnackBar.buildErrorSnackbar(
                    context,
                    errorMessage.toString(),
                  );
                }
                _setLoading();
              }
            } else {
              controller.changeValidationStatus('verificationCode');
            }
          },
          text: delegate.verify,
          isLoading: _isLoading,
        ),
      ],
    );
  }

  void _setLoading() {
    setState(() {
      _isLoading = !_isLoading;
    });
  }
}
