import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class PhoneNumbersForm extends StatefulWidget {
  final VoidCallback goNext;
  const PhoneNumbersForm({
    Key? key,
    required this.goNext,
  }) : super(key: key);

  @override
  State<PhoneNumbersForm> createState() => _PhoneNumbersFormState();
}

class _PhoneNumbersFormState extends State<PhoneNumbersForm> {
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
          padding: const EdgeInsets.symmetric(vertical: 15.5),
          child: Text(
            delegate.enterYourPhoneNumber,
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: delegate.phoneNumber,
          prefixIcon: Icons.phone,
          onChanged: (String value) => controller.changePhoneNumber(value),
          error: controller.phoneNumber.isValidating
              ? controller.phoneNumber.error
              : null,
        ),
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Text(
            delegate.enterAnotherPhoneNumber,
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: delegate.secondPhoneNumber,
          prefixIcon: Icons.phone,
          onChanged: (String value) =>
              controller.changeSecondPhoneNumber(value),
          error: controller.secondPhoneNumber.isValidating
              ? controller.secondPhoneNumber.error
              : null,
        ),
        const SizedBox(height: 20),
        ContainedButton(
          onPressed: () async {
            if (controller.arePhoneNumbersValid) {
              if (isConnected) {
                try {
                  _setLoading();
                  await controller.verifyPhoneNumber();
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
              controller.changeValidationStatus('phoneNumber');
              controller.changeValidationStatus('secondPhoneNumber');
            }
          },
          text: delegate.signUp,
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
