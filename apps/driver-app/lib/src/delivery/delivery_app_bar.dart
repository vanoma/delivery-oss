import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/account/models/driver.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_icons/custom_icons_icons.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/top_button.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';

class DeliveryAppBar extends StatefulWidget {
  const DeliveryAppBar({Key? key, required this.stopsNumber}) : super(key: key);

  final int stopsNumber;

  @override
  State<DeliveryAppBar> createState() => _DeliveryAppBarState();
}

class _DeliveryAppBarState extends State<DeliveryAppBar> {
  bool _isLoading = false;
  Map<DriverStatusColor, Map<String, dynamic>> driverOnlineStatus = {
    DriverStatusColor.AVAILABLE: {'status': 'Online', 'color': 0xFF53D61B},
    DriverStatusColor.UNAVAILABLE: {'status': 'Offline', 'color': 0xFF454F5B},
  };

  @override
  Widget build(BuildContext context) {
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);
    final AccountController accountController =
        Provider.of<AccountController>(context);
    final isConnected = context.watch<bool>();

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: <Widget>[
        Builder(builder: (cxt) {
          return TopButton(
            child: Icon(
              CustomIcons.menu,
              color: Theme.of(context).colorScheme.onPrimary,
              size: 18,
            ),
            color: Theme.of(context).colorScheme.primary,
            width: 40,
            onTap: () => Scaffold.of(cxt).openDrawer(),
          );
        }),
        Row(
          children: <Widget>[
            deliveryController.stops.isNotEmpty
                ? TopButton(
                    child: Padding(
                      padding: const EdgeInsets.all(8),
                      child: Center(
                        child: Text.rich(
                          TextSpan(
                            children: <TextSpan>[
                              TextSpan(
                                text: '${widget.stopsNumber} ',
                                style: TextStyle(
                                  color:
                                      Theme.of(context).colorScheme.onSurface,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              const TextSpan(text: 'Stops'),
                            ],
                          ),
                        ),
                      ),
                    ),
                    color: Theme.of(context).colorScheme.surface,
                  )
                : const SizedBox(),
            const SizedBox(width: 8),
            TopButton(
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: Center(
                  child: !_isLoading
                      ? Text(
                          driverOnlineStatus[
                              accountController.driverStatusColor]!['status']!,
                          style: TextStyle(
                            color: Theme.of(context).colorScheme.onPrimary,
                            fontWeight: FontWeight.bold,
                          ),
                        )
                      : SpinKitThreeBounce(
                          size: 16,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                ),
              ),
              color: Color(_isLoading
                  ? driverOnlineStatus[DriverStatusColor.UNAVAILABLE]!['color']!
                  : driverOnlineStatus[accountController.driverStatusColor]![
                      'color']!),
              onTap: () async {
                if (isConnected) {
                  try {
                    _setLoading();
                    await accountController.updateDriverStatus(
                      !accountController.driver!.isAvailable,
                    );
                  } on HttpException catch (errorMessage) {
                    CustomSnackBar.buildErrorSnackbar(
                      context,
                      errorMessage.toString(),
                    );
                  }
                  _setLoading();
                }
              },
            ),
          ],
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
