import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:vanoma_driver/src/settings/settings_controller.dart';

class LanguagesView extends StatelessWidget {
  LanguagesView({Key? key, required this.settingsController}) : super(key: key);

  final SettingsController settingsController;

  final Map<String, String> _languages = {
    'en': 'English',
    'fr': 'Français',
    'rw': 'Kinyarwanda'
  };

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(24, 32, 0, 8),
          child: Text(
            'Choose language',
            style:
                Theme.of(context).textTheme.headline5!.copyWith(fontSize: 24),
          ),
        ),
        ...AppLocalizations.supportedLocales.map(
          (locale) {
            String title = _languages[locale.languageCode]!;
            return RadioListTile(
              title: Text(
                locale == const Locale('en') ? '$title default' : title,
              ),
              value: locale,
              groupValue: settingsController.language,
              onChanged: settingsController.updateLanguage,
              activeColor: Theme.of(context).colorScheme.primary,
            );
          },
        ).toList(),
        const SizedBox(height: 24)
      ],
    );
  }
}
