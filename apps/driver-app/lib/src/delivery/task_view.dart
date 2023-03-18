import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_card.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_icon_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_icons/custom_icons_icons.dart';
import 'package:vanoma_driver/src/delivery/confirm_pickup_or_dropoff.dart';
import 'package:vanoma_driver/src/delivery/models/task.dart';
import 'package:vanoma_driver/src/delivery/other_party.dart';

class TaskView extends StatefulWidget {
  const TaskView({
    Key? key,
    required this.index,
    required this.stopIndex,
    required this.isLast,
    required this.task,
    required this.handleToggleEffect,
    required this.homeContext,
    required this.hasArrived,
  }) : super(key: key);

  final int index;
  final int stopIndex;
  final bool isLast;
  final Task task;
  final void Function(int) handleToggleEffect;
  final BuildContext homeContext;
  final bool hasArrived;

  @override
  State<TaskView> createState() => _TaskViewState();
}

class _TaskViewState extends State<TaskView> {
  @override
  Widget build(BuildContext context) {
    return CustomCard(
      margin: const EdgeInsets.all(8),
      showShadow: false,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            height: 20,
            width: 20,
            color: Theme.of(context).primaryColorLight.withOpacity(0.4),
            child: Center(
                child: Text(
              '${widget.index + 1}',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: Theme.of(context).primaryColor,
              ),
            )),
          ),
          ListTile(
            minVerticalPadding: 0,
            minLeadingWidth: 24,
            leading: Icon(
              widget.task.type == TaskType.PICK_UP
                  ? CustomIcons.pickup
                  : CustomIcons.dropoff,
              color: Theme.of(context).iconTheme.color,
              size: 36,
            ),
            title: Text(
              widget.task.type == TaskType.PICK_UP ? 'Pick-up' : 'Drop-off',
              style: const TextStyle(
                fontStyle: FontStyle.italic,
                fontWeight: FontWeight.bold,
                fontSize: 18,
              ),
            ),
            subtitle: Text(widget.task.address.addressLine ?? ''),
            trailing: widget.task.isCompleted
                ? Icon(
                    Icons.check_circle,
                    color: Theme.of(context).iconTheme.color,
                  )
                : const SizedBox(),
          ),
          ListTile(
            title: Text(
                widget.task.contact.name ?? widget.task.contact.phoneNumberOne),
            subtitle: widget.task.contact.name != null
                ? Text(
                    widget.task.contact.phoneNumberOne,
                    style: Theme.of(context).textTheme.headline6,
                  )
                : null,
            trailing: CustomIconButton(
              color: Theme.of(context).primaryColor,
              icon: Icon(
                Icons.phone,
                color: Theme.of(context).iconTheme.color,
              ),
              onTap: () async {
                await launch('tel:${widget.task.contact.phoneNumberOne}');
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              widget.task.note ?? '',
              style:
                  Theme.of(context).textTheme.caption?.copyWith(fontSize: 16),
            ),
          ),
          Align(
            alignment: Alignment.centerRight,
            child: Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 12,
              ),
              child: widget.hasArrived && !widget.task.isCompleted
                  ? OutlinedButton(
                      onPressed: () {
                        showModalBottomSheet(
                          enableDrag: false,
                          isDismissible: false,
                          context: context,
                          builder: (_) => ConfirmPickupOrDropoff(
                            isLast: widget.isLast,
                            task: widget.task,
                            handleToggleEffect: widget.handleToggleEffect,
                            stopIndex: widget.stopIndex,
                            homeContext: widget.homeContext,
                          ),
                        );
                      },
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: <Widget>[
                          Icon(
                            widget.task.type == TaskType.PICK_UP
                                ? Icons.upload
                                : Icons.download,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            widget.task.type == TaskType.PICK_UP
                                ? 'Pick Up'
                                : 'Drop Off',
                          ),
                        ],
                      ))
                  : const SizedBox(),
            ),
          ),
          OtherParty(task: widget.task)
        ],
      ),
    );
  }
}
