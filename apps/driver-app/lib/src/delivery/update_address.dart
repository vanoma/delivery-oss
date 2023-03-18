import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_outlined_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/custom_widgets/form_input_field.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/address.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:flutter_background_geolocation/flutter_background_geolocation.dart'
    as bg;

class UpdateAddress extends StatefulWidget {
  const UpdateAddress({
    Key? key,
    required this.address,
    required this.homeContext,
    required this.handleDeliveryCompletion,
  }) : super(key: key);

  final Address address;
  final BuildContext homeContext;
  final VoidCallback handleDeliveryCompletion;

  @override
  _UpdateAddressState createState() => _UpdateAddressState();
}

class _UpdateAddressState extends State<UpdateAddress> {
  bool _update = false;
  bool _isLoading = false;
  String? _houseNumber;

  @override
  Widget build(BuildContext context) {
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);
    final isConnected = widget.homeContext.watch<bool>();

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 32),
      child: _update
          ? Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                Text(
                  'Are you sure you want to update?',
                  style: Theme.of(context).textTheme.headline5,
                ),
                const SizedBox(height: 16),
                widget.address.houseNumber == null ||
                        widget.address.houseNumber == ''
                    ? Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: FormInputField(
                          label: 'House number (optional)',
                          onChanged: (String value) {
                            setState(() {
                              _houseNumber = value;
                            });
                          },
                        ),
                      )
                    : const SizedBox(),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    OutlinedButton(
                      onPressed: () {
                        widget.handleDeliveryCompletion();
                        Navigator.of(context).pop();
                      },
                      child: const Text('No'),
                    ),
                    CustomOutlinedButton(
                      onPressed: () async {
                        if (isConnected) {
                          try {
                            _setLoading();
                            bg.Location currentPosition = await bg
                                .BackgroundGeolocation.getCurrentPosition(
                              desiredAccuracy: 10,
                              maximumAge: 5000,
                            );
                            await deliveryController.updateAddress(
                              widget.address.parentAddressId,
                              currentPosition.coords.latitude,
                              currentPosition.coords.longitude,
                              _houseNumber != '' ? _houseNumber : null,
                            );
                          } on HttpException catch (errorMessage) {
                            CustomSnackBar.buildErrorSnackbar(
                              context,
                              errorMessage.toString(),
                            );
                          }
                          widget.handleDeliveryCompletion();
                          _setLoading();
                          Navigator.of(context).pop();
                        }
                      },
                      text: 'Yes',
                      isLoading: _isLoading,
                    )
                  ],
                ),
              ],
            )
          : Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                Text(
                  'Was the address not accurate?',
                  style: Theme.of(context).textTheme.headline5,
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    OutlinedButton(
                      onPressed: () {
                        widget.handleDeliveryCompletion();
                        Navigator.of(context).pop();
                      },
                      child: const Text('No'),
                    ),
                    CustomOutlinedButton(
                      onPressed: () {
                        setState(() {
                          _update = true;
                        });
                      },
                      text: 'Update address',
                      isLoading: _isLoading,
                    )
                  ],
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
