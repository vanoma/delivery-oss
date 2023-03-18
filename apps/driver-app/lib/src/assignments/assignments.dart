import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/contained_button.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_card.dart';
import 'package:vanoma_driver/src/custom_widgets/custom_snackbar.dart';
import 'package:vanoma_driver/src/delivery/delivery_controller.dart';
import 'package:vanoma_driver/src/delivery/models/package.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';

class Assignments extends StatefulWidget {
  const Assignments({Key? key}) : super(key: key);

  @override
  State<Assignments> createState() => _AssignmentsState();
}

class _AssignmentsState extends State<Assignments> {
  bool _isLoading = false;

  @override
  void initState() {
    final DeliveryController deliveryController =
        Provider.of<DeliveryController>(context, listen: false);
    Future.delayed(Duration.zero, deliveryController.fetchAssignments);

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final DeliveryController deliveryController =
        Provider.of<DeliveryController>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Assignments'),
        centerTitle: true,
      ),
      body: !deliveryController.isLoading
          ? ListView(
              padding: const EdgeInsets.all(8),
              children: [
                ListView.builder(
                  physics: const NeverScrollableScrollPhysics(),
                  shrinkWrap: true,
                  itemCount: deliveryController.assignments.length,
                  itemBuilder: (context, index) {
                    Package package =
                        deliveryController.assignments[index].package;
                    return CustomCard(
                      margin: const EdgeInsets.only(bottom: 16),
                      child: ListTile(
                        title: Row(
                          children: <Widget>[
                            _Stop(
                              title: package.fromContact.name ??
                                  package.fromContact.phoneNumberOne,
                              subtitle: package.fromAddress.streetName,
                              alignment: CrossAxisAlignment.start,
                            ),
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 8.0),
                              child: Icon(Icons.east),
                            ),
                            _Stop(
                              title: package.toContact.name ??
                                  package.toContact.phoneNumberOne,
                              subtitle: package.toAddress.streetName,
                              alignment: CrossAxisAlignment.end,
                            ),
                          ],
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                            horizontal: 16, vertical: 16),
                      ),
                    );
                  },
                ),
                deliveryController.assignments.isNotEmpty
                    ? ContainedButton(
                        text: 'Confirm',
                        onPressed: () async {
                          _setLoading();
                          try {
                            await deliveryController.confirmAssignments();
                          } on HttpException catch (errorMessage) {
                            deliveryController.clearAssignments();
                            CustomSnackBar.buildErrorSnackbar(
                              context,
                              errorMessage.toString(),
                            );
                          }
                          _setLoading();
                          Navigator.of(context).pop();
                        },
                        isLoading: _isLoading,
                      )
                    : Column(
                        children: [
                          const Center(
                            child: Padding(
                              padding: EdgeInsets.symmetric(vertical: 32),
                              child: Text('There is no new assignment.'),
                            ),
                          ),
                          ContainedButton(
                            text: 'Go back',
                            onPressed: () async {
                              Navigator.of(context).pop();
                            },
                          ),
                        ],
                      )
              ],
            )
          : Center(
              child: SpinKitThreeBounce(
                size: 32,
                color: Theme.of(context).colorScheme.primary,
              ),
            ),
    );
  }

  void _setLoading() {
    setState(() {
      _isLoading = !_isLoading;
    });
  }
}

class _Stop extends StatelessWidget {
  const _Stop({
    Key? key,
    required this.title,
    required this.subtitle,
    required this.alignment,
  }) : super(key: key);

  final String title;
  final String subtitle;
  final CrossAxisAlignment alignment;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        crossAxisAlignment: alignment,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          Text(
            subtitle,
            style: TextStyle(
              color: Theme.of(context).textTheme.caption!.color,
            ),
          ),
        ],
      ),
    );
  }
}
