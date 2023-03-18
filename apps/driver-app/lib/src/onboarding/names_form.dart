import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class NamesForm extends StatefulWidget {
  final VoidCallback changePage;
  const NamesForm({Key? key, required this.changePage}) : super(key: key);

  @override
  State<NamesForm> createState() => _NamesFormState();
}

class _NamesFormState extends State<NamesForm> {
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
          padding: const EdgeInsets.symmetric(vertical: 17),
          child: Text(
            delegate.finishSettingUpYourAccount,
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        FormInputField(
          label: delegate.firstName,
          prefixIcon: Icons.emoji_people,
          onChanged: (String value) => controller.changeFirstName(value),
          error: controller.firstName.isValidating
              ? controller.firstName.error
              : null,
        ),
        const SizedBox(height: 20),
        FormInputField(
          label: delegate.secondName,
          prefixIcon: Icons.emoji_people,
          onChanged: (String value) => controller.changeLastName(value),
          error: controller.lastName.isValidating
              ? controller.lastName.error
              : null,
        ),
        const SizedBox(height: 20),
        ContainedButton(
          onPressed: () async {
            if (controller.areNamesValid) {
              if (isConnected) {
                try {
                  _setLoading();
                  await controller.signUp();
                  CustomSnackBar.buildSuccessSnackBar(
                    context,
                    'Account created. Please sign in',
                  );
                  controller.resetFields();
                  widget.changePage();
                } on HttpException catch (errorMessage) {
                  CustomSnackBar.buildErrorSnackbar(
                    context,
                    errorMessage.toString(),
                  );
                }
                _setLoading();
              }
            } else {
              controller.changeValidationStatus('firstName');
              controller.changeValidationStatus('lastName');
            }
          },
          text: delegate.finish,
          isLoading: _isLoading,
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
