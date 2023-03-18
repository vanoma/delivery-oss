import 'package:expandable/expandable.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_card.dart';
import 'package:vanoma_driver/src/delivery/delivery_app_bar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/stop_view.dart';

class DeliveryView extends StatefulWidget {
  final Future<void> Function(double, double) openGoogleMapsNavigation;
  final BuildContext homeContext;

  const DeliveryView({
    Key? key,
    required this.openGoogleMapsNavigation,
    required this.homeContext,
  }) : super(key: key);

  @override
  _DeliveryViewState createState() => _DeliveryViewState();
}

class _DeliveryViewState extends State<DeliveryView> {
  final List<Map<String, ExpandableController>> _controllers = [];

  void _initializeControllers(int taskCount) {
    if (_controllers.isNotEmpty) {
      _controllers.clear();
    }
    for (int i = 0; i < taskCount; i++) {
      _controllers.add({'$i': ExpandableController()});
    }
    setState(() {});
  }

  void _handleToggleEffect(int index) {
    if (index < _controllers.length) {
      for (var controller in _controllers) {
        if (controller.values.first.expanded ||
            controller.keys.first == _controllers[index].keys.first) {
          controller.values.first.toggle();
        }
      }
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    final Size size = MediaQuery.of(context).size;
    DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);
    if (_controllers.isEmpty && deliveryController.stops.isNotEmpty ||
        _controllers.isNotEmpty &&
            _controllers.length != deliveryController.stops.length) {
      _initializeControllers(
        deliveryController.stops.length,
      );
    }

    return Padding(
      padding: const EdgeInsets.fromLTRB(8, 16, 8, 8),
      child: Column(
        children: <Widget>[
          DeliveryAppBar(stopsNumber: _controllers.length),
          const SizedBox(height: 8),
          !(deliveryController.isLoading && deliveryController.stops.isEmpty)
              ? ConstrainedBox(
                  constraints: BoxConstraints(
                    minHeight: 0,
                    maxHeight: size.height != 0 && size.width != 0
                        ? size.height - 88
                        : 0,
                  ),
                  child: deliveryController.stops.isNotEmpty
                      ? CustomCard(
                          child: ListView.builder(
                            shrinkWrap: true,
                            padding: EdgeInsets.zero,
                            itemCount: _controllers.length,
                            itemBuilder: (_, index) => StopView(
                              controller: _controllers[index].values.first,
                              stop: deliveryController.stops[index],
                              isFist: index == 0,
                              isLast: index == _controllers.length - 1,
                              active:
                                  index == deliveryController.currentStopIndex,
                              handleToggleEffect: _handleToggleEffect,
                              openGoogleMapsNavigation:
                                  widget.openGoogleMapsNavigation,
                              homeContext: widget.homeContext,
                              allControllers: _controllers,
                            ),
                          ),
                        )
                      : const Center(
                          child: Text('No assignment at the moment'),
                        ),
                )
              : Center(
                  child: Padding(
                    padding: const EdgeInsets.only(top: 48),
                    child: SpinKitThreeBounce(
                      size: 32,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ),
        ],
      ),
    );
  }
}
