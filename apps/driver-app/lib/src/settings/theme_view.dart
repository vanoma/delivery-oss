import 'package:flutter/material.dart';
import 'package:vanoma_driver/src/settings/settings_controller.dart';
import 'package:vanoma_driver/src/extensions/string_extension.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class ThemeView extends StatelessWidget {
  const ThemeView({Key? key, required this.settingsController})
      : super(key: key);

  final SettingsController settingsController;

  @override
  Widget build(BuildContext context) {
    final delegate = AppLocalizations.of(context)!;
    final Map<ThemeMode, String> _themes = {
      ThemeMode.system: delegate.systemDefault,
      ThemeMode.light: delegate.light,
      ThemeMode.dark: delegate.dark,
    };

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(24, 32, 0, 8),
          child: Text(
            ' delegate.chooseTheme',
            style:
                Theme.of(context).textTheme.headline5!.copyWith(fontSize: 24),
          ),
        ),
        ...ThemeMode.values.map(
          (mode) {
            return RadioListTile(
              title: Text(
                _themes[mode]!,
                style: Theme.of(context).textTheme.bodyText1,
              ),
              value: mode,
              groupValue: settingsController.themeMode,
              onChanged: settingsController.updateThemeMode,
              activeColor: Theme.of(context).colorScheme.primary,
              subtitle: mode == ThemeMode.system
                  ? Text(
                      MediaQuery.of(context)
                          .platformBrightness
                          .toString()
                          .substring(11)
                          .capitalize(),
                    )
                  : null,
            );
          },
        ).toList(),
        const SizedBox(height: 24)
      ],
    );
  }
}
