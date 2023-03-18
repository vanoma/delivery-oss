import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_outlined_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';

class AccountView extends StatefulWidget {
  const AccountView({Key? key}) : super(key: key);

  @override
  State<AccountView> createState() => _AccountViewState();
}

class _AccountViewState extends State<AccountView> {
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    final delegate = AppLocalizations.of(context)!;
    final onboardingController =
        Provider.of<OnboardingController>(context, listen: false);
    final isConnected = context.watch<bool>();
    final AccountController accountController =
        Provider.of<AccountController>(context);

    return Scaffold(
      appBar: AppBar(
        title: Text(delegate.account),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text(
              delegate.done,
            ),
            style: ButtonStyle(
              foregroundColor: MaterialStateProperty.all(Colors.white),
            ),
          ),
        ],
      ),
      body: ListView(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: <Widget>[
                SizedBox(
                  width: MediaQuery.of(context).size.width,
                  child: Stack(
                    alignment: Alignment.topCenter,
                    children: <Widget>[
                      const Padding(
                        padding: EdgeInsets.only(top: 42),
                        child: CircleAvatar(
                          backgroundImage:
                              AssetImage('assets/images/avatar.png'),
                          radius: 64,
                          backgroundColor: Colors.grey,
                        ),
                      ),
                      Positioned(
                        top: 116,
                        child: Padding(
                          padding: const EdgeInsets.only(left: 110),
                          child: Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(30),
                              color: Theme.of(context)
                                  .primaryColor
                                  .withOpacity(.1),
                            ),
                            height: 54,
                            width: 54,
                            child: const Icon(Icons.camera_alt),
                          ),
                        ),
                      )
                    ],
                  ),
                ),
                const SizedBox(height: 8),
                Center(child: Text(delegate.changeProfilePicture)),
                const SizedBox(height: 24),
                FormInputField(
                  label: accountController.driver!.firstName,
                  prefixIcon: Icons.account_circle,
                  onChanged: (value) {},
                  disabled: true,
                ),
                const SizedBox(height: 20),
                FormInputField(
                  label: accountController.driver!.lastName,
                  prefixIcon: Icons.account_circle,
                  onChanged: (value) {},
                  disabled: true,
                ),
                const SizedBox(height: 20),
                FormInputField(
                  label: accountController.driver!.phoneNumber,
                  prefixIcon: Icons.account_circle,
                  onChanged: (value) {},
                  disabled: true,
                ),
                const SizedBox(height: 20),
              ],
            ),
          ),
          const Divider(),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 32),
            child: CustomOutlinedButton(
              onPressed: () async {
                if (isConnected) {
                  try {
                    _setLoading();
                    await onboardingController.signOut();
                    OneSignal.shared.removeExternalUserId();
                    Navigator.of(context).pop();
                  } on HttpException catch (errorMessage) {
                    CustomSnackBar.buildErrorSnackbar(
                      context,
                      errorMessage.toString(),
                    );
                  }
                  _setLoading();
                }
              },
              text: delegate.signOut,
              isLoading: _isLoading,
            ),
          )
        ],
      ),
    );
  }

  void _setLoading() {
    setState(() {
      _isLoading = !_isLoading;
    });
  }
}
