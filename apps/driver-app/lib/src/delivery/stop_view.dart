import 'package:expandable/expandable.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';
import 'package:vanoma_driver/src/delivery/stop_view_body.dart';
import 'package:vanoma_driver/src/delivery/stop_view_head.dart';

class StopView extends StatefulWidget {
  final bool isFist;
  final bool isLast;
  final bool active;
  final Stop stop;
  final void Function(int) handleToggleEffect;
  final Future<void> Function(double, double) openGoogleMapsNavigation;
  final BuildContext homeContext;
  final ExpandableController controller;
  final List<Map<String, ExpandableController>> allControllers;

  const StopView({
    Key? key,
    required this.controller,
    required this.stop,
    this.isLast = false,
    this.isFist = false,
    this.active = false,
    required this.handleToggleEffect,
    required this.openGoogleMapsNavigation,
    required this.homeContext,
    required this.allControllers,
  }) : super(key: key);

  @override
  State<StopView> createState() => _StopViewState();
}

class _StopViewState extends State<StopView> {
  @override
  void initState() {
    int currentStopIndex =
        Provider.of<DeliveryController>(context, listen: false)
            .currentStopIndex;
    int expandedIndex = widget.allControllers
        .indexWhere((_controller) => _controller.values.first.expanded);

    if (expandedIndex == -1 && currentStopIndex == widget.stop.ranking) {
      Future.delayed(const Duration(milliseconds: 250), () {
        widget.handleToggleEffect(widget.stop.ranking);
      });
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return ExpandableNotifier(
      controller: widget.controller,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          StopHead(
            controller: widget.controller,
            stop: widget.stop,
            handleToggleEffect: widget.handleToggleEffect,
            isFist: widget.isFist,
            isLast: widget.isLast,
            active: widget.active,
          ),
          Expandable(
            collapsed: Container(),
            expanded: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Divider(
                  color: Theme.of(context).colorScheme.secondaryVariant,
                ),
                widget.controller.expanded
                    ? StopViewBody(
                        stop: widget.stop,
                        handleToggleEffect: widget.handleToggleEffect,
                        isFirst: widget.isFist,
                        isLast: widget.isLast,
                        active: widget.active,
                        openGoogleMapsNavigation:
                            widget.openGoogleMapsNavigation,
                        homeContext: widget.homeContext,
                      )
                    : const SizedBox(),
              ],
            ),
          ),
          !widget.isLast
              ? Divider(
                  color: Theme.of(context).colorScheme.secondaryVariant,
                )
              : const SizedBox(),
        ],
      ),
    );
  }
}
