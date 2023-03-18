import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/notification/notification_service.dart';
import 'package:vanoma_driver/src/onboarding/names_form.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_controller.dart';
import 'package:vanoma_driver/src/onboarding/password_form.dart';
import 'package:vanoma_driver/src/onboarding/verification_code_form.dart';
import 'package:vanoma_driver/src/onboarding/phone_numbers_form.dart';
import 'package:vanoma_driver/src/onboarding/sign_in_form.dart';

class OnboardingView extends StatefulWidget {
  const OnboardingView({Key? key}) : super(key: key);

  @override
  _OnboardingViewState createState() => _OnboardingViewState();
}

class _OnboardingViewState extends State<OnboardingView> {
  @override
  void initState() {
    WidgetsBinding.instance?.addPostFrameCallback(
      (_) => NotificationService().initPlatformState(context),
    );
    super.initState();
  }

  int index = 0;
  bool isSinUp = false;

  @override
  Widget build(BuildContext context) {
    Size size = MediaQuery.of(context).size;
    double space = (size.height - 350);
    final controller = Provider.of<OnboardingController>(context);

    return Scaffold(
      body: Stack(
        alignment: Alignment.center,
        children: <Widget>[
          Container(
            decoration: const BoxDecoration(
              image: DecorationImage(
                image: AssetImage('assets/images/map_background.png'),
                fit: BoxFit.cover,
              ),
            ),
          ),
          Positioned(
            top: 48,
            child: Column(
              children: <Widget>[
                SvgPicture.asset(
                  'assets/images/logo.svg',
                  height: space * .4,
                  color: Theme.of(context).colorScheme.surface,
                ),
                const SizedBox(height: 24),
                SvgPicture.asset(
                  'assets/images/vanoma.svg',
                  width: space * .6,
                  color: Theme.of(context).colorScheme.surface,
                ),
              ],
            ),
          ),
          Positioned(
            bottom: 0,
            child: Column(
              children: <Widget>[
                SvgPicture.asset(
                  'assets/images/city_illustration.svg',
                  color: Theme.of(context).colorScheme.surface,
                  width: size.width,
                ),
                Container(
                  margin: const EdgeInsets.only(top: 0),
                  color: Theme.of(context).colorScheme.surface,
                  width: size.width,
                  padding: const EdgeInsets.fromLTRB(32, 0, 32, 32),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: <Widget>[
                      isSinUp
                          ? Column(
                              children: <Widget>[
                                getSignUpForms()[index],
                                const SizedBox(height: 12),
                                Padding(
                                  padding: const EdgeInsets.only(left: 16),
                                  child: Row(
                                    children: <Widget>[
                                      const Text(
                                        'Have an account? ',
                                      ),
                                      InkWell(
                                        onTap: () {
                                          controller.resetFields();
                                          setState(() {
                                            isSinUp = false;
                                            index = 0;
                                          });
                                        },
                                        child: Text(
                                          'Sign in',
                                          style: TextStyle(
                                            color: Theme.of(context)
                                                .colorScheme
                                                .primary,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                )
                              ],
                            )
                          : SignInForm(
                              changePage: () => setState(() {
                                isSinUp = true;
                              }),
                              goNext: _goNext,
                            ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> getSignUpForms() {
    List<Widget> widgets = [
      PhoneNumbersForm(
        goNext: _goNext,
      ),
      VerificationCodeForm(goNext: _goNext),
      PasswordForm(
        goNext: _goNext,
      ),
      NamesForm(
        changePage: () {
          setState(() {
            isSinUp = false;
          });
        },
      ),
    ];
    return widgets;
  }

  void _goNext() {
    setState(() {
      if (index != 3) {
        index += 1;
      } else {
        index = 0;
      }
    });
  }
}
