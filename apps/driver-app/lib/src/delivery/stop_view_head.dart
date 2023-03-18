import 'package:expandable/expandable.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';

class StopHead extends StatelessWidget {
  const StopHead({
    Key? key,
    required this.controller,
    this.isLast = false,
    this.isFist = false,
    this.active = false,
    required this.stop,
    required this.handleToggleEffect,
  }) : super(key: key);

  final bool isFist;
  final bool isLast;
  final bool active;
  final Stop stop;
  final void Function(int) handleToggleEffect;
  final ExpandableController controller;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: active
            ? Theme.of(context).primaryColorLight.withOpacity(0.2)
            : stop.completedAllTasks
                ? Colors.green.withOpacity(0.2)
                : Colors.grey.withOpacity(.3),
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(isFist ? 15 : 0),
          bottom: Radius.circular(
            isLast && !controller.expanded ? 16 : 0,
          ),
        ),
      ),
      clipBehavior: Clip.hardEdge,
      child: ListTile(
        minLeadingWidth: 24,
        leading: Container(
          height: 32,
          width: 32,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Theme.of(context).primaryColor),
          ),
          child: Center(
            child: Text(
              '${stop.ranking + 1}',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: Theme.of(context).primaryColor,
                fontSize: 20,
              ),
            ),
          ),
        ),
        title: Text(
          stop.address.addressLine ?? 'No address name',
          style: Theme.of(context).textTheme.headline5?.copyWith(
                color: !active
                    ? Theme.of(context)
                        .textTheme
                        .headline5
                        ?.color
                        ?.withOpacity(.4)
                    : null,
              ),
        ),
        trailing: stop.completedAllTasks
            ? Icon(
                Icons.check_circle,
                color: Theme.of(context).iconTheme.color,
              )
            : const SizedBox(),
        onTap: () => handleToggleEffect(stop.ranking),
      ),
    );
  }
}
