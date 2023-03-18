import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';
import 'package:vanoma_driver/src/geolocation/background_geolocation_service.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';

class StopActions extends StatefulWidget {
  const StopActions({
    Key? key,
    required this.stop,
    this.isFirst = false,
    this.active = false,
    required this.openGoogleMapsNavigation,
  }) : super(key: key);

  final bool isFirst;
  final bool active;
  final Stop stop;
  final Future<void> Function(double, double) openGoogleMapsNavigation;

  @override
  State<StopActions> createState() => _StopActionsState();
}

class _StopActionsState extends State<StopActions> {
  bool _isNavigateLoading = false;
  bool _isReachedLoading = false;

  @override
  Widget build(BuildContext context) {
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context, listen: false);
    final isConnected = context.watch<bool>();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: widget.stop.hasArrived || !widget.active
          ? const SizedBox()
          : Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                OutlinedButton(
                  onPressed: () async {
                    if (isConnected) {
                      try {
                        _seIsNavigateLoading();
                        if (!widget.stop.hasDeparted) {
                          await deliveryController
                              .confirmDeparture(widget.stop.stopId);
                        }
                        // BackgroundGeolocationService.changePaceToMoving();
                        await widget.openGoogleMapsNavigation(
                          widget.stop.address.latitude,
                          widget.stop.address.longitude,
                        );
                      } on HttpException catch (errorMessage) {
                        CustomSnackBar.buildErrorSnackbar(
                          context,
                          errorMessage.toString(),
                        );
                      }
                      _seIsNavigateLoading();
                    }
                  },
                  child: !_isNavigateLoading
                      ? Row(
                          children: <Widget>[
                            const Icon(Icons.navigation),
                            const SizedBox(width: 8),
                            Text(
                              widget.isFirst && !widget.stop.hasDeparted
                                  ? 'Start Trip'
                                  : 'Navigate',
                            ),
                          ],
                        )
                      : SpinKitThreeBounce(
                          size: 24,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                ),
                widget.stop.hasDeparted
                    ? OutlinedButton(
                        onPressed: () async {
                          if (isConnected) {
                            try {
                              _seIsReachedLoading();
                              await deliveryController
                                  .confirmArrival(widget.stop.stopId);
                            } on HttpException catch (errorMessage) {
                              CustomSnackBar.buildErrorSnackbar(
                                context,
                                errorMessage.toString(),
                              );
                            }
                            _seIsReachedLoading();
                          }
                        },
                        child: !_isReachedLoading
                            ? Row(
                                children: const <Widget>[
                                  Icon(Icons.sports_score),
                                  SizedBox(width: 8),
                                  Text('Reached'),
                                ],
                              )
                            : SpinKitThreeBounce(
                                size: 24,
                                color: Theme.of(context).colorScheme.primary,
                              ),
                      )
                    : const SizedBox()
              ],
            ),
    );
  }

  void _seIsNavigateLoading() {
    setState(() {
      _isNavigateLoading = !_isNavigateLoading;
    });
  }

  void _seIsReachedLoading() {
    setState(() {
      _isReachedLoading = !_isReachedLoading;
    });
  }
}
