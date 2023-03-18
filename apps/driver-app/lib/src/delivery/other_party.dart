import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_icon_button.dart';
import 'package:vanoma_driver/src/delivery/models/task.dart';

class OtherParty extends StatelessWidget {
  const OtherParty({Key? key, required this.task}) : super(key: key);

  final Task task;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(left: 16),
      decoration: BoxDecoration(
        color: Colors.grey.withOpacity(.2),
        borderRadius: const BorderRadius.only(
          bottomRight: Radius.circular(16),
        ).copyWith(topLeft: const Radius.circular(16)),
      ),
      clipBehavior: Clip.hardEdge,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  task.type == TaskType.PICK_UP ? 'To' : 'From',
                  style: const TextStyle(
                    fontStyle: FontStyle.italic,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                CustomIconButton(
                  color: const Color(0xFF454F5B),
                  icon: const Icon(
                    Icons.zoom_in,
                    color: Color(0xFF454F5B),
                    size: 20,
                  ),
                  onTap: () {
                    showModalBottomSheet(
                      context: context,
                      builder: (_) => Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            height: 4,
                            width: 48,
                            margin: const EdgeInsets.all(12),
                            decoration: const BoxDecoration(
                              color: Colors.grey,
                              borderRadius: BorderRadius.all(
                                Radius.circular(1),
                              ),
                            ),
                          ),
                          Padding(
                            padding: const EdgeInsets.symmetric(vertical: 72),
                            child: Text(
                              task.otherParty.phoneNumberOne,
                              style: const TextStyle(
                                fontSize: 48,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
          ListTile(
            title: Text(
              task.otherParty.name ?? task.otherParty.phoneNumberOne,
              style: Theme.of(context).textTheme.bodyText1!.copyWith(
                    color: Theme.of(context)
                        .textTheme
                        .bodyText1!
                        .color!
                        .withOpacity(0.5),
                  ),
            ),
            subtitle: task.otherParty.name != null
                ? Text(
                    task.otherParty.phoneNumberOne,
                    style: Theme.of(context).textTheme.headline6!.copyWith(
                          color: Theme.of(context)
                              .textTheme
                              .headline6!
                              .color!
                              .withOpacity(0.5),
                        ),
                  )
                : null,
            trailing: CustomIconButton(
              color: const Color(0xFF454F5B),
              icon: const Icon(
                Icons.phone,
                color: Color(0xFF454F5B),
                size: 16,
              ),
              onTap: () async {
                await launch('tel:${task.otherParty.phoneNumberOne}');
              },
            ),
          ),
        ],
      ),
    );
  }
}
