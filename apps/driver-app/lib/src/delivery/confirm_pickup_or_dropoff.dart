import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_outlined_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/address.dart';
import 'package:vanoma_driver/src/delivery/models/task.dart';
import 'package:vanoma_driver/src/delivery/update_address.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';

class ConfirmPickupOrDropoff extends StatefulWidget {
  const ConfirmPickupOrDropoff({
    Key? key,
    required this.isLast,
    required this.task,
    required this.handleToggleEffect,
    required this.stopIndex,
    required this.homeContext,
  }) : super(key: key);

  final bool isLast;
  final Task task;
  final void Function(int) handleToggleEffect;
  final int stopIndex;
  final BuildContext homeContext;

  @override
  _ConfirmPickupOrDropoffState createState() => _ConfirmPickupOrDropoffState();
}

class _ConfirmPickupOrDropoffState extends State<ConfirmPickupOrDropoff> {
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);
    final isConnected = widget.homeContext.watch<bool>();
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(
            'Confirm ${widget.task.type == TaskType.PICK_UP ? 'Pick Up' : 'Drop Off'}',
            style: Theme.of(context).textTheme.headline5,
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              OutlinedButton(
                onPressed: () {
                  Navigator.of(context).pop();
                },
                child: const Text('No'),
              ),
              CustomOutlinedButton(
                onPressed: () async {
                  if (isConnected) {
                    try {
                      Address addressToBeUpdated = widget.task.address;
                      _setLoading();
                      bool isDone = await deliveryController
                          .completeTask(widget.task.taskId);
                      Navigator.of(context).pop();
                      if (!addressToBeUpdated.isConfirmed) {
                        showModalBottomSheet(
                          enableDrag: false,
                          isDismissible: false,
                          isScrollControlled: true,
                          context: widget.homeContext,
                          builder: (context) => Padding(
                            padding: EdgeInsets.only(
                              bottom: MediaQuery.of(context).viewInsets.bottom,
                            ),
                            child: UpdateAddress(
                              address: addressToBeUpdated,
                              homeContext: widget.homeContext,
                              handleDeliveryCompletion: () =>
                                  _handleDeliveryCompletion(
                                isDone,
                                widget.homeContext,
                              ),
                            ),
                          ),
                        );
                      } else {
                        _handleDeliveryCompletion(isDone, widget.homeContext);
                      }
                    } on HttpException catch (errorMessage) {
                      CustomSnackBar.buildErrorSnackbar(
                        context,
                        errorMessage.toString(),
                      );
                      Navigator.of(context).pop();
                    }
                    _setLoading();
                  }
                },
                text: 'Yes',
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

  void _handleDeliveryCompletion(bool isDone, BuildContext homeContext) {
    if (isDone) {
      widget.handleToggleEffect(widget.stopIndex);
      if (widget.isLast) {
        CustomSnackBar.buildSuccessSnackBar(
          homeContext,
          "You have completed the delivery!",
        );
      } else {
        widget.handleToggleEffect(widget.stopIndex + 1);
      }
    }
  }
}
