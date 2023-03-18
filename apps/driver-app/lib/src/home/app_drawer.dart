import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/route_names.dart';
import 'package:vanoma_driver/src/settings/languages_view.dart';
import 'package:vanoma_driver/src/settings/settings_controller.dart';
import 'package:vanoma_driver/src/settings/theme_view.dart';

class AppDrawer extends StatelessWidget {
  const AppDrawer({
    Key? key,
    required this.settingsController,
  }) : super(key: key);

  final SettingsController settingsController;

  @override
  Widget build(BuildContext context) {
    final delegate = AppLocalizations.of(context)!;
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);
    AccountController accountController = Provider.of<AccountController>(
      context,
    );
    final isConnected = context.watch<bool>();

    return Drawer(
      child: SafeArea(
        child: ListView(
          children: <Widget>[
            SizedBox(
              height: 160,
              child: DrawerHeader(
                padding: const EdgeInsets.fromLTRB(16.0, 16.0, 16.0, 0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    const CircleAvatar(
                      backgroundImage: AssetImage('assets/images/avatar.png'),
                      radius: 28,
                      backgroundColor: Colors.grey,
                    ),
                    ListTile(
                      title: Text(
                        '${accountController.driver?.firstName} ${accountController.driver?.lastName}',
                        style: Theme.of(context)
                            .textTheme
                            .headline5!
                            .copyWith(overflow: TextOverflow.ellipsis),
                      ),
                      subtitle: Text(delegate.driver),
                      contentPadding: EdgeInsets.zero,
                    ),
                  ],
                ),
              ),
            ),
            ListTile(
              title: Text(delegate.assignments),
              leading: Icon(
                Icons.assessment,
                color: Theme.of(context).iconTheme.color,
              ),
              trailing: deliveryController.isLoading
                  ? SizedBox(
                      height: 24,
                      width: 24,
                      child: SpinKitThreeBounce(
                        size: 16,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                    )
                  : const SizedBox(),
              selected: deliveryController.isLoading,
              onTap: () async {
                if (isConnected) {
                  Navigator.of(context).pop();
                  await Navigator.pushNamed(context, '/assignments');
                  deliveryController.fetchStops();
                }
              },
            ),
            ListTile(
              title: Text(delegate.account),
              leading: Icon(
                Icons.account_circle,
                color: Theme.of(context).iconTheme.color,
              ),
              onTap: () => Navigator.of(context).pushNamed(account),
            ),
            ListTile(
              title: Text(delegate.languages),
              leading: Icon(
                Icons.language,
                color: Theme.of(context).iconTheme.color,
              ),
              onTap: () {
                showModalBottomSheet(
                  enableDrag: true,
                  context: context,
                  builder: (context) => LanguagesView(
                    settingsController: settingsController,
                  ),
                );
              },
            ),
            ListTile(
              title: Text(delegate.theme),
              leading: Icon(
                Icons.brightness_6,
                color: Theme.of(context).iconTheme.color,
              ),
              onTap: () {
                showModalBottomSheet(
                  enableDrag: true,
                  context: context,
                  builder: (context) => ThemeView(
                    settingsController: settingsController,
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
