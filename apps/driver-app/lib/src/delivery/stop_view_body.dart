import 'package:flutter/material.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';
import 'package:vanoma_driver/src/delivery/stop_actions.dart';
import 'package:vanoma_driver/src/delivery/task_view.dart';

class StopViewBody extends StatelessWidget {
  final Stop stop;
  final void Function(int) handleToggleEffect;
  final bool isFirst;
  final bool isLast;
  final bool active;
  final BuildContext homeContext;
  final Future<void> Function(double, double) openGoogleMapsNavigation;

  const StopViewBody({
    Key? key,
    required this.stop,
    required this.handleToggleEffect,
    required this.isFirst,
    required this.isLast,
    required this.active,
    required this.homeContext,
    required this.openGoogleMapsNavigation,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
          child: Text(
            'Tasks',
            style: Theme.of(context).textTheme.headline5,
          ),
        ),
        ListView.builder(
          padding: EdgeInsets.zero,
          physics: const NeverScrollableScrollPhysics(),
          shrinkWrap: true,
          itemCount: stop.tasks.length,
          itemBuilder: (context, index) => TaskView(
            index: index,
            stopIndex: stop.ranking,
            task: stop.tasks[index],
            isLast: isLast,
            handleToggleEffect: handleToggleEffect,
            homeContext: homeContext,
            hasArrived: stop.hasArrived,
          ),
        ),
        stop.tasks.isEmpty
            ? const SizedBox()
            : StopActions(
                stop: stop,
                openGoogleMapsNavigation: openGoogleMapsNavigation,
                isFirst: isFirst,
                active: active,
              )
      ],
    );
  }
}
