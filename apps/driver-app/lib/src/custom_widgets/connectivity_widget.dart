import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vanoma_driver/src/custom_widgets/loading_screen.dart';
import 'package:vanoma_driver/src/utils/connection_util.dart';

class ConnectivityWidget extends StatefulWidget {
  const ConnectivityWidget(
      {Key? key, required this.builder, required this.connectionStatus})
      : super(key: key);
  final Widget Function(BuildContext context) builder;
  final ConnectionUtil connectionStatus;

  @override
  _ConnectivityWidgetState createState() => _ConnectivityWidgetState();
}

class _ConnectivityWidgetState extends State<ConnectivityWidget>
    with SingleTickerProviderStateMixin {
  bool? dontAnimate;
  late AnimationController animationController;

  @override
  void initState() {
    animationController = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
    if (dontAnimate == null && !widget.connectionStatus.hasConnection) {
      animationController.value = 1.0;
    }
    widget.connectionStatus.connectionChange.listen((status) {
      if (dontAnimate == null) {
        dontAnimate = true;
        if (!widget.connectionStatus.hasConnection) {
          animationController.value = 1.0;
        }
        return;
      }
      if (!status) {
        animationController.forward();
      } else {
        animationController.reverse();
      }
      dontAnimate = true;
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<bool>(
      initialData: widget.connectionStatus.hasConnection,
      stream: widget.connectionStatus.connectionChange,
      builder: (context, snapshot) {
        return Stack(
          alignment: Alignment.center,
          children: <Widget>[
            StreamProvider<bool>(
                initialData: widget.connectionStatus.hasConnection,
                create: (context) => widget.connectionStatus.connectionChange,
                child: widget.builder(context)),
            if (!(snapshot.data ?? false))
              Align(
                alignment: Alignment.bottomCenter,
                child: SlideTransition(
                  position: animationController.drive(Tween<Offset>(
                    begin: const Offset(0.0, 1.0),
                    end: Offset.zero,
                  ).chain(CurveTween(
                    curve: Curves.fastOutSlowIn,
                  ))),
                  child: Material(
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      width: double.infinity,
                      color: Theme.of(context).colorScheme.error,
                      child: Text(
                        'No internet connection',
                        style: TextStyle(
                            fontSize: 16,
                            color: Theme.of(context).colorScheme.onError,
                            fontWeight: FontWeight.bold),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                ),
              )
          ],
        );
      },
    );
  }
}
